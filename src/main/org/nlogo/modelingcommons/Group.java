package org.nlogo.modelingcommons;

/**
 * Created with IntelliJ IDEA.
 * User: Ben
 * Date: 1/15/13
 * Time: 5:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class Group {
  private int id;
  private String name;
  public Group(int id, String name) {
    this.id = id;
    this.name = name;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String toString() {
    return getName();
  }
}