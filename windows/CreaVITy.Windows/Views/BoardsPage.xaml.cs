using System;
using System.Collections.Generic;
using System.Linq;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using CreaVITy.Windows.Domain.Models;
using CreaVITy.Windows.Presentation.ViewModels;

namespace CreaVITy.Windows.Views;

public sealed partial class BoardsPage : Page
{
    private WhiteboardViewModel _viewModel = null!;

    public BoardsPage()
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
        var boards = _viewModel.UiState.SavedBoards.Where(b => !b.IsInBin).ToList();

        string query = SearchBox?.Text?.Trim().ToLowerInvariant() ?? "";
        if (!string.IsNullOrEmpty(query))
        {
            boards = boards.Where(b => 
                b.Title.ToLowerInvariant().Contains(query) ||
                b.Venue.ToLowerInvariant().Contains(query) ||
                b.Group.ToLowerInvariant().Contains(query)
            ).ToList();
        }

        switch (SortCombo?.SelectedIndex ?? 0)
        {
            case 0:
                boards = boards.OrderByDescending(b => b.LastAccessedAt).ToList();
                break;
            case 1:
                boards = boards.OrderByDescending(b => b.ModifiedAt).ToList();
                break;
            case 2:
                boards = boards.OrderBy(b => b.Title).ToList();
                break;
        }

        BoardsGridView.ItemsSource = boards;
    }

    private void SearchBox_TextChanged(object sender, TextChangedEventArgs e) => RefreshList();
    private void SortCombo_SelectionChanged(object sender, SelectionChangedEventArgs e) => RefreshList();

    private void NewBoard_Click(object sender, RoutedEventArgs e)
    {
        if (_viewModel == null) return;
        _viewModel.CreateNewBoard("Untitled Board");
        MainWindow.Instance?.NavigateTo(MainWindow.NavPage.Canvas);
    }

    private void BoardsGridView_ItemClick(object sender, ItemClickEventArgs e)
    {
        if (e.ClickedItem is Board board)
        {
            _viewModel?.SwitchBoard(board);
            MainWindow.Instance?.NavigateTo(MainWindow.NavPage.Canvas);
        }
    }

    private void DeleteBoard_Click(object sender, RoutedEventArgs e)
    {
        if (sender is Button btn && btn.Tag is string boardId && _viewModel != null)
        {
            _viewModel.MoveBoardToBin(boardId);
            RefreshList();
        }
    }
}
