using System.Linq;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using CreaVITy.Windows.Presentation.ViewModels;

namespace CreaVITy.Windows.Views;

public sealed partial class PeerConnectPage : Page
{
    private WhiteboardViewModel _viewModel = null!;

    public PeerConnectPage()
    {
        this.InitializeComponent();
    }

    public void SetViewModel(WhiteboardViewModel viewModel)
    {
        _viewModel = viewModel;
        _viewModel.PropertyChanged += (s, e) => UpdateUi();
        UpdateUi();
    }

    private void UpdateUi()
    {
        if (_viewModel == null) return;
        var state = _viewModel.UiState;

        DeviceNameText.Text = string.IsNullOrWhiteSpace(state.DisplayName) ? "🎨 Windows PC" : state.DisplayName;

        if (state.IsAdvertising && state.IsDiscovering)
            SessionStatusText.Text = "Status: Hosting & Scanning";
        else if (state.IsAdvertising)
            SessionStatusText.Text = "Status: Hosting Room (Broadcasting Beacon)";
        else if (state.IsDiscovering)
            SessionStatusText.Text = "Status: Scanning Local Network";
        else if (state.ConnectedPeers.Count > 0)
            SessionStatusText.Text = $"Status: Connected to {state.ConnectedPeers.Count} Peer(s)";
        else
            SessionStatusText.Text = "Status: Standalone";

        HostButton.Content = state.IsAdvertising ? "Stop Hosting" : "Host Session";
        ScanButton.Content = state.IsDiscovering ? "Stop Scanning" : "Find Peers";

        if (!string.IsNullOrWhiteSpace(state.RoomCode))
        {
            RoomCodeBadge.Visibility = Visibility.Visible;
            RoomCodeText.Text = state.RoomCode;
        }
        else
        {
            RoomCodeBadge.Visibility = Visibility.Collapsed;
        }

        // Connected Peers
        if (state.ConnectedPeers.Count > 0)
        {
            ConnectedSection.Visibility = Visibility.Visible;
            ConnectedListView.ItemsSource = state.ConnectedPeers.ToList();
        }
        else
        {
            ConnectedSection.Visibility = Visibility.Collapsed;
            ConnectedListView.ItemsSource = null;
        }

        // Discovered Peers
        if (state.DiscoveredPeers.Count > 0)
        {
            EmptyPeersNotice.Visibility = Visibility.Collapsed;
            DiscoveredListView.Visibility = Visibility.Visible;
            DiscoveredListView.ItemsSource = state.DiscoveredPeers.ToList();
        }
        else
        {
            EmptyPeersNotice.Visibility = Visibility.Visible;
            DiscoveredListView.Visibility = Visibility.Collapsed;
            DiscoveredListView.ItemsSource = null;
        }
    }

    private void HostButton_Click(object sender, RoutedEventArgs e)
    {
        if (_viewModel == null) return;
        if (_viewModel.UiState.IsAdvertising)
        {
            _viewModel.StopAdvertising();
        }
        else
        {
            _viewModel.StartAdvertising();
        }
        UpdateUi();
    }

    private void ScanButton_Click(object sender, RoutedEventArgs e)
    {
        if (_viewModel == null) return;
        if (_viewModel.UiState.IsDiscovering)
        {
            _viewModel.StopDiscovery();
        }
        else
        {
            _viewModel.StartDiscovery();
        }
        UpdateUi();
    }

    private async void ConnectPeer_Click(object sender, RoutedEventArgs e)
    {
        if (_viewModel == null) return;
        if (sender is Button btn && btn.Tag is string endpointId)
        {
            btn.IsEnabled = false;
            btn.Content = "Connecting...";
            var ok = await _viewModel.ConnectToPeerAsync(endpointId);
            if (!ok)
            {
                btn.IsEnabled = true;
                btn.Content = "Retry";
            }
        }
    }

    private void DisconnectPeer_Click(object sender, RoutedEventArgs e)
    {
        if (_viewModel == null) return;
        if (sender is Button btn && btn.Tag is string endpointId)
        {
            _viewModel.DisconnectPeer(endpointId);
        }
    }
}
