// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

import org.nlogo.core.AgentKindJ;
import org.nlogo.api.AgentException;
import org.nlogo.api.Color;
import org.nlogo.api.ImporterUser;
import org.nlogo.core.Breed;
import org.nlogo.core.Program;
import org.nlogo.api.WorldDimensionException;
import org.nlogo.core.WorldDimensions;
import org.nlogo.api.WorldDimensions3D;

import java.util.Arrays;
import java.util.Iterator;

public final strictfp class World3D
    extends World
    implements org.nlogo.api.World3D {

  Drawing3D drawing;

  public org.nlogo.api.Protractor3D protractor3D() {
    return (org.nlogo.api.Protractor3D) _protractor;
  }

  public World3D() {
    linkManager = new LinkManager3D(this);
    tieManager = new TieManager3D(this, linkManager);
    drawing = new Drawing3D(this);
    inRadiusOrCone = new InRadiusOrCone3D(this);
    _protractor = new Protractor3D(this);
    mayHavePartiallyTransparentObjects = false;
  }

  @Override
  Observer createObserver() {
    return new Observer3D(this);
  }

  @Override
  public void changeTopology(boolean xWrapping, boolean yWrapping) {
    topology = new Torus3D(this);
  }

  public void changeTopology(boolean xWrapping, boolean yWrapping, boolean zWrapping) {
    topology = new Torus3D(this);
  }

  public double shortestPathZ(double z1, double z2) {
    return ((Topology3D) topology).shortestPathZ(z1, z2);
  }

  public boolean wrappingAllowedInZ() {
    return true;
  }

  public double wrappedObserverZ(double z) {
    z = ((Topology3D) topology).wrapZ(z - followOffsetZ());

    return z;
  }

  public double followOffsetZ() {
    return ((Observer3D) _observer).followOffsetZ();
  }

  @Override
  public void diffuse4(double param, int vn) {
    throw new UnsupportedOperationException();
  }

  int _worldDepth;

  public int worldDepth() {
    return _worldDepth;
  }

  int _maxPzcor;

  public int maxPzcor() {
    return _maxPzcor;
  }

  int _minPzcor;

  public int minPzcor() {
    return _minPzcor;
  }

  Double _minPzcorBoxed;

  public Double minPzcorBoxed() {
    return _minPzcorBoxed;
  }

  Double _maxPzcorBoxed;

  public Double maxPzcorBoxed() {
    return _maxPzcorBoxed;
  }

  Double _worldDepthBoxed;

  public Double worldDepthBoxed() {
    return _worldDepthBoxed;
  }

  public double wrapZ(double z) {
    return Topology.wrap(z, _minPzcor - 0.5, _maxPzcor + 0.5);
  }

  public int roundZ(double z) {
    // floor() is slow so we don't use it
    z = ((Topology3D) topology).wrapZ(z);

    if (z > 0) {
      return (int) (z + 0.5);
    } else {
      int intPart = (int) z;
      double fractPart = intPart - z;
      return (fractPart > 0.5) ? intPart - 1 : intPart;
    }
  }

  // this procedure is the same as calling getPatchAt when the topology is a torus
  // meaning it will override the Topology's wrapping rules and
  public Patch getPatchAtWrap(double x, double y, double z) {
    int xc, yc, zc, intPart;
    double fractPart;
    x = Topology.wrap(x, _minPxcor - 0.5, _maxPxcor + 0.5);
    y = Topology.wrap(y, _minPycor - 0.5, _maxPycor + 0.5);
    z = Topology.wrap(z, _minPzcor - 0.5, _maxPzcor + 0.5);

    if (x > 0) {
      xc = (int) (x + 0.5);
    } else {
      intPart = (int) x;
      fractPart = intPart - x;
      xc = (fractPart > 0.5) ? intPart - 1 : intPart;
    }
    if (y > 0) {
      yc = (int) (y + 0.5);
    } else {
      intPart = (int) y;
      fractPart = intPart - y;
      yc = (fractPart > 0.5) ? intPart - 1 : intPart;
    }
    if (z > 0) {
      zc = (int) (z + 0.5);
    } else {
      intPart = (int) z;
      fractPart = intPart - z;
      zc = (fractPart > 0.5) ? intPart - 1 : intPart;
    }
    int patchid = ((_worldWidth * _worldHeight * (_maxPzcor - zc)) +
        (_worldWidth * (_maxPycor - yc))
        + xc - _minPxcor);


    return (Patch) _patches.toArray()[patchid];
  }

  public boolean validPatchCoordinates(int xc, int yc, int zc) {
    return
        xc >= _minPxcor &&
            xc <= _maxPxcor &&
            yc >= _minPycor &&
            yc <= _maxPycor &&
            zc >= _minPzcor &&
            zc <= _maxPzcor;
  }

  public Patch fastGetPatchAt(int xc, int yc, int zc) {
    return (Patch) _patches.toArray()[((_worldWidth * _worldHeight * (_maxPzcor - zc)) +
        (_worldWidth * (_maxPycor - yc))
        + xc - _minPxcor)];
  }

  @Override
  public Patch fastGetPatchAt(int xc, int yc) {
    return fastGetPatchAt(xc, yc, 0);
  }

  @Override
  public void createPatches(WorldDimensions dim) {
    WorldDimensions3D d = (WorldDimensions3D) dim;
    createPatches(dim.minPxcor(), dim.maxPxcor(),
                  dim.minPycor(), dim.maxPycor(),
                  d.minPzcor(), d.maxPzcor());
  }

  @Override
  public void createPatches(int minPxcor, int maxPxcor,
                            int minPycor, int maxPycor) {
    createPatches(minPxcor, maxPxcor, minPycor, maxPycor, 0, 0);
  }

  @Override
  public Program newProgram() {
    return org.nlogo.core.Program$.MODULE$.fromDialect(org.nlogo.api.NetLogoThreeDDialect$.MODULE$);
  }

  @Override
  public Program newProgram(scala.collection.Seq<String> interfaceGlobals) {
    Program emptyProgram = newProgram();
    return emptyProgram.copy(
        interfaceGlobals,
        emptyProgram.userGlobals(),
        emptyProgram.turtleVars(),
        emptyProgram.patchVars(),
        emptyProgram.linkVars(),
        emptyProgram.breeds(),
        emptyProgram.linkBreeds(),
        emptyProgram.dialect());
  }

  public void createPatches(int minPxcor, int maxPxcor,
                            int minPycor, int maxPycor,
                            int minPzcor, int maxPzcor) {
    patchScratch = null;
    patchScratch3d = null;
    _minPxcor = minPxcor;
    _maxPxcor = maxPxcor;
    _minPycor = minPycor;
    _maxPycor = maxPycor;
    _minPzcor = minPzcor;
    _maxPzcor = maxPzcor;
    _worldWidth = _maxPxcor - _minPxcor + 1;
    _worldHeight = _maxPycor - _minPycor + 1;
    _worldDepth = _maxPzcor - _minPzcor + 1;

    rootsTable = new RootsTable(_worldWidth, _worldHeight);

    _worldWidthBoxed = Double.valueOf(_worldWidth);
    _worldHeightBoxed = Double.valueOf(_worldHeight);
    _worldDepthBoxed = Double.valueOf(_worldDepth);
    _minPxcorBoxed = Double.valueOf(_minPxcor);
    _minPycorBoxed = Double.valueOf(_minPycor);
    _minPzcorBoxed = Double.valueOf(_minPzcor);
    _maxPxcorBoxed = Double.valueOf(_maxPxcor);
    _maxPycorBoxed = Double.valueOf(_maxPycor);
    _maxPzcorBoxed = Double.valueOf(_maxPzcor);


    breeds.clear();

    scala.collection.Iterator<scala.Tuple2<String, Breed>> breedIterator =
      program().breeds().iterator();

    while (breedIterator.hasNext()) {
      scala.Tuple2<String, Breed> b = breedIterator.next();
      AgentSet agentset = new TreeAgentSet(AgentKindJ.Turtle(), b._2.name());
      breeds.put(b._1.toUpperCase(), agentset);
    }

    if (_turtles != null) _turtles.clear(); // so a SimpleChangeEvent is published
    _turtles = new TreeAgentSet(AgentKindJ.Turtle(), "TURTLES");
    if (_links != null) _links.clear(); // so a SimpleChangeEvent is published
    _links = new TreeAgentSet(AgentKindJ.Link(), "LINKS");

    int x = _minPxcor;
    int y = _maxPycor;
    int z = _maxPzcor;
    Agent[] patchArray = new Agent[_worldWidth * _worldHeight * _worldDepth];
    patchColors = new int[_worldWidth * _worldHeight * _worldDepth];
    Arrays.fill(patchColors, Color.getARGBbyPremodulatedColorNumber(0.0));
    patchColorsDirty = true;

    int numVariables = program().patchesOwn().size();

    _observer.resetPerspective();

    for (int i = 0; _worldWidth * _worldHeight * _worldDepth != i; i++) {
      Patch3D patch = new Patch3D(this, i, x, y, z, numVariables);
      x++;
      if (x == (_maxPxcor + 1)) {
        x = _minPxcor;
        y--;
        if (y == (_minPycor - 1)) {
          y = _maxPycor;
          z--;
        }
      }
      patchArray[i] = patch;
    }
    _patches = new ArrayAgentSet(AgentKindJ.Patch(), patchArray, "patches");
    patchesWithLabels = 0;
    patchesAllBlack = true;
    mayHavePartiallyTransparentObjects = false;
  }

  /// export world

  @Override
  public void exportWorld(java.io.PrintWriter writer, boolean full) {
    new Exporter3D(this, writer).exportWorld(full);
  }

  @Override
  public void importWorld(org.nlogo.agent.Importer.ErrorHandler errorHandler, ImporterUser importerUser,
                          org.nlogo.agent.Importer.StringReader stringReader, java.io.BufferedReader reader)
      throws java.io.IOException {
    new Importer3D(errorHandler, this,
        importerUser, stringReader).importWorld(reader);
  }

  // used by Importer and Parser
  @Override
  public Turtle getOrCreateTurtle(long id) {
    Turtle turtle = getTurtle(id);
    if (turtle == null) {
      turtle = new Turtle3D(this, id);
    }
    return turtle;
  }

  private double[][][] patchScratch3d;

  public double[][][] getPatchScratch3d() {
    if (patchScratch3d == null) {
      patchScratch3d = new double[_worldWidth][_worldHeight][_worldDepth];
    }
    return patchScratch3d;
  }

  // these methods are primarily for behaviorspace
  // to vary the size of the world without
  // knowing quite so much about the world.
  // ev 2/20/06
  @Override
  public WorldDimensions getDimensions() {
    return new WorldDimensions3D
        (_minPxcor, _maxPxcor, _minPycor, _maxPycor, _minPzcor, _maxPzcor, 12.0, true, true, true);
  }

  @Override
  public boolean isDimensionVariable(String variableName) {
    return
        variableName.equalsIgnoreCase("MIN-PXCOR") ||
            variableName.equalsIgnoreCase("MAX-PXCOR") ||
            variableName.equalsIgnoreCase("MIN-PYCOR") ||
            variableName.equalsIgnoreCase("MAX-PYCOR") ||
            variableName.equalsIgnoreCase("MIN-PZCOR") ||
            variableName.equalsIgnoreCase("MAX-PZCOR") ||
            variableName.equalsIgnoreCase("WORLD-WIDTH") ||
            variableName.equalsIgnoreCase("WORLD-HEIGHT") ||
            variableName.equalsIgnoreCase("WORLD-DEPTH");
  }

  @Override
  public WorldDimensions setDimensionVariable(String variableName, int value, WorldDimensions d)
      throws WorldDimensionException {
    WorldDimensions3D wd = (WorldDimensions3D) d;
    if (variableName.equalsIgnoreCase("MIN-PZCOR")) {
      return new WorldDimensions3D(wd.minPxcor(), wd.maxPxcor(), wd.minPycor(), wd.maxPycor(), value, wd.maxPzcor(), 12.0, true, true, true);
    } else if (variableName.equalsIgnoreCase("MAX-PZCOR")) {
      return new WorldDimensions3D(wd.minPxcor(), wd.maxPxcor(), wd.minPycor(), wd.maxPycor(), wd.minPzcor(), value, 12.0, true, true, true);
    } else if (variableName.equalsIgnoreCase("WORLD-DEPTH")) {
      int minPzcor = growMin(wd.minPzcor(), wd.maxPzcor(), value, wd.minPzcor());
      int maxPzcor = growMax(wd.minPzcor(), wd.maxPzcor(), value, wd.maxPzcor());
      return new WorldDimensions3D(wd.minPxcor(), wd.maxPxcor(), wd.minPycor(), wd.maxPycor(), minPzcor, maxPzcor, 12.0, true, true, true);
    } else {
      WorldDimensions newWd = super.setDimensionVariable(variableName, value, d);
      return new WorldDimensions3D(newWd.minPxcor(), newWd.maxPxcor(), newWd.minPycor(), newWd.maxPycor(), wd.minPzcor(), wd.maxPzcor(), 12.0, true, true, true);
    }
  }

  @Override
  public boolean equalDimensions(WorldDimensions d) {
    return d.minPxcor() == _minPxcor &&
      d.maxPxcor() == _maxPxcor &&
      d.minPycor() == _minPycor &&
      d.maxPycor() == _maxPycor &&
      ((WorldDimensions3D) d).minPzcor() == _minPzcor &&
      ((WorldDimensions3D) d).maxPzcor() == _maxPzcor;
  }

  @Override
  public Patch getPatchAt(double x, double y)
      throws AgentException {
    return getPatchAt(x, y, 0);
  }

  public Patch3D getPatchAt(double x, double y, double z)
      throws AgentException {
    int xc = roundX(x);
    int yc = roundY(y);
    int zc = roundZ(z);

    int id = ((_worldWidth * _worldHeight * (_maxPzcor - zc)) +
        (_worldWidth * (_maxPycor - yc))
        + xc - _minPxcor);

    return (Patch3D) _patches.toArray()[id];
  }

  @Override
  public Turtle createTurtle(AgentSet breed) {
    return new Turtle3D(this, breed, ZERO, ZERO, ZERO);
  }

  // c must be in 0-13 range
  // h can be out of range
  @Override
  public Turtle createTurtle(AgentSet breed, int c, int h) {
    Turtle baby = new Turtle3D(this, breed, ZERO, ZERO, ZERO);
    baby.colorDoubleUnchecked(Double.valueOf(5 + 10 * c));
    baby.heading(h);
    return baby;
  }

  @Override
  public Object getDrawing() {
    return drawing;
  }

  // we don't ever send pixels in 3D because
  // 3D drawing is vector based. ev 5/30/06
  @Override
  public boolean sendPixels() {
    return false;
  }

  @Override
  void drawLine(double x0, double y0, double x1, double y1,
                Object color, double size, String mode) {
    drawing.drawLine(x0, y0, 0, x1, y1, 0, size, color);
  }

  void drawLine(double x0, double y0, double z0,
                double x1, double y1, double z1,
                Object color, double size) {
    drawing.drawLine(x0, y0, z0, x1, y1, z1, size, color);
  }

  @Override
  public void clearAll() {
    super.clearAll();
    drawing.clear();
  }

  @Override
  public void clearDrawing() {
    drawing.clear();
  }

  @Override
  public void stamp(Agent agent, boolean erase) {
    if (!erase) {
      drawing.stamp(agent);
    }
  }
}
