package org.nlogo.modelingcommons;

/**
 * Created with IntelliJ IDEA.
 * User: Ben
 * Date: 12/11/12
 * Time: 2:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class ImageException extends Exception {
  public ImageException() {
  }

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
