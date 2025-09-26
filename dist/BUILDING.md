# Building NetLogo for Release

## General Build Structure

The NetLogo build is a matrix across platforms and products (SubApplications). Most builds are similar with a few minor differences.

Platforms:

* Windows
* Mac OS X
* Linux

Products:

* NetLogo
* NetLogo 3D
* HubNet Client

Additionally, each platform has an aggregate build which pulls all of the sub applications together.
This makes it convenient to install 3 different applications (as opposed to installing each of them individually.

### Step-by-step

1. Ensure all submodules are up-to-date with `git submodule update --init`
2. Copy QTJava into the qtj extension
3. Ensure your active java is set as java 8
4. Build the core components of the build

```
> buildNetLogo
# ...
```

5. Generate each of the 5 installers
  * Mac OS X
```
sbt
> packageMacAggregate
# ...
```
  * Linux
```
sbt
> packageLinuxAggregate <your-32-bit-jdk-version>-32
# ...
> packageLinuxAggregate <your-64-bit-jdk-version>-64
# ...
```
  * Windows
```
sbt
> packageWinAggregate <your-32-bit-jdk-version>-32
# ...
> packageWinAggregate <your-64-bit-jdk-version>-64
# ...
```
Note that the installers can also be copied to the `target/downloadPages` folder if generated on another machine.

6. Generate and upload the website
```
sbt
> downloadPages
# ...
> uploadWebsite
# ...
```

### `sbt` task

Use the `packageApp` task with one of `mac`, `linux`, `win` followed by one of the above Product Names. Examples:

```sbt
packageApp mac NetLogo 3D
packageApp win HubNet Client
packageApp linux NetLogo
```

## Prerequisites

* sbt
* java 1.6
* java 1.8
* `javapackager` on the `PATH` pointing to a the `javapackager` executable in java 1.8.
   The build may or may not work with java 8 below u60, due to the fact that `javapackager` remains under active development.
* `pandoc` on the `PATH`
* `wkhtmltopdf` on the `PATH` ([website](http://wkhtmltopdf.org/))

## Conventions

Shared platform configuration files are stored in `configuration/shared/[platform]/`.
Shared platform resources are stored in `configuration/shared/[platform]/image`.
Application specific configuration files are stored in `configuration/[appName]/[platform]/`.
Application specific resources are stored in `configuration/[appName]/[platform]`.
Resources specific to the aggregate build in a particular platform are stored in `configuration/[aggregate]/[platform]`
Configuration files may be mustache templates, which get rendered with variables like `appName` and `version`.

Finished build images are written to `target/[appName]/[platform]/target/bundles/`.
Intermediate files are left at `target/[appName]/[platform]` for inspection and debugging.

### Notes on Mac Installer

If you wish to package from a mac, you *must* have the NetLogo Developer ID keys (Robert or Jason should have copies).

Apple forbids symbolic links in packages which point to paths other than (a) those within the package or (b) those in system directories.
Signing will succeed, but the app will fail to validate at runtime.
In order to overcome this limitation, the following system properties were added and are used only on Mac:

* `netlogo.docs.dir`
* `netlogo.extensions.dir`
* `netlogo.models.dir`

Apple determines the appropriate application to use to open a file via the Info.plist's `CFBundleDocumentTypes`.
While this method appears to be on its way out in favor of UTI [see this blog for more information](https://www.cocoanetics.com/2012/09/fun-with-uti/), we're continuing to use it for the time being because it works.

Finally, the `lib` and `natives` folders reside in each app precisely because of the problems with symlinking.
They are duplicated and future refinements to package signing requirements by Apple *could* provide a way to reduce that duplication.

### Notes on Windows Installer

The Windows installer is generated using the WiX toolset.
WiX uses a number of different file types, here's a quick overview of what the hand-written files in the project do.

* `NetLogo.wxs`: This file specifies how the entire package is put together. Anyone trying to understand how the NetLogo msi is put together should *start* by looking at this file.
* `ElementNamer.xsl`: This is used to transform the raw output of the `heat` WiX tool by setting the ID of each file and component to it's name (with some xml-escaping).
* `NetLogoUI.wxs`: Provides the general flow of UI during the installation. Based on the Wix "InstallDir" UI.
* `ShortcutDialog.wxs`: Provides a particular dialog for selecting whether to install desktop shortcuts and under what name to install start menu shortcuts.
* `NetLogoTranslation.wxl`: Provides text for the ShortcutDialog.

Note that a 32-bit `javapackager` will give a 32-bit bundled JRE and a 64-bit `javapackager` will give a 64-bit bundled JRE.

## Documentation

NetLogo contains a substantial amount of documentation.
This is a *brief* look at how that documentation is generated.

First, you will notice the `docs` directory contains a number of resources which form the core of the NetLogo documentation.
Those resources are templated and copied up to `docs` in the NetLogo root for use with NetLogo.
If you're using NetLogo and can't access the docs, it's because you haven't yet generated them.
One interesting "twist" here is that the info tab documentation is pulled partly from a file in the models library (the Info Tab Example model).
This requires running markdown on the info component of that file (done with `InfoTabGenerator`).

Secondly, the NetLogo dictionary is split up into one file per primitive heading and these files are placed into the `docs/dict` folder of the NetLogo root.
These files have various paths rewritten to make them point to the correct document.
The `PrimIndex` process writes primitive indices which is used by NetLogo to locate the particular file for a given primitive.

Finally, the process is done again with slightly different variables for generating a pdf with `wkhtmltopdf`.
This PDF is placed in the NetLogo root.
