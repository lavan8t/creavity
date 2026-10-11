using System;
using System.Linq;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using CreaVITy.Windows.Presentation.ViewModels;

namespace CreaVITy.Windows.Views;

public sealed partial class MainWindow : Window
{
    public static MainWindow? Instance { get; private set; }
    private readonly WhiteboardViewModel _viewModel;

    private readonly BoardsPage _boardsPage = new();
    private readonly CanvasPage _canvasPage = new();
    private readonly PeerConnectPage _peersPage = new();
    private readonly BinPage _binPage = new();
    private readonly SettingsPage _settingsPage = new();
    private readonly AboutPage _aboutPage = new();

    public enum NavPage
    {
        Boards,
        Canvas,
        Peers,
        Bin,
        Settings,
        About
    }

    public MainWindow()
    {
        Instance = this;
        this.InitializeComponent();

        _viewModel = new WhiteboardViewModel();

        _boardsPage.SetViewModel(_viewModel);
        _canvasPage.SetViewModel(_viewModel);
        _peersPage.SetViewModel(_viewModel);
        _binPage.SetViewModel(_viewModel);
        _settingsPage.SetViewModel(_viewModel);

        // Start on Boards Library screen exactly like Android startDestination
        NavigateTo(NavPage.Boards);
    }

    public void NavigateTo(NavPage page)
    {
        switch (page)
        {
            case NavPage.Boards:
                ContentFrame.Content = _boardsPage;
                SetNavSelection("Boards");
                break;
            case NavPage.Canvas:
                ContentFrame.Content = _canvasPage;
                SetNavSelection("Canvas");
                break;
            case NavPage.Peers:
                ContentFrame.Content = _peersPage;
                SetNavSelection("Peers");
                break;
            case NavPage.Bin:
                ContentFrame.Content = _binPage;
                SetNavSelection("Bin");
                break;
            case NavPage.Settings:
                ContentFrame.Content = _settingsPage;
                SetNavSelection("Settings");
                break;
            case NavPage.About:
                ContentFrame.Content = _aboutPage;
                SetNavSelection("About");
                break;
        }
    }

    private void SetNavSelection(string tag)
    {
        var item = NavView.MenuItems.OfType<NavigationViewItem>().FirstOrDefault(i => (string)i.Tag == tag)
                   ?? NavView.FooterMenuItems.OfType<NavigationViewItem>().FirstOrDefault(i => (string)i.Tag == tag);
        if (item != null)
        {
            NavView.SelectedItem = item;
        }
    }

    private void NavView_ItemInvoked(NavigationView sender, NavigationViewItemInvokedEventArgs args)
    {
        if (args.InvokedItemContainer is NavigationViewItem item && item.Tag is string tag)
        {
            if (Enum.TryParse<NavPage>(tag, true, out var navPage))
            {
                NavigateTo(navPage);
            }
        }
    }
}
