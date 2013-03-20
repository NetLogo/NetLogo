// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mc;

public strictfp class ImageException extends Exception {

  public ImageException() {}

  public ImageException(String s) {
    super(s);
  }

  public ImageException(String s, Throwable throwable) {
    super(s, throwable);
  }

  public ImageException(Throwable throwable) {
    super(throwable);
  }

}

