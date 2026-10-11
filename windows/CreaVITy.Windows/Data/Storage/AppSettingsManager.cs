using System;
using System.IO;
using System.Text.Json;
using CreaVITy.Windows.Domain.Models;

namespace CreaVITy.Windows.Data.Storage;

public enum ThemeMode { System, Light, Dark }

public class AppSettingsManager
{
    private static readonly Lazy<AppSettingsManager> _instance = new(() => new AppSettingsManager());
    public static AppSettingsManager Instance => _instance.Value;

    private readonly string _settingsFilePath;
    private readonly object _lock = new();

    private SettingsData _data;

    public AppSettingsManager()
    {
        string dir = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "CreaVITy");
        Directory.CreateDirectory(dir);
        _settingsFilePath = Path.Combine(dir, "settings.json");

        lock (_lock)
        {
            _data = LoadSettingsInternal();
        }
    }

    public string DisplayName
    {
        get { lock (_lock) return _data.DisplayName; }
        set { lock (_lock) { _data.DisplayName = value; SaveSettingsInternal(); } }
    }

    public bool HapticsEnabled
    {
        get { lock (_lock) return _data.HapticsEnabled; }
        set { lock (_lock) { _data.HapticsEnabled = value; SaveSettingsInternal(); } }
    }

    public float DefaultStrokeWidth
    {
        get { lock (_lock) return _data.DefaultStrokeWidth; }
        set { lock (_lock) { _data.DefaultStrokeWidth = value; SaveSettingsInternal(); } }
    }

    public AppThemePreset ThemePreset
    {
        get { lock (_lock) return _data.ThemePreset; }
        set { lock (_lock) { _data.ThemePreset = value; SaveSettingsInternal(); } }
    }

    public ThemeMode ThemeMode
    {
        get { lock (_lock) return _data.ThemeMode; }
        set { lock (_lock) { _data.ThemeMode = value; SaveSettingsInternal(); } }
    }

    private SettingsData LoadSettingsInternal()
    {
        if (!File.Exists(_settingsFilePath)) return new SettingsData();

        try
        {
            string json = File.ReadAllText(_settingsFilePath);
            return JsonSerializer.Deserialize<SettingsData>(json) ?? new SettingsData();
        }
        catch
        {
            return new SettingsData();
        }
    }

    private void SaveSettingsInternal()
    {
        try
        {
            string json = JsonSerializer.Serialize(_data, new JsonSerializerOptions { WriteIndented = true });
            File.WriteAllText(_settingsFilePath, json);
        }
        catch { }
    }

    private class SettingsData
    {
        public string DisplayName { get; set; } = "Whiteboard User";
        public bool HapticsEnabled { get; set; } = true;
        public float DefaultStrokeWidth { get; set; } = 6.0f;
        public AppThemePreset ThemePreset { get; set; } = AppThemePreset.Dynamic;
        public ThemeMode ThemeMode { get; set; } = ThemeMode.System;
    }
}
