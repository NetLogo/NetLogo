package org.nlogo.modelingcommons;

import java.awt.image.BufferedImage;

/**
 * Created with IntelliJ IDEA.
 * User: Ben
 * Date: 1/15/13
 * Time: 5:16 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Image {
  public BufferedImage getImage() throws ImageException;
}