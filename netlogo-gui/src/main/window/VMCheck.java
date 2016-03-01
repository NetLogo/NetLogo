// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import org.nlogo.core.I18N;

public strictfp final class VMCheck {

  // not instantiable
  private VMCheck() {
    throw new IllegalStateException();
  }

  // Our class files are in Java 1.5 format, so there's no need in
  // this code to detect and reject older Java versions. Ideally the
  // app would have some Java 1.1 compatible startup code that would
  // check the Java version and then hand off to the real code.  We
  // have such code in the applet.  Doing it for the app doesn't
  // seem worth the effort given that we bundle Java 1.5 with the
  // app on Windows, and given that Apple bundles Java 1.5 with Mac
  // OS X.  (At least I think it was bundled with 10.4.0... even if
  // not, surely nearly every Mac user will have gotten it through
  // Software Update...)  - ST 2/25/08

  // It might also be nice to add some checks here for Mac users who
  // are not up to date with Java in Software Update, advising them
  // they should be.  At one point we had a similar check for Java 1.4,
  // but that was because we knew of some specific, crippling bugs.
  // We don't know about any of those in any of Apple's Java 1.5 releases,
  // so it seems OK to let it slide. - ST 2/25/08

  public static void detectBadJVMs() {
    if (org.nlogo.util.SysInfo.isLibgcj()) {
      warn("You have started NetLogo under the GNU libgcj Java VM. " +
          "NetLogo may not run well, or at all, under libgcj. " +
          "We recommend using the Oracle Java VM to" +
          "run NetLogo. Recent OpenJDK versions may also work.");
    }
  }

  private static void warn(String message) {
    java.awt.Frame bogusFrame = new java.awt.Frame();
    bogusFrame.pack(); // otherwise OptionDialog will fail to get font metrics
    int choice = org.nlogo.swing.OptionDialog.show
        (bogusFrame, I18N.guiJ().get("common.messages.warning"),
            message + "  If you choose to continue, NetLogo may not " +
                "function properly.",
            new String[]{I18N.guiJ().get("common.buttons.quit"), I18N.guiJ().get("common.buttons.continue")});
    if (choice == 0) {
      System.exit(0);
    }
  }

}
