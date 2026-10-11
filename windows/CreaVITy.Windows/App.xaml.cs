using Microsoft.UI.Xaml;

namespace CreaVITy.Windows;

public partial class App : Application
{
    private Window? _window;

    public App()
    {
        string logPath = System.IO.Path.Combine(System.Environment.GetFolderPath(System.Environment.SpecialFolder.Desktop), "creavity_boot.txt");
        System.IO.File.AppendAllText(logPath, "App ctor entered\n");
        this.InitializeComponent();
        System.IO.File.AppendAllText(logPath, "App InitializeComponent done\n");
        this.UnhandledException += App_UnhandledException;
    }

    private void App_UnhandledException(object sender, Microsoft.UI.Xaml.UnhandledExceptionEventArgs e)
    {
        string logPath = System.IO.Path.Combine(System.Environment.GetFolderPath(System.Environment.SpecialFolder.Desktop), "creavity_crash.txt");
        System.IO.File.WriteAllText(logPath, e.Exception?.ToString() ?? e.Message);
    }

    protected override void OnLaunched(LaunchActivatedEventArgs args)
    {
        string logPath = System.IO.Path.Combine(System.Environment.GetFolderPath(System.Environment.SpecialFolder.Desktop), "creavity_boot.txt");
        System.IO.File.AppendAllText(logPath, "OnLaunched entered\n");
        try
        {
            _window = new Views.MainWindow();
            System.IO.File.AppendAllText(logPath, "MainWindow instantiated\n");
            _window.Activate();
            System.IO.File.AppendAllText(logPath, "MainWindow activated!\n");
        }
        catch (System.Exception ex)
        {
            System.IO.File.AppendAllText(logPath, "OnLaunched EXCEPTION:\n" + ex.ToString() + "\n");
            throw;
        }
    }
}
