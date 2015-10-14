// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui;

public interface ModelElementFigure {
  org.nlogo.sdm.ModelElement getModelElement();

  boolean dirty();
}
