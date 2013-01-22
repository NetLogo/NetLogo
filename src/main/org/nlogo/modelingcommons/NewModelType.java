package org.nlogo.modelingcommons;

/**
 * Created with IntelliJ IDEA.
 * User: Ben
 * Date: 1/15/13
 * Time: 5:35 PM
 * To change this template use File | Settings | File Templates.
 */
public enum NewModelType {
  NEW("new"),
  NEW_VERSION("overwrite"),
  CHILD("child");

  private String str;
  private NewModelType(String str) {
    this.str = str;
  }
  public String toString() {
    return str;
  }
  public NewModelType get(String str) {
    if(str.equals(NEW)) {
      return NEW;
    } else if(str.equals(NEW_VERSION)) {
      return NEW_VERSION;
    } else if(str.equals(CHILD)) {
      return CHILD;
    } else {
      return null;
    }
  }
}
