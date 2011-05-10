package org.nlogo.api;

public interface World {
  double INFINITESIMAL = 3.2e-15;

  double patchSize();

  int worldWidth();

  int worldHeight();

  int minPxcor();

  int minPycor();

  int maxPxcor();

  int maxPycor();

  boolean wrappingAllowedInX();

  boolean wrappingAllowedInY();

  double wrap(double pos, double min, double max);

  double ticks();

  Observer observer();

  Patch getPatch(int i);

  Patch getPatchAt(double x, double y) throws AgentException;

  Patch fastGetPatchAt(int x, int y);

  int[] patchColors();

  boolean patchesAllBlack();

  int patchesWithLabels();

  double followOffsetX();

  double followOffsetY();

  double wrapX(double x) throws AgentException;

  double wrapY(double y) throws AgentException;

  AgentSet turtles();

  AgentSet patches();

  AgentSet links();

  Program program();

  ShapeList turtleShapeList();

  ShapeList linkShapeList();

  Object getDrawing();

  boolean sendPixels();

  void markDrawingClean();

  Protractor protractor();

  double wrappedObserverX(double x);

  double wrappedObserverY(double y);

  boolean patchColorsDirty();

  void markPatchColorsDirty();

  void markPatchColorsClean();

  int getVariablesArraySize(Link link, AgentSet breed);

  int getVariablesArraySize(Turtle turtle, AgentSet breed);

  String linksOwnNameAt(int i);

  String turtlesOwnNameAt(int i);

  String breedsOwnNameAt(AgentSet breed, int i);
}
