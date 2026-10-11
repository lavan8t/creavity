using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Numerics;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using Windows.Storage.Streams;
using Windows.UI;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.Geometry;
using Microsoft.Graphics.Canvas.Text;
using PdfSharpCore.Drawing;
using PdfSharpCore.Pdf;
using CreaVITy.Windows.Data.Storage;
using CreaVITy.Windows.Domain.Models;

// Disambiguate types that exist in both Domain.Models and Data.Storage
using Board = CreaVITy.Windows.Domain.Models.Board;
using BoardPage = CreaVITy.Windows.Domain.Models.BoardPage;
using Stroke = CreaVITy.Windows.Domain.Models.Stroke;
using DrawingTool = CreaVITy.Windows.Domain.Models.DrawingTool;
using StickyNote = CreaVITy.Windows.Domain.Models.StickyNote;
using TextElement = CreaVITy.Windows.Domain.Models.TextElement;
using Point = CreaVITy.Windows.Domain.Models.Point;
using ImageElement = CreaVITy.Windows.Domain.Models.ImageElement;
using StorageBoard = CreaVITy.Windows.Data.Storage.Board;
using StorageBoardPage = CreaVITy.Windows.Data.Storage.BoardPage;
using StorageStroke = CreaVITy.Windows.Data.Storage.BoardStroke;
using StorageStrokePoint = CreaVITy.Windows.Data.Storage.StrokePoint;
using StorageTextElement = CreaVITy.Windows.Data.Storage.TextElement;
using StorageStickyNote = CreaVITy.Windows.Data.Storage.StickyNote;
using StorageImageElement = CreaVITy.Windows.Data.Storage.ImageElement;
using StorageDrawingTool = CreaVITy.Windows.Data.Storage.DrawingTool;

namespace CreaVITy.Windows.Export;

/// <summary>
/// Supported export formats matching the mobile CreaVITy application.
/// </summary>
public enum ExportFormat
{
    PDF,
    PNG,
    SVG,
    CREAVITY,
    BREVITY,
    Pdf = PDF,
    Png = PNG,
    Svg = SVG,
    Creavity = CREAVITY,
    Brevity = BREVITY
}

/// <summary>
/// Metadata and extension helper methods for <see cref="ExportFormat"/>.
/// </summary>
public static class ExportFormatExtensions
{
    public static string GetExtension(this ExportFormat format) => format switch
    {
        ExportFormat.PDF => "pdf",
        ExportFormat.PNG => "png",
        ExportFormat.SVG => "svg",
        ExportFormat.CREAVITY => "creavity",
        ExportFormat.BREVITY => "breVITy",
        _ => "png"
    };

    public static string GetMimeType(this ExportFormat format) => format switch
    {
        ExportFormat.PDF => "application/pdf",
        ExportFormat.PNG => "image/png",
        ExportFormat.SVG => "image/svg+xml",
        ExportFormat.CREAVITY => "application/json",
        ExportFormat.BREVITY => "application/json",
        _ => "application/octet-stream"
    };

    public static string GetDisplayName(this ExportFormat format) => format switch
    {
        ExportFormat.PDF => "PDF Document",
        ExportFormat.PNG => "PNG Image",
        ExportFormat.SVG => "SVG Vector",
        ExportFormat.CREAVITY => "CreaVITy Backup",
        ExportFormat.BREVITY => "CreaVITy Backup",
        _ => "Export"
    };

    public static string GetDescription(this ExportFormat format) => format switch
    {
        ExportFormat.PDF => "Multi-page vector document for sharing or printing",
        ExportFormat.PNG => "High-resolution raster image of the active board",
        ExportFormat.SVG => "Scalable vector graphic compatible with Figma & Illustrator",
        ExportFormat.CREAVITY => "Native lossless whiteboard file for backup and transfer",
        ExportFormat.BREVITY => "Native lossless whiteboard file for backup and transfer",
        _ => string.Empty
    };

    public static string GetIconName(this ExportFormat format) => format switch
    {
        ExportFormat.PDF => "picture_as_pdf",
        ExportFormat.PNG => "image",
        ExportFormat.SVG => "auto_awesome",
        ExportFormat.CREAVITY => "folder",
        ExportFormat.BREVITY => "folder",
        _ => "folder"
    };
}

/// <summary>
/// Exports CreaVITy whiteboard documents to various vector, raster, and native file formats.
/// Ported from Kotlin BoardExporter.
/// </summary>
public static class BoardExporter
{
    private const float DefaultCanvasWidth = 1920f;
    private const float DefaultCanvasHeight = 1080f;

    /// <summary>
    /// Exports the specified board in the given format to the local exports directory and returns the file path.
    /// </summary>
    /// <param name="board">Board to export.</param>
    /// <param name="format">Export format (PDF, PNG, SVG, CREAVITY, BREVITY).</param>
    /// <param name="pageIndex">Target page index for single-page exports (-1 uses <see cref="Board.ActivePageIndex"/>).</param>
    /// <returns>Absolute path to the exported file.</returns>
    public static async Task<string> ExportBoard(Board board, ExportFormat format, int pageIndex = -1)
    {
        ArgumentNullException.ThrowIfNull(board);

        string exportDir = Path.Combine(
            Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
            "CreaVITy",
            "exports");
        Directory.CreateDirectory(exportDir);

        string rawTitle = string.IsNullOrWhiteSpace(board.Title) ? "Whiteboard" : board.Title;
        string sanitizedTitle = Regex.Replace(rawTitle, @"[^a-zA-Z0-9.-]", "_").Trim('_');
        if (string.IsNullOrWhiteSpace(sanitizedTitle))
        {
            sanitizedTitle = "Whiteboard";
        }

        string targetFile = Path.Combine(exportDir, $"{sanitizedTitle}.{format.GetExtension()}");
        int resolvedPageIndex = pageIndex >= 0 ? pageIndex : board.ActivePageIndex;

        switch (format)
        {
            case ExportFormat.PDF:
                ExportToPdf(board, targetFile);
                break;

            case ExportFormat.PNG:
                await ExportToPng(board, resolvedPageIndex, targetFile);
                break;

            case ExportFormat.SVG:
                ExportToSvg(board, resolvedPageIndex, targetFile);
                break;

            case ExportFormat.CREAVITY:
            case ExportFormat.BREVITY:
                ExportToCreavity(board, targetFile);
                break;

            default:
                throw new ArgumentOutOfRangeException(nameof(format), format, "Unsupported export format.");
        }

        return targetFile;
    }

    /// <summary>
    /// Overload of <see cref="ExportBoard(Board, ExportFormat, int)"/> accepting storage model <see cref="StorageBoard"/>.
    /// </summary>
    public static Task<string> ExportBoard(StorageBoard board, ExportFormat format, int pageIndex = -1)
    {
        ArgumentNullException.ThrowIfNull(board);
        return ExportBoard(ToDomainBoard(board), format, pageIndex);
    }

    /// <summary>
    /// Exports the board or active page to an SVG vector file using quadratic bezier curves for strokes.
    /// </summary>
    public static string ExportToSvg(Board board, int pageIndex, string targetPath)
    {
        ArgumentNullException.ThrowIfNull(board);

        int resolvedIndex = pageIndex >= 0 && pageIndex < board.Pages.Count ? pageIndex : board.ActivePageIndex;
        var page = (board.Pages != null && resolvedIndex >= 0 && resolvedIndex < board.Pages.Count)
            ? board.Pages[resolvedIndex]
            : (board.Pages?.FirstOrDefault() ?? new BoardPage());

        float width = board.CanvasWidth > 0 ? board.CanvasWidth : DefaultCanvasWidth;
        float height = board.CanvasHeight > 0 ? board.CanvasHeight : DefaultCanvasHeight;
        string bgHex = $"#{board.BackgroundColor & 0xFFFFFF:X6}";

        var inv = CultureInfo.InvariantCulture;
        var sb = new StringBuilder();

        sb.Append($"<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 {width.ToString("0.##", inv)} {height.ToString("0.##", inv)}\" width=\"{width.ToString("0.##", inv)}\" height=\"{height.ToString("0.##", inv)}\">\n");
        sb.Append($"  <rect width=\"100%\" height=\"100%\" fill=\"{bgHex}\" />\n");

        // Strokes
        if (page.Strokes != null)
        {
            foreach (var stroke in page.Strokes)
            {
                if (stroke.Points == null || stroke.Points.Count < 2 || stroke.IsEraser)
                    continue;

                string colorHex = $"#{stroke.Color & 0xFFFFFF:X6}";
                string opacity = stroke.Tool == DrawingTool.Highlighter ? "0.35" : "1.0";
                float sw = stroke.StrokeWidth * (stroke.Tool == DrawingTool.Highlighter ? 3f : 1f);

                string d = BuildSvgPathData(stroke.Points);
                sb.Append($"  <path d=\"{d}\" fill=\"none\" stroke=\"{colorHex}\" stroke-width=\"{sw.ToString("0.##", inv)}\" stroke-linecap=\"round\" stroke-linejoin=\"round\" opacity=\"{opacity}\" />\n");
            }
        }

        // Sticky notes
        if (page.StickyNotes != null)
        {
            foreach (var note in page.StickyNotes)
            {
                string noteHex = $"#{note.Color & 0xFFFFFF:X6}";
                sb.Append($"  <rect x=\"{note.X.ToString("0.##", inv)}\" y=\"{note.Y.ToString("0.##", inv)}\" width=\"{note.Width.ToString("0.##", inv)}\" height=\"{note.Height.ToString("0.##", inv)}\" rx=\"12\" fill=\"{noteHex}\" />\n");

                if (!string.IsNullOrWhiteSpace(note.Text))
                {
                    var lines = note.Text.Split('\n');
                    float yOffset = note.Y + 36f;
                    foreach (var rawLine in lines)
                    {
                        var line = rawLine.TrimEnd('\r');
                        if (yOffset < note.Y + note.Height - 12f)
                        {
                            sb.Append($"  <text x=\"{(note.X + 16f).ToString("0.##", inv)}\" y=\"{yOffset.ToString("0.##", inv)}\" font-size=\"20\" fill=\"#1C1B1F\" font-family=\"sans-serif\">{EscapeXml(line)}</text>\n");
                            yOffset += 26f;
                        }
                    }
                }
            }
        }

        // Text elements
        if (page.TextElements != null)
        {
            foreach (var txt in page.TextElements)
            {
                string colorHex = $"#{txt.Color & 0xFFFFFF:X6}";
                float fontSize = txt.FontSize > 0 ? txt.FontSize : 24f;
                string fontFam = string.IsNullOrWhiteSpace(txt.FontName) ? "sans-serif" : EscapeXml(txt.FontName);
                sb.Append($"  <text x=\"{txt.X.ToString("0.##", inv)}\" y=\"{(txt.Y + fontSize).ToString("0.##", inv)}\" font-size=\"{fontSize.ToString("0.##", inv)}\" fill=\"{colorHex}\" font-family=\"{fontFam}\">{EscapeXml(txt.Text)}</text>\n");
            }
        }

        sb.Append("</svg>\n");
        string svg = sb.ToString();

        string? dir = Path.GetDirectoryName(targetPath);
        if (!string.IsNullOrEmpty(dir) && !Directory.Exists(dir))
        {
            Directory.CreateDirectory(dir);
        }

        File.WriteAllText(targetPath, svg, Encoding.UTF8);
        return svg;
    }

    /// <summary>
    /// Exports the specified page to a PNG raster image using Win2D hardware or software render target.
    /// </summary>
    public static async Task ExportToPng(Board board, int pageIndex, string targetPath)
    {
        ArgumentNullException.ThrowIfNull(board);

        float width = board.CanvasWidth > 0 ? board.CanvasWidth : DefaultCanvasWidth;
        float height = board.CanvasHeight > 0 ? board.CanvasHeight : DefaultCanvasHeight;

        int resolvedIndex = pageIndex >= 0 && pageIndex < board.Pages.Count ? pageIndex : board.ActivePageIndex;
        var page = (board.Pages != null && resolvedIndex >= 0 && resolvedIndex < board.Pages.Count)
            ? board.Pages[resolvedIndex]
            : (board.Pages?.FirstOrDefault() ?? new BoardPage());

        CanvasDevice device;
        try
        {
            device = CanvasDevice.GetSharedDevice();
        }
        catch
        {
            device = new CanvasDevice(forceSoftwareRenderer: true);
        }

        using var renderTarget = new CanvasRenderTarget(device, width, height, 96.0f);
        using (var ds = renderTarget.CreateDrawingSession())
        {
            RenderPageToCanvas(ds, board.BackgroundColor, page, width, height);
        }

        string? dir = Path.GetDirectoryName(targetPath);
        if (!string.IsNullOrEmpty(dir) && !Directory.Exists(dir))
        {
            Directory.CreateDirectory(dir);
        }

        using var memStream = new InMemoryRandomAccessStream();
        await renderTarget.SaveAsync(memStream, CanvasBitmapFileFormat.Png);
        memStream.Seek(0);

        using var readStream = memStream.AsStreamForRead();
        using var outStream = File.Create(targetPath);
        await readStream.CopyToAsync(outStream);
    }

    /// <summary>
    /// Exports all pages of the board to a multi-page PDF document using PdfSharpCore.
    /// </summary>
    public static void ExportToPdf(Board board, string targetPath)
    {
        ArgumentNullException.ThrowIfNull(board);

        string? dir = Path.GetDirectoryName(targetPath);
        if (!string.IsNullOrEmpty(dir) && !Directory.Exists(dir))
        {
            Directory.CreateDirectory(dir);
        }

        double width = board.CanvasWidth > 0 ? board.CanvasWidth : DefaultCanvasWidth;
        double height = board.CanvasHeight > 0 ? board.CanvasHeight : DefaultCanvasHeight;

        using var document = new PdfDocument();
        var pages = (board.Pages != null && board.Pages.Count > 0)
            ? board.Pages
            : new List<BoardPage> { new BoardPage() };

        foreach (var page in pages)
        {
            var pdfPage = document.AddPage();
            pdfPage.Width = XUnit.FromPoint(width);
            pdfPage.Height = XUnit.FromPoint(height);

            using var gfx = XGraphics.FromPdfPage(pdfPage);
            RenderPageToPdf(gfx, board.BackgroundColor, page, width, height);
        }

        document.Save(targetPath);
    }

    /// <summary>
    /// Exports native CreaVITy whiteboard document JSON using BoardManager storage.
    /// </summary>
    public static void ExportToCreavity(Board board, string targetPath)
    {
        ArgumentNullException.ThrowIfNull(board);

        var storageBoard = ToStorageBoard(board);
        var bm = BoardManager.Instance;
        bm.SaveBoard(storageBoard);

        string boardsDir = Path.Combine(
            Environment.GetFolderPath(Environment.SpecialFolder.MyDocuments),
            "CreaVITy");
        string sourceFile = Path.Combine(boardsDir, $"{board.Id}.json");

        string? targetDir = Path.GetDirectoryName(targetPath);
        if (!string.IsNullOrEmpty(targetDir) && !Directory.Exists(targetDir))
        {
            Directory.CreateDirectory(targetDir);
        }

        if (File.Exists(sourceFile))
        {
            if (!string.Equals(Path.GetFullPath(sourceFile), Path.GetFullPath(targetPath), StringComparison.OrdinalIgnoreCase))
            {
                File.Copy(sourceFile, targetPath, overwrite: true);
            }
        }
    }

    /// <summary>
    /// Exports native CreaVITy whiteboard document JSON using BoardManager storage.
    /// </summary>
    public static void ExportToCreavity(StorageBoard board, string targetPath)
    {
        ArgumentNullException.ThrowIfNull(board);

        var bm = BoardManager.Instance;
        bm.SaveBoard(board);

        string boardsDir = Path.Combine(
            Environment.GetFolderPath(Environment.SpecialFolder.MyDocuments),
            "CreaVITy");
        string sourceFile = Path.Combine(boardsDir, $"{board.Id}.json");

        string? targetDir = Path.GetDirectoryName(targetPath);
        if (!string.IsNullOrEmpty(targetDir) && !Directory.Exists(targetDir))
        {
            Directory.CreateDirectory(targetDir);
        }

        if (File.Exists(sourceFile))
        {
            if (!string.Equals(Path.GetFullPath(sourceFile), Path.GetFullPath(targetPath), StringComparison.OrdinalIgnoreCase))
            {
                File.Copy(sourceFile, targetPath, overwrite: true);
            }
        }
    }

    // ── Internal Win2D Canvas Rendering ───────────────────────────────────────

    private static void RenderPageToCanvas(CanvasDrawingSession ds, long backgroundColor, BoardPage page, float width, float height)
    {
        // 1. Background
        var bgColor = ToWin2DColor(backgroundColor);
        ds.Clear(bgColor);

        // 2. Strokes
        if (page.Strokes != null)
        {
            foreach (var stroke in page.Strokes)
            {
                if (stroke.Points == null || stroke.Points.Count < 2 || stroke.IsEraser)
                    continue;

                var strokeColor = ToWin2DColor(stroke.Color);
                float strokeWidth = stroke.StrokeWidth;
                bool isHighlighter = stroke.Tool == DrawingTool.Highlighter;
                bool isMarker = stroke.Tool == DrawingTool.Marker;

                if (isHighlighter)
                {
                    strokeColor = Color.FromArgb(90, strokeColor.R, strokeColor.G, strokeColor.B);
                    strokeWidth *= 3f;
                }
                else if (isMarker)
                {
                    strokeWidth *= 2f;
                }

                using var pathBuilder = new CanvasPathBuilder(ds.Device);
                pathBuilder.BeginFigure(new Vector2(stroke.Points[0].X, stroke.Points[0].Y));
                for (int i = 1; i < stroke.Points.Count; i++)
                {
                    var p0 = stroke.Points[i - 1];
                    var p1 = stroke.Points[i];
                    var mid = new Vector2((p0.X + p1.X) / 2f, (p0.Y + p1.Y) / 2f);
                    pathBuilder.AddQuadraticBezier(new Vector2(p0.X, p0.Y), mid);
                }
                pathBuilder.AddLine(new Vector2(stroke.Points[^1].X, stroke.Points[^1].Y));
                pathBuilder.EndFigure(CanvasFigureLoop.Open);

                using var geometry = CanvasGeometry.CreatePath(pathBuilder);
                using var style = new CanvasStrokeStyle
                {
                    StartCap = isHighlighter ? CanvasCapStyle.Square : CanvasCapStyle.Round,
                    EndCap = isHighlighter ? CanvasCapStyle.Square : CanvasCapStyle.Round,
                    LineJoin = CanvasLineJoin.Round
                };
                ds.DrawGeometry(geometry, strokeColor, strokeWidth, style);
            }
        }

        // 3. Sticky Notes
        if (page.StickyNotes != null)
        {
            using var noteTextFormat = new CanvasTextFormat
            {
                FontSize = 20f,
                FontFamily = "Segoe UI"
            };

            foreach (var note in page.StickyNotes)
            {
                var noteColor = ToWin2DColor(note.Color);
                ds.FillRoundedRectangle(note.X, note.Y, note.Width, note.Height, 16f, 16f, noteColor);

                if (!string.IsNullOrWhiteSpace(note.Text))
                {
                    var textColor = Color.FromArgb(255, 0x1C, 0x1B, 0x1F);
                    var lines = note.Text.Split('\n');
                    float yOffset = note.Y + 36f;
                    foreach (var rawLine in lines)
                    {
                        var line = rawLine.TrimEnd('\r');
                        if (yOffset < note.Y + note.Height - 12f)
                        {
                            ds.DrawText(line, note.X + 16f, yOffset - 20f, textColor, noteTextFormat);
                            yOffset += 26f;
                        }
                    }
                }
            }
        }

        // 4. Text Elements
        if (page.TextElements != null)
        {
            foreach (var txt in page.TextElements)
            {
                var textColor = ToWin2DColor(txt.Color);
                using var format = new CanvasTextFormat
                {
                    FontSize = txt.FontSize > 0 ? txt.FontSize : 24f,
                    FontFamily = string.IsNullOrWhiteSpace(txt.FontName) ? "Segoe UI" : txt.FontName
                };
                ds.DrawText(txt.Text, txt.X, txt.Y, textColor, format);
            }
        }
    }

    // ── Internal PDF Rendering (PdfSharpCore) ────────────────────────────────

    private static void RenderPageToPdf(XGraphics gfx, long backgroundColor, BoardPage page, double width, double height)
    {
        // 1. Background
        var bgBrush = new XSolidBrush(ToPdfColor(backgroundColor));
        gfx.DrawRectangle(bgBrush, 0, 0, width, height);

        // 2. Strokes
        if (page.Strokes != null)
        {
            foreach (var stroke in page.Strokes)
            {
                if (stroke.Points == null || stroke.Points.Count < 2 || stroke.IsEraser)
                    continue;

                bool isHighlighter = stroke.Tool == DrawingTool.Highlighter;
                bool isMarker = stroke.Tool == DrawingTool.Marker;

                float sw = stroke.StrokeWidth * (isHighlighter ? 3f : (isMarker ? 2f : 1f));
                var strokeColor = isHighlighter ? ToPdfColor(stroke.Color, 90) : ToPdfColor(stroke.Color, 255);

                var pen = new XPen(strokeColor, sw)
                {
                    LineCap = isHighlighter ? XLineCap.Square : XLineCap.Round,
                    LineJoin = XLineJoin.Round
                };

                var path = new XGraphicsPath();
                path.StartFigure();

                var start = stroke.Points[0];
                for (int i = 1; i < stroke.Points.Count; i++)
                {
                    var prev = stroke.Points[i - 1];
                    var curr = stroke.Points[i];
                    double midX = (prev.X + curr.X) / 2.0;
                    double midY = (prev.Y + curr.Y) / 2.0;

                    // Convert Quadratic Bezier to Cubic Bezier
                    double c1x = start.X + (2.0 / 3.0) * (prev.X - start.X);
                    double c1y = start.Y + (2.0 / 3.0) * (prev.Y - start.Y);
                    double c2x = midX + (2.0 / 3.0) * (prev.X - midX);
                    double c2y = midY + (2.0 / 3.0) * (prev.Y - midY);

                    path.AddBezier(start.X, start.Y, c1x, c1y, c2x, c2y, midX, midY);
                    start = new Point((float)midX, (float)midY);
                }

                var last = stroke.Points[^1];
                path.AddLine(start.X, start.Y, last.X, last.Y);

                gfx.DrawPath(pen, path);
            }
        }

        // 3. Sticky Notes
        if (page.StickyNotes != null)
        {
            var noteFont = new XFont("Segoe UI", 14, XFontStyle.Regular);
            var noteTextBrush = new XSolidBrush(XColor.FromArgb(255, 0x1C, 0x1B, 0x1F));

            foreach (var note in page.StickyNotes)
            {
                var noteBrush = new XSolidBrush(ToPdfColor(note.Color));
                gfx.DrawRoundedRectangle(noteBrush, note.X, note.Y, note.Width, note.Height, 16.0, 16.0);

                if (!string.IsNullOrWhiteSpace(note.Text))
                {
                    var lines = note.Text.Split('\n');
                    double yOffset = note.Y + 36.0;
                    foreach (var rawLine in lines)
                    {
                        var line = rawLine.TrimEnd('\r');
                        if (yOffset < note.Y + note.Height - 12.0)
                        {
                            gfx.DrawString(line, noteFont, noteTextBrush, note.X + 16.0, yOffset);
                            yOffset += 26.0;
                        }
                    }
                }
            }
        }

        // 4. Text Elements
        if (page.TextElements != null)
        {
            foreach (var txt in page.TextElements)
            {
                var fontName = string.IsNullOrWhiteSpace(txt.FontName) ? "Segoe UI" : txt.FontName;
                double fontSize = txt.FontSize > 0 ? txt.FontSize : 24.0;
                var font = new XFont(fontName, fontSize, XFontStyle.Regular);
                var brush = new XSolidBrush(ToPdfColor(txt.Color));
                gfx.DrawString(txt.Text, font, brush, txt.X, txt.Y + fontSize);
            }
        }
    }

    // ── SVG Helpers ──────────────────────────────────────────────────────────

    private static string BuildSvgPathData(IReadOnlyList<Point> points)
    {
        if (points == null || points.Count == 0) return string.Empty;

        var inv = CultureInfo.InvariantCulture;
        var sb = new StringBuilder();
        sb.Append($"M {points[0].X.ToString("0.##", inv)} {points[0].Y.ToString("0.##", inv)}");

        for (int i = 1; i < points.Count; i++)
        {
            var p0 = points[i - 1];
            var p1 = points[i];
            float midX = (p0.X + p1.X) / 2f;
            float midY = (p0.Y + p1.Y) / 2f;
            sb.Append($" Q {p0.X.ToString("0.##", inv)} {p0.Y.ToString("0.##", inv)} {midX.ToString("0.##", inv)} {midY.ToString("0.##", inv)}");
        }

        sb.Append($" L {points[^1].X.ToString("0.##", inv)} {points[^1].Y.ToString("0.##", inv)}");
        return sb.ToString();
    }

    private static string EscapeXml(string str)
    {
        if (string.IsNullOrEmpty(str)) return string.Empty;

        return str.Replace("&", "&amp;")
            .Replace("<", "&lt;")
            .Replace(">", "&gt;")
            .Replace("\"", "&quot;")
            .Replace("'", "&apos;");
    }

    // ── Color Helpers ────────────────────────────────────────────────────────

    private static Color ToWin2DColor(long argb)
    {
        byte a = (byte)((argb >> 24) & 0xFF);
        if (a == 0) a = 255;
        byte r = (byte)((argb >> 16) & 0xFF);
        byte g = (byte)((argb >> 8) & 0xFF);
        byte b = (byte)(argb & 0xFF);
        return Color.FromArgb(a, r, g, b);
    }

    private static XColor ToPdfColor(long argb, int? overrideAlpha = null)
    {
        int a = overrideAlpha ?? (int)((argb >> 24) & 0xFF);
        if (a == 0) a = 255;
        int r = (int)((argb >> 16) & 0xFF);
        int g = (int)((argb >> 8) & 0xFF);
        int b = (int)(argb & 0xFF);
        return XColor.FromArgb(a, r, g, b);
    }

    // ── Model Mapping ────────────────────────────────────────────────────────

    private static StorageBoard ToStorageBoard(Board board)
    {
        return new StorageBoard
        {
            Id = board.Id,
            Title = board.Title,
            ActivePageIndex = board.ActivePageIndex,
            BackgroundColor = board.BackgroundColor,
            CreatedAt = board.CreatedAt,
            ModifiedAt = board.ModifiedAt,
            LastAccessedAt = board.LastAccessedAt == 0 ? board.ModifiedAt : board.LastAccessedAt,
            CreatedBy = board.CreatedBy,
            OwnerName = board.OwnerName,
            IsReadOnly = board.IsReadOnly,
            IsStarred = board.IsStarred,
            IsInBin = board.IsInBin,
            BinnedAt = board.BinnedAt,
            IsInfiniteCanvas = board.IsInfiniteCanvas,
            CanvasWidth = board.CanvasWidth,
            CanvasHeight = board.CanvasHeight,
            PresentationUrl = board.PresentationUrl,
            Pages = (board.Pages ?? Array.Empty<BoardPage>()).Select(p => new StorageBoardPage
            {
                Id = p.Id,
                Name = p.Name,
                Strokes = (p.Strokes ?? Array.Empty<Stroke>()).Select(s => new StorageStroke
                {
                    Id = s.Id,
                    Color = s.Color,
                    StrokeWidth = s.StrokeWidth,
                    Tool = MapToolToStorage(s.Tool),
                    IsEraser = s.IsEraser,
                    Author = s.Author,
                    CreatedAt = s.CreatedAt,
                    Points = (s.Points ?? Array.Empty<Point>()).Select(pt => new StorageStrokePoint
                    {
                        X = pt.X,
                        Y = pt.Y,
                        P = pt.Pressure,
                        T = pt.TimestampMs,
                        Tilt = pt.Tilt,
                        Ori = pt.Orientation
                    }).ToList()
                }).ToList(),
                TextElements = (p.TextElements ?? Array.Empty<TextElement>()).Select(t => new StorageTextElement
                {
                    Id = t.Id,
                    Text = t.Text,
                    X = t.X,
                    Y = t.Y,
                    Color = t.Color,
                    FontSize = t.FontSize,
                    FontName = t.FontName,
                    Author = t.Author,
                    CreatedAt = t.CreatedAt
                }).ToList(),
                ImageElements = (p.ImageElements ?? Array.Empty<ImageElement>()).Select(img => new StorageImageElement
                {
                    Id = img.Id,
                    Path = img.Path,
                    X = img.X,
                    Y = img.Y,
                    Width = img.Width,
                    Height = img.Height,
                    Author = img.Author,
                    CreatedAt = img.CreatedAt
                }).ToList(),
                StickyNotes = (p.StickyNotes ?? Array.Empty<StickyNote>()).Select(sn => new StorageStickyNote
                {
                    Id = sn.Id,
                    Text = sn.Text,
                    X = sn.X,
                    Y = sn.Y,
                    Width = sn.Width,
                    Height = sn.Height,
                    Color = sn.Color,
                    Author = sn.Author,
                    CreatedAt = sn.CreatedAt
                }).ToList()
            }).ToList()
        };
    }

    private static Board ToDomainBoard(StorageBoard board)
    {
        return new Board
        {
            Id = board.Id,
            Title = board.Title,
            ActivePageIndex = board.ActivePageIndex,
            BackgroundColor = board.BackgroundColor,
            CreatedAt = board.CreatedAt,
            ModifiedAt = board.ModifiedAt,
            LastAccessedAt = board.LastAccessedAt,
            CreatedBy = board.CreatedBy,
            OwnerName = board.OwnerName,
            IsReadOnly = board.IsReadOnly,
            IsStarred = board.IsStarred,
            IsInBin = board.IsInBin,
            BinnedAt = board.BinnedAt,
            IsInfiniteCanvas = board.IsInfiniteCanvas,
            CanvasWidth = board.CanvasWidth,
            CanvasHeight = board.CanvasHeight,
            PresentationUrl = board.PresentationUrl,
            Pages = (board.Pages ?? new List<StorageBoardPage>()).Select(p => new BoardPage
            {
                Id = p.Id,
                Name = p.Name,
                Strokes = (p.Strokes ?? new List<StorageStroke>()).Select(s => new Stroke
                {
                    Id = s.Id,
                    Color = s.Color,
                    StrokeWidth = s.StrokeWidth,
                    Tool = MapToolToDomain(s.Tool),
                    IsEraser = s.IsEraser,
                    Author = s.Author,
                    CreatedAt = s.CreatedAt,
                    Points = (s.Points ?? new List<StorageStrokePoint>()).Select(pt => new Point(
                        pt.X,
                        pt.Y,
                        pt.P,
                        pt.T,
                        pt.Tilt ?? 0f,
                        pt.Ori ?? 0f
                    )).ToList()
                }).ToList(),
                TextElements = (p.TextElements ?? new List<StorageTextElement>()).Select(t => new TextElement
                {
                    Id = t.Id,
                    Text = t.Text,
                    X = t.X,
                    Y = t.Y,
                    Color = t.Color,
                    FontSize = t.FontSize,
                    FontName = t.FontName,
                    Author = t.Author,
                    CreatedAt = t.CreatedAt
                }).ToList(),
                ImageElements = (p.ImageElements ?? new List<StorageImageElement>()).Select(img => new ImageElement
                {
                    Id = img.Id,
                    Path = img.Path,
                    X = img.X,
                    Y = img.Y,
                    Width = img.Width,
                    Height = img.Height,
                    Author = img.Author,
                    CreatedAt = img.CreatedAt
                }).ToList(),
                StickyNotes = (p.StickyNotes ?? new List<StorageStickyNote>()).Select(sn => new StickyNote
                {
                    Id = sn.Id,
                    Text = sn.Text,
                    X = sn.X,
                    Y = sn.Y,
                    Width = sn.Width,
                    Height = sn.Height,
                    Color = sn.Color,
                    Author = sn.Author,
                    CreatedAt = sn.CreatedAt
                }).ToList()
            }).ToList()
        };
    }

    private static StorageDrawingTool MapToolToStorage(DrawingTool tool) => tool switch
    {
        DrawingTool.Highlighter => StorageDrawingTool.Highlighter,
        DrawingTool.Marker => StorageDrawingTool.Marker,
        DrawingTool.Eraser => StorageDrawingTool.Eraser,
        DrawingTool.Laser => StorageDrawingTool.Laser,
        DrawingTool.Select or DrawingTool.SelectErase or DrawingTool.SelectMove or DrawingTool.RectSelect or DrawingTool.LassoSelect => StorageDrawingTool.Selection,
        _ => StorageDrawingTool.Pen
    };

    private static DrawingTool MapToolToDomain(StorageDrawingTool tool) => tool switch
    {
        StorageDrawingTool.Highlighter => DrawingTool.Highlighter,
        StorageDrawingTool.Marker => DrawingTool.Marker,
        StorageDrawingTool.Eraser => DrawingTool.Eraser,
        StorageDrawingTool.Laser => DrawingTool.Laser,
        StorageDrawingTool.Selection => DrawingTool.Select,
        _ => DrawingTool.Pen
    };
}
