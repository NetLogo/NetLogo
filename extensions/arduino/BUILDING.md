It uses version 2.6.0 of the Java-Simple-Serial-Connector (JSSC) (https://code.google.com/p/java-simple-serial-connector/w/list usb-to-serial library).  The extension offers simple write functionality from NetLogo to the board, as well as the ability to read data that has been sent from the board to the computer and stored in a lookup table of name-value pairs.

## Building

In addition to the source, which expects to be compiled against a 5x. version of NetLogo and the JSSC library, this repo contains a ZIP file that can be unzipped and used without compilation.

The zip file also contains a sample NetLogo model and an Arduino Sketch that corresponds to it.

## Future improvements

Because I anticipate that most situations will use chatty Arduino sketches, there is an opportunity to support data smoothing.  This is not in place at the moment, but it should be an easy addition.

Also, the primitive `arduino:is-open?` can be misleading.  Its return indicates whether the port is open (from the computer's perspective) and whether it has not been closed explicitly.  If you pull the USB connection to the arduino, this will not be detected (at least not immediately).  So, a future improvement would be to do a more elaborate (i.e., acknowledged) ping that confirmed that communications were in fact occurring.  However, this would impose constraints & requirements on the arduino sketch, which I did not want to do.  Suggestions are welcome....

