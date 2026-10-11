using System;
using System.Buffers.Binary;
using System.Text;

namespace CreaVITy.Windows.Data.Mesh
{
    public enum MeshRole { HOST, EDITOR, VIEWER }

    public record PermissionTicket(
        string DeviceId,
        string BoardId,
        MeshRole Role,
        long ExpiresAt = 0L,
        string Signature = "");

    /// <summary>
    /// Wire format (big-endian):
    /// packetId(8) | ttl(1) | hopCount(1) | isMutation(1)
    /// | originLen(2,BE) | originBytes
    /// | senderLen(2,BE) | senderBytes
    /// | tokenLen(2,BE)  | tokenBytes
    /// | payloadLen(4,BE)| payload
    /// Minimum serialized size: 8+1+1+1+2+2+2+4 = 21 bytes (empty strings, empty payload)
    /// Minimum check for deserialization: bytes.Length &lt; 19
    /// </summary>
    public record MeshEnvelope(
        long PacketId,
        string OriginNodeId,
        string SenderNodeId,
        byte Ttl = 12,
        byte HopCount = 0,
        string RoleToken = "",
        bool IsMutation = false,
        byte[] Payload = null!)
    {
        // ── Payload content equality ────────────────────────────────────────────
        public virtual bool Equals(MeshEnvelope? other)
        {
            if (other is null) return false;
            if (ReferenceEquals(this, other)) return true;
            return PacketId == other.PacketId
                && OriginNodeId == other.OriginNodeId
                && SenderNodeId == other.SenderNodeId
                && Ttl == other.Ttl
                && HopCount == other.HopCount
                && RoleToken == other.RoleToken
                && IsMutation == other.IsMutation
                && PayloadEquals(Payload, other.Payload);
        }

        public override int GetHashCode()
        {
            var hc = new HashCode();
            hc.Add(PacketId);
            hc.Add(OriginNodeId);
            hc.Add(SenderNodeId);
            hc.Add(Ttl);
            hc.Add(HopCount);
            hc.Add(RoleToken);
            hc.Add(IsMutation);
            if (Payload is not null)
                foreach (var b in Payload) hc.Add(b);
            return hc.ToHashCode();
        }

        private static bool PayloadEquals(byte[]? a, byte[]? b)
        {
            if (a is null && b is null) return true;
            if (a is null || b is null) return false;
            return a.AsSpan().SequenceEqual(b.AsSpan());
        }

        // ── Serialization ───────────────────────────────────────────────────────
        public byte[] ToByteArray()
        {
            byte[] originBytes  = Encoding.UTF8.GetBytes(OriginNodeId ?? string.Empty);
            byte[] senderBytes  = Encoding.UTF8.GetBytes(SenderNodeId ?? string.Empty);
            byte[] tokenBytes   = Encoding.UTF8.GetBytes(RoleToken    ?? string.Empty);
            byte[] payloadBytes = Payload ?? Array.Empty<byte>();

            int totalLen = 8 + 1 + 1 + 1                // packetId, ttl, hopCount, isMutation
                         + 2 + originBytes.Length        // originLen + originBytes
                         + 2 + senderBytes.Length        // senderLen + senderBytes
                         + 2 + tokenBytes.Length         // tokenLen  + tokenBytes
                         + 4 + payloadBytes.Length;      // payloadLen + payload

            byte[] buf = new byte[totalLen];
            int pos = 0;

            BinaryPrimitives.WriteInt64BigEndian(buf.AsSpan(pos, 8), PacketId);
            pos += 8;

            buf[pos++] = Ttl;
            buf[pos++] = HopCount;
            buf[pos++] = IsMutation ? (byte)1 : (byte)0;

            BinaryPrimitives.WriteInt16BigEndian(buf.AsSpan(pos, 2), (short)originBytes.Length);
            pos += 2;
            originBytes.CopyTo(buf, pos);
            pos += originBytes.Length;

            BinaryPrimitives.WriteInt16BigEndian(buf.AsSpan(pos, 2), (short)senderBytes.Length);
            pos += 2;
            senderBytes.CopyTo(buf, pos);
            pos += senderBytes.Length;

            BinaryPrimitives.WriteInt16BigEndian(buf.AsSpan(pos, 2), (short)tokenBytes.Length);
            pos += 2;
            tokenBytes.CopyTo(buf, pos);
            pos += tokenBytes.Length;

            BinaryPrimitives.WriteInt32BigEndian(buf.AsSpan(pos, 4), payloadBytes.Length);
            pos += 4;
            payloadBytes.CopyTo(buf, pos);

            return buf;
        }

        // ── Deserialization ─────────────────────────────────────────────────────
        public static MeshEnvelope? FromByteArray(byte[] bytes)
        {
            if (bytes is null || bytes.Length < 19) return null;

            try
            {
                int pos = 0;

                long packetId = BinaryPrimitives.ReadInt64BigEndian(bytes.AsSpan(pos, 8));
                pos += 8;

                byte ttl       = bytes[pos++];
                byte hopCount  = bytes[pos++];
                bool isMutation = bytes[pos++] != 0;

                // originNodeId
                short originLen = BinaryPrimitives.ReadInt16BigEndian(bytes.AsSpan(pos, 2));
                pos += 2;
                if (originLen < 0 || pos + originLen > bytes.Length) return null;
                string originNodeId = Encoding.UTF8.GetString(bytes, pos, originLen);
                pos += originLen;

                // senderNodeId
                short senderLen = BinaryPrimitives.ReadInt16BigEndian(bytes.AsSpan(pos, 2));
                pos += 2;
                if (senderLen < 0 || pos + senderLen > bytes.Length) return null;
                string senderNodeId = Encoding.UTF8.GetString(bytes, pos, senderLen);
                pos += senderLen;

                // roleToken
                short tokenLen = BinaryPrimitives.ReadInt16BigEndian(bytes.AsSpan(pos, 2));
                pos += 2;
                if (tokenLen < 0 || pos + tokenLen > bytes.Length) return null;
                string roleToken = Encoding.UTF8.GetString(bytes, pos, tokenLen);
                pos += tokenLen;

                // payload
                int payloadLen = BinaryPrimitives.ReadInt32BigEndian(bytes.AsSpan(pos, 4));
                pos += 4;
                if (payloadLen < 0 || pos + payloadLen > bytes.Length) return null;
                byte[] payload = new byte[payloadLen];
                Buffer.BlockCopy(bytes, pos, payload, 0, payloadLen);

                return new MeshEnvelope(
                    PacketId:    packetId,
                    OriginNodeId: originNodeId,
                    SenderNodeId: senderNodeId,
                    Ttl:         ttl,
                    HopCount:    hopCount,
                    RoleToken:   roleToken,
                    IsMutation:  isMutation,
                    Payload:     payload);
            }
            catch
            {
                return null;
            }
        }
    }
}
