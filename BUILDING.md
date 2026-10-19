## Building

Build with `sbt package`. `sbt test` runs tests.

### Camera Capture Hardware Libraries

The Vid extension has some confusing dependency issues that require a little more elaboration due to the fact that it needs to access hardware through some kind of Java interop layer.

1. What is the `videoDeviceUtils` directory?

The Vid extension has historically use OpenImaj for video device detection, but OpenImaj doesn't provide support for Silicon Macs. Now that NetLogo is bundled separately for Intel and Silicon Macs (since 7.0.0), the dependency on OpenImaj has been replaced with custom native code for video device detection, which can be found in the `videoDeviceUtils` directory.

This code has been compiled into the necessary dynamic libraries, which are stored in `src/main/resources/lib` and shipped with the Vid extension. However, the `videoDeviceUtils` directory is included with the extension just in case the dynamic libraries need to be recompiled in the future.

2. What is that `sbt-javacpp` sbt plugin stuff?  Why is the `javacv` dep listed as `"org.bytedeco" % "javacv" % "1.5.7"` when the JavaCV project docs say to use `javacv-platform` like `"org.bytedeco" % "javacv-platform" % "1.5.7"`?

Sigh.  JavaCV uses a custom interop layer to get native hardware libraries loaded and usable in Java code.  That library is JavaCPP.  The way JavaCPP includes the native libraries (dll, so, or dylib) through Java app jars is with the idea of "platforms" for each library that has native dependencies.  So the `opencv` library has a corresponding `opencv-platform` library which depends on `opencv` as well as on all the platform-specific jars.  The "platform" library doesn't really have any code of its own.

So the problem is that JavaCPP and JavaCV build for many platforms, including iOS and Android, across many different architectures.  We only need macOS, Linux 32/64, and Windows 32/64.  The native library bundles are big, 20 megs each.  The full "naive" install of all the native libraries, most of which we don't need, is almost 1 gigabyte.  The JavaCPP devs recognize this problem, and provide the `sbt-javacpp` sbt plugin to manage the specific architectures used by a project.  Those settings are in the `build.sbt` like so:

```scala
javaCppVersion    :=  "1.5.7"
javaCppPresetLibs ++= Seq("opencv" -> "4.5.5", "openblas" -> "0.3.19")
javaCppPlatform   :=  Seq("windows-x86_64", "windows-x86", "macosx-arm64", "macosx-x86_64", "linux-x86", "linux-x86_64")
```

Note that we don't actually 100% need the plugin to do this.  We could do something like:

```scala
, "org.bytedeco" % "javacv-platform" % "1.5.7"
  // other exclusions clipped...
  exclude("org.bytedeco", "opencv-platform")
  exclude("org.bytedeco", "javacpp-platform")
  exclude("org.bytedeco", "ffmpeg-platform")
// now include the specific packages we need for each platform
, "org.bytedeco" % "opencv" % "4.5.5-1.5.7" classifier "windows-x86_64"
, "org.bytedeco" % "javacpp" % "4.5.5-1.5.7" classifier "windows-x86_64"
, "org.bytedeco" % "openblas" % "0.3.19-1.5.7" classifier "windows-x86_64"
```

But we'd have to write code to enumerate all the projects and architecture combinations, and keep them all up to date as things change.  So why bother if we already have an sbt plugin that will do it for us?  (The secret answer is because that sbt plugin might break in the future and we might not want to deal with updating it.)

### Compiling Video Device Detection Libraries

As mentioned above, there may come a time when the native libraries for video device detection will need to be recompiled and/or extended. This section provides more detailed information about how this should be done for each platform.

#### Windows

The relevant code for compiling the Windows native libraries can be found in `videoDeviceUtils/windows`. Note that the following steps must be performed on a machine with 64-bit architecture. To compile the libraries, you will need the latest version of Visual Studio with C++ development tools. The installer can be found at [this](https://visualstudio.microsoft.com/downloads/) link. Make sure to select the C++ development tools option when it prompts you to select which products you would like to install. Once you have the necessary tools installed, compiling the libraries is a two-step process. Before you begin, ensure that you have properly set the JAVA_HOME environment variable. Then, to compile for 64-bit architectures, run the "Developer Command Prompt for VS" shortcut, which should appear if you search for it in the Start Menu. Navigate to the directory mentioned above, then run `compile64`. Next, to compile for 32-bit architectures, run the "x86 Native Tools Command Prompt for VS" shortcut, which can be found in the same way. Navigate to the directory mentioned above, then run `compile32`. After performing these two compilation steps, the libraries will be output as `windows-amd64/videoDeviceUtils.dll` and `windows-x86/videoDeviceUtils.dll`, respectively. Replace the existing Windows libraries in `src/main/resources/lib` with these freshly compiled versions. Note that other build files with extensions such as .obj and .lib may be generated, but the only files you need to copy to `resources` are the .dll files.

#### Mac

The relevant code for compiling the Mac native libraries can be found in `videoDeviceUtils/macosx`. Note that the following steps must be performed on an Apple Silicon machine. To compile the libraries, you will need `g++` and `make`, which can be installed with `xcode-select --install`. Before you begin, ensure that you have properly set the JAVA_HOME environment variable. Then, navigate to the directory mentioned above, and run `make all`. This will build libraries for Silicon and Intel, which will be output as `macosx-aarch64/videoDeviceUtils.dylib` and `macosx-x86_64/videoDeviceUtils.dylib`, respectively. Replace the existing Mac libraries in `src/main/resources/lib` with these freshly compiled versions.

#### Linux

The relevant code for compiling the Linux native libraries can be found in `videoDeviceUtils/linux`. Note that the following steps must be performed on a machine with a 64-bit architecture. To compile the libraries, you will need `g++` and `make`, which can be installed with any standard package manager. Before you begin, ensure that you have installed both a 64-bit and 32-bit version of the JDK. The latter is usually identified by the `i386` suffix when using a package manager to install the JDK. Also ensure that you have added support for 32-bit architectures. For example, this can be done on Ubuntu with the command `sudo dpkg --add-architecture i386`. Once the necessary setup is complete, navigate to the directory mentioned above, and run `make all`. This will build libraries for 64-bit and 32-bit architectures, which will be output as `linux-amd64/videoDeviceUtils.so` and `linux-i386/videoDeviceUtils.so`, respectively. Replace the existing Linux libraries in `src/main/resources/lib` with these freshly compiled versions.
