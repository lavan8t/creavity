using System;
using System.Collections.Generic;
using Windows.UI;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.Geometry;
using CreaVITy.Windows.Domain.Models;

namespace CreaVITy.Windows.Presentation.Canvas.Renderers;

/// <summary>
/// Genuine graphite pencil engine with authentic graphite grain, paper tooth,
/// pressure-dependent tonal darkness, tilt-based shading, and soft tapers.
///
/// Deterministic texture placement:
/// Seeded by stroke timestamp and coordinate geometry so redrawing, zooming,
/// and exporting never flicker or change randomly.
/// </summary>
public static class PencilStrokeRenderer
{
    private static CanvasStrokeStyle? _roundCapStrokeStyle;

    private static CanvasStrokeStyle RoundCapStrokeStyle =>
        _roundCapStrokeStyle ??= new CanvasStrokeStyle
        {
            StartCap = CanvasCapStyle.Round,
            EndCap = CanvasCapStyle.Round
        };

    /// <summary>
    /// Fast deterministic hash for stable, jitter-free pseudo-random values.
    /// Returns a float in [-0.5f, 0.5f].
    /// </summary>
    private static float DeterministicNoise(long seed, int index, int pass)
    {
        long h = seed ^ (index * 374761393L) ^ (pass * 668265263L);
        h = (h ^ (h >> 16)) * 0x85ebca6bL;
        h = (h ^ (h >> 13)) * 0xc2b2ae35L;
        h = h ^ (h >> 16);
        int intVal = (int)(h & 0xFFFFL);
        return (intVal / 65535f) - 0.5f;
    }

    /// <summary>
    /// Draws a pencil stroke onto a Win2D <see cref="CanvasDrawingSession"/> using sampled points.
    /// </summary>
    /// <param name="session">The active Win2D drawing session.</param>
    /// <param name="points">Ordered list of sampled stroke points.</param>
    /// <param name="color">The stroke base color.</param>
    /// <param name="baseWidth">The base stroke width in DIPs.</param>
    public static void DrawPencil(
        this CanvasDrawingSession session,
        IReadOnlyList<Point> points,
        Color color,
        float baseWidth)
    {
        if (session == null || points == null || points.Count == 0)
            return;

        // Single tap dot with soft graphite tooth
        if (points.Count == 1)
        {
            var p = points[0];
            float press = Math.Clamp(p.Pressure, 0.2f, 1.3f);
            float dotRadius = MathF.Max(baseWidth * 0.45f * press, 1.2f);
            float baseDotAlpha = color.A / 255.0f;
            float dotAlpha = Math.Clamp(baseDotAlpha * (0.50f + 0.45f * press), 0.1f, 1.0f);
            session.FillCircle(p.X, p.Y, dotRadius, WithAlpha(color, dotAlpha));
            return;
        }

        // Tilt detection for soft graphite shading
        float sumTilt = 0f;
        int tiltCount = 0;
        for (int i = 0; i < points.Count; i++)
        {
            if (points[i].Tilt > 0f)
            {
                sumTilt += points[i].Tilt;
                tiltCount++;
            }
        }
        float avgTilt = tiltCount > 0 ? sumTilt / tiltCount : 0f;
        float tiltRatio = Math.Clamp(avgTilt / 1.57f, 0f, 1f);
        float tiltSpread = 1.0f + 2.2f * tiltRatio;
        float effectiveWidth = MathF.Max(baseWidth * tiltSpread, 1.5f);

        // Seed derived from first and last points and timestamps for 100% stable determinism
        var pFirst = points[0];
        var pLast = points[points.Count - 1];
        long seed = (pFirst.TimestampMs & 0xFFFFFFFFL) ^
                    ((long)BitConverter.SingleToInt32Bits(pFirst.X) << 16) ^
                    (long)BitConverter.SingleToInt32Bits(pLast.Y);

        int pointCount = points.Count;
        float baseAlpha = color.A / 255.0f;

        // Precompute per-point width, pressure, and taper factors
        var widths = new float[pointCount];
        var alphas = new float[pointCount];

        for (int i = 0; i < pointCount; i++)
        {
            var pt = points[i];
            float press = Math.Clamp(pt.Pressure, 0.15f, 1.4f);

            // Natural taper at stroke head and tail (first/last 2 points)
            float headTaper = i < 3 ? (i + 1) / 3.5f : 1.0f;
            float tailTaper = i >= pointCount - 3 ? (pointCount - i) / 3.5f : 1.0f;
            float taper = MathF.Min(headTaper, tailTaper);

            // Pressure modulation: width and darkness
            float widthFactor = (0.75f + 0.35f * press) * taper;
            float alphaFactor = (0.40f + 0.55f * press) * (1.0f - 0.25f * tiltRatio) * taper;

            widths[i] = MathF.Max(effectiveWidth * widthFactor, 1.0f);
            alphas[i] = Math.Clamp(baseAlpha * alphaFactor, 0.15f, 0.95f);
        }

        // Pass 1: Graphite tooth & edge irregularity (soft micro-tooth caught on paper)
        float toothJitter = Math.Clamp(effectiveWidth * 0.22f, 0.4f, 2.5f);
        float toothAlpha = Math.Clamp(baseAlpha * 0.25f * (1.0f + 0.3f * tiltRatio), 0.08f, 0.45f);
        float toothWidth = effectiveWidth * (1.1f + 0.4f * tiltRatio);
        var toothColor = WithAlpha(color, toothAlpha);

        for (int i = 0; i < pointCount - 1; i++)
        {
            var p0 = points[i];
            var p1 = points[i + 1];
            float dx = p1.X - p0.X;
            float dy = p1.Y - p0.Y;
            float dist = MathF.Sqrt(dx * dx + dy * dy);
            if (dist < 0.1f) continue;

            // Normal vector for transverse tooth displacement
            float nx = -dy / dist;
            float ny = dx / dist;

            float j0 = DeterministicNoise(seed, i, 1) * toothJitter;
            float j1 = DeterministicNoise(seed, i + 1, 1) * toothJitter;

            session.DrawLine(
                p0.X + nx * j0, p0.Y + ny * j0,
                p1.X + nx * j1, p1.Y + ny * j1,
                toothColor,
                toothWidth,
                RoundCapStrokeStyle);
        }

        // Pass 2: Main graphite core with pressure-modulated width and opacity
        for (int i = 0; i < pointCount - 1; i++)
        {
            var p0 = points[i];
            var p1 = points[i + 1];
            float segWidth = (widths[i] + widths[i + 1]) / 2f;
            float segAlpha = (alphas[i] + alphas[i + 1]) / 2f;
            var segColor = WithAlpha(color, segAlpha);

            session.DrawLine(
                p0.X, p0.Y,
                p1.X, p1.Y,
                segColor,
                segWidth,
                RoundCapStrokeStyle);
        }

        // Pass 3: Fine graphite grain striae (internal crystal grain texture)
        // High pressure fills grain densely; light pressure exposes paper grain valleys
        int grainPasses = tiltRatio > 0.3f ? 2 : 1;
        for (int pass = 0; pass < grainPasses; pass++)
        {
            for (int i = 0; i < pointCount - 1; i++)
            {
                var p0 = points[i];
                var p1 = points[i + 1];
                float dx = p1.X - p0.X;
                float dy = p1.Y - p0.Y;
                float dist = MathF.Sqrt(dx * dx + dy * dy);
                if (dist < 0.2f) continue;

                float nx = -dy / dist;
                float ny = dx / dist;

                float grainShift = DeterministicNoise(seed, i, 10 + pass) * (widths[i] * 0.35f);
                float grainAlpha = Math.Clamp(alphas[i] * 0.55f, 0.1f, 0.75f);
                float grainWidth = MathF.Max(widths[i] * 0.45f, 0.8f);
                var grainColor = WithAlpha(color, grainAlpha);

                session.DrawLine(
                    p0.X + nx * grainShift, p0.Y + ny * grainShift,
                    p1.X + nx * grainShift, p1.Y + ny * grainShift,
                    grainColor,
                    grainWidth,
                    RoundCapStrokeStyle);
            }
        }
    }

    /// <summary>
    /// Draws a pencil <see cref="Stroke"/> onto a Win2D <see cref="CanvasDrawingSession"/>.
    /// </summary>
    /// <param name="session">The active Win2D drawing session.</param>
    /// <param name="stroke">The whiteboard stroke to render.</param>
    public static void DrawPencil(
        this CanvasDrawingSession session,
        Stroke stroke)
    {
        if (session == null || stroke == null || stroke.Points == null || stroke.Points.Count == 0)
            return;

        var color = ColorFromArgb(stroke.Color);
        DrawPencil(session, stroke.Points, color, stroke.StrokeWidth);
    }

    /// <summary>
    /// Draws a pencil stroke onto a Win2D <see cref="CanvasDrawingSession"/> using sampled points and an ARGB long color.
    /// </summary>
    /// <param name="session">The active Win2D drawing session.</param>
    /// <param name="points">Ordered list of sampled stroke points.</param>
    /// <param name="argbColor">ARGB-packed 32-bit color.</param>
    /// <param name="baseWidth">The base stroke width in DIPs.</param>
    public static void DrawPencil(
        this CanvasDrawingSession session,
        IReadOnlyList<Point> points,
        long argbColor,
        float baseWidth)
    {
        DrawPencil(session, points, ColorFromArgb(argbColor), baseWidth);
    }

    /// <summary>
    /// Converts a 32-bit ARGB-packed color into a <see cref="Color"/>.
    /// </summary>
    public static Color ColorFromArgb(long argb)
    {
        byte a = (byte)((argb >> 24) & 0xFF);
        byte r = (byte)((argb >> 16) & 0xFF);
        byte g = (byte)((argb >> 8) & 0xFF);
        byte b = (byte)(argb & 0xFF);
        return new Color { A = a, R = r, G = g, B = b };
    }

    /// <summary>
    /// Creates a copy of <paramref name="color"/> with its alpha channel replaced by <paramref name="alpha"/> [0, 1].
    /// </summary>
    private static Color WithAlpha(Color color, float alpha)
    {
        byte a = (byte)Math.Clamp((int)Math.Round(alpha * 255.0f), 0, 255);
        return new Color { A = a, R = color.R, G = color.G, B = color.B };
    }
}
