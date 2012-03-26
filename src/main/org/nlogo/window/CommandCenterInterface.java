// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

public interface CommandCenterInterface {
  void repaintPrompt();

  void cycleAgentType(boolean forward);

  void requestFocus();
}
