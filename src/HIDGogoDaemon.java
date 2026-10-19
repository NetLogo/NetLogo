package gogohid.daemon;

import org.hid4java.HidDevice;
import org.hid4java.HidException;
import org.hid4java.HidManager;
import org.hid4java.HidServices;
import org.hid4java.HidServicesListener;
import org.hid4java.event.HidServicesEvent;

public class HIDGogoDaemon implements HidServicesListener {

  private HidServices hidServices;
  private HidDevice gogoBoard;

  private void loadUpHIDServices() throws HidException  {
    // Get HID services and store
      hidServices = HidManager.getHidServices();
      hidServices.addHidServicesListener(this);

      // Provide a list of attached devices
      for (HidDevice hidDevice : hidServices.getAttachedHidDevices()) {
        System.err.println(hidDevice);
        if (hidDevice.getVendorId() == 0x461 && hidDevice.getProductId() == 0x20) {
          //store a gogo as gogo (limitation of current implementation -- only allow one gogo)
          gogoBoard = hidServices.getHidDevice(0x461, 0x20, null);
          System.err.println("GoGo board found, using this one: " + gogoBoard);
        }
      }
  }

  public void boot() {
    try {
      loadUpHIDServices();
    } catch (Exception e) {
      System.err.println("HID EXCEPTION");
      e.printStackTrace();
      throw new RuntimeException("Error in loading HID services");
    }

  }

  private void write(byte[] message) {
    if(gogoBoard != null) {
      gogoBoard.write(message, message.length, (byte)0);
    }
  }

  private void read(byte[] message) {
    if(gogoBoard != null) {
      gogoBoard.read(message, 200);
    }
  }

  private int getNumDevices() {
    int numDevices = 0;
    for (HidDevice hidDevice : hidServices.getAttachedHidDevices()) {
      if (hidDevice.getVendorId() == 0x461 && hidDevice.getProductId() == 0x20) {
        numDevices++;
      }
    }
    return numDevices;
  }

  //Attach and detach events
  @Override
  public void hidDeviceAttached(HidServicesEvent event) {
    System.err.println("Device attached: " + event);
      if (event.getHidDevice().getVendorId() == 0x461 &&
        event.getHidDevice().getProductId() == 0x20) {
        gogoBoard = hidServices.getHidDevice(0x461, 0x20, null);
      }
  }


  @Override
  public void hidDeviceDetached(HidServicesEvent event) {
    System.err.println("device detached");
    System.err.println(event.getHidDevice().toString());
    System.err.println(event.toString());
    if (event.getHidDevice().getVendorId() == 0x461 &&
        event.getHidDevice().getProductId() == 0x20) {
        gogoBoard = null;
      }
  }


  @Override
  public void hidFailure(HidServicesEvent arg0) {
    System.err.println("HID failure");
  }

  @Override
  public void hidDataReceived(HidServicesEvent event) {
    // We don't need to process any received data right now.
  }

  public static void main(String[] args) throws java.io.IOException {
    System.err.println("HID daemon started!");
    HIDGogoDaemon d = new HIDGogoDaemon();
    d.boot();
    boolean quit = false;
    while (!quit) {
      int c = System.in.read();
      if (c == 'S') {
        int numBytes = System.in.read();
        byte[] message = new byte[numBytes];
        System.in.read(message);
        d.write(message);
      } else if (c == 'R') {
        int numBytes = System.in.read();
        byte[] message = new byte[numBytes];
        d.read(message);
        System.out.write(message);
        System.out.flush();
      } else if (c == 'N') {
        System.out.write(d.getNumDevices());
        System.out.flush();
      } else if (c == 'X') {
        System.err.println("HID daemon quit code received.");
        quit = true;
      }
    }
    if (d != null && d.gogoBoard != null) {
      d.gogoBoard.close();
    }
    System.err.println("HID daemon run complete.");
  }

}
