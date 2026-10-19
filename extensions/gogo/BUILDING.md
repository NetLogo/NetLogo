## Building

Use the netlogo.jar.url environment variable to tell sbt which NetLogo.jar to compile against (defaults to most recent version of NetLogo). For example:

    sbt -Dnetlogo.jar.url=file:///path/to/NetLogo/target/NetLogo.jar package

If compilation succeeds, `gogo.jar` and `gogo-daemon.jar` will be created.
