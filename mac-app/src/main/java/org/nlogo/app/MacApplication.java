// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import ca.weblite.objc.Client;
import ca.weblite.objc.Proxy;

import com.apple.eawt.Application;

// for those of you wondering why this song and dance is necessary
// * We must compile most of NetLogo against JRE 1.5/6 (with a specified bootclasspath)
// * The bootclasspath we compile against DOES NOT contain the relevant `com.apple.eawt` classes
// * Changing the JRE we compile against most likely breaks binary compatibility
// * MRJAdapter doesn't work for JRE > 6.
// * This should *DEFINITELY* be removed in future packagings, as it shouldn't be necessary
//   once we're running against the same JRE we use for packaging (JRE 8, btw).
public class MacApplication {

  Application application;

  public static void main(String[] args) {
    MacHandler handler = new MacHandler(Application.getApplication());

    // on Mac OS X 10.5, we have to explicitly ask for the Quartz
    // renderer. perhaps we should eventually switch to the Sun
    // renderer since that's the new default, but for now, the
    // Quartz renderer is what we've long used and tested, so
    // let's stick with it - ST 12/4/07
    // Not sure whether these are necessary, now that we're packaging for
    // OS X 10.7+
    System.setProperty("apple.awt.graphics.UseQuartz", "true");
    System.setProperty("apple.awt.showGrowBox", "true");
    System.setProperty("apple.laf.useScreenMenuBar", "true");

    long userInitiatedAllowingIdleSleep = 0x00FFFFFFL;

    Client c = Client.getInstance();
    Proxy processInfo = c.sendProxy("NSProcessInfo", "processInfo");
    processInfo.sendProxy("beginActivityWithOptions:reason:", userInitiatedAllowingIdleSleep, "Running NetLogo simulation");

    try {
      String mainApplicationClassName = System.getProperty("org.nlogo.mac.appClassName", "org.nlogo.app.App$");
      Class<?> mainAppClass = Class.forName(mainApplicationClassName);
      Field appField = mainAppClass.getDeclaredField("MODULE$");
      Object app = appField.get(mainAppClass);
      Method mainWithHandler =
        mainAppClass.getDeclaredMethod("mainWithAppHandler", String[].class, Object.class);

      mainWithHandler.invoke(app, args, handler);
    } catch (Exception e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
    }
  }
}
