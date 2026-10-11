using System;
using System.Collections.Generic;
using System.Linq;
using CreaVITy.Windows.Domain.Models;

namespace CreaVITy.Windows.Domain.Mutations;

/// <summary>
/// Base record for all board mutations. Each subtype describes a reversible
/// change to a <see cref="BoardPage"/> and can be applied or reverted.
/// </summary>
public abstract record BoardMutation
{
    public string Id { get; init; } = Guid.NewGuid().ToString();
    public long Timestamp { get; init; } = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();

    /// <summary>Applies the mutation to <paramref name="page"/> and returns the updated page.</summary>
    public abstract BoardPage Apply(BoardPage page);

    /// <summary>Reverts the mutation from <paramref name="page"/> and returns the restored page.</summary>
    public abstract BoardPage Revert(BoardPage page);

    // ── Stroke mutations ────────────────────────────────────────────────────

    /// <summary>Adds a stroke to the page, replacing any existing stroke with the same ID.</summary>
    public sealed record AddStroke(
        Stroke Stroke,
        string Id = "",
        long Timestamp = 0) : BoardMutation
    {
        public new string Id { get; init; } = string.IsNullOrEmpty(Id) ? Guid.NewGuid().ToString() : Id;
        public new long Timestamp { get; init; } = Timestamp == 0 ? DateTimeOffset.UtcNow.ToUnixTimeMilliseconds() : Timestamp;

        public override BoardPage Apply(BoardPage page) =>
            page with { Strokes = page.Strokes.Where(s => s.Id != Stroke.Id).Append(Stroke).ToList() };

        public override BoardPage Revert(BoardPage page) =>
            page with { Strokes = page.Strokes.Where(s => s.Id != Stroke.Id).ToList() };
    }

    /// <summary>Removes a stroke from the page; reverts by re-inserting if absent.</summary>
    public sealed record DeleteStroke(
        Stroke Stroke,
        string Id = "",
        long Timestamp = 0) : BoardMutation
    {
        public new string Id { get; init; } = string.IsNullOrEmpty(Id) ? Guid.NewGuid().ToString() : Id;
        public new long Timestamp { get; init; } = Timestamp == 0 ? DateTimeOffset.UtcNow.ToUnixTimeMilliseconds() : Timestamp;

        public override BoardPage Apply(BoardPage page) =>
            page with { Strokes = page.Strokes.Where(s => s.Id != Stroke.Id).ToList() };

        public override BoardPage Revert(BoardPage page) =>
            page.Strokes.All(s => s.Id != Stroke.Id)
                ? page with { Strokes = page.Strokes.Append(Stroke).ToList() }
                : page;
    }

    // ── Text element mutations ──────────────────────────────────────────────

    /// <summary>Adds a text element, replacing any existing one with the same ID.</summary>
    public sealed record AddText(
        TextElement TextElement,
        string Id = "",
        long Timestamp = 0) : BoardMutation
    {
        public new string Id { get; init; } = string.IsNullOrEmpty(Id) ? Guid.NewGuid().ToString() : Id;
        public new long Timestamp { get; init; } = Timestamp == 0 ? DateTimeOffset.UtcNow.ToUnixTimeMilliseconds() : Timestamp;

        public override BoardPage Apply(BoardPage page) =>
            page with { TextElements = page.TextElements.Where(t => t.Id != TextElement.Id).Append(TextElement).ToList() };

        public override BoardPage Revert(BoardPage page) =>
            page with { TextElements = page.TextElements.Where(t => t.Id != TextElement.Id).ToList() };
    }

    /// <summary>Removes a text element; reverts by re-inserting if absent.</summary>
    public sealed record DeleteText(
        TextElement TextElement,
        string Id = "",
        long Timestamp = 0) : BoardMutation
    {
        public new string Id { get; init; } = string.IsNullOrEmpty(Id) ? Guid.NewGuid().ToString() : Id;
        public new long Timestamp { get; init; } = Timestamp == 0 ? DateTimeOffset.UtcNow.ToUnixTimeMilliseconds() : Timestamp;

        public override BoardPage Apply(BoardPage page) =>
            page with { TextElements = page.TextElements.Where(t => t.Id != TextElement.Id).ToList() };

        public override BoardPage Revert(BoardPage page) =>
            page.TextElements.All(t => t.Id != TextElement.Id)
                ? page with { TextElements = page.TextElements.Append(TextElement).ToList() }
                : page;
    }

    // ── Image element mutations ─────────────────────────────────────────────

    /// <summary>Adds an image element, replacing any existing one with the same ID.</summary>
    public sealed record AddImage(
        ImageElement ImageElement,
        string Id = "",
        long Timestamp = 0) : BoardMutation
    {
        public new string Id { get; init; } = string.IsNullOrEmpty(Id) ? Guid.NewGuid().ToString() : Id;
        public new long Timestamp { get; init; } = Timestamp == 0 ? DateTimeOffset.UtcNow.ToUnixTimeMilliseconds() : Timestamp;

        public override BoardPage Apply(BoardPage page) =>
            page with { ImageElements = page.ImageElements.Where(i => i.Id != ImageElement.Id).Append(ImageElement).ToList() };

        public override BoardPage Revert(BoardPage page) =>
            page with { ImageElements = page.ImageElements.Where(i => i.Id != ImageElement.Id).ToList() };
    }

    /// <summary>Removes an image element; reverts by re-inserting if absent.</summary>
    public sealed record DeleteImage(
        ImageElement ImageElement,
        string Id = "",
        long Timestamp = 0) : BoardMutation
    {
        public new string Id { get; init; } = string.IsNullOrEmpty(Id) ? Guid.NewGuid().ToString() : Id;
        public new long Timestamp { get; init; } = Timestamp == 0 ? DateTimeOffset.UtcNow.ToUnixTimeMilliseconds() : Timestamp;

        public override BoardPage Apply(BoardPage page) =>
            page with { ImageElements = page.ImageElements.Where(i => i.Id != ImageElement.Id).ToList() };

        public override BoardPage Revert(BoardPage page) =>
            page.ImageElements.All(i => i.Id != ImageElement.Id)
                ? page with { ImageElements = page.ImageElements.Append(ImageElement).ToList() }
                : page;
    }

    // ── Batch / compound mutations ──────────────────────────────────────────

    /// <summary>
    /// Records a move of one or more strokes, texts, and images.
    /// Apply swaps in new positions; Revert restores previous positions.
    /// </summary>
    public sealed record MoveElements(
        IReadOnlyList<Stroke> PreviousStrokes,
        IReadOnlyList<Stroke> NewStrokes,
        IReadOnlyList<TextElement> PreviousTexts,
        IReadOnlyList<TextElement> NewTexts,
        IReadOnlyList<ImageElement> PreviousImages,
        IReadOnlyList<ImageElement> NewImages,
        string Id = "",
        long Timestamp = 0) : BoardMutation
    {
        public new string Id { get; init; } = string.IsNullOrEmpty(Id) ? Guid.NewGuid().ToString() : Id;
        public new long Timestamp { get; init; } = Timestamp == 0 ? DateTimeOffset.UtcNow.ToUnixTimeMilliseconds() : Timestamp;

        public override BoardPage Apply(BoardPage page)
        {
            var newStrokeMap = NewStrokes.ToDictionary(s => s.Id);
            var newTextMap = NewTexts.ToDictionary(t => t.Id);
            var newImageMap = NewImages.ToDictionary(i => i.Id);

            return page with
            {
                Strokes = page.Strokes
                    .Select(s => newStrokeMap.TryGetValue(s.Id, out var n) ? n : s)
                    .ToList(),
                TextElements = page.TextElements
                    .Select(t => newTextMap.TryGetValue(t.Id, out var n) ? n : t)
                    .ToList(),
                ImageElements = page.ImageElements
                    .Select(i => newImageMap.TryGetValue(i.Id, out var n) ? n : i)
                    .ToList()
            };
        }

        public override BoardPage Revert(BoardPage page)
        {
            var prevStrokeMap = PreviousStrokes.ToDictionary(s => s.Id);
            var prevTextMap = PreviousTexts.ToDictionary(t => t.Id);
            var prevImageMap = PreviousImages.ToDictionary(i => i.Id);

            return page with
            {
                Strokes = page.Strokes
                    .Select(s => prevStrokeMap.TryGetValue(s.Id, out var p) ? p : s)
                    .ToList(),
                TextElements = page.TextElements
                    .Select(t => prevTextMap.TryGetValue(t.Id, out var p) ? p : t)
                    .ToList(),
                ImageElements = page.ImageElements
                    .Select(i => prevImageMap.TryGetValue(i.Id, out var p) ? p : i)
                    .ToList()
            };
        }
    }

    /// <summary>
    /// Records a color change for one or more strokes and texts.
    /// Apply swaps in recolored versions; Revert restores previous colors.
    /// </summary>
    public sealed record RecolorElements(
        IReadOnlyList<Stroke> PreviousStrokes,
        IReadOnlyList<Stroke> NewStrokes,
        IReadOnlyList<TextElement> PreviousTexts,
        IReadOnlyList<TextElement> NewTexts,
        string Id = "",
        long Timestamp = 0) : BoardMutation
    {
        public new string Id { get; init; } = string.IsNullOrEmpty(Id) ? Guid.NewGuid().ToString() : Id;
        public new long Timestamp { get; init; } = Timestamp == 0 ? DateTimeOffset.UtcNow.ToUnixTimeMilliseconds() : Timestamp;

        public override BoardPage Apply(BoardPage page)
        {
            var newStrokeMap = NewStrokes.ToDictionary(s => s.Id);
            var newTextMap = NewTexts.ToDictionary(t => t.Id);

            return page with
            {
                Strokes = page.Strokes
                    .Select(s => newStrokeMap.TryGetValue(s.Id, out var n) ? n : s)
                    .ToList(),
                TextElements = page.TextElements
                    .Select(t => newTextMap.TryGetValue(t.Id, out var n) ? n : t)
                    .ToList()
            };
        }

        public override BoardPage Revert(BoardPage page)
        {
            var prevStrokeMap = PreviousStrokes.ToDictionary(s => s.Id);
            var prevTextMap = PreviousTexts.ToDictionary(t => t.Id);

            return page with
            {
                Strokes = page.Strokes
                    .Select(s => prevStrokeMap.TryGetValue(s.Id, out var p) ? p : s)
                    .ToList(),
                TextElements = page.TextElements
                    .Select(t => prevTextMap.TryGetValue(t.Id, out var p) ? p : t)
                    .ToList()
            };
        }
    }

    /// <summary>Clears all content from the page; Revert restores the previous state.</summary>
    public sealed record ClearPage(
        IReadOnlyList<Stroke> PreviousStrokes,
        IReadOnlyList<TextElement> PreviousTexts,
        IReadOnlyList<ImageElement> PreviousImages,
        string Id = "",
        long Timestamp = 0) : BoardMutation
    {
        public new string Id { get; init; } = string.IsNullOrEmpty(Id) ? Guid.NewGuid().ToString() : Id;
        public new long Timestamp { get; init; } = Timestamp == 0 ? DateTimeOffset.UtcNow.ToUnixTimeMilliseconds() : Timestamp;

        public override BoardPage Apply(BoardPage page) =>
            page with
            {
                Strokes = [],
                TextElements = [],
                ImageElements = []
            };

        public override BoardPage Revert(BoardPage page) =>
            page with
            {
                Strokes = PreviousStrokes.ToList(),
                TextElements = PreviousTexts.ToList(),
                ImageElements = PreviousImages.ToList()
            };
    }

    /// <summary>
    /// Restores the board to a saved version (strokes + texts).
    /// Apply moves to the new version; Revert returns to the previous version.
    /// </summary>
    public sealed record RestoreVersion(
        IReadOnlyList<Stroke> PreviousStrokes,
        IReadOnlyList<Stroke> NewStrokes,
        IReadOnlyList<TextElement> PreviousTexts,
        IReadOnlyList<TextElement> NewTexts,
        string Id = "",
        long Timestamp = 0) : BoardMutation
    {
        public new string Id { get; init; } = string.IsNullOrEmpty(Id) ? Guid.NewGuid().ToString() : Id;
        public new long Timestamp { get; init; } = Timestamp == 0 ? DateTimeOffset.UtcNow.ToUnixTimeMilliseconds() : Timestamp;

        public override BoardPage Apply(BoardPage page) =>
            page with
            {
                Strokes = NewStrokes.ToList(),
                TextElements = NewTexts.ToList()
            };

        public override BoardPage Revert(BoardPage page) =>
            page with
            {
                Strokes = PreviousStrokes.ToList(),
                TextElements = PreviousTexts.ToList()
            };
    }
}
