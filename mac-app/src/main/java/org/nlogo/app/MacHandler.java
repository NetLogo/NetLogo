// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app;

import org.nlogo.core.I18N;
import org.nlogo.awt.UserCancelException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.Application;
import com.apple.eawt.AppEvent;
import com.apple.eawt.OpenFilesHandler;
import com.apple.eawt.OpenURIHandler;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;

class MacHandler implements AppHandler {
  Application application;
  FileOutputStream output;
  App app;
  String openMeLater;

  MacHandler(Application application) {
    log("starting mac application");
    this.application = application;
    this.application.setOpenFileHandler(new MyOpenFilesHandler());
    this.application.setOpenURIHandler(new MyOpenURIHandler());
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

  class MyAboutHandler implements AboutHandler {
    @Override
    public void handleAbout(AppEvent.AboutEvent event) {
      app.showAboutWindow();
    }
  }

  class MyQuitHandler implements QuitHandler {
    @Override
    public void handleQuitRequestWith(AppEvent.QuitEvent event, QuitResponse response) {
      try {
        app.fileMenu().quit();
      } catch (UserCancelException e) {
        response.cancelQuit();
      } catch (Exception e) {
        response.performQuit();
      }
    }
  }

  public void init() { }

  public void ready(App app) {
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
        org.nlogo.awt.EventQueue.mustBeEventDispatchThread();
        app.fileMenu().offerSave();
        app.open(path);
      } catch (org.nlogo.awt.UserCancelException ex) {
        org.nlogo.util.Exceptions.ignore(ex);
      } catch (java.io.IOException ex) {
        javax.swing.JOptionPane.showMessageDialog(
          app.frame(), ex.getMessage(),
          I18N.guiJ().get("common.messages.error"), javax.swing.JOptionPane.ERROR_MESSAGE);
      }
    }
  }
}
