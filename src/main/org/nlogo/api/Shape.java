package org.nlogo.api;

// This interface exists so that org.nlogo.shape doesn't need to
// depend on org.nlogo.agent.

public interface Shape {
  String getName();

  void setName(String name);
}
