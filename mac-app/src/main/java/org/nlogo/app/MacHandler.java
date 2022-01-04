// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.Application;
import com.apple.eawt.AppEvent;
import com.apple.eawt.OpenFilesHandler;
import com.apple.eawt.OpenURIHandler;
import com.apple.eawt.PreferencesHandler;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;

public class MacHandler {
  Application application;
  FileOutputStream output;
  Object app;
  String openMeLater;

  MacHandler(Application application) {
    log("starting mac application");
    this.application = application;
    this.application.setOpenFileHandler(new MyOpenFilesHandler());
    this.application.setOpenURIHandler(new MyOpenURIHandler());
    this.application.setPreferencesHandler(new MyPreferencesHandler());
    this.application.setAboutHandler(new MyAboutHandler());
    this.application.setQuitHandler(new MyQuitHandler());
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
    public void openURI(AppEvent.OpenURIEvent event) {
      log("received OpenURI event");
      log(event.getURI().toString());
    }
  }

  class MyOpenFilesHandler implements OpenFilesHandler {
    @Override
    public void openFiles(AppEvent.OpenFilesEvent event) {
      log("received OpenFiles event");
      log(event.getFiles().get(0).getAbsolutePath());
      doOpen(event.getFiles().get(0).getAbsolutePath());
    }
  }

  class MyPreferencesHandler implements PreferencesHandler {
    @Override
    public void handlePreferences(AppEvent.PreferencesEvent event) {
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
    public void handleAbout(AppEvent.AboutEvent event) {
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
    public void handleQuitRequestWith(AppEvent.QuitEvent event, QuitResponse response) {
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

  public void init() { }

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
