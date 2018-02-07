// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import org.nlogo.core.I18N;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.file.Path;

final strictfp class QuickHelp {

  // not instantiable
  private QuickHelp() {
    throw new IllegalStateException();
  }

  /**
   * This action is to provide context-sensitive help in the procedures
   * tab.  Based on what their cursor is on, it should jump the user
   * to the proper documentation, found in docs/dict/
   * -- which is just a decomposed version of docs/dictionary.html
   * <p/>
   * Maybe it would be best to pull everything out of some new file,
   * maybe xml format, and we could autogenerate the primitives dictionary file
   * from that.
   * <p/>
   * ~Forrest (4/26/2007)
   */
  private static boolean quickHelpLoaded = false;
  private static final String QUICKHELPWORDS_PATH = "/system/dict.txt";
  private static final String QUICKHELPWORDS_PATH3D = "/system/dict3d.txt";
  private static Map<String, String> quickHelpWords;
  private static Map<String, String> quickHelpWords3d;

  private static Map<String, String> loadHelp(String path) {
    String[] lines = org.nlogo.api.FileIO$.MODULE$.getResourceAsStringArray(path);
    HashMap<String, String> words =
        new HashMap<String, String>();
    for (int i = 0; i < lines.length; i++) {
      String anchor = lines[i].substring(0, lines[i].indexOf(' '));
      String file = lines[i].substring(lines[i].indexOf(' ') + 1);
      words.put(anchor, file);
    }
    return words;
  }

  private static Path docPath(String docName) {
    return org.nlogo.swing.BrowserLauncher.docPath(docName);
  }

  private static void openDictionary(java.awt.Component comp, String theWord,
                                     Map<String, String> words) {
    String theFile = words.get(theWord);
    org.nlogo.swing.BrowserLauncher.openPath(comp, docPath("dict/" + theFile), null);
  }

  public static void doHelp(java.awt.Component comp, String token, boolean is3D) {
    if (!quickHelpLoaded) {
      quickHelpWords = loadHelp(QUICKHELPWORDS_PATH);
      quickHelpWords3d = loadHelp(QUICKHELPWORDS_PATH3D);
      quickHelpLoaded = true;
    }
    if (token == null) {
      return;
    }
    token = token.toLowerCase();
    // if there is an entry in the 3D dictionary then it overrides
    // the 2D entry ev 10/25/07
    if (is3D && quickHelpWords3d.containsKey(token)) {
      openDictionary(comp, token, quickHelpWords3d);
    } else if (quickHelpWords.containsKey(token)) {
      openDictionary(comp, token, quickHelpWords);
    } else {
      if (0 == javax.swing.JOptionPane.showConfirmDialog
          (comp, I18N.guiJ().getN("tabs.code.rightclick.quickhelp.notfound", token.toUpperCase()),
           I18N.guiJ().get("common.netlogo"), javax.swing.JOptionPane.YES_NO_OPTION)) {
        org.nlogo.swing.BrowserLauncher.openPath(comp, docPath("index2.html"), null);
      }
    }
  }
}
