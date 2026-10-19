package org.nlogo.extensions.sound;

import org.nlogo.core.SyntaxJ;

/**
 * NetLogo command plays a drum.
 */
public class PlayDrum
    implements org.nlogo.api.Command {
  public String getAgentClassString() {
    return "OTP";
  }

  public org.nlogo.core.Syntax getSyntax() {
    int[] right =
        {
            org.nlogo.core.Syntax.StringType(),
            org.nlogo.core.Syntax.NumberType()
        };
    return SyntaxJ.commandSyntax(right);
  }

  public boolean getSwitchesBoolean() {
    return false;
  }

  public org.nlogo.api.Command newInstance(String name) {
    return new PlayDrum();
  }

  public void perform(org.nlogo.api.Argument args[], org.nlogo.api.Context context)
      throws org.nlogo.api.ExtensionException, org.nlogo.api.LogoException {
    int drum = SoundExtension.getDrum(args[0].getString());
    int velocity = args[1].getIntValue();
    SoundExtension.playDrum(drum, velocity);
  }
}
