package org.nlogo.modelingcommons;

/**
 * Created with IntelliJ IDEA.
 * User: Ben
 * Date: 12/11/12
 * Time: 2:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class PreviewImageException extends Exception {
  public PreviewImageException() {
  }

  public PreviewImageException(String s) {
    super(s);
  }

  public PreviewImageException(String s, Throwable throwable) {
    super(s, throwable);
  }

  public PreviewImageException(Throwable throwable) {
    super(throwable);
  }
}
