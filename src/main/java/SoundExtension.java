package org.nlogo.extensions.sound;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.MidiUnavailableException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.nlogo.api.ExtensionException;
import org.nlogo.api.ExtensionManager;

public class SoundExtension extends org.nlogo.api.DefaultClassManager {
  private boolean isHeadless = true;

  /**
   * Names of the drums. <p>
   * The drums on the MIDI percussion channel start at 35,
   * so the indexes in this array are off by -35.
   */
  static final java.util.List<String> DRUM_NAMES = java.util.Arrays.asList(
      new String[]
          {
              "ACOUSTIC BASS DRUM", "BASS DRUM 1", "SIDE STICK",
              "ACOUSTIC SNARE", "HAND CLAP", "ELECTRIC SNARE", "LOW FLOOR TOM",
              "CLOSED HI HAT", "HI FLOOR TOM", "PEDAL HI HAT", "LOW TOM",
              "OPEN HI HAT", "LOW MID TOM", "HI MID TOM", "CRASH CYMBAL 1",
              "HI TOM", "RIDE CYMBAL 1", "CHINESE CYMBAL", "RIDE BELL",
              "TAMBOURINE", "SPLASH CYMBAL", "COWBELL", "CRASH CYMBAL 2",
              "VIBRASLAP", "RIDE CYMBAL 2", "HI BONGO", "LOW BONGO",
              "MUTE HI CONGA", "OPEN HI CONGA", "LOW CONGA", "HI TIMBALE",
              "LOW TIMBALE", "HI AGOGO", "LOW AGOGO", "CABASA", "MARACAS",
              "SHORT WHISTLE", "LONG WHISTLE", "SHORT GUIRO", "LONG GUIRO",
              "CLAVES", "HI WOOD BLOCK", "LOW WOOD BLOCK", "MUTE CUICA",
              "OPEN CUICA", "MUTE TRIANGLE", "OPEN TRIANGLE"
          }
  );

  /**
   * Names of the instruments. <p>
   * According to the specification, MIDI instrument numbers start at 1, so the the
   * index of the instruments in this array should be off by -1. But it seems
   * that it's indexing from zero.
   */
  static final java.util.List<String> INSTRUMENT_NAMES = java.util.Arrays.asList(
      new String[]{
          "ACOUSTIC GRAND PIANO", "BRIGHT ACOUSTIC PIANO",
          "ELECTRIC GRAND PIANO", "HONKY-TONK PIANO", "ELECTRIC PIANO 1",
          "ELECTRIC PIANO 2", "HARPSICHORD", "CLAVI", "CELESTA", "GLOCKENSPIEL",
          "MUSIC BOX", "VIBRAPHONE", "MARIMBA", "XYLOPHONE", "TUBULAR BELLS",
          "DULCIMER", "DRAWBAR ORGAN", "PERCUSSIVE ORGAN", "ROCK ORGAN",
          "CHURCH ORGAN", "REED ORGAN", "ACCORDION", "HARMONICA",
          "TANGO ACCORDION", "NYLON STRING GUITAR",
          "STEEL ACOUSTIC GUITAR", "JAZZ ELECTRIC GUITAR",
          "CLEAN ELECTRIC GUITAR", "MUTED ELECTRIC GUITAR",
          "OVERDRIVEN GUITAR", "DISTORTION GUITAR", "GUITAR HARMONICS",
          "ACOUSTIC BASS", "FINGERED ELECTRIC BASS",
          "PICKED ELECTRIC BASS", "FRETLESS BASS", "SLAP BASS 1",
          "SLAP BASS 2", "SYNTH BASS 1", "SYNTH BASS 2", "VIOLIN",
          "VIOLA", "CELLO", "CONTRABASS", "TREMOLO STRINGS", "PIZZICATO STRINGS",
          "ORCHESTRAL HARP", "TIMPANI", "STRING ENSEMBLE 1",
          "STRING ENSEMBLE 2", "SYNTH STRINGS 1", "SYNTH STRINGS 2",
          "CHOIR AAHS", "VOICE OOHS", "SYNTH VOICE", "ORCHESTRA HIT",
          "TRUMPET", "TROMBONE", "TUBA", "MUTED TRUMPET", "FRENCH HORN",
          "BRASS SECTION", "SYNTH BRASS 1", "SYNTH BRASS 2",
          "SOPRANO SAX", "ALTO SAX", "TENOR SAX", "BARITONE SAX", "OBOE",
          "ENGLISH HORN", "BASSOON", "CLARINET", "PICCOLO", "FLUTE", "RECORDER",
          "PAN FLUTE", "BLOWN BOTTLE", "SHAKUHACHI", "WHISTLE", "OCARINA",
          "SQUARE WAVE", "SAWTOOTH WAVE", "CALLIOPE", "CHIFF", "CHARANG",
          "VOICE", "FIFTHS", "BASS AND LEAD", "NEW AGE", "WARM", "POLYSYNTH",
          "CHOIR", "BOWED", "METAL", "HALO", "SWEEP", "RAIN", "SOUNDTRACK",
          "CRYSTAL", "ATMOSPHERE", "BRIGHTNESS", "GOBLINS", "ECHOES", "SCI-FI",
          "SITAR", "BANJO", "SHAMISEN", "KOTO", "KALIMBA", "BAG PIPE", "FIDDLE",
          "SHANAI", "TINKLE BELL", "AGOGO", "STEEL DRUMS", "WOODBLOCK",
          "TAIKO DRUM", "MELODIC TOM", "SYNTH DRUM", "REVERSE CYMBAL",
          "GUITAR FRET NOISE", "BREATH NOISE", "SEASHORE", "BIRD TWEET",
          "TELEPHONE RING", "HELICOPTER", "APPLAUSE", "GUNSHOT"
      }
  );

  /**
   * The number of the MIDI percussion channel. *
   */
  private static final int PERCUSSION_CHANNEL = 9;

  private static javax.sound.midi.Synthesizer synth;
  private static javax.sound.midi.MidiChannel[] channels;

  /**
   * Next channel to allocate. *
   */
  private static int nextChannel = 0;

  /**
   * Registers extension primitives.
   */
  public void load(org.nlogo.api.PrimitiveManager primManager) {
    primManager.addPrimitive("drums", new ListDrums());
    primManager.addPrimitive("instruments", new ListInstruments());
    primManager.addPrimitive("loop-sound", new LoopSound());
    primManager.addPrimitive("play-drum", new PlayDrum());
    primManager.addPrimitive("play-note", new PlayNote());
    primManager.addPrimitive("play-note-later", new PlayNoteLater());
    primManager.addPrimitive("play-sound", new PlaySound());
    primManager.addPrimitive("play-sound-and-wait", new PlaySoundAndWait());
    primManager.addPrimitive("play-sound-later", new PlaySoundLater());
    primManager.addPrimitive("start-note", new StartNote());
    primManager.addPrimitive("stop-note", new StopNote());
    primManager.addPrimitive("stop-instrument", new StopInstrument());
    primManager.addPrimitive("stop-music", new StopMusic());
    primManager.addPrimitive("stop-sound", new StopSound());


    // undocumented primitive for debugging
    primManager.addPrimitive("__synth-dump", new Dump());
  }

  /**
   * Initializes this extension.
   */
  public void runOnce(org.nlogo.api.ExtensionManager em)
      throws org.nlogo.api.ExtensionException {
    isHeadless = !em.workspaceContext().workspaceGUI();

    if (!isHeadless) {
      try {
        // Open a synthesizer
        synth = javax.sound.midi.MidiSystem.getSynthesizer();
        synth.open();

        // Get array of synth channels
        channels = synth.getChannels();
        // initializing channels to 0 to fix bug #1027
        // channels have program=0 to begin with, but this additional initialization
        // seems to make them work properly for instrument 0 (acoustic grand piano)
        // when they arent initialized like this, then when inst 0 is used
        // the channel plays the wrong instrument.
        // maybe someday we can figure out why, but for now this is sufficient. -JC 7/2/10
        for (javax.sound.midi.MidiChannel ch : channels) {
          ch.programChange(0);
        }

        javax.sound.midi.Soundbank soundbank = synth.getDefaultSoundbank();

        if (soundbank == null) {
          try {
            java.io.InputStream soundbankStream = getClass().getClassLoader().getResourceAsStream("soundbank-min.gm");
            java.io.BufferedInputStream bufferedSoundbankStream = new java.io.BufferedInputStream(soundbankStream);
            soundbank = javax.sound.midi.MidiSystem.getSoundbank(bufferedSoundbankStream);
          } catch (java.io.IOException e) {
            throw new org.nlogo.api.ExtensionException("Failed to load soundbank: " + e.toString());
          } catch (javax.sound.midi.InvalidMidiDataException e) {
            throw new org.nlogo.api.ExtensionException("Failed to load soundbank: " + e.toString());
          }
        }

        boolean loaded = synth.loadAllInstruments(soundbank);
      } catch (MidiUnavailableException ex) {
        throw new ExtensionException("MIDI is not available.");
      }
    }
  }

  public void unload(ExtensionManager em) {
    if (synth != null) {
      synth.close();
    }
  }

  /**
   * Starts playing a note.
   *
   * @param instrument the MIDI patch number of the instrument
   * @param note       the MIDI note number, from 0 to 127 (60 = Middle C)
   * @param velocity   the speed with which the key was depressed
   */
  static void startNote(int instrument, int note, int velocity) {
    javax.sound.midi.MidiChannel channel = ensureChannel(instrument);

    if (channel != null) {
      channel.noteOn(note, velocity);
    }
  }

  /**
   * Stops playing a note.
   *
   * @param instrument the MIDI patch number of the instrument
   * @param note       the MIDI note number, from 0 to 127 (60 = Middle C)
   */
  static void stopNote(int instrument, int note) {
    javax.sound.midi.MidiChannel channel = getChannel(instrument);
    if (channel != null) {
      channel.noteOff(note);
    }
  }

  /**
   * Stops all notes of an instrument.
   *
   * @param instrument the MIDI patch number of the instrument
   */
  static void stopNotes(int instrument) {
    javax.sound.midi.MidiChannel channel = getChannel(instrument);
    if (channel != null) {
      channel.setMute(true);
      channel.setMute(false);
    }
  }

  /**
   * Stops all notes.
   */
  static void stopNotes() {
    if (channels != null && channels.length > 0) {
      // the api says stop messages are recevied by all channels
      channels[0].allNotesOff();
    }
  }

  /**
   * Plays a note for a specified duration.
   *
   * @param instrument the MIDI patch number of the instrument
   * @param note       the MIDI note number, from 0 to 127 (60 = Middle C)
   * @param velocity   the speed with which the key was depressed
   * @param duration   time in milliseconds before the note is stopped
   */
  static void playNote(int instrument, int note, int velocity, int duration) {
    javax.sound.midi.MidiChannel channel = ensureChannel(instrument);

    if (channel != null) {
      channel.noteOn(note, velocity);

      // start the stop thread
      if (duration > -1) {
        (new StopNoteThread(channel, note, duration)).start();
      }
    }
  }


  static void playNoteLater(int instrument, int note, int velocity, int duration, int delay) {
    javax.sound.midi.MidiChannel channel = ensureChannel(instrument);

    if (channel != null) {
      new PlayNoteThread(channel, note, velocity, duration, delay).start();
    }
  }

  /**
   * Hits a drum.
   *
   * @param drum     the key number of the drum.
   * @param velocity the speed at which the drum was hit, from 0 to 128
   */
  static void playDrum(int drum, int velocity) {
    if (channels != null && channels.length > 0) {
      channels[PERCUSSION_CHANNEL].noteOn(drum, velocity);
    }
  }


  /**
   * Starts a sample after a specified duration
   */

  private static class PlaySoundThread extends Thread implements LineListener {
    private File soundFile;
    private int delay;
    /**
     * Used by methods play and update to check whether playback has completed.
     */
    boolean playbackCompleted;

    PlaySoundThread(File soundFile) {
      super("PlaySoundThread");
      this.soundFile = soundFile;
      this.delay = 0;
    }

    PlaySoundThread(File soundFile, int delay) {
      super("PlaySoundThread");
      this.soundFile = soundFile;
      this.delay = delay;
    }

    /**
     * Play the audio file PlaySoundThread.soundFile, with delay
     * PlaySoundThread.delay before starting
     */
    void play() {
      Clip audioClip = null;
      try {
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
        AudioFormat format = audioStream.getFormat();
        DataLine.Info info = new DataLine.Info(Clip.class, format);
        audioClip = (Clip) AudioSystem.getLine(info);
        audioClip.addLineListener(this);

        audioClip.open(audioStream);

        if (delay > 0) {
          try {
            Thread.sleep(delay);
          } catch (InterruptedException e) {
              org.nlogo.api.Exceptions.ignore(e);
          }
        }

        audioClip.start();

        while (!playbackCompleted) {
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            org.nlogo.api.Exceptions.ignore(e);
          }
        }
      } catch (UnsupportedAudioFileException e) {
          org.nlogo.api.Exceptions.ignore(e);
      } catch (LineUnavailableException e) {
          org.nlogo.api.Exceptions.ignore(e);
      } catch (IOException e) {
          org.nlogo.api.Exceptions.ignore(e);
      } finally {
        if (audioClip != null) {
          audioClip.close();
        }
      }
    }

    /**
     * Listens to the the audio line events and detects when play is over.
     */
    @Override
    public void update(LineEvent event) {
      LineEvent.Type type = event.getType();
      if (type == LineEvent.Type.STOP) {
        playbackCompleted = true;
      }
    }

    public void run() {
      play();
    }
  }

  static void playSound(File file)
      throws org.nlogo.api.ExtensionException {
    new PlaySoundThread(file).start();
  }

  static void playSoundLater(File file, int delay)
      throws org.nlogo.api.ExtensionException {
    new PlaySoundThread(file, delay).start();
  }

  static void playSoundAndWait(File file)
      throws org.nlogo.api.ExtensionException {
        PlaySoundThread playSoundThread = new PlaySoundThread(file);
        playSoundThread.play();
  }

  static Clip currentClip = null;

  static void loopSound(File file)
      throws org.nlogo.api.ExtensionException {
        try {
          AudioInputStream audioInputStream =
          AudioSystem.getAudioInputStream(file);
          currentClip = AudioSystem.getClip();

          currentClip.open(audioInputStream);
          currentClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (UnsupportedAudioFileException e) {
          org.nlogo.api.Exceptions.ignore(e);
        } catch (LineUnavailableException e) {
          org.nlogo.api.Exceptions.ignore(e);
        } catch (IOException e) {
          org.nlogo.api.Exceptions.ignore(e);
        }
      }

  static void stopSound()
      throws org.nlogo.api.ExtensionException {
    if (currentClip != null) {
      currentClip.stop();
      currentClip = null;
    }
  }


  /**
   * Finds the MIDI patch number of an instrument by name.
   *
   * @return index for the instrument
   * @throws org.nlogo.api.ExtensionException
   *          if the instrument does not exist
   */
  static int getInstrument(String name)
      throws org.nlogo.api.ExtensionException {
    name = name.toUpperCase().intern();

    int instrument = INSTRUMENT_NAMES.indexOf(name);

    if (instrument != -1) {
      return instrument;
    } else {
      throw new org.nlogo.api.ExtensionException
          ("No such instrument: " + name);
    }
  }

  /**
   * Finds the MIDI note number of a drum sound by name.
   *
   * @throws org.nlogo.api.ExtensionException
   *          if the instrument does not exist
   */
  static int getDrum(String name)
      throws org.nlogo.api.ExtensionException {
    name = name.toUpperCase().intern();
    int drum = DRUM_NAMES.indexOf(name);

    if (drum != -1) {
      return drum + 35;
    } else {
      throw new org.nlogo.api.ExtensionException
          ("No such drum: " + name);
    }
  }

  /**
   * Debugging output shows channel allocations.
   */
  static String dump() {
    if (channels == null) {
      return "";
    }

    StringBuilder s = new StringBuilder();

    for (int j = 0; j < channels.length; j++) {
      String name;
      int instrument = channels[j].getProgram();
      name = (instrument > -1)
          ? ("" + instrument + ". " + INSTRUMENT_NAMES.get(instrument))
          : "N/A";

      if (j == PERCUSSION_CHANNEL) {
        name = name + " <PERCUSSION>";
      }
      s.append("" + j + "\t" + name + "\n");
    }

    return s.toString();
  }

  /**
   * Returns the channel playing this instrument,
   * -1 if no channel is playing it.
   */
  private static javax.sound.midi.MidiChannel getChannel(int instrument) {
    if (channels == null) {
      return null;
    }

    for (int j = 0; j < channels.length; j++) {
      if (j != PERCUSSION_CHANNEL && channels[j].getProgram() == instrument) {
        return channels[j];
      }
    }
    return null;
  }

  /**
   * Returns a channel playing this instrument.
   * If no channel was playing it, allocates a new one.
   */
  private static javax.sound.midi.MidiChannel ensureChannel(int instrument) {
    javax.sound.midi.MidiChannel channel = getChannel(instrument);

    if (channel != null || channels == null || channels.length == 0) {
      return channel;
    }

    channel = channels[nextChannel];
    channel.allNotesOff();
    channel.programChange(instrument);

    // figure out the next channel to allocate
    nextChannel++;
    if (nextChannel == PERCUSSION_CHANNEL) nextChannel++;
    if (!(nextChannel < channels.length)) nextChannel = 0;

    return channel;
  }


  /**
   * Starts a note after a specified duration
   */

  private static class PlayNoteThread extends Thread {
    private javax.sound.midi.MidiChannel channel;
    private int note, velocity, duration, delay;

    PlayNoteThread(javax.sound.midi.MidiChannel channel, int note, int velocity, int duration, int delay) {
      super("PlayNoteThread");
      this.channel = channel;
      this.note = note;
      this.velocity = velocity;
      this.duration = duration;
      this.delay = delay;
    }

    public void run() {
      try {
        Thread.sleep(delay);
      } catch (InterruptedException e) {
        org.nlogo.api.Exceptions.ignore(e);
      }
      channel.noteOn(note, velocity);

      // start the stop thread
      if (duration > -1) {
        (new StopNoteThread(channel, note, duration)).start();
      }
    }
  }


  /**
   * Stops a note after a specified duration
   */
  private static class StopNoteThread extends Thread {
    private javax.sound.midi.MidiChannel channel;
    private int note, duration;

    StopNoteThread(javax.sound.midi.MidiChannel channel, int note, int duration) {
      super("StopNoteThread");
      this.channel = channel;
      this.note = note;
      this.duration = duration;
    }

    public void run() {
      try {
        Thread.sleep(duration);
      } catch (InterruptedException e) {
        org.nlogo.api.Exceptions.ignore(e);
      }
      channel.noteOff(note);
    }
  }

}
