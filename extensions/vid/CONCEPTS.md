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
