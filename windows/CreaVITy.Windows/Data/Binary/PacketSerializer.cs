using System;
using System.Buffers.Binary;
using System.IO;
using System.Text;
using CreaVITy.Windows.Domain.Models;

namespace CreaVITy.Windows.Data.Binary
{
    // ──────────────────────────────────────────────────────────────────────────
    // Packet type discriminator bytes (must match Kotlin constants exactly)
    // ──────────────────────────────────────────────────────────────────────────
    internal static class PacketType
    {
        public const byte StrokeStart     = 1;
        public const byte StrokePoint     = 2;
        public const byte StrokeEnd       = 3;
        public const byte CanvasClear     = 4;
        public const byte SyncRequest     = 5;
        public const byte SyncResponse    = 6;
        public const byte StrokeDelete    = 7;
        public const byte TextAdd         = 8;
        public const byte TextDelete      = 9;
        public const byte BoardSync       = 10;
        public const byte PermissionUpdate = 11;
        public const byte StickyNoteAdd   = 12;
        public const byte StickyNoteDelete = 13;
        public const byte ViewportPresence = 14;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Packet hierarchy — abstract record + sealed nested records
    // ──────────────────────────────────────────────────────────────────────────
    public abstract record WhiteboardPacket
    {
        private WhiteboardPacket() { }

        public sealed record StrokeStart(
            string StrokeId,
            long   Color,
            float  Width,
            bool   IsEraser,
            StrokePoint FirstPoint) : WhiteboardPacket;

        public sealed record StrokePoint(
            string StrokeId,
            float  X,
            float  Y,
            float  Pressure,
            long   TimestampMs) : WhiteboardPacket;

        public sealed record StrokeEnd(
            string StrokeId) : WhiteboardPacket;

        public sealed record StrokeDelete(
            string StrokeId) : WhiteboardPacket;

        public sealed record TextAdd(
            string Id,
            string Text,
            float  X,
            float  Y,
            long   Color,
            float  FontSize,
            string FontName) : WhiteboardPacket;

        public sealed record TextDelete(
            string Id) : WhiteboardPacket;

        public sealed record StickyNoteAdd(
            string Id,
            string Text,
            float  X,
            float  Y,
            float  Width,
            float  Height,
            long   Color,
            string Author) : WhiteboardPacket;

        public sealed record StickyNoteDelete(
            string Id) : WhiteboardPacket;

        public sealed record ViewportPresence(
            string EndpointId,
            string Name,
            long   Color,
            float  Left,
            float  Top,
            float  Right,
            float  Bottom) : WhiteboardPacket;

        /// <summary>Singleton — no fields.</summary>
        public sealed record CanvasClear : WhiteboardPacket;

        /// <summary>Singleton — no fields.</summary>
        public sealed record SyncRequest : WhiteboardPacket;

        public sealed record SyncResponse(
            StrokeData[]      Strokes,
            TextElementData[] Texts) : WhiteboardPacket;

        public sealed record BoardSync(
            string            BoardId,
            string            Title,
            long              BgColor,
            bool              IsReadOnly,
            StrokeData[]      Strokes,
            TextElementData[] Texts) : WhiteboardPacket;

        public sealed record PermissionUpdate(
            string EndpointId,
            bool   IsReadOnly) : WhiteboardPacket;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Supporting data models (mirror Kotlin data classes used in lists)
    // ──────────────────────────────────────────────────────────────────────────

    /// <summary>
    /// Flattened stroke used inside SyncResponse / BoardSync binary lists.
    /// </summary>
    public sealed record StrokeData(
        string           Id,
        long             Color,
        float            Width,
        DrawingTool      Tool,
        bool             IsEraser,
        StrokePointData[] Points);

    /// <summary>
    /// Per-point data inside a <see cref="StrokeData"/>.
    /// </summary>
    public sealed record StrokePointData(
        float X,
        float Y,
        float Pressure,
        long  TimestampMs);

    /// <summary>
    /// Text element used inside SyncResponse / BoardSync binary lists.
    /// </summary>
    public sealed record TextElementData(
        string Id,
        string Text,
        float  X,
        float  Y,
        long   Color,
        float  FontSize,
        string FontName);

    // ──────────────────────────────────────────────────────────────────────────
    // DrawingTool ordinal enum — ordinal must match Kotlin exactly
    // ──────────────────────────────────────────────────────────────────────────
    public enum DrawingTool : byte
    {
        Pen           = 0,
        Marker        = 1,
        Highlighter   = 2,
        Brush         = 3,
        Eraser        = 4,
        Select        = 5,
        StickyNote    = 6,
        Laser         = 7,
        Pencil        = 8,
        Calligraphy   = 9,
        Line          = 10,
        Arrow         = 11,
        Rectangle     = 12,
        Circle        = 13,
        Text          = 14,
        SelectErase   = 15,
        SelectMove    = 16,
        RectSelect    = 17,
        LassoSelect   = 18,
        Triangle      = 19,
    }

    // ──────────────────────────────────────────────────────────────────────────
    // PacketSerializer — static class, big-endian wire format
    // ──────────────────────────────────────────────────────────────────────────
    public static class PacketSerializer
    {
        // ── Serialize ────────────────────────────────────────────────────────

        /// <summary>
        /// Serializes a <see cref="WhiteboardPacket"/> to a big-endian byte array
        /// that is wire-compatible with the Android Kotlin implementation.
        /// </summary>
        public static byte[] Serialize(WhiteboardPacket packet)
        {
            using var ms = new MemoryStream(256);
            using var w  = new BigEndianWriter(ms);

            switch (packet)
            {
                case WhiteboardPacket.StrokeStart p:
                    w.WriteByte(PacketType.StrokeStart);
                    w.WriteString16(p.StrokeId);
                    w.WriteInt64(p.Color);
                    w.WriteFloat(p.Width);
                    w.WriteByte(p.IsEraser ? (byte)1 : (byte)0);
                    w.WriteFloat(p.FirstPoint.X);
                    w.WriteFloat(p.FirstPoint.Y);
                    w.WriteFloat(p.FirstPoint.Pressure);
                    w.WriteInt64(p.FirstPoint.TimestampMs);
                    break;

                case WhiteboardPacket.StrokePoint p:
                    w.WriteByte(PacketType.StrokePoint);
                    w.WriteString16(p.StrokeId);
                    w.WriteFloat(p.X);
                    w.WriteFloat(p.Y);
                    w.WriteFloat(p.Pressure);
                    w.WriteInt64(p.TimestampMs);
                    break;

                case WhiteboardPacket.StrokeEnd p:
                    w.WriteByte(PacketType.StrokeEnd);
                    w.WriteString16(p.StrokeId);
                    break;

                case WhiteboardPacket.StrokeDelete p:
                    w.WriteByte(PacketType.StrokeDelete);
                    w.WriteString16(p.StrokeId);
                    break;

                case WhiteboardPacket.TextAdd p:
                    w.WriteByte(PacketType.TextAdd);
                    w.WriteString16(p.Id);
                    w.WriteString16(p.Text);
                    w.WriteFloat(p.X);
                    w.WriteFloat(p.Y);
                    w.WriteInt64(p.Color);
                    w.WriteFloat(p.FontSize);
                    w.WriteString16(p.FontName);
                    break;

                case WhiteboardPacket.TextDelete p:
                    w.WriteByte(PacketType.TextDelete);
                    w.WriteString16(p.Id);
                    break;

                case WhiteboardPacket.StickyNoteAdd p:
                    w.WriteByte(PacketType.StickyNoteAdd);
                    w.WriteString16(p.Id);
                    w.WriteString16(p.Text);
                    w.WriteFloat(p.X);
                    w.WriteFloat(p.Y);
                    w.WriteFloat(p.Width);
                    w.WriteFloat(p.Height);
                    w.WriteInt64(p.Color);
                    w.WriteString16(p.Author);
                    break;

                case WhiteboardPacket.StickyNoteDelete p:
                    w.WriteByte(PacketType.StickyNoteDelete);
                    w.WriteString16(p.Id);
                    break;

                case WhiteboardPacket.ViewportPresence p:
                    w.WriteByte(PacketType.ViewportPresence);
                    w.WriteString16(p.EndpointId);
                    w.WriteString16(p.Name);
                    w.WriteInt64(p.Color);
                    w.WriteFloat(p.Left);
                    w.WriteFloat(p.Top);
                    w.WriteFloat(p.Right);
                    w.WriteFloat(p.Bottom);
                    break;

                case WhiteboardPacket.CanvasClear:
                    w.WriteByte(PacketType.CanvasClear);
                    break;

                case WhiteboardPacket.SyncRequest:
                    w.WriteByte(PacketType.SyncRequest);
                    break;

                case WhiteboardPacket.SyncResponse p:
                    w.WriteByte(PacketType.SyncResponse);
                    WriteStrokeList(w, p.Strokes);
                    WriteTextList(w, p.Texts);
                    break;

                case WhiteboardPacket.BoardSync p:
                    w.WriteByte(PacketType.BoardSync);
                    w.WriteString16(p.BoardId);
                    w.WriteString16(p.Title);
                    w.WriteInt64(p.BgColor);
                    w.WriteByte(p.IsReadOnly ? (byte)1 : (byte)0);
                    WriteStrokeList(w, p.Strokes);
                    WriteTextList(w, p.Texts);
                    break;

                case WhiteboardPacket.PermissionUpdate p:
                    w.WriteByte(PacketType.PermissionUpdate);
                    w.WriteString16(p.EndpointId);
                    w.WriteByte(p.IsReadOnly ? (byte)1 : (byte)0);
                    break;

                default:
                    throw new ArgumentException($"Unknown packet type: {packet.GetType().Name}");
            }

            return ms.ToArray();
        }

        // ── Deserialize ──────────────────────────────────────────────────────

        /// <summary>
        /// Deserializes a big-endian byte array into a <see cref="WhiteboardPacket"/>.
        /// Returns <c>null</c> if the data is malformed or the type byte is unrecognized.
        /// </summary>
        public static WhiteboardPacket? Deserialize(byte[] bytes)
        {
            if (bytes == null || bytes.Length == 0)
                return null;

            try
            {
                using var ms = new MemoryStream(bytes);
                using var r  = new BigEndianReader(ms);

                byte typeByte = r.ReadByte();

                return typeByte switch
                {
                    PacketType.StrokeStart =>
                        DeserializeStrokeStart(r),

                    PacketType.StrokePoint =>
                        DeserializeStrokePoint(r),

                    PacketType.StrokeEnd =>
                        new WhiteboardPacket.StrokeEnd(r.ReadString16()),

                    PacketType.StrokeDelete =>
                        new WhiteboardPacket.StrokeDelete(r.ReadString16()),

                    PacketType.TextAdd =>
                        DeserializeTextAdd(r),

                    PacketType.TextDelete =>
                        new WhiteboardPacket.TextDelete(r.ReadString16()),

                    PacketType.StickyNoteAdd =>
                        DeserializeStickyNoteAdd(r),

                    PacketType.StickyNoteDelete =>
                        new WhiteboardPacket.StickyNoteDelete(r.ReadString16()),

                    PacketType.ViewportPresence =>
                        DeserializeViewportPresence(r),

                    PacketType.CanvasClear =>
                        new WhiteboardPacket.CanvasClear(),

                    PacketType.SyncRequest =>
                        new WhiteboardPacket.SyncRequest(),

                    PacketType.SyncResponse =>
                        DeserializeSyncResponse(r),

                    PacketType.BoardSync =>
                        DeserializeBoardSync(r),

                    PacketType.PermissionUpdate =>
                        DeserializePermissionUpdate(r),

                    _ => null,
                };
            }
            catch
            {
                return null;
            }
        }

        // ── Private deserialize helpers ──────────────────────────────────────

        private static WhiteboardPacket.StrokeStart DeserializeStrokeStart(BigEndianReader r)
        {
            string strokeId    = r.ReadString16();
            long   color       = r.ReadInt64();
            float  width       = r.ReadFloat();
            bool   isEraser    = r.ReadByte() != 0;
            float  ptX         = r.ReadFloat();
            float  ptY         = r.ReadFloat();
            float  ptPressure  = r.ReadFloat();
            long   ptTimestamp = r.ReadInt64();

            var firstPoint = new WhiteboardPacket.StrokePoint(
                strokeId, ptX, ptY, ptPressure, ptTimestamp);

            return new WhiteboardPacket.StrokeStart(
                strokeId, color, width, isEraser, firstPoint);
        }

        private static WhiteboardPacket.StrokePoint DeserializeStrokePoint(BigEndianReader r)
        {
            string strokeId  = r.ReadString16();
            float  x         = r.ReadFloat();
            float  y         = r.ReadFloat();
            float  pressure  = r.ReadFloat();
            long   timestamp = r.ReadInt64();

            return new WhiteboardPacket.StrokePoint(strokeId, x, y, pressure, timestamp);
        }

        private static WhiteboardPacket.TextAdd DeserializeTextAdd(BigEndianReader r)
        {
            string id       = r.ReadString16();
            string text     = r.ReadString16();
            float  x        = r.ReadFloat();
            float  y        = r.ReadFloat();
            long   color    = r.ReadInt64();
            float  fontSize = r.ReadFloat();
            string fontName = r.ReadString16();

            return new WhiteboardPacket.TextAdd(id, text, x, y, color, fontSize, fontName);
        }

        private static WhiteboardPacket.StickyNoteAdd DeserializeStickyNoteAdd(BigEndianReader r)
        {
            string id     = r.ReadString16();
            string text   = r.ReadString16();
            float  x      = r.ReadFloat();
            float  y      = r.ReadFloat();
            float  width  = r.ReadFloat();
            float  height = r.ReadFloat();
            long   color  = r.ReadInt64();
            string author = r.ReadString16();

            return new WhiteboardPacket.StickyNoteAdd(id, text, x, y, width, height, color, author);
        }

        private static WhiteboardPacket.ViewportPresence DeserializeViewportPresence(BigEndianReader r)
        {
            string endpointId = r.ReadString16();
            string name       = r.ReadString16();
            long   color      = r.ReadInt64();
            float  left       = r.ReadFloat();
            float  top        = r.ReadFloat();
            float  right      = r.ReadFloat();
            float  bottom     = r.ReadFloat();

            return new WhiteboardPacket.ViewportPresence(endpointId, name, color, left, top, right, bottom);
        }

        private static WhiteboardPacket.SyncResponse DeserializeSyncResponse(BigEndianReader r)
        {
            StrokeData[]      strokes = ReadStrokeList(r);
            TextElementData[] texts   = ReadTextList(r);

            return new WhiteboardPacket.SyncResponse(strokes, texts);
        }

        private static WhiteboardPacket.BoardSync DeserializeBoardSync(BigEndianReader r)
        {
            string            boardId    = r.ReadString16();
            string            title      = r.ReadString16();
            long              bgColor    = r.ReadInt64();
            bool              isReadOnly = r.ReadByte() != 0;
            StrokeData[]      strokes    = ReadStrokeList(r);
            TextElementData[] texts      = ReadTextList(r);

            return new WhiteboardPacket.BoardSync(boardId, title, bgColor, isReadOnly, strokes, texts);
        }

        private static WhiteboardPacket.PermissionUpdate DeserializePermissionUpdate(BigEndianReader r)
        {
            string endpointId = r.ReadString16();
            bool   isReadOnly = r.ReadByte() != 0;

            return new WhiteboardPacket.PermissionUpdate(endpointId, isReadOnly);
        }

        // ── List serialization helpers ────────────────────────────────────────

        private static void WriteStrokeList(BigEndianWriter w, StrokeData[] strokes)
        {
            w.WriteInt32(strokes.Length);
            foreach (var s in strokes)
            {
                w.WriteString16(s.Id);
                w.WriteInt64(s.Color);
                w.WriteFloat(s.Width);
                w.WriteByte((byte)s.Tool);
                w.WriteByte(s.IsEraser ? (byte)1 : (byte)0);
                w.WriteInt32(s.Points.Length);
                foreach (var pt in s.Points)
                {
                    w.WriteFloat(pt.X);
                    w.WriteFloat(pt.Y);
                    w.WriteFloat(pt.Pressure);
                    w.WriteInt64(pt.TimestampMs);
                }
            }
        }

        private static StrokeData[] ReadStrokeList(BigEndianReader r)
        {
            int count = r.ReadInt32();
            var result = new StrokeData[count];
            for (int i = 0; i < count; i++)
            {
                string id        = r.ReadString16();
                long   color     = r.ReadInt64();
                float  width     = r.ReadFloat();
                var    tool      = (DrawingTool)r.ReadByte();
                bool   isEraser  = r.ReadByte() != 0;
                int    ptCount   = r.ReadInt32();
                var    points    = new StrokePointData[ptCount];
                for (int j = 0; j < ptCount; j++)
                {
                    float x           = r.ReadFloat();
                    float y           = r.ReadFloat();
                    float pressure    = r.ReadFloat();
                    long  timestampMs = r.ReadInt64();
                    points[j] = new StrokePointData(x, y, pressure, timestampMs);
                }
                result[i] = new StrokeData(id, color, width, tool, isEraser, points);
            }
            return result;
        }

        private static void WriteTextList(BigEndianWriter w, TextElementData[] texts)
        {
            w.WriteInt32(texts.Length);
            foreach (var t in texts)
            {
                w.WriteString16(t.Id);
                w.WriteString16(t.Text);
                w.WriteFloat(t.X);
                w.WriteFloat(t.Y);
                w.WriteInt64(t.Color);
                w.WriteFloat(t.FontSize);
                w.WriteString16(t.FontName);
            }
        }

        private static TextElementData[] ReadTextList(BigEndianReader r)
        {
            int count  = r.ReadInt32();
            var result = new TextElementData[count];
            for (int i = 0; i < count; i++)
            {
                string id       = r.ReadString16();
                string text     = r.ReadString16();
                float  x        = r.ReadFloat();
                float  y        = r.ReadFloat();
                long   color    = r.ReadInt64();
                float  fontSize = r.ReadFloat();
                string fontName = r.ReadString16();
                result[i] = new TextElementData(id, text, x, y, color, fontSize, fontName);
            }
            return result;
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // BigEndianWriter — wraps MemoryStream, writes all multi-byte values BE
    // ──────────────────────────────────────────────────────────────────────────
    internal sealed class BigEndianWriter : IDisposable
    {
        private readonly Stream _stream;
        private readonly byte[] _buf8 = new byte[8];

        public BigEndianWriter(Stream stream) => _stream = stream;

        public void WriteByte(byte value) =>
            _stream.WriteByte(value);

        public void WriteInt16(short value)
        {
            BinaryPrimitives.WriteInt16BigEndian(_buf8, value);
            _stream.Write(_buf8, 0, 2);
        }

        public void WriteInt32(int value)
        {
            BinaryPrimitives.WriteInt32BigEndian(_buf8, value);
            _stream.Write(_buf8, 0, 4);
        }

        public void WriteInt64(long value)
        {
            BinaryPrimitives.WriteInt64BigEndian(_buf8, value);
            _stream.Write(_buf8, 0, 8);
        }

        public void WriteFloat(float value)
        {
            // Encode float bits as big-endian int32 — identical to Java ByteBuffer behavior
            BinaryPrimitives.WriteInt32BigEndian(_buf8, BitConverter.SingleToInt32Bits(value));
            _stream.Write(_buf8, 0, 4);
        }

        /// <summary>
        /// Writes a UTF-8 string prefixed by a big-endian int16 byte length.
        /// Matches Kotlin: putShort(len.toShort()); put(bytes)
        /// </summary>
        public void WriteString16(string value)
        {
            byte[] encoded = Encoding.UTF8.GetBytes(value);
            WriteInt16((short)encoded.Length);
            _stream.Write(encoded, 0, encoded.Length);
        }

        public void Dispose() { /* stream owned externally */ }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // BigEndianReader — wraps MemoryStream, reads all multi-byte values BE
    // ──────────────────────────────────────────────────────────────────────────
    internal sealed class BigEndianReader : IDisposable
    {
        private readonly Stream _stream;
        private readonly byte[] _buf8 = new byte[8];

        public BigEndianReader(Stream stream) => _stream = stream;

        public byte ReadByte()
        {
            int v = _stream.ReadByte();
            if (v < 0) throw new EndOfStreamException();
            return (byte)v;
        }

        private void ReadExact(int count)
        {
            int read = 0;
            while (read < count)
            {
                int n = _stream.Read(_buf8, read, count - read);
                if (n == 0) throw new EndOfStreamException();
                read += n;
            }
        }

        public short ReadInt16()
        {
            ReadExact(2);
            return BinaryPrimitives.ReadInt16BigEndian(_buf8);
        }

        public int ReadInt32()
        {
            ReadExact(4);
            return BinaryPrimitives.ReadInt32BigEndian(_buf8);
        }

        public long ReadInt64()
        {
            ReadExact(8);
            return BinaryPrimitives.ReadInt64BigEndian(_buf8);
        }

        public float ReadFloat()
        {
            ReadExact(4);
            return BitConverter.Int32BitsToSingle(BinaryPrimitives.ReadInt32BigEndian(_buf8));
        }

        /// <summary>
        /// Reads a big-endian int16 byte-length prefix then the UTF-8 string bytes.
        /// </summary>
        public string ReadString16()
        {
            short len = ReadInt16();
            if (len < 0) throw new InvalidDataException("Negative string length.");
            if (len == 0) return string.Empty;

            byte[] buf = new byte[len];
            int read = 0;
            while (read < len)
            {
                int n = _stream.Read(buf, read, len - read);
                if (n == 0) throw new EndOfStreamException();
                read += n;
            }
            return Encoding.UTF8.GetString(buf);
        }

        public void Dispose() { /* stream owned externally */ }
    }
}
