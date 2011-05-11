package org.nlogo.agent;

// only used as a place holder by agent monitors
public strictfp class DummyLink
    extends Link {
  public DummyLink(World world, Object end1, Object end2, AgentSet breed) {
    super(world, (Turtle) (end1 instanceof Turtle ? end1 : null),
        (Turtle) (end2 instanceof Turtle ? end2 : null),
        world.getLinkVariablesArraySize(breed));
    variables[VAR_BREED] = breed;
    variables[VAR_END1] = end1;
    variables[VAR_END2] = end2;
  }

  @Override
  public String toString() {
    String str = world.getLinkBreedSingular(getBreed()).toLowerCase();
    str += " ";
    if (end1() == null) {
      str += "? ";
    } else {
      str += (end1().id + " ");
    }
    if (end2() == null) {
      str += "?";
    } else {
      str += end2().id;
    }
    return str;
  }
}
