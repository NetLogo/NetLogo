## Building

Make sure your sbt is at least at version 0.13.6

Run `sbt package`.

If compilation succeeds, `py.jar` will be created and the required dependencies will be copied to the root of the repository.  Copy all the `jar` files and `pyext.py` from the repository root to a `py` directory inside your NetLogo `extensions` directory.

Run `sbt test` to run the NetLogo language test code from `tests.txt`.
