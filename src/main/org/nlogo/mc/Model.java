// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mc;

public strictfp class Model {

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

