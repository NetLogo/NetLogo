// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app;

import java.awt.Desktop;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import ca.weblite.objc.Client;
import ca.weblite.objc.Proxy;

// for those of you wondering why this song and dance is necessary
// * We must compile most of NetLogo against JRE 1.5/6 (with a specified bootclasspath)
// * The bootclasspath we compile against DOES NOT contain the relevant `com.apple.eawt` classes
// * Changing the JRE we compile against most likely breaks binary compatibility
// * MRJAdapter doesn't work for JRE > 6.
// * This should *DEFINITELY* be removed in future packagings, as it shouldn't be necessary
//   once we're running against the same JRE we use for packaging (JRE 8, btw).
public class MacApplication {

  Desktop application;

  public static void main(String[] args) {
    MacHandler handler = new MacHandler();

    System.setProperty("apple.awt.graphics.UseQuartz", "false");
    System.setProperty("apple.awt.showGrowBox", "true");
    System.setProperty("apple.laf.useScreenMenuBar", "true");

    long userInitiatedAllowingIdleSleep = 0x00FFFFFFL;

    Client c = Client.getInstance();
    Proxy processInfo = c.sendProxy("NSProcessInfo", "processInfo");
    processInfo.sendProxy("beginActivityWithOptions:reason:", userInitiatedAllowingIdleSleep, "Running NetLogo simulation");

    try {
      String mainApplicationClassName = System.getProperty("org.nlogo.mac.appClassName", "org.nlogo.app.App$");
      Class<?> mainAppClass = Class.forName(mainApplicationClassName);
      try {
        launchScalaMain(mainAppClass, args, handler);
      } catch (NoSuchFieldException e) {
        launchJavaMain(mainAppClass, args, handler);
      }
    } catch (Exception e) {
      printException(e);
    }
  }

  static void printException(Exception e) {
    System.err.println(e.getMessage());
    e.printStackTrace();
  }

  static void launchScalaMain(Class<?> klass, String[] args, MacHandler handler) throws IllegalAccessException, NoSuchFieldException {
    Field appField = klass.getDeclaredField("MODULE$");
    Object app = appField.get(klass);
    invokeMainWithAppHandlerOrFallback(klass, app, args, handler);
  }

  static void launchJavaMain(Class<?> klass, String[] args, MacHandler handler) throws IllegalAccessException {
    invokeMainWithAppHandlerOrFallback(klass, null, args, handler);
  }

  static void invokeMainWithAppHandlerOrFallback(Class<?> klass, Object target, String[] args, MacHandler handler) throws IllegalAccessException {
    try {
      Method mainWithHandler = klass.getDeclaredMethod("mainWithAppHandler", String[].class, Object.class);
      mainWithHandler.invoke(target, args, handler);
    } catch (NoSuchMethodException e) {
      System.err.println("Could not find mainWithAppHandler method on class " + klass.getName());
      printException(e);
      System.err.println("attempting to invoke main...");
      invokeMain(klass, target, args);
    } catch (InvocationTargetException e) {
      System.err.println("Could not invoke mainWithAppHandler on " + klass.getName());
      printException(e);
    }
  }

  static void invokeMain(Class<?> klass, Object target, String[] args) throws IllegalAccessException {
    try {
      Method mainWithHandler = klass.getDeclaredMethod("main", String[].class);
      mainWithHandler.invoke(target, (Object) args);
    } catch (NoSuchMethodException e) {
      System.err.println("Could not find main method on class " + klass.getName());
      printException(e);
    } catch (InvocationTargetException e) {
      System.err.println("Could not invoke main on " + klass.getName());
      printException(e);
    }
  }

}
