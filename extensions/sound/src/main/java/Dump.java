package org.nlogo.extensions.sound;

/**
 * Returns debug information about the state of the synthesizer.
 */
public class Dump
    implements org.nlogo.api.Reporter {
  public String getAgentClassString() {
    return "OTP";
  }

  public org.nlogo.core.Syntax getSyntax() {
    return org.nlogo.core.SyntaxJ.reporterSyntax(
        org.nlogo.core.Syntax.StringType()
    );
  }

  public boolean getSwitchesBoolean() {
    return false;
  }

  public org.nlogo.api.Reporter newInstance(String name) {
    return new Dump();
  }

  public Object report(org.nlogo.api.Argument args[], org.nlogo.api.Context context) {
    return SoundExtension.dump();
  }
}
