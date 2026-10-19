## Using

The Sound Extension lets NetLogo models make two kinds of sounds:
MIDI sounds and playback of pre-recorded sound files.

The Java APIs used are `javax.sound.midi` and `java.applet.AudioClip`.

### How to Use

The sound extension comes preinstalled. To use the extension in your
model, add this at the top of your Code tab:
```
extensions [sound]
```

If your model already uses other extensions, then it already has an
`extensions` line in it, so just add `sound` to the list.

For more information on using NetLogo extensions,
see the [Extensions Guide](http://ccl.northwestern.edu/netlogo/docs/extensions.html)

For examples that use the sound extension, see the Sound section
under Code Examples in the NetLogo Models Library.

### MIDI support

The MIDI part of the extension simulates a 128-key electronic
keyboard with [47 drums](#drum-names) and [128 melodic instruments](#instrument-names), as provided by
<a href="https://www.midi.org/about-midi/gm/gm1_spec.shtml" target="_blank">General
MIDI Level 1 specification</a>.

It supports 15 polyphonic instrument channels and a single percussion
channel. Using more than 15 different melodic instruments
simultaneously in a model will cause some sounds to be lost or cut
off.

The pitch of a melodic instrument is specified by a key number. The
keys on the keyboard are numbered consecutively from 0 to 127, where
0 is the left-most key. Middle C is key number 60.

The loudness of an instrument is specified by a velocity, which
represents the force with which the keyboard key is depressed.
Velocity ranges from 0 to 127, where 64 is the standard velocity. A
higher velocity results in a louder sound.
