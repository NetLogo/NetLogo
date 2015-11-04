// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app;

import org.nlogo.api.I18N;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

import com.apple.eawt.Application;
import com.apple.eawt.AppEvent;
import com.apple.eawt.OpenFilesHandler;
import com.apple.eawt.OpenURIHandler;

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
    log("in directory:");
    log(System.getProperty("user.dir"));
    log(new File(".").getAbsolutePath());
    // the following were used with MRJAdapter and may or may not be needed
    /*
    MRJAdapter.addAboutListener(() => app.showAboutWindow())
    MRJAdapter.addQuitApplicationListener{() =>
      try app.fileMenu.quit()
      catch { case e: UserCancelException => } // ignore
    }
    */
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
    MyOpenFilesHandler() {}

    @Override
    public void openFiles(AppEvent.OpenFilesEvent event) {
      log("received OpenFiles event");
      log(event.getFiles().get(0).getAbsolutePath());
      doOpen(event.getFiles().get(0).getAbsolutePath());
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
