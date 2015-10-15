// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.mirroring;

import org.nlogo.api.AgentException;
import org.nlogo.api.Perspective;
import org.nlogo.api.PerspectiveJ;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static scala.collection.JavaConversions.seqAsJavaList;
import static scala.collection.JavaConversions.setAsJavaSet;

public strictfp class ClientWorld
    implements org.nlogo.api.World {

  /**
   * whether or not to print messages for protocol errors.
   * false is useful when unit testing.
   */
  private final boolean printErrors;

  // these are package protected for unit testing...

  // since we want to keep the turtles sorted, but we also
  // want to be able to look them up just by who number, we
  // keep two parallel maps, one sorted, one not -- AZS 11/16/04

  java.util.SortedMap<TurtleKey, TurtleData> sortedTurtles;
  Map<Long, TurtleKey> turtleKeys;

  Map<Long, TurtleData> uninitializedTurtles = new HashMap<Long, TurtleData>();

  java.util.SortedMap<LinkKey, LinkData> sortedLinks;
  Map<Long, LinkKey> linkKeys;

  Map<Long, LinkData> uninitializedLinks = new HashMap<Long, LinkData>();

  public PatchData[] patches;
  private int[] patchColors;

  public int[] patchColors() {
    return patchColors;
  }

  private int minPxcor = -1;
  private int maxPxcor = -1;
  private int minPycor = -1;
  private int maxPycor = -1;

  private boolean shapes;

  public ClientWorld() {
    this(true);
  }

  public ClientWorld(boolean printErrors) {
    this.printErrors = printErrors;
    sortedTurtles = new TreeMap<TurtleKey, TurtleData>(new TurtleKeyComparator());
    turtleKeys = new HashMap<Long, TurtleKey>();
    sortedLinks = new TreeMap<LinkKey, LinkData>(new LinkKeyComparator());
    linkKeys = new HashMap<Long, LinkKey>();
  }

  // temporary hack for the review tab experiments
  public void reset(){
    sortedTurtles = new TreeMap<TurtleKey, TurtleData>(new TurtleKeyComparator());
    turtleKeys = new HashMap<Long, TurtleKey>();
    sortedLinks = new TreeMap<LinkKey, LinkData>(new LinkKeyComparator());
    linkKeys = new HashMap<Long, LinkKey>();
  }

  public ClientWorld(int numPatches, boolean printErrors) {
    this(printErrors);
    createPatches(numPatches);
  }

  public double ticks() {
    throw new UnsupportedOperationException();
  }

  public void setWorldSize(int minx, int maxx, int miny, int maxy) {
    this.minPxcor = minx;
    this.maxPxcor = maxx;
    this.minPycor = miny;
    this.maxPycor = maxy;
    createPatches(worldWidth() * worldHeight());
  }

  private void createPatches(int numPatches) {
    patches = new PatchData[numPatches];
    patchColors = new int[numPatches];
    for (int i = 0; i < patches.length; i++) {
      patches[i] = new PatchData(i, PatchData.COMPLETE, 0, 0, 0.0, "", 0.0);
      patches[i].patchColors = patchColors;
    }
  }

  private org.nlogo.api.TrailDrawerInterface trailDrawer;

  public void setTrailDrawer(org.nlogo.api.TrailDrawerInterface trailDrawer) {
    this.trailDrawer = trailDrawer;
  }

  /**
   * Returns descriptions of the turtles in this world.
   * In the correct order for drawing.
   */
  public Iterable<TurtleData> getTurtles() {
    return sortedTurtles.values();
  }

  /**
   * Returns descriptions of the turtles in this world.
   * In the correct order for drawing.
   */
  public Iterable<LinkData> getLinks() {
    return sortedLinks.values();
  }

  /**
   * Returns descriptions of the patches in this world.
   */
  public PatchData[] getPatches() {
    return patches;
  }

  private int fontSize;

  public int fontSize() {
    return (int) (fontSize * zoom());
  }

  public int minPxcor() {
    return minPxcor;
  }

  public int maxPxcor() {
    return maxPxcor;
  }

  public int minPycor() {
    return minPycor;
  }

  public int maxPycor() {
    return maxPycor;
  }

  public int worldWidth() {
    return maxPxcor - minPxcor + 1;
  }

  public int worldHeight() {
    return maxPycor - minPycor + 1;
  }

  public boolean shapesOn() {
    return shapes;
  }

  public double wrapX(double x)
      throws AgentException {
    double max = maxPxcor + 0.5;
    double min = minPxcor - 0.5;
    if (!xWrap) {
      if (x >= max || x < min) {
        throw new AgentException("Cannot move turtle beyond the world's edge.");
      }
      return x;
    } else {
      return wrap(x, min, max);
    }
  }

  public double wrapY(double y)
      throws AgentException {
    double max = maxPycor + 0.5;
    double min = minPycor - 0.5;
    if (!yWrap) {
      if (y >= max || y < min) {
        throw new AgentException("Cannot move turtle beyond the world's edge.");
      }
      return y;
    } else {
      return wrap(y, min, max);
    }
  }

  public double wrap(double pos, double min, double max) {
    if (pos >= max) {
      return (min + ((pos - max) % (max - min)));
    } else if (pos < min) {
      double result = ((min - pos) % (max - min));
      return (result == 0) ? min : (max - result);
    } else {
      return pos;
    }
  }

  private double patchSize = 13;

  public void patchSize(double patchSize) {
    this.patchSize = patchSize;
  }

  public double patchSize() {
    return perspectiveMode == PerspectiveMode.SERVER ? patchSize : ((StrictMath.max(viewWidth, viewHeight)) / ((radius * 2) + 1));
  }

  public double zoom() {
    return patchSize() / patchSize;
  }

  private boolean xWrap;
  private boolean yWrap;

  public boolean wrappingAllowedInX() {
    return xWrap;
  }

  public boolean wrappingAllowedInY() {
    return yWrap;
  }

  // for now we're not keeping track of this on the client,
  // but we could ev 4/24/08
  public boolean patchesAllBlack() {
    return false;
  }

  void updatePatch(PatchData patch) {
    if (patch.id() >= patches.length) {
      handleError("ERROR: received update for "
          + "non-existent patch (" + patch.stringRep() + ").");
      return;
    }

    // otherwise, we'll need our version, if we've got one.
    PatchData bufPatch = patches[(int) patch.id()];

    // if we haven't got one, this patch better have all its info...
    if (bufPatch == null && !patch.isComplete()) {
      handleError
          ("ERROR: received incremental update for non-existent patch (" + patch.stringRep() + ").");
    }

    // otherwise, perform the update...
    bufPatch.updateFrom(patch);
    patchColors[(int) patch.id()] = org.nlogo.api.Color.getARGBIntByRGBAList((org.nlogo.core.LogoList) bufPatch.pcolor());
  }

  void updateTurtle(TurtleData turtle) {
    Long simpleKey = Long.valueOf(turtle.id());
    TurtleKey sortedKey = turtleKeys.get(simpleKey);

    // if this turtle has died, just remove it...
    if (turtle.isDead()) {
      if (sortedKey == null) {
        handleError("ERROR: received death message for "
            + "non-existent turtle (" + turtle.stringRep() + ").");
        return;
      }
      sortedTurtles.remove(sortedKey);
      turtleKeys.remove(simpleKey);
      return;
    }

    // otherwise, we'll need our version, if we've got one.
    TurtleData bufTurtle = null;

    if (sortedKey == null) {
      bufTurtle = uninitializedTurtles.get(simpleKey);
      if (bufTurtle != null) {
        sortedKey = new TurtleKey(turtle.id(), turtle.getBreedIndex());
        sortedTurtles.put(sortedKey, bufTurtle);
        turtleKeys.put(simpleKey, sortedKey);
        uninitializedTurtles.remove(simpleKey);
      }
    }

    if (sortedKey != null) {
      bufTurtle = sortedTurtles.get(sortedKey);
    }

    // if we haven't got one, this turtle better have all its info...
    if (bufTurtle == null) {
      if (turtle.isComplete()) {
        sortedKey = new TurtleKey(turtle.id(), turtle.getBreedIndex());
        sortedTurtles.put(sortedKey, turtle);
        turtleKeys.put(simpleKey, sortedKey);
      } else {
        handleError("ERROR: received incremental update for non-existent turtle ("
            + turtle.stringRep() + ").");
      }
      return;
    }

    // otherwise, perform the update...
    bufTurtle.updateFrom(turtle);

    if (bufTurtle.getBreedIndex() != sortedKey.breedIndex) {
      // the breed of this turtle changed so we need to make
      // a new key in the sortedTurtles map ev 5/19/08
      sortedTurtles.remove(sortedKey);
      sortedKey = new TurtleKey(bufTurtle.id(), turtle.getBreedIndex());
      sortedTurtles.put(sortedKey, bufTurtle);
      turtleKeys.put(simpleKey, sortedKey);
    }
  }

  void updateLink(LinkData link) {
    Long simpleKey = Long.valueOf(link.id);
    LinkKey sortedKey = linkKeys.get(simpleKey);

    if (link.isDead()) {
      if (sortedKey == null) {
        handleError
            ("ERROR: received death message for non-existent link ( "
                + link.stringRep() + " ).");
        return;
      }

      sortedLinks.remove(sortedKey);
      linkKeys.remove(simpleKey);

      return;
    }

    LinkData bufLink = null;

    if (sortedKey == null) {
      bufLink = uninitializedLinks.get(simpleKey);
      if (bufLink != null) {
        sortedKey = new LinkKey(link.id, link.end1, link.end2, link.getBreedIndex());
        linkKeys.put(simpleKey, sortedKey);
        sortedLinks.put(sortedKey, link);
        uninitializedLinks.remove(simpleKey);
      }
    }

    if (sortedKey != null) {
      bufLink = sortedLinks.get(sortedKey);
    }

    // if we haven't got one, this link better have all its info...
    if (bufLink == null) {
      if (link.isComplete()) {
        sortedKey = new LinkKey(link.id, link.end1, link.end2, link.getBreedIndex());
        linkKeys.put(simpleKey, sortedKey);
        sortedLinks.put(sortedKey, link);
      } else {
        handleError("ERROR: received incremental update for "
            + "non-existent turtle (" + link.stringRep() + ").");
      }
    } else {
      bufLink.updateFrom(link);

      if (link.isComplete() || bufLink.getBreedIndex() != sortedKey.breedIndex) {
        sortedLinks.remove(sortedKey);
        sortedKey = new LinkKey(link.id, link.end1, link.end2, link.getBreedIndex());
        sortedLinks.put(sortedKey, bufLink);
        linkKeys.put(simpleKey, sortedKey);
      }
    }
  }

  public void updateFrom(java.io.DataInputStream is)
      throws java.io.IOException {
    short mask = is.readShort();
    boolean reallocatePatches = false;
    if ((mask & DiffBuffer.MINX) == DiffBuffer.MINX) {
      int minx = is.readInt();
      reallocatePatches = reallocatePatches || minx != minPxcor;
      minPxcor = minx;
    }
    if ((mask & DiffBuffer.MINY) == DiffBuffer.MINY) {
      int miny = is.readInt();
      reallocatePatches = reallocatePatches || miny != minPycor;
      minPycor = miny;
    }
    if ((mask & DiffBuffer.MAXX) == DiffBuffer.MAXX) {
      int maxx = is.readInt();
      reallocatePatches = reallocatePatches || maxx != maxPxcor;
      maxPxcor = maxx;
    }
    if ((mask & DiffBuffer.MAXY) == DiffBuffer.MAXY) {
      int maxy = is.readInt();
      reallocatePatches = reallocatePatches || maxy != maxPycor;
      maxPycor = maxy;
    }
    if (reallocatePatches) {
      createPatches(worldWidth() * worldHeight());
    }
    if ((mask & DiffBuffer.SHAPES) == DiffBuffer.SHAPES) {
      shapes = is.readBoolean();
    }
    if ((mask & DiffBuffer.FONT_SIZE) == DiffBuffer.FONT_SIZE) {
      fontSize = is.readInt();
    }
    if ((mask & DiffBuffer.WRAPX) == DiffBuffer.WRAPX) {
      xWrap = is.readBoolean();
    }
    if ((mask & DiffBuffer.WRAPY) == DiffBuffer.WRAPY) {
      yWrap = is.readBoolean();
    }
    if ((mask & DiffBuffer.PERSPECTIVE) == DiffBuffer.PERSPECTIVE) {
      updateServerPerspective(new AgentPerspective(is));
    }
    if ((mask & DiffBuffer.PATCHES) == DiffBuffer.PATCHES) {
      int numToRead = is.readInt();
      for (int i = 0; i < numToRead; i++) {
        updatePatch(new PatchData(is));
      }
    }
    if ((mask & DiffBuffer.TURTLES) == DiffBuffer.TURTLES) {
      int numToRead = is.readInt();
      for (int i = 0; i < numToRead; i++) {
        updateTurtle(new TurtleData(is));
      }
    }
    if ((mask & DiffBuffer.LINKS) == DiffBuffer.LINKS) {
      int numToRead = is.readInt();
      for (int i = 0; i < numToRead; i++) {
        updateLink(new LinkData(is));
      }
    }
    if ((mask & DiffBuffer.DRAWING) == DiffBuffer.DRAWING) {
      trailDrawer.readImage(is);
    }
  }

  private void handleError(Object o) {
    if (printErrors) {
      System.err.println("@ " + new java.util.Date() + " : ");
      System.err.println(o.toString());
    }
  }

  // used by TestClientWorld only
  TurtleData getTurtleDataByWho(long who) {
    TurtleKey key = turtleKeys.get(Long.valueOf(who));
    if (key != null) {
      return sortedTurtles.get(key);
    }
    return null;
  }

  LinkData getLink(Long id) {
    LinkKey key = linkKeys.get(id);
    if (key == null) {
      LinkData link = uninitializedLinks.get(id);
      if (link == null) {
        link = new LinkData(id);
        uninitializedLinks.put(id, link);
      }
      return link;
    } else {
      return sortedLinks.get(key);
    }
  }

  TurtleData getTurtle(Long id) {
    TurtleKey key = turtleKeys.get(id);
    if (key == null) {
      TurtleData turtle = uninitializedTurtles.get(id);
      if (turtle == null) {
        turtle = new TurtleData(id);
        uninitializedTurtles.put(id, turtle);
      }
      return turtle;
    } else {
      return sortedTurtles.get(key);
    }
  }

  public enum PerspectiveMode {
    SERVER, CLIENT;
  }

  private PerspectiveMode perspectiveMode = PerspectiveMode.SERVER;

  public Perspective perspective = PerspectiveJ.OBSERVE();
  public AgentData targetAgent;
  public double radius;

  public boolean serverMode() {
    return perspectiveMode == PerspectiveMode.SERVER;
  }

  public void updateServerPerspective(AgentPerspective p) {
    if (perspectiveMode == PerspectiveMode.SERVER) {
      perspective = Perspective.load(p.perspective);
      radius = p.radius;
      targetAgent = getAgent(p.agent);
    }
  }

  public void updateClientPerspective(AgentPerspective p) {
    perspective = Perspective.load(p.perspective);
    perspectiveMode = p.serverMode ? PerspectiveMode.SERVER : PerspectiveMode.CLIENT;
    targetAgent = getAgent(p.agent);
    radius = p.radius;
  }

  public double followOffsetX() {
    if (targetAgent == null || (perspective != PerspectiveJ.FOLLOW() && perspective != PerspectiveJ.RIDE())) {
      return 0;
    }

    if (perspectiveMode == PerspectiveMode.CLIENT) {
      return targetAgent.xcor() - ((viewWidth() - 1) / 2) - minPxcor;
    } else {
      return targetAgent.xcor() - ((minPxcor - 0.5) + worldWidth() / 2.0);
    }
  }

  public double followOffsetY() {
    AgentData agent = targetAgent();

    if (agent == null || (perspective != PerspectiveJ.FOLLOW() && perspective != PerspectiveJ.RIDE())) {
      return 0;
    }

    if (perspectiveMode == PerspectiveMode.CLIENT) {
      return targetAgent.ycor() + ((viewHeight() - 1) / 2) - maxPycor;
    } else {
      return targetAgent.ycor() - ((minPycor - 0.5) + worldHeight() / 2.0);
    }
  }

  public AgentData targetAgent() {
    return targetAgent;
  }

  private int viewWidth;
  private int viewHeight;

  public void viewWidth(int viewWidth) {
    this.viewWidth = viewWidth;
  }

  public void viewHeight(int viewHeight) {
    this.viewHeight = viewHeight;
  }

  public double viewWidth() {
    return (viewWidth / patchSize());
  }

  public double viewHeight() {
    return (viewHeight / patchSize());
  }

  AgentData getAgent(Agent agent) {
    if (agent.type == Agent.AgentType.TURTLE) {
      return getTurtle(Long.valueOf(agent.id));
    }
    if (agent.type == Agent.AgentType.PATCH) {
      return patches[(int) agent.id];
    }
    if (agent.type == Agent.AgentType.LINK) {
      return getLink(Long.valueOf(agent.id));
    }
    return null;
  }

  private final java.util.Map<Overridable, java.util.Map<Integer, Object>> overrideMap
      = new java.util.HashMap<Overridable, java.util.Map<Integer, Object>>();

  public void clearOverrides() {
    overrideMap.clear();
  }

  public void updateOverrides(SendOverride list) {
    if (list.type == Agent.AgentType.TURTLE) {
      for (Long id : setAsJavaSet(list.overrides().keySet())) {
        addOverride(getTurtle(id), list.variable, list.overrides().apply(id));
      }
    } else if (list.type == Agent.AgentType.PATCH) {
      for (Long id : setAsJavaSet(list.overrides().keySet())) {
        addOverride(patches[id.intValue()], list.variable, list.overrides().apply(id));
      }
    } else if (list.type == Agent.AgentType.LINK) {
      for (Long id : setAsJavaSet(list.overrides().keySet())) {
        addOverride(getLink(id), list.variable, list.overrides().apply(id));
      }
    }
  }

  private void addOverride(Overridable rider, int variable, Object value) {
    java.util.Map<Integer, Object> map = overrideMap.get(rider);
    if (map == null) {
      map = new java.util.HashMap<Integer, Object>();
      overrideMap.put(rider, map);
    }
    map.put(Integer.valueOf(variable), value);
  }

  public void updateOverrides(ClearOverride list) {
    if (list.type == Agent.AgentType.TURTLE) {
      for (Long id : seqAsJavaList(list.agents())) {
        removeOverride(getTurtle(id), list.variable);
      }
    } else if (list.type == Agent.AgentType.PATCH) {
      for (Long id : seqAsJavaList(list.agents())) {
        removeOverride(patches[id.intValue()], list.variable);
      }
    } else if (list.type == Agent.AgentType.LINK) {
      for (Long id : seqAsJavaList(list.agents())) {
        removeOverride(getLink(id), list.variable);
      }
    }
  }

  private void removeOverride(Overridable rider, int variable) {
    java.util.Map<Integer, Object> map = overrideMap.get(rider);
    if (map != null) {
      map.remove(Integer.valueOf(variable));
    }
  }

  public void applyOverrides() {
    for (Overridable rider : overrideMap.keySet()) {
      java.util.Map<Integer, Object> overrides = overrideMap.get(rider);
      for (Integer var : overrides.keySet()) {
        rider.set(var.intValue(), overrides.get(var));
      }
    }
  }

  public void rollbackOverrides() {
    for (Overridable rider : overrideMap.keySet()) {
      rider.rollback();
    }
  }

  private static class TurtleKey {
    long who;
    int breedIndex;

    public TurtleKey(long who, int breedIndex) {
      this.who = who;
      this.breedIndex = breedIndex;
    }

    @Override
    public boolean equals(Object o) {
      return (who == ((TurtleKey) o).who) &&
          (breedIndex == ((TurtleKey) o).breedIndex);
    }

    @Override
    public int hashCode() {
      return (int) (breedIndex * 1000 + who);
    }

    @Override
    public String toString() {
      return "(" + who + " " + breedIndex + ")";
    }
  }

  private static class TurtleKeyComparator
      implements Comparator<TurtleKey> {
    public int compare(TurtleKey tk1, TurtleKey tk2) {
      if (tk1.breedIndex == tk2.breedIndex) {
        return (int) (tk1.who - tk2.who);
      } else {
        return tk1.breedIndex - tk2.breedIndex;
      }
    }
  }

  static class LinkKey {
    long id;
    long end1;
    long end2;
    int breedIndex;

    public LinkKey(long id, long end1, long end2, int breedIndex) {
      this.id = id;
      this.end1 = end1;
      this.end2 = end2;
      this.breedIndex = breedIndex;
    }

    @Override
    public boolean equals(Object o) {
      return id == ((LinkKey) o).id;
    }

    @Override
    public int hashCode() {
      return (int) id;
    }

    @Override
    public String toString() {
      return "(" + id + " " + breedIndex + ")";
    }
  }

  private static class LinkKeyComparator
      implements Comparator<LinkKey> {
    public int compare(LinkKey key1, LinkKey key2) {
      if (key1.end1 == key2.end1) {
        if (key1.end2 == key2.end2) {
          if (key1.breedIndex == key2.breedIndex) {
            return (int) (key1.id - key2.id);
          } else {
            return key1.breedIndex - key2.breedIndex;
          }
        } else {
          return (int) (key1.end2 - key2.end2);
        }
      } else {
        return (int) (key1.end1 - key2.end1);
      }
    }
  }

  public void clearGlobals() {
    throw new UnsupportedOperationException();
  }

  public org.nlogo.api.AgentSet links() {
    throw new UnsupportedOperationException();
  }

  public org.nlogo.api.AgentSet turtles() {
    throw new UnsupportedOperationException();
  }

  public org.nlogo.api.AgentSet patches() {
    throw new UnsupportedOperationException();
  }

  public org.nlogo.core.Program program() {
    throw new UnsupportedOperationException();
  }

  public org.nlogo.api.ShapeList turtleShapeList() {
    throw new UnsupportedOperationException();
  }

  public org.nlogo.api.ShapeList linkShapeList() {
    throw new UnsupportedOperationException();
  }

  public int patchesWithLabels() {
    throw new UnsupportedOperationException();
  }

  public org.nlogo.api.Patch getPatch(int i) {
    throw new UnsupportedOperationException();
  }

  public org.nlogo.api.Patch getPatchAt(double x, double y) {
    throw new UnsupportedOperationException();
  }

  public org.nlogo.api.Observer observer() {
    throw new UnsupportedOperationException();
  }

  public Object getDrawing() {
    throw new UnsupportedOperationException();
  }

  public boolean sendPixels() {
    throw new UnsupportedOperationException();
  }

  public void markDrawingClean() {
    throw new UnsupportedOperationException();
  }

  public org.nlogo.api.Protractor protractor() {
    throw new UnsupportedOperationException();
  }

  public double wrappedObserverX(double x) {
    throw new UnsupportedOperationException();
  }

  public double wrappedObserverY(double y) {
    throw new UnsupportedOperationException();
  }

  public void markPatchColorsClean() {
    throw new UnsupportedOperationException();
  }

  public void markPatchColorsDirty() {
    throw new UnsupportedOperationException();
  }

  public boolean patchColorsDirty() {
    throw new UnsupportedOperationException();
  }

  public org.nlogo.api.Patch fastGetPatchAt(int x, int y) {
    throw new UnsupportedOperationException();
  }

  public int getVariablesArraySize(org.nlogo.api.Link link, org.nlogo.api.AgentSet breed) {
    throw new UnsupportedOperationException();
  }

  public String linksOwnNameAt(int i) {
    throw new UnsupportedOperationException();
  }

  public int getVariablesArraySize(org.nlogo.api.Turtle turtle, org.nlogo.api.AgentSet breed) {
    throw new UnsupportedOperationException();
  }

  public String turtlesOwnNameAt(int i) {
    throw new UnsupportedOperationException();
  }

  public String breedsOwnNameAt(org.nlogo.api.AgentSet breed, int i) {
    throw new UnsupportedOperationException();
  }

  public scala.collection.Iterator<Object> allStoredValues() {
    throw new UnsupportedOperationException();
  }

  public boolean mayHavePartiallyTransparentObjects() {
    return false;
  }

  public Map<String, ? extends org.nlogo.api.AgentSet> getBreeds() {
    throw new UnsupportedOperationException();
  }

  public Map<String, ? extends org.nlogo.api.AgentSet> getLinkBreeds() {
    throw new UnsupportedOperationException();
  }

}
