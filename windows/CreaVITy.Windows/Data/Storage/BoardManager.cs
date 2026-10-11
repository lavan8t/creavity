using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text.Json;

namespace CreaVITy.Windows.Data.Storage
{
    // ── Enumerations ────────────────────────────────────────────────────────────

    /// <summary>Drawing tool identifiers — kept in the storage layer so that
    /// domain models can reference them without a circular dependency.</summary>
    public enum DrawingTool
    {
        Pen,
        Highlighter,
        Marker,
        Eraser,
        Laser,
        Selection
    }

    // ── Domain records ──────────────────────────────────────────────────────────

    public sealed class StrokePoint
    {
        public float X  { get; set; }
        public float Y  { get; set; }
        public float P  { get; set; }   // pressure (0–1)
        public long  T  { get; set; }   // timestamp ms
        public float? Tilt { get; set; }
        public float? Ori  { get; set; }
    }

    public sealed class BoardStroke
    {
        public string      Id          { get; set; } = string.Empty;
        public long        Color       { get; set; }
        public float       StrokeWidth { get; set; }
        public DrawingTool Tool        { get; set; } = DrawingTool.Pen;
        public bool        IsEraser    { get; set; }
        public string      Author      { get; set; } = string.Empty;
        public long        CreatedAt   { get; set; }
        public List<StrokePoint> Points { get; set; } = new();
    }

    public sealed class TextElement
    {
        public string Id        { get; set; } = string.Empty;
        public string Text      { get; set; } = string.Empty;
        public float  X         { get; set; }
        public float  Y         { get; set; }
        public long   Color     { get; set; }
        public float  FontSize  { get; set; }
        public string FontName  { get; set; } = string.Empty;
        public string Author    { get; set; } = string.Empty;
        public long   CreatedAt { get; set; }
    }

    public sealed class ImageElement
    {
        public string Id        { get; set; } = string.Empty;
        public string Path      { get; set; } = string.Empty;
        public float  X         { get; set; }
        public float  Y         { get; set; }
        public float  Width     { get; set; }
        public float  Height    { get; set; }
        public string Author    { get; set; } = string.Empty;
        public long   CreatedAt { get; set; }
    }

    public sealed class StickyNote
    {
        public string Id        { get; set; } = string.Empty;
        public string Text      { get; set; } = string.Empty;
        public float  X         { get; set; }
        public float  Y         { get; set; }
        public float  Width     { get; set; }
        public float  Height    { get; set; }
        public long   Color     { get; set; }
        public string Author    { get; set; } = string.Empty;
        public long   CreatedAt { get; set; }
    }

    public sealed class BoardPage
    {
        public string             Id            { get; set; } = Guid.NewGuid().ToString();
        public string             Name          { get; set; } = "Page 1";
        public List<BoardStroke>  Strokes       { get; set; } = new();
        public List<TextElement>  TextElements  { get; set; } = new();
        public List<ImageElement> ImageElements { get; set; } = new();
        public List<StickyNote>   StickyNotes   { get; set; } = new();
    }

    public sealed class Board
    {
        public string       Id               { get; set; } = string.Empty;
        public string       Title            { get; set; } = string.Empty;
        public int          ActivePageIndex  { get; set; }
        public long         BackgroundColor  { get; set; }
        public long         CreatedAt        { get; set; }
        public long         ModifiedAt       { get; set; }
        public long         LastAccessedAt   { get; set; }
        public string       CreatedBy        { get; set; } = string.Empty;
        public string       OwnerName        { get; set; } = string.Empty;
        public bool         IsReadOnly       { get; set; }
        public bool         IsStarred        { get; set; }
        public bool         IsInBin          { get; set; }
        public long?        BinnedAt         { get; set; }
        public bool         IsInfiniteCanvas { get; set; }
        public float        CanvasWidth      { get; set; }
        public float        CanvasHeight     { get; set; }
        public string?      PresentationUrl  { get; set; }
        public string       Venue            { get; set; } = string.Empty;
        public string       Group            { get; set; } = string.Empty;
        public List<string> Labels           { get; set; } = new();
        public List<BoardPage> Pages         { get; set; } = new();

        /// <summary>Computed — returns the currently active page (never null).</summary>
        public BoardPage ActivePage =>
            Pages.ElementAtOrDefault(ActivePageIndex) ?? new BoardPage();
    }

    // ── BoardManager ────────────────────────────────────────────────────────────

    /// <summary>Persists <see cref="Board"/> objects as JSON files inside
    /// <c>%USERPROFILE%\Documents\CreaVITy\</c>.  The JSON schema is
    /// intentionally identical to the Android implementation so that board files
    /// can be shared between platforms.</summary>
    public sealed class BoardManager
    {
        // ── Singleton ────────────────────────────────────────────────────────────
        private static readonly Lazy<BoardManager> _lazy =
            new(() => new BoardManager(), System.Threading.LazyThreadSafetyMode.ExecutionAndPublication);

        public static BoardManager Instance => _lazy.Value;

        // ── State ────────────────────────────────────────────────────────────────
        private readonly object _syncRoot = new();

        private readonly string _boardsDir;
        private const string FoldersFileName = "creavity_folders.json";

        private static readonly JsonSerializerOptions _jsonOpts = new()
        {
            PropertyNamingPolicy = null,      // exact field names — matches Android
            WriteIndented        = false
        };

        // ── Construction ─────────────────────────────────────────────────────────
        private BoardManager()
        {
            _boardsDir = Path.Combine(
                Environment.GetFolderPath(Environment.SpecialFolder.MyDocuments),
                "CreaVITy");
            Directory.CreateDirectory(_boardsDir);
        }

        // ── Public API ───────────────────────────────────────────────────────────

        /// <summary>Atomically writes <paramref name="board"/> to disk.</summary>
        public void SaveBoard(Board board)
        {
            lock (_syncRoot)
            {
                string target  = BoardFilePath(board.Id);
                string tmp     = target + ".tmp";

                string json = SerializeBoard(board);
                File.WriteAllText(tmp, json, System.Text.Encoding.UTF8);
                File.Move(tmp, target, overwrite: true);
            }
        }

        /// <summary>Loads a single board by id, or <see langword="null"/> if
        /// the file does not exist or cannot be parsed.</summary>
        public Board? LoadBoard(string boardId)
        {
            string path = BoardFilePath(boardId);
            if (!File.Exists(path)) return null;

            try
            {
                string json = File.ReadAllText(path, System.Text.Encoding.UTF8);
                using JsonDocument doc = JsonDocument.Parse(json);
                return ParseBoard(doc.RootElement);
            }
            catch
            {
                return null;
            }
        }

        /// <summary>Returns all valid boards sorted by <c>lastAccessedAt</c>
        /// descending.</summary>
        public List<Board> LoadAllBoards()
        {
            var boards = new List<Board>();

            if (!Directory.Exists(_boardsDir)) return boards;

            foreach (string file in Directory.EnumerateFiles(_boardsDir, "*.json"))
            {
                // Skip the folders manifest
                if (string.Equals(
                        Path.GetFileName(file),
                        FoldersFileName,
                        StringComparison.OrdinalIgnoreCase)) continue;

                try
                {
                    string json = File.ReadAllText(file, System.Text.Encoding.UTF8);
                    using JsonDocument doc = JsonDocument.Parse(json);
                    Board? board = ParseBoard(doc.RootElement);
                    if (board is not null) boards.Add(board);
                }
                catch
                {
                    // Skip corrupt files
                }
            }

            boards.Sort((a, b) => b.LastAccessedAt.CompareTo(a.LastAccessedAt));
            return boards;
        }

        /// <summary>Deletes the board file for <paramref name="boardId"/>.</summary>
        public void DeleteBoard(string boardId)
        {
            string path = BoardFilePath(boardId);
            if (File.Exists(path))
                File.Delete(path);
        }

        /// <summary>Returns the set of custom folder names persisted in
        /// <c>creavity_folders.json</c>.</summary>
        public HashSet<string> GetCustomFolders()
        {
            string path = Path.Combine(_boardsDir, FoldersFileName);
            if (!File.Exists(path)) return new HashSet<string>(StringComparer.Ordinal);

            try
            {
                string json = File.ReadAllText(path, System.Text.Encoding.UTF8);
                using JsonDocument doc = JsonDocument.Parse(json);
                var set = new HashSet<string>(StringComparer.Ordinal);
                if (doc.RootElement.ValueKind == JsonValueKind.Array)
                {
                    foreach (JsonElement el in doc.RootElement.EnumerateArray())
                    {
                        string? s = el.GetString();
                        if (!string.IsNullOrEmpty(s)) set.Add(s);
                    }
                }
                return set;
            }
            catch
            {
                return new HashSet<string>(StringComparer.Ordinal);
            }
        }

        /// <summary>Persists the custom folder set to
        /// <c>creavity_folders.json</c>.</summary>
        public void SaveCustomFolders(HashSet<string> folders)
        {
            string path = Path.Combine(_boardsDir, FoldersFileName);
            string json = JsonSerializer.Serialize(folders.ToArray(), _jsonOpts);
            File.WriteAllText(path, json, System.Text.Encoding.UTF8);
        }

        // ── Helpers ──────────────────────────────────────────────────────────────

        private string BoardFilePath(string boardId) =>
            Path.Combine(_boardsDir, boardId + ".json");

        // ── Serialization ────────────────────────────────────────────────────────

        private static string SerializeBoard(Board board)
        {
            using var ms     = new System.IO.MemoryStream();
            using var writer = new Utf8JsonWriter(ms);

            writer.WriteStartObject();
            writer.WriteString("id",          board.Id);
            writer.WriteString("title",       board.Title);
            writer.WriteNumber("activePageIndex", board.ActivePageIndex);
            writer.WriteNumber("backgroundColor",  board.BackgroundColor);
            writer.WriteNumber("createdAt",    board.CreatedAt);
            writer.WriteNumber("modifiedAt",   board.ModifiedAt);
            writer.WriteNumber("lastAccessedAt", board.LastAccessedAt);
            writer.WriteString("createdBy",   board.CreatedBy);
            writer.WriteString("ownerName",   board.OwnerName);
            writer.WriteBoolean("isReadOnly", board.IsReadOnly);
            writer.WriteBoolean("isStarred",  board.IsStarred);
            writer.WriteBoolean("isInBin",    board.IsInBin);

            if (board.BinnedAt.HasValue && board.BinnedAt.Value != 0L)
                writer.WriteNumber("binnedAt", board.BinnedAt.Value);
            else
                writer.WriteNull("binnedAt");

            writer.WriteBoolean("isInfiniteCanvas", board.IsInfiniteCanvas);
            writer.WriteNumber("canvasWidth",  board.CanvasWidth);
            writer.WriteNumber("canvasHeight", board.CanvasHeight);

            if (board.PresentationUrl is not null)
                writer.WriteString("presentationUrl", board.PresentationUrl);
            else
                writer.WriteNull("presentationUrl");

            writer.WriteString("venue", board.Venue);
            writer.WriteString("group", board.Group);

            writer.WriteStartArray("labels");
            foreach (string lbl in board.Labels) writer.WriteStringValue(lbl);
            writer.WriteEndArray();

            writer.WriteStartArray("pages");
            foreach (BoardPage page in board.Pages) WritePage(writer, page);
            writer.WriteEndArray();

            writer.WriteEndObject();
            writer.Flush();

            return System.Text.Encoding.UTF8.GetString(ms.ToArray());
        }

        private static void WritePage(Utf8JsonWriter w, BoardPage page)
        {
            w.WriteStartObject();
            w.WriteString("id",   page.Id);
            w.WriteString("name", page.Name);

            // strokes
            w.WriteStartArray("strokes");
            foreach (BoardStroke stroke in page.Strokes)
            {
                w.WriteStartObject();
                w.WriteString("id",          stroke.Id);
                w.WriteNumber("color",       stroke.Color);
                w.WriteNumber("strokeWidth", stroke.StrokeWidth);
                w.WriteString("tool",        stroke.Tool.ToString());
                w.WriteBoolean("isEraser",   stroke.IsEraser);
                w.WriteString("author",      stroke.Author);
                w.WriteNumber("createdAt",   stroke.CreatedAt);

                w.WriteStartArray("points");
                foreach (StrokePoint pt in stroke.Points)
                {
                    w.WriteStartObject();
                    w.WriteNumber("x", pt.X);
                    w.WriteNumber("y", pt.Y);
                    w.WriteNumber("p", pt.P);
                    w.WriteNumber("t", pt.T);
                    if (pt.Tilt.HasValue) w.WriteNumber("tilt", pt.Tilt.Value);
                    if (pt.Ori.HasValue)  w.WriteNumber("ori",  pt.Ori.Value);
                    w.WriteEndObject();
                }
                w.WriteEndArray();

                w.WriteEndObject();
            }
            w.WriteEndArray();

            // textElements
            w.WriteStartArray("textElements");
            foreach (TextElement te in page.TextElements)
            {
                w.WriteStartObject();
                w.WriteString("id",       te.Id);
                w.WriteString("text",     te.Text);
                w.WriteNumber("x",        te.X);
                w.WriteNumber("y",        te.Y);
                w.WriteNumber("color",    te.Color);
                w.WriteNumber("fontSize", te.FontSize);
                w.WriteString("fontName", te.FontName);
                w.WriteString("author",   te.Author);
                w.WriteNumber("createdAt", te.CreatedAt);
                w.WriteEndObject();
            }
            w.WriteEndArray();

            // imageElements
            w.WriteStartArray("imageElements");
            foreach (ImageElement ie in page.ImageElements)
            {
                w.WriteStartObject();
                w.WriteString("id",        ie.Id);
                w.WriteString("path",      ie.Path);
                w.WriteNumber("x",         ie.X);
                w.WriteNumber("y",         ie.Y);
                w.WriteNumber("width",     ie.Width);
                w.WriteNumber("height",    ie.Height);
                w.WriteString("author",    ie.Author);
                w.WriteNumber("createdAt", ie.CreatedAt);
                w.WriteEndObject();
            }
            w.WriteEndArray();

            // stickyNotes
            w.WriteStartArray("stickyNotes");
            foreach (StickyNote sn in page.StickyNotes)
            {
                w.WriteStartObject();
                w.WriteString("id",        sn.Id);
                w.WriteString("text",      sn.Text);
                w.WriteNumber("x",         sn.X);
                w.WriteNumber("y",         sn.Y);
                w.WriteNumber("width",     sn.Width);
                w.WriteNumber("height",    sn.Height);
                w.WriteNumber("color",     sn.Color);
                w.WriteString("author",    sn.Author);
                w.WriteNumber("createdAt", sn.CreatedAt);
                w.WriteEndObject();
            }
            w.WriteEndArray();

            w.WriteEndObject();
        }

        // ── Deserialization ──────────────────────────────────────────────────────

        private static Board? ParseBoard(JsonElement root)
        {
            if (root.ValueKind != JsonValueKind.Object) return null;

            var board = new Board
            {
                Id              = OptString(root, "id"),
                Title           = OptString(root, "title"),
                ActivePageIndex = OptInt(root,    "activePageIndex"),
                BackgroundColor = OptLong(root,   "backgroundColor"),
                CreatedAt       = OptLong(root,   "createdAt"),
                ModifiedAt      = OptLong(root,   "modifiedAt"),
                LastAccessedAt  = OptLong(root,   "lastAccessedAt"),
                CreatedBy       = OptString(root, "createdBy"),
                OwnerName       = OptString(root, "ownerName"),
                IsReadOnly      = OptBool(root,   "isReadOnly"),
                IsStarred       = OptBool(root,   "isStarred"),
                IsInBin         = OptBool(root,   "isInBin"),
                BinnedAt        = OptNullableLong(root, "binnedAt"),
                IsInfiniteCanvas = OptBool(root,  "isInfiniteCanvas"),
                CanvasWidth     = OptFloat(root,  "canvasWidth"),
                CanvasHeight    = OptFloat(root,  "canvasHeight"),
                PresentationUrl = OptNullableString(root, "presentationUrl"),
                Venue           = OptString(root, "venue"),
                Group           = OptString(root, "group"),
                Labels          = ParseStringArray(root, "labels"),
                Pages           = ParsePages(root)
            };

            return board;
        }

        private static List<BoardPage> ParsePages(JsonElement root)
        {
            var list = new List<BoardPage>();
            if (!root.TryGetProperty("pages", out JsonElement arr) ||
                arr.ValueKind != JsonValueKind.Array) return list;

            foreach (JsonElement el in arr.EnumerateArray())
            {
                list.Add(new BoardPage
                {
                    Id            = OptString(el, "id"),
                    Name          = OptString(el, "name"),
                    Strokes       = ParseStrokes(el),
                    TextElements  = ParseTextElements(el),
                    ImageElements = ParseImageElements(el),
                    StickyNotes   = ParseStickyNotes(el)
                });
            }
            return list;
        }

        private static List<BoardStroke> ParseStrokes(JsonElement page)
        {
            var list = new List<BoardStroke>();
            if (!page.TryGetProperty("strokes", out JsonElement arr) ||
                arr.ValueKind != JsonValueKind.Array) return list;

            foreach (JsonElement el in arr.EnumerateArray())
            {
                string toolName = OptString(el, "tool");
                DrawingTool tool = Enum.TryParse<DrawingTool>(toolName, ignoreCase: true, out DrawingTool parsed)
                    ? parsed
                    : DrawingTool.Pen;

                list.Add(new BoardStroke
                {
                    Id          = OptString(el, "id"),
                    Color       = OptLong(el,   "color"),
                    StrokeWidth = OptFloat(el,  "strokeWidth"),
                    Tool        = tool,
                    IsEraser    = OptBool(el,   "isEraser"),
                    Author      = OptString(el, "author"),
                    CreatedAt   = OptLong(el,   "createdAt"),
                    Points      = ParsePoints(el)
                });
            }
            return list;
        }

        private static List<StrokePoint> ParsePoints(JsonElement stroke)
        {
            var list = new List<StrokePoint>();
            if (!stroke.TryGetProperty("points", out JsonElement arr) ||
                arr.ValueKind != JsonValueKind.Array) return list;

            foreach (JsonElement el in arr.EnumerateArray())
            {
                list.Add(new StrokePoint
                {
                    X    = OptFloat(el, "x"),
                    Y    = OptFloat(el, "y"),
                    P    = OptFloat(el, "p"),
                    T    = OptLong(el,  "t"),
                    Tilt = OptNullableFloat(el, "tilt"),
                    Ori  = OptNullableFloat(el, "ori")
                });
            }
            return list;
        }

        private static List<TextElement> ParseTextElements(JsonElement page)
        {
            var list = new List<TextElement>();
            if (!page.TryGetProperty("textElements", out JsonElement arr) ||
                arr.ValueKind != JsonValueKind.Array) return list;

            foreach (JsonElement el in arr.EnumerateArray())
            {
                list.Add(new TextElement
                {
                    Id        = OptString(el, "id"),
                    Text      = OptString(el, "text"),
                    X         = OptFloat(el,  "x"),
                    Y         = OptFloat(el,  "y"),
                    Color     = OptLong(el,   "color"),
                    FontSize  = OptFloat(el,  "fontSize"),
                    FontName  = OptString(el, "fontName"),
                    Author    = OptString(el, "author"),
                    CreatedAt = OptLong(el,   "createdAt")
                });
            }
            return list;
        }

        private static List<ImageElement> ParseImageElements(JsonElement page)
        {
            var list = new List<ImageElement>();
            if (!page.TryGetProperty("imageElements", out JsonElement arr) ||
                arr.ValueKind != JsonValueKind.Array) return list;

            foreach (JsonElement el in arr.EnumerateArray())
            {
                list.Add(new ImageElement
                {
                    Id        = OptString(el, "id"),
                    Path      = OptString(el, "path"),
                    X         = OptFloat(el,  "x"),
                    Y         = OptFloat(el,  "y"),
                    Width     = OptFloat(el,  "width"),
                    Height    = OptFloat(el,  "height"),
                    Author    = OptString(el, "author"),
                    CreatedAt = OptLong(el,   "createdAt")
                });
            }
            return list;
        }

        private static List<StickyNote> ParseStickyNotes(JsonElement page)
        {
            var list = new List<StickyNote>();
            if (!page.TryGetProperty("stickyNotes", out JsonElement arr) ||
                arr.ValueKind != JsonValueKind.Array) return list;

            foreach (JsonElement el in arr.EnumerateArray())
            {
                list.Add(new StickyNote
                {
                    Id        = OptString(el, "id"),
                    Text      = OptString(el, "text"),
                    X         = OptFloat(el,  "x"),
                    Y         = OptFloat(el,  "y"),
                    Width     = OptFloat(el,  "width"),
                    Height    = OptFloat(el,  "height"),
                    Color     = OptLong(el,   "color"),
                    Author    = OptString(el, "author"),
                    CreatedAt = OptLong(el,   "createdAt")
                });
            }
            return list;
        }

        private static List<string> ParseStringArray(JsonElement root, string key)
        {
            var list = new List<string>();
            if (!root.TryGetProperty(key, out JsonElement arr) ||
                arr.ValueKind != JsonValueKind.Array) return list;

            foreach (JsonElement el in arr.EnumerateArray())
            {
                string? s = el.GetString();
                if (s is not null) list.Add(s);
            }
            return list;
        }

        // ── JsonElement helpers (mirrors Kotlin optString / optDouble) ────────────

        private static string OptString(JsonElement el, string key)
        {
            if (el.TryGetProperty(key, out JsonElement v) && v.ValueKind == JsonValueKind.String)
                return v.GetString() ?? string.Empty;
            return string.Empty;
        }

        private static string? OptNullableString(JsonElement el, string key)
        {
            if (!el.TryGetProperty(key, out JsonElement v)) return null;
            if (v.ValueKind == JsonValueKind.String) return v.GetString();
            return null;
        }

        private static bool OptBool(JsonElement el, string key)
        {
            if (el.TryGetProperty(key, out JsonElement v))
            {
                if (v.ValueKind == JsonValueKind.True)  return true;
                if (v.ValueKind == JsonValueKind.False) return false;
            }
            return false;
        }

        private static int OptInt(JsonElement el, string key)
        {
            if (el.TryGetProperty(key, out JsonElement v) && v.TryGetInt32(out int i))
                return i;
            return 0;
        }

        private static long OptLong(JsonElement el, string key)
        {
            if (el.TryGetProperty(key, out JsonElement v) && v.TryGetInt64(out long l))
                return l;
            return 0L;
        }

        private static long? OptNullableLong(JsonElement el, string key)
        {
            if (!el.TryGetProperty(key, out JsonElement v)) return null;
            if (v.ValueKind == JsonValueKind.Null) return null;
            if (v.TryGetInt64(out long l)) return l == 0L ? null : l;
            return null;
        }

        private static float OptFloat(JsonElement el, string key)
        {
            if (el.TryGetProperty(key, out JsonElement v) && v.TryGetSingle(out float f))
                return f;
            if (el.TryGetProperty(key, out JsonElement v2) && v2.TryGetDouble(out double d))
                return (float)d;
            return 0f;
        }

        private static float? OptNullableFloat(JsonElement el, string key)
        {
            if (!el.TryGetProperty(key, out JsonElement v)) return null;
            if (v.ValueKind == JsonValueKind.Null) return null;
            if (v.TryGetSingle(out float f)) return f;
            if (v.TryGetDouble(out double d)) return (float)d;
            return null;
        }
    }
}
