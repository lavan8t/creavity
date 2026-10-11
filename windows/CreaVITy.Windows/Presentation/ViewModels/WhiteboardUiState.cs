using System;
using System.Collections.Generic;
using System.Linq;
using System.Numerics;
using CreaVITy.Windows.Domain.Models;

namespace CreaVITy.Windows.Presentation.ViewModels;

// ---------------------------------------------------------------------------
// Enumerations
// ---------------------------------------------------------------------------

/// <summary>Background pattern style for the whiteboard canvas.</summary>
public enum CanvasBackgroundPattern
{
    Blank,
    Dots,
    Grid,
    Ruled,
    IsometricDots
}

/// <summary>Theme mode options for the application.</summary>
public enum ThemeMode
{
    System,
    Light,
    Dark
}

/// <summary>Application theme preset options.</summary>
public enum AppThemePreset
{
    Dynamic
}

// ---------------------------------------------------------------------------
// UI State Record
// ---------------------------------------------------------------------------

/// <summary>
/// Immutable UI state representation for the whiteboard presentation layer.
/// Ported from Kotlin WhiteboardUiState.
/// </summary>
public sealed record WhiteboardUiState
{
    /// <summary>Currently active whiteboard document.</summary>
    public Board CurrentBoard { get; init; } = new Board();

    /// <summary>All locally saved whiteboard documents.</summary>
    public IReadOnlyList<Board> SavedBoards { get; init; } = Array.Empty<Board>();

    /// <summary>User-defined board folder names.</summary>
    public IReadOnlySet<string> UserFolders { get; init; } = new HashSet<string>();

    /// <summary>Current active drawing color as ARGB-packed 64-bit integer (0 = unspecified).</summary>
    public long CurrentColor { get; init; } = 0L;

    /// <summary>Canvas background color override as ARGB-packed 64-bit integer (0 = unspecified).</summary>
    public long BackgroundColor { get; init; } = 0L;

    /// <summary>Pattern rendered on the canvas background.</summary>
    public CanvasBackgroundPattern BackgroundPattern { get; init; } = CanvasBackgroundPattern.IsometricDots;

    /// <summary>Current stroke width in canvas units.</summary>
    public float CurrentWidth { get; init; } = 6.0f;

    /// <summary>Currently selected drawing tool.</summary>
    public DrawingTool SelectedTool { get; init; } = DrawingTool.Pen;

    /// <summary>Canvas zoom magnification scale factor (1.0 = 100%).</summary>
    public float Zoom { get; init; } = 1.0f;

    /// <summary>Canvas pan offset vector.</summary>
    public Vector2 PanOffset { get; init; } = Vector2.Zero;

    /// <summary>Canvas pan offset X coordinate.</summary>
    public float PanOffsetX
    {
        get => PanOffset.X;
        init => PanOffset = new Vector2(value, PanOffset.Y);
    }

    /// <summary>Canvas pan offset Y coordinate.</summary>
    public float PanOffsetY
    {
        get => PanOffset.Y;
        init => PanOffset = new Vector2(PanOffset.X, value);
    }

    /// <summary>Unique room code for collaborative mesh session, or null if local.</summary>
    public string? RoomCode { get; init; } = null;

    /// <summary>Local user's display name broadcast to mesh peers.</summary>
    public string DisplayName { get; init; } = "Whiteboard User";

    /// <summary>Whether haptic feedback is enabled.</summary>
    public bool HapticsEnabled { get; init; } = true;

    /// <summary>Whether local device is actively advertising for mesh connections.</summary>
    public bool IsAdvertising { get; init; } = false;

    /// <summary>Whether local device is actively discovering mesh peers.</summary>
    public bool IsDiscovering { get; init; } = false;

    /// <summary>Connected peer devices in the current session.</summary>
    public IReadOnlyList<PeerDevice> ConnectedPeers { get; init; } = Array.Empty<PeerDevice>();

    /// <summary>Discovered peer devices available for connection.</summary>
    public IReadOnlyList<PeerDevice> DiscoveredPeers { get; init; } = Array.Empty<PeerDevice>();

    /// <summary>Selected theme preset for the application.</summary>
    public AppThemePreset SelectedThemePreset { get; init; } = AppThemePreset.Dynamic;

    /// <summary>Application theme mode (System, Light, Dark).</summary>
    public ThemeMode ThemeMode { get; init; } = ThemeMode.System;

    /// <summary>Whether undo action is available.</summary>
    public bool CanUndo { get; init; } = false;

    /// <summary>Whether redo action is available.</summary>
    public bool CanRedo { get; init; } = false;

    /// <summary>Live presence information for remote collaborating peers indexed by peer ID.</summary>
    public IReadOnlyDictionary<string, PeerPresence> LivePeerPresences { get; init; } = new Dictionary<string, PeerPresence>();

    /// <summary>Named version snapshots of the current board.</summary>
    public IReadOnlyList<BoardVersionSnapshot> VersionHistory { get; init; } = Array.Empty<BoardVersionSnapshot>();

    /// <summary>Whether current user's access level is restricted to read-only.</summary>
    public bool MyAccessReadOnly { get; init; } = false;

    /// <summary>Whether canvas extends infinitely in all directions.</summary>
    public bool IsInfiniteCanvas { get; init; } = true;

    /// <summary>External presentation URL if presentation mode is active.</summary>
    public string? PresentationUrl { get; init; } = null;

    /// <summary>Whether the board is in full-screen presentation mode.</summary>
    public bool IsPresentationMode { get; init; } = false;

    /// <summary>Zero-based index of current presentation slide.</summary>
    public int PresentationCurrentSlide { get; init; } = 0;

    /// <summary>Total number of slides in presentation.</summary>
    public int PresentationTotalSlides { get; init; } = 1;

    // -----------------------------------------------------------------------
    // Computed Helpers
    // -----------------------------------------------------------------------

    /// <summary>
    /// Whether this device is acting as the mesh session host.
    /// Returns true if actively advertising, or not discovering and no connected peer holds the Host role.
    /// </summary>
    public bool IsHost =>
        IsAdvertising || (!IsDiscovering && !ConnectedPeers.Any(p => p.Role == MeshRole.Host));

    /// <summary>Whether the canvas cannot be modified (board is read-only or user access is read-only).</summary>
    public bool IsCanvasReadOnly =>
        CurrentBoard.IsReadOnly || MyAccessReadOnly;

    /// <summary>Convenience accessor for active page on the current board.</summary>
    public BoardPage ActivePage =>
        CurrentBoard.ActivePage;

    /// <summary>Convenience accessor for strokes on the active page.</summary>
    public IReadOnlyList<Stroke> Strokes =>
        ActivePage.Strokes;

    /// <summary>Convenience accessor for text annotations on the active page.</summary>
    public IReadOnlyList<TextElement> TextElements =>
        ActivePage.TextElements;

    /// <summary>Convenience accessor for image elements on the active page.</summary>
    public IReadOnlyList<ImageElement> ImageElements =>
        ActivePage.ImageElements;

    /// <summary>Convenience accessor for sticky notes on the active page.</summary>
    public IReadOnlyList<StickyNote> StickyNotes =>
        ActivePage.StickyNotes;
}
