// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mc;

import java.awt.image.BufferedImage;

public interface Image {

  public BufferedImage getImage() throws ImageException;

}

