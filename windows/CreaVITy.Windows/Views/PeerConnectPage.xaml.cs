using System.Collections.Generic;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using CreaVITy.Windows.Presentation.ViewModels;

namespace CreaVITy.Windows.Views;

public record PeerInfo(string NodeId, string Endpoint, string State);

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
        RefreshPeers();
    }

    private void RefreshPeers()
    {
        var peers = new List<PeerInfo>
        {
            new PeerInfo("local-node", "Local Device (Coordinator)", "Ready")
        };
        PeersListView.ItemsSource = peers;
    }

    private void HostRoom_Click(object sender, RoutedEventArgs e)
    {
        string roomId = string.IsNullOrWhiteSpace(RoomIdBox.Text) ? "CREAVITY-ROOM" : RoomIdBox.Text.Trim();
    }

    private void JoinRoom_Click(object sender, RoutedEventArgs e)
    {
        string roomId = string.IsNullOrWhiteSpace(RoomIdBox.Text) ? "CREAVITY-ROOM" : RoomIdBox.Text.Trim();
    }
}
