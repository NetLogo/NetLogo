package org.nlogo.modelingcommons;

/**
 * Created with IntelliJ IDEA.
 * User: Ben
 * Date: 1/15/13
 * Time: 5:13 PM
 * To change this template use File | Settings | File Templates.
 */
public enum Sex {
  MALE("m"),
  FEMALE("f");

  private String str;
  private Sex(String str) {
    this.str = str;
  }
  public String toString() {
    return str;
  }
}
