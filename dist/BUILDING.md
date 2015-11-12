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
* NetLogo Logging
* HubNet Client

Additionally, each platform has an aggregate build which pulls all of the sub applications together.
This makes it convenient to install 4 different applications (as opposed to installing each of them individually.

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

### Platform-specific Prerequisites

* Use Cygwin to build on windows

## Conventions

Shared platform configuration files are stored in `configuration/shared/[platform]/`.
Shared platform resources are stored in `configuration/shared/[platform]/image`.
Application specific configuration files are stored in `configuration/[appName]/[platform]/`.
Application specific resources are stored in `configuration/[appName]/[platform]/image`.
Configuration files may be mustache templates, which get rendered with variables like `appName` and `version`.

Finished build images are written to `target/[appName]/[platform]/target/bundles/`.
Intermediate files are left at `target/[appName]/[platform]` for inspection and debugging.

##


