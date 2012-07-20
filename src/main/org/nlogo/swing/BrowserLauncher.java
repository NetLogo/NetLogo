// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing;

import java.io.IOException;

public strictfp class BrowserLauncher {
  private BrowserLauncher() {
    throw new IllegalStateException();
  } // not instantiable

  public static void openURL(java.awt.Component comp, String urlString, boolean local) {
    openURL(comp, urlString, "", local);
  }

  public static void openURL(java.awt.Component comp, String urlString, String anchor, boolean local) {
    if (local) {
      urlString = makeURLFromFile(new java.io.File(urlString));
    }
    openURL(urlString + anchor);
  }

  private static String makeURLFromFile(java.io.File file) {
    return makeURLFromFilePath(file.getAbsolutePath());
  }

  private static String makeURLFromFilePath(String filePath) {
    try {
      return new java.net.URI("file", filePath, null).toURL().toString();
    } catch (java.net.URISyntaxException ex) {
      throw new IllegalStateException(ex);
    } catch (java.net.MalformedURLException ex) {
      throw new IllegalStateException(ex);
    }
  }

  private static void openURL(String url) {
    try {
      java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
    }
    catch(java.net.URISyntaxException ex) {
      throw new IllegalStateException(ex);
    }
    catch(IOException ex) {
      throw new IllegalStateException(ex);
    }
  }

}
