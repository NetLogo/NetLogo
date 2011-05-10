package org.nlogo.api;

public interface Drawing3D {
  Iterable<DrawingLine3D> lines();

  Iterable<TurtleStamp3D> turtleStamps();

  Iterable<LinkStamp3D> linkStamps();
}
