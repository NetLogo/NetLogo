/** (c) 2004 Uri Wilensky. See README.txt for terms of use. **/

package org.nlogo.extensions.gogo;

import org.nlogo.api.Syntax;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultCommand;
import org.nlogo.api.DefaultReporter;
import org.nlogo.api.Argument; 
import org.nlogo.api.Context;
import org.nlogo.api.LogoList;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.ExtensionManager ;
import java.net.URL;
import java.io.File;

public class GoGoExtension extends org.nlogo.api.DefaultClassManager
{
	
    public static GoGoController controller ;
	
    public void load( org.nlogo.api.PrimitiveManager primManager )
    {
	
		// Reset the "sys_paths" field of the ClassLoader to null.
		try {
	    
			final String basedir = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile()).getParent();
			final String filesep = System.getProperty("file.separator");
			String libdir = basedir + filesep + "lib" + filesep;
	    
			String osname = System.getProperty("os.name");	    
			if ( osname.equals("Mac OS X") )
			{
				libdir = libdir + osname ;
			}
			else if( osname.startsWith("Windows") )
			{
				libdir = libdir + "Windows";
			}
			else
			{
				final String systype = System.getProperty("os.name") + "-" + System.getProperty("os.arch");		    
				libdir = libdir + osname + "-" + System.getProperty("os.arch") ; 
			}
			org.nlogo.api.JavaLibraryPath.add(new java.io.File(libdir));
	    
		}catch(Exception ex) {
			System.err.println("Cannot add our native libraries to java.library.path: " + ex.getMessage());
		}

	
		primManager.addPrimitive( "ports", new GoGoListPorts() ) ;
		primManager.addPrimitive( "open", new GoGoOpen() ) ;
		primManager.addPrimitive( "open?", new GoGoOpenPredicate() ) ;
		primManager.addPrimitive( "close", new GoGoClose() ) ;
		primManager.addPrimitive( "ping", new GoGoPing() ) ;
		primManager.addPrimitive( "output-port-on", new GoGoOutputPortOn() ) ;
		primManager.addPrimitive( "output-port-off", new GoGoOutputPortOff() ) ;
		primManager.addPrimitive( "output-port-coast", new GoGoOutputPortCoast() ) ;
		primManager.addPrimitive( "output-port-thisway", new GoGoOutputPortThisWay() ) ;
		primManager.addPrimitive( "output-port-thatway", new GoGoOutputPortThatWay() ) ;
		primManager.addPrimitive( "set-output-port-power", new GoGoOutputPortPower() ) ;
		primManager.addPrimitive( "output-port-reverse", new GoGoOutputPortReverse() ) ;
		primManager.addPrimitive( "talk-to-output-ports", new GoGoTalkToOutputPorts() ) ;
		primManager.addPrimitive( "set-burst-mode", new GoGoSetBurstMode() ) ;
		primManager.addPrimitive( "stop-burst-mode", new GoGoStopBurstMode() ) ;
		primManager.addPrimitive( "burst-value", new GoGoSensorBurstValue() ) ;
		primManager.addPrimitive( "sensor", new GoGoSensor() ) ;
		//primManager.addPrimitive( "switch", new GoGoSwitch() ) ;
    }
	
    public void unload( ExtensionManager em )
    {
		close() ;
		// Since native libraries cannot be loaded in more than one classloader at once
		// and even though we are going dispose of this classloader we can't be sure
		// it will be GC'd before we want to reload this extension, we unload it manually
		// as described here: http://forums.sun.com/thread.jspa?forumID=52&threadID=283774
		// This is a hack, but it works. ev 6/25/09
		try
		{
			ClassLoader classLoader = this.getClass().getClassLoader() ;
			java.lang.reflect.Field field = ClassLoader.class.getDeclaredField( "nativeLibraries" ) ;
			field.setAccessible(true);
			java.util.Vector libs = (java.util.Vector) field.get(classLoader) ;
			for ( Object o : libs ) 
			{
				java.lang.reflect.Method finalize = o.getClass().getDeclaredMethod( "finalize" , new Class[0] ) ;
				finalize.setAccessible( true ) ;
				finalize.invoke( o , new Object[0] ) ;
			}
		}
		catch( Exception e )
		{
			System.err.println( e.getMessage() ) ;
		}
    }


    public static void ensureGoGoPort()
		throws ExtensionException
    {
		if ( controller == null || controller.currentPort() == null )
	    {
			throw new ExtensionException( "No GoGo port open." ) ;
	    }
		
    }

	public static void close()
	{
		if( controller != null )
		{
			if( controller.currentPort() != null )
			{
				controller.closePort() ;
			}
			controller = null ;
		}
	}
		

    public static void initController( String portName )
    {
		controller = new GoGoController( portName ) ;
		// ping to clear out any queued up output
    }

    public static class GoGoOpen extends DefaultCommand
    {
		public Syntax getSyntax() {
			int[] right = { Syntax.TYPE_STRING } ;
			return Syntax.commandSyntax(right);
		}

		public void perform(Argument args[], Context context)
			throws ExtensionException , org.nlogo.api.LogoException
		{
			try {
				close() ;
			}
			catch ( RuntimeException e )
			{
				throw new ExtensionException("Cannot close port: " +
											 controller.currentPortName() + " : " + e.getLocalizedMessage()) ;
			}
			try {
				initController( args[0].getString() ) ;
				controller.openPort() ;
				controller.setReadTimeout( 50 );
			}
			catch ( java.lang.NoClassDefFoundError e )
			{
				throw new ExtensionException(
					"Could not initialize GoGo Extension.  Please ensure that you have installed RXTX correctly.  Full error message: " + args[0].getString() + " : " + e.getLocalizedMessage() ) ;
			}
			catch ( RuntimeException e )
			{
				throw new ExtensionException( "Could not open port " + args[0].getString() + " : " + e.getLocalizedMessage() ) ;
			}

			try {
				if ( ! controller.ping() ) {
					throw new ExtensionException( "GoGo board not responding." ) ;
				}
				
			}
			catch ( RuntimeException e )
			{
				throw new ExtensionException( "GoGo board not responding: " + e.getLocalizedMessage() ) ;
			}
		}
    }

    public static class GoGoClose extends DefaultCommand
    {
		public Syntax getSyntax() {
			return Syntax.commandSyntax();
		}

		public void perform(Argument args[], Context context)
			throws ExtensionException , org.nlogo.api.LogoException
		{
			try
			{
				close() ;
			}
			catch ( RuntimeException e )
			{
				throw new ExtensionException("Cannot close port: " +
											 controller.currentPortName() + " : " + e.getLocalizedMessage()) ;
			}
		}
    }

    public static class GoGoListPorts extends DefaultReporter
    {
		public Syntax getSyntax() {
			return Syntax.reporterSyntax(Syntax.TYPE_LIST);
		}
		
		public Object report(Argument args[], Context context)
			throws ExtensionException , org.nlogo.api.LogoException
		{
			try  {
				return LogoList.fromJava( GoGoController.availablePorts() ) ;
			}
			catch ( java.lang.NoClassDefFoundError e )
			{
				throw new ExtensionException(
					"Could not initialize GoGo Extension.  Please ensure that you have installed RXTX correctly.  Full error message: " + e.getLocalizedMessage() ) ;
			}
		}
    }

    public static class GoGoOpenPredicate extends DefaultReporter
    {
		public Syntax getSyntax() {
			return Syntax.reporterSyntax(Syntax.TYPE_BOOLEAN);
		}
		
		public Object report(Argument args[], Context context)
			throws ExtensionException , org.nlogo.api.LogoException
		{
			return ( ( controller != null )
					 && ( controller.currentPort() != null ) )
				? Boolean.TRUE
				: Boolean.FALSE ;
		}
    }
	
    public static class GoGoPing extends DefaultReporter
    {
		public Syntax getSyntax() {
			return Syntax.reporterSyntax(Syntax.TYPE_BOOLEAN);
		}

		public Object report(Argument args[], Context context)
			throws ExtensionException , org.nlogo.api.LogoException
		{
			if ( controller == null )
			{
				return Boolean.FALSE ;
			}
			
			return controller.ping()
				? Boolean.TRUE
				: Boolean.FALSE ;
		}
    }

    public static class GoGoOutputPortOn extends DefaultCommand
    {
		public Syntax getSyntax() {
			return Syntax.commandSyntax();
		}
		
		public void perform(Argument args[], Context context)
			throws ExtensionException , org.nlogo.api.LogoException
		{
			ensureGoGoPort() ;
			controller.outputPortOn();
		}
    }

    public static class GoGoOutputPortOff extends DefaultCommand
    {
		public Syntax getSyntax() {
			return Syntax.commandSyntax();
		}
		
		public void perform(Argument args[], Context context)
			throws ExtensionException , org.nlogo.api.LogoException
		{
			ensureGoGoPort() ;
			controller.outputPortOff();
		}
    }

    public static class GoGoOutputPortCoast extends DefaultCommand
    {
		public Syntax getSyntax() {
			return Syntax.commandSyntax();
		}
		
		public void perform(Argument args[], Context context)
			throws ExtensionException , org.nlogo.api.LogoException
		{
			ensureGoGoPort() ;
			controller.outputPortOff();
		}
    }
	
    public static class GoGoOutputPortReverse extends DefaultCommand
    {
		public Syntax getSyntax() {
			return Syntax.commandSyntax();
		}
		
		public void perform(Argument args[], Context context)
			throws ExtensionException , org.nlogo.api.LogoException
		{
			ensureGoGoPort() ;
			controller.outputPortReverse();
		}
    }

    public static class GoGoOutputPortThisWay extends DefaultCommand
    {
		public Syntax getSyntax() {
			return Syntax.commandSyntax();
		}
		
		public void perform(Argument args[], Context context)
			throws ExtensionException , org.nlogo.api.LogoException
		{
			ensureGoGoPort() ;
			controller.outputPortThisWay();
		}
    }

    public static class GoGoOutputPortThatWay extends DefaultCommand
    {
		public Syntax getSyntax() {
			return Syntax.commandSyntax();
		}
		
		public void perform(Argument args[], Context context)
			throws ExtensionException , org.nlogo.api.LogoException
		{
			ensureGoGoPort() ;
			controller.outputPortThatWay();
		}
    }

    public static class GoGoOutputPortPower extends DefaultCommand
    {
		public Syntax getSyntax() {
			int[] right = { Syntax.TYPE_NUMBER } ;
			return Syntax.commandSyntax(right);
		}
		
		public void perform(Argument args[], Context context)
			throws ExtensionException , org.nlogo.api.LogoException
		{
			ensureGoGoPort() ;
			try
			{
				int level = args[0].getIntValue() ;
				controller.setOutputPortPower( level );
			} catch (RuntimeException e) {
				throw new ExtensionException( "Cannot set output port power: " + e.getLocalizedMessage() );
			}
		}
    }

    public static class GoGoTalkToOutputPorts extends DefaultCommand
    {
		public Syntax getSyntax() {
			int[] right = { Syntax.TYPE_LIST } ;
			return Syntax.commandSyntax(right);
		}
		
		public void perform(Argument args[], Context context)
			throws ExtensionException , org.nlogo.api.LogoException
		{
			ensureGoGoPort() ;
			
			java.util.Iterator iter = args[0].getList().iterator() ;
			int outputPortMask = 0;
			
			while ( iter.hasNext() )
			{
				Object val = iter.next() ;
				switch ( val.toString().toLowerCase().charAt(0) ) {
					case 'a':
						outputPortMask = outputPortMask | GoGoController.OUTPUT_PORT_A;
						break;
					case 'b':
						outputPortMask = outputPortMask | GoGoController.OUTPUT_PORT_B;
						break;
					case 'c':
						outputPortMask = outputPortMask | GoGoController.OUTPUT_PORT_C;
						break;
					case 'd':
						outputPortMask = outputPortMask | GoGoController.OUTPUT_PORT_D;
						break;
				}
			}
			controller.talkToOutputPorts(outputPortMask);
		}
    }

	public static NLBurstCycleHandler burstCycleHandler = null;

	public static class NLBurstCycleHandler
		implements GoGoController.BurstCycleHandler
	{
		public final int[] sensorValues = new int[8];
		
		synchronized public void handleBurstCycle(int sensor, int value)
		{
			//System.out.println( "Sensor " + sensor + " value: " + value );
			sensorValues[sensor - 1] = value;
		}
		public int sensorValue(int sensor)
		{
			return sensorValues[sensor - 1];
		}

	}

    public static class GoGoSensorBurstValue extends DefaultReporter
    {
		public Syntax getSyntax() {
			int[] right = { Syntax.TYPE_NUMBER } ;
			return Syntax.reporterSyntax(right, Syntax.TYPE_NUMBER);
		}
		
		public Object report(Argument args[], Context context)
			throws ExtensionException , org.nlogo.api.LogoException
		{
			int sensor = args[0].getIntValue() ;
			if ( burstCycleHandler != null)
			{
				if ( sensor > 0  && sensor < 9 )
				{
					return Double.valueOf( burstCycleHandler.sensorValue(sensor) );		
				}
				else
				{
					throw new ExtensionException( "Sensor id " + sensor + " is out of range, should be 1-8." ) ;
				}
			}
			else
			{
				throw new ExtensionException( "Burst Mode is not set, use set-burst-mode to turn on burst mode for specific sensors.");
			}
		}
    }
	
	
	static private int sensorMask(java.util.AbstractSequentialList sensorList)
	{
		int sensorMask = 0;
		java.util.Iterator iter = null;
		
		if (sensorList != null)
		{
			iter = sensorList.iterator();
			
			while ( iter.hasNext() )
			{
				Object val = iter.next() ;
				switch ( val.toString().toLowerCase().charAt(0) ) {
					case '1':
						sensorMask = sensorMask | GoGoController.SENSOR_1;
						break;
					case '2':
						sensorMask = sensorMask | GoGoController.SENSOR_2;
						break;
					case '3':
						sensorMask = sensorMask | GoGoController.SENSOR_3;
						break;
					case '4':
						sensorMask = sensorMask | GoGoController.SENSOR_4;
						break;
					case '5':
						sensorMask = sensorMask | GoGoController.SENSOR_5;
						break;
					case '6':
						sensorMask = sensorMask | GoGoController.SENSOR_6;
						break;
					case '7':
						sensorMask = sensorMask | GoGoController.SENSOR_7;
						break;
					case '8':
						sensorMask = sensorMask | GoGoController.SENSOR_8;
						break;
				}
			}
		}
		return sensorMask;
	}

    public static class GoGoSetBurstMode extends DefaultCommand
    {
		public Syntax getSyntax() {
			int[] right = { Syntax.TYPE_LIST, Syntax.TYPE_BOOLEAN };
			return Syntax.commandSyntax(right);
		}
		
		public void perform(Argument args[], Context context)
			throws ExtensionException , org.nlogo.api.LogoException
		{
			int sensorMask = sensorMask(args[0].getList());			
			int speed = args[1].getBoolean().booleanValue() ? GoGoController.BURST_SPEED_HIGH : GoGoController.BURST_SPEED_LOW;
			
			controller.setBurstMode(sensorMask, speed);
			burstCycleHandler = new NLBurstCycleHandler();
			controller.startBurstReader(burstCycleHandler);
		}
    }

	public static class GoGoStopBurstMode extends DefaultCommand
    {
		public Syntax getSyntax() {
			int[] right = { };
			return Syntax.commandSyntax(right);
		}
		
		public void perform(Argument args[], Context context)
			throws ExtensionException , org.nlogo.api.LogoException
		{
			controller.stopBurstReader();
			burstCycleHandler = null;
			controller.setBurstMode(0, GoGoController.BURST_SPEED_HIGH);
			controller.setBurstMode(0, GoGoController.BURST_SPEED_LOW);

		}
    }


	
    public static class GoGoSensor extends DefaultReporter
    {
		public Syntax getSyntax() {
			int[] right = { Syntax.TYPE_NUMBER } ;
			return Syntax.reporterSyntax(right, Syntax.TYPE_NUMBER);
		}

		public Object report(Argument args[], Context context)
			throws ExtensionException , org.nlogo.api.LogoException
		{

			int sensor = args[0].getIntValue() ;
			try {
				return Double.valueOf( controller.readSensor(sensor) );
			} catch (RuntimeException e) {
				return Double.valueOf(0);
			}
		}
    }

    public GoGoController getController()	{ return controller ; }


    @Override
    public java.util.List<String> additionalJars() {
		return java.util.Arrays.asList( new String[] { "RXTXcomm.jar" } ) ;
    }
}
