using System;
using System.Collections.Generic;
using System.Linq;
using System.Numerics;
using Windows.UI;
using Microsoft.Graphics.Canvas.UI.Xaml;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Input;
using CreaVITy.Windows.Domain.Models;
using CreaVITy.Windows.Presentation.ViewModels;
using CreaVITy.Windows.Presentation.Canvas.Renderers;

namespace CreaVITy.Windows.Views;

public sealed partial class MainWindow : Window
{
    private readonly WhiteboardViewModel _viewModel;
    private readonly List<Point> _currentStrokePoints = new();
    private bool _isDrawing;

    public MainWindow()
    {
        try
        {
            _viewModel = new WhiteboardViewModel();
            this.InitializeComponent();
            _viewModel.PropertyChanged += (s, e) => CanvasView?.Invalidate();
        }
        catch (Exception ex)
        {
            string logPath = System.IO.Path.Combine(System.Environment.GetFolderPath(System.Environment.SpecialFolder.Desktop), "creavity_crash.txt");
            System.IO.File.WriteAllText(logPath, "MainWindow ctor: " + ex.ToString());
            throw;
        }
    }

    private void CanvasView_Draw(CanvasControl sender, CanvasDrawEventArgs args)
    {
        var ds = args.DrawingSession;
        var state = _viewModel.UiState;

        // Clear background
        byte a = (byte)((state.BackgroundColor >> 24) & 0xFF);
        byte r = (byte)((state.BackgroundColor >> 16) & 0xFF);
        byte g = (byte)((state.BackgroundColor >> 8) & 0xFF);
        byte b = (byte)(state.BackgroundColor & 0xFF);
        ds.Clear(Color.FromArgb(a == 0 ? (byte)255 : a, r, g, b));

        // Draw existing strokes
        foreach (var stroke in state.Strokes)
        {
            DrawStroke(ds, stroke.Points, stroke.Color, stroke.StrokeWidth, stroke.Tool);
        }

        // Draw active in-progress stroke
        if (_isDrawing && _currentStrokePoints.Count > 0)
        {
            DrawStroke(ds, _currentStrokePoints, state.CurrentColor, state.CurrentWidth, state.SelectedTool);
        }
    }

    private void DrawStroke(Microsoft.Graphics.Canvas.CanvasDrawingSession ds, IReadOnlyList<Point> points, long colorLong, float width, DrawingTool tool)
    {
        if (points.Count == 0) return;

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
            default:
                PenStrokeRenderer.DrawPen(ds, points.ToList(), color, width);
                break;
        }
    }

    private void CanvasView_PointerPressed(object sender, PointerRoutedEventArgs e)
    {
        var pt = e.GetCurrentPoint(CanvasView);
        if (!pt.Properties.IsLeftButtonPressed) return;

        _isDrawing = true;
        _currentStrokePoints.Clear();
        _currentStrokePoints.Add(new Point((float)pt.Position.X, (float)pt.Position.Y, pt.Properties.Pressure));
        CanvasView.CapturePointer(e.Pointer);
        CanvasView.Invalidate();
    }

    private void CanvasView_PointerMoved(object sender, PointerRoutedEventArgs e)
    {
        if (!_isDrawing) return;

        var pt = e.GetCurrentPoint(CanvasView);
        _currentStrokePoints.Add(new Point((float)pt.Position.X, (float)pt.Position.Y, pt.Properties.Pressure));
        CanvasView.Invalidate();
    }

    private void CanvasView_PointerReleased(object sender, PointerRoutedEventArgs e)
    {
        if (!_isDrawing) return;
        _isDrawing = false;
        CanvasView.ReleasePointerCapture(e.Pointer);

        if (_currentStrokePoints.Count > 0)
        {
            var stroke = new Stroke
            {
                Points = _currentStrokePoints.ToList(),
                Color = _viewModel.UiState.CurrentColor,
                StrokeWidth = _viewModel.UiState.CurrentWidth,
                Tool = _viewModel.UiState.SelectedTool
            };
            _viewModel.OnStrokeCompleted(stroke);
            _currentStrokePoints.Clear();
        }
        CanvasView.Invalidate();
    }

    private void UndoButton_Click(object sender, RoutedEventArgs e) => _viewModel.Undo();
    private void RedoButton_Click(object sender, RoutedEventArgs e) => _viewModel.Redo();
    private void SaveButton_Click(object sender, RoutedEventArgs e) => _viewModel.SaveCurrentBoard();
    private void ClearButton_Click(object sender, RoutedEventArgs e) => _viewModel.ClearCanvas();

    private async void ExportButton_Click(object sender, RoutedEventArgs e)
    {
        var board = _viewModel.UiState.CurrentBoard;
        await Export.BoardExporter.ExportBoard(board, Export.ExportFormat.Png);
    }

    private void Tool_Checked(object sender, RoutedEventArgs e)
    {
        if (sender is RadioButton rb && rb.Tag is string tag)
        {
            if (Enum.TryParse<DrawingTool>(tag, true, out var tool))
            {
                _viewModel.SelectTool(tool);
            }
        }
    }
}
