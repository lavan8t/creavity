using System.Linq;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using CreaVITy.Windows.Presentation.ViewModels;

namespace CreaVITy.Windows.Views;

public sealed partial class BinPage : Page
{
    private WhiteboardViewModel _viewModel = null!;

    public BinPage()
    {
        this.InitializeComponent();
    }

    public void SetViewModel(WhiteboardViewModel viewModel)
    {
        _viewModel = viewModel;
        _viewModel.PropertyChanged += (s, e) => RefreshList();
        RefreshList();
    }

    private void RefreshList()
    {
        if (_viewModel == null) return;
        var binned = _viewModel.UiState.SavedBoards.Where(b => b.IsInBin).ToList();
        BinnedListView.ItemsSource = binned;
    }

    private void RestoreBoard_Click(object sender, RoutedEventArgs e)
    {
        if (sender is Button btn && btn.Tag is string boardId && _viewModel != null)
        {
            _viewModel.RestoreBoardFromBin(boardId);
            RefreshList();
        }
    }

    private void PurgeBoard_Click(object sender, RoutedEventArgs e)
    {
        if (sender is Button btn && btn.Tag is string boardId && _viewModel != null)
        {
            _viewModel.DeleteBoardPermanently(boardId);
            RefreshList();
        }
    }

    private void EmptyBin_Click(object sender, RoutedEventArgs e)
    {
        if (_viewModel == null) return;
        var binnedIds = _viewModel.UiState.SavedBoards.Where(b => b.IsInBin).Select(b => b.Id).ToList();
        foreach (var id in binnedIds)
        {
            _viewModel.DeleteBoardPermanently(id);
        }
        RefreshList();
    }
}
