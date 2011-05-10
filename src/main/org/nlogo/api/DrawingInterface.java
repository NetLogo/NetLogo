package org.nlogo.api;

public interface DrawingInterface {
  int[] colors();

  boolean isDirty();

  boolean isBlank();

  void markClean();

  void markDirty();

  int getWidth();

  int getHeight();
}
