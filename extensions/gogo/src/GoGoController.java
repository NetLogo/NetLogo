package org.nlogo.extensions.gogo;

import java.io.PushbackInputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.ListIterator;

// Use the full replacement RXTX which has it's own package space
// and doesn't need to Sun javax.comm jar
import gnu.io.*;

public class GoGoController
{
	RXTXCommDriver driver ;
	String portName;
	CommPortIdentifier portId;
	BurstReader burstReader;
	SerialPort port ;
	
	public PushbackInputStream inputStream ;
	public OutputStream outputStream ;

	// current burst mode
	public int burstModeMask = 0 ;
	
	public static final byte IN_HEADER1 = (byte)0x55 ;
	public static final byte IN_HEADER2 = (byte)0xFF ;
	public static final byte OUT_HEADER1    = (byte)0x54 ;
	public static final byte OUT_HEADER2    = (byte)0xFE ;
	public static final byte ACK_BYTE   = (byte)0xAA ;
	
	public static final byte CMD_PING = (byte)0x00 ;
	public static final byte CMD_READ_SENSOR = (byte)0x20 ;
	public static final byte CMD_OUTPUT_PORT_ON = (byte)0x40 ;
	public static final byte CMD_OUTPUT_PORT_OFF = (byte)0x44 ;
	public static final byte CMD_OUTPUT_PORT_RD = (byte)0x48 ;
	public static final byte CMD_OUTPUT_PORT_THISWAY = (byte)0x4C ;
	public static final byte CMD_OUTPUT_PORT_THATWAY = (byte)0x50 ;
	public static final byte CMD_OUTPUT_PORT_COAST = (byte)0x54 ;
	public static final byte CMD_OUTPUT_PORT_POWER = (byte)0x60 ;
	public static final byte CMD_TALK_TO_OUTPUT_PORT = (byte)0x80 ;
	public static final byte CMD_SET_BURST_MODE = (byte)0xA0 ;

	public static final byte CMD_LED_ON = (byte)0xC0 ;
	public static final byte CMD_LED_OFF    = (byte)0xC1 ;
	public static final byte CMD_BEEP   = (byte)0xC4	 ;

	// read modes for sensors
	public static final byte SENSOR_READ_NORMAL = (byte)0x00 ;
	public static final byte SENSOR_READ_MAX = (byte)0x01 ;
	public static final byte SENSOR_READ_MIN = (byte)0x02 ;

	// Output port identifiers
	// An output port mask is created by XORing these
	// OUTPUT_PORT_A | OUTPUT_PORT_C means output ports A and C
	// See talkToOutputPorts for usage
	public static final int OUTPUT_PORT_A = 0x01 ;
	public static final int OUTPUT_PORT_B = 0x02 ;
	public static final int OUTPUT_PORT_C = 0x04 ;
	public static final int OUTPUT_PORT_D = 0x08 ;

	// Sensor identifiers
	// A sendor mask is created by XORing these
	// See setBurtMode for usage
	public static final int SENSOR_1 = 0x01 ;
	public static final int SENSOR_2 = 0x02 ;
	public static final int SENSOR_3 = 0x04 ;
	public static final int SENSOR_4 = 0x08 ;
	public static final int SENSOR_5 = 0x10 ;
	public static final int SENSOR_6 = 0x20 ;
	public static final int SENSOR_7 = 0x40 ;
	public static final int SENSOR_8 = 0x80 ;

	public static final int BURST_SPEED_HIGH = 0x00 ;
	public static final int BURST_SPEED_LOW = 0x01 ;
	public static final byte BURST_CHUNK_HEADER = (byte)0x0C ;

	private static final int[] sensorIDs = { SENSOR_1,
											 SENSOR_2,
											 SENSOR_3,
											 SENSOR_4,
											 SENSOR_5,
											 SENSOR_6,
											 SENSOR_7,
											 SENSOR_8 } ;
	
	
	public static int sensorID(int sensor)
	{
		if ( (sensor < 1) || (sensor > 8)) 
			throw new RuntimeException("Sensor number out of range: " + sensor);
		return sensorIDs[ ( sensor - 1 ) ] ;
	}
	
	public static CommPortIdentifier findPortByName( String portName )
	{
		Enumeration portList = CommPortIdentifier.getPortIdentifiers() ;
		CommPortIdentifier id ;
		
		while (portList.hasMoreElements()) {
			id = (CommPortIdentifier) portList.nextElement();
			if (id.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				if (id.getName().equals(portName)) {
					return id ;
				} 
			} 
		}
		return null;
	}
	
	
	public GoGoController( String portName )
	{
		this.portName = portName ;
	}
	
	public static List<String> availablePorts()
	{
		return listPorts( true ) ;
	}
	
	public static List<String> serialPorts()
	{
		return listPorts( false ) ;
	}
	
	public static List<String> listPorts( boolean onlyAvailable )
	{
		Enumeration portList;
		CommPortIdentifier portId;
		List<String> portNames = new ArrayList<String>() ;
		
		portList = CommPortIdentifier.getPortIdentifiers();
		while (portList.hasMoreElements()) {
			portId = (CommPortIdentifier) portList.nextElement();
			if ( portId.getPortType() == CommPortIdentifier.PORT_SERIAL
				 && ( ! onlyAvailable || ! portId.isCurrentlyOwned() ) )
			{
				portNames.add( portId.getName() ) ;
			}
		}
		return portNames ;
	}

	public String currentPortName()
	{
		if ( port != null )
			return port.getName() ;
		return null;
	}

	public SerialPort currentPort()
	{
		return port ;
	}
	
	public void closePort() {
		synchronized( inputStream )
		{
			synchronized( outputStream )
			{
				if (port != null) {
					port.removeEventListener();
					if (inputStream != null) {
						try {
							inputStream.close();
							inputStream = null;
						}
						catch (IOException e) {
							e.printStackTrace();
						}
					}
					if (outputStream != null) {
						try {
							outputStream.close();		
							outputStream = null;
						}
						catch (IOException e) {
							e.printStackTrace();
						}
					}
					port.close();			
					port = null;			
				}
			}
		}
	}

	
	public boolean openPort()
	{
		// if already open, just return true
		if ( port != null ){ return true ; }
		
		portId = findPortByName( portName ) ;
		
		if ( portId == null )
		{
			throw new RuntimeException(
				"Cannot find port: " + portName ) ;
		}
		
		try {
			port = (SerialPort) portId.open("GoGoController", 2000) ;
		}
		catch (PortInUseException e)
		{
			throw new RuntimeException(
				"Port is already in use: " + e ) ;
		}
		catch ( RuntimeException e )
		{
			throw new RuntimeException(
				"Unable to open port: " + e ) ;
		}

		if ( port != null ) {

			try {
				inputStream = new PushbackInputStream(port.getInputStream());
				outputStream = port.getOutputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				port.setSerialPortParams(9600,
										 SerialPort.DATABITS_8,
										 SerialPort.STOPBITS_1,
										 SerialPort.PARITY_NONE);
			} catch (UnsupportedCommOperationException e) {
				e.printStackTrace();
			}
			return true ;
		}
		else
		{
			return false ;
		}
		
	}

	public void setReadTimeout(int ms)
	{
		try {
			synchronized( inputStream )
			{
				port.enableReceiveTimeout( ms );
				// update our input stream
				inputStream = new PushbackInputStream( port.getInputStream() );
			}
		} catch (UnsupportedCommOperationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public  void writeCommand(byte[] command)
	{
		synchronized( outputStream )
		{
			try {
				writeByte(OUT_HEADER1);
				writeByte(OUT_HEADER2);
				outputStream.write(command);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public byte readByte()
		throws IOException
	{
		int b;
		synchronized( inputStream )
		{
			b = inputStream.read();
		}
		return (byte) b;
		//System.out.println( "Read: " + b ) ;

		
	}

	public byte peekByte()
		throws IOException
	{
		int b;
		synchronized( inputStream )
		{
			b = inputStream.read();
			inputStream.unread(b);
		}
		return (byte) b;

	}

	public int readInt()
		throws IOException
	{
		int b;
		synchronized( inputStream )
		{
			b = inputStream.read();
		}
		//System.out.println( "Read: " + b ) ;
		return b;
	}

	public void writeByte(byte b)
		throws IOException
	{
		synchronized( outputStream )
		{
			outputStream.write(b);
			//System.out.println( "Wrote: " + b ) ;
		}
	}
	
	public boolean waitForReplyHeader()
	{
		try {
			int b;
			for (int i = 0; i < 256; i++)
			{
				synchronized( inputStream )
				{
					b = readByte();
					if (b == IN_HEADER1)
					{
						b = readByte();
						if ( b == IN_HEADER2 ) return true;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false ;
	}
	
	public boolean waitForByte(byte target)
	{
		try {
			int b;
			for (int i = 0; i < 256; i++)
			{
				synchronized( inputStream )
				{
					b = readByte();
					if ( b == target )
					{
						return true ;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false ;
	}

	public boolean waitForAck()
	{
		waitForReplyHeader() ;
		return waitForByte(ACK_BYTE) ;
	}

	public boolean waitForAck(int msec)
	{
		waitForReplyHeader() ;
		return waitForByte(ACK_BYTE) ;
	}
	


	// useful commands
	
	public boolean ping()
	{
		if ( port == null ) { return false ; } 
		writeCommand(new byte[] {CMD_PING});
		return waitForAck() ;
	}

	public boolean beep()
	{
		if ( port == null ) { return false ; } 
		writeCommand(new byte[] {CMD_BEEP, (byte)0x00});
		return waitForAck() ;
	}

	public int _readSensor(int sensor, int mode)
	{
		int sensorVal = 0;

		if ( (sensor < 1) || (sensor > 8)) 
			throw new RuntimeException("Sensor number out of range: " + sensor);
		
		int b = CMD_READ_SENSOR | ( ( sensor - 1 ) << 2 ) | mode;
		
		try {
			writeCommand(new byte[] { (byte)b } );
			synchronized( inputStream )
			{
				waitForReplyHeader();
				sensorVal = readInt() << 8;
				sensorVal += readInt();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		return sensorVal;
	}

	
	public int readSensor(int sensor)
	{
		return _readSensor(sensor, SENSOR_READ_NORMAL);
	}

	public int readSensorMin(int sensor)
	{
		return _readSensor(sensor, SENSOR_READ_MIN);
	}

	public int readSensorMax(int sensor)
	{
		return _readSensor(sensor, SENSOR_READ_MAX);
	}


	public void talkToOutputPorts(int outputPortMask)
	{
		writeCommand( new byte[] { CMD_TALK_TO_OUTPUT_PORT,
								   (byte) outputPortMask } ) ;
		waitForAck();
	}


	public void setBurstMode(int sensorMask) {
		setBurstMode(sensorMask, BURST_SPEED_HIGH);
	}
	
	public void setBurstMode(int sensorMask, int speed)
	{
		writeCommand( new byte[] { ( (byte) ( CMD_SET_BURST_MODE | (byte) speed ) ),
								   (byte) sensorMask } ) ;
		waitForAck();
		burstModeMask = sensorMask;
	}

	public void startBurstReader(BurstCycleHandler handler)
	{
		burstReader = new BurstReader( this, handler );
		burstReader.start();
	}

	public void stopBurstReader()
	{
		
		burstReader.stopReading();
	}
	
	public int[] readBurstCycle()
	{

		try {

			int b;
			for (int i = 0; i < 256; i++)
			{
				synchronized ( inputStream )
				{
					b = peekByte();
					if ( b == BURST_CHUNK_HEADER )
					{
						// we got a burst cycle header, so read it to dump
						// it off the stream

						readByte();
						
						// grab the controller lock so none of the other
						// operations can get in our way.
						
						int high = readInt();
						int low = readInt();
						int sensor = ( high >> 5 ) + 1;
						int val = ( high & 0x03 ) <<  8;
						val += low;
						if ( sensor > 0 )
						{
							return new int[] { sensor, val };
						}
						else
						{
							// we got a bad read, return empty values
							return new int[] {};
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return new int[] {};
		}
		return new int[] {}; //we didn't see anything before our timeout
	}
		
	public void outputPortControl(byte cmd)
	{
		writeCommand(new byte[] { cmd });
		waitForAck();
	}

	public void outputPortOn()
	{
		outputPortControl(CMD_OUTPUT_PORT_ON);
	}

	public void outputPortOff()
	{
        outputPortControl(CMD_OUTPUT_PORT_OFF);
	}

	public void outputPortCoast()
	{
		outputPortControl(CMD_OUTPUT_PORT_COAST);
	}

	public void outputPortThatWay()
	{
		outputPortControl(CMD_OUTPUT_PORT_THATWAY);
	}

	public void outputPortThisWay()
	{
		outputPortControl(CMD_OUTPUT_PORT_THATWAY);
	}
	
	public void outputPortReverse()
	{
		outputPortControl(CMD_OUTPUT_PORT_RD);
	}

	public void setOutputPortPower( int level )
	{
		if ( ( level < 0 ) || ( level > 7 ) )
			throw new RuntimeException(
				"Power level out of range: " + level ) ;

		int comm = CMD_OUTPUT_PORT_POWER | level << 2 ;
		
		writeCommand( new byte[] { (byte)comm } ) ;

		waitForAck();
	}

	public void serialEvent(SerialPortEvent event) {
	
	}

	public interface BurstCycleHandler
	{
		void handleBurstCycle(int sensor, int value);
	}


	public class DefaultBurstCycleHandler
		implements BurstCycleHandler
	{
		private final int[] sensorValues = new int[8];
		synchronized public void handleBurstCycle(int sensor, int value)
		{
			System.out.println( "Sensor " + sensor + " value: " + value );
			sensorValues[sensor -1] = value;
		}
	}
	
	public class BurstReader extends Thread
	{
		GoGoController controller;
		BurstCycleHandler handler;
		boolean keepRunning = true;
		
		BurstReader(GoGoController cont, BurstCycleHandler handler)
		{
			this.controller = cont;
			this.handler = handler;
		}
		
		public void stopReading()
		{
			keepRunning = false;
		}
		
		public void run()
		{
			int[] result;
			while ( keepRunning )
			{
				result = controller.readBurstCycle();
				if ( result.length == 2 && handler != null ) {
					handler.handleBurstCycle(result[0], result[1]);
				}
			}
		}
	}


	// main for GoGoController class, which functions as a small utility


	public static void main(String[] args) 
		throws java.io.IOException
	{
		String port = null ;

		for( int i = 0 ; i < args.length ; i++ )
		{
			if( args[ i ].equals( "-l" ) )
			{
				ListIterator portIterator = serialPorts().listIterator() ;
				while (portIterator.hasNext()) {
					System.out.println( ( String ) portIterator.next() ) ;
				}
				System.exit( 0 ) ;
			}
			else if( args[ i ].equals( "-p" ) )
			{
				i++ ;
				port = args[ i ] ;
			}
		}
	}
	
}
