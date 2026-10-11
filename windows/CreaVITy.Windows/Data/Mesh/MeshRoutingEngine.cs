using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using CreaVITy.Windows.Domain.Models;

namespace CreaVITy.Windows.Data.Mesh
{
    // ---------------------------------------------------------------------------
    // InboundResult — discriminated union via sealed abstract record hierarchy
    // ---------------------------------------------------------------------------
    public abstract record InboundResult
    {
        public sealed record DeliverAndForward(MeshEnvelope Envelope, List<string> TargetNeighbors) : InboundResult;
        public sealed record DeliverOnly(MeshEnvelope Envelope) : InboundResult;
        public sealed record DroppedDuplicate : InboundResult;
        public sealed record DroppedTtlExpired : InboundResult;
        public sealed record DroppedUnauthorized : InboundResult;

        // Singleton accessors — avoids repeated allocation for terminal cases
        public static readonly InboundResult Duplicate    = new DroppedDuplicate();
        public static readonly InboundResult TtlExpired   = new DroppedTtlExpired();
        public static readonly InboundResult Unauthorized = new DroppedUnauthorized();

        private InboundResult() { }  // Prevent external subclassing
    }

    // ---------------------------------------------------------------------------
    // LRU cache: Dictionary<TKey,TValue> + LinkedList for access-order eviction
    // ---------------------------------------------------------------------------
    internal sealed class LruCache<TKey, TValue> where TKey : notnull
    {
        private readonly int _capacity;
        private readonly Dictionary<TKey, LinkedListNode<(TKey Key, TValue Value)>> _map;
        private readonly LinkedList<(TKey Key, TValue Value)> _order;

        public LruCache(int capacity)
        {
            _capacity = capacity;
            _map      = new Dictionary<TKey, LinkedListNode<(TKey, TValue)>>(capacity);
            _order    = new LinkedList<(TKey, TValue)>();
        }

        public bool ContainsKey(TKey key) => _map.ContainsKey(key);

        public void Set(TKey key, TValue value)
        {
            if (_map.TryGetValue(key, out var node))
            {
                _order.Remove(node);
                node.Value = (key, value);
                _order.AddFirst(node);
            }
            else
            {
                if (_map.Count >= _capacity && _order.Last is { } last)
                {
                    _map.Remove(last.Value.Key);
                    _order.RemoveLast();
                }
                var newNode = new LinkedListNode<(TKey, TValue)>((key, value));
                _order.AddFirst(newNode);
                _map[key] = newNode;
            }
        }

        /// <summary>Retrieves a value, promoting it to MRU position.</summary>
        public bool TryGet(TKey key, out TValue value)
        {
            if (_map.TryGetValue(key, out var node))
            {
                _order.Remove(node);
                _order.AddFirst(node);
                value = node.Value.Value;
                return true;
            }
            value = default!;
            return false;
        }

        public void Clear()
        {
            _map.Clear();
            _order.Clear();
        }
    }

    // ---------------------------------------------------------------------------
    // MeshRoutingEngine
    // ---------------------------------------------------------------------------
    public sealed class MeshRoutingEngine
    {
        public string NodeId    { get; }
        public int    MaxDegree { get; }
        public string? SecretKey { get; set; }

        private readonly object _lock = new();
        private long _seqCounter = 0;

        private readonly HashSet<string>                      _activeNeighbors = new();
        private readonly LruCache<long, long>                 _seenPacketIds   = new(4096);
        private readonly Dictionary<string, PermissionTicket> _peerTickets     = new();

        public MeshRoutingEngine(string nodeId, int maxDegree = 6, string? secretKey = null)
        {
            NodeId    = nodeId;
            MaxDegree = maxDegree;
            SecretKey = secretKey;
        }

        public bool AddNeighbor(string peerId)
        {
            lock (_lock)
            {
                if (peerId == NodeId)                      return false;
                if (_activeNeighbors.Count >= MaxDegree)   return false;
                return _activeNeighbors.Add(peerId);
            }
        }

        public void RemoveNeighbor(string peerId)
        {
            lock (_lock)
            {
                _activeNeighbors.Remove(peerId);
                _peerTickets.Remove(peerId);
            }
        }

        public List<string> GetNeighbors()
        {
            lock (_lock) { return _activeNeighbors.ToList(); }
        }

        public void RegisterTicket(PermissionTicket ticket)
        {
            lock (_lock) { _peerTickets[ticket.DeviceId] = ticket; }
        }

        public PermissionTicket? GetTicket(string deviceId)
        {
            lock (_lock) { return _peerTickets.TryGetValue(deviceId, out var t) ? t : null; }
        }

        public (MeshEnvelope Envelope, List<string> Targets) CreateOutbound(
            byte[] payload, bool isMutation, PermissionTicket? ticket = null)
        {
            lock (_lock)
            {
                long now      = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
                long packetId = (now << 20) | (Interlocked.Increment(ref _seqCounter) & 0xFFFFF);

                var envelope = new MeshEnvelope(
                    PacketId:     packetId,
                    OriginNodeId: NodeId,
                    SenderNodeId: NodeId,
                    Ttl:          12,
                    HopCount:     0,
                    RoleToken:    ticket?.Signature ?? string.Empty,
                    IsMutation:   isMutation,
                    Payload:      payload
                );

                _seenPacketIds.Set(packetId, DateTimeOffset.UtcNow.ToUnixTimeMilliseconds());
                return (envelope, _activeNeighbors.ToList());
            }
        }

        public InboundResult ProcessInbound(MeshEnvelope envelope)
        {
            lock (_lock)
            {
                if (envelope.Ttl <= 0)
                    return InboundResult.TtlExpired;

                if (_seenPacketIds.ContainsKey(envelope.PacketId) || envelope.OriginNodeId == NodeId)
                    return InboundResult.Duplicate;

                _seenPacketIds.Set(envelope.PacketId, DateTimeOffset.UtcNow.ToUnixTimeMilliseconds());

                if (envelope.IsMutation)
                {
                    var key = SecretKey;
                    if (key is not null && _peerTickets.TryGetValue(envelope.OriginNodeId, out var ticket))
                    {
                        if (!MeshAccessController.CanWrite(ticket) ||
                            !MeshAccessController.ValidateTicket(key, ticket))
                        {
                            return InboundResult.Unauthorized;
                        }
                    }
                }

                byte nextTtl    = (byte)(envelope.Ttl - 1);
                var  targets    = _activeNeighbors.Where(n => n != envelope.SenderNodeId).ToList();

                if (nextTtl > 0 && targets.Count > 0)
                {
                    var forwarded = envelope with
                    {
                        SenderNodeId = NodeId,
                        Ttl          = nextTtl,
                        HopCount     = (byte)(envelope.HopCount + 1)
                    };
                    return new InboundResult.DeliverAndForward(forwarded, targets);
                }

                return new InboundResult.DeliverOnly(envelope);
            }
        }

        public bool IsAtCapacity()
        {
            lock (_lock) { return _activeNeighbors.Count >= MaxDegree; }
        }

        public void Clear()
        {
            lock (_lock)
            {
                _activeNeighbors.Clear();
                _seenPacketIds.Clear();
                _peerTickets.Clear();
            }
        }
    }
}
