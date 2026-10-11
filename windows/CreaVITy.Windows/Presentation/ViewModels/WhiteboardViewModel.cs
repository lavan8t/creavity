using System;
using System.Collections.Generic;
using System.Linq;
using System.Numerics;
using System.Text.RegularExpressions;
using System.Threading;
using System.Threading.Tasks;
using CommunityToolkit.Mvvm.ComponentModel;
using CreaVITy.Windows.Data.Storage;
using CreaVITy.Windows.Domain.Models;
using CreaVITy.Windows.Domain.Mutations;

// Disambiguation aliases between Domain and Storage layers
using Board = CreaVITy.Windows.Domain.Models.Board;
using BoardPage = CreaVITy.Windows.Domain.Models.BoardPage;
using TextElement = CreaVITy.Windows.Domain.Models.TextElement;
using ImageElement = CreaVITy.Windows.Domain.Models.ImageElement;
using StickyNote = CreaVITy.Windows.Domain.Models.StickyNote;
using DrawingTool = CreaVITy.Windows.Domain.Models.DrawingTool;
using StorageBoard = CreaVITy.Windows.Data.Storage.Board;
using StorageBoardPage = CreaVITy.Windows.Data.Storage.BoardPage;
using StorageStroke = CreaVITy.Windows.Data.Storage.BoardStroke;
using StorageStrokePoint = CreaVITy.Windows.Data.Storage.StrokePoint;
using StorageTextElement = CreaVITy.Windows.Data.Storage.TextElement;
using StorageImageElement = CreaVITy.Windows.Data.Storage.ImageElement;
using StorageStickyNote = CreaVITy.Windows.Data.Storage.StickyNote;
using StorageDrawingTool = CreaVITy.Windows.Data.Storage.DrawingTool;
using StorageThemeMode = CreaVITy.Windows.Data.Storage.ThemeMode;
using ThemeMode = CreaVITy.Windows.Presentation.ViewModels.ThemeMode;

namespace CreaVITy.Windows.Presentation.ViewModels;

/// <summary>
/// Core ViewModel managing the active whiteboard document, multi-page layout,
/// undo/redo history, canvas element mutations, persistence, and UI presentation state.
/// Ported from Kotlin WhiteboardViewModel.
/// </summary>
public partial class WhiteboardViewModel : ObservableObject
{
    private readonly BoardManager _boardManager;
    private readonly AppSettingsManager _appSettings;
    private readonly MutationHistory _mutationHistory;
    private readonly object _persistLock = new();
    private CancellationTokenSource? _persistDebounceCts;

    [ObservableProperty]
    private WhiteboardUiState _uiState;

    /// <summary>Access to storage persistence subsystem.</summary>
    public BoardManager BoardManager => _boardManager;

    /// <summary>Access to application settings subsystem.</summary>
    public AppSettingsManager AppSettings => _appSettings;

    /// <summary>Access to undo/redo mutation history subsystem.</summary>
    public MutationHistory MutationHistory => _mutationHistory;

    public WhiteboardViewModel(
        BoardManager? boardManager = null,
        AppSettingsManager? appSettings = null,
        MutationHistory? mutationHistory = null)
    {
        _boardManager = boardManager ?? BoardManager.Instance;
        _appSettings = appSettings ?? AppSettingsManager.Instance;
        _mutationHistory = mutationHistory ?? new MutationHistory();

        _uiState = new WhiteboardUiState
        {
            DisplayName = _appSettings.DisplayName,
            HapticsEnabled = _appSettings.HapticsEnabled,
            CurrentWidth = _appSettings.DefaultStrokeWidth,
            ThemeMode = (ThemeMode)(int)_appSettings.ThemeMode
        };

        LoadSavedBoards();
    }

    private void UpdateUiState(Func<WhiteboardUiState, WhiteboardUiState> update)
    {
        UiState = update(UiState);
    }

    #region Board Management & Persistence

    /// <summary>
    /// Loads all saved boards from disk, aggregates folder groups, and restores initial active board.
    /// </summary>
    public void LoadSavedBoards()
    {
        var storageBoards = _boardManager.LoadAllBoards();
        var boards = storageBoards.Select(ToDomainBoard).ToList();
        var customFolders = _boardManager.GetCustomFolders();
        var allFolders = new HashSet<string>(customFolders, StringComparer.Ordinal);
        foreach (var b in boards)
        {
            var group = b.Group.Trim();
            if (!string.IsNullOrWhiteSpace(group))
            {
                allFolders.Add(group);
            }
        }

        var initial = boards.FirstOrDefault() ?? new Board { Id = string.Empty, Title = string.Empty };
        UpdateUiState(state => state with
        {
            CurrentBoard = initial,
            SavedBoards = boards,
            UserFolders = allFolders,
            BackgroundColor = boards.Count > 0 ? initial.BackgroundColor : state.BackgroundColor
        });
    }

    /// <summary>
    /// Creates a new whiteboard document and sets it as the active board.
    /// </summary>
    public void CreateNewBoard(string title = "Untitled Board")
    {
        var current = UiState.CurrentBoard;
        if (!string.IsNullOrWhiteSpace(current.Id))
        {
            Task.Run(() => _boardManager.SaveBoard(ToStorageBoard(current)));
        }

        long now = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        var newBoard = new Board
        {
            Title = string.IsNullOrWhiteSpace(title) ? "Untitled Board" : title,
            BackgroundColor = UiState.BackgroundColor,
            CreatedBy = UiState.DisplayName,
            OwnerName = UiState.DisplayName,
            CreatedAt = now,
            ModifiedAt = now,
            LastAccessedAt = now
        };
        PersistBoard(newBoard);
        UpdateUiState(state => state with
        {
            BackgroundColor = newBoard.BackgroundColor,
            Zoom = 1.0f,
            PanOffset = Vector2.Zero
        });
    }

    /// <summary>
    /// Switches active whiteboard to the specified board and persists last-accessed timestamp.
    /// </summary>
    public void SwitchBoard(Board board)
    {
        if (string.IsNullOrWhiteSpace(board.Id)) return;
        var current = UiState.CurrentBoard;
        long now = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        var updatedTarget = board with { LastAccessedAt = now };

        UpdateUiState(state =>
        {
            var updatedSaved = state.SavedBoards
                .Select(b => b.Id == updatedTarget.Id ? updatedTarget : b)
                .ToList();
            return state with
            {
                CurrentBoard = updatedTarget,
                SavedBoards = updatedSaved,
                BackgroundColor = updatedTarget.BackgroundColor,
                Zoom = 1.0f,
                PanOffset = Vector2.Zero
            };
        });

        Task.Run(() =>
        {
            if (!string.IsNullOrWhiteSpace(current.Id) && current.Id != board.Id)
            {
                _boardManager.SaveBoard(ToStorageBoard(current));
            }
            _boardManager.SaveBoard(ToStorageBoard(updatedTarget));
        });
    }

    /// <summary>
    /// Immediately persists the active board to disk.
    /// </summary>
    public void SaveCurrentBoard()
    {
        PersistBoard(UiState.CurrentBoard, immediate: true);
    }

    /// <summary>
    /// Persists the specified board to disk with debouncing (or immediately if specified).
    /// </summary>
    public void PersistBoard(Board board, bool immediate = false)
    {
        if (string.IsNullOrWhiteSpace(board.Id)) return;

        lock (_persistLock)
        {
            _persistDebounceCts?.Cancel();
            _persistDebounceCts?.Dispose();
            _persistDebounceCts = null;

            if (immediate)
            {
                Task.Run(() => _boardManager.SaveBoard(ToStorageBoard(board)));
            }
            else
            {
                var cts = new CancellationTokenSource();
                _persistDebounceCts = cts;
                var token = cts.Token;
                Task.Run(async () =>
                {
                    try
                    {
                        await Task.Delay(350, token);
                        if (!token.IsCancellationRequested)
                        {
                            _boardManager.SaveBoard(ToStorageBoard(board));
                        }
                    }
                    catch (OperationCanceledException) { }
                }, token);
            }
        }

        UpdateUiState(state =>
        {
            var updatedSaved = state.SavedBoards.Any(b => b.Id == board.Id)
                ? state.SavedBoards.Select(b => b.Id == board.Id ? board : b).ToList()
                : new[] { board }.Concat(state.SavedBoards).ToList();

            return state with
            {
                CurrentBoard = board,
                SavedBoards = updatedSaved
            };
        });
    }

    /// <summary>
    /// Renames a board document by ID.
    /// </summary>
    public void RenameBoard(string boardId, string newTitle)
    {
        if (string.IsNullOrWhiteSpace(newTitle)) return;
        var current = UiState.CurrentBoard;
        var target = current.Id == boardId ? current : UiState.SavedBoards.FirstOrDefault(b => b.Id == boardId);
        if (target == null) return;

        var updatedBoard = target with
        {
            Title = newTitle,
            ModifiedAt = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds()
        };
        PersistBoard(updatedBoard);
    }

    /// <summary>
    /// Toggles read-only lock on a board document.
    /// </summary>
    public void ToggleReadOnlyBoard(string boardId, bool isReadOnly)
    {
        var current = UiState.CurrentBoard;
        var target = current.Id == boardId ? current : UiState.SavedBoards.FirstOrDefault(b => b.Id == boardId);
        if (target == null) return;

        var updatedBoard = target with
        {
            IsReadOnly = isReadOnly,
            ModifiedAt = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds()
        };
        PersistBoard(updatedBoard);
    }

    /// <summary>
    /// Clones an existing board document with a new ID and title suffix.
    /// </summary>
    public void DuplicateBoard(string boardId)
    {
        var target = UiState.SavedBoards.FirstOrDefault(b => b.Id == boardId)
            ?? (UiState.CurrentBoard.Id == boardId ? UiState.CurrentBoard : null);
        if (target == null) return;

        long now = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        var duplicate = target with
        {
            Id = Guid.NewGuid().ToString(),
            Title = $"{target.Title} (Copy)",
            CreatedAt = now,
            ModifiedAt = now,
            LastAccessedAt = now
        };
        PersistBoard(duplicate);
    }

    /// <summary>
    /// Moves a board to the recycle bin. If it was active, switches to another non-binned board.
    /// </summary>
    public void MoveBoardToBin(string boardId)
    {
        var target = UiState.SavedBoards.FirstOrDefault(b => b.Id == boardId)
            ?? (UiState.CurrentBoard.Id == boardId ? UiState.CurrentBoard : null);
        if (target == null) return;

        var updated = target with
        {
            IsInBin = true,
            BinnedAt = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds()
        };
        PersistBoard(updated);

        if (UiState.CurrentBoard.Id == boardId)
        {
            var available = UiState.SavedBoards.Where(b => b.Id != boardId && !b.IsInBin).ToList();
            if (available.Count > 0)
            {
                SwitchBoard(available[0]);
            }
            else
            {
                UpdateUiState(state => state with
                {
                    CurrentBoard = new Board { Id = string.Empty, Title = string.Empty }
                });
            }
        }
    }

    /// <summary>Alias for <see cref="MoveBoardToBin"/>.</summary>
    public void DeleteBoard(string boardId) => MoveBoardToBin(boardId);

    /// <summary>
    /// Restores a board from the recycle bin.
    /// </summary>
    public void RestoreBoardFromBin(string boardId)
    {
        var target = UiState.SavedBoards.FirstOrDefault(b => b.Id == boardId);
        if (target == null) return;

        var updated = target with { IsInBin = false, BinnedAt = null };
        PersistBoard(updated);
    }

    /// <summary>
    /// Permanently removes a board file from disk.
    /// </summary>
    public void DeleteBoardPermanently(string boardId)
    {
        var remaining = UiState.SavedBoards.Where(b => b.Id != boardId).ToList();
        bool isCurrent = UiState.CurrentBoard.Id == boardId;
        if (isCurrent)
        {
            var available = remaining.Where(b => !b.IsInBin).ToList();
            if (available.Count > 0)
            {
                SwitchBoard(available[0]);
            }
            else
            {
                UpdateUiState(state => state with
                {
                    CurrentBoard = new Board { Id = string.Empty, Title = string.Empty },
                    SavedBoards = remaining
                });
            }
        }
        else
        {
            UpdateUiState(state => state with { SavedBoards = remaining });
        }

        Task.Run(() => _boardManager.DeleteBoard(boardId));
    }

    /// <summary>
    /// Permanently deletes all boards currently in the recycle bin.
    /// </summary>
    public void EmptyBin()
    {
        var binned = UiState.SavedBoards.Where(b => b.IsInBin).ToList();
        var remaining = UiState.SavedBoards.Where(b => !b.IsInBin).ToList();
        UpdateUiState(state => state with { SavedBoards = remaining });

        Task.Run(() =>
        {
            foreach (var b in binned)
            {
                _boardManager.DeleteBoard(b.Id);
            }
        });
    }

    /// <summary>Toggles starred status on a board.</summary>
    public void ToggleStarBoard(string boardId)
    {
        var current = UiState.CurrentBoard;
        var target = current.Id == boardId ? current : UiState.SavedBoards.FirstOrDefault(b => b.Id == boardId);
        if (target == null) return;

        var updated = target with
        {
            IsStarred = !target.IsStarred,
            ModifiedAt = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds()
        };
        PersistBoard(updated);
    }

    /// <summary>Updates title, venue, group/folder, and tags for a board.</summary>
    public void UpdateBoardMetadata(string boardId, string title, string venue, string group, IReadOnlyList<string> labels)
    {
        var current = UiState.CurrentBoard;
        var target = UiState.SavedBoards.FirstOrDefault(b => b.Id == boardId) ?? (current.Id == boardId ? current : null);
        if (target == null) return;

        var updated = target with
        {
            Title = string.IsNullOrWhiteSpace(title) ? target.Title : title,
            Venue = venue,
            Group = group,
            Labels = labels,
            ModifiedAt = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds()
        };

        var updatedSaved = UiState.SavedBoards.Select(b => b.Id == boardId ? updated : b).ToList();
        var updatedCurrent = UiState.CurrentBoard.Id == boardId ? updated : UiState.CurrentBoard;
        var updatedFolders = new HashSet<string>(UiState.UserFolders, StringComparer.Ordinal);
        if (!string.IsNullOrWhiteSpace(group))
        {
            updatedFolders.Add(group.Trim());
            _boardManager.SaveCustomFolders(new HashSet<string>(updatedFolders, StringComparer.Ordinal));
        }

        UpdateUiState(s => s with
        {
            CurrentBoard = updatedCurrent,
            SavedBoards = updatedSaved,
            UserFolders = updatedFolders
        });

        Task.Run(() => _boardManager.SaveBoard(ToStorageBoard(updated)));
    }

    #endregion

    #region Page Management

    /// <summary>
    /// Appends a new blank page to the active board and activates it.
    /// </summary>
    public void AddNewPage()
    {
        var board = UiState.CurrentBoard;
        int newPageNumber = board.Pages.Count + 1;
        var newPage = new BoardPage { Name = $"Page {newPageNumber}" };
        var updatedPages = board.Pages.Append(newPage).ToList();
        var updatedBoard = board with
        {
            Pages = updatedPages,
            ActivePageIndex = updatedPages.Count - 1,
            ModifiedAt = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds()
        };
        PersistBoard(updatedBoard);
    }

    /// <summary>
    /// Switches the active page index on the current board.
    /// </summary>
    public void SwitchPage(int index)
    {
        var board = UiState.CurrentBoard;
        if (index >= 0 && index < board.Pages.Count)
        {
            var updatedBoard = board with { ActivePageIndex = index };
            PersistBoard(updatedBoard);
        }
    }

    /// <summary>
    /// Deletes the page at the given index. Clears canvas if only one page remains.
    /// </summary>
    public void DeletePage(int index)
    {
        var board = UiState.CurrentBoard;
        if (board.Pages.Count <= 1)
        {
            ClearCanvas();
            return;
        }

        var updatedPages = board.Pages.Where((_, i) => i != index).ToList();
        int newIndex = Math.Max(0, index >= updatedPages.Count ? updatedPages.Count - 1 : index);
        var updatedBoard = board with
        {
            Pages = updatedPages,
            ActivePageIndex = newIndex,
            ModifiedAt = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds()
        };
        PersistBoard(updatedBoard);
    }

    #endregion

    #region Drawing & Canvas Element Operations

    /// <summary>
    /// Appends a completed ink stroke, records an AddStroke mutation, and creates milestone snapshots.
    /// </summary>
    public void OnStrokeCompleted(Stroke stroke)
    {
        var state = UiState;
        var baseBoard = string.IsNullOrWhiteSpace(state.CurrentBoard.Id)
            ? new Board
            {
                Title = "Untitled Board",
                CreatedBy = state.DisplayName,
                OwnerName = state.DisplayName
            }
            : state.CurrentBoard;

        var taggedStroke = stroke.Author == "You" || string.IsNullOrWhiteSpace(stroke.Author)
            ? stroke with { Author = state.DisplayName }
            : stroke;

        var activePage = baseBoard.Pages.ElementAtOrDefault(baseBoard.ActivePageIndex) ?? new BoardPage();
        var updatedStrokes = activePage.Strokes.Append(taggedStroke).ToList();
        var updatedPage = activePage with { Strokes = updatedStrokes };

        var updatedPages = baseBoard.Pages.ToList();
        if (baseBoard.ActivePageIndex >= 0 && baseBoard.ActivePageIndex < updatedPages.Count)
        {
            updatedPages[baseBoard.ActivePageIndex] = updatedPage;
        }
        else
        {
            updatedPages.Add(updatedPage);
        }

        long now = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        var updatedBoard = baseBoard with
        {
            Pages = updatedPages,
            ModifiedAt = now,
            LastAccessedAt = now
        };

        PersistBoard(updatedBoard);
        RecordMutation(new BoardMutation.AddStroke(taggedStroke));

        if (updatedStrokes.Count % 15 == 0)
        {
            CreateVersionSnapshot($"Milestone ({updatedStrokes.Count} strokes)");
        }
    }

    /// <summary>
    /// Adds a text annotation element to the active page and records an AddText mutation.
    /// </summary>
    public void AddTextElement(string text, float x, float y, long color, float fontSize, string fontName)
    {
        if (string.IsNullOrWhiteSpace(text)) return;
        var state = UiState;
        var baseBoard = string.IsNullOrWhiteSpace(state.CurrentBoard.Id)
            ? new Board
            {
                Title = "Untitled Board",
                CreatedBy = state.DisplayName,
                OwnerName = state.DisplayName
            }
            : state.CurrentBoard;

        var newText = new TextElement
        {
            Text = text,
            X = x,
            Y = y,
            Color = color,
            FontSize = fontSize,
            FontName = string.IsNullOrWhiteSpace(fontName) ? "Segoe UI" : fontName,
            Author = state.DisplayName
        };

        var activePage = baseBoard.Pages.ElementAtOrDefault(baseBoard.ActivePageIndex) ?? new BoardPage();
        var updatedTextList = activePage.TextElements.Append(newText).ToList();
        var updatedPage = activePage with { TextElements = updatedTextList };

        var updatedPages = baseBoard.Pages.ToList();
        if (baseBoard.ActivePageIndex >= 0 && baseBoard.ActivePageIndex < updatedPages.Count)
        {
            updatedPages[baseBoard.ActivePageIndex] = updatedPage;
        }
        else
        {
            updatedPages.Add(updatedPage);
        }

        long now = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        var updatedBoard = baseBoard with
        {
            Pages = updatedPages,
            ModifiedAt = now,
            LastAccessedAt = now
        };

        PersistBoard(updatedBoard);
        RecordMutation(new BoardMutation.AddText(newText));
    }

    /// <summary>
    /// Removes a stroke by ID and logs a DeleteStroke mutation.
    /// </summary>
    public void DeleteStrokeById(string strokeId)
    {
        var state = UiState;
        var activePage = state.ActivePage;
        var deletedStroke = activePage.Strokes.FirstOrDefault(s => s.Id == strokeId);
        var updatedStrokes = activePage.Strokes.Where(s => s.Id != strokeId).ToList();
        var updatedPage = activePage with { Strokes = updatedStrokes };

        var updatedPages = state.CurrentBoard.Pages.ToList();
        if (state.CurrentBoard.ActivePageIndex >= 0 && state.CurrentBoard.ActivePageIndex < updatedPages.Count)
        {
            updatedPages[state.CurrentBoard.ActivePageIndex] = updatedPage;
        }

        long now = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        var updatedBoard = state.CurrentBoard with
        {
            Pages = updatedPages,
            ModifiedAt = now,
            LastAccessedAt = now
        };

        PersistBoard(updatedBoard);
        if (deletedStroke != null)
        {
            RecordMutation(new BoardMutation.DeleteStroke(deletedStroke));
        }
    }

    /// <summary>
    /// Removes a text element by ID and logs a DeleteText mutation.
    /// </summary>
    public void DeleteTextElementById(string textId)
    {
        var state = UiState;
        var activePage = state.ActivePage;
        var deletedText = activePage.TextElements.FirstOrDefault(t => t.Id == textId);
        var updatedTextList = activePage.TextElements.Where(t => t.Id != textId).ToList();
        var updatedPage = activePage with { TextElements = updatedTextList };

        var updatedPages = state.CurrentBoard.Pages.ToList();
        if (state.CurrentBoard.ActivePageIndex >= 0 && state.CurrentBoard.ActivePageIndex < updatedPages.Count)
        {
            updatedPages[state.CurrentBoard.ActivePageIndex] = updatedPage;
        }

        long now = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        var updatedBoard = state.CurrentBoard with
        {
            Pages = updatedPages,
            ModifiedAt = now,
            LastAccessedAt = now
        };

        PersistBoard(updatedBoard);
        if (deletedText != null)
        {
            RecordMutation(new BoardMutation.DeleteText(deletedText));
        }
    }

    /// <summary>
    /// Removes an image element by ID and logs a DeleteImage mutation.
    /// </summary>
    public void DeleteImageElementById(string imageId)
    {
        var state = UiState;
        var activePage = state.ActivePage;
        var deletedImg = activePage.ImageElements.FirstOrDefault(i => i.Id == imageId);
        var updatedImageList = activePage.ImageElements.Where(i => i.Id != imageId).ToList();
        var updatedPage = activePage with { ImageElements = updatedImageList };

        var updatedPages = state.CurrentBoard.Pages.ToList();
        if (state.CurrentBoard.ActivePageIndex >= 0 && state.CurrentBoard.ActivePageIndex < updatedPages.Count)
        {
            updatedPages[state.CurrentBoard.ActivePageIndex] = updatedPage;
        }

        long now = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        var updatedBoard = state.CurrentBoard with
        {
            Pages = updatedPages,
            ModifiedAt = now,
            LastAccessedAt = now
        };
        PersistBoard(updatedBoard);

        if (deletedImg != null)
        {
            RecordMutation(new BoardMutation.DeleteImage(deletedImg));
        }
    }

    /// <summary>
    /// Adds a sticky note to the active page.
    /// </summary>
    public void AddStickyNote(StickyNote note)
    {
        var currentBoard = UiState.CurrentBoard;
        int activePageIndex = currentBoard.ActivePageIndex;
        var page = currentBoard.Pages.ElementAtOrDefault(activePageIndex) ?? new BoardPage();
        var updatedNotes = page.StickyNotes.Append(note).ToList();
        var updatedPage = page with { StickyNotes = updatedNotes };

        var updatedPages = currentBoard.Pages.ToList();
        if (activePageIndex >= 0 && activePageIndex < updatedPages.Count)
        {
            updatedPages[activePageIndex] = updatedPage;
        }
        else
        {
            updatedPages.Add(updatedPage);
        }

        long now = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        var updatedBoard = currentBoard with
        {
            Pages = updatedPages,
            ModifiedAt = now,
            LastAccessedAt = now
        };
        PersistBoard(updatedBoard);
    }

    /// <summary>
    /// Deletes a sticky note by ID from the active page.
    /// </summary>
    public void DeleteStickyNoteById(string noteId)
    {
        var currentBoard = UiState.CurrentBoard;
        int activePageIndex = currentBoard.ActivePageIndex;
        var page = currentBoard.Pages.ElementAtOrDefault(activePageIndex) ?? new BoardPage();
        var updatedNotes = page.StickyNotes.Where(n => n.Id != noteId).ToList();
        var updatedPage = page with { StickyNotes = updatedNotes };

        var updatedPages = currentBoard.Pages.ToList();
        if (activePageIndex >= 0 && activePageIndex < updatedPages.Count)
        {
            updatedPages[activePageIndex] = updatedPage;
        }

        long now = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        var updatedBoard = currentBoard with
        {
            Pages = updatedPages,
            ModifiedAt = now,
            LastAccessedAt = now
        };
        PersistBoard(updatedBoard);
    }

    /// <summary>
    /// Clears all strokes, text annotations, and images from the active page with undo support.
    /// </summary>
    public void ClearCanvas()
    {
        var state = UiState;
        var activePage = state.ActivePage;
        if (activePage.Strokes.Count > 0 || activePage.TextElements.Count > 0 || activePage.ImageElements.Count > 0)
        {
            RecordMutation(new BoardMutation.ClearPage(
                previousStrokes: activePage.Strokes,
                previousTexts: activePage.TextElements,
                previousImages: activePage.ImageElements
            ));
        }

        var updatedActivePage = activePage with
        {
            Strokes = Array.Empty<Stroke>(),
            TextElements = Array.Empty<TextElement>(),
            ImageElements = Array.Empty<ImageElement>()
        };

        var updatedPages = state.CurrentBoard.Pages.ToList();
        if (state.CurrentBoard.ActivePageIndex >= 0 && state.CurrentBoard.ActivePageIndex < updatedPages.Count)
        {
            updatedPages[state.CurrentBoard.ActivePageIndex] = updatedActivePage;
        }

        long now = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        var updatedBoard = state.CurrentBoard with
        {
            Pages = updatedPages,
            ModifiedAt = now,
            LastAccessedAt = now
        };
        PersistBoard(updatedBoard);
    }

    /// <summary>
    /// Applies batch translation changes to strokes, text elements, and images with undo support.
    /// </summary>
    public void UpdateMovedElements(
        IReadOnlyList<Stroke> movedStrokes,
        IReadOnlyList<TextElement> movedTexts,
        IReadOnlyList<ImageElement>? movedImages = null)
    {
        movedImages ??= Array.Empty<ImageElement>();
        var state = UiState;
        var activePage = state.ActivePage;

        var strokeMap = movedStrokes.ToDictionary(s => s.Id);
        var textMap = movedTexts.ToDictionary(t => t.Id);
        var imageMap = movedImages.ToDictionary(i => i.Id);

        var previousStrokes = activePage.Strokes.Where(s => strokeMap.ContainsKey(s.Id)).ToList();
        var previousTexts = activePage.TextElements.Where(t => textMap.ContainsKey(t.Id)).ToList();
        var previousImages = activePage.ImageElements.Where(i => imageMap.ContainsKey(i.Id)).ToList();

        var updatedStrokes = activePage.Strokes.Select(s => strokeMap.TryGetValue(s.Id, out var m) ? m : s).ToList();
        var updatedTexts = activePage.TextElements.Select(t => textMap.TryGetValue(t.Id, out var m) ? m : t).ToList();
        var updatedImages = activePage.ImageElements.Select(i => imageMap.TryGetValue(i.Id, out var m) ? m : i).ToList();

        var updatedPage = activePage with
        {
            Strokes = updatedStrokes,
            TextElements = updatedTexts,
            ImageElements = updatedImages
        };

        var updatedPages = state.CurrentBoard.Pages.ToList();
        if (state.CurrentBoard.ActivePageIndex >= 0 && state.CurrentBoard.ActivePageIndex < updatedPages.Count)
        {
            updatedPages[state.CurrentBoard.ActivePageIndex] = updatedPage;
        }

        long now = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        var updatedBoard = state.CurrentBoard with
        {
            Pages = updatedPages,
            ModifiedAt = now,
            LastAccessedAt = now
        };
        PersistBoard(updatedBoard);

        RecordMutation(new BoardMutation.MoveElements(
            previousStrokes: previousStrokes,
            newStrokes: movedStrokes,
            previousTexts: previousTexts,
            newTexts: movedTexts,
            previousImages: previousImages,
            newImages: movedImages
        ));
    }

    /// <summary>
    /// Clones specified strokes and text elements with an offset and appends them to the active page.
    /// </summary>
    public void DuplicateElements(IReadOnlyList<string> strokeIds, IReadOnlyList<string> textIds)
    {
        var state = UiState;
        var activePage = state.ActivePage;
        var strokeIdSet = new HashSet<string>(strokeIds);
        var textIdSet = new HashSet<string>(textIds);

        const float offset = 24f;
        var newStrokes = activePage.Strokes
            .Where(s => strokeIdSet.Contains(s.Id))
            .Select(s => s with
            {
                Id = Guid.NewGuid().ToString(),
                Points = s.Points.Select(p => p with { X = p.X + offset, Y = p.Y + offset }).ToList(),
                Author = state.DisplayName
            })
            .ToList();

        var newTexts = activePage.TextElements
            .Where(t => textIdSet.Contains(t.Id))
            .Select(t => t with
            {
                Id = Guid.NewGuid().ToString(),
                X = t.X + offset,
                Y = t.Y + offset,
                Author = state.DisplayName
            })
            .ToList();

        var updatedStrokes = activePage.Strokes.Concat(newStrokes).ToList();
        var updatedTexts = activePage.TextElements.Concat(newTexts).ToList();
        var updatedPage = activePage with
        {
            Strokes = updatedStrokes,
            TextElements = updatedTexts
        };

        var updatedPages = state.CurrentBoard.Pages.ToList();
        if (state.CurrentBoard.ActivePageIndex >= 0 && state.CurrentBoard.ActivePageIndex < updatedPages.Count)
        {
            updatedPages[state.CurrentBoard.ActivePageIndex] = updatedPage;
        }

        long now = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        var updatedBoard = state.CurrentBoard with
        {
            Pages = updatedPages,
            ModifiedAt = now,
            LastAccessedAt = now
        };
        PersistBoard(updatedBoard);
    }

    /// <summary>
    /// Batch-recolors specified strokes and text elements with undo support.
    /// </summary>
    public void RecolorElements(IReadOnlyList<string> strokeIds, IReadOnlyList<string> textIds, long color)
    {
        var state = UiState;
        var activePage = state.ActivePage;
        var strokeIdSet = new HashSet<string>(strokeIds);
        var textIdSet = new HashSet<string>(textIds);

        var previousStrokes = activePage.Strokes.Where(s => strokeIdSet.Contains(s.Id)).ToList();
        var previousTexts = activePage.TextElements.Where(t => textIdSet.Contains(t.Id)).ToList();

        var updatedStrokes = activePage.Strokes
            .Select(s => strokeIdSet.Contains(s.Id) ? s with { Color = color } : s)
            .ToList();
        var updatedTexts = activePage.TextElements
            .Select(t => textIdSet.Contains(t.Id) ? t with { Color = color } : t)
            .ToList();

        var updatedPage = activePage with
        {
            Strokes = updatedStrokes,
            TextElements = updatedTexts
        };

        var updatedPages = state.CurrentBoard.Pages.ToList();
        if (state.CurrentBoard.ActivePageIndex >= 0 && state.CurrentBoard.ActivePageIndex < updatedPages.Count)
        {
            updatedPages[state.CurrentBoard.ActivePageIndex] = updatedPage;
        }

        long now = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        var updatedBoard = state.CurrentBoard with
        {
            Pages = updatedPages,
            ModifiedAt = now,
            LastAccessedAt = now
        };
        PersistBoard(updatedBoard);

        RecordMutation(new BoardMutation.RecolorElements(
            previousStrokes: previousStrokes,
            newStrokes: updatedStrokes.Where(s => strokeIdSet.Contains(s.Id)).ToList(),
            previousTexts: previousTexts,
            newTexts: updatedTexts.Where(t => textIdSet.Contains(t.Id)).ToList()
        ));
    }

    #endregion

    #region Undo / Redo & Mutations

    /// <summary>
    /// Records a reversible canvas mutation to history and updates undo/redo state flags.
    /// </summary>
    public void RecordMutation(BoardMutation mutation)
    {
        _mutationHistory.Record(mutation);
        UpdateUiState(state => state with
        {
            CanUndo = _mutationHistory.CanUndo,
            CanRedo = _mutationHistory.CanRedo
        });
    }

    /// <summary>
    /// Reverts the most recent mutation on the active page.
    /// </summary>
    public void Undo()
    {
        var state = UiState;
        var (updatedPage, undoneMutation) = _mutationHistory.Undo(state.ActivePage);
        if (undoneMutation == null) return;

        var updatedPages = state.CurrentBoard.Pages.ToList();
        if (state.CurrentBoard.ActivePageIndex >= 0 && state.CurrentBoard.ActivePageIndex < updatedPages.Count)
        {
            updatedPages[state.CurrentBoard.ActivePageIndex] = updatedPage;
        }

        long now = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        var updatedBoard = state.CurrentBoard with
        {
            Pages = updatedPages,
            ModifiedAt = now,
            LastAccessedAt = now
        };

        PersistBoard(updatedBoard);
        UpdateUiState(s => s with
        {
            CanUndo = _mutationHistory.CanUndo,
            CanRedo = _mutationHistory.CanRedo
        });
    }

    /// <summary>
    /// Re-applies the most recently reverted mutation on the active page.
    /// </summary>
    public void Redo()
    {
        var state = UiState;
        var (updatedPage, redoneMutation) = _mutationHistory.Redo(state.ActivePage);
        if (redoneMutation == null) return;

        var updatedPages = state.CurrentBoard.Pages.ToList();
        if (state.CurrentBoard.ActivePageIndex >= 0 && state.CurrentBoard.ActivePageIndex < updatedPages.Count)
        {
            updatedPages[state.CurrentBoard.ActivePageIndex] = updatedPage;
        }

        long now = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        var updatedBoard = state.CurrentBoard with
        {
            Pages = updatedPages,
            ModifiedAt = now,
            LastAccessedAt = now
        };

        PersistBoard(updatedBoard);
        UpdateUiState(s => s with
        {
            CanUndo = _mutationHistory.CanUndo,
            CanRedo = _mutationHistory.CanRedo
        });
    }

    #endregion

    #region Version Snapshots

    /// <summary>
    /// Captures the full content of the active page into a named version snapshot.
    /// </summary>
    public void CreateVersionSnapshot(string name = "Manual Snapshot")
    {
        var state = UiState;
        var activePage = state.ActivePage;
        var snapshot = new BoardVersionSnapshot
        {
            Name = string.IsNullOrWhiteSpace(name) ? $"Snapshot #{state.CurrentBoard.VersionHistory.Count + 1}" : name,
            Author = state.DisplayName,
            PageIndex = state.CurrentBoard.ActivePageIndex,
            Strokes = activePage.Strokes,
            TextElements = activePage.TextElements,
            ImageElements = activePage.ImageElements,
            StickyNotes = activePage.StickyNotes
        };

        var updatedHistory = new[] { snapshot }.Concat(state.CurrentBoard.VersionHistory).ToList();
        var updatedBoard = state.CurrentBoard with { VersionHistory = updatedHistory };
        PersistBoard(updatedBoard);
        UpdateUiState(s => s with { VersionHistory = updatedHistory });
    }

    /// <summary>
    /// Restores the active page state to match a named version snapshot with undo support.
    /// </summary>
    public void RestoreVersionSnapshot(BoardVersionSnapshot snapshot)
    {
        var state = UiState;
        var activePage = state.ActivePage;

        RecordMutation(new BoardMutation.RestoreVersion(
            previousStrokes: activePage.Strokes,
            newStrokes: snapshot.Strokes,
            previousTexts: activePage.TextElements,
            newTexts: snapshot.TextElements
        ));

        var updatedPage = activePage with
        {
            Strokes = snapshot.Strokes,
            TextElements = snapshot.TextElements
        };
        var updatedPages = state.CurrentBoard.Pages.ToList();
        if (state.CurrentBoard.ActivePageIndex >= 0 && state.CurrentBoard.ActivePageIndex < updatedPages.Count)
        {
            updatedPages[state.CurrentBoard.ActivePageIndex] = updatedPage;
        }

        long now = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        var updatedBoard = state.CurrentBoard with
        {
            Pages = updatedPages,
            ModifiedAt = now,
            LastAccessedAt = now
        };
        PersistBoard(updatedBoard);
    }

    #endregion

    #region Tooling, Viewport & Preferences

    /// <summary>Selects the active drawing tool.</summary>
    public void SelectTool(DrawingTool tool) =>
        UpdateUiState(s => s with { SelectedTool = tool });

    /// <summary>Sets the active drawing stroke color and switches away from eraser.</summary>
    public void SetPenColor(long color) =>
        UpdateUiState(s => s with
        {
            CurrentColor = color,
            SelectedTool = (s.SelectedTool == DrawingTool.Eraser || s.SelectedTool == DrawingTool.SelectErase)
                ? DrawingTool.Pen
                : s.SelectedTool
        });

    /// <summary>Sets the canvas background color for the current board.</summary>
    public void SetCanvasBackgroundColor(long color)
    {
        var updatedBoard = UiState.CurrentBoard with
        {
            BackgroundColor = color,
            ModifiedAt = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds()
        };
        PersistBoard(updatedBoard);
        UpdateUiState(s => s with { BackgroundColor = color });
    }

    /// <summary>Applies zoom and pan delta transformations around a focal point.</summary>
    public void OnTransform(float zoomChange, float focalX = 0f, float focalY = 0f, float panChangeX = 0f, float panChangeY = 0f)
    {
        UpdateUiState(state =>
        {
            float oldZoom = state.Zoom;
            float newZoom = Math.Clamp(oldZoom * zoomChange, 0.15f, 6.0f);
            float actualScale = newZoom / oldZoom;

            Vector2 newPan;
            if (Math.Abs(zoomChange - 1.0f) < 0.0001f || Math.Abs(actualScale - 1.0f) < 0.0001f)
            {
                newPan = state.PanOffset + new Vector2(panChangeX, panChangeY);
            }
            else
            {
                newPan = new Vector2(
                    focalX - (focalX - state.PanOffset.X) * actualScale + panChangeX,
                    focalY - (focalY - state.PanOffset.Y) * actualScale + panChangeY
                );
            }

            return state with
            {
                Zoom = newZoom,
                PanOffset = newPan
            };
        });
    }

    /// <summary>Sets absolute canvas zoom and pan values.</summary>
    public void SetCanvasTransform(float zoom, float panOffsetX, float panOffsetY)
    {
        UpdateUiState(state => state with
        {
            Zoom = Math.Clamp(zoom, 0.2f, 5.0f),
            PanOffset = new Vector2(panOffsetX, panOffsetY)
        });
    }

    /// <summary>Resets canvas zoom to 1.0 and pan to origin.</summary>
    public void ResetCanvasTransform()
    {
        UpdateUiState(state => state with
        {
            Zoom = 1.0f,
            PanOffset = Vector2.Zero
        });
    }

    /// <summary>Sets default stroke thickness and updates app settings.</summary>
    public void SetStrokeWidth(float width)
    {
        _appSettings.DefaultStrokeWidth = width;
        UpdateUiState(s => s with { CurrentWidth = width });
    }

    /// <summary>Toggles infinite canvas mode on the active board.</summary>
    public void ToggleInfiniteCanvas(bool enabled)
    {
        var updatedBoard = UiState.CurrentBoard with
        {
            IsInfiniteCanvas = enabled,
            ModifiedAt = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds()
        };
        PersistBoard(updatedBoard);
        UpdateUiState(s => s with { IsInfiniteCanvas = enabled });
    }

    /// <summary>Sets and normalizes external Google Slides presentation embed URL.</summary>
    public void SetPresentationUrl(string? url)
    {
        string? normalized = url == null ? null : NormalizeGoogleSlidesUrl(url);
        var updatedBoard = UiState.CurrentBoard with
        {
            PresentationUrl = normalized,
            ModifiedAt = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds()
        };
        PersistBoard(updatedBoard);
        UpdateUiState(s => s with { PresentationUrl = normalized });
    }

    /// <summary>Toggles full-screen presentation mode.</summary>
    public void SetPresentationMode(bool enabled) =>
        UpdateUiState(s => s with { IsPresentationMode = enabled });

    /// <summary>Updates local user display name.</summary>
    public void UpdateDisplayName(string name)
    {
        string safeName = string.IsNullOrWhiteSpace(name) ? "Whiteboard User" : name;
        _appSettings.DisplayName = safeName;
        UpdateUiState(s => s with { DisplayName = safeName });
    }

    /// <summary>Toggles haptic feedback preference.</summary>
    public void SetHapticsEnabled(bool enabled)
    {
        _appSettings.HapticsEnabled = enabled;
        UpdateUiState(s => s with { HapticsEnabled = enabled });
    }

    /// <summary>Sets application theme preset.</summary>
    public void SetAppThemePreset(AppThemePreset preset) =>
        UpdateUiState(s => s with { SelectedThemePreset = preset });

    /// <summary>Sets light/dark theme mode and persists to settings.</summary>
    public void SetThemeMode(ThemeMode mode)
    {
        _appSettings.ThemeMode = (StorageThemeMode)(int)mode;
        UpdateUiState(s => s with { ThemeMode = mode });
    }

    /// <summary>Sets canvas background grid pattern.</summary>
    public void SetBackgroundPattern(CanvasBackgroundPattern pattern) =>
        UpdateUiState(s => s with { BackgroundPattern = pattern });

    private static string NormalizeGoogleSlidesUrl(string input)
    {
        var trimmed = input.Trim();
        var match = Regex.Match(trimmed, @"docs\.google\.com/presentation/d/([a-zA-Z0-9_-]+)");
        if (match.Success)
        {
            string id = match.Groups[1].Value;
            return $"https://docs.google.com/presentation/d/{id}/embed?start=false&loop=false&delayms=3000";
        }
        return trimmed;
    }

    #endregion

    #region Domain <-> Storage Mapping

    private static StorageDrawingTool ToStorageTool(DrawingTool tool) => tool switch
    {
        DrawingTool.Pen => StorageDrawingTool.Pen,
        DrawingTool.Highlighter => StorageDrawingTool.Highlighter,
        DrawingTool.Marker => StorageDrawingTool.Marker,
        DrawingTool.Eraser => StorageDrawingTool.Eraser,
        DrawingTool.Laser => StorageDrawingTool.Laser,
        DrawingTool.Select or DrawingTool.SelectErase or DrawingTool.SelectMove
            or DrawingTool.RectSelect or DrawingTool.LassoSelect => StorageDrawingTool.Selection,
        _ => StorageDrawingTool.Pen
    };

    private static DrawingTool ToDomainTool(StorageDrawingTool tool) => tool switch
    {
        StorageDrawingTool.Pen => DrawingTool.Pen,
        StorageDrawingTool.Highlighter => DrawingTool.Highlighter,
        StorageDrawingTool.Marker => DrawingTool.Marker,
        StorageDrawingTool.Eraser => DrawingTool.Eraser,
        StorageDrawingTool.Laser => DrawingTool.Laser,
        StorageDrawingTool.Selection => DrawingTool.Select,
        _ => DrawingTool.Pen
    };

    private static StorageStrokePoint ToStoragePoint(Point p) =>
        new() { X = p.X, Y = p.Y, P = p.Pressure, T = p.TimestampMs, Tilt = p.Tilt, Ori = p.Orientation };

    private static Point ToDomainPoint(StorageStrokePoint p) =>
        new(p.X, p.Y, p.P, p.T, p.Tilt ?? 0f, p.Ori ?? 0f);

    private static StorageStroke ToStorageStroke(Stroke s) =>
        new()
        {
            Id = s.Id,
            Color = s.Color,
            StrokeWidth = s.StrokeWidth,
            Tool = ToStorageTool(s.Tool),
            IsEraser = s.IsEraser,
            Author = s.Author,
            CreatedAt = s.CreatedAt,
            Points = s.Points.Select(ToStoragePoint).ToList()
        };

    private static Stroke ToDomainStroke(StorageStroke s) =>
        new()
        {
            Id = s.Id,
            Color = s.Color,
            StrokeWidth = s.StrokeWidth,
            Tool = ToDomainTool(s.Tool),
            IsEraser = s.IsEraser,
            Author = s.Author,
            CreatedAt = s.CreatedAt,
            Points = s.Points.Select(ToDomainPoint).ToList()
        };

    private static StorageTextElement ToStorageText(TextElement t) =>
        new()
        {
            Id = t.Id,
            Text = t.Text,
            X = t.X,
            Y = t.Y,
            Color = t.Color,
            FontSize = t.FontSize,
            FontName = t.FontName,
            Author = t.Author,
            CreatedAt = t.CreatedAt
        };

    private static TextElement ToDomainText(StorageTextElement t) =>
        new()
        {
            Id = t.Id,
            Text = t.Text,
            X = t.X,
            Y = t.Y,
            Color = t.Color,
            FontSize = t.FontSize,
            FontName = t.FontName,
            Author = t.Author,
            CreatedAt = t.CreatedAt
        };

    private static StorageImageElement ToStorageImage(ImageElement i) =>
        new()
        {
            Id = i.Id,
            Path = i.Path,
            X = i.X,
            Y = i.Y,
            Width = i.Width,
            Height = i.Height,
            Author = i.Author,
            CreatedAt = i.CreatedAt
        };

    private static ImageElement ToDomainImage(StorageImageElement i) =>
        new()
        {
            Id = i.Id,
            Path = i.Path,
            X = i.X,
            Y = i.Y,
            Width = i.Width,
            Height = i.Height,
            Author = i.Author,
            CreatedAt = i.CreatedAt
        };

    private static StorageStickyNote ToStorageStickyNote(StickyNote sn) =>
        new()
        {
            Id = sn.Id,
            Text = sn.Text,
            X = sn.X,
            Y = sn.Y,
            Width = sn.Width,
            Height = sn.Height,
            Color = sn.Color,
            Author = sn.Author,
            CreatedAt = sn.CreatedAt
        };

    private static StickyNote ToDomainStickyNote(StorageStickyNote sn) =>
        new()
        {
            Id = sn.Id,
            Text = sn.Text,
            X = sn.X,
            Y = sn.Y,
            Width = sn.Width,
            Height = sn.Height,
            Color = sn.Color,
            Author = sn.Author,
            CreatedAt = sn.CreatedAt
        };

    private static StorageBoardPage ToStoragePage(BoardPage p) =>
        new()
        {
            Id = p.Id,
            Name = p.Name,
            Strokes = p.Strokes.Select(ToStorageStroke).ToList(),
            TextElements = p.TextElements.Select(ToStorageText).ToList(),
            ImageElements = p.ImageElements.Select(ToStorageImage).ToList(),
            StickyNotes = p.StickyNotes.Select(ToStorageStickyNote).ToList()
        };

    private static BoardPage ToDomainPage(StorageBoardPage p) =>
        new()
        {
            Id = p.Id,
            Name = p.Name,
            Strokes = p.Strokes.Select(ToDomainStroke).ToList(),
            TextElements = p.TextElements.Select(ToDomainText).ToList(),
            ImageElements = p.ImageElements.Select(ToDomainImage).ToList(),
            StickyNotes = p.StickyNotes.Select(ToDomainStickyNote).ToList()
        };

    private static StorageBoard ToStorageBoard(Board b) =>
        new()
        {
            Id = b.Id,
            Title = b.Title,
            ActivePageIndex = b.ActivePageIndex,
            BackgroundColor = b.BackgroundColor,
            CreatedAt = b.CreatedAt,
            ModifiedAt = b.ModifiedAt,
            LastAccessedAt = b.LastAccessedAt,
            CreatedBy = b.CreatedBy,
            OwnerName = b.OwnerName,
            IsReadOnly = b.IsReadOnly,
            IsStarred = b.IsStarred,
            IsInBin = b.IsInBin,
            BinnedAt = b.BinnedAt,
            IsInfiniteCanvas = b.IsInfiniteCanvas,
            CanvasWidth = b.CanvasWidth,
            CanvasHeight = b.CanvasHeight,
            PresentationUrl = b.PresentationUrl,
            Venue = b.Venue,
            Group = b.Group,
            Labels = b.Labels.ToList(),
            Pages = b.Pages.Select(ToStoragePage).ToList()
        };

    private static Board ToDomainBoard(StorageBoard b) =>
        new()
        {
            Id = b.Id,
            Title = b.Title,
            ActivePageIndex = b.ActivePageIndex,
            BackgroundColor = b.BackgroundColor,
            CreatedAt = b.CreatedAt,
            ModifiedAt = b.ModifiedAt,
            LastAccessedAt = b.LastAccessedAt,
            CreatedBy = b.CreatedBy,
            OwnerName = b.OwnerName,
            IsReadOnly = b.IsReadOnly,
            IsStarred = b.IsStarred,
            IsInBin = b.IsInBin,
            BinnedAt = b.BinnedAt,
            IsInfiniteCanvas = b.IsInfiniteCanvas,
            CanvasWidth = b.CanvasWidth,
            CanvasHeight = b.CanvasHeight,
            PresentationUrl = b.PresentationUrl,
            Venue = b.Venue,
            Group = b.Group,
            Labels = b.Labels,
            Pages = b.Pages.Select(ToDomainPage).ToList()
        };

    #endregion
}
