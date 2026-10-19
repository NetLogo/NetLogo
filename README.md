# NetLogo sample Scala extension

This is a very small example NetLogo extension, written in Scala.

## Building

Make sure you have `sbt` installed on your system, then run `sbt package` to package the sample extension jar file.  If the build succeeds, `samplescala.jar` is created.

Run `sbt test` to run the NetLogo language tests from the `tests.txt` file.

Run `sbt packageZip` to create a zip file with all necessary files for publishing the extension.

## Terms of Use

[![CC0](http://i.creativecommons.org/p/zero/1.0/88x31.png)](http://creativecommons.org/publicdomain/zero/1.0/)

The NetLogo sample Scala extension is in the public domain.  To the extent possible under law, Uri Wilensky has waived all copyright and related or neighboring rights.
