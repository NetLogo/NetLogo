## Using

For a first use without compiling code, do the following:

1. [Acquire the NetLogo software](http://ccl.northwestern.edu/netlogo/download.shtml). The Arduino extension comes pre-installed with NetLogo 5.2.1 and later.

2. Acquire an Arduino board and install the arduino IDE

3. Use the Arduino IDE to edit the Sketch (if desired) and send to the board. (See elaborate comments in the sketch for recommendations about what to comment out/leave in depending on your setup & circuit on the board.)

4. Once the Arduino has the sketch loaded on it, it will run that sketch whenever it is powered on.

5. Open the test "Arduino Example" model in the NetLogo Models library (it's in the "IABM Textbook" > "Chapter 8" folder)

6. Connect the Arduino to a USB port on the computer if it is not still connected from step 3.

7. Press OPEN to choose the port to communicate with and establish the connection.

8. Use the buttons to send byte commands; use the interface to inspect variable value(s) that your sketch is sending.

9. Note that by typing `arduino:primitives` you can get a list of the available commands in the extension.

### Notes

A NetLogo model using this extension must work in conjunction with an Arduino Sketch.  These two endpoints communicate by way of an application protocol that they define.  For example, if the NetLogo model sends a byte '1' over the wire this may mean something to the Arduino Sketch, which will respond accordingly.  The Arduino Sketch for its own part may send name-value pairs over the serial port, which then can be looked up asynchronously by the NetLogo model.

The modeler is free to build as simple or as complex an application protocol on top of this raw communication mechanism.

The asynchronous nature of the board-to-computer communications has one notable limitation.  If you choose to try to simulate a synchronous, BLOCKING READ communications pattern, (e.g., by sending a byte-based signal to the board, which triggers a response in a known name-value pair), then you are likely to be 'off by one' response.  That is, if you do the following in NetLogo code:

```NetLogo
arduino:write-byte b
show arduino:get "varname"
```

You are likely to get the value of `varname` from the PRIOR command represented by writing the byte `b`.  This is because the second line of NetLogo code will execute while the Arduino is off generating a new value for `varname`.

There are ways of getting around this (simulating a blocking interface by polling on a value to indicate fresh "news" on `varname`). But this extension works best in settings where the Arduino Sketch is "chatty" and the NetLogo model samples this stream when it needs data.

### Compatibility

This code has been tested on Windows 7 and 10 with 32-bit NetLogo and on Mac OS X.
You are likely to encounter issues when running this with 64-bit NetLogo in Windows 8 or Windows 10, so if you have Windows 8 or 10, please download the *32-Bit* version of NetLogo if you plan on using the Arduino extension.
We strive for cross-platform compatibility across Mac, Win, and Linux.
So if you have troubles, please let us know.

### Questions

If you run into problems or have questions about the extension, please email [ccl-feedback](mailto:ccl-feedback@ccl.northwestern.edu) or [cbrady@inquirelearning.com](mailto:cbrady@inquirelearning.com).
