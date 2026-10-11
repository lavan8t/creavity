using System;
using System.Collections.Generic;
using System.Numerics;
using Windows.UI;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.Geometry;
using CreaVITy.Windows.Domain.Math;
using CreaVITy.Windows.Domain.Models;

namespace CreaVITy.Windows.Presentation.Canvas.Renderers
{
    /// <summary>
    /// High-performance, mathematically rigorous broad-edge chisel nib calligraphy renderer.
    ///
    /// Produces authentic thick-and-thin lettering:
    /// - Broad-edge nib model oriented at 45° angle.
    /// - Direction-dependent width: strokes perpendicular to nib are broad, strokes parallel to nib are hairlines.
    /// - Pressure sensitivity gently modulates nib width.
    /// - Constructs a single continuous closed outline with smooth joins and clean chisel caps.
    /// - Zero gaps, zero overlapping transparency seams, zero disconnected polygon segments.
    /// </summary>
    public static class CalligraphyStrokeRenderer
    {
        /// <summary>Standard Western / italic chisel nib angle: 45 degrees (π / 4 radians).</summary>
        public const float DEFAULT_NIB_ANGLE_RAD = MathF.PI / 4.0f;

        private static readonly float CosAngle = MathF.Cos(DEFAULT_NIB_ANGLE_RAD);
        private static readonly float SinAngle = MathF.Sin(DEFAULT_NIB_ANGLE_RAD);

        /// <summary>
        /// Builds path commands representing the closed vector contour of the broad-edge calligraphy stroke.
        /// </summary>
        public static IReadOnlyList<PathCommand> CreateCalligraphyPathCommands(
            List<Point> points,
            float baseWidth)
        {
            return CreateCalligraphyPathCommands((IReadOnlyList<Point>)points, baseWidth, DEFAULT_NIB_ANGLE_RAD);
        }

        /// <summary>
        /// Builds path commands representing the closed vector contour of the broad-edge calligraphy stroke.
        /// </summary>
        public static IReadOnlyList<PathCommand> CreateCalligraphyPathCommands(
            IReadOnlyList<Point> points,
            float baseWidth,
            float nibAngleRad = DEFAULT_NIB_ANGLE_RAD)
        {
            if (points == null || points.Count == 0)
                return Array.Empty<PathCommand>();

            float cosA = MathF.Abs(nibAngleRad - DEFAULT_NIB_ANGLE_RAD) < 1e-6f ? CosAngle : MathF.Cos(nibAngleRad);
            float sinA = MathF.Abs(nibAngleRad - DEFAULT_NIB_ANGLE_RAD) < 1e-6f ? SinAngle : MathF.Sin(nibAngleRad);

            // Single tap chisel imprint
            if (points.Count == 1)
            {
                var p = points[0];
                float clampedPressure = Math.Clamp(p.Pressure, 0.2f, 1.4f);
                float w = MathF.Max(2f, baseWidth * (0.80f + 0.35f * clampedPressure));
                float halfW = w * 0.5f;
                float nx = cosA * halfW;
                float ny = -sinA * halfW;
                float perpX = -ny * 0.25f;
                float perpY = nx * 0.25f;

                return new List<PathCommand>(5)
                {
                    new MoveToCmd(p.X - nx, p.Y - ny),
                    new LineToCmd(p.X + perpX, p.Y + perpY),
                    new LineToCmd(p.X + nx, p.Y + ny),
                    new LineToCmd(p.X - perpX, p.Y - perpY),
                    new CloseCmd()
                };
            }

            int count = points.Count;
            var leftPoints = new Vector2[count];
            var rightPoints = new Vector2[count];

            // 1. Calculate left and right nib edges for each sampled point
            for (int i = 0; i < count; i++)
            {
                var pt = points[i];
                float clampedPressure = Math.Clamp(pt.Pressure, 0.2f, 1.4f);
                float w = MathF.Max(2f, baseWidth * (0.80f + 0.35f * clampedPressure));
                float halfW = w * 0.5f;
                float nx = cosA * halfW;
                float ny = -sinA * halfW;

                leftPoints[i] = new Vector2(pt.X - nx, pt.Y - ny);
                rightPoints[i] = new Vector2(pt.X + nx, pt.Y + ny);
            }

            var commands = new List<PathCommand>(count * 2 + 4);

            // 2. Start at first left edge point
            commands.Add(new MoveToCmd(leftPoints[0].X, leftPoints[0].Y));

            // 3. Trace left contour with smooth quadratic Bezier midpoints
            if (count == 2)
            {
                commands.Add(new LineToCmd(leftPoints[1].X, leftPoints[1].Y));
            }
            else
            {
                for (int i = 0; i < count - 1; i++)
                {
                    var pCurr = leftPoints[i];
                    var pNext = leftPoints[i + 1];
                    float midX = (pCurr.X + pNext.X) * 0.5f;
                    float midY = (pCurr.Y + pNext.Y) * 0.5f;
                    if (i == 0)
                    {
                        commands.Add(new LineToCmd(midX, midY));
                    }
                    else
                    {
                        commands.Add(new QuadraticToCmd(pCurr.X, pCurr.Y, midX, midY));
                    }
                }
                commands.Add(new LineToCmd(leftPoints[count - 1].X, leftPoints[count - 1].Y));
            }

            // 4. Chisel end cap: connect to right contour
            commands.Add(new LineToCmd(rightPoints[count - 1].X, rightPoints[count - 1].Y));

            // 5. Trace right contour backwards with smooth quadratic Bezier midpoints
            if (count == 2)
            {
                commands.Add(new LineToCmd(rightPoints[0].X, rightPoints[0].Y));
            }
            else
            {
                for (int i = count - 1; i >= 1; i--)
                {
                    var pCurr = rightPoints[i];
                    var pPrev = rightPoints[i - 1];
                    float midX = (pCurr.X + pPrev.X) * 0.5f;
                    float midY = (pCurr.Y + pPrev.Y) * 0.5f;
                    commands.Add(new QuadraticToCmd(pCurr.X, pCurr.Y, midX, midY));
                }
                commands.Add(new LineToCmd(rightPoints[0].X, rightPoints[0].Y));
            }

            // 6. Close back to start of left contour
            commands.Add(new CloseCmd());

            return commands;
        }

        /// <summary>
        /// Builds a Win2D <see cref="CanvasGeometry"/> representing the closed calligraphy stroke outline.
        /// Caller is responsible for disposing the returned geometry.
        /// </summary>
        public static CanvasGeometry? CreateCalligraphyGeometry(
            ICanvasResourceCreator resourceCreator,
            List<Point> points,
            float baseWidth)
        {
            return CreateCalligraphyGeometry(resourceCreator, (IReadOnlyList<Point>)points, baseWidth, DEFAULT_NIB_ANGLE_RAD);
        }

        /// <summary>
        /// Builds a Win2D <see cref="CanvasGeometry"/> representing the closed calligraphy stroke outline.
        /// Caller is responsible for disposing the returned geometry.
        /// </summary>
        public static CanvasGeometry? CreateCalligraphyGeometry(
            ICanvasResourceCreator resourceCreator,
            IReadOnlyList<Point> points,
            float baseWidth,
            float nibAngleRad = DEFAULT_NIB_ANGLE_RAD)
        {
            if (points == null || points.Count == 0)
                return null;

            float cosA = MathF.Abs(nibAngleRad - DEFAULT_NIB_ANGLE_RAD) < 1e-6f ? CosAngle : MathF.Cos(nibAngleRad);
            float sinA = MathF.Abs(nibAngleRad - DEFAULT_NIB_ANGLE_RAD) < 1e-6f ? SinAngle : MathF.Sin(nibAngleRad);

            // Single tap chisel imprint
            if (points.Count == 1)
            {
                var p = points[0];
                float clampedPressure = Math.Clamp(p.Pressure, 0.2f, 1.4f);
                float w = MathF.Max(2f, baseWidth * (0.80f + 0.35f * clampedPressure));
                float halfW = w * 0.5f;
                float nx = cosA * halfW;
                float ny = -sinA * halfW;
                float perpX = -ny * 0.25f;
                float perpY = nx * 0.25f;

                using var singleBuilder = new CanvasPathBuilder(resourceCreator);
                singleBuilder.BeginFigure(new Vector2(p.X - nx, p.Y - ny));
                singleBuilder.AddLine(new Vector2(p.X + perpX, p.Y + perpY));
                singleBuilder.AddLine(new Vector2(p.X + nx, p.Y + ny));
                singleBuilder.AddLine(new Vector2(p.X - perpX, p.Y - perpY));
                singleBuilder.EndFigure(CanvasFigureLoop.Closed);
                return CanvasGeometry.CreatePath(singleBuilder);
            }

            int count = points.Count;
            var leftPoints = new Vector2[count];
            var rightPoints = new Vector2[count];

            // 1. Calculate left and right nib edges for each sampled point
            for (int i = 0; i < count; i++)
            {
                var pt = points[i];
                float clampedPressure = Math.Clamp(pt.Pressure, 0.2f, 1.4f);
                float w = MathF.Max(2f, baseWidth * (0.80f + 0.35f * clampedPressure));
                float halfW = w * 0.5f;
                float nx = cosA * halfW;
                float ny = -sinA * halfW;

                leftPoints[i] = new Vector2(pt.X - nx, pt.Y - ny);
                rightPoints[i] = new Vector2(pt.X + nx, pt.Y + ny);
            }

            using var builder = new CanvasPathBuilder(resourceCreator);

            // 2. Start at first left edge point
            builder.BeginFigure(leftPoints[0]);

            // 3. Trace left contour with smooth quadratic Bezier midpoints
            if (count == 2)
            {
                builder.AddLine(leftPoints[1]);
            }
            else
            {
                for (int i = 0; i < count - 1; i++)
                {
                    var pCurr = leftPoints[i];
                    var pNext = leftPoints[i + 1];
                    var mid = (pCurr + pNext) * 0.5f;
                    if (i == 0)
                    {
                        builder.AddLine(mid);
                    }
                    else
                    {
                        builder.AddQuadraticBezier(pCurr, mid);
                    }
                }
                builder.AddLine(leftPoints[count - 1]);
            }

            // 4. Chisel end cap: connect to right contour
            builder.AddLine(rightPoints[count - 1]);

            // 5. Trace right contour backwards with smooth quadratic Bezier midpoints
            if (count == 2)
            {
                builder.AddLine(rightPoints[0]);
            }
            else
            {
                for (int i = count - 1; i >= 1; i--)
                {
                    var pCurr = rightPoints[i];
                    var pPrev = rightPoints[i - 1];
                    var mid = (pCurr + pPrev) * 0.5f;
                    builder.AddQuadraticBezier(pCurr, mid);
                }
                builder.AddLine(rightPoints[0]);
            }

            // 6. Close back to start of left contour
            builder.EndFigure(CanvasFigureLoop.Closed);

            return CanvasGeometry.CreatePath(builder);
        }

        /// <summary>
        /// Renders a calligraphy stroke on the given Win2D <see cref="CanvasDrawingSession"/>.
        /// </summary>
        public static void DrawCalligraphy(
            this CanvasDrawingSession session,
            List<Point> points,
            Color color,
            float baseWidth)
        {
            DrawCalligraphy(session, (IReadOnlyList<Point>)points, color, baseWidth, null, DEFAULT_NIB_ANGLE_RAD);
        }

        /// <summary>
        /// Renders a calligraphy stroke on the given Win2D <see cref="CanvasDrawingSession"/>,
        /// optionally utilizing a pre-computed <paramref name="cachedGeometry"/>.
        /// </summary>
        public static void DrawCalligraphy(
            this CanvasDrawingSession session,
            IReadOnlyList<Point> points,
            Color color,
            float baseWidth,
            CanvasGeometry? cachedGeometry = null,
            float nibAngleRad = DEFAULT_NIB_ANGLE_RAD)
        {
            if (points == null || points.Count == 0)
                return;

            if (cachedGeometry != null)
            {
                session.FillGeometry(cachedGeometry, color);
                return;
            }

            using var geometry = CreateCalligraphyGeometry(session, points, baseWidth, nibAngleRad);
            if (geometry != null)
            {
                session.FillGeometry(geometry, color);
            }
        }
    }
}
