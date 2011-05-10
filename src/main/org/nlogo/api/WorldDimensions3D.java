package org.nlogo.api;

// use this class to wrap up dimensions to resize the world 
// using WorldResizer

public final strictfp class WorldDimensions3D
    extends WorldDimensions {
  public int minPzcor;
  public int maxPzcor;

  public WorldDimensions3D(int minx, int maxx, int miny, int maxy, int minz, int maxz) {
    super(minx, maxx, miny, maxy);
    minPzcor = minz;
    maxPzcor = maxz;
  }
}
