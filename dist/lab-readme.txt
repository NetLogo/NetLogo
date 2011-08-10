COPYRIGHT AND LICENSE

Copyright 2009-2011 by Uri Wilensky. All rights reserved.

BehaviorSpace is free and open source software. You can redistribute
it and/or modify it under the terms of the GNU Lesser General Public
License (LGPL) as published by the Free Software Foundation, either
version 3 of the License, or (at your option) any later version.

A copy of the LGPL is included in the NetLogo distribution. See also
http://www.gnu.org/licenses/ .


ABOUT THE SOURCE CODE

BehaviorSpace is written in the Scala programming language. Scala code
compiles to Java byte code and is fully interoperable with Java and
other JVM languages.

The code is divided into two packages.  The core code is in
org.nlogo.lab, where the main class is Lab, and the GUI is in
org.nlogo.lab.gui, where the main class is LabManager.
Nothing in lab depends on anything in lab.gui.

Here's how the classes in org.nlogo.lab interrelate:

- The specification for how to run an experiment is called a Protocol.
  In the GUI, this is called an "experiment setup".  Among other
  things, a Protocol may contain one or more ValueSets; each ValueSet
  specifies what series or range of values a particular variable
  should take.

- The class that actually runs the experiment, Worker, makes Runner
  objects, one per model run, and runs them using a thread pool.
  Worker and its Runners notify a ProgressListener of how things are
  going.  Off in the org.nlogo.lab.gui package, ProgressDialog
  implements this interface and displays the progress in the GUI.

- TableExporter and SpreadsheetExporter are also ProgressListeners.  A
  listener can hang on to the generated results if it wants; that's
  how SpreadsheetExporter writes all the data out at once at the end.

- Also off in the org.nlogo.lab.gui package, there exists a Supervisor
  thread.  Supervisor takes care of creating a Worker and reporting
  errors to the user in the GUI.

- When running headless, headless.Main uses lab.Lab, which knows how
  to use ProtocolLoader to load experiment setups from the model file
  or a separate XML file.

- Protocols can be written back to disk in XML format by
  ProtocolSaver.

Feel free to write us at feedback@ccl.northwestern.edu with any
questions, comments, or concerns about the source code.
User-contributed code is welcome.


HOW TO BUILD

Make sure you are using Scala 2.9.1.

The build script expects to find the unzipped Scala distribution
directory at /usr/local/scala-2.9.1.  If you have it in another
location, edit the build script to point the location you are using.

Unjar the sources jar.  cd into the resulting BehaviorSpace directory
and run the included shell script, build.sh.

If the build succeeds, a new BehaviorSpace.jar will be generated.  To
test the jar, copy it into NetLogo's lib directory, overwriting the
old BehaviorSpace.jar, and then run NetLogo (in GUI or headless mode).

After building BehaviorSpace.jar, you can run the ScalaTest unit tests
with the included test.sh script.  The tests run using the
BehaviorSpace.jar in the current directory, not the one in the parent
lib directory.  Some supporting test files are in the test directory.
