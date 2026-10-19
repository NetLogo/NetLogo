# Resource Extension

This extension allows users to read resources (like CSV files and images) that are bundled within a `.nlogox` model.  It only allows you to *read* the resources as strings in NetLogo code; it cannot add or remove resources from the bundle.  For that, use the "File" > "Manage Bundled Resources" option within NetLogo.

## Primitives

  * `resource:list`
    * Lists all possible resource keys
  * `resource:get KEY`
    * Looks up the resource with keyname `KEY`.  If such a resource exists, it is returned as a string.  If not, throws an error.

## Building

Make sure you have `sbt` installed on your system, then run `sbt package` to package the Resource extension `.jar` file.  If the build succeeds, `resource.jar` is created.

Run `sbt packageZip` to create a zip file with all necessary files for publishing the extension.

## Terms of Use

[![CC0](http://i.creativecommons.org/p/zero/1.0/88x31.png)](http://creativecommons.org/publicdomain/zero/1.0/)

The NetLogo Resource extension is in the public domain.  To the extent possible under law, Uri Wilensky has waived all copyright and related or neighboring rights.
