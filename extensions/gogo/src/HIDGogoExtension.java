package gogohid.extension;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultClassManager;
import org.nlogo.api.Command;
import org.nlogo.api.Reporter;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.core.LogoList;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.api.PrimitiveManager;
import org.nlogo.api.ExtensionManager;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;

import org.nlogo.swing.BrowserLauncher;
import org.nlogo.swing.FileDialog;
import org.nlogo.swing.OptionPane;

import java.util.List;
import java.util.Arrays;
import java.io.*;
import java.util.ArrayList;

import scala.jdk.javaapi.CollectionConverters;

public class HIDGogoExtension extends DefaultClassManager {

  static final int NUM_SENSORS = 8;

  private boolean isHeadless = true;

  private boolean shownErrorMessage = false;

  private class UnsuccessfulReadOperation extends Exception {};

  private void alertToNewGogoBoards(String primitiveName) throws ExtensionException {
    if(!shownErrorMessage) {
      if (isHeadless) {
        System.out.println("This model is using a primitive (gogo:" + primitiveName + ") from the old version of the GoGo extension. Use gogo-serial for older GoGo boards.");
      } else {
        while(true) {
          final int choice =
              new OptionPane(null, "GoGo Extension has been updated!",
                            "This model is using a primitive (gogo:" + primitiveName + ") from the old version of the GoGo extension. Use gogo-serial for older GoGo boards.",
                            CollectionConverters.asScala(Arrays.asList(new String[] { "More Information", "Close" })).toSeq()).getSelectedIndex();
          if (choice == 1) {
            break;
          }
          URI uri = null;
          try {
            uri = new URI("https://github.com/NetLogo/NetLogo/wiki/GoGo-Upgrade");
          } catch (URISyntaxException ex) {
            System.getProperties().put("org.nlogo.gogo.shownErrorMessage", "");
            throw new ExtensionException("Could not create GoGo upgrade URI?");
          }
          BrowserLauncher.openURI(null, uri);
        }
      }
      System.getProperties().put("org.nlogo.gogo.shownErrorMessage", "");
      shownErrorMessage = true;
    }
  }

  private class OldReporter implements Reporter {
    private Syntax syntax;
    private String primitiveName;

    private OldReporter(String primitiveName, Syntax syntax) {
      this.syntax = syntax;
      this.primitiveName = primitiveName;
    }

    public Syntax getSyntax() { return syntax; }
    public Object report(Argument args[], Context context) throws ExtensionException {
      alertToNewGogoBoards(primitiveName);
      return null;
    }
  }

  private class OldCommand implements Command {
    private Syntax syntax;
    private String primitiveName;

    private OldCommand(String primitiveName, Syntax syntax) {
      this.syntax = syntax;
      this.primitiveName = primitiveName;
    }

    public Syntax getSyntax() { return syntax; }
    public void perform(Argument args[], Context context) throws ExtensionException {
      alertToNewGogoBoards(primitiveName);
    }
  }

  @Override
  public void load(PrimitiveManager pm) throws ExtensionException {

    shownErrorMessage = System.getProperties().containsKey("org.nlogo.gogo.shownErrorMessage");

    pm.addPrimitive("primitives", new Prims() );

    pm.addPrimitive("init", new Init() );

    pm.addPrimitive("beep", new Beep() );

    pm.addPrimitive("read-sensors", new ReadSensors() );
    pm.addPrimitive("read-sensor", new ReadSensorNumber() );

    pm.addPrimitive("led", new LED() );

    pm.addPrimitive("talk-to-output-ports",new TalkToOutputPort() );
    pm.addPrimitive("set-output-port-power", new SetOutputPortPower() );
    pm.addPrimitive("output-port-on", new OutputPortOn() );
    pm.addPrimitive("output-port-off", new OutputPortOff() );
    pm.addPrimitive("output-port-clockwise", new OutputPortClockwise() );
    pm.addPrimitive("output-port-counterclockwise", new OutputPortCounterClockwise() );

    pm.addPrimitive("talk-to-servos", new TalkToServo() );
    pm.addPrimitive("set-servo", new SetServo() );

    pm.addPrimitive("read-all", new ReadAll() );
    pm.addPrimitive("send-bytes", new SendBytes() );
    pm.addPrimitive("howmany-gogos", new Enumerate() );

    // Old Primitives that should let you know we've changed the extension
    pm.addPrimitive("ports", new OldReporter("ports", SyntaxJ.reporterSyntax(Syntax.ListType())));
    pm.addPrimitive("burst-value", new OldReporter("burst-value", SyntaxJ.reporterSyntax(new int[] {Syntax.NumberType()}, Syntax.NumberType())));
    pm.addPrimitive("close", new OldCommand("close", SyntaxJ.commandSyntax()));
    pm.addPrimitive("install", new OldCommand("install", SyntaxJ.commandSyntax()));
    pm.addPrimitive("led-off", new OldCommand("led-off", SyntaxJ.commandSyntax()));
    pm.addPrimitive("led-on", new OldCommand("led-on", SyntaxJ.commandSyntax()));
    pm.addPrimitive("open", new OldCommand("open", SyntaxJ.commandSyntax(new int[] {Syntax.StringType()})));
    pm.addPrimitive("open?", new OldReporter("open?", SyntaxJ.reporterSyntax(Syntax.BooleanType())));
    pm.addPrimitive("output-port-coast", new OldCommand("output-port-coast", SyntaxJ.commandSyntax()));
    pm.addPrimitive("output-port-reverse", new OldCommand("output-port-reverse", SyntaxJ.commandSyntax()));
    pm.addPrimitive("output-port-thatway", new OldCommand("output-port-thatway", SyntaxJ.commandSyntax()));
    pm.addPrimitive("output-port-thisway", new OldCommand("output-port-thisway", SyntaxJ.commandSyntax()));
    pm.addPrimitive("ping", new OldReporter("ping", SyntaxJ.reporterSyntax(Syntax.BooleanType())));
    pm.addPrimitive("sensor", new OldReporter("sensor", SyntaxJ.reporterSyntax(new int[] {Syntax.NumberType()}, Syntax.NumberType())));
    pm.addPrimitive("set-burst-mode", new OldCommand("set-burst-mode", SyntaxJ.commandSyntax(new int[] {Syntax.ListType(), Syntax.BooleanType()})));
    pm.addPrimitive("stop-burst-mode", new OldCommand("stop-burst-mode", SyntaxJ.commandSyntax()));

  }

  @Override
  public void runOnce(ExtensionManager manager) throws ExtensionException {
    isHeadless = !manager.workspaceContext().workspaceGUI();
  }

  @Override public void unload(ExtensionManager pm) throws ExtensionException {
    unloaded = true;

    killProc();
  }

  private void killProc() throws ExtensionException {
    if (proc == null || !proc.isAlive()) {
      System.err.println("Daemon process was not alive, not attempting to shut it down.");
      return;
    }

    System.err.println("Unloading GoGo extension, shutting down daemon.");
    try {
      os.write('X');
      os.flush();
      proc.waitFor(5L, TimeUnit.SECONDS);
      os.close();
      is.close();
      err.close();
    } catch (IOException e) {
      System.err.println("Failed to close GoGo extension resources.");
      e.printStackTrace();
      throw new ExtensionException("Failed to close GoGo extension resources");
    } catch (InterruptedException e) {
      System.err.println("Interrupted while closing GoGo extension resources.");
      e.printStackTrace();
      throw new ExtensionException("Interrupted while closing GoGo extension resources.");
    }

    proc.destroy();
  }

  private OutputStream os = null;
  private InputStream is = null;
  private InputStream err = null;
  private Process proc = null;
  private boolean stillRunning = false;
  private boolean unloaded = false;

  // I don't think it is necessary to run Java via a command line process, I think we
  // can just spawn a process off from this one instead since everything is now
  // contained in a single jar.  But I don't want to make big changes like that
  // at the moment -Jeremy B January 2022
  private void bootHIDDaemon(final boolean useGoGo6) throws ExtensionException {
    String executable = Paths.get(System.getProperty("java.home"), "bin", "java").toAbsolutePath().toString();
    File gogoFile = new File(HIDGogoExtension.class.getProtectionDomain().getCodeSource().getLocation().getFile());
    Path gogoParentPath = gogoFile.toPath().getParent();
    String gogoExtensionPath = gogoParentPath.toString().replaceAll("%20", " ") + File.separator;
    System.out.println("gogoExtensionPath: " + gogoExtensionPath);
    try {
      String classpath =
        new File(gogoExtensionPath + "gogo.jar").getCanonicalPath() + File.pathSeparator +
        new File(gogoExtensionPath + "hid4java-develop-SNAPSHOT.jar").getCanonicalPath() + File.pathSeparator +
        new File(gogoExtensionPath + "jna.jar").getCanonicalPath();
      List<String> command = Arrays.asList(executable, "-classpath", classpath, "-showversion", useGoGo6 ? "gogohid.daemon.HIDGogo6Daemon" : "gogohid.daemon.HIDGogoDaemon");
      System.out.println("running: " + String.join(" ", command));
      ProcessBuilder procBuilder = new ProcessBuilder(command);
      proc = procBuilder.start();
      // this ensures that the daemon exits if NetLogo is closed while gogo is loaded (Isaac B 11/11/25)
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          try {
            killProc();
          } catch (ExtensionException ex) {
            // ignore, can't do anything if the app is already closing (Isaac B 11/11/25)
          }
        }
      });
      stillRunning = true;
      os = proc.getOutputStream();
      is = proc.getInputStream();
      err = proc.getErrorStream();
      System.out.println("Booted daemon!");

      new Thread() {
        public void run() {
          try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(err));
            String line = null;
            while((line = reader.readLine()) != null) {
              System.err.println(line);
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }.start();
      new Thread() {
        public void run() {
          try {
            proc.waitFor();
            stillRunning = false;
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }.start();
    } catch(Exception e) {
      System.out.println("ERROR BOOTING DAEMON:");
      System.out.println(e.getClass());
      System.out.println(e.getMessage());
      e.printStackTrace();
      throw new ExtensionException("Couldn't boot daemon");
    }
  }

  private void sendMessage(byte[] bytes) throws ExtensionException {
    try {
      if (unloaded) {
        return;
      }
      if (!stillRunning) {
        bootHIDDaemon(false);
      }
      os.write('S');
      os.write(bytes.length);
      os.write(bytes);
      os.flush();
    } catch(IOException e) {
      throw new ExtensionException("Couldn't write message to daemon");
    }
  }

  private short[] readSensors() throws ExtensionException, UnsuccessfulReadOperation {
    short[] sensors = readSensorsEx();
    // At some point with a new "batch" of GoGo boards, they started returning data
    // in a different format (probably long instead of short), but the values were still
    // only in the short range.  This means since we just read the data at 64 bytes at
    // a time, we get every other read as "all zeroes".  Since I don't have info on
    // how to differentiate the GoGo board versions in order to do things properly
    // we have this hacky workaround to purge any "all zeroes" readings.  We only run it
    // once, so if the sensors are on the old/short version and really are reading all
    // zeroes, at least that data will be relayed.  -Jeremy B January 2022
    boolean allAreZero = true;
    for (int index = 0; index < NUM_SENSORS; index++) {
      if (sensors[index] != 0) {
        allAreZero = false;
      }
    }
    if (allAreZero) {
      sensors = readSensorsEx();
    }
    return sensors;
  }

  private short[] readSensorsEx() throws ExtensionException, UnsuccessfulReadOperation {
    byte[] data = receiveMessage(64);
    short[] sensors = new short[NUM_SENSORS];
    for (int index = 0; index < NUM_SENSORS; index++) {
      ByteBuffer bb = ByteBuffer.wrap(data, (2 * index) + 1, 2);
      bb.order(ByteOrder.BIG_ENDIAN);
      sensors[index] = bb.getShort();
    }
    return sensors;
  }

  private byte[] receiveMessage(int numBytes) throws ExtensionException, UnsuccessfulReadOperation {
    try {
      if (unloaded) {
        byte[] empty = new byte[0];
        return empty;
      }
      if (!stillRunning) {
        bootHIDDaemon(false);
      }
      os.write('R');
      os.write(numBytes);
      os.flush();

      byte[] data = new byte[numBytes];
      is.read(data);
      return data;
    } catch(IOException e) {
      throw new ExtensionException("Couldn't write message to daemon");
    }
  }

  private int numAttached() throws ExtensionException {
    try {
      if (unloaded) {
        return 0;
      }
      if (!stillRunning) {
        bootHIDDaemon(false);
      }
      os.write('N');
      os.flush();
      return is.read();
    } catch (IOException e) {
      throw new ExtensionException("Couldn't write message to daemon");
    }
  }

  private class Prims implements Reporter {
    @Override
    public Syntax getSyntax() {
        return SyntaxJ.reporterSyntax(Syntax.ListType());
      }

    @Override
    public Object report(Argument[] arg0, Context arg1)
        throws ExtensionException, LogoException {
      LogoListBuilder llb = new LogoListBuilder();
      llb.add("primitives <this command, returns prims listing>");
      llb.add("----reading (inputs)---");
      llb.add("read-sensors {returns all 8 sensor values in a list}");
      llb.add("read-sensor <number> {number should be between 1 and 8 inclusive}");
      llb.add("----writing (outputs)---");
      llb.add("beep");
      llb.add("led <0 off 1 on> {refers to the user LED (yellow)");
      llb.add("talk-to-output-ports <List of output letter(s) to talk to> {e.g, [\"A\"] for port A, [\"A\" \"B\" \"D\" ] for ports A, B, and D}");
      llb.add("set-output-port-power <power value 0-100 for current output-port (see talk-to-output-ports)> {0 = full off; 100 = full on.  Sets board value to <power>% of 255.}");
      llb.add("output-port-on {turns current output-port (see talk-to-output-ports) on}");
      llb.add("output-port-off {turns current output-port (see talk-to-output-ports) off}");
      llb.add("output-port-clockwise {makes current output-port (see talk-to-output-ports) go clockwise}");
      llb.add("output-port-counterclockwise {makes current output-port (see talk-to-output-ports) go counterclockwise}");
      llb.add("set-servo <position> {makes current servo (see talk-to-output-ports) move to position <position> 0-255}");
      llb.add("----utility functions---");
      llb.add("read-all <returns entire byte sequence of HID device's status message> {converts to byte digits to hex strings}");
      llb.add("send-bytes <sends any byte sequence to the gogo> {list of integers will get padded out with zeroes.}");
      llb.add("howmany-gogos <returns number of HID gogos currently connected>");
      return llb.toLogoList();
    }
  }

  private class ReadSensors implements Reporter {
    @Override
    public Syntax getSyntax() {
      return SyntaxJ.reporterSyntax(Syntax.ListType());
    }

    @Override
    public Object report(Argument[] arg0, Context arg1)
        throws ExtensionException, LogoException {
      short[] sensors = null;
      try {
        sensors = readSensors();
      } catch (UnsuccessfulReadOperation e) {
        System.err.println("Couldn't read from gogo board");
        return (new LogoListBuilder()).toLogoList();
      }
      LogoListBuilder llb = new LogoListBuilder();
      for (int i = 0; i< sensors.length; i++) {
        llb.add(Double.valueOf(sensors[i]) );
      }
      return llb.toLogoList();
    }
  }

  private class ReadSensorNumber implements Reporter {
    @Override
    public Syntax getSyntax() {
      return SyntaxJ.reporterSyntax(new int[] {Syntax.NumberType() }, Syntax.NumberType());
    }

    @Override
    public Object report(Argument[] args, Context arg1)
        throws ExtensionException, LogoException {
      int sensorNumber = args[0].getIntValue();
      if ((sensorNumber > NUM_SENSORS) || (sensorNumber < 1)) {
        throw new ExtensionException("Sensor Number must be between 1 and 8 (inclusive).  You entered " + sensorNumber);
      }
      Short sensorReading = 0;
      try {
        short[] sensors = readSensors();
        sensorReading = sensors[sensorNumber - 1];
      } catch(UnsuccessfulReadOperation e) {
        System.err.println("Couldn't read from gogo board");
      }

      return sensorReading.doubleValue();
    }
  }

  private class Init implements Command {
    @Override
    public Syntax getSyntax() {
      return SyntaxJ.commandSyntax(new int[] {Syntax.StringType() });
    }

    @Override
    public void perform(Argument[] args, Context arg1)
        throws ExtensionException, LogoException {
      try {
        if (stillRunning) {
          throw new ExtensionException("Attempted to initialize a daemon but there's already one running");
        }

        final String daemonName = args[0].getString();

        switch (daemonName) {
          case "gogo":
          bootHIDDaemon(false);
          break;

          case "gogo6":
          bootHIDDaemon(true);
          break;

          default:
          throw new ExtensionException("Unknown daemon name: " + daemonName);
        }
      } catch (Exception e) {
        System.err.println("FAILED TO BOOT DAEMON");
        e.printStackTrace();
        throw new ExtensionException("Error in loading HID services");
      }
    }
  }

  private class Beep implements Command {
    @Override
    public Syntax getSyntax() {
      return SyntaxJ.commandSyntax();
    }

    @Override
    public void perform(Argument[] arg0, Context arg1)
        throws ExtensionException, LogoException {
      byte[] message = new byte[64];
      message[0] = (byte)0;
      message[1] = (byte)11;
      message[2] = (byte)0;
      message[3] = (byte)0;
      sendMessage(message);
    }
  }

  private class LED implements Command {
    @Override
    public Syntax getSyntax() {
      return SyntaxJ.commandSyntax(new int[] {Syntax.NumberType() });
    }

    @Override
    public void perform(Argument[] args, Context arg1)
        throws ExtensionException, LogoException {
      byte[] message = new byte[64];
      boolean turnOn = ( args[0].getIntValue() == 1);
      message[0] = (byte)0;
      message[1] = (byte)10;
      message[2] = (byte)0;
      if (turnOn) {
        message[3] = (byte)1;
      } else {
        message[3] = (byte)0;
      }

      sendMessage(message);
    }
  }

  private class TalkToOutputPort implements Command {
    @Override
    public Syntax getSyntax() {
      return SyntaxJ.commandSyntax(new int[] {Syntax.ListType() });
    }

    @Override
    public void perform(Argument args[], Context context)
        throws ExtensionException, org.nlogo.api.LogoException {
      java.util.Iterator<?> iter = args[0].getList().javaIterator();

      int outputPortMask = 0;
      while (iter.hasNext()) {
        Object val = iter.next();
        switch (val.toString().toLowerCase().charAt(0)) {
          case 'a':
          case 'A':
            outputPortMask = outputPortMask  | 1;
            break;
          case 'b':
          case 'B':
            outputPortMask = outputPortMask  | 2;
            break;
          case 'c':
          case 'C':
            outputPortMask = outputPortMask  | 4;
            break;
          case 'd':
          case 'D':
            outputPortMask = outputPortMask  | 8;
            break;
        }
      }

      byte[] message = new byte[64];
      message[0] = (byte)0;
      message[1] = (byte)7;
      message[2] = (byte)outputPortMask;
      message[3] = (byte)0;
      sendMessage(message);
    }
  }

  private class SetOutputPortPower implements Command {
    @Override
    public Syntax getSyntax() {
      return SyntaxJ.commandSyntax(new int[] {Syntax.NumberType() });
    }

    @Override
    public void perform(Argument[] args, Context ctx)
        throws ExtensionException, LogoException {
      int outputPortPowerVal = args[0].getIntValue();
      byte[] message = new byte[64];
      message[0] = (byte)0;
      message[1] = (byte)6;
      message[2] = (byte)0;
      message[3] = (byte)0; //high byte
      message[4] = (byte)outputPortPowerVal;
      sendMessage(message);
    }
  }

  private class OutputPortOn implements Command {
    @Override
    public Syntax getSyntax() {
      return SyntaxJ.commandSyntax();
    }

    @Override
    public void perform(Argument[] args, Context ctx)
        throws ExtensionException, LogoException {
      byte[] message = new byte[64];
      message[0] = (byte)0;
      message[1] = (byte)2;
      message[2] = (byte)0;
      message[3] = (byte)1; //on
      sendMessage(message);
    }
  }

  private class OutputPortOff implements Command {
    @Override
    public Syntax getSyntax() {
      return SyntaxJ.commandSyntax();
    }

    @Override
    public void perform(Argument[] args, Context ctx)
        throws ExtensionException, LogoException {
      byte[] message = new byte[64];
      message[0] = (byte)0;
      message[1] = (byte)2;
      message[2] = (byte)0;
      message[3] = (byte)0; //off

      sendMessage(message);
    }
  }


  private class OutputPortClockwise implements Command {
    @Override
    public Syntax getSyntax() {
      return SyntaxJ.commandSyntax();
    }

    @Override
    public void perform(Argument[] arg0, Context arg1)
        throws ExtensionException, LogoException {
      byte[] message = new byte[64];
      message[0] = (byte)0;
      message[1] = (byte)3;
      message[2] = (byte)0;
      message[3] = (byte)1;  //cw

      sendMessage(message);
    }
  }

  private class OutputPortCounterClockwise implements Command {
    @Override
    public Syntax getSyntax() {
      return SyntaxJ.commandSyntax();
    }

    @Override
    public void perform(Argument[] arg0, Context arg1)
        throws ExtensionException, LogoException {
      byte[] message = new byte[64];
      message[0] = (byte)0;
      message[1] = (byte)3;
      message[2] = (byte)0;
      message[3] = (byte)0;  //ccw
      sendMessage(message);
    }
  }

  private class TalkToServo implements Command {
    @Override
    public Syntax getSyntax() {
      return SyntaxJ.commandSyntax(new int[] {Syntax.ListType() });
    }

    @Override
    public void perform(Argument args[], Context context)
        throws ExtensionException, org.nlogo.api.LogoException {
      java.util.Iterator<?> iter = args[0].getList().javaIterator();

      int serverPortMask = 0;
      while (iter.hasNext()) {
        Object val = iter.next();
        switch (val.toString().toLowerCase().charAt(0)) {
          case 'a':
          case 'A':
            serverPortMask = serverPortMask  | 1;
            break;
          case 'b':
          case 'B':
            serverPortMask = serverPortMask  | 2;
            break;
          case 'c':
          case 'C':
            serverPortMask = serverPortMask  | 4;
            break;
          case 'd':
          case 'D':
            serverPortMask = serverPortMask  | 8;
            break;
        }
      }

      byte[] message = new byte[64];
      message[0] = (byte)0;
      message[1] = (byte)14;
      message[2] = (byte)serverPortMask;
      message[3] = (byte)0;
      sendMessage(message);
    }
  }

  private class SetServo implements Command {
    @Override
    public Syntax getSyntax() {
      return SyntaxJ.commandSyntax(new int[] {Syntax.NumberType() });
    }

    @Override
    public void perform(Argument[] args, Context ctx)
        throws ExtensionException, LogoException {
      int dutyLevel = args[0].getIntValue();
      byte[] message = new byte[64];
      message[0] = (byte)0;
      message[1] = (byte)9;
      message[2] = (byte)0;
      message[3] = (byte)0;
      message[4] = (byte)dutyLevel;
      sendMessage(message);
    }
  }

  private class SendBytes implements Command {
    @Override
    public Syntax getSyntax() {
      return SyntaxJ.commandSyntax(new int[] { Syntax.ListType() });
    }

    @Override
    public void perform(Argument[] args, Context ctx)
        throws ExtensionException, LogoException {
      LogoList messageList = args[0].getList();
      byte[] message = new byte[64];
      for (int i = 0; i<messageList.size() && i < 64; i++) {
        Double val = (Double)messageList.get(i);
        int v = val.intValue();
        message[i] = (byte)v;
      }
      sendMessage(message);
    }
  }

  private class ReadAll implements Reporter {
    @Override
    public Syntax getSyntax() {
        return SyntaxJ.reporterSyntax(Syntax.ListType());
      }

    @Override
    public Object report(Argument[] arg0, Context arg1)
        throws ExtensionException, LogoException {
      LogoListBuilder llb = new LogoListBuilder();
      try {
        byte[] data = receiveMessage(64);
        for (int i = 0; i< data.length; i++) {
          llb.add( byteToHex(data[i]) );
        }
      } catch(UnsuccessfulReadOperation e) {
        System.err.println("Couldn't read from gogo board");
      }
      return llb.toLogoList();
    }
  }

  private class Enumerate implements Reporter {
    @Override
    public Syntax getSyntax() {
      return SyntaxJ.reporterSyntax(Syntax.NumberType());
    }

    @Override
    public Object report(Argument[] arg0, Context arg1)
        throws ExtensionException, LogoException {
      return (double) numAttached();
    }
  }

  //utility.  converts a single byte to a 2-digit hex string representation.
  final private static char[] hexArray = "0123456789ABCDEF".toCharArray();
  public String byteToHex(byte b) {
      char[] hexChars = new char[2];

        int v = b & 0xFF;
        hexChars[0] = hexArray[v >>> 4];
        hexChars[1] = hexArray[v & 0x0F];

      return new String(hexChars);
  }
}
