// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mc;

public strictfp class Group {

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

