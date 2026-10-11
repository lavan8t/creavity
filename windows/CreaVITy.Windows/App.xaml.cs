using Microsoft.UI.Xaml;

namespace CreaVITy.Windows;

public partial class App : Application
{
    private Window? _window;

    public App()
    {
        this.InitializeComponent();
        this.UnhandledException += App_UnhandledException;
    }

    private void App_UnhandledException(object sender, Microsoft.UI.Xaml.UnhandledExceptionEventArgs e)
    {
        string logPath = System.IO.Path.Combine(System.Environment.GetFolderPath(System.Environment.SpecialFolder.Desktop), "creavity_crash.txt");
        System.IO.File.WriteAllText(logPath, e.Exception?.ToString() ?? e.Message);
    }

    protected override void OnLaunched(LaunchActivatedEventArgs args)
    {
        try
        {
            _window = new Views.MainWindow();
            _window.Activate();
        }
        catch (System.Exception ex)
        {
            string logPath = System.IO.Path.Combine(System.Environment.GetFolderPath(System.Environment.SpecialFolder.Desktop), "creavity_crash.txt");
            System.IO.File.WriteAllText(logPath, ex.ToString());
            throw;
        }
    }
}
