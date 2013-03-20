// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mc;

public enum SexOfPerson {

  MALE("m"),
  FEMALE("f");

  private String str;

  private SexOfPerson(String str) {
    this.str = str;
  }

  public String toString() {
    return str;
  }

}

