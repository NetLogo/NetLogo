// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

public interface LabManagerInterface
    extends Event.LinkChild {
  void show();

  String save();
}
