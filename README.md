
# NetLogo Sound extension

This package contains the NetLogo sound extension.

## Building

Use the netlogo.jar.url environment variable to tell sbt which NetLogo.jar to compile against (defaults to NetLogo 5.3). For example:

    sbt -Dnetlogo.jar.url=file:///path/to/NetLogo/target/NetLogo.jar

If compilation succeeds, `sound.jar` will be created.

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

## Primitives

[`sound:drums`](#sounddrums)
[`sound:instruments`](#soundinstruments)
[`sound:play-drum`](#soundplay-drum)
[`sound:play-note`](#soundplay-note)
[`sound:play-note-later`](#soundplay-note-later)


### `sound:drums`

```NetLogo
sound:drums
```


Reports a list of the names of the [47 drums](#drum-names)
for use with `sound:play-drum`.



### `sound:instruments`

```NetLogo
sound:instruments
```


Reports a list of the names of the [128 instruments](#instrument-names)
for use with `sound:play-note`, `sound:play-note-later`,
`sound:start-note` and `sound:stop-note`.



### `sound:play-drum`

```NetLogo
sound:play-drum drum velocity
```


Plays a drum.

Example:

```NetLogo
sound:play-drum "ACOUSTIC SNARE" 64
```



### `sound:play-note`

```NetLogo
sound:play-note instrument keynumber velocity duration
```


Plays a note for a specified duration, in seconds. The agent does not
wait for the note to finish before continuing to next command.

```NetLogo
;; play a trumpet at middle C for two seconds
sound:play-note "TRUMPET" 60 64 2
```



### `sound:play-note-later`

```NetLogo
sound:play-note-later delay instrument keynumber velocity duration
```


Waits for the specified delay before playing the note for a specified
duration, in seconds. The agent does not wait for the note to finish
before continuing to next command.

Example:

```NetLogo
;; in one second, play a trumpet at middle C for two seconds
sound:play-note-later 1 "TRUMPET" 60 64 2
```



## Drum Names

```
35. Acoustic Bass Drum             59. Ride Cymbal 2
36. Bass Drum 1                    60. Hi Bongo
37. Side Stick                     61. Low Bongo
38. Acoustic Snare                 62. Mute Hi Conga
39. Hand Clap                      63. Open Hi Conga
40. Electric Snare                 64. Low Conga
41. Low Floor Tom                  65. Hi Timbale
42. Closed Hi Hat                  66. Low Timbale
43. Hi Floor Tom                   67. Hi Agogo
44. Pedal Hi Hat                   68. Low Agogo
45. Low Tom                        69. Cabasa
47. Open Hi Hat                    70. Maracas
47. Low Mid Tom                    71. Short Whistle
48. Hi Mid Tom                     72. Long Whistle
49. Crash Cymbal 1                 73. Short Guiro
50. Hi Tom                         74. Long Guiro
51. Ride Cymbal 1                  75. Claves
52. Chinese Cymbal                 76. Hi Wood Block
53. Ride Bell                      77. Low Wood Block
54. Tambourine                     78. Mute Cuica
55. Splash Cymbal                  79. Open Cuica
56. Cowbell                        80. Mute Triangle
57. Crash Cymbal 2                 81. Open Triangle
58. Vibraslap
```

## Instrument Names

```
*Piano*                              *Reed*
1. Acoustic Grand Piano            65. Soprano Sax
2. Bright Acoustic Piano           66. Alto Sax
3. Electric Grand Piano            67. Tenor Sax
4. Honky-tonk Piano                68. Baritone Sax
5. Electric Piano 1                69. Oboe
6. Electric Piano 2                70. English Horn
7. Harpsichord                     71. Bassoon
8. Clavi                           72. Clarinet

*Chromatic Percussion*               *Pipe*
9. Celesta                         73. Piccolo
10. Glockenspiel                   74. Flute
11. Music Box                      75. Recorder
12. Vibraphone                     76. Pan Flute
13. Marimba                        77. Blown Bottle
14. Xylophone                      78. Shakuhachi
15. Tubular Bells                  79. Whistle
16. Dulcimer                       80. Ocarina

*Organ*                              *Synth Lead*
17. Drawbar Organ                  81. Square Wave
18. Percussive Organ               82. Sawtooth Wave
19. Rock Organ                     83. Calliope
20. Church Organ                   84. Chiff
21. Reed Organ                     85. Charang
22. Accordion                      86. Voice
23. Harmonica                      87. Fifths
24. Tango Accordion                88. Bass and Lead

*Guitar*                             *Synth Pad*
25. Nylon String Guitar            89. New Age
26. Steel Acoustic Guitar          90. Warm
27. Jazz Electric Guitar           91. Polysynth
28. Clean Electric Guitar          92. Choir
29. Muted Electric Guitar          93. Bowed
30. Overdriven Guitar              94. Metal
31. Distortion Guitar              95. Halo
32. Guitar harmonics               96. Sweep

*Bass*                               *Synth Effects*
33. Acoustic Bass                  97. Rain
34. Fingered Electric Bass         98. Soundtrack
35. Picked Electric Bass           99. Crystal
36. Fretless Bass                  100. Atmosphere
37. Slap Bass 1                    101. Brightness
38. Slap Bass 2                    102. Goblins
39. Synth Bass 1                   103. Echoes
40. Synth Bass 2                   104. Sci-fi

*Strings*                            *Ethnic*
41. Violin                         105. Sitar
42. Viola                          106. Banjo
43. Cello                          107. Shamisen
44. Contrabass                     108. Koto
45. Tremolo Strings                109. Kalimba
47. Pizzicato Strings              110. Bag pipe
47. Orchestral Harp                111. Fiddle
48. Timpani                        112. Shanai

*Ensemble*                           *Percussive*
49. String Ensemble 1              113. Tinkle Bell
50. String Ensemble 2              114. Agogo
51. Synth Strings 1                115. Steel Drums
52. Synth Strings 2                116. Woodblock
53. Choir Aahs                     117. Taiko Drum
54. Voice Oohs                     118. Melodic Tom
55. Synth Voice                    119. Synth Drum
56. Orchestra Hit                  120. Reverse Cymbal

*Brass*                              *Sound Effects*
57. Trumpet                        121. Guitar Fret Noise
58. Trombone                       122. Breath Noise
59. Tuba                           123. Seashore
60. Muted Trumpet                  124. Bird Tweet
61. French Horn                    125. Telephone Ring
62. Brass Section                  126. Helicopter
63. Synth Brass 1                  127. Applause
64. Synth Brass 2                  128. Gunshot
```


## Terms of Use

[![CC0](http://i.creativecommons.org/p/zero/1.0/88x31.png)](http://creativecommons.org/publicdomain/zero/1.0/)

The NetLogo sound extension is in the public domain.  To the extent possible under law, Uri Wilensky has waived all copyright and related or neighboring rights.
