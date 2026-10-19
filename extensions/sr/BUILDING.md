## Building

Run `sbt simpleR/package`.  If compilation succeeds, `sr.jar`
will be created in the `root-simple-r/` folder, and the required
dependencies will be copied there as well.  For testing, copy all the `jar` files and `rext.r` from the repository root to a `sr` directory inside your NetLogo `extensions` directory.  To package for release to the extensions library, run `simpleR/packageZip`.
