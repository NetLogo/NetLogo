package org.nlogo.extensions.sound;

import org.nlogo.core.SyntaxJ;

/**
 * NetLogo command plays a note for a specified duration.
 */
public class PlayNote
    implements org.nlogo.api.Command {
  public String getAgentClassString() {
    return "OTP";
  }

  public org.nlogo.core.Syntax getSyntax() {
    int[] right =
        {
            org.nlogo.core.Syntax.StringType(),
            org.nlogo.core.Syntax.NumberType(),
            org.nlogo.core.Syntax.NumberType(),
            org.nlogo.core.Syntax.NumberType()
        };
    return SyntaxJ.commandSyntax(right);
  }

  public boolean getSwitchesBoolean() {
    return false;
  }

  public org.nlogo.api.Command newInstance(String name) {
    return new PlayNote();
  }


  public void perform(org.nlogo.api.Argument args[], org.nlogo.api.Context context)
      throws org.nlogo.api.ExtensionException, org.nlogo.api.LogoException {
    int instrument = SoundExtension.getInstrument(args[0].getString());
    int note = args[1].getIntValue();
    int velocity = args[2].getIntValue();

    // convert duration from seconds to milliseconds
    int duration = Math.round((float) args[3].getDoubleValue() * (float) 1000.0);

    SoundExtension.playNote(instrument, note, velocity, duration);
  }
}
