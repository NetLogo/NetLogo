// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor;

import javax.swing.Timer;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;


// some of this is cribbed from Sun's TextComponentDemo - ST 7/30/04

// compound edit stuff copied from My World GIS - ER 4/11/08

public strictfp class UndoManager extends javax.swing.undo.UndoManager
    implements java.awt.event.ActionListener, java.awt.event.FocusListener, java.beans.PropertyChangeListener {

  // Edits that happen less than this number of milliseconds
  // apart will be coalesced into a single CompoundEdit for
  // the purposes of undo and redo - ER 4/11/08
  private static final int EDIT_COMPOSITION_THRESHOLD = 200;

  private static UndoAction undoAction = new UndoAction();

  public static UndoAction undoAction() {
    return undoAction;
  }

  private static RedoAction redoAction = new RedoAction();

  public static javax.swing.Action redoAction() {
    return redoAction;
  }

  private static UndoManager currentManager = null;

  public static void setCurrentManager(UndoManager manager) {
    currentManager = manager;
    undoAction.updateUndoState();
    redoAction.updateRedoState();
  }

  private final Timer _timer;
  private CompoundEdit _editInProgress;
  private long _lastEditTime;
  private Object _lastEditSource;
  // Keeping a hard reference to the last event's source could
  // potentially lead to memory leaks, but the _lastEditSource
  // is always set to null in response to action events from the
  // timer, so any leaks that do occur won't last longer than
  // EDIT_COMPOSITION_THRESHOLD milliseconds. - ER 4/11/08

  public UndoManager() {
    _timer = new Timer(EDIT_COMPOSITION_THRESHOLD, this);
    _editInProgress = null;
    _lastEditTime = 0;
    _lastEditSource = null;
  }

  public void watch(javax.swing.text.JTextComponent textComponent) {
    textComponent.getDocument().addUndoableEditListener(this);
    // we watch the document property to ensure we
    // continue to track the document of this component, even if it changes.
    textComponent.addPropertyChangeListener(this);
    textComponent.addFocusListener(this);
  }

  @Override
  public synchronized void undo() throws CannotUndoException {
    closeEditInProgress(true);
    super.undo();
  }

  @Override
  public synchronized void redo() throws CannotRedoException {
    closeEditInProgress(false);
    super.redo();
  }

  @Override
  public void undoableEditHappened(javax.swing.event.UndoableEditEvent e) {
    _timer.stop();
    long currentTime = System.currentTimeMillis();
    Object currentSource = e.getSource();
    if (_editInProgress == null) {
      _editInProgress = new CompoundEdit();
    } else if ((_lastEditSource != currentSource)
        || ((currentTime - _lastEditTime) > EDIT_COMPOSITION_THRESHOLD)) {
      closeEditInProgress(false);
      _editInProgress = new CompoundEdit();
    }
    _editInProgress.addEdit(e.getEdit());
    _lastEditTime = currentTime;
    _lastEditSource = currentSource;
    _timer.restart();
  }

  public void actionPerformed(java.awt.event.ActionEvent evt) {
    if (evt.getSource() == _timer) {
      closeEditInProgress(true);
    }
  }

  private void closeEditInProgress(boolean perform) {
    if (_editInProgress != null) {
      _editInProgress.end();
      if (perform) {
        addEdit(_editInProgress);
      } else {
        _editInProgress.die();
      }
      _editInProgress = null;
      _lastEditTime = 0;
      _lastEditSource = null;
      _timer.stop();
    }
  }

  public void propertyChange(java.beans.PropertyChangeEvent event) {
    if (event.getPropertyName().equals("document")) {
      ((javax.swing.text.Document) event.getOldValue()).removeUndoableEditListener(this);
      ((javax.swing.text.Document) event.getNewValue()).addUndoableEditListener(this);
    }
  }

  public void focusGained(java.awt.event.FocusEvent event) {
    UndoManager.setCurrentManager(this);
  }

  public void focusLost(java.awt.event.FocusEvent event) {
    if (! event.isTemporary()) {
      UndoManager.setCurrentManager(null);
    }
  }

  @Override
  public boolean addEdit(UndoableEdit anEdit) {
    boolean result = super.addEdit(anEdit);
    if (this == currentManager) {
      undoAction.updateUndoState();
      redoAction.updateRedoState();
    }
    return result;
  }

  @Override
  public void discardAllEdits() {
    closeEditInProgress(false);
    super.discardAllEdits();
    if (this == currentManager) {
      undoAction.updateUndoState();
      redoAction.updateRedoState();
    }
  }

  static strictfp class UndoAction extends javax.swing.AbstractAction {

    public UndoAction() {
      super("Undo");
      setEnabled(false);
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
      if(currentManager.canUndo()) {
        currentManager.undo();
        updateUndoState();
        redoAction.updateRedoState();
      }
    }

    protected void updateUndoState() {
      if (currentManager != null && currentManager.canUndo()) {
        setEnabled(true);
        putValue(NAME, currentManager.getUndoPresentationName());
      } else {
        setEnabled(false);
        putValue(NAME, "Undo");
      }
    }
  }

  static strictfp class RedoAction extends javax.swing.AbstractAction {

    public RedoAction() {
      super("Redo");
      setEnabled(false);
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
      currentManager.redo();
      updateRedoState();
      undoAction.updateUndoState();
    }

    protected void updateRedoState() {
      if (currentManager != null && currentManager.canRedo()) {
        setEnabled(true);
        putValue(NAME, currentManager.getRedoPresentationName());
      } else {
        setEnabled(false);
        putValue(NAME, "Redo");
      }
    }
  }

}
