// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public strictfp class FileImage implements Image {

  private String filePath;

  public FileImage(String filePath) {
    this.filePath = filePath;
  }

  @Override
  public BufferedImage getImage() throws ImageException {
    if(filePath == null || filePath.length() == 0) {
      throw new ImageException("Image path cannot be blank");
    }
    File file = new File(filePath);
    BufferedImage image = null;
    try {
      return ImageIO.read(file);
    } catch(IOException ioException) {
      throw new ImageException("Invalid image file", ioException);
    }
  }

}

