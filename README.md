
# NetLogo Vid Extension

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

## Concepts

### Video Source

The `vid` extension has a built-in concept of a video source.
At the moment, the only video sources available are movies in the directory the model lives in and cameras attached to the computer.
The `vid` extension opens a new video source with the `vid:<source>-open` and `vid:<source>-select`.
These primitives change the source to the selected source.
If a source is already open, it closes it before opening a new one.

### Source Lifecycle

Movie sources are "stopped" after being created by `vid:movie-select` or `vid:movie-open`.
Camera sources start off as "playing" after being created by `vid:camera-select` or `vid:camera-open`.
If a source is in status "stopped" it can be started with `vid:start`.
Conversely, if the source is "playing" it can be stopped with `vid:stop`.
When a source is "stopped", each call to `vid:capture-image` will return the same image.

### Video Recorder

The `vid` extension also has the concept of a recording, a series of frames which can be sewn into an "mp4" movie.
The recorder status can be queried using `vid:recorder-status`.
The recorder status is "inactive" until started with `vid:start-recorder`, which sets it to "recording".
While the recorder is "recording" the `vid:record-view`, `vid:record-interface`, and `vid:record-source` can be used to save frames to the recording.
You can choose to save the recording while recording using `vid:save-recording` which saves the movie to the specified file and reset the recording status to "inactive".
If you would prefer to throw away the recorded frames without saving, use `vid:reset-recorder`.

### Known Issues

When running the extension on macOS and using `vid:camera-open` or `vid:camera-select` and then starting a different program (like Zoom) and using the same camera will crash NetLogo.  If you start the other program first and then start NetLogo things should work better.

## Primitives

[`vid:camera-names`](#vidcamera-names)
[`vid:camera-open`](#vidcamera-open)
[`vid:camera-select`](#vidcamera-select)
[`vid:movie-select`](#vidmovie-select)
[`vid:movie-open`](#vidmovie-open)
[`vid:movie-open-remote`](#vidmovie-open-remote)
[`vid:close`](#vidclose)
[`vid:start`](#vidstart)
[`vid:stop`](#vidstop)
[`vid:status`](#vidstatus)
[`vid:capture-image`](#vidcapture-image)
[`vid:set-time`](#vidset-time)
[`vid:show-player`](#vidshow-player)
[`vid:hide-player`](#vidhide-player)
[`vid:record-view`](#vidrecord-view)
[`vid:record-interface`](#vidrecord-interface)
[`vid:record-source`](#vidrecord-source)
[`vid:recorder-status`](#vidrecorder-status)
[`vid:reset-recorder`](#vidreset-recorder)
[`vid:start-recorder`](#vidstart-recorder)
[`vid:save-recording`](#vidsave-recording)


### `vid:camera-names`


Provides a list of all available cameras.

Example:
```NetLogo
vid:camera-names => []
vid:camera-names => ["Mac Camera"]
vid:camera-names => ["Logitech Camera"]
```



### `vid:camera-open`


Opens the named camera as a video source.
If no name is provided, opens the first camera that would be listed by `camera-names`.

Example:
```NetLogo
vid:camera-open ; opens first camera
(vid:camera-open "Logitech Camera")
```

Errors:

* Message `"vid: no cameras found"`: no cameras are available.
* Message `"vid: camera "\<name\>" not found"`: if the named camera is not available.



### `vid:camera-select`


Prompts the user to select a camera as video source. This command does not error if the user cancels. Use `vid:status` to see if a user selected a camera.

Example:
```NetLogo
vid:camera-select
```

Errors:

* Message "vid: no cameras found": no cameras are available.



### `vid:movie-select`


Prompts the user to select a movie to use as a video source.
The formats supported are those [supported by JavaFX2](https://docs.oracle.com/javafx/2/api/javafx/scene/media/package-summary.html#SupportedMediaTypes).
This command does not error if the user cancels.
Use `vid:status` to see if the user selected a movie.

Example:

```NetLogo
vid:movie-select
```

Errors:

* Message `"vid: format not supported"`: the user selected a movie with an unsupported format.


### `vid:movie-open`


Opens a video from the file system.
If the provided path is not absolute the extension searches for the given path relative to the current model directory.
If the provided path is absolute the extension opens the file.

Example:

```NetLogo
vid:movie-open "foo.mp4"      ; Opens foo.mp4 in the directory containing the model
vid:movie-open user-file      ; Opens a dialog for the user to select a movie
vid:movie-open "/tmp/foo.mp4" ; Opens a movie from the "/tmp" directory
```

Errors:

* Message `"vid: no movie found"`: the movie could not be found.
* Message `"vid: format not supported"`: the user selected a movie with an unsupported format.



### `vid:movie-open-remote`


Opens a remote video from a website or ftp server.

Example:

```NetLogo
vid:movie-open-remote "http://example.org/foo.mp4"
```

Errors:

* Message `"vid: no movie found"`: The specified URL could not be loaded or errored while loading.
* Message `"vid: format not supported"`: The file type of the remote movie is not supported.
* Message `"vid: protocol not supported"`: The movie was at an unsupported URL protocol. Supported protocols are `ftp` and `http`.



### `vid:close`


Closes the currently selected video source.
Has no effect if there is no active video source.

Example:

```NetLogo
vid:close
```



### `vid:start`


Starts the selected video source.
A video source must have been selected before calling `vid:start`.

Example:

```NetLogo
vid:start
```

Errors:

* Message `"vid: no selected source"`: There is no currently selected video source. Select a source with `vid:movie-open`, `vid:movie-select`, `vid:camera-open`, or `vid:camera-select`.



### `vid:stop`


Stops the currently running video source.

Example:
```NetLogo
vid:stop
```



### `vid:status`


Reports the current status of an active video.
Note that after calling `vid:movie-open` or `vid:movie-select` the status will be set to "stopped",
while after calling `vid:camera-open` or `vid:camera-select` the status will be "playing".

Example:

```NetLogo
vid:status     ; => "inactive"

vid:movie-open "foobar.mp4"
vid:status      ; => "stopped"

vid:movie-start
vid:status       ; => "playing"
```



### `vid:capture-image`


Captures an image from the currently selected active source.

If width and height are not specified, the image is captured at the current source resolution.

Example:

```NetLogo
extensions [ vid bitmap ]

to capture
  ; capture an image if a video source is open,
  ; have the user select a camera if no video source found
  carefully [
    ; when camera open, take an image
    let image vid:capture-image ; returns image suitable for use with bitmap extension
    bitmap:copy-to-drawing image 0 0
  ] [
    if error-message = "Extension exception: vid: no selected source" [
      vid:camera-select
      vid:start
      let image vid:capture-image
      bitmap:copy-to-drawing image 0 0
    ]
  ]
end
```

If you want to capture images at a different resolution, simply replace `vid:capture-image` with, e.g., `(vid:capture-image 640 480)`.


Errors:

* Message `"vid: no selected source"`: There is no currently selected video source. Select a source with `vid:movie-open`, `vid:movie-select`, `vid:camera-open`, or `vid:camera-select`.
* Message `"vid: invalid dimensions"`: The selected dimensions are invalid (one of the dimensions is zero or negative).



### `vid:set-time`


Sets the time of the current video source to `*seconds*`.
This has no effect when the current video source is a camera.

Example:
```NetLogo
vid:set-time 100
```

Errors:

* Message `"vid: no selected source"`: There is no currently selected video source. Select a source with `vid:movie-open`, `vid:movie-select`, `vid:camera-open`, or `vid:camera-select`.
* Message `"vid: invalid time"`: The currently active video does not contain the specified second. The second may be negative, or greater than the length of the video.



### `vid:show-player`


Shows a player in a separate window.
If there is no video source, the window will be an empty black frame.
If there is an active video source, it will be displayed in the window with the specified width and height.
If there is a playing video source, it will be displayed in the window at its specified width and height.
If width and height are omitted, the video will be displayed in its native resolution.

Example with native resolution:

```NetLogo
vid:show-player
```

Example with custom resolution:

```NetLogo
(vid:show-player 640 480)
```

Errors:

* Message `"vid: invalid dimensions"`: The selected dimensions are invalid (one of the dimensions is zero or negative).



### `vid:hide-player`


Hides the player if open. Does nothing if there is no player window.

Example:

```NetLogo
vid:hide-player
```



### `vid:record-view`


Records the current image shown in the NetLogo view to the active recording.

Example:

```NetLogo
vid:record-view
```

Errors:

* Message `"vid: recorder not started"`: The recorder has not been started. Start the recorder with `vid:start-recorder`.



### `vid:record-interface`


Records the NetLogo interface view to the active recording.

Example:

```NetLogo
vid:record-interface
```

Errors:

* Message `"vid: recorder not started"`: The recorder has not been started. Start the recorder with `vid:start-recorder`.
* Message `"vid: export interface not supported"`: The calling NetLogo version does not support interface exports. This will occur when running NetLogo headlessly.



### `vid:record-source`


Records a frame to the active recording from the currently active source.

Example:

```NetLogo
vid:record-source
```

Errors:

* Message `"vid: recorder not started"`: The recorder has not been started. Start the recorder with `vid:start-recorder`.
* Message `"vid: no selected source"`: There is no currently selected video source. Select a source with `vid:movie-open`, `vid:movie-select`, `vid:camera-open`, or `vid:camera-select`.



### `vid:recorder-status`


Reports the current status of the recorder.
Initially and after the recorder is saved (via `vid:save-recording`) or reset (via `vid:reset-recorder`) the recorder status is "inactive".
After calling `vid:start-recorder` the status will be "recording".

Example:

```NetLogo
vid:recorder-status ; => "inactive"

vid:start-recorder
vid:recorder-status ; => "recording"

vid:reset-recorder
vid:recorder-status ; => "inactive"
```



### `vid:reset-recorder`

Stops the current recording, discards any recorded frames without saving them, and forgets the currently set recording resolution.


### `vid:start-recorder`


Starts the recorder.
If the recorder is already running this will cause an error to be raised.
If desired, a recording width and height can be supplied.
If width and height are not supplied, they will be determined from the first frame recorded.

Example:

```NetLogo
vid:start-recorder
(vid:start-recorder 640 480)
```

Errors:

* Message `"vid: recorder already started"`: The recorder has already been started. The existing recording should be saved or reset before starting the recording.
* Message `"vid: invalid dimensions"`: The selected dimensions are invalid (one of the dimensions is zero or negative).



### `vid:save-recording`


Saves the recording to the specified path.
If the recorder is not running this will cause an error to be raised.
Note that at present the recording will always be saved in the "mp4" format.
If the supplied filename does not end in ".mp4", the ".mp4" suffix will be added.
Note that `vid:save-recording` *will* overwrite existing files of the same name.
`vid:save-recording` will error if the recorder has not been started or if the file cannot be written since the containing directory does not exist.

Example:

```NetLogo
vid:save-recording "foo.mp4"      ; Saves to foo.mp4 in the directory containing the model
vid:save-recording user-new-file  ; Opens a dialog for the user to select a save path
vid:save-recording "/tmp/foo.mp4" ; Saves the recording to the "/tmp" directory
```

Errors:

* Message `"vid: recorder not started"`: The recorder has not been started. Start the recorder with `vid:start-recorder`.
* Message `"vid: no such directory"`: The directory containing the specified save file does not exist.
* Message `"vid: no frames recorded"`: You tried to save a recording with no frames recorded. Check that you are recording properly or use `vid:reset-recording` to to change the recording format without saving.



## Terms of Use

[![CC0](http://i.creativecommons.org/p/zero/1.0/88x31.png)](http://creativecommons.org/publicdomain/zero/1.0/)

The NetLogo Vid Extension is in the public domain.  To the extent possible under law, Uri Wilensky has waived all copyright and related or neighboring rights.

