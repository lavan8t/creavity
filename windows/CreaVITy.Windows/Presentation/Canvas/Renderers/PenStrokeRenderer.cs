using System;
using System.Collections.Generic;
using System.Numerics;
using Windows.UI;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.Geometry;
using CreaVITy.Windows.Domain.Math;
using CreaVITy.Windows.Domain.Models;

namespace CreaVITy.Windows.Presentation.Canvas.Renderers;

/// <summary>
/// Realistic ballpoint pen renderer ported from Kotlin.
///
/// Characteristics:
/// - Relatively uniform, narrow, highly opaque ink strokes.
/// - Smooth Catmull-Rom / Quadratic Bezier curves and antialiased edges.
/// - Rounded, natural beginnings, joins, and endings.
/// - Subtle, controlled changes in width with pressure (0.88x to 1.12x of base width).
/// - Speed dampening (-8% to +5%): fast cursive strokes thin out subtly while maintaining crisp body.
/// - Zero gaps between samples when drawing slowly or quickly via overlapping segments with round caps.
/// - Clean integration with Win2D CanvasDrawingSession and hardware path caching.
/// </summary>
public static class PenStrokeRenderer
{
    /// <summary>
    /// Shared stroke style with round start cap, end cap, and joins.
    /// </summary>
    public static readonly CanvasStrokeStyle RoundStrokeStyle = new()
    {
        StartCap = CanvasCapStyle.Round,
        EndCap = CanvasCapStyle.Round,
        LineJoin = CanvasLineJoin.Round
    };

    /// <summary>
    /// Converts a 32-bit/64-bit ARGB packed integer into a <see cref="Color"/>.
    /// </summary>
    public static Color ToColor(long argb)
    {
        byte a = (byte)((argb >> 24) & 0xFF);
        byte r = (byte)((argb >> 16) & 0xFF);
        byte g = (byte)((argb >> 8) & 0xFF);
        byte b = (byte)(argb & 0xFF);
        return Color.FromArgb(a, r, g, b);
    }

    /// <summary>
    /// Calculates the spherical dot imprint radius for a single tap.
    /// </summary>
    public static float CalculateDotRadius(float baseWidth, float pressure)
    {
        float press = Math.Clamp(pressure, 0.4f, 1.3f);
        return Math.Max(baseWidth * 0.5f * (0.90f + 0.15f * press), 1.2f);
    }

    /// <summary>
    /// Checks whether dynamic stylus pressure variance exceeds the variable pressure threshold.
    /// </summary>
    public static bool HasVariablePressure(IReadOnlyList<Point> points, float threshold = 0.12f)
    {
        if (points == null || points.Count == 0)
            return false;

        float minPress = points[0].Pressure;
        float maxPress = points[0].Pressure;

        for (int i = 1; i < points.Count; i++)
        {
            float p = points[i].Pressure;
            if (p < minPress) minPress = p;
            if (p > maxPress) maxPress = p;
        }

        return (maxPress - minPress) > threshold;
    }

    /// <summary>
    /// Calculates smoothed widths per point taking velocity dampening and pressure response into account.
    /// </summary>
    public static float[] CalculatePointWidths(IReadOnlyList<Point> points, float baseWidth)
    {
        if (points == null || points.Count == 0)
            return Array.Empty<float>();

        int pointCount = points.Count;
        float[] widths = new float[pointCount];
        float smoothedWidth = baseWidth;

        for (int i = 0; i < pointCount; i++)
        {
            var curr = points[i];
            var prev = i > 0 ? points[i - 1] : curr;
            float dt = (float)Math.Clamp(curr.TimestampMs - prev.TimestampMs, 1L, 120L);
            float dx = curr.X - prev.X;
            float dy = curr.Y - prev.Y;
            float dist = MathF.Sqrt(dx * dx + dy * dy);
            float speed = dist / dt;

            // Subtle speed dampening (-8% to +5%)
            float speedFactor = 1.0f - Math.Clamp(speed * 0.05f, -0.05f, 0.08f);

            // Subtle pressure response (0.88x to 1.12x)
            float pressFactor = 0.88f + 0.24f * Math.Clamp(curr.Pressure, 0.2f, 1.2f);

            float targetWidth = Math.Clamp(
                baseWidth * speedFactor * pressFactor,
                baseWidth * 0.82f,
                baseWidth * 1.18f);

            smoothedWidth = smoothedWidth * 0.60f + targetWidth * 0.40f;
            widths[i] = smoothedWidth;
        }

        return widths;
    }

    /// <summary>
    /// Builds a smooth vector <see cref="CanvasGeometry"/> for the ballpoint pen stroke with quadratic Bezier smoothing.
    /// </summary>
    public static CanvasGeometry? CreatePenGeometry(
        ICanvasResourceCreator resourceCreator,
        IReadOnlyList<Point> points)
    {
        if (resourceCreator == null || points == null || points.Count == 0)
            return null;

        using var builder = new CanvasPathBuilder(resourceCreator);
        builder.BeginFigure(new Vector2(points[0].X, points[0].Y));

        if (points.Count == 1)
        {
            builder.AddLine(new Vector2(points[0].X + 0.1f, points[0].Y + 0.1f));
            builder.EndFigure(CanvasFigureLoop.Open);
            return CanvasGeometry.CreatePath(builder);
        }

        if (points.Count == 2)
        {
            builder.AddLine(new Vector2(points[1].X, points[1].Y));
            builder.EndFigure(CanvasFigureLoop.Open);
            return CanvasGeometry.CreatePath(builder);
        }

        for (int i = 1; i < points.Count - 1; i++)
        {
            var p0 = points[i - 1];
            var p1 = points[i];
            var p2 = points[i + 1];

            float midX1 = (p0.X + p1.X) * 0.5f;
            float midY1 = (p0.Y + p1.Y) * 0.5f;
            float midX2 = (p1.X + p2.X) * 0.5f;
            float midY2 = (p1.Y + p2.Y) * 0.5f;

            if (i == 1)
            {
                builder.AddLine(new Vector2(midX1, midY1));
            }

            builder.AddQuadraticBezier(
                new Vector2(p1.X, p1.Y),
                new Vector2(midX2, midY2));
        }

        var last = points[points.Count - 1];
        builder.AddLine(new Vector2(last.X, last.Y));
        builder.EndFigure(CanvasFigureLoop.Open);

        return CanvasGeometry.CreatePath(builder);
    }

    /// <summary>
    /// Builds quadratic Bezier smoothing path commands (pure logic / device independent).
    /// </summary>
    public static IReadOnlyList<PathCommand> CreatePenPathCommands(IReadOnlyList<Point> points)
    {
        if (points == null || points.Count == 0)
            return Array.Empty<PathCommand>();

        var cmds = new List<PathCommand>();
        cmds.Add(new MoveToCmd(points[0].X, points[0].Y));

        if (points.Count == 1)
        {
            cmds.Add(new LineToCmd(points[0].X + 0.1f, points[0].Y + 0.1f));
            return cmds;
        }

        if (points.Count == 2)
        {
            cmds.Add(new LineToCmd(points[1].X, points[1].Y));
            return cmds;
        }

        for (int i = 1; i < points.Count - 1; i++)
        {
            var p0 = points[i - 1];
            var p1 = points[i];
            var p2 = points[i + 1];

            float midX1 = (p0.X + p1.X) * 0.5f;
            float midY1 = (p0.Y + p1.Y) * 0.5f;
            float midX2 = (p1.X + p2.X) * 0.5f;
            float midY2 = (p1.Y + p2.Y) * 0.5f;

            if (i == 1)
            {
                cmds.Add(new LineToCmd(midX1, midY1));
            }

            cmds.Add(new QuadraticToCmd(p1.X, p1.Y, midX2, midY2));
        }

        var last = points[points.Count - 1];
        cmds.Add(new LineToCmd(last.X, last.Y));

        return cmds;
    }

    /// <summary>
    /// Draws the ballpoint pen stroke onto the Win2D <see cref="CanvasDrawingSession"/>.
    /// </summary>
    public static void DrawPen(
        CanvasDrawingSession session,
        IReadOnlyList<Point> points,
        Color color,
        float baseWidth,
        CanvasGeometry? cachedGeometry = null)
    {
        if (session == null || points == null || points.Count == 0)
            return;

        // Single tap dot: crisp ballpoint spherical imprint
        if (points.Count == 1)
        {
            var p = points[0];
            float r = CalculateDotRadius(baseWidth, p.Pressure);
            session.FillCircle(p.X, p.Y, r, color);
            return;
        }

        // If a cached vector path is provided and points are uniform, draw GPU-accelerated smooth path
        if (cachedGeometry != null && !HasVariablePressure(points))
        {
            session.DrawGeometry(
                cachedGeometry,
                color,
                baseWidth,
                RoundStrokeStyle);
            return;
        }

        int pointCount = points.Count;
        float[] widths = CalculatePointWidths(points, baseWidth);

        // Draw overlapping smooth segments with round caps to guarantee zero gaps
        for (int i = 0; i < pointCount - 1; i++)
        {
            var p0 = points[i];
            var p1 = points[i + 1];
            float midWidth = (widths[i] + widths[i + 1]) * 0.5f;

            session.DrawLine(
                p0.X,
                p0.Y,
                p1.X,
                p1.Y,
                color,
                midWidth,
                RoundStrokeStyle);
        }
    }

    /// <summary>
    /// Convenience overload to draw pen stroke using an ARGB-packed 32/64-bit integer color.
    /// </summary>
    public static void DrawPen(
        CanvasDrawingSession session,
        IReadOnlyList<Point> points,
        long argbColor,
        float baseWidth,
        CanvasGeometry? cachedGeometry = null)
    {
        DrawPen(session, points, ToColor(argbColor), baseWidth, cachedGeometry);
    }

    /// <summary>
    /// Convenience overload to draw a <see cref="Stroke"/> object using the pen renderer.
    /// </summary>
    public static void DrawPen(
        CanvasDrawingSession session,
        Stroke stroke,
        CanvasGeometry? cachedGeometry = null)
    {
        if (stroke == null)
            return;

        DrawPen(
            session,
            stroke.Points,
            ToColor(stroke.Color),
            stroke.StrokeWidth,
            cachedGeometry);
    }
}
