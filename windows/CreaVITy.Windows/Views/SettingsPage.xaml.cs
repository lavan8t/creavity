using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Controls.Primitives;
using CreaVITy.Windows.Presentation.ViewModels;

namespace CreaVITy.Windows.Views;

public sealed partial class SettingsPage : Page
{
    private WhiteboardViewModel _viewModel = null!;

    public SettingsPage()
    {
        this.InitializeComponent();
    }

    public void SetViewModel(WhiteboardViewModel viewModel)
    {
        _viewModel = viewModel;
        if (_viewModel != null)
        {
            DarkThemeToggle.IsOn = _viewModel.UiState.BackgroundColor == 0xFF121212L;
            StrokeWidthSlider.Value = _viewModel.UiState.CurrentWidth;
        }
    }

    private void GridTypeCombo_SelectionChanged(object sender, SelectionChangedEventArgs e)
    {
        // Grid type selection
    }

    private void DarkThemeToggle_Toggled(object sender, RoutedEventArgs e)
    {
        if (_viewModel == null) return;
        _viewModel.SetCanvasBackgroundColor(DarkThemeToggle.IsOn ? 0xFF121212L : 0xFFFFFFFFL);
    }

    private void SmoothingToggle_Toggled(object sender, RoutedEventArgs e)
    {
        // Laplacian smoothing toggle
    }

    private void StrokeWidthSlider_ValueChanged(object sender, RangeBaseValueChangedEventArgs e)
    {
        if (_viewModel == null) return;
        _viewModel.SetStrokeWidth((float)e.NewValue);
    }
}
