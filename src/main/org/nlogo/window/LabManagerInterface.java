// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

public interface LabManagerInterface
    extends org.nlogo.window.Event.LinkChild {
  void show();

  String save();
}
