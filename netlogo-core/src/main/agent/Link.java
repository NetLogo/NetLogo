// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

import org.nlogo.core.AgentKind;
import org.nlogo.core.AgentKindJ;
import org.nlogo.core.Breed;
import org.nlogo.core.I18N;
import org.nlogo.core.LogoList;
import org.nlogo.core.Program;
import org.nlogo.api.AgentException;
import org.nlogo.api.AgentVariableNumbers;
import org.nlogo.api.AgentVariables;
import org.nlogo.api.Color;
import org.nlogo.api.Dump;
import org.nlogo.api.LogoException;
import org.nlogo.log.LogManager;

import java.util.Iterator;

public class Link
    extends Agent
    implements org.nlogo.api.Link, AgentColors {

  public AgentKind kind() { return AgentKindJ.Link(); }

  /// ends

  final Turtle _end1;

  public Turtle end1() {
    return _end1;
  }

  final Turtle _end2;

  public Turtle end2() {
    return _end2;
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
    Object[] variables = new Object[arraySize];
    variables[VAR_END1] = end1;
    variables[VAR_END2] = end2;
    this._end1 = end1;
    this._end2 = end2;

    for (int i = 2; i < variables.length; i++) {
      variables[i] = World.Zero();
    }

    setVariables(variables);

    colorDoubleUnchecked(DEFAULT_COLOR);
  }

  Link(World world, Turtle end1, Turtle end2, AgentSet breed) {
    super(world);
    Object[] variables = new Object[world.getVariablesArraySize(this, breed)];
    variables[VAR_COLOR] = Color.BoxedBlack();
    variables[VAR_END1] = end1;
    variables[VAR_END2] = end2;
    variables[VAR_LABEL] = "";
    variables[VAR_LABELCOLOR] = Color.BoxedWhite();
    variables[VAR_HIDDEN] = Boolean.FALSE;
    variables[VAR_THICKNESS] = World.Zero();
    variables[VAR_SHAPE] = _world.linkBreedShapes().breedShape(breed);
    variables[VAR_TIEMODE] = MODE_NONE;

    setVariables(variables);

    this._end1 = end1;
    this._end2 = end2;

    variables[VAR_BREED] = breed;
    world.links().add(this);

    if (breed != world.links()) {
      ((TreeAgentSet) breed).add(this);
    }

    for (int i = LAST_PREDEFINED_VAR + 1; i < variables.length; i++) {
      variables[i] = World.Zero();
    }

    colorDoubleUnchecked(DEFAULT_COLOR);
  }

  public void die() {
    if (_id == -1) {
      return;
    }
    AgentSet breed = getBreed();
    _world.links().remove(agentKey());
    if (breed != _world.links()) {
      ((TreeAgentSet) breed).remove(agentKey());
    }
    _world.linkManager().cleanupLink(this);
    Long oldId = this.id();
    setId(-1);
    LogManager.linkRemoved(oldId, breed.printName(), _end1.id(), _end2.id());
  }

  ///

  @Override
  public Agent realloc(Program oldProgram, Program newProgram) {
    return realloc(oldProgram, newProgram, null);
  }

  Agent realloc(Program oldProgram, Program program, AgentSet oldBreed) {
    boolean compiling = oldProgram != null;

    // first check if we recompiled and our breed disappeared!
    if (compiling && getBreed() != _world.links() &&
        _world.getLinkBreed(getBreed().printName()) == null) {
      return this;
    }

    // stage 0: get ready
    Object[] oldvars = variables();
    Object[] variables = new Object[_world.getVariablesArraySize(this, getBreed())];
    int linksOwnSize = _world.getVariablesArraySize((Link) null, _world.links());

    // stage 1: use arraycopy to copy over as many variables as possible
    // (if compiling it's just the predefined ones, if not compiling it's links-own too!)
    int numberToCopyDirectly = compiling ? NUMBER_PREDEFINED_VARS : linksOwnSize;
    System.arraycopy(oldvars, 0, variables, 0, numberToCopyDirectly);

    // stage 2: shift the turtles-own variables into their new positions
    // (unless we already did turtles-own during stage 1)
    if (compiling) {
      for (int i = NUMBER_PREDEFINED_VARS; i < linksOwnSize; i++) {
        String name = _world.linksOwnNameAt(i);
        int oldpos = oldProgram.linksOwn().indexOf(name);
        if (oldpos == -1) {
          variables[i] = World.Zero();
        } else {
          variables[i] = oldvars[oldpos];
          oldvars[oldpos] = null;
        }
      }
    }

    // stage 3: handle the BREED-own variables
    for (int i = linksOwnSize; i < variables.length; i++) {
      String name = _world.linkBreedsOwnNameAt(getBreed(), i);
      int oldpos = compiling ? oldBreedsOwnIndexOf(oldProgram, getBreed(), name)
          : _world.linkBreedsOwnIndexOf(oldBreed, name);
      if (oldpos == -1) {
        variables[i] = World.Zero();
      } else {
        variables[i] = oldvars[oldpos];
        oldvars[oldpos] = null;
      }
    }
    setVariables(variables);
    return null;
  }


  /**
   * used by Link.realloc()
   */
  private int oldBreedsOwnIndexOf(Program oldProgram, AgentSet breed, String name) {
    scala.Option<Breed> found = oldProgram.linkBreeds().get(breed.printName());
    if (found.isEmpty()) {
      return -1;
    }
    int result = found.get().owns().indexOf(name);
    if (result == -1) {
      return -1;
    }
    return oldProgram.linksOwn().size() + result;
  }

  @Override
  public Object getVariable(int vn) {
    return getLinkVariable(vn);
  }

  public String variableName(int vn) {
    if (vn < _world.program().linksOwn().size()) {
      return _world.linksOwnNameAt(vn);
    } else {
      return _world.linkBreedsOwnNameAt(getBreed(), vn);
    }
  }

  @Override
  public Object getTurtleOrLinkVariable(String varName) {
    return getLinkVariable(_world.program().linksOwn().indexOf(varName));
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
    setLinkVariable(_world.program().linksOwn().indexOf(varName), value);
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
            if (breed != _world.links() && !_world.isLinkBreed(breed)) {
              throw new AgentException(I18N.errorsJ().get("org.nlogo.agent.Link.cantSetBreedToNonLinkBreedAgentSet"));
            }
            if (_world.getLink(_end1.agentKey(), _end2.agentKey(), breed) != null) {
              throw new AgentException("there is already a "
                  + _world.getLinkBreedSingular(breed)
                  + " with endpoints "
                  + _end1.toString() + " and " + _end2.toString());
            }
            if (!_world.linkManager().checkBreededCompatibility(breed == _world.links())) {
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
            String newShape = _world.checkLinkShapeName((String) value);
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
    _world.notifyWatchers(this, vn, value);
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
    int vn = _world.linkBreedsOwnIndexOf(getBreed(), name);
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
    int vn = _world.linkBreedsOwnIndexOf(getBreed(), name);
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
    if (name != null && !_world.linkBreedOwns(getBreed(), name)) {
      throw new AgentException(
          I18N.errorsJ().getN("org.nlogo.agent.Agent.breedDoesNotOwnVariable",getBreed().printName(), name));
    }
  }

  ///

  public double x1() {
    return _end1.xcor();
  }

  public double y1() {
    return _end1.ycor();
  }

  public double x2() {
    return _world.topology().shortestPathX(_end1.xcor(), _end2.xcor());
  }

  public double y2() {
    return _world.topology().shortestPathY(_end1.ycor(), _end2.ycor());
  }

  public double midpointX() {
    double x1 = x1();
    double x2 = x2();

    return Topology.wrap((x1 + x2) / 2, _world.minPxcor() - 0.5, _world.maxPxcor() + 0.5);
  }

  public double midpointY() {
    double y1 = y1();
    double y2 = y2();

    return Topology.wrap((y1 + y2) / 2, _world.minPycor() - 0.5, _world.maxPycor() + 0.5);
  }

  public double heading() {
    try {
      return _world.protractor().towards(_end1, _end2, true);
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
    return _end2.size();
  }

  public double size() {
    return _world.protractor().distance(_end1, _end2, true);
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
    _world.tieManager().setTieMode(this, mode);
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
    validRGBList(rgb, true);
    variables()[VAR_COLOR] = rgb;
    if(rgb.size() > 3) {
      _world.mayHavePartiallyTransparentObjects(true);
    }
  }

  public AgentSet bothEnds() {
    return AgentSet.fromArray(AgentKindJ.Turtle(), new Turtle[] {_end1, _end2});
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

  public TreeAgentSet getBreed() {
    return (TreeAgentSet) variables()[VAR_BREED];
  }

  public Object labelColor() {
    return variables()[VAR_LABELCOLOR];
  }

  public void labelColor(double labelColor) {
    variables()[VAR_LABELCOLOR] = Double.valueOf(Color.modulateDouble(labelColor));
  }

  public void labelColor(LogoList rgb)
      throws AgentException {
    validRGBList(rgb, true);
    variables()[VAR_LABELCOLOR] = rgb;
  }

  @Override
  public String toString() {
    return _world.getLinkBreedSingular(getBreed()).toLowerCase() + " " +
        _end1._id + " " + _end2._id;
  }

  @Override
  public String classDisplayName() {
    return _world.getLinkBreedSingular(getBreed()).toLowerCase();
  }

  public static final int BIT = 8;

  @Override
  public int agentBit() {
    return BIT;
  }

  public void setBreed(AgentSet breed) {
    AgentSet oldBreed = null;
    if (variables()[VAR_BREED] instanceof AgentSet) {
      _world.linkManager().removeLink(this);
      oldBreed = (AgentSet) variables()[VAR_BREED];
      if (breed == oldBreed) {
        return;
      }
      if (oldBreed != _world.links()) {
        ((TreeAgentSet) variables()[VAR_BREED]).remove(agentKey());
      }
    }
    if (breed != _world.links()) {
      ((TreeAgentSet) breed).add(this);
    }
    variables()[VAR_BREED] = breed;
    if (oldBreed != null) {
      _world.linkManager().addLink(this);
    }
    shape(_world.linkBreedShapes().breedShape(breed));
    realloc(null, _world.program(), oldBreed);
  }

  // returns the index of the breed of this link, 0 means a generic link;
  // this is super kludge. is there a better way? -AZS 10/28/04, ST 7/21/07
  @SuppressWarnings("unchecked")
  public int getBreedIndex() {
    AgentSet mybreed = getBreed();
    if (mybreed == _world.links()) {
      return 0;
    }
    int j = 1;
    for (Iterator<TreeAgentSet> iter = (Iterator<TreeAgentSet>)_world.linkBreeds().values().iterator(); iter.hasNext();) {
      if (mybreed == iter.next()) {
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
    if (_end1._id < otherLink._end1._id) {
      return -1;
    }
    if (_end1._id > otherLink._end1._id) {
      return 1;
    }
    if (_end2._id < otherLink._end2._id) {
      return -1;
    }
    if (_end2._id > otherLink._end2._id) {
      return 1;
    }
    if (getBreed() == otherLink.getBreed()) {
      return 0;
    }
    if (getBreed() == _world.links()) {
      return -1;
    }
    if (otherLink.getBreed() == _world.links()) {
      return 1;
    }
    // when all else fails
    // whichever breed was declared first comes first in the list.
    return _world.compareLinkBreeds(getBreed(), otherLink.getBreed());
  }

  public int alpha() {
    return org.nlogo.api.Color.getColor(color()).getAlpha();
  }

  public Turtle otherEnd(Turtle parent) {
    return _end1 == parent ? _end2 : _end1;
  }
}
