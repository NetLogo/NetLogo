package org.nlogo.modelingcommons;

/**
 * Created with IntelliJ IDEA.
 * User: Ben
 * Date: 1/15/13
 * Time: 5:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class Model {
  private String name;
  private int id;

  public Model(int id, String name) {
    this.id = id;
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public int getId() {
    return id;
  }
  public String toString() {
    return name;
  }
}