// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

public interface LabManagerInterface extends org.nlogo.window.Event.LinkChild, org.nlogo.api.ModelSections.Saveable {
  void show();

  String save();
}
