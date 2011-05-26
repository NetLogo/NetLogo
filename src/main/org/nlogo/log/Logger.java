package org.nlogo.log;

// A note on log4j: Reuven, who was one of the main clients for this
// logging stuff, requested log4j.  We did not investigate log4j
// vs. java.util.logging but just went along with Reuven's
// suggestion. - ST 2/25/08

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.xml.DOMConfigurator;

import org.nlogo.api.CompilerException;

public strictfp class Logger
    implements org.nlogo.api.NetLogoListener {

  public final static org.apache.log4j.Logger BUTTONS =
      org.apache.log4j.Logger.getLogger(Logger.class.getName() + ".BUTTONS");
  public final static org.apache.log4j.Logger GREENS =
      org.apache.log4j.Logger.getLogger(Logger.class.getName() + ".GREENS");
  public final static org.apache.log4j.Logger CODE =
      org.apache.log4j.Logger.getLogger(Logger.class.getName() + ".CODE");
  public final static org.apache.log4j.Logger WIDGETS =
      org.apache.log4j.Logger.getLogger(Logger.class.getName() + ".WIDGETS");
  public static final org.apache.log4j.Logger GLOBALS =
      org.apache.log4j.Logger.getLogger(Logger.class.getName() + ".GLOBALS");
  public static final org.apache.log4j.Logger SPEED =
      org.apache.log4j.Logger.getLogger(Logger.class.getName() + ".SPEED");
  public static final org.apache.log4j.Logger TURTLES =
      org.apache.log4j.Logger.getLogger(Logger.class.getName() + ".TURTLES");
  public static final org.apache.log4j.Logger LINKS =
      org.apache.log4j.Logger.getLogger(Logger.class.getName() + ".LINKS");

  private final String studentName;
  String logDirectory;

  public Logger(String name) {
    this.studentName = name;
    logDirectory = System.getProperty("java.io.tmpdir");
  }

  public void configure(java.io.Reader reader) {
    DOMConfigurator configurator = new DOMConfigurator();
    configurator.doConfigure(reader, org.apache.log4j.LogManager.getLoggerRepository());
  }

  public void changeLogDirectory(String path) {
    java.io.File directory = new java.io.File(path);

    if (!directory.isAbsolute()) {
      try {
        String newPath =
            System.getProperty("user.home")
                + java.io.File.separatorChar + "dummy.txt";
        java.net.URL urlForm =
            new java.net.URL
                (toURL(new java.io.File(newPath)), path);

        directory = new java.io.File(urlForm.getFile());
        path = directory.getAbsolutePath();
      } catch (java.net.MalformedURLException e) {
        return;
      }
    }
    if (!directory.isDirectory()) {
      return;
    }
    logDirectory = path;
  }

  List<String> filenames = null; // for TestLogger

  public void modelOpened(String name) {
    filenames = new ArrayList<String>();
    filenames.addAll(newFiles(org.apache.log4j.Logger.getRootLogger().getAllAppenders(), name));
    Enumeration<?> loggers =
        org.apache.log4j.Logger.getRootLogger().getLoggerRepository().getCurrentLoggers();
    while (loggers.hasMoreElements()) {
      org.apache.log4j.Logger l = (org.apache.log4j.Logger) loggers.nextElement();
      filenames.addAll(newFiles(l.getAllAppenders(), name));
    }
  }

  public List<String> newFiles(Enumeration<?> e, String name) {
    List<String> filenames = new ArrayList<String>();
    while (e.hasMoreElements()) {
      Object obj = e.nextElement();
      if (obj instanceof FileAppender) {
        FileAppender appender = (FileAppender) obj;
        String filename = logFileName(appender.getName());
        filenames.add(filename);
        appender.setFile(filename);
        if (appender instanceof XMLFileAppender) {
          XMLFileAppender xappender = (XMLFileAppender) appender;
          xappender.setStudentName(studentName);
          xappender.setUsername(System.getProperty("user.name"));
          xappender.setIPAddress(getIPAddress());
          xappender.setModelName(name == null ? "new model" : name);
          xappender.setVersion(org.nlogo.api.Version.version());
        }
        appender.activateOptions();
      }
    }
    return filenames;
  }

  public void close() {
    closeFiles(org.apache.log4j.Logger.getRootLogger().getAllAppenders());
    Enumeration<?> loggers =
        org.apache.log4j.Logger.getRootLogger().getLoggerRepository().getCurrentLoggers();
    while (loggers.hasMoreElements()) {
      org.apache.log4j.Logger l = (org.apache.log4j.Logger) loggers.nextElement();
      closeFiles(l.getAllAppenders());
    }
  }

  private void closeFiles(Enumeration<?> e) {
    while (e.hasMoreElements()) {
      ((Appender) e.nextElement()).close();
    }
  }

  private static LogMessage widgetMsg = LogMessage.createWidgetMessage();

  public static void logAddWidget(String type, String name) {
    widgetMsg.updateWidgetMessage(type.toLowerCase(), name, "added");
    WIDGETS.info(widgetMsg);
  }

  public static void logWidgetRemoved(String type, String name) {
    widgetMsg.updateWidgetMessage(type.toLowerCase(), name, "removed");
    WIDGETS.info(widgetMsg);
  }

  private static LogMessage speedMsg = LogMessage.createSpeedMessage();

  public static void logSpeedSlider(double value) {
    speedMsg.updateSpeedMessage(Double.toString(value));
    SPEED.info(speedMsg);
  }

  private static LogMessage tickMsg = LogMessage.createGlobalMessage("ticks");

  public void tickCounterChanged(double ticks) {
    tickMsg.updateGlobalMessage("ticks", Double.toString(ticks));
    GLOBALS.info(tickMsg);
  }

  // logging this would be excessive - ST 11/11/10
  public void possibleViewUpdate() {
  }

  private static LogMessage mortalityMsg = LogMessage.createAgentMessage();

  public static void logTurtleBirth(String name, String breed) {
    mortalityMsg.updateAgentMessage("turtle", name, "born", breed);
    TURTLES.info(mortalityMsg);
  }

  public static void logTurtleDeath(String name, String breed) {
    mortalityMsg.updateAgentMessage("turtle", name, "died", breed);
    TURTLES.info(mortalityMsg);
  }

  private static LogMessage buttonMsg = LogMessage.createButtonMessage();

  public static void logButtonStopped(String name, boolean onceButton, boolean stopping) {
    if (BUTTONS.isInfoEnabled()) {
      buttonMsg.updateButtonMessage
          (name, "released", (onceButton ? "once" : (stopping ? "user" : "code")));
      BUTTONS.info(buttonMsg);
    }
  }

  public static void logButtonPressed(String name) {
    buttonMsg.updateButtonMessage(name, "pressed", "user");
    BUTTONS.info(buttonMsg);
  }

  public void buttonPressed(String buttonName) {
  }

  public void buttonStopped(String buttonName) {
  }

  private static LogMessage sliderMsg = LogMessage.createSliderMessage();

  public void sliderChanged(String name, double value, double min,
                            double increment, double max, boolean valueChanged,
                            boolean buttonReleased) {
    if (valueChanged) {
      sliderMsg.updateSliderMessage(name.toUpperCase(), value, min, max, increment);
      if (buttonReleased) {
        GREENS.info(sliderMsg);
      } else {
        GREENS.debug(sliderMsg);
      }
    }
  }

  private static LogMessage switchMsg = LogMessage.createGlobalMessage("switch");

  public void switchChanged(String name, boolean value, boolean valueChanged) {
    if (valueChanged) {
      switchMsg.updateGlobalMessage(name.toUpperCase(), Boolean.toString(value));
      GREENS.info(switchMsg);
    }
  }

  private static LogMessage chooserMsg = LogMessage.createGlobalMessage("chooser");

  public void chooserChanged(String name, Object value, boolean valueChanged) {
    if (valueChanged) {
      chooserMsg.updateGlobalMessage(name.toUpperCase(), value.toString());
      GREENS.info(chooserMsg);
    }
  }

  private static LogMessage inputBoxMsg = LogMessage.createGlobalMessage("input box");

  public void inputBoxChanged(String name, Object value, boolean valueChanged) {
    if (valueChanged) {
      inputBoxMsg.updateGlobalMessage(name.toUpperCase(), value.toString());
      GREENS.info(inputBoxMsg);
    }
  }

  private static LogMessage commandMsg = LogMessage.createCommandMessage();

  public void commandEntered(String owner, String text, char agentType, CompilerException error) {
    if (error == null) {
      commandMsg.updateCommandMessage
          (owner.toLowerCase(), "compiled", text, Character.toString(agentType), "success", 0, 0);
    } else {
      commandMsg.updateCommandMessage
          (owner.toLowerCase(), "compiled", text, Character.toString(agentType),
              error.getMessage(), error.startPos(), error.endPos());
    }
    if (!owner.startsWith("Slider")) {
      CODE.info(commandMsg);
    }
  }

  private static LogMessage codeTabMsg = LogMessage.createCodeTabMessage();

  public void codeTabCompiled(String text, CompilerException error) {
    codeTabMsg.updateCodeTabMessage("compiled",
                                    text,
                                    error == null ? "success" : error.getMessage(),
                                    error == null ? 0 : error.startPos(),
                                    error == null ? 0 : error.endPos());
    CODE.info(codeTabMsg);
  }

  private static LogMessage globalMsg = LogMessage.createGlobalMessage("globals");

  public static void logGlobal(String name, Object value, boolean changed) {
    globalMsg.updateGlobalMessage(name, value.toString());
    if (changed) {
      GLOBALS.info(globalMsg);
    } else {
      GLOBALS.debug(globalMsg);
    }
  }

  public String getIPAddress() {
    try {
      return java.net.InetAddress.getLocalHost().getHostAddress();
    } catch (java.net.UnknownHostException e) {
      return "unknown";
    }
  }

  private final java.text.DateFormat dateFormat =
      new java.text.SimpleDateFormat("yyyy-MM-dd.HH_mm_ss_SS", java.util.Locale.US);

  private String logFileName(String appender) {
    return logDirectory
        + System.getProperty("file.separator") + "logfile_" + appender + "_"
        + dateFormat.format(new java.util.Date()) + ".xml";
  }

  public void deleteSessionFiles() {
    deleteSessionFiles(logDirectory);
  }

  public static void deleteSessionFiles(String path) {
    java.io.File directory = new java.io.File(path);
    if (!directory.isDirectory()) {
      return;
    }

    String[] files = directory.list
        (new java.io.FilenameFilter() {
          public boolean accept(java.io.File dir, String name) {
            return name.startsWith("logfile_") && name.endsWith(".xml");
          }
        });

    for (int i = 0; i < files.length; i++) {
      java.io.File file = new java.io.File
          (path + System.getProperty("file.separator") + files[i]);
      file.delete();
    }
  }

  public void zipSessionFiles(String filename)
      throws java.io.IOException {
    zipSessionFiles(logDirectory, filename);
  }

  public String[] getFileList() {
    java.io.File directory = new java.io.File(logDirectory);
    return directory.list
        (new java.io.FilenameFilter() {
          public boolean accept(java.io.File dir, String name) {
            return name.startsWith("logfile_") && name.endsWith(".xml");
          }
        });
  }

  public static void zipSessionFiles(String path, String filename)
      throws java.io.IOException {
    java.io.File directory = new java.io.File(path);
    if (!directory.isDirectory()) {
      return;
    }

    String[] files = directory.list
        (new java.io.FilenameFilter() {
          public boolean accept(java.io.File dir, String name) {
            return name.startsWith("logfile_") && name.endsWith(".xml");
          }
        });

    if (files.length > 0) {
      java.util.zip.ZipOutputStream out =
          new java.util.zip.ZipOutputStream(new java.io.FileOutputStream(filename));
      for (int i = 0; i < files.length; i++) {
        try {
          String file = path + System.getProperty("file.separator") + files[i];
          byte[] data = org.nlogo.api.FileIO.file2String(file).getBytes();
          out.putNextEntry(new java.util.zip.ZipEntry(files[i]));
          out.write(data, 0, data.length);
          out.closeEntry();
        } catch (java.io.IOException e) {
          // this probably shouldn't ever happen but in case
          // it does just skip the file and move on. ev 3/14/07
          org.nlogo.util.Exceptions.ignore(e);
        }
      }
      out.close();
    }
  }

  public static void beQuiet() {
    org.apache.log4j.helpers.LogLog.setQuietMode(true);
  }

  public static void configure(String properties) {
    DOMConfigurator.configure(properties);
  }

  // for 4.1 we have too much fragile, difficult-to-understand,
  // under-tested code involving URLs -- we can't get rid of our
  // uses of toURL() until 4.2, the risk of breakage is too high.
  // so for now, at least we make this a separate method so the
  // SuppressWarnings annotation is narrowly targeted. - ST 12/7/09
  @SuppressWarnings("deprecation")
  private static java.net.URL toURL(java.io.File file)
      throws java.net.MalformedURLException {
    return file.toURL();
  }

}
