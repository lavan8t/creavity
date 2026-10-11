using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using CreaVITy.Windows.Domain.Models;

namespace CreaVITy.Windows.Domain.Mutations;

public class MutationHistory
{
    private readonly int _maxStackSize;
    private readonly List<BoardMutation> _undoStack = new();
    private readonly List<BoardMutation> _redoStack = new();
    private readonly object _lock = new();

    public MutationHistory(int maxStackSize = 100)
    {
        _maxStackSize = maxStackSize;
    }

    public bool CanUndo
    {
        get { lock (_lock) return _undoStack.Count > 0; }
    }

    public bool CanRedo
    {
        get { lock (_lock) return _redoStack.Count > 0; }
    }

    public void Record(BoardMutation mutation)
    {
        lock (_lock)
        {
            if (_undoStack.Count >= _maxStackSize)
            {
                _undoStack.RemoveAt(0);
            }
            _undoStack.Add(mutation);
            _redoStack.Clear();
        }
    }

    public (BoardPage Page, BoardMutation? Mutation) Undo(BoardPage currentPage)
    {
        lock (_lock)
        {
            if (_undoStack.Count == 0) return (currentPage, null);
            var mutation = _undoStack[^1];
            _undoStack.RemoveAt(_undoStack.Count - 1);
            _redoStack.Add(mutation);
            var reverted = mutation.Revert(currentPage);
            return (reverted, mutation);
        }
    }

    public (BoardPage Page, BoardMutation? Mutation) Redo(BoardPage currentPage)
    {
        lock (_lock)
        {
            if (_redoStack.Count == 0) return (currentPage, null);
            var mutation = _redoStack[^1];
            _redoStack.RemoveAt(_redoStack.Count - 1);
            _undoStack.Add(mutation);
            var applied = mutation.Apply(currentPage);
            return (applied, mutation);
        }
    }

    public void Clear()
    {
        lock (_lock)
        {
            _undoStack.Clear();
            _redoStack.Clear();
        }
    }
}
