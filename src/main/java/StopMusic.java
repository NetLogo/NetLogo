package org.nlogo.extensions.sound;

/**
 * NetLogo command stops all notes.
 */
public class StopMusic
    implements org.nlogo.api.Command {
  public String getAgentClassString() {
    return "OTP";
  }

  public org.nlogo.core.Syntax getSyntax() {
    return org.nlogo.core.SyntaxJ.commandSyntax();
  }

  public boolean getSwitchesBoolean() {
    return false;
  }

  public org.nlogo.api.Command newInstance(String name) {
    return new StopMusic();
  }

  public void perform(org.nlogo.api.Argument args[], org.nlogo.api.Context context)
      throws org.nlogo.api.ExtensionException, org.nlogo.api.LogoException {
    SoundExtension.stopNotes();
  }
}
