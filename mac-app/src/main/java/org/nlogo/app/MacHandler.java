// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app;

import java.awt.Desktop;
import java.awt.desktop.AboutEvent;
import java.awt.desktop.AboutHandler;
import java.awt.desktop.OpenFilesEvent;
import java.awt.desktop.OpenFilesHandler;
import java.awt.desktop.OpenURIEvent;
import java.awt.desktop.OpenURIHandler;
import java.awt.desktop.PreferencesEvent;
import java.awt.desktop.PreferencesHandler;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitHandler;
import java.awt.desktop.QuitResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.nlogo.util.AppHandler;

public class MacHandler extends AppHandler {
  Desktop application;
  FileOutputStream output;
  Object app;
  String openMeLater;

  MacHandler() {
    log("starting mac application");
    this.application = Desktop.getDesktop();
    application.setOpenFileHandler(new MyOpenFilesHandler());
    application.setOpenURIHandler(new MyOpenURIHandler());
    application.setPreferencesHandler(new MyPreferencesHandler());
    application.setAboutHandler(new MyAboutHandler());
    application.setQuitHandler(new MyQuitHandler());
    log("in directory:");
    log(System.getProperty("user.dir"));
    log(new File(".").getAbsolutePath());
  }

  void log(String s) {
    try {
      if (output == null) {
        this.output = new FileOutputStream("/tmp/nlogoapp.log");
      }
      output.write((s + "\n").getBytes());
      output.flush();
    } catch (FileNotFoundException ex) {
    } catch (IOException ex) {
    }
  }

  class MyOpenURIHandler implements OpenURIHandler {
    @Override
    public void openURI(OpenURIEvent event) {
      log("received OpenURI event");
      log(event.getURI().toString());
    }
  }

  class MyOpenFilesHandler implements OpenFilesHandler {
    @Override
    public void openFiles(OpenFilesEvent event) {
      log("received OpenFiles event");
      log(event.getFiles().get(0).getAbsolutePath());
      doOpen(event.getFiles().get(0).getAbsolutePath());
    }
  }

  class MyPreferencesHandler implements PreferencesHandler {
    @Override
    public void handlePreferences(PreferencesEvent event) {
      try {
        Class<?> appClass = app.getClass();
        appClass.getDeclaredMethod("handleShowPreferences").invoke(app);
      } catch (NoSuchMethodException e) {
      } catch (IllegalAccessException e) {
      } catch (InvocationTargetException e) {
        log("failed to invoke handleShowPreferences");
      }
    }
  }

  class MyAboutHandler implements AboutHandler {
    @Override
    public void handleAbout(AboutEvent event) {
      try {
        Class<?> appClass = app.getClass();
        appClass.getDeclaredMethod("handleShowAbout").invoke(app);
      } catch (NoSuchMethodException e) {
      } catch (IllegalAccessException e) {
      } catch (InvocationTargetException e) {
        log("failed to invoke handleShowAbout");
      }
    }
  }

  class MyQuitHandler implements QuitHandler {
    @Override
    public void handleQuitRequestWith(QuitEvent event, QuitResponse response) {
      try {
        Class<?> appClass = app.getClass();
        Method handleQuit = appClass.getDeclaredMethod("handleQuit");
        handleQuit.invoke(app);
      } catch (NoSuchMethodException e) {
        response.performQuit();
      } catch (InvocationTargetException e) {
        if (e.getCause().getClass().getName().contains("UserCancelException")) {
          log("cancelled quit");
          response.cancelQuit();
        } else {
          log("invocation of handleQuit failed");
          e.printStackTrace();
          log(e.getMessage());
          response.performQuit();
        }
      } catch (Exception e) {
        response.performQuit();
      }
    }
  }

  @Override
  public void init() { }

  @Override
  public void ready(Object app) {
    this.app = app;
    if (openMeLater != null) {
      doOpen(openMeLater);
    }
  }

  void doOpen(String path) {
    if (app == null) {
      openMeLater = path;
    } else {
      try {
        Class<?> appClass = app.getClass();
        appClass.getDeclaredMethod("handleOpenPath", String.class).invoke(app, path);
      } catch (NoSuchMethodException e) {
      } catch (IllegalAccessException e) {
      } catch (InvocationTargetException e) {
        log("failed to invoke handleOpenPath");
      }
    }
  }
}
