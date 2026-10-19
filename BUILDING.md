## Building

The extension is written in Scala (version 2.9.2).

Run `sbt package` to build the extension.

Unless they are already present, sbt will download the needed Jung and JGraphT jar files from the Internet.

If the build succeeds, `nw.jar` will be created. To use the extension, this file and all the other jars will need to be in the `extensions/nw` folder under your NetLogo installation.
