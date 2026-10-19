package arduino;

import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingDeque;

import jssc.SerialPort;
import jssc.SerialPortException;

import org.nlogo.api.Argument;

import org.nlogo.api.Context;
import org.nlogo.api.DefaultClassManager;
import org.nlogo.api.Command;
import org.nlogo.api.Reporter;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.ExtensionManager;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.api.PrimitiveManager;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;


public class ArduinoExtension extends DefaultClassManager {

  static SerialPort serialPort;
  static PortListener portListener;
  static int BAUD_RATE = 9600;
  static int KEPT_MESSAGES_COUNT = 5;

  public Map<String,Object> values =
    Collections.<String,Object>synchronizedMap(new HashMap<String,Object>());

  public Deque<ErrorRecord> inboundErrors = new LinkedBlockingDeque<ErrorRecord>();

  public LinkedList<String> outboundMessages = new LinkedList<String>();

  public ArduinoExtension() {
    super();
    values.put("BaudRate", (double) BAUD_RATE);
  }

  @Override
  public void load(PrimitiveManager pm) throws ExtensionException {
    pm.addPrimitive("primitives", new Primitives());
    pm.addPrimitive("ports", new Ports());
    pm.addPrimitive("open", new Open(values, inboundErrors));
    pm.addPrimitive("close", new Close());
    pm.addPrimitive("get", new Get(values));
    pm.addPrimitive("write-string", new WriteString(outboundMessages));
    pm.addPrimitive("write-int", new WriteInt(outboundMessages));
    pm.addPrimitive("write-byte", new WriteByte(outboundMessages));
    pm.addPrimitive("is-open?", new IsOpen());
    pm.addPrimitive("debug-to-arduino", new DebugToArduino(outboundMessages));
    pm.addPrimitive("debug-from-arduino", new DebugFromArduino(inboundErrors));
  }

  static void addMessage(LinkedList<String> messages, String messageType, String messageValue) {
    while (messages.size() >= 5) {
      messages.removeLast();
    }
    messages.addFirst(messageType + ":" + messageValue);
  }

  public static class Primitives implements Reporter {
    @Override
    public Syntax getSyntax() {
        return SyntaxJ.reporterSyntax(Syntax.ListType());
      }

    @Override
    public Object report(Argument[] arg0, Context arg1)
        throws ExtensionException, LogoException {

      LogoListBuilder llist = new LogoListBuilder();
      String[] prims = {"reporter:primitives",
          "reporter:ports",
          "reporter:get[Name:String(case-insensitive)]",
          "reporter:is-open?",
          "reporter:debug-to-arduino",
          "reporter:debug-from-arduino",
          "",
          "command:open[Port:String]", "command:close",
          "command:write-string[Message:String]",
          "command:write-int[Message:int]",
          "command:write-byte[Message:byte]",
          "",
          "ALSO NOTE: Baud Rate of 9600 is expected"};
      for (String prim : prims ) {
        llist.add(prim);
      }
      return llist.toLogoList();
    }
  }

  public static class Ports implements Reporter {
    @Override
    public Syntax getSyntax() {
        return SyntaxJ.reporterSyntax(Syntax.ListType());
      }

    @Override
    public Object report(Argument[] arg0, Context arg1)
        throws ExtensionException, LogoException {
      LogoListBuilder llist = new LogoListBuilder();
      String[] names = jssc.SerialPortList.getPortNames();
      for (String name : names ) {
        llist.add(name);
      }
      return llist.toLogoList();
    }
  }


  public static class Open implements Command {
    private Map<String, Object> values;
    private Deque<ErrorRecord> inboundErrors;

    public Open(Map<String, Object> values, Deque<ErrorRecord> inboundErrors) {
      this.values = values;
      this.inboundErrors = inboundErrors;
    }

    @Override
    public Syntax getSyntax() {
      return SyntaxJ.commandSyntax(new int[] {Syntax.StringType() });
    }

    @Override
    public void perform(Argument[] args, Context ctxt)
        throws ExtensionException, LogoException {

      if ( serialPort != null && serialPort.isOpened() ) {
        throw new ExtensionException("Port is already open");
      } else {
        try {
          serialPort = new SerialPort(args[0].getString());
          serialPort.openPort();
          serialPort.setParams(BAUD_RATE, 8, 1, 0);
                int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;//Prepare mask
                serialPort.setEventsMask(mask); //Set mask
          portListener = new PortListener(serialPort, values, inboundErrors);
          serialPort.addEventListener(portListener);
        } catch (SerialPortException e) {
          throw new ExtensionException("Error in opening port: " + e.getMessage());
        }
      }
    }
  }


  public static class IsOpen implements Reporter {
    @Override
    public Syntax getSyntax() {
        return SyntaxJ.reporterSyntax(Syntax.BooleanType());
      }

    @Override
    public Object report(Argument[] arg0, Context arg1)
        throws ExtensionException, LogoException {
      return (serialPort != null && serialPort.isOpened() && portListener != null);
    }
  }


  public static void doClose() throws ExtensionException {
    try {
      serialPort.removeEventListener();
      serialPort.closePort();
    } catch (SerialPortException e) {
      throw new ExtensionException( "Error in writing: " + e.getMessage() );
    }
  }

  public static class Close implements Command {

    @Override
    public Syntax getSyntax() {
      return SyntaxJ.commandSyntax();
    }

    @Override
    public void perform(Argument[] args, Context ctxt)
        throws ExtensionException, LogoException {
      if ( (serialPort == null) || (!serialPort.isOpened()) ) {
        throw new ExtensionException( "Serial Port not Open");
      } else {
        doClose();
      }
    }
  }


  public static class Get implements Reporter {
    private Map<String, Object> values;

    public Get(Map<String, Object> values) {
      this.values = values;
    }

    @Override
    public Syntax getSyntax() {
        return SyntaxJ.reporterSyntax(new int[] {Syntax.StringType()},
            Syntax.StringType() | Syntax.BooleanType() | Syntax.NumberType());
      }

    @Override
    public Object report(Argument[] args, Context ctxt)
        throws ExtensionException, LogoException {
      return get(args[0].getString());
    }

    public Object get(String key) {
      String lcKey = key.toLowerCase();
      if (values.containsKey(lcKey))  {
        return values.get(lcKey);
      } else {
        return Boolean.FALSE;
      }
    }
  }

  public static class WriteString implements Command {
    final private LinkedList<String> outboundMessages;

    public WriteString(LinkedList<String> outboundMessages) {
      this.outboundMessages = outboundMessages;
    }

    @Override
    public Syntax getSyntax() {
      return SyntaxJ.commandSyntax(new int[] {Syntax.StringType() });
    }
    @Override
    public void perform(Argument[] args, Context ctxt)
        throws ExtensionException, LogoException {
      if ( (serialPort == null) || (!serialPort.isOpened()) ) {
        throw new ExtensionException( "Serial Port not Open");
      }
      try {
        String arg = args[0].getString();
        serialPort.writeString(arg);
        addMessage(outboundMessages, "s", arg);
      } catch (SerialPortException e) {
        throw new ExtensionException( "Error in writing: " + e.getMessage() );
      }
    }
  }

  public static class WriteInt implements Command {
    final private LinkedList<String> outboundMessages;

    public WriteInt(LinkedList<String> outboundMessages) {
      this.outboundMessages = outboundMessages;
    }

    @Override
    public Syntax getSyntax() {
      return SyntaxJ.commandSyntax(new int[] {Syntax.NumberType() });
    }
    @Override
    public void perform(Argument[] args, Context ctxt)
        throws ExtensionException, LogoException {
      if ( (serialPort == null) || (!serialPort.isOpened()) ) {
        throw new ExtensionException( "Serial Port not Open");
      }
      try {
        int arg = args[0].getIntValue();
        serialPort.writeInt(arg);
        addMessage(outboundMessages, "i", Integer.toString(arg));
      } catch (SerialPortException e) {
        throw new ExtensionException( "Error in writing: " + e.getMessage() );
      }
    }
  }

  public static class WriteByte implements Command {
    final private LinkedList<String> outboundMessages;

    public WriteByte(LinkedList<String> outboundMessages) {
      this.outboundMessages = outboundMessages;
    }

    @Override
    public Syntax getSyntax() {
      return SyntaxJ.commandSyntax(new int[] {Syntax.NumberType() });
    }
    @Override
    public void perform(Argument[] args, Context ctxt)
        throws ExtensionException, LogoException {
      if ( (serialPort == null) || (!serialPort.isOpened()) ) {
        throw new ExtensionException( "Serial Port not Open");
      }
      try {
        byte arg = (byte) args[0].getIntValue();
        serialPort.writeByte(arg);
        addMessage(outboundMessages, "b", Integer.toString((int) arg));
      } catch (SerialPortException e) {
        throw new ExtensionException( "Error in writing: " + e.getMessage() );
      }
    }
  }

  @Override
  public void unload( ExtensionManager em) {
    //first remove the event listener (if any) and close the port
    if (portListener != null && serialPort != null) {
      try {
        serialPort.removeEventListener();
      } catch (SerialPortException e) {
        e.printStackTrace();
      }
      try {
        serialPort.closePort();
      } catch (SerialPortException e2) {
        e2.printStackTrace();
      }
    } else {
      if (serialPort != null && serialPort.isOpened()) {
        try {
          serialPort.closePort();
        } catch (SerialPortException e) {
          e.printStackTrace();
        }
      }
    }
    //now unload the native library, so that if we're loading the extension
    //again on the same NetLogo run, it doesn't cause us troubles.
    try
    {
      ClassLoader classLoader = this.getClass().getClassLoader() ;
      java.lang.reflect.Field field = ClassLoader.class.getDeclaredField( "nativeLibraries" ) ;
      field.setAccessible(true);
      @SuppressWarnings("unchecked")
      Vector<Object> libs = (Vector<Object>) (field.get(classLoader)) ;
      for ( Object o : libs )
      {
        java.lang.reflect.Method finalize = o.getClass().getDeclaredMethod( "finalize" , new Class<?>[0] ) ;
        finalize.setAccessible( true ) ;
        finalize.invoke( o , new Object[0] ) ;
      }
    }
    catch( Exception e )
    {
      System.err.println( e.getMessage() ) ;
    }
  }


  public static class DebugFromArduino implements Reporter {
    final private Deque<ErrorRecord> inboundErrors;

    public DebugFromArduino(Deque<ErrorRecord> inboundErrors) {
      this.inboundErrors = inboundErrors;
    }

    @Override
    public Syntax getSyntax() {
      return SyntaxJ.reporterSyntax(Syntax.ListType());
    }

    @Override
    public Object report(Argument[] arg0, Context arg1)
      throws ExtensionException, LogoException {
      LogoListBuilder llist = new LogoListBuilder();

      Iterator<ErrorRecord> iter = inboundErrors.iterator();

      while (iter.hasNext()) {
        LogoListBuilder itemBuilder = new LogoListBuilder();
        ErrorRecord record = iter.next();
        itemBuilder.add(record.inboundString());
        itemBuilder.add(record.errorDescription());
        if (record.exception().nonEmpty()) {
          itemBuilder.add(record.exception().get().getMessage());
        }
        llist.add(itemBuilder.toLogoList());
      }

      return llist.toLogoList();
    }
  }

  public static class DebugToArduino implements Reporter {
    final private LinkedList<String> outboundMessages;

    public DebugToArduino(LinkedList<String> outboundMessages) {
      this.outboundMessages = outboundMessages;
    }

    @Override
    public Syntax getSyntax() {
      return SyntaxJ.reporterSyntax(Syntax.ListType());
    }

    @Override
    public Object report(Argument[] arg0, Context arg1)
      throws ExtensionException, LogoException {
      LogoListBuilder llist = new LogoListBuilder();

      Iterator<String> iter = outboundMessages.iterator();

      while(iter.hasNext()) {
        llist.add(iter.next());
      }

      return llist.toLogoList();
    }
  }
}
