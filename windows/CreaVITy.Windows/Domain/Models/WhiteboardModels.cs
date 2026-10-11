using System;
using System.Collections.Generic;
using System.Linq;

namespace CreaVITy.Windows.Domain.Models;

// ---------------------------------------------------------------------------
// Enumerations
// ---------------------------------------------------------------------------

/// <summary>Drawing tool options available on the whiteboard canvas.</summary>
public enum DrawingTool
{
    Pen, Marker, Highlighter, Brush, Eraser, Select,
    StickyNote, Laser, Pencil, Calligraphy, Line, Arrow,
    Rectangle, Circle, Text, SelectErase, SelectMove,
    RectSelect, LassoSelect, Triangle
}

/// <summary>Type of modification applied to the canvas undo/redo log.</summary>
public enum ModificationType
{
    AddStroke, DeleteStroke,
    AddText, DeleteText,
    AddImage, DeleteImage,
    AddStickyNote, DeleteStickyNote,
    MoveElements, RecolorElements,
    ClearPage, RestoreVersion
}

/// <summary>Role a peer device plays in a collaborative mesh session.</summary>
public enum MeshRole { Host, Editor, Viewer }

// ---------------------------------------------------------------------------
// Core drawing primitives
// ---------------------------------------------------------------------------

/// <summary>
/// A single sampled point in a stroke, including stylus metadata.
/// </summary>
/// <param name="X">Canvas X coordinate.</param>
/// <param name="Y">Canvas Y coordinate.</param>
/// <param name="Pressure">Stylus pressure [0, 1]. Defaults to 1.0.</param>
/// <param name="TimestampMs">UTC epoch milliseconds at sample time.</param>
/// <param name="Tilt">Stylus tilt angle in radians.</param>
/// <param name="Orientation">Stylus orientation angle in radians.</param>
public sealed record Point(
    float X,
    float Y,
    float Pressure = 1.0f,
    long TimestampMs = 0,
    float Tilt = 0f,
    float Orientation = 0f)
{
    /// <summary>UTC epoch milliseconds, defaulting to now when not supplied.</summary>
    public long TimestampMs { get; init; } =
        TimestampMs == 0 ? DateTimeOffset.UtcNow.ToUnixTimeMilliseconds() : TimestampMs;
}

/// <summary>
/// An ink stroke composed of sampled points with rendering metadata.
/// </summary>
public sealed record Stroke
{
    /// <summary>Unique identifier for this stroke.</summary>
    public string Id { get; init; } = Guid.NewGuid().ToString();

    /// <summary>Ordered list of sampled points.</summary>
    public IReadOnlyList<Point> Points { get; init; } = Array.Empty<Point>();

    /// <summary>ARGB-packed color (matches Android/Kotlin convention).</summary>
    public long Color { get; init; } = unchecked((long)0xFFFFFFFF);

    /// <summary>Stroke width in canvas units.</summary>
    public float StrokeWidth { get; init; } = 5.0f;

    /// <summary>Drawing tool used to produce this stroke.</summary>
    public DrawingTool Tool { get; init; } = DrawingTool.Pen;

    /// <summary>True when the stroke was drawn with the eraser tool.</summary>
    public bool IsEraser { get; init; }

    /// <summary>Display name of the author.</summary>
    public string Author { get; init; } = "You";

    /// <summary>UTC epoch milliseconds when the stroke was created.</summary>
    public long CreatedAt { get; init; } = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();

    /// <summary>
    /// Initialises <see cref="IsEraser"/> from <see cref="Tool"/> when not
    /// explicitly provided.
    /// </summary>
    public Stroke() { IsEraser = Tool == DrawingTool.Eraser; }
}

// ---------------------------------------------------------------------------
// Canvas elements
// ---------------------------------------------------------------------------

/// <summary>A text annotation placed at an arbitrary position on the canvas.</summary>
public sealed record TextElement
{
    /// <summary>Unique identifier.</summary>
    public string Id { get; init; } = Guid.NewGuid().ToString();

    /// <summary>Text content.</summary>
    public string Text { get; init; } = string.Empty;

    /// <summary>Canvas X position.</summary>
    public float X { get; init; } = 0f;

    /// <summary>Canvas Y position.</summary>
    public float Y { get; init; } = 0f;

    /// <summary>ARGB-packed text color.</summary>
    public long Color { get; init; } = unchecked((long)0xFFFFFFFF);

    /// <summary>Font size in SP units.</summary>
    public float FontSize { get; init; } = 24.0f;

    /// <summary>Font family name (Windows default: Segoe UI).</summary>
    public string FontName { get; init; } = "Segoe UI";

    /// <summary>Display name of the author.</summary>
    public string Author { get; init; } = "You";

    /// <summary>UTC epoch milliseconds when the element was created.</summary>
    public long CreatedAt { get; init; } = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
}

/// <summary>An image element placed at an arbitrary position on the canvas.</summary>
public sealed record ImageElement
{
    /// <summary>Unique identifier.</summary>
    public string Id { get; init; } = Guid.NewGuid().ToString();

    /// <summary>Absolute or relative path to the image resource.</summary>
    public string Path { get; init; } = string.Empty;

    /// <summary>Canvas X position.</summary>
    public float X { get; init; } = 0f;

    /// <summary>Canvas Y position.</summary>
    public float Y { get; init; } = 0f;

    /// <summary>Rendered width in canvas units.</summary>
    public float Width { get; init; } = 240f;

    /// <summary>Rendered height in canvas units.</summary>
    public float Height { get; init; } = 240f;

    /// <summary>Display name of the author.</summary>
    public string Author { get; init; } = "You";

    /// <summary>UTC epoch milliseconds when the element was created.</summary>
    public long CreatedAt { get; init; } = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
}

/// <summary>A sticky-note element placed at an arbitrary position on the canvas.</summary>
public sealed record StickyNote
{
    /// <summary>Unique identifier.</summary>
    public string Id { get; init; } = Guid.NewGuid().ToString();

    /// <summary>Text content of the note.</summary>
    public string Text { get; init; } = string.Empty;

    /// <summary>Canvas X position.</summary>
    public float X { get; init; } = 0f;

    /// <summary>Canvas Y position.</summary>
    public float Y { get; init; } = 0f;

    /// <summary>Width in canvas units.</summary>
    public float Width { get; init; } = 220f;

    /// <summary>Height in canvas units.</summary>
    public float Height { get; init; } = 220f;

    /// <summary>ARGB-packed background color (default: yellow #FFF176).</summary>
    public long Color { get; init; } = unchecked((long)0xFFFFF176);

    /// <summary>Display name of the author.</summary>
    public string Author { get; init; } = "You";

    /// <summary>UTC epoch milliseconds when the note was created.</summary>
    public long CreatedAt { get; init; } = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
}

// ---------------------------------------------------------------------------
// Collaboration / presence
// ---------------------------------------------------------------------------

/// <summary>Visible region of the canvas in canvas-space coordinates.</summary>
public sealed record ViewportBounds
{
    /// <summary>Left edge in canvas units.</summary>
    public float Left { get; init; } = 0f;

    /// <summary>Top edge in canvas units.</summary>
    public float Top { get; init; } = 0f;

    /// <summary>Right edge in canvas units (default: 1920).</summary>
    public float Right { get; init; } = 1920f;

    /// <summary>Bottom edge in canvas units (default: 1080).</summary>
    public float Bottom { get; init; } = 1080f;
}

/// <summary>Real-time presence state for a remote collaborating peer.</summary>
public sealed record PeerPresence
{
    /// <summary>Network endpoint identifier for the peer.</summary>
    public string PeerId { get; init; } = string.Empty;

    /// <summary>Display name of the peer.</summary>
    public string Name { get; init; } = string.Empty;

    /// <summary>Last reported canvas point (null if not drawing).</summary>
    public Point? CurrentPoint { get; init; } = null;

    /// <summary>Whether the peer is actively drawing.</summary>
    public bool IsDrawing { get; init; } = false;

    /// <summary>Currently selected drawing tool on the peer's device.</summary>
    public DrawingTool ActiveTool { get; init; } = DrawingTool.Pen;

    /// <summary>ARGB-packed cursor/highlight color for this peer.</summary>
    public long Color { get; init; } = unchecked((long)0xFFDC5F00);

    /// <summary>Peer's current viewport, if known.</summary>
    public ViewportBounds? Viewport { get; init; } = null;

    /// <summary>UTC epoch milliseconds of last presence update.</summary>
    public long LastUpdatedMs { get; init; } = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();

    /// <summary>Required: peer ID must be supplied at construction.</summary>
    public PeerPresence(string peerId, string name)
    {
        PeerId = peerId;
        Name = name;
    }

    // Parameterless ctor for record with-expressions and deserialisers.
    private PeerPresence() { }
}

// ---------------------------------------------------------------------------
// Undo / redo modification log
// ---------------------------------------------------------------------------

/// <summary>
/// A single entry in the canvas modification log, used for undo/redo and
/// collaborative conflict resolution.
/// </summary>
public sealed record CanvasModification
{
    /// <summary>Unique identifier for this modification.</summary>
    public string Id { get; init; } = Guid.NewGuid().ToString();

    /// <summary>Kind of modification performed.</summary>
    public required ModificationType Type { get; init; }

    /// <summary>UTC epoch milliseconds when the modification was applied.</summary>
    public long Timestamp { get; init; } = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();

    /// <summary>Display name of the author.</summary>
    public string Author { get; init; } = "You";

    /// <summary>Zero-based index of the page this modification targets.</summary>
    public int PageIndex { get; init; } = 0;

    // -- stroke --
    /// <summary>Stroke payload (ADD_STROKE).</summary>
    public Stroke? Stroke { get; init; } = null;
    /// <summary>Stroke ID payload (DELETE_STROKE).</summary>
    public string? StrokeId { get; init; } = null;

    // -- text --
    /// <summary>Text element payload (ADD_TEXT).</summary>
    public TextElement? TextElement { get; init; } = null;
    /// <summary>Text element ID payload (DELETE_TEXT).</summary>
    public string? TextId { get; init; } = null;

    // -- image --
    /// <summary>Image element payload (ADD_IMAGE).</summary>
    public ImageElement? ImageElement { get; init; } = null;
    /// <summary>Image element ID payload (DELETE_IMAGE).</summary>
    public string? ImageId { get; init; } = null;

    // -- sticky note --
    /// <summary>Sticky-note payload (ADD_STICKY_NOTE).</summary>
    public StickyNote? StickyNote { get; init; } = null;
    /// <summary>Sticky-note ID payload (DELETE_STICKY_NOTE).</summary>
    public string? StickyNoteId { get; init; } = null;

    // -- bulk move / recolor snapshots --
    /// <summary>Stroke state before a MOVE / RECOLOR operation.</summary>
    public IReadOnlyList<Stroke> PreviousStrokes { get; init; } = Array.Empty<Stroke>();
    /// <summary>Stroke state after a MOVE / RECOLOR operation.</summary>
    public IReadOnlyList<Stroke> NewStrokes { get; init; } = Array.Empty<Stroke>();

    /// <summary>Text state before a MOVE / RECOLOR operation.</summary>
    public IReadOnlyList<TextElement> PreviousTexts { get; init; } = Array.Empty<TextElement>();
    /// <summary>Text state after a MOVE / RECOLOR operation.</summary>
    public IReadOnlyList<TextElement> NewTexts { get; init; } = Array.Empty<TextElement>();

    /// <summary>Image state before a MOVE / RECOLOR operation.</summary>
    public IReadOnlyList<ImageElement> PreviousImages { get; init; } = Array.Empty<ImageElement>();
    /// <summary>Image state after a MOVE / RECOLOR operation.</summary>
    public IReadOnlyList<ImageElement> NewImages { get; init; } = Array.Empty<ImageElement>();

    /// <summary>Sticky-note state before a MOVE / RECOLOR operation.</summary>
    public IReadOnlyList<StickyNote> PreviousStickyNotes { get; init; } = Array.Empty<StickyNote>();
    /// <summary>Sticky-note state after a MOVE / RECOLOR operation.</summary>
    public IReadOnlyList<StickyNote> NewStickyNotes { get; init; } = Array.Empty<StickyNote>();
}

// ---------------------------------------------------------------------------
// Version history
// ---------------------------------------------------------------------------

/// <summary>
/// A full-state snapshot of a board page, used for named version history.
/// </summary>
public sealed record BoardVersionSnapshot
{
    /// <summary>Unique identifier for this snapshot.</summary>
    public string Id { get; init; } = Guid.NewGuid().ToString();

    /// <summary>Human-readable version label.</summary>
    public string Name { get; init; } = "Version";

    /// <summary>UTC epoch milliseconds when the snapshot was taken.</summary>
    public long Timestamp { get; init; } = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();

    /// <summary>Display name of the author who created this snapshot.</summary>
    public string Author { get; init; } = "You";

    /// <summary>Zero-based index of the snapshotted page.</summary>
    public int PageIndex { get; init; } = 0;

    /// <summary>All strokes present at snapshot time.</summary>
    public IReadOnlyList<Stroke> Strokes { get; init; } = Array.Empty<Stroke>();

    /// <summary>All text elements present at snapshot time.</summary>
    public IReadOnlyList<TextElement> TextElements { get; init; } = Array.Empty<TextElement>();

    /// <summary>All image elements present at snapshot time.</summary>
    public IReadOnlyList<ImageElement> ImageElements { get; init; } = Array.Empty<ImageElement>();

    /// <summary>All sticky notes present at snapshot time.</summary>
    public IReadOnlyList<StickyNote> StickyNotes { get; init; } = Array.Empty<StickyNote>();

    /// <summary>Convenience count derived from <see cref="Strokes"/>.</summary>
    public int StrokeCount => Strokes.Count;

    /// <summary>Convenience count derived from <see cref="TextElements"/>.</summary>
    public int TextCount => TextElements.Count;
}

// ---------------------------------------------------------------------------
// Board page & board
// ---------------------------------------------------------------------------

/// <summary>A single page within a multi-page whiteboard.</summary>
public sealed record BoardPage
{
    /// <summary>Unique identifier.</summary>
    public string Id { get; init; } = Guid.NewGuid().ToString();

    /// <summary>Display name shown in the page navigator.</summary>
    public string Name { get; init; } = "Page 1";

    /// <summary>Strokes on this page.</summary>
    public IReadOnlyList<Stroke> Strokes { get; init; } = Array.Empty<Stroke>();

    /// <summary>Text elements on this page.</summary>
    public IReadOnlyList<TextElement> TextElements { get; init; } = Array.Empty<TextElement>();

    /// <summary>Image elements on this page.</summary>
    public IReadOnlyList<ImageElement> ImageElements { get; init; } = Array.Empty<ImageElement>();

    /// <summary>Sticky notes on this page.</summary>
    public IReadOnlyList<StickyNote> StickyNotes { get; init; } = Array.Empty<StickyNote>();
}

/// <summary>
/// Root board document containing all pages, history, metadata, and settings.
/// </summary>
public sealed record Board
{
    /// <summary>Unique identifier.</summary>
    public string Id { get; init; } = Guid.NewGuid().ToString();

    /// <summary>Human-readable board title.</summary>
    public string Title { get; init; } = "Untitled Board";

    /// <summary>Ordered list of pages; always contains at least one.</summary>
    public IReadOnlyList<BoardPage> Pages { get; init; } =
        new[] { new BoardPage(Name: "Page 1") };

    /// <summary>Zero-based index of the currently active page.</summary>
    public int ActivePageIndex { get; init; } = 0;

    /// <summary>ARGB-packed canvas background color.</summary>
    public long BackgroundColor { get; init; } = unchecked((long)0xFF18181C);

    /// <summary>UTC epoch milliseconds when the board was created.</summary>
    public long CreatedAt { get; init; } = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();

    /// <summary>UTC epoch milliseconds of the last modification.</summary>
    public long ModifiedAt { get; init; } = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();

    /// <summary>UTC epoch milliseconds of the last access (defaults to <see cref="ModifiedAt"/>).</summary>
    public long LastAccessedAt { get; init; }

    /// <summary>Username of the board creator.</summary>
    public string CreatedBy { get; init; } = "You";

    /// <summary>Display name of the board owner.</summary>
    public string OwnerName { get; init; } = "You";

    /// <summary>Whether the board is starred/pinned.</summary>
    public bool IsStarred { get; init; } = false;

    /// <summary>Whether the board is in read-only mode.</summary>
    public bool IsReadOnly { get; init; } = false;

    /// <summary>Whether the board has been moved to the bin.</summary>
    public bool IsInBin { get; init; } = false;

    /// <summary>UTC epoch milliseconds when the board was binned, or null.</summary>
    public long? BinnedAt { get; init; } = null;

    /// <summary>Whether the canvas extends infinitely in all directions.</summary>
    public bool IsInfiniteCanvas { get; init; } = true;

    /// <summary>Finite canvas width when <see cref="IsInfiniteCanvas"/> is false.</summary>
    public float CanvasWidth { get; init; } = 1920f;

    /// <summary>Finite canvas height when <see cref="IsInfiniteCanvas"/> is false.</summary>
    public float CanvasHeight { get; init; } = 1080f;

    /// <summary>Optional external presentation URL.</summary>
    public string? PresentationUrl { get; init; } = null;

    /// <summary>Named version snapshots for this board.</summary>
    public IReadOnlyList<BoardVersionSnapshot> VersionHistory { get; init; } =
        Array.Empty<BoardVersionSnapshot>();

    /// <summary>Chronological log of all canvas modifications.</summary>
    public IReadOnlyList<CanvasModification> ModificationLog { get; init; } =
        Array.Empty<CanvasModification>();

    /// <summary>User-defined labels / tags.</summary>
    public IReadOnlyList<string> Labels { get; init; } = Array.Empty<string>();

    /// <summary>Venue / location metadata.</summary>
    public string Venue { get; init; } = string.Empty;

    /// <summary>Group / team metadata.</summary>
    public string Group { get; init; } = string.Empty;

    /// <summary>
    /// Convenience accessor for the currently active <see cref="BoardPage"/>.
    /// Returns a new empty page if the index is out of range.
    /// </summary>
    public BoardPage ActivePage =>
        Pages.ElementAtOrDefault(ActivePageIndex) ?? new BoardPage();

    /// <summary>Ensures <see cref="LastAccessedAt"/> defaults to <see cref="ModifiedAt"/>.</summary>
    public Board() { LastAccessedAt = ModifiedAt; }
}

// ---------------------------------------------------------------------------
// Mesh collaboration
// ---------------------------------------------------------------------------

/// <summary>A remote peer device participating in a mesh collaboration session.</summary>
/// <param name="EndpointId">Network endpoint identifier (required).</param>
/// <param name="Name">Display name of the peer device (required).</param>
public sealed record PeerDevice(string EndpointId, string Name)
{
    /// <summary>Whether the peer is currently connected.</summary>
    public bool IsConnected { get; init; } = false;

    /// <summary>Whether the peer is in read-only (viewer) mode.</summary>
    public bool IsReadOnly { get; init; } = false;

    /// <summary>Role this peer holds in the mesh session.</summary>
    public MeshRole Role { get; init; } = MeshRole.Viewer;
}

/// <summary>
/// Complete state of a local or collaborative whiteboard room/session.
/// </summary>
public sealed record RoomState
{
    /// <summary>Human-readable room name.</summary>
    public string RoomName { get; init; } = "Local Whiteboard";

    /// <summary>Whether this device is the session host.</summary>
    public bool IsHost { get; init; } = false;

    /// <summary>All currently connected peers.</summary>
    public IReadOnlyList<PeerDevice> ConnectedPeers { get; init; } = Array.Empty<PeerDevice>();

    /// <summary>Shared strokes visible to all peers.</summary>
    public IReadOnlyList<Stroke> Strokes { get; init; } = Array.Empty<Stroke>();

    /// <summary>Shared text elements visible to all peers.</summary>
    public IReadOnlyList<TextElement> TextElements { get; init; } = Array.Empty<TextElement>();

    /// <summary>Shared image elements visible to all peers.</summary>
    public IReadOnlyList<ImageElement> ImageElements { get; init; } = Array.Empty<ImageElement>();

    /// <summary>Shared sticky notes visible to all peers.</summary>
    public IReadOnlyList<StickyNote> StickyNotes { get; init; } = Array.Empty<StickyNote>();
}
