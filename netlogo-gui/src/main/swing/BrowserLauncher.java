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
    try {
      if (local) {
        urlString = makeURLFromFile(new java.io.File(urlString));
      }
      openURL(urlString + anchor);
    }
    // this happens on Linux if the user doesn't have something called
    // "mozilla" in their path - ST 11/8/07
    catch (BrowserLauncher.BrowserNotFoundException ex) {
      javax.swing.JOptionPane.showMessageDialog
          (comp, ex.getLocalizedMessage());
    } catch (java.io.IOException ex) {
      throw new IllegalStateException(ex);
    }
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

  private static void openURL(String url) throws BrowserNotFoundException, IOException {
    String osName = System.getProperty("os.name");
    // first see if we can call Java 6's Desktop.browse() method
    try {
      Class<?> desktopClass = Class.forName("java.awt.Desktop");
      Object result = desktopClass.getMethod("isDesktopSupported").invoke(null);
      if (result instanceof Boolean && ((Boolean) result).booleanValue()) {
        Object desktop = desktopClass.getMethod("getDesktop").invoke(null);
        desktopClass.getMethod("browse", java.net.URI.class).invoke(desktop, new java.net.URI(url));
        return;
      }
    } catch (Exception e) { } // ignore NOPMD
    // fall back on stuff that works on Java 5
    if (osName.startsWith("Windows")) {
      Runtime.getRuntime().exec(
          new String[]{"cmd.exe", "/c", "start", "\"\"", '"' + url + '"'});
    } else if (osName.startsWith("Mac")) {
      throw new BrowserNotFoundException(
          "We were unable to open a browser on your system.\n" +
          "This error can be reported to ccl-bugs@ccl.northwestern.edu");
    } else {
      try {
        Process process = Runtime.getRuntime().exec(new String[]{
          "firefox", "-remote", "'openURL(", url + ")'"});
        int exitCode = process.waitFor();
        if (exitCode != 0) {  // if Firefox was not open
          Runtime.getRuntime().exec(new String[]{"firefox", url});
        }
      } catch (InterruptedException ie) {
        throw new IllegalStateException(ie);
      } catch (IOException ex) {
        throw new BrowserNotFoundException(
            "NetLogo could not find and execute a web browser named \'firefox\'." +
            "Please install Firefox and ensure that the \'firefox\' command " +
            "is in your executable PATH.  Firefox is available here:\n " +
            "http://www.mozilla.com/firefox/\n\n" +
            "The full error message was:\n " + ex.getLocalizedMessage()
            );
      }
    }
  }

  static class BrowserNotFoundException extends Exception {
    BrowserNotFoundException(String message) {
      super(message);
    }
  }
}
