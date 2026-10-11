using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using CreaVITy.Windows.Data.Binary;
using CreaVITy.Windows.Domain.Models;

namespace CreaVITy.Windows.Data.Mesh;

/// <summary>
/// Cross-platform LAN peer-to-peer transport using UDP beacon discovery
/// and TCP binary framed socket streams. Interoperable with Android CreaVITy.
/// </summary>
public sealed class LanMeshTransport : IDisposable
{
    public const int DiscoveryPort = 53535;
    public const int DefaultTcpPort = 53536;
    private const string BeaconMagic = "CREAVITY_BEACON";

    public string LocalNodeId { get; } = Guid.NewGuid().ToString();
    public MeshRoutingEngine RoutingEngine { get; }

    private readonly ConcurrentDictionary<string, PeerConnection> _connectedPeers = new();
    private readonly ConcurrentDictionary<string, DiscoveredPeerInfo> _discoveredPeers = new();

    private UdpClient? _udpListener;
    private TcpListener? _tcpListener;

    private CancellationTokenSource? _advCts;
    private CancellationTokenSource? _discCts;
    private CancellationTokenSource? _listenCts;

    private int _boundTcpPort = DefaultTcpPort;
    private bool _isDisposed;

    public bool IsAdvertising => _advCts != null && !_advCts.IsCancellationRequested;
    public bool IsDiscovering => _discCts != null && !_discCts.IsCancellationRequested;

    public event Action<List<PeerDevice>>? DiscoveredPeersChanged;
    public event Action<List<PeerDevice>>? ConnectedPeersChanged;
    public event Action<WhiteboardPacket>? PacketReceived;

    public LanMeshTransport()
    {
        RoutingEngine = new MeshRoutingEngine(LocalNodeId, maxDegree: 6);
    }

    public void StartAdvertising(string deviceName, string roomCode)
    {
        StopAdvertising();
        EnsureTcpServerStarted();

        _advCts = new CancellationTokenSource();
        var token = _advCts.Token;

        Task.Run(async () =>
        {
            using var udp = new UdpClient();
            udp.EnableBroadcast = true;
            var targetEp = new IPEndPoint(IPAddress.Broadcast, DiscoveryPort);

            while (!token.IsCancellationRequested)
            {
                try
                {
                    var beaconText = $"{BeaconMagic}\n{LocalNodeId}\n{deviceName}\n{roomCode}\n{_boundTcpPort}\nHOST";
                    var bytes = Encoding.UTF8.GetBytes(beaconText);
                    await udp.SendAsync(bytes, bytes.Length, targetEp);
                }
                catch
                {
                    // Ignore broadcast transient errors
                }

                try
                {
                    await Task.Delay(1500, token);
                }
                catch (OperationCanceledException)
                {
                    break;
                }
            }
        }, token);
    }

    public void StopAdvertising()
    {
        _advCts?.Cancel();
        _advCts = null;
    }

    public void StartDiscovery()
    {
        StopDiscovery();

        _discCts = new CancellationTokenSource();
        var token = _discCts.Token;

        try
        {
            _udpListener = new UdpClient();
            _udpListener.Client.SetSocketOption(SocketOptionLevel.Socket, SocketOptionName.ReuseAddress, true);
            _udpListener.Client.Bind(new IPEndPoint(IPAddress.Any, DiscoveryPort));
        }
        catch
        {
            // Fallback without reuse address if not supported
            _udpListener = new UdpClient(DiscoveryPort);
        }

        Task.Run(async () =>
        {
            while (!token.IsCancellationRequested && _udpListener != null)
            {
                try
                {
                    var result = await _udpListener.ReceiveAsync(token);
                    var text = Encoding.UTF8.GetString(result.Buffer);
                    var lines = text.Split('\n');
                    if (lines.Length >= 6 && lines[0] == BeaconMagic)
                    {
                        var nodeId = lines[1].Trim();
                        if (nodeId == LocalNodeId) continue; // Ignore self

                        var name = lines[2].Trim();
                        var roomCode = lines[3].Trim();
                        if (!int.TryParse(lines[4].Trim(), out var remotePort)) remotePort = DefaultTcpPort;
                        var roleStr = lines[5].Trim();

                        var ip = result.RemoteEndPoint.Address.ToString();
                        var endpointId = $"lan_{ip}_{remotePort}";

                        var peer = new DiscoveredPeerInfo(
                            endpointId,
                            name,
                            ip,
                            remotePort,
                            roomCode,
                            roleStr.Equals("HOST", StringComparison.OrdinalIgnoreCase) ? MeshRole.Host : MeshRole.Viewer,
                            DateTime.UtcNow
                        );

                        _discoveredPeers[endpointId] = peer;
                        NotifyDiscoveredPeers();
                    }
                }
                catch (OperationCanceledException)
                {
                    break;
                }
                catch
                {
                    // Network transient error
                }
            }
        }, token);

        // Pruning timer
        Task.Run(async () =>
        {
            while (!token.IsCancellationRequested)
            {
                try
                {
                    await Task.Delay(3000, token);
                    var now = DateTime.UtcNow;
                    var removed = false;
                    foreach (var kvp in _discoveredPeers)
                    {
                        if ((now - kvp.Value.LastSeen).TotalSeconds > 7)
                        {
                            if (_discoveredPeers.TryRemove(kvp.Key, out _)) removed = true;
                        }
                    }
                    if (removed) NotifyDiscoveredPeers();
                }
                catch (OperationCanceledException)
                {
                    break;
                }
                catch { }
            }
        }, token);
    }

    public void StopDiscovery()
    {
        _discCts?.Cancel();
        _discCts = null;
        try { _udpListener?.Close(); } catch { }
        _udpListener = null;
        _discoveredPeers.Clear();
        NotifyDiscoveredPeers();
    }

    private void EnsureTcpServerStarted()
    {
        if (_tcpListener != null) return;

        int port = DefaultTcpPort;
        while (port < DefaultTcpPort + 10)
        {
            try
            {
                _tcpListener = new TcpListener(IPAddress.Any, port);
                _tcpListener.Start();
                _boundTcpPort = port;
                break;
            }
            catch
            {
                port++;
            }
        }

        if (_tcpListener == null) return;

        _listenCts = new CancellationTokenSource();
        var token = _listenCts.Token;

        Task.Run(async () =>
        {
            while (!token.IsCancellationRequested && _tcpListener != null)
            {
                try
                {
                    var client = await _tcpListener.AcceptTcpClientAsync(token);
                    var remoteEp = client.Client.RemoteEndPoint as IPEndPoint;
                    var endpointId = remoteEp != null ? $"lan_{remoteEp.Address}_{remoteEp.Port}" : $"lan_client_{Guid.NewGuid()}";
                    var peerName = $"Peer ({remoteEp?.Address})";

                    var peerConn = new PeerConnection(endpointId, peerName, client);
                    _connectedPeers[endpointId] = peerConn;
                    RoutingEngine.AddNeighbor(endpointId);
                    NotifyConnectedPeers();

                    StartPeerReader(peerConn);

                    // Send initial SyncRequest
                    SendPacketDirect(peerConn, new WhiteboardPacket.SyncRequest());
                }
                catch (OperationCanceledException)
                {
                    break;
                }
                catch { }
            }
        }, token);
    }

    public async Task<bool> ConnectToPeerAsync(string endpointId, string localName)
    {
        if (!_discoveredPeers.TryGetValue(endpointId, out var info)) return false;
        try
        {
            var client = new TcpClient();
            await client.ConnectAsync(info.IpAddress, info.Port);

            var peerConn = new PeerConnection(endpointId, info.Name, client);
            _connectedPeers[endpointId] = peerConn;
            RoutingEngine.AddNeighbor(endpointId);
            NotifyConnectedPeers();

            StartPeerReader(peerConn);

            // Request sync immediately
            SendPacketDirect(peerConn, new WhiteboardPacket.SyncRequest());
            return true;
        }
        catch
        {
            return false;
        }
    }

    private void StartPeerReader(PeerConnection peer)
    {
        Task.Run(async () =>
        {
            var stream = peer.Client.GetStream();
            var lenBuffer = new byte[4];

            try
            {
                while (!_isDisposed && peer.Client.Connected)
                {
                    // Read 4-byte big endian length
                    if (!await ReadExactAsync(stream, lenBuffer, 4)) break;
                    int length = (lenBuffer[0] << 24) | (lenBuffer[1] << 16) | (lenBuffer[2] << 8) | lenBuffer[3];
                    if (length <= 0 || length > 10 * 1024 * 1024) break; // 10MB sanity cap

                    var payload = new byte[length];
                    if (!await ReadExactAsync(stream, payload, length)) break;

                    ProcessInboundBytes(peer.EndpointId, payload);
                }
            }
            catch { }
            finally
            {
                DisconnectFromPeer(peer.EndpointId);
            }
        });
    }

    private static async Task<bool> ReadExactAsync(NetworkStream stream, byte[] buffer, int count)
    {
        int totalRead = 0;
        while (totalRead < count)
        {
            int read = await stream.ReadAsync(buffer.AsMemory(totalRead, count - totalRead));
            if (read == 0) return false;
            totalRead += read;
        }
        return true;
    }

    private void ProcessInboundBytes(string senderEndpointId, byte[] bytes)
    {
        var envelope = MeshEnvelope.FromByteArray(bytes);
        if (envelope != null)
        {
            var result = RoutingEngine.ProcessInbound(envelope);
            if (result is InboundResult.DeliverAndForward df)
            {
                var packet = PacketSerializer.Deserialize(df.Envelope.Payload);
                if (packet != null) PacketReceived?.Invoke(packet);

                // Forward to other neighbors
                var fwdBytes = df.Envelope.ToByteArray();
                foreach (var neighbor in df.TargetNeighbors)
                {
                    if (_connectedPeers.TryGetValue(neighbor, out var conn))
                    {
                        SendRawFrame(conn, fwdBytes);
                    }
                }
            }
            else if (result is InboundResult.DeliverOnly d)
            {
                var packet = PacketSerializer.Deserialize(d.Envelope.Payload);
                if (packet != null) PacketReceived?.Invoke(packet);
            }
        }
        else
        {
            var packet = PacketSerializer.Deserialize(bytes);
            if (packet != null) PacketReceived?.Invoke(packet);
        }
    }

    public void SendPacketToAll(WhiteboardPacket packet, bool isMutation = false, PermissionTicket? ticket = null)
    {
        if (_connectedPeers.IsEmpty) return;

        var rawBytes = PacketSerializer.Serialize(packet);
        var (envelope, targets) = RoutingEngine.CreateOutbound(rawBytes, isMutation, ticket);
        var envelopeBytes = envelope.ToByteArray();

        foreach (var target in targets)
        {
            if (_connectedPeers.TryGetValue(target, out var conn))
            {
                SendRawFrame(conn, envelopeBytes);
            }
        }
    }

    private void SendPacketDirect(PeerConnection conn, WhiteboardPacket packet)
    {
        var rawBytes = PacketSerializer.Serialize(packet);
        var (envelope, _) = RoutingEngine.CreateOutbound(rawBytes, false, null);
        SendRawFrame(conn, envelope.ToByteArray());
    }

    private void SendRawFrame(PeerConnection conn, byte[] payload)
    {
        try
        {
            var stream = conn.Client.GetStream();
            int len = payload.Length;
            byte[] frame = new byte[4 + len];
            frame[0] = (byte)((len >> 24) & 0xFF);
            frame[1] = (byte)((len >> 16) & 0xFF);
            frame[2] = (byte)((len >> 8) & 0xFF);
            frame[3] = (byte)(len & 0xFF);
            Buffer.BlockCopy(payload, 0, frame, 4, len);
            stream.Write(frame, 0, frame.Length);
        }
        catch
        {
            DisconnectFromPeer(conn.EndpointId);
        }
    }

    public void DisconnectFromPeer(string endpointId)
    {
        if (_connectedPeers.TryRemove(endpointId, out var conn))
        {
            try { conn.Client.Close(); } catch { }
            RoutingEngine.RemoveNeighbor(endpointId);
            NotifyConnectedPeers();
        }
    }

    public void StopAllEndpoints()
    {
        StopAdvertising();
        StopDiscovery();
        _listenCts?.Cancel();
        _listenCts = null;
        try { _tcpListener?.Stop(); } catch { }
        _tcpListener = null;

        foreach (var p in _connectedPeers.Values)
        {
            try { p.Client.Close(); } catch { }
        }
        _connectedPeers.Clear();
        _discoveredPeers.Clear();
        RoutingEngine.Clear();
        NotifyConnectedPeers();
        NotifyDiscoveredPeers();
    }

    private void NotifyDiscoveredPeers()
    {
        var list = _discoveredPeers.Values
            .Select(d => new PeerDevice(d.EndpointId, d.Name) { Role = (Domain.Models.MeshRole)(int)d.Role, IsConnected = false })
            .ToList();
        DiscoveredPeersChanged?.Invoke(list);
    }

    private void NotifyConnectedPeers()
    {
        var list = _connectedPeers.Values
            .Select(c => new PeerDevice(c.EndpointId, c.Name) { IsConnected = true })
            .ToList();
        ConnectedPeersChanged?.Invoke(list);
    }

    public void Dispose()
    {
        if (_isDisposed) return;
        _isDisposed = true;
        StopAllEndpoints();
    }

    private sealed record PeerConnection(string EndpointId, string Name, TcpClient Client);

    private sealed record DiscoveredPeerInfo(
        string EndpointId,
        string Name,
        string IpAddress,
        int Port,
        string RoomCode,
        MeshRole Role,
        DateTime LastSeen
    );
}
