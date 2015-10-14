// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

// any class that implements this needs to be a Widget

public interface ViewWidgetInterface {
  Widget asWidget();  // this should just return the object itself

  int getAdditionalHeight();
}
