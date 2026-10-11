using System;
using System.IO;
using System.Threading;
using Microsoft.Windows.ApplicationModel.DynamicDependency;
using Microsoft.UI.Dispatching;
using Microsoft.UI.Xaml;
using WinRT;

namespace CreaVITy.Windows;

public static class Program
{
    [System.Runtime.InteropServices.DllImport("Microsoft.ui.xaml.dll")]
    private static extern void XamlCheckProcessRequirements();

    [STAThread]
    public static void Main(string[] args)
    {
        string logPath = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.Desktop), "creavity_boot.txt");
        File.WriteAllText(logPath, $"Starting Main at {DateTime.Now}\n");

        try
        {
            // Explicitly initialize Windows App SDK bootstrapper for 1.6
            Bootstrap.Initialize(0x00010006, "");
            File.AppendAllText(logPath, "Bootstrap.Initialize succeeded\n");

            XamlCheckProcessRequirements();
            File.AppendAllText(logPath, "XamlCheckProcessRequirements succeeded\n");

            ComWrappersSupport.InitializeComWrappers();
            File.AppendAllText(logPath, "ComWrappers initialized\n");

            Application.Start(p =>
            {
                var context = new DispatcherQueueSynchronizationContext(DispatcherQueue.GetForCurrentThread());
                SynchronizationContext.SetSynchronizationContext(context);
                new App();
            });
        }
        catch (Exception ex)
        {
            File.AppendAllText(logPath, "Main FATAL EXCEPTION:\n" + ex.ToString() + "\n");
        }
        finally
        {
            try
            {
                Bootstrap.Shutdown();
            }
            catch { }
        }
    }
}
