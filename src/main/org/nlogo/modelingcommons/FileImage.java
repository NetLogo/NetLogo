package org.nlogo.modelingcommons;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Ben
 * Date: 1/15/13
 * Time: 5:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileImage implements Image {
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
      image = ImageIO.read(file);
    } catch(IOException e) {
      throw new ImageException("Invalid image file");
    }
    return image;
  }
}
