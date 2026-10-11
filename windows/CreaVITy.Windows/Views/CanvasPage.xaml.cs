using System;
using System.Collections.Generic;
using System.Linq;
using System.Numerics;
using global::Windows.UI;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Input;
using Microsoft.Graphics.Canvas.UI.Xaml;
using Microsoft.Graphics.Canvas.Text;
using Microsoft.Graphics.Canvas.Geometry;
using CreaVITy.Windows.Domain.Models;
using CreaVITy.Windows.Presentation.ViewModels;
using CreaVITy.Windows.Presentation.Canvas.Renderers;

namespace CreaVITy.Windows.Views;

public sealed partial class CanvasPage : Page
{
    private WhiteboardViewModel _viewModel = null!;
    private readonly List<Point> _currentStrokePoints = new();
    private bool _isDrawing;
    private bool _isPanning;
    private global::Windows.Foundation.Point _lastPanPosition;
    private string _currentStrokeId = Guid.NewGuid().ToString();

    public CanvasPage()
    {
        this.InitializeComponent();
    }

    public void SetViewModel(WhiteboardViewModel viewModel)
    {
        _viewModel = viewModel;
        _viewModel.PropertyChanged += (s, e) =>
        {
            CanvasView?.Invalidate();
            UpdateZoomIndicator();
        };
        CanvasView?.Invalidate();
        UpdateZoomIndicator();
    }

    private void UpdateZoomIndicator()
    {
        if (_viewModel == null || ZoomPercentText == null) return;
        int pct = (int)Math.Round(_viewModel.UiState.Zoom * 100);
        ZoomPercentText.Text = $"{pct}%";
    }

    private void BackToBoards_Click(object sender, RoutedEventArgs e)
    {
        MainWindow.Instance?.NavigateTo(MainWindow.NavPage.Boards);
    }

    private void CanvasView_Draw(CanvasControl sender, CanvasDrawEventArgs args)
    {
        if (_viewModel == null) return;
        var ds = args.DrawingSession;
        var state = _viewModel.UiState;

        // 1. Clear with Effective Background Color (Matching M3 / Compose)
        long bgLong = state.EffectiveBackgroundColor;
        byte bgA = (byte)((bgLong >> 24) & 0xFF);
        byte bgR = (byte)((bgLong >> 16) & 0xFF);
        byte bgG = (byte)((bgLong >> 8) & 0xFF);
        byte bgB = (byte)(bgLong & 0xFF);
        var bgColor = Color.FromArgb(bgA == 0 ? (byte)255 : bgA, bgR, bgG, bgB);
        ds.Clear(bgColor);

        // 2. Apply Direct2D World Transformation (Scale & Translation)
        var transform = Matrix3x2.CreateScale(state.Zoom) * Matrix3x2.CreateTranslation(state.PanOffsetX, state.PanOffsetY);
        ds.Transform = transform;

        // 3. Draw Background Grid / Pattern
        DrawBackgroundPattern(ds, state, sender.Size.Width, sender.Size.Height);

        // 4. Draw Sticky Notes
        foreach (var note in state.StickyNotes)
        {
            DrawStickyNote(ds, note);
        }

        // 5. Draw Saved Strokes
        foreach (var stroke in state.Strokes)
        {
            DrawStroke(ds, stroke.Points, stroke.Color, stroke.StrokeWidth, stroke.Tool, stroke.IsEraser, bgColor);
        }

        // 6. Draw Active Stroke Currently Being Drawn
        if (_currentStrokePoints.Count > 0)
        {
            DrawStroke(ds, _currentStrokePoints, state.EffectivePenColor, state.CurrentWidth, state.SelectedTool, state.SelectedTool == DrawingTool.Eraser, bgColor);
        }

        // 7. Draw Text Elements
        foreach (var text in state.TextElements)
        {
            DrawTextElement(ds, text);
        }

        // 8. Draw Remote Peer Presences / Cursors
        foreach (var presence in state.LivePeerPresences.Values)
        {
            DrawPeerPresence(ds, presence);
        }

        // Reset Transform for screen overlay
        ds.Transform = Matrix3x2.Identity;
    }

    private void DrawBackgroundPattern(Microsoft.Graphics.Canvas.CanvasDrawingSession ds, WhiteboardUiState state, double viewportWidth, double viewportHeight)
    {
        float zoom = state.Zoom;
        float gridSpacing = 40f;
        float dotSize = 1.5f;

        // Viewport bounds in world space
        float left = -state.PanOffsetX / zoom - 80f;
        float top = -state.PanOffsetY / zoom - 80f;
        float right = left + (float)viewportWidth / zoom + 160f;
        float bottom = top + (float)viewportHeight / zoom + 160f;

        bool isDark = state.ThemeMode == ThemeMode.Dark;
        var dotColor = isDark ? Color.FromArgb(40, 255, 255, 255) : Color.FromArgb(40, 0, 0, 0);

        float startX = (float)Math.Floor(left / gridSpacing) * gridSpacing;
        float startY = (float)Math.Floor(top / gridSpacing) * gridSpacing;

        for (float x = startX; x <= right; x += gridSpacing)
        {
            for (float y = startY; y <= bottom; y += gridSpacing)
            {
                ds.FillCircle(x, y, dotSize, dotColor);
            }
        }
    }

    private void DrawStickyNote(Microsoft.Graphics.Canvas.CanvasDrawingSession ds, StickyNote note)
    {
        byte a = (byte)((note.Color >> 24) & 0xFF);
        byte r = (byte)((note.Color >> 16) & 0xFF);
        byte g = (byte)((note.Color >> 8) & 0xFF);
        byte b = (byte)(note.Color & 0xFF);
        var noteColor = Color.FromArgb(a == 0 ? (byte)255 : a, r, g, b);

        // Shadow & Card
        ds.FillRoundedRectangle(note.X + 3, note.Y + 3, note.Width, note.Height, 8, 8, Color.FromArgb(50, 0, 0, 0));
        ds.FillRoundedRectangle(note.X, note.Y, note.Width, note.Height, 8, 8, noteColor);
        ds.DrawRoundedRectangle(note.X, note.Y, note.Width, note.Height, 8, 8, Color.FromArgb(70, 0, 0, 0), 1.0f);

        // Author Header
        using var authorFormat = new CanvasTextFormat { FontSize = 11, FontWeight = Microsoft.UI.Text.FontWeights.SemiBold };
        ds.DrawText(note.Author, note.X + 12, note.Y + 8, Color.FromArgb(160, 0, 0, 0), authorFormat);

        // Note Body Text
        using var bodyFormat = new CanvasTextFormat { FontSize = 14, WordWrapping = CanvasWordWrapping.Wrap };
        ds.DrawText(note.Text, new global::Windows.Foundation.Rect(note.X + 12, note.Y + 28, note.Width - 24, note.Height - 36), Color.FromArgb(230, 0, 0, 0), bodyFormat);
    }

    private void DrawTextElement(Microsoft.Graphics.Canvas.CanvasDrawingSession ds, TextElement text)
    {
        byte a = (byte)((text.Color >> 24) & 0xFF);
        byte r = (byte)((text.Color >> 16) & 0xFF);
        byte g = (byte)((text.Color >> 8) & 0xFF);
        byte b = (byte)(text.Color & 0xFF);
        var textColor = Color.FromArgb(a == 0 ? (byte)255 : a, r, g, b);

        using var format = new CanvasTextFormat
        {
            FontSize = text.FontSize > 0 ? text.FontSize : 18,
            FontFamily = string.IsNullOrWhiteSpace(text.FontName) ? "Segoe UI" : text.FontName
        };
        ds.DrawText(text.Text, text.X, text.Y, textColor, format);
    }

    private void DrawPeerPresence(Microsoft.Graphics.Canvas.CanvasDrawingSession ds, PeerPresence presence)
    {
        if (presence.CurrentPoint == null) return;
        var pt = presence.CurrentPoint;

        byte a = (byte)((presence.Color >> 24) & 0xFF);
        byte r = (byte)((presence.Color >> 16) & 0xFF);
        byte g = (byte)((presence.Color >> 8) & 0xFF);
        byte b = (byte)(presence.Color & 0xFF);
        var cursorColor = Color.FromArgb(a == 0 ? (byte)255 : a, r, g, b);

        // Cursor Dot
        ds.FillCircle(pt.X, pt.Y, 5, cursorColor);
        ds.DrawCircle(pt.X, pt.Y, 7, Color.FromArgb(180, 255, 255, 255), 1.5f);

        // Peer Name Tag
        using var tagFormat = new CanvasTextFormat { FontSize = 11, FontWeight = Microsoft.UI.Text.FontWeights.Bold };
        ds.FillRoundedRectangle(pt.X + 8, pt.Y - 14, 60, 18, 4, 4, cursorColor);
        ds.DrawText(presence.Name, pt.X + 12, pt.Y - 12, Color.FromArgb(255, 255, 255, 255), tagFormat);
    }

    private void DrawStroke(Microsoft.Graphics.Canvas.CanvasDrawingSession ds, IReadOnlyList<Point> points, long colorLong, float width, DrawingTool tool, bool isEraser = false, Color? bgColor = null)
    {
        if (points.Count == 0) return;

        if (isEraser)
        {
            var eraseColor = bgColor ?? Color.FromArgb(255, 251, 248, 246);
            PenStrokeRenderer.DrawPen(ds, points.ToList(), eraseColor, width * 1.5f);
            return;
        }

        byte a = (byte)((colorLong >> 24) & 0xFF);
        byte r = (byte)((colorLong >> 16) & 0xFF);
        byte g = (byte)((colorLong >> 8) & 0xFF);
        byte b = (byte)(colorLong & 0xFF);
        var color = Color.FromArgb(a == 0 ? (byte)255 : a, r, g, b);

        switch (tool)
        {
            case DrawingTool.Pencil:
                PencilStrokeRenderer.DrawPencil(ds, points.ToList(), color, width);
                break;
            case DrawingTool.Calligraphy:
                CalligraphyStrokeRenderer.DrawCalligraphy(ds, points.ToList(), color, width);
                break;
            case DrawingTool.Highlighter:
                PenStrokeRenderer.DrawPen(ds, points.ToList(), Color.FromArgb(90, r, g, b), width * 2.2f);
                break;
            case DrawingTool.Rectangle:
                if (points.Count >= 2)
                {
                    var p1 = points[0];
                    var p2 = points[^1];
                    float rx = Math.Min(p1.X, p2.X);
                    float ry = Math.Min(p1.Y, p2.Y);
                    float rw = Math.Abs(p1.X - p2.X);
                    float rh = Math.Abs(p1.Y - p2.Y);
                    ds.DrawRectangle(rx, ry, rw, rh, color, width);
                }
                break;
            case DrawingTool.Circle:
                if (points.Count >= 2)
                {
                    var p1 = points[0];
                    var p2 = points[^1];
                    float cx = (p1.X + p2.X) / 2f;
                    float cy = (p1.Y + p2.Y) / 2f;
                    float radX = Math.Abs(p1.X - p2.X) / 2f;
                    float radY = Math.Abs(p1.Y - p2.Y) / 2f;
                    ds.DrawEllipse(cx, cy, radX, radY, color, width);
                }
                break;
            case DrawingTool.Arrow:
                if (points.Count >= 2)
                {
                    var p1 = points[0];
                    var p2 = points[^1];
                    ds.DrawLine(p1.X, p1.Y, p2.X, p2.Y, color, width);
                    DrawArrowHead(ds, p1.X, p1.Y, p2.X, p2.Y, color, width);
                }
                break;
            default:
                PenStrokeRenderer.DrawPen(ds, points.ToList(), color, width);
                break;
        }
    }

    private void DrawArrowHead(Microsoft.Graphics.Canvas.CanvasDrawingSession ds, float fromX, float fromY, float toX, float toY, Color color, float width)
    {
        double angle = Math.Atan2(toY - fromY, toX - fromX);
        float arrowLen = Math.Max(12f, width * 3f);
        float x1 = toX - (float)(arrowLen * Math.Cos(angle - Math.PI / 6));
        float y1 = toY - (float)(arrowLen * Math.Sin(angle - Math.PI / 6));
        float x2 = toX - (float)(arrowLen * Math.Cos(angle + Math.PI / 6));
        float y2 = toY - (float)(arrowLen * Math.Sin(angle + Math.PI / 6));
        ds.DrawLine(toX, toY, x1, y1, color, width);
        ds.DrawLine(toX, toY, x2, y2, color, width);
    }

    private void CanvasView_PointerWheelChanged(object sender, PointerRoutedEventArgs e)
    {
        if (_viewModel == null) return;
        var pt = e.GetCurrentPoint(CanvasView);
        float factor = pt.Properties.MouseWheelDelta > 0 ? 1.15f : 0.87f;
        _viewModel.OnTransform(factor, (float)pt.Position.X, (float)pt.Position.Y);
        CanvasView.Invalidate();
        UpdateZoomIndicator();
    }

    private void CanvasView_PointerPressed(object sender, PointerRoutedEventArgs e)
    {
        if (_viewModel == null) return;
        var pt = e.GetCurrentPoint(CanvasView);

        // Check for Middle-Click or Right-Click Drag for Panning
        if (pt.Properties.IsMiddleButtonPressed || pt.Properties.IsRightButtonPressed || _viewModel.UiState.SelectedTool == DrawingTool.SelectMove)
        {
            _isPanning = true;
            _lastPanPosition = pt.Position;
            CanvasView.CapturePointer(e.Pointer);
            return;
        }

        if (!pt.Properties.IsLeftButtonPressed) return;

        // Convert Screen Position to World Canvas Coordinates
        var state = _viewModel.UiState;
        float worldX = (float)((pt.Position.X - state.PanOffsetX) / state.Zoom);
        float worldY = (float)((pt.Position.Y - state.PanOffsetY) / state.Zoom);

        // Text & Sticky Note tool placement
        if (state.SelectedTool == DrawingTool.Text)
        {
            PromptAddText(worldX, worldY);
            return;
        }
        if (state.SelectedTool == DrawingTool.StickyNote)
        {
            PromptAddStickyNote(worldX, worldY);
            return;
        }

        _isDrawing = true;
        _currentStrokeId = Guid.NewGuid().ToString();
        var worldPt = new Point(worldX, worldY, pt.Properties.Pressure);

        _currentStrokePoints.Clear();
        _currentStrokePoints.Add(worldPt);

        _viewModel.BroadcastStrokeStart(
            _currentStrokeId,
            state.EffectivePenColor,
            state.CurrentWidth,
            state.SelectedTool == DrawingTool.Eraser,
            worldPt
        );

        CanvasView.CapturePointer(e.Pointer);
        CanvasView.Invalidate();
    }

    private void CanvasView_PointerMoved(object sender, PointerRoutedEventArgs e)
    {
        if (_viewModel == null) return;
        var pt = e.GetCurrentPoint(CanvasView);

        if (_isPanning)
        {
            float dx = (float)(pt.Position.X - _lastPanPosition.X);
            float dy = (float)(pt.Position.Y - _lastPanPosition.Y);
            _lastPanPosition = pt.Position;
            _viewModel.OnTransform(1.0f, panChangeX: dx, panChangeY: dy);
            CanvasView.Invalidate();
            return;
        }

        if (!_isDrawing) return;

        var state = _viewModel.UiState;
        float worldX = (float)((pt.Position.X - state.PanOffsetX) / state.Zoom);
        float worldY = (float)((pt.Position.Y - state.PanOffsetY) / state.Zoom);
        var worldPt = new Point(worldX, worldY, pt.Properties.Pressure);

        _currentStrokePoints.Add(worldPt);
        _viewModel.BroadcastStrokePoint(_currentStrokeId, worldPt);
        CanvasView.Invalidate();
    }

    private void CanvasView_PointerReleased(object sender, PointerRoutedEventArgs e)
    {
        if (_viewModel == null) return;

        if (_isPanning)
        {
            _isPanning = false;
            CanvasView.ReleasePointerCapture(e.Pointer);
            return;
        }

        if (!_isDrawing) return;
        _isDrawing = false;
        CanvasView.ReleasePointerCapture(e.Pointer);

        _viewModel.BroadcastStrokeEnd(_currentStrokeId);

        if (_currentStrokePoints.Count > 0)
        {
            var stroke = new Stroke
            {
                Id = _currentStrokeId,
                Points = _currentStrokePoints.ToList(),
                Color = _viewModel.UiState.EffectivePenColor,
                StrokeWidth = _viewModel.UiState.CurrentWidth,
                Tool = _viewModel.UiState.SelectedTool,
                IsEraser = _viewModel.UiState.SelectedTool == DrawingTool.Eraser
            };
            _viewModel.OnStrokeCompleted(stroke);
            _currentStrokePoints.Clear();
        }
        CanvasView.Invalidate();
    }

    private async void PromptAddText(float x, float y)
    {
        var box = new TextBox { PlaceholderText = "Enter text annotation...", TextWrapping = TextWrapping.Wrap };
        var dialog = new ContentDialog
        {
            Title = "Add Text Annotation",
            Content = box,
            PrimaryButtonText = "Add",
            CloseButtonText = "Cancel",
            XamlRoot = this.XamlRoot
        };

        if (await dialog.ShowAsync() == ContentDialogResult.Primary && !string.IsNullOrWhiteSpace(box.Text))
        {
            _viewModel.AddTextElement(box.Text.Trim(), x, y, _viewModel.UiState.EffectivePenColor, 20f, "Segoe UI");
        }
    }

    private async void PromptAddStickyNote(float x, float y)
    {
        var box = new TextBox { PlaceholderText = "Enter sticky note content...", AcceptsReturn = true, Height = 100 };
        var dialog = new ContentDialog
        {
            Title = "New Sticky Note",
            Content = box,
            PrimaryButtonText = "Create",
            CloseButtonText = "Cancel",
            XamlRoot = this.XamlRoot
        };

        if (await dialog.ShowAsync() == ContentDialogResult.Primary && !string.IsNullOrWhiteSpace(box.Text))
        {
            _viewModel.AddStickyNote(new StickyNote
            {
                Text = box.Text.Trim(),
                X = x,
                Y = y,
                Color = _viewModel.UiState.CurrentColor
            });
        }
    }

    private void ZoomIn_Click(object sender, RoutedEventArgs e)
    {
        _viewModel?.OnTransform(1.25f, (float)CanvasView.ActualWidth / 2f, (float)CanvasView.ActualHeight / 2f);
        CanvasView.Invalidate();
        UpdateZoomIndicator();
    }

    private void ZoomOut_Click(object sender, RoutedEventArgs e)
    {
        _viewModel?.OnTransform(0.80f, (float)CanvasView.ActualWidth / 2f, (float)CanvasView.ActualHeight / 2f);
        CanvasView.Invalidate();
        UpdateZoomIndicator();
    }

    private void ResetView_Click(object sender, RoutedEventArgs e)
    {
        _viewModel?.SetCanvasTransform(1.0f, 0f, 0f);
        CanvasView.Invalidate();
        UpdateZoomIndicator();
    }

    private void ToggleTheme_Click(object sender, RoutedEventArgs e)
    {
        if (_viewModel == null) return;
        var newTheme = _viewModel.UiState.ThemeMode == ThemeMode.Dark ? ThemeMode.Light : ThemeMode.Dark;
        long newBg = newTheme == ThemeMode.Dark ? unchecked((long)0xFF1E1F22) : unchecked((long)0xFFFBF8F6);
        long newInk = newTheme == ThemeMode.Dark ? unchecked((long)0xFFFBF8F6) : unchecked((long)0xFF1B1B1F);
        _viewModel.SetCanvasBackgroundColor(newBg);
        _viewModel.SetPenColor(newInk);
        CanvasView.Invalidate();
    }

    private void UndoButton_Click(object sender, RoutedEventArgs e) => _viewModel?.Undo();
    private void RedoButton_Click(object sender, RoutedEventArgs e) => _viewModel?.Redo();
    private void SaveButton_Click(object sender, RoutedEventArgs e) => _viewModel?.SaveCurrentBoard();
    private void ClearButton_Click(object sender, RoutedEventArgs e)
    {
        _viewModel?.BroadcastCanvasClear();
        _viewModel?.ClearCanvas();
    }

    private async void ExportButton_Click(object sender, RoutedEventArgs e)
    {
        if (_viewModel == null) return;
        var board = _viewModel.UiState.CurrentBoard;
        await Export.BoardExporter.ExportBoard(board, Export.ExportFormat.Png);
    }

    private void WidthBtn_Click(object sender, RoutedEventArgs e)
    {
        if (sender is Button btn && btn.Tag is string tag && float.TryParse(tag, out float w))
        {
            _viewModel?.SetStrokeWidth(w);
        }
    }

    private void ColorSwatch_Click(object sender, RoutedEventArgs e)
    {
        if (sender is Button btn && btn.Tag is string tag && long.TryParse(tag, out long color))
        {
            _viewModel?.SetPenColor(color);
        }
    }

    private void Tool_Checked(object sender, RoutedEventArgs e)
    {
        if (_viewModel == null) return;
        if (sender is RadioButton rb && rb.Tag is string tag)
        {
            var tool = tag switch
            {
                "Pencil" => DrawingTool.Pencil,
                "Calligraphy" => DrawingTool.Calligraphy,
                "Highlighter" => DrawingTool.Highlighter,
                "Eraser" => DrawingTool.Eraser,
                "Text" => DrawingTool.Text,
                "StickyNote" => DrawingTool.StickyNote,
                "Arrow" => DrawingTool.Arrow,
                "Rectangle" => DrawingTool.Rectangle,
                "Circle" => DrawingTool.Circle,
                "SelectMove" => DrawingTool.SelectMove,
                _ => DrawingTool.Pen
            };
            _viewModel.SelectTool(tool);
        }
    }
}
