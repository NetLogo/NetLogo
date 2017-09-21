// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

import org.nlogo.core.AgentKind;
import org.nlogo.core.AgentKindJ;
import org.nlogo.core.WorldDimensions;
import org.nlogo.api.AgentVariables;
import org.nlogo.api.ImporterUser;
import org.nlogo.api.JobOwner;
import org.nlogo.api.WorldDimensions3D;

import java.util.Map;

public strictfp class Importer3D
    extends Importer {
  static final String MIN_PZCOR_HEADER = "MIN-PZCOR";
  static final String MAX_PZCOR_HEADER = "MAX-PZCOR";

  public Importer3D(org.nlogo.agent.ImporterJ.ErrorHandler errorHandler, World world, ImporterUser importerUser,
                    org.nlogo.agent.ImporterJ.StringReader stringReader) {
    super(errorHandler, world, importerUser, stringReader);
    TURTLE_BREED = Turtle3D.VAR_BREED3D;
  }

  @Override
  public String[] getImplicitVariables(AgentKind kind) {
    if (kind == AgentKindJ.Turtle())
      return AgentVariables.getImplicitTurtleVariables(true);
    else if (kind == AgentKindJ.Patch())
      return AgentVariables.getImplicitPatchVariables(true);
    else
      return super.getImplicitVariables(kind);
  }

  @Override
  public WorldDimensions addPatchSizeToDimensions(WorldDimensions dimensions, double patchSize) {
    if (dimensions instanceof WorldDimensions3D) {
      WorldDimensions3D dims = (WorldDimensions3D) dimensions;
      return dims.copyThreeD(
        dims.minPxcor(), dims.maxPxcor(),
        dims.minPycor(), dims.maxPycor(),
        dims.minPzcor(), dims.maxPzcor(),
        patchSize,
        dims.wrappingAllowedInX(), dims.wrappingAllowedInY(), dims.wrappingAllowedInZ());
    } else
      return super.addPatchSizeToDimensions(dimensions, patchSize);
  }

  @Override
  void checkVersion(String versionNumber)
      throws AbortingImportException {
    // we now have dashed and undashed versions of the 3-D version out there.
    // so we have to check both.
    // just don't allow importing 2D worlds into 3D NetLogo.  It's possible to
    // fix this code to work but it will contribute to the overall messification
    // of this code and it seems like a non-essential capability ev 1/11/07
    if (!versionNumber.startsWith("3-D") && !versionNumber.startsWith("3D")) {
      throw new AbortingImportException
          (ImportError.IMPORT_3D_ERROR, "");
    }
  }

  @Override
  void importDrawing()
      throws java.io.IOException {
    Drawing3D drawing = ((World3D) world).drawing();

    while (hasMoreLines(false)) {
      String[] line = nextLine();
      if (line[0].equalsIgnoreCase("x0")) {
        continue;
      }
      if (line[0].equalsIgnoreCase("shape")) {
        while (hasMoreLines(false)) {
          line = nextLine();
          if (line[1].equalsIgnoreCase("x1")) {
            while (hasMoreLines(false)) {
              try {
                line = nextLine();
                drawing.addStamp
                    (line[0],
                        ((Double) stringReader.readFromString(line[1])).doubleValue(),
                        ((Double) stringReader.readFromString(line[2])).doubleValue(),
                        ((Double) stringReader.readFromString(line[3])).doubleValue(),
                        ((Double) stringReader.readFromString(line[4])).doubleValue(),
                        ((Double) stringReader.readFromString(line[5])).doubleValue(),
                        ((Double) stringReader.readFromString(line[6])).doubleValue(),
                        stringReader.readFromString(line[7]),
                        ((Double) stringReader.readFromString(line[8])).doubleValue(),
                        ((Boolean) stringReader.readFromString(line[9])).booleanValue(),
                        ((Double) stringReader.readFromString(line[10])).doubleValue(),
                        ((Double) stringReader.readFromString(line[11])).doubleValue(),
                        ((Double) stringReader.readFromString(line[12])).doubleValue());
              } catch (StringReaderException ex) {
                throw new AbortingImportException
                    (ImportError.ILLEGAL_CLASS_CAST_ERROR, "Coordinates in the drawing must be doubles");
              }
            }
            return;
          }
          try {
            drawing.addStamp
                (line[0],
                    ((Double) stringReader.readFromString(line[1])).doubleValue(),
                    ((Double) stringReader.readFromString(line[2])).doubleValue(),
                    ((Double) stringReader.readFromString(line[3])).doubleValue(),
                    ((Double) stringReader.readFromString(line[4])).doubleValue(),
                    ((Double) stringReader.readFromString(line[5])).doubleValue(),
                    ((Double) stringReader.readFromString(line[6])).doubleValue(),
                    ((Double) stringReader.readFromString(line[7])).doubleValue(),
                    ((Double) stringReader.readFromString(line[8])).doubleValue(),
                    ((Double) stringReader.readFromString(line[9])).doubleValue());
          } catch (StringReaderException ex) {
            throw new AbortingImportException
                (ImportError.ILLEGAL_CLASS_CAST_ERROR,
                    "Coordinates in the drawing must be doubles");
          }
        }
      }
      try {
        drawing.addLine
            (((Double) stringReader.readFromString(line[0])).doubleValue(),
                ((Double) stringReader.readFromString(line[1])).doubleValue(),
                ((Double) stringReader.readFromString(line[2])).doubleValue(),
                ((Double) stringReader.readFromString(line[3])).doubleValue(),
                ((Double) stringReader.readFromString(line[4])).doubleValue(),
                ((Double) stringReader.readFromString(line[5])).doubleValue(),
                ((Double) stringReader.readFromString(line[6])).doubleValue(),
                stringReader.readFromString(line[7]));
      } catch (StringReaderException ex) {
        throw new AbortingImportException
            (ImportError.ILLEGAL_CLASS_CAST_ERROR,
                "Coordinates in the drawing must be doubles");
      }
    }
  }

  @Override
  void handleSpecialTurtleVariable(Turtle turtle, Object val, int varIndex) {
    switch (varIndex) {
      case Turtle3D.VAR_SHAPE3D:
        setTurtleShape(turtle, val, builtInVars[Turtle3D.VAR_SHAPE3D], varIndex);
        break;
      case Turtle3D.VAR_LABEL3D:
        setVarVal(turtle, varIndex, builtInVars[Turtle3D.VAR_LABEL3D], getLabel(val));
        break;
      case Turtle.VAR_WHO:
      case Turtle3D.VAR_BREED3D:
        //do nothing since we already took care of them
        break;
      default:
        throw new IllegalStateException();
    }
  }

  @Override
  void handleSpecialPatchVariable(Patch patch, Object val, int varIndex) {
    switch (varIndex) {
      case Patch3D.VAR_PLABEL3D:
        setVarVal(patch, varIndex, builtInVars[Patch3D.VAR_PLABEL3D], getLabel(val));
        break;
      case Patch3D.VAR_PXCOR3D:
      case Patch3D.VAR_PYCOR3D:
      case Patch3D.VAR_PZCOR3D:
        //do nothing since we already took care of them
        break;
      default:
        throw new IllegalStateException();
    }
  }

  //functions for special handling of variables
  @Override
  Patch getPatch(Map<String, Object> varVals) {
    try {
      World3D w = (World3D) world;
      int pxcor = ((Double) varVals.get(builtInVars[Patch3D.VAR_PXCOR3D])).intValue();
      int pycor = ((Double) varVals.get(builtInVars[Patch3D.VAR_PYCOR3D])).intValue();
      int pzcor = ((Double) varVals.get(builtInVars[Patch3D.VAR_PZCOR3D])).intValue();
      if (!w.validPatchCoordinates(pxcor, pycor, pzcor)) {
        String abortingError = "Illegal Patch Coordinate- pxcor, pycor and pzcor must be in range.";
        throw new AbortingImportException
            (ImportError.ILLEGAL_PCOR_ERROR, abortingError);
      }
      return w.fastGetPatchAt(pxcor, pycor, pzcor);
    } catch (ClassCastException cce) {
      String abortingError = "Illegal Patch Coordinate- pxcor and pycor must be integers.";
      throw new AbortingImportException
          (ImportError.ILLEGAL_CLASS_CAST_ERROR, abortingError);
    }
  }

  @Override
  WorldDimensions getScreenDimensions(Map<String, Object> varVals) {
    try {
      int minx, maxx, miny, maxy, minz, maxz;

      if (!convertTopology) {
        minx = ((Double) varVals.get(MIN_PXCOR_HEADER)).intValue();
        maxx = ((Double) varVals.get(MAX_PXCOR_HEADER)).intValue();
        miny = ((Double) varVals.get(MIN_PYCOR_HEADER)).intValue();
        maxy = ((Double) varVals.get(MAX_PYCOR_HEADER)).intValue();
        minz = ((Double) varVals.get(MIN_PZCOR_HEADER)).intValue();
        maxz = ((Double) varVals.get(MAX_PZCOR_HEADER)).intValue();
      } else {
        int sex = ((Double) varVals.get(SCREEN_EDGE_X_HEADER)).intValue();
        int sey = ((Double) varVals.get(SCREEN_EDGE_Y_HEADER)).intValue();
        minx = -sex;
        maxx = sex;
        miny = -sey;
        maxy = sey;
        minz = maxz = 0;
      }

      World3D w = (World3D) world;

      if (minx != world.minPxcor() || maxx != world.maxPxcor() ||
          miny != world.minPycor() || maxy != world.maxPycor() ||
          minz != w.minPzcor() || maxz != w.maxPzcor()) {
        needToResize = true;
        return new WorldDimensions3D(minx, maxx, miny, maxy, minz, maxz, 12.0, true, true, true);
      } else {
        return new WorldDimensions3D(
            world.minPxcor(), world.maxPxcor(),
            world.minPycor(), world.maxPycor(),
            w.minPzcor(), w.maxPzcor(), world.patchSize(), true, true, true);
      }
    } catch (ClassCastException cce) {
      String abortingError = "Illegal Screen dimension- max-px/y/zcor, min-px/y/zcor must be numbers.";
      throw new AbortingImportException
          (ImportError.ILLEGAL_CLASS_CAST_ERROR, abortingError);
    }
  }

  @Override
  public String[] getSpecialObserverVariables() {
    return new String[]{MIN_PXCOR_HEADER,
        MAX_PXCOR_HEADER,
        MIN_PYCOR_HEADER,
        MAX_PYCOR_HEADER,
        MIN_PZCOR_HEADER,
        MAX_PZCOR_HEADER,
        PERSPECTIVE_HEADER,
        SUBJECT_HEADER,
        NEXT_INDEX_HEADER,
        DIRECTED_LINKS_HEADER,
        TICKS_HEADER};
  }

  @Override
  public String[] getSpecialTurtleVariables() {
    String[] vars = AgentVariables.getImplicitTurtleVariables(true);
    return new String[]
        {vars[Turtle.VAR_WHO], vars[Turtle3D.VAR_BREED3D],
            vars[Turtle3D.VAR_LABEL3D], vars[Turtle3D.VAR_SHAPE3D]};
  }

  @Override
  public String[] getSpecialPatchVariables() {
    String[] vars = AgentVariables.getImplicitPatchVariables(true);
    return new String[]
        {vars[Patch3D.VAR_PXCOR3D], vars[Patch3D.VAR_PYCOR3D],
            vars[Patch3D.VAR_PZCOR3D], vars[Patch3D.VAR_PLABEL3D]};
  }

  @Override
  String[] getEssentialObserverVars() {
    return new String[]
        {MIN_PXCOR_HEADER,
            MAX_PXCOR_HEADER,
            MIN_PYCOR_HEADER,
            MAX_PYCOR_HEADER,
            MIN_PZCOR_HEADER,
            MAX_PZCOR_HEADER};
  }

  @Override
  String[] getEssentialPatchVariables() {
    String[] vars = AgentVariables.getImplicitPatchVariables(true);
    return new String[]
        {vars[Patch3D.VAR_PXCOR3D], vars[Patch3D.VAR_PYCOR3D],
            vars[Patch3D.VAR_PZCOR3D]};
  }
}
