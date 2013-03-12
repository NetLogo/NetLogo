// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mc;

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

