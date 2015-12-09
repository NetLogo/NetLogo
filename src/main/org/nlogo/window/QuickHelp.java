// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final strictfp class QuickHelp<TokenType> {

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
    String[] lines = getResourceAsStringArray(path);
    HashMap<String, String> words =
        new HashMap<String, String>();
    for (int i = 0; i < lines.length; i++) {
      String anchor = lines[i].substring(0, lines[i].indexOf(' '));
      String file = lines[i].substring(lines[i].indexOf(' ') + 1);
      words.put(anchor, file);
    }
    return words;
  }

  private static String docPath(String docName) {
    return System.getProperty("netlogo.docs.dir", "docs") + "/" + docName;
  }

  private static void openDictionary(java.awt.Component comp, String theWord,
                                     Map<String, String> words) {
    String theFile = words.get(theWord);
    org.nlogo.swing.BrowserLauncher.openURL
        (comp, docPath("dict/" + theFile), true);
  }

  public static void doHelp(java.awt.Component comp, String token) {
    if (!quickHelpLoaded) {
      quickHelpWords = loadHelp(QUICKHELPWORDS_PATH);
      // if we're not in 3D don't load the 3d dictionary words
      // cause we don't need 'em and they'll override the 2d
      // dictionary ev 10/25/07
      if (org.nlogo.api.Version.is3D()) {
        quickHelpWords3d = loadHelp(QUICKHELPWORDS_PATH3D);
      } else {
        quickHelpWords3d = new HashMap<String, String>();
      }
      quickHelpLoaded = true;
    }
    if (token == null) {
      return;
    }
    token = token.toLowerCase();
    // if there is an entry in the 3D dictionary then it overrides
    // the 2D entry ev 10/25/07
    if (quickHelpWords3d.containsKey(token)) {
      openDictionary(comp, token, quickHelpWords3d);
    } else if (quickHelpWords.containsKey(token)) {
      openDictionary(comp, token, quickHelpWords);
    } else {
      if (0 == javax.swing.JOptionPane.showConfirmDialog
          (comp,
              token.toUpperCase() + " could not be found in the NetLogo Dictionary.\n" +
                  "Would you like to open the full NetLogo Dictionary?",
              "NetLogo", javax.swing.JOptionPane.YES_NO_OPTION)) {
        org.nlogo.swing.BrowserLauncher.openURL
            (comp, docPath("index2.html"), true);
      }
    }
  }

  /// copy-n-pasted from org.nlogo.util.Utils

  public static String[] getResourceAsStringArray(String path) {
    try {
      List<String> result = new ArrayList<String>();
      java.io.InputStream stream = QuickHelp.class.getResourceAsStream(path);
      java.io.BufferedReader in =
          new java.io.BufferedReader(new java.io.InputStreamReader(stream));
      while (true) {
        String line = in.readLine();
        if (line == null) {
          break;
        }
        result.add(line);
      }
      return result.toArray(new String[]{});
    } catch (java.io.IOException ex) {
      throw new IllegalStateException(ex);
    }
  }

}
