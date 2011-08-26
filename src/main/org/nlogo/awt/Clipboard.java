package org.nlogo.awt;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

public final strictfp class Clipboard {

  // this class is not instantiable
  private Clipboard() { throw new IllegalStateException(); }

  /// clipboard

  public static String getClipboardAsString(Object requester) {
    Toolkit kit = Toolkit.getDefaultToolkit();
    java.awt.datatransfer.Clipboard clipboard = kit.getSystemClipboard();
    Transferable transferable = clipboard.getContents(requester);
    if (transferable == null) {
      return null;
    }
    try {
      return (String) transferable.getTransferData(DataFlavor.stringFlavor);
    } catch (java.io.IOException ex) {
      // ignore exception
      return null;
    } catch (UnsupportedFlavorException ex) {
      // ignore exception
      return null;
    }
  }

}
