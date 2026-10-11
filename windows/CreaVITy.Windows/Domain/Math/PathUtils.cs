using System;
using System.Collections.Generic;
using CreaVITy.Windows.Domain.Models;
using CreaVITy.Windows.Presentation.Canvas.Renderers;

namespace CreaVITy.Windows.Domain.Math
{
    // ---------------------------------------------------------------------------
    // PathCommand discriminated union
    // ---------------------------------------------------------------------------

    public abstract record PathCommand;
    public sealed record MoveToCmd(float X, float Y) : PathCommand;
    public sealed record LineToCmd(float X, float Y) : PathCommand;
    public sealed record QuadraticToCmd(float Cx, float Cy, float X, float Y) : PathCommand;
    public sealed record CloseCmd : PathCommand;
    public sealed record AddRectCmd(float Left, float Top, float Right, float Bottom) : PathCommand;
    public sealed record AddOvalCmd(float Left, float Top, float Right, float Bottom) : PathCommand;

    // ---------------------------------------------------------------------------
    // PathUtils
    // ---------------------------------------------------------------------------

    public static class PathUtils
    {
        /// <summary>
        /// Filters points that are too close together, then applies a 3-point
        /// weighted Laplacian smooth (weights: 0.2 / 0.6 / 0.2).
        /// </summary>
        public static IReadOnlyList<Point> SmoothAndSimplifyPoints(
            IReadOnlyList<Point> points,
            float minDistance = 2.0f)
        {
            if (points.Count <= 2)
                return points;

            // ---- distance filter ----
            var filtered = new List<Point>(points.Count);
            filtered.Add(points[0]);
            var lastPt = points[0];
            float minDistSq = minDistance * minDistance;

            for (int i = 1; i < points.Count - 1; i++)
            {
                var pt = points[i];
                float dx = pt.X - lastPt.X;
                float dy = pt.Y - lastPt.Y;
                if (dx * dx + dy * dy >= minDistSq)
                {
                    filtered.Add(pt);
                    lastPt = pt;
                }
            }
            filtered.Add(points[points.Count - 1]);

            if (filtered.Count <= 2)
                return filtered;

            // ---- Laplacian smooth ----
            var smoothed = new List<Point>(filtered.Count);
            smoothed.Add(filtered[0]);

            for (int i = 1; i < filtered.Count - 1; i++)
            {
                var pPrev = filtered[i - 1];
                var pCurr = filtered[i];
                var pNext = filtered[i + 1];

                float sx = pPrev.X * 0.2f + pCurr.X * 0.6f + pNext.X * 0.2f;
                float sy = pPrev.Y * 0.2f + pCurr.Y * 0.6f + pNext.Y * 0.2f;
                float sp = pPrev.Pressure * 0.2f + pCurr.Pressure * 0.6f + pNext.Pressure * 0.2f;

                smoothed.Add(pCurr with { X = sx, Y = sy, Pressure = sp });
            }
            smoothed.Add(filtered[filtered.Count - 1]);

            return smoothed;
        }

        /// <summary>
        /// Builds a quadratic-Bézier midpoint-chain path from a smoothed/simplified
        /// point list. Each interior midpoint becomes a Bézier anchor; the original
        /// points become control points.
        /// </summary>
        public static IReadOnlyList<PathCommand> CreateSmoothPath(IReadOnlyList<Point> points)
        {
            var cmds = new List<PathCommand>();
            if (points.Count == 0)
                return cmds;

            if (points.Count == 1)
            {
                cmds.Add(new MoveToCmd(points[0].X, points[0].Y));
                return cmds;
            }

            if (points.Count == 2)
            {
                cmds.Add(new MoveToCmd(points[0].X, points[0].Y));
                cmds.Add(new LineToCmd(points[1].X, points[1].Y));
                return cmds;
            }

            // Start: move to midpoint between [0] and [1]
            float startMidX = (points[0].X + points[1].X) * 0.5f;
            float startMidY = (points[0].Y + points[1].Y) * 0.5f;
            cmds.Add(new MoveToCmd(startMidX, startMidY));

            // Middle segments: quadratic Bézier with control point = points[i],
            //   anchor = midpoint(points[i], points[i+1])
            for (int i = 1; i < points.Count - 1; i++)
            {
                var pCurr = points[i];
                var pNext = points[i + 1];
                float midX = (pCurr.X + pNext.X) * 0.5f;
                float midY = (pCurr.Y + pNext.Y) * 0.5f;
                cmds.Add(new QuadraticToCmd(pCurr.X, pCurr.Y, midX, midY));
            }

            // End: line to the last point
            cmds.Add(new LineToCmd(points[points.Count - 1].X, points[points.Count - 1].Y));

            return cmds;
        }

        /// <summary>
        /// Builds the path commands for a given <paramref name="tool"/> and point list.
        /// </summary>
        public static IReadOnlyList<PathCommand> CreateToolPath(
            DrawingTool tool,
            IReadOnlyList<Point> points,
            float strokeWidth = 6f)
        {
            if (points.Count == 0)
                return Array.Empty<PathCommand>();

            switch (tool)
            {
                case DrawingTool.Arrow:
                    return BuildArrowPath(points);

                case DrawingTool.Rectangle:
                    return BuildRectanglePath(points);

                case DrawingTool.Circle:
                    return BuildCirclePath(points);

                case DrawingTool.Triangle:
                    return BuildTrianglePath(points);

                case DrawingTool.Line:
                    return BuildLinePath(points);

                case DrawingTool.Calligraphy:
                    return CalligraphyStrokeRenderer.CreateCalligraphyPathCommands(points, strokeWidth);

                // PEN / MARKER / BRUSH / PENCIL / HIGHLIGHTER / ERASER / LASER
                default:
                    return CreateSmoothPath(SmoothAndSimplifyPoints(points));
            }
        }

        // -----------------------------------------------------------------------
        // Tool-specific path builders
        // -----------------------------------------------------------------------

        private static IReadOnlyList<PathCommand> BuildArrowPath(IReadOnlyList<Point> points)
        {
            var start = points[0];
            var end   = points[points.Count - 1];

            const float arrowLength = 28.0f;
            const float arrowAngle  = MathF.PI / 6f; // 30°

            float angle = MathF.Atan2(end.Y - start.Y, end.X - start.X);

            // Main shaft
            var cmds = new List<PathCommand>
            {
                new MoveToCmd(start.X, start.Y),
                new LineToCmd(end.X,   end.Y),
            };

            // Left arrowhead wing
            float leftAngle = angle + MathF.PI - arrowAngle;
            cmds.Add(new MoveToCmd(end.X, end.Y));
            cmds.Add(new LineToCmd(
                end.X + arrowLength * MathF.Cos(leftAngle),
                end.Y + arrowLength * MathF.Sin(leftAngle)));

            // Right arrowhead wing
            float rightAngle = angle + MathF.PI + arrowAngle;
            cmds.Add(new MoveToCmd(end.X, end.Y));
            cmds.Add(new LineToCmd(
                end.X + arrowLength * MathF.Cos(rightAngle),
                end.Y + arrowLength * MathF.Sin(rightAngle)));

            return cmds;
        }

        private static IReadOnlyList<PathCommand> BuildRectanglePath(IReadOnlyList<Point> points)
        {
            var start = points[0];
            var end   = points[points.Count - 1];

            float left   = MathF.Min(start.X, end.X);
            float top    = MathF.Min(start.Y, end.Y);
            float right  = MathF.Max(start.X, end.X);
            float bottom = MathF.Max(start.Y, end.Y);

            return new List<PathCommand>
            {
                new AddRectCmd(left, top, right, bottom),
            };
        }

        private static IReadOnlyList<PathCommand> BuildCirclePath(IReadOnlyList<Point> points)
        {
            var start = points[0];
            var end   = points[points.Count - 1];

            float left   = MathF.Min(start.X, end.X);
            float top    = MathF.Min(start.Y, end.Y);
            float right  = MathF.Max(start.X, end.X);
            float bottom = MathF.Max(start.Y, end.Y);

            return new List<PathCommand>
            {
                new AddOvalCmd(left, top, right, bottom),
            };
        }

        private static IReadOnlyList<PathCommand> BuildTrianglePath(IReadOnlyList<Point> points)
        {
            // start = first touch, end = last touch
            // moveTo(start.x, end.y) → lineTo(mid.x, start.y) → lineTo(end.x, end.y) → close
            var start = points[0];
            var end   = points[points.Count - 1];
            float midX = (start.X + end.X) * 0.5f;

            return new List<PathCommand>
            {
                new MoveToCmd(start.X, end.Y),
                new LineToCmd(midX,    start.Y),
                new LineToCmd(end.X,   end.Y),
                new CloseCmd(),
            };
        }

        private static IReadOnlyList<PathCommand> BuildLinePath(IReadOnlyList<Point> points)
        {
            var start = points[0];
            var end   = points[points.Count - 1];

            return new List<PathCommand>
            {
                new MoveToCmd(start.X, start.Y),
                new LineToCmd(end.X,   end.Y),
            };
        }

        // -----------------------------------------------------------------------
        // Point-in-polygon (ray-casting)
        // -----------------------------------------------------------------------

        /// <summary>
        /// Ray-casting algorithm: counts edge crossings along a horizontal ray
        /// cast rightward from <paramref name="point"/>.
        /// </summary>
        public static bool IsPointInPolygon(Point point, IReadOnlyList<Point> polygon)
        {
            int n = polygon.Count;
            if (n < 3)
                return false;

            bool inside = false;
            float px = point.X;
            float py = point.Y;

            for (int i = 0, j = n - 1; i < n; j = i++)
            {
                float xi = polygon[i].X, yi = polygon[i].Y;
                float xj = polygon[j].X, yj = polygon[j].Y;

                bool intersects = ((yi > py) != (yj > py))
                               && (px < (xj - xi) * (py - yi) / (yj - yi) + xi);

                if (intersects)
                    inside = !inside;
            }

            return inside;
        }

        /// <summary>
        /// Returns <c>true</c> if any point in <paramref name="strokePoints"/>
        /// lies inside <paramref name="polygon"/>.
        /// </summary>
        public static bool IsStrokeInPolygon(
            IReadOnlyList<Point> strokePoints,
            IReadOnlyList<Point> polygon)
        {
            foreach (var pt in strokePoints)
            {
                if (IsPointInPolygon(pt, polygon))
                    return true;
            }
            return false;
        }
    }
}
