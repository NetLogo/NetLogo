// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

import org.nlogo.core.AgentKind;
import org.nlogo.core.AgentKindJ;
import org.nlogo.api.AgentException;
import org.nlogo.api.AgentVariableNumbers;
import org.nlogo.core.AgentVariables;
import org.nlogo.api.Color;
import org.nlogo.api.Dump;
import org.nlogo.core.I18N;
import org.nlogo.core.LogoList;

public strictfp class Link
    extends Agent
    implements org.nlogo.api.Link {

  public AgentKind kind() { return AgentKindJ.Link(); }

  /// ends

  final Turtle end1;

  public Turtle end1() {
    return end1;
  }

  final Turtle end2;

  public Turtle end2() {
    return end2;
  }

  /// id

  @Override
  public Object agentKey() {
    return this;
  }

  /// variables

  public static final int VAR_END1 = AgentVariableNumbers.VAR_END1;
  public static final int VAR_END2 = AgentVariableNumbers.VAR_END2;
  public static final int VAR_COLOR = AgentVariableNumbers.VAR_LCOLOR;
  public static final int VAR_LABEL = AgentVariableNumbers.VAR_LLABEL;
  static final int VAR_LABELCOLOR = AgentVariableNumbers.VAR_LLABELCOLOR;
  static final int VAR_HIDDEN = AgentVariableNumbers.VAR_LHIDDEN;
  public static final int VAR_BREED = AgentVariableNumbers.VAR_LBREED;
  public static final int VAR_THICKNESS = AgentVariableNumbers.VAR_THICKNESS;
  public static final int VAR_SHAPE = AgentVariableNumbers.VAR_LSHAPE;
  public static final int VAR_TIEMODE = AgentVariableNumbers.VAR_TIEMODE;

  public int LAST_PREDEFINED_VAR = VAR_TIEMODE;
  public int NUMBER_PREDEFINED_VARS = LAST_PREDEFINED_VAR + 1;

  /// birth & death

  public static final Double DEFAULT_COLOR = Double.valueOf(5.0); // gray

  public Link(World world, Turtle end1, Turtle end2, int arraySize) {
    super(world);
    _variables_$eq(new Object[arraySize]);
    variables()[VAR_END1] = end1;
    variables()[VAR_END2] = end2;
    this.end1 = end1;
    this.end2 = end2;

    for (int i = 2; i < variables().length; i++) {
      variables()[i] = World.ZERO;
    }
    colorDoubleUnchecked(DEFAULT_COLOR);
  }

  Link(World world, Turtle end1, Turtle end2, AgentSet breed) {
    super(world);
    _variables_$eq(new Object[world.getVariablesArraySize(this, breed)]);
    colorDoubleUnchecked(DEFAULT_COLOR);
    variables()[VAR_END1] = end1;
    variables()[VAR_END2] = end2;
    variables()[VAR_LABEL] = "";
    variables()[VAR_LABELCOLOR] = Color.BoxedWhite();
    variables()[VAR_HIDDEN] = Boolean.FALSE;
    variables()[VAR_THICKNESS] = World.ZERO;
    variables()[VAR_SHAPE] = world.linkBreedShapes.breedShape(breed);
    variables()[VAR_TIEMODE] = MODE_NONE;
    this.end1 = end1;
    this.end2 = end2;

    variables()[VAR_BREED] = breed;
    world._links.add(this);

    if (breed != world.links()) {
      ((TreeAgentSet) breed).add(this);
    }

    for (int i = LAST_PREDEFINED_VAR + 1; i < variables().length; i++) {
      variables()[i] = World.ZERO;
    }
  }

  public void die() {
    if (id() == -1) {
      return;
    }
    TreeAgentSet breed = (TreeAgentSet) variables()[VAR_BREED];
    world()._links.remove(agentKey());
    if (breed != world().links()) {
      breed.remove(agentKey());
    }
    world().linkManager().cleanupLink(this);
    _id_$eq(-1);
  }

  ///

  @Override
  public void realloc(boolean compiling) {
    realloc(compiling, null);
  }

  void realloc(boolean compiling, AgentSet oldBreed) {

    // stage 0: get ready
    Object[] oldvars = variables();
    _variables_$eq(new Object[world().getVariablesArraySize(this, getBreed())]);
    int linksOwnSize = world().program().linksOwn().size();

    // stage 1: use arraycopy to copy over as many variables as possible
    // (if compiling it's just the predefined ones, if not compiling it's links-own too!)
    int numberToCopyDirectly = compiling ? NUMBER_PREDEFINED_VARS : linksOwnSize;
    System.arraycopy(oldvars, 0, variables(), 0, numberToCopyDirectly);

    // stage 2: shift the turtles-own variables into their new positions
    // (unless we already did turtles-own during stage 1)
    if (compiling) {
      for (int i = NUMBER_PREDEFINED_VARS; i < linksOwnSize; i++) {
        String name = world().linksOwnNameAt(i);
        int oldpos = world().oldLinksOwnIndexOf(name);
        if (oldpos == -1) {
          variables()[i] = World.ZERO;
        } else {
          variables()[i] = oldvars[oldpos];
          oldvars[oldpos] = null;
        }
      }
    }

    // stage 3: handle the BREED-own variables
    for (int i = linksOwnSize; i < variables().length; i++) {
      String name = world().linkBreedsOwnNameAt(getBreed(), i);
      int oldpos = compiling ? world().oldLinkBreedsOwnIndexOf(getBreed(), name)
          : world().linkBreedsOwnIndexOf(oldBreed, name);
      if (oldpos == -1) {
        variables()[i] = World.ZERO;
      } else {
        variables()[i] = oldvars[oldpos];
        oldvars[oldpos] = null;
      }
    }
  }

  @Override
  public Object getVariable(int vn) {
    return getLinkVariable(vn);
  }

  public String variableName(int vn) {
    if (vn < world().program().linksOwn().size()) {
      return world().linksOwnNameAt(vn);
    } else {
      return world().linkBreedsOwnNameAt(getBreed(), vn);
    }
  }

  @Override
  public Object getTurtleOrLinkVariable(String varName) {
    return getLinkVariable(world().program().linksOwn().indexOf(varName));
  }

  @Override
  public void setVariable(int vn, Object value)
      throws AgentException {
    setLinkVariable(vn, value);
  }

  @Override
  public Object getLinkVariable(int vn) {
    return variables()[vn];
  }

  public double getLinkVariableDouble(int vn) {
    switch (vn) {
      case VAR_THICKNESS:
        return lineThickness();
      default:
        throw new IllegalArgumentException
            (vn + " is not a double variable");
    }
  }

  @Override
  public void setTurtleOrLinkVariable(String varName, Object value)
      throws AgentException {
    setLinkVariable(world().program().linksOwn().indexOf(varName), value);
  }

  @Override
  public void setLinkVariable(int vn, double value) {
    switch (vn) {
      case VAR_THICKNESS:
        lineThickness(Double.valueOf(value));
        break;
      default:
        throw new IllegalArgumentException
            (vn + " is not a double variable");
    }
  }

  @Override
  public void setLinkVariable(int vn, Object value)
      throws AgentException {
    if (vn > LAST_PREDEFINED_VAR) {
      variables()[vn] = value;
    } else {
      switch (vn) {
        case VAR_COLOR:
          if (value instanceof Double) {
            colorDouble((Double) value);
          } else if (value instanceof LogoList) {
            color((LogoList) value);
          } else {
            wrongTypeForVariable(AgentVariables.getImplicitLinkVariables()[vn],
                Double.class, value);
          }
          break;
        case VAR_LABEL:
          label(value);
          break;
        case VAR_LABELCOLOR:
          if (value instanceof Double) {
            labelColor(((Double) value).doubleValue());
          } else if (value instanceof LogoList) {
            labelColor((LogoList) value);
          } else {
            wrongTypeForVariable(AgentVariables.getImplicitLinkVariables()[vn],
                Double.class, value);
          }
          break;
        case VAR_HIDDEN:
          if (value instanceof Boolean) {
            hidden(((Boolean) value).booleanValue());
          } else {
            wrongTypeForVariable(AgentVariables.getImplicitLinkVariables()[vn],
                Boolean.class, value);
          }
          break;
        case VAR_BREED:
          if (value instanceof AgentSet) {
            AgentSet breed = (AgentSet) value;
            if (breed != world().links() && !world().isLinkBreed(breed)) {
              throw new AgentException(I18N.errorsJ().get("org.nlogo.agent.Link.cantSetBreedToNonLinkBreedAgentSet"));
            }
            if (world().getLink(end1.agentKey(), end2.agentKey(), breed) != null) {
              throw new AgentException("there is already a "
                  + world().getLinkBreedSingular(breed)
                  + " with endpoints "
                  + end1.toString() + " and " + end2.toString());
            }
            if (!world().linkManager().checkBreededCompatibility(breed == world().links())) {
              throw new AgentException
                  (I18N.errorsJ().get("org.nlogo.agent.Link.cantHaveBreededAndUnbreededLinks"));
            }
            setBreed(breed);
          } else {
            wrongTypeForVariable(AgentVariables.getImplicitLinkVariables()[vn],
                AgentSet.class, value);
          }
          break;
        case VAR_THICKNESS:
          if (value instanceof Double) {
            lineThickness((Double) value);
          } else {
            wrongTypeForVariable(AgentVariables.getImplicitLinkVariables()[vn],
                Double.class, value);
          }
          break;
        case VAR_SHAPE:
          if (value instanceof String) {
            String newShape = world().checkLinkShapeName((String) value);
            if (newShape == null) {
              throw new AgentException(I18N.errorsJ().getN("org.nlogo.agent.Agent.shapeUndefined", value));
            }
            shape(newShape);
          } else {
            wrongTypeForVariable(AgentVariables.getImplicitLinkVariables()[vn],
                String.class, value);
          }
          break;
        case VAR_TIEMODE:
          if (value instanceof String) {
            mode((String) value);
          } else {
            wrongTypeForVariable(AgentVariables.getImplicitLinkVariables()[vn],
                String.class, value);
          }
          break;
        case VAR_END1:
        case VAR_END2:
          throw new AgentException("you can't change a link's endpoints");
        default:
          break;
      }
    }
    world().notifyWatchers(this, vn, value);
  }

  @Override
  public Object getTurtleVariable(int vn)
      throws AgentException {
    throw new AgentException
        ("a link can't access a turtle variable without specifying which turtle");
  }

  @Override
  public Object getBreedVariable(String name)
      throws AgentException {
    throw new AgentException
        ("a link can't access a turtle variable without specifying which turtle");
  }

  @Override
  public Object getLinkBreedVariable(String name)
      throws AgentException {
    mustOwn(name);
    int vn = world().linkBreedsOwnIndexOf(getBreed(), name);
    return getLinkVariable(vn);
  }

  @Override
  public Object getPatchVariable(int vn)
      throws AgentException {
    throw new AgentException
        ("a link can't access a patch variable without specifying which patch");
  }

  @Override
  public void setTurtleVariable(int vn, Object value)
      throws AgentException {
    throw new AgentException
        ("a link can't set a turtle variable without specifying which turtle");
  }

  @Override
  public void setTurtleVariable(int vn, double value)
      throws AgentException {
    throw new AgentException
        ("a link can't set a turtle variable without specifying which turtle");
  }

  @Override
  public void setBreedVariable(String name, Object value)
      throws AgentException {
    throw new AgentException
        ("a link can't set a turtle variable without specifying which turtle");
  }

  public void setBreedVariable(int vn, double value)
      throws AgentException {
    throw new AgentException
        ("a link can't set a turtle variable without specifying which turtle");
  }

  @Override
  public void setLinkBreedVariable(String name, Object value)
      throws AgentException {
    mustOwn(name);
    int vn = world().linkBreedsOwnIndexOf(getBreed(), name);
    setLinkVariable(vn, value);
  }

  @Override
  public void setPatchVariable(int vn, Object value)
      throws AgentException {
    throw new AgentException
        ("a link can't set a patch variable without specifying which turtle");
  }

  @Override
  public void setPatchVariable(int vn, double value)
      throws AgentException {
    throw new AgentException
        ("a link can't set a patch variable without specifying which turtle");
  }

  void mustOwn(String name)
      throws AgentException {
    if (name != null && !world().linkBreedOwns(getBreed(), name)) {
      throw new AgentException(
          I18N.errorsJ().getN("org.nlogo.agent.Agent.breedDoesNotOwnVariable",getBreed().printName(), name));
    }
  }

  ///

  public double x1() {
    return end1.xcor();
  }

  public double y1() {
    return end1.ycor();
  }

  public double x2() {
    return world().topology().shortestPathX(end1.xcor(), end2.xcor());
  }

  public double y2() {
    return world().topology().shortestPathY(end1.ycor(), end2.ycor());
  }

  public double midpointX() {
    double x1 = x1();
    double x2 = x2();

    return Topology.wrap((x1 + x2) / 2, world()._minPxcor - 0.5, world()._maxPxcor + 0.5);
  }

  public double midpointY() {
    double y1 = y1();
    double y2 = y2();

    return Topology.wrap((y1 + y2) / 2, world()._minPycor - 0.5, world()._maxPycor + 0.5);
  }

  public double heading() {
    try {
      return world().protractor().towards(end1, end2, true);
    } catch (AgentException e) {
      return 0;
    }
  }

  public double lineThickness() {
    return ((Double) variables()[VAR_THICKNESS]).doubleValue();
  }

  public void lineThickness(Double value) {
    variables()[VAR_THICKNESS] = value;
  }

  public boolean isDirectedLink() {
    return ((AgentSet) variables()[VAR_BREED]).isDirected();
  }

  public double linkDestinationSize() {
    return end2.size();
  }

  public double size() {
    return world().protractor().distance(end1, end2, true);
  }

  public String shape() {
    return (String) variables()[VAR_SHAPE];
  }

  public void shape(String shape) {
    variables()[VAR_SHAPE] = shape;
  }

  public static final String MODE_NONE = "none";
  public static final String MODE_FREE = "free";
  public static final String MODE_FIXED = "fixed";

  public String mode() {
    return (String) variables()[VAR_TIEMODE];
  }

  public void mode(String mode) {
    world().tieManager.setTieMode(this, mode);
    variables()[VAR_TIEMODE] = mode;
  }

  public boolean isTied() {
    return !variables()[VAR_TIEMODE].equals(MODE_NONE);
  }

  public void untie() {
    mode(MODE_NONE);
  }

  public Object color() {
    return variables()[VAR_COLOR];
  }

  public void colorDouble(Double boxedColor) {
    double c = boxedColor.doubleValue();
    if (c < 0 || c >= Color.MaxColor()) {
      c = Color.modulateDouble(c);
      boxedColor = Double.valueOf(c);
    }
    variables()[VAR_COLOR] = boxedColor;
  }

  public void colorDoubleUnchecked(Double boxedColor) {
    variables()[VAR_COLOR] = boxedColor;
  }

  public void color(LogoList rgb)
      throws AgentException {
    org.nlogo.api.Color.validRGBList(rgb, true);
    variables()[VAR_COLOR] = rgb;
    if(rgb.size() > 3) {
      world().mayHavePartiallyTransparentObjects = true;
    }
  }

  public AgentSet bothEnds() {
    return AgentSet.fromArray(
      AgentKindJ.Turtle(),
      new Agent[]{end1, end2});
  }

  @Override
  public Patch getPatchAtOffsets(double dx, double dy)
      throws AgentException {
    throw new AgentException
        ("links can't access patches via relative coordinates");
  }

  public Object label() {
    return variables()[VAR_LABEL];
  }

  public boolean hasLabel() {
    return !(label() instanceof String &&
        ((String) label()).length() == 0);
  }

  public String labelString() {
    return Dump.logoObject(variables()[VAR_LABEL]);
  }

  public void label(Object label) {
    variables()[VAR_LABEL] = label;
  }

  public boolean hidden() {
    return ((Boolean) variables()[VAR_HIDDEN]).booleanValue();
  }

  public void hidden(boolean hidden) {
    variables()[VAR_HIDDEN] = hidden ? Boolean.TRUE : Boolean.FALSE;
  }

  public AgentSet getBreed() {
    return (AgentSet) variables()[VAR_BREED];
  }

  public Object labelColor() {
    return variables()[VAR_LABELCOLOR];
  }

  public void labelColor(double labelColor) {
    variables()[VAR_LABELCOLOR] = Double.valueOf(Color.modulateDouble(labelColor));
  }

  public void labelColor(LogoList rgb)
      throws AgentException {
    org.nlogo.api.Color.validRGBList(rgb, true);
    variables()[VAR_LABELCOLOR] = rgb;
  }

  @Override
  public String toString() {
    return world().getLinkBreedSingular(getBreed()).toLowerCase() + " " +
      end1.id() + " " + end2.id();
  }

  @Override
  public String classDisplayName() {
    return world().getLinkBreedSingular(getBreed()).toLowerCase();
  }

  public static final int BIT = AgentBit.apply(AgentKindJ.Link());

  @Override
  public int agentBit() {
    return BIT;
  }

  public void setBreed(AgentSet breed) {
    TreeAgentSet oldBreed = (TreeAgentSet) variables()[VAR_BREED];
    if (variables()[VAR_BREED] instanceof AgentSet) {
      oldBreed = (TreeAgentSet) variables()[VAR_BREED];
      if (breed == oldBreed) {
        return;
      }
      if (oldBreed != world().links()) {
        ((TreeAgentSet) variables()[VAR_BREED]).remove(agentKey());
      }
    }
    if (breed != world().links()) {
      ((TreeAgentSet) breed).add(this);
    }
    variables()[VAR_BREED] = breed;
    shape(world().linkBreedShapes.breedShape(breed));
    realloc(false, oldBreed);
  }

  // returns the index of the breed of this link, 0 means a generic link;
  // this is super kludge. is there a better way? -AZS 10/28/04, ST 7/21/07
  public int getBreedIndex() {
    AgentSet mybreed = getBreed();
    if (mybreed == world().links()) {
      return 0;
    }
    int j = 1;
    scala.collection.Iterator<String> iter =
      world().program().linkBreeds().keys().iterator();
    while(iter.hasNext()) {
      if (world().linkBreedAgents.get(iter.next()) == mybreed) {
        return j;
      }
      j++;
    }
    return 0;
  }

  @Override
  public int compareTo(Agent a) {
    if (a == this) {
      return 0;
    }
    Link otherLink = (Link) a;
    if (end1.id() < otherLink.end1.id()) {
      return -1;
    }
    if (end1.id() > otherLink.end1.id()) {
      return 1;
    }
    if (end2.id() < otherLink.end2.id()) {
      return -1;
    }
    if (end2.id() > otherLink.end2.id()) {
      return 1;
    }
    if (getBreed() == otherLink.getBreed()) {
      return 0;
    }
    if (getBreed() == world().links()) {
      return -1;
    }
    if (otherLink.getBreed() == world().links()) {
      return 1;
    }
    // when all else fails
    // whichever breed was declared first comes first in the list.
    return world().compareLinkBreeds(getBreed(), otherLink.getBreed());
  }

  public int alpha() {
    return org.nlogo.api.Color.getColor(color()).getAlpha();
  }

}
