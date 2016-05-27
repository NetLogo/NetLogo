// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

import org.nlogo.core.AgentKind;
import org.nlogo.core.AgentKindJ;
import org.nlogo.core.WorldDimensions;
import org.nlogo.api.AgentException;
import org.nlogo.core.AgentVariables;
import org.nlogo.core.Breed;
import org.nlogo.api.ImporterUser;
import org.nlogo.api.Perspective;
import org.nlogo.api.PerspectiveJ;

import scala.collection.Seq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract strictfp class ImporterJ
    implements org.nlogo.api.ImportErrorHandler {
  final ImporterUser importerUser;
  final ErrorHandler errorHandler;
  final World world;
  final StringReader stringReader;

  Set<Object> shapesNotToImport;
  Set<String> breedsNotToImport;
  List<String> someBreedOwns;
  List<String> someLinkBreedOwns;

  static final String SCREEN_EDGE_X_HEADER = "SCREEN-EDGE-X";
  static final String SCREEN_EDGE_Y_HEADER = "SCREEN-EDGE-Y";

  static final String MIN_PXCOR_HEADER = "MIN-PXCOR";
  static final String MAX_PXCOR_HEADER = "MAX-PXCOR";
  static final String MIN_PYCOR_HEADER = "MIN-PYCOR";
  static final String MAX_PYCOR_HEADER = "MAX-PYCOR";

  static final String PERSPECTIVE_HEADER = "PERSPECTIVE";
  static final String SUBJECT_HEADER = "SUBJECT";
  static final String NEXT_INDEX_HEADER = "NEXTINDEX";
  static final String DIRECTED_LINKS_HEADER = "DIRECTED-LINKS";
  static final String TICKS_HEADER = "TICKS";

  boolean needToResize = false;

  private boolean olderThan40beta2 = false;

  //these variables, varsToImport, builtInVars, and tooManyValuesForSection,
  //are initialized each time importAgents() is called. the variables are
  //reused for each of the three types of agents.  they should be global since
  //they are passed around a lot and it makes the code more general by not
  //forcing us to have different variables for each agent type when the
  //reusing them will suffice.
  //--mag 12/13/01, 5/2/03
  java.util.BitSet varsToImport;
  String[] builtInVars;
  boolean tooManyValuesForSection = false;

  boolean convertPenDown = false;
  boolean convertTopology = false;
  boolean importLinks = true;

  int TURTLE_BREED;

  void setupVarsToImport(int size) {
    varsToImport = new java.util.BitSet(size);
    for (int j = 0; j < size; j++) {
      varsToImport.set(j);
    }
  }

  public ImporterJ(ImporterJ.ErrorHandler errorHandler, World world, ImporterUser importerUser,
                   ImporterJ.StringReader stringReader) {
    this.errorHandler = errorHandler;
    this.world = world;
    this.importerUser = importerUser;
    this.stringReader = stringReader;

    TURTLE_BREED = Turtle.VAR_BREED;

    shapesNotToImport = new HashSet<Object>();
    breedsNotToImport = new HashSet<String>();
    someBreedOwns = getAllBreedVars();
    someLinkBreedOwns = getAllLinkBreedVars();

    specialVariables = fillSpecialVariables();
    //essentialVarHeadersToImport = fillEssentialVarsToImport() ;
  }

  void checkVersion(String versionNumber)
      throws AbortingImportException {
    if (versionNumber.startsWith("1.") ||
        versionNumber.startsWith("2.0") ||
        versionNumber.startsWith("2.1") ||
        versionNumber.startsWith("2.2pre1") ||
        versionNumber.startsWith("2.2pre2")) {
      convertPenDown = true;
      convertTopology = true;
      importLinks = false;
    } else if (versionNumber.startsWith("3.0")) {
      convertTopology = true;
      importLinks = false;
    }
    if (versionNumber.startsWith("1.") ||
        versionNumber.startsWith("2.") ||
        versionNumber.startsWith("3.")) {
      importLinks = false;
    }
    if (versionNumber.startsWith("1.") ||
        versionNumber.startsWith("2.") ||
        versionNumber.startsWith("3.") ||
        versionNumber.startsWith("4.0pre") ||
        versionNumber.startsWith("4.0alpha") ||
        versionNumber.startsWith("4.0beta1")) {
      olderThan40beta2 = true;
    }
  }

  abstract void importPlots();

  public void importWorld(java.io.BufferedReader fileBuff)
      throws java.io.IOException {
    lines = fileBuff;

    try {
      while (hasMoreLines(false)) {
        String[] line = nextLine();
        String versionHeader = "export-world data (NetLogo ";
        if (line[0].startsWith(versionHeader)) {
          String versionNumber = line[0].substring(versionHeader.length());
          checkVersion(versionNumber);
        }

        if (line[0].trim().equals("RANDOM STATE")) {
          hasMoreLines(false);
          line = nextLine();
          world.mainRNG().load(line[0]);
        }
      }

      essentialVarHeadersToImport = fillEssentialVarsToImport();

      world.clearAll();

      importAgents(AgentKindJ.Observer());
      importAgents(AgentKindJ.Turtle());
      importAgents(AgentKindJ.Patch());
      checkForBlankTurtles();
      if (importLinks) {
        importAgents(AgentKindJ.Link());
      }
      if (nextLine != null && nextLine.indexOf("DRAWING") != -1) {
        importDrawing();
      }
      if (nextLine != null && nextLine.indexOf("OUTPUT") != -1) {
        importOutputArea();
      }
      if (needToResize) {
        importerUser.resizeView();
      }
      importPlots();
      importExtensionData();
    } catch (AbortingImportException aix) {
      world.clearAll();
      if (aix.errorType != ImportError.ERROR_GIVEN) {
        showError(aix);
      }
    } catch (InvalidDataException e) {
      errorHandler.showError("Error Importing Drawing",
          "Invalid data length, the drawing will not be imported", false);
    }
  }

  void importOutputArea()
      throws java.io.IOException {
    StringBuilder outputString = new StringBuilder();

    while (hasMoreLines(false)) {
      String[] fields = nextLine();
      for (int i = 0; i < fields.length; i++) {
        outputString.append(fields[i]);
      }
    }
    if (outputString.length() > 0) {
      importerUser.setOutputAreaContents((String) getTokenValue(outputString.toString(), false, false));
    } else {
      // blank the output area
      importerUser.setOutputAreaContents("");
    }
  }

  void importDrawing()
      throws java.io.IOException {
    if (hasMoreLines(false)) {
      Double patchSize = Double.valueOf(nextLine()[0]);
      importerUser.patchSize(patchSize.doubleValue());
      importerUser.resizeView();
      needToResize = false;

      int width = (int) (patchSize.doubleValue() * world.worldWidth());
      int height = (int) (patchSize.doubleValue() * world.worldHeight());

      StringBuilder colorString = new StringBuilder(width * height * 32);

      try {
        while (hasMoreLines(false)) {
          String[] line = nextLine();
          for (int i = 0; i < line.length; i++) {
            colorString.append(stringReader.readFromString(line[i].replaceAll(",", "")));
          }
        }

        int[] colors = fromHexString(colorString.toString());

        if (colors.length != (width * height * 4)) {
          throw new InvalidDataException
              ("The data was not the correct length for the size of the world");
        }

        world.trailDrawer.setColors(colors);
      } catch (StringReaderException e) {
        throw new InvalidDataException
            ("invalid drawing data: drawing will not be imported");
      }
    }
  }

  public void importExtensionData() {
    try {
      if (hasMoreLines(false)) {
        String[] line = nextLine();
        String extensionName = line[0];
        while (hasMoreLines(false)) {
          List<String[]> lines = new ArrayList<String[]>();
          do {
            line = nextLine();
            if (importerUser.isExtensionName(line[0])) {
              break;
            }
            lines.add(line);
          } while (hasMoreLines(false));
          importerUser.importExtensionData(extensionName, lines, this);
          extensionName = line[0];
        }
      }
    } catch (org.nlogo.api.ExtensionException e) {
      errorHandler.showError("Error Importing Extension Data", e.getMessage(), false);
    } catch (java.io.IOException e) {
      errorHandler.showError("Error Importing Extension Data", e.getMessage(), false);
    }
  }

  void importAgents(AgentKind kind)
      throws java.io.IOException {
    tooManyValuesForSection = false;
    builtInVars = getImplicitVariables(kind);
    String[] headers = getHeaders(kind);

    setupVarsToImport(headers.length);
    while (hasMoreLines(false)) {
      String[] line = nextLine();
      importOneAgent(kind, line, headers);
    }
  }

  void importOneAgent(AgentKind kind, String[] line, String[] headers) {
    Map<String, Object> varVals = getVarVals(headers, line, kind);

    if (kind == AgentKindJ.Observer()) {
      setScreenDimensions(varVals);
    }
    // if there were any agentsets in the values that getVarVals() fetched,
    // then those values may have become invalid as a result of resizing
    // the world, so we'd better call getVarVals over again - ST 12/21/04
    varVals = getVarVals(headers, line, kind);
    Agent agent = nextAgent(kind, varVals);
    for (int i = 0; i < headers.length; i++) {
      String header = headers[i];
      if (isSpecialVariable(kind, header)) // breed, sex/sey, pxcor/pycor, label/plabel, who, others?
      {
        handleSpecialVariable(agent, header, varVals, i);
      } else {
        int varIndex = getVarIndex(agent, header, i);
        if (varIndex != -1) {
          Object value = varVals.get(header);
          if (value != null) {
            setVarVal(agent, varIndex, header, value);
          }
        }
        // otherwise leave default value alone
      }
    }
  }

  Agent nextAgent(AgentKind kind, Map<String, Object> varVals) {
    if (kind == AgentKindJ.Observer()) {
      return world.observer();
    }
    if (kind == AgentKindJ.Turtle()) {
      // don't use agent.setVariable() for the turtles' id and breed since
      // these are needed in order to create the right number and types of
      // variables for the turtle.  --mag 3/25/03
      AgentSet breed =
          getTurtleBreed(varVals,
              builtInVars[TURTLE_BREED]);
      long id = getTurtleId(varVals, builtInVars[Turtle.VAR_WHO]);
      Turtle turtle = world.getOrCreateTurtle(id);
      turtle.setBreed(breed);
      return turtle;
    }
    if (kind == AgentKindJ.Patch()) {
      // don't use agent.setVariable() for the patches' pxcor and pycor since
      // these are needed in order to create the right patch.  --mag 3/25/03
      return getPatch(varVals);
    }
    if (kind == AgentKindJ.Link()) {
      AgentSet breed =
          getLinkBreed(varVals,
              builtInVars[Link.VAR_BREED]);
      Turtle end1 = getLinkEnd(varVals, builtInVars[Link.VAR_END1]);
      Turtle end2 = getLinkEnd(varVals, builtInVars[Link.VAR_END2]);
      return world.getOrCreateLink(end1, end2, breed);
    }

    // there are no other agents
    return null;
  }

  void handleSpecialVariable(Agent agent, String header, Map<String, Object> varVals, int headerIndex) {
    try {
      if (!(agent instanceof Observer)) {
        int varIndex = getVarIndex(agent, header, headerIndex);
        if (varIndex != -1) {
          if (agent instanceof Turtle) {
            handleSpecialTurtleVariable((Turtle) agent, varVals.get(header), varIndex);
          } else if (agent instanceof Patch) {
            handleSpecialPatchVariable((Patch) agent, varVals.get(header), varIndex);
          } else if (agent instanceof Link) {
            handleSpecialLinkVariable((Link) agent, varVals.get(header), varIndex, header);
          }
        }
      } else {
        handleSpecialObserverVariable((Observer) agent, varVals.get(header), header);
      }
    } catch (ImportException ix) {
      showError(ix);
    }
  }

  int perspectiveType = 0;

  void handleSpecialObserverVariable(Observer observer, Object val, String header) {
    if (header.equals(PERSPECTIVE_HEADER)) {
      perspectiveType = ((Double) val).intValue();
    } else if (header.equals(SUBJECT_HEADER) && val instanceof Agent) {
      int followDistance = 0;
      if (perspectiveType == PerspectiveJ.FOLLOW) {
        followDistance = 5;
      }
      Perspective newPerspective = PerspectiveJ.create(perspectiveType, (Agent) val, followDistance);
      observer.setPerspective(newPerspective);
    } else if (header.equals(NEXT_INDEX_HEADER)) {
      world.nextTurtleIndex(((Double) val).longValue());
    } else if (header.equals(DIRECTED_LINKS_HEADER)) {
      String str = (String) val;
      if (!str.equals("NEITHER")) {
        world.links().setDirected(str.equals("DIRECTED"));
      }
    } else if (header.equals(TICKS_HEADER)) {
      world.tickCounter.ticks_$eq(((Double) val).doubleValue());
    }
  }

  void handleSpecialTurtleVariable(Turtle turtle, Object val, int varIndex) {
    switch (varIndex) {
      case Turtle.VAR_SHAPE:
        setTurtleShape(turtle, val, builtInVars[Turtle.VAR_SHAPE], varIndex);
        break;
      case Turtle.VAR_LABEL:
        setVarVal(turtle, varIndex, builtInVars[Turtle.VAR_LABEL], getLabel(val));
        break;
      case Turtle.VAR_WHO:
      case Turtle.VAR_BREED:
        //do nothing since we already took care of them
        break;
      default:
        throw new IllegalStateException();
    }
  }

  void handleSpecialLinkVariable(Link link, Object val, int varIndex, String header) {
    switch (varIndex) {
      case Link.VAR_LABEL:
        setVarVal(link, varIndex, builtInVars[Link.VAR_LABEL], getLabel(val));
        break;
      case Link.VAR_END1:
      case Link.VAR_END2:
      case Link.VAR_BREED:
        break;
      default:
        throw new IllegalStateException();
    }
  }

  void handleSpecialPatchVariable(Patch patch, Object val, int varIndex) {
    switch (varIndex) {
      case Patch.VAR_PLABEL:
        setVarVal(patch, varIndex, builtInVars[Patch.VAR_PLABEL], getLabel(val));
        break;
      case Patch.VAR_PXCOR:
      case Patch.VAR_PYCOR:
        //do nothing since we already took care of them
        break;
      default:
        throw new IllegalStateException();
    }
  }


  //
  // utility functions:
  //

  //getting and setting variables

  //given the set of lines of the file, the delimiter for the values in a line, and the type of agent
  //(or Globals for the observer) for these headers,
  //this will return an array of uppercase strings containing the headers to be imported for this agent
  String[] getHeaders(AgentKind kind)
      throws java.io.IOException {
    if (!hasMoreLines(false)) {
      String abortingError = "No " + printName(kind) + " headers have been imported. " +
          "Globals, Turtles, and Patches must be in the same import file.";
      throw new AbortingImportException
          (ImportError.UNEXPECTED_EOF_ERROR, abortingError);
    }
    String[] mixedCaseHeaders = nextLine();
    List<String> headers = new ArrayList<String>();
    for (int i = 0; i < mixedCaseHeaders.length; i++) {
      // ignore blank fields in headers
      if (!mixedCaseHeaders[i].trim().equals("")) {
        if (convertPenDown && mixedCaseHeaders[i].equalsIgnoreCase("PEN-DOWN?")) {
          headers.add("PEN-MODE");
        } else {
          headers.add(mixedCaseHeaders[i].toUpperCase());
        }
      }
    }
    String[] headersArr = headers.toArray(new String[headers.size()]);
    varHeadersImported(kind, headersArr, true);
    varHeadersImported(kind, headersArr, false);
    return headersArr;
  }

  // don't ask the user about these headers when we're
  // importing from an old export file
  // we know they are not there and should be set to a default
  List<String> getOptionalHeaders(AgentKind kind) {
    if (convertPenDown && kind == AgentKindJ.Turtle()) {
      return Arrays.asList(new String[]
          {"PEN-SIZE", "PEN-COLOR"});
    }
    if (olderThan40beta2 && kind == AgentKindJ.Link()) {
      return Arrays.asList(new String[]
          {"SHAPE", "TIE-MODE"});
    } else if (kind == AgentKindJ.Link()) {
      return Arrays.asList(new String[]
          {"TIE-MODE"});
    }
    return null;
  }


  //given an array of headers, a comma-delimited string of the agent's values
  //for each header, and the agent's class, this will return a map of
  //the parsed values keyed by the variable header.  if an essential variable
  //for this agent type does not have a valid value, this will throw an
  //abortingexception
  Map<String, Object> getVarVals(String[] headersArr, String[] values, AgentKind kind) {
    Map<String, Object> varVals = new HashMap<String, Object>();

    if (!tooManyValuesForSection && values.length > headersArr.length) {
      // Only warn the user if one of the extra values is non empty
      for (int i = headersArr.length; i < values.length; i++) {
        if (!values[i].equals("")) {
          tooManyValuesForSection = true;
          showError(new ImportException(
              ImportError.TOO_MANY_VALUES_ERROR,
              "Too Many Values For Agent",
              "There are a total of " + headersArr.length + " "
                  + printName(kind) + " variables declared in this "
                  + "model (including built-in " +
              ((kind == AgentKindJ.Turtle() || kind == AgentKindJ.Link())
                      ? "and breed " : "")
                  + "variables).  The import-world file has at least one agent "
                  + "in the " + printSectionName()
                  + " section with more than this number of values.",
              "All the extra values will be ignored for this section."));
        }
      }
    }

    for (int i = 0; i < headersArr.length; i++) {
      // handle turtle breeds specially so that we can give a different
      // error message if something there is an error
      boolean turtleBreedVar =
        (kind == AgentKindJ.Turtle()) && (headersArr[i].equals(builtInVars[TURTLE_BREED]));

      boolean linkBreedVar =
        (kind == AgentKindJ.Link()) && (headersArr[i].equals
              (builtInVars[Link.VAR_BREED]));

      if (convertPenDown && headersArr[i].equals("PEN-MODE")) {
        if (values[i].equalsIgnoreCase("FALSE")) {
          values[i] = "\"up\"";
        } else if (values[i].equals("TRUE")) {
          values[i] = "\"down\"";
        }
      }

      Object value = values[i].equals("") ? new Junk() :
          getTokenValue(values[i], turtleBreedVar, linkBreedVar);

      // check to see if this variable is an essential variable and if it is,
      // that we were actually able to get a valid value for it.
      if (essentialVarHeadersToImport.get(kind).contains(headersArr[i])
          && (value instanceof Junk)) {
        String abortingError = "A " + printName(kind) +
            " with the essential variable " + headersArr[i] +
            " cannot be imported since the agent's value in the import" +
            " file for " + headersArr[i] + " could not be imported.";
        throw new AbortingImportException
            (ImportError.UNIMPORTED_ESSENTIAL_VAR_ERROR, abortingError);
      }
      varVals.put(headersArr[i], value);
    }
    return varVals;
  }

  boolean validBreed(String breed) {
    return (world.getBreed(breed.toUpperCase()) != null)
        || breed.equalsIgnoreCase("TURTLES")
        || breed.equalsIgnoreCase("PATCHES")
        || breed.equalsIgnoreCase("LINKS");
  }

  // if we have troubles parsing the value and the user doesn't care, return
  // an instance of Junk.  we will replace it later in the import with an
  // appropriate value.
  // default access for unit testing
  Object getTokenValue(String valueString, boolean turtleBreedVar, boolean linkBreedVar) {

    try {
      return stringReader.readFromString(valueString);
    } catch (StringReaderException ex) {
      // this is where we should look for ExtensionTypes that
      // can handle this value -- CLB

      if (turtleBreedVar) {
        if (!breedsNotToImport.contains(valueString)) {
          breedsNotToImport.add(valueString);
          showError(
              new ImportException(
                  ImportError.ILLEGAL_BREED_ERROR,
                  "Illegal Breed",
                  ex.getMessage(),
                  "all turtles with this breed will be made as regular turtles"));
        }
        return world.turtles();
      } else if (linkBreedVar) {
        if (!breedsNotToImport.contains(valueString)) {
          breedsNotToImport.add(valueString);
          showError(
              new ImportException
                  (ImportError.ILLEGAL_BREED_ERROR,
                      "Illegal Link Breed",
                      ex.getMessage(),
                      "all links with this breed will be made as regular links"));
        }
        return world.links();
      } else {
        showError(
            new ImportException(
                ImportError.PARSING_ERROR,
                "Parsing Error",
                "error parsing the values:\n" + valueString,
                "the import will continue if it can, but values for this " +
                    "agent's variables will be set to an appropriate default",
                ex.getMessage()));
      }
    }
    return new Junk();
  }

  //given an agent, a header of the variable being imported, and the index of that header in the headers array
  //this will return the index to the variables array of the variable at headerIndex in the import file
  //or it will return -1 if the variable at headerIndex is not in this agent's set of variables
  int getVarIndex(Agent agent, String header, int headerIndex) {
    AgentKind kind = agent.kind();
    int varIndex = Arrays.asList(builtInVars).indexOf(header);
    String agentType = printName(kind);
    //check to see if the variable is a built-in variable agent variable
    if (varIndex == -1) {
      if (kind == AgentKindJ.Observer()) {
        varIndex = world.observerOwnsIndexOf(header);
      } else if (kind == AgentKindJ.Patch()) {
        varIndex = world.patchesOwnIndexOf(header);
      } else if (kind == AgentKindJ.Turtle()) {
        varIndex = world.turtlesOwnIndexOf(header);
        //check to see if the variable is a turtlesOwn variable
        if (varIndex == -1) {
          varIndex = getBreedVarIndex((Turtle) agent, header);
          if (varIndex == -1 && someBreedOwns.contains(header)) {
            // return -1 since some breed in the model owns this var
            return -1;
          }
        }
      } else if (kind == AgentKindJ.Link()) {
        varIndex = world.linksOwnIndexOf(header);
        if (varIndex == -1) {
          varIndex = getLinkBreedVarIndex((Link) agent, header);
          if (varIndex == -1 && someLinkBreedOwns.contains(header)) {
            // return -1 since some breed in the model owns this var
            return -1;
          }
        }
      }
    }
    //if none do, check to see if we are still importing this variable.
    //if we are, throw an error.
    if ((varsToImport.get(headerIndex)) && (varIndex == -1)) {
      varsToImport.clear(headerIndex);
      showError(
          new ImportException(
              ImportError.ILLEGAL_AGENT_VAR_ERROR,
              "Illegal " + agentType + " Variable",
              "the " + agentType + " variable " + header + " does not " +
                  "exist in this model.",
              "the import will continue but this variable will be ignored."));
    }
    return varIndex;
  }

  //given a turtle, the header for the variable being imported, and the index to the header in the headers array
  //this will return the index to the location in the variables array for this breed variable
  //or it will return -1 if it is not this turtle's breeds variable
  int getBreedVarIndex(Turtle turtle, String header) {
    //check to see if this turtle is a breed or not and check to see if this turtle's breed
    //has this variable in it
    if (turtle.getBreed() != world.turtles() && world.breedOwns(turtle.getBreed(), header)) {
      //if it does, get the index for it and return it
      return world.breedsOwnIndexOf(turtle.getBreed(), header);
    }

    //since this variable is not in this turtle's set of variables, return -1
    return -1;
  }

  int getLinkBreedVarIndex(Link link, String header) {
    if (link.getBreed() != world.links() && world.linkBreedOwns(link.getBreed(), header)) {
      return world.linkBreedsOwnIndexOf(link.getBreed(), header);
    }

    return -1;
  }

  void setVarVal(Agent agent, int index, String header, Object value) {
    try {
      if (value instanceof Junk) {
        value = World.ZERO;
      }
      agent.setVariable(index, value);
    } catch (AgentException ae) {
      showError(
          new ImportException(
              ImportError.SETTING_VAR_ERROR,
              "Error Setting Value",
              "could not set " + agent + "'s variable " + header +
                  " to " + value,
              "the import will continue, but the variable will be set " +
                  "to an appropriate default."));

    } catch (org.nlogo.api.LogoException ae) {
      showError(
          new ImportException(
              ImportError.SETTING_VAR_ERROR,
              "Error Setting Value",
              "could not set " + agent + "'s variable " + header +
                  " to " + value,
              "the import will continue, but the variable will be set " +
                  "to an appropriate default."));
    }
  }

  //functions for special handling of variables
  Patch getPatch(Map<String, Object> varVals) {
    try {
      int pxcor = ((Double) varVals.get(builtInVars[Patch.VAR_PXCOR])).intValue();
      int pycor = ((Double) varVals.get(builtInVars[Patch.VAR_PYCOR])).intValue();
      if (!world.validPatchCoordinates(pxcor, pycor)) {
        String abortingError = "Illegal Patch Coordinate- pxcor and pycor must be in range.";
        throw new AbortingImportException
            (ImportError.ILLEGAL_PCOR_ERROR, abortingError);
      }
      return world.fastGetPatchAt(pxcor, pycor);
    } catch (ClassCastException cce) {
      String abortingError = "Illegal Patch Coordinate- pxcor and pycor must be integers.";
      throw new AbortingImportException
          (ImportError.ILLEGAL_CLASS_CAST_ERROR, abortingError);
    }
  }

  long getTurtleId(Map<String, Object> varVals, String whoHeaderName) {
    try {
      return ((Double) varVals.get(whoHeaderName)).longValue();
    } catch (ClassCastException cce) {
      String abortingError = "Illegal Who- a turtle's who must be an integer.";
      throw new AbortingImportException
          (ImportError.ILLEGAL_CLASS_CAST_ERROR, abortingError);
    }
  }

  long getLinkId(Map<String, Object> varVals, String whoHeaderName) {
    try {
      return ((Double) varVals.get(whoHeaderName)).longValue();
    } catch (ClassCastException cce) {
      String abortingError = "Illegal lwho- a link's who must be an integer.";
      throw new AbortingImportException
          (ImportError.ILLEGAL_CLASS_CAST_ERROR, abortingError);
    }
  }

  Turtle getLinkEnd(Map<String, Object> varVals, String headerName) {
    try {
      return (Turtle) varVals.get(headerName);
    } catch (ClassCastException cce) {
      String abortingError = "Illegal End a link's end points must be a turtle.";
      throw new AbortingImportException
          (ImportError.ILLEGAL_CLASS_CAST_ERROR, abortingError);
    }
  }

  void setTurtleShape(Turtle turtle, Object shape, String header, int varIndex) {
    try {
      turtle.setVariable(varIndex, shape);
    } catch (AgentException ae) {
      if (!shapesNotToImport.contains(shape)) {
        shapesNotToImport.add(shape);
        throw new ImportException(
            ImportError.ILLEGAL_SHAPE_ERROR,
            "Illegal Shape",
            ae.getMessage(),
            "setting " + turtle + "'s shape to its breed's default shape");
      }
      setVarVal(turtle, varIndex, header, world.turtleBreedShapes.breedShape
          (turtle.getBreed()));
    }
  }

  AgentSet getTurtleBreed(Map<String, Object> varVals, String breedHeaderName) {
    if (varVals.containsKey(breedHeaderName)) {
      return (AgentSet) varVals.get(breedHeaderName);
    }
    return world.turtles();
  }

  AgentSet getLinkBreed(Map<String, Object> varVals, String breedHeaderName) {
    if (varVals.containsKey(breedHeaderName)) {
      return (AgentSet) varVals.get(breedHeaderName);
    }
    return world.links();
  }

  Object getLabel(Object val) {
    if (val instanceof Junk) {
      return "";
    }
    return val;
  }

  void setScreenDimensions(Map<String, Object> varVals) {
    try {
      int minx, maxx, miny, maxy;

      if (!convertTopology) {
        minx = ((Double) varVals.get(MIN_PXCOR_HEADER)).intValue();
        maxx = ((Double) varVals.get(MAX_PXCOR_HEADER)).intValue();
        miny = ((Double) varVals.get(MIN_PYCOR_HEADER)).intValue();
        maxy = ((Double) varVals.get(MAX_PYCOR_HEADER)).intValue();
      } else {
        int sex = ((Double) varVals.get(SCREEN_EDGE_X_HEADER)).intValue();
        int sey = ((Double) varVals.get(SCREEN_EDGE_Y_HEADER)).intValue();
        minx = -sex;
        maxx = sex;
        miny = -sey;
        maxy = sey;
      }

      if (minx != world.minPxcor() || maxx != world.maxPxcor() ||
          miny != world.minPycor() || maxy != world.maxPycor()) {
        importerUser.setDimensions(new WorldDimensions(minx, maxx, miny, maxy));
        needToResize = true;
      }
    } catch (ClassCastException cce) {
      String abortingError = "Illegal Screen dimension- max-px/ycor, min-px/ycor must be numbers.";
      throw new AbortingImportException
          (ImportError.ILLEGAL_CLASS_CAST_ERROR, abortingError);
    }
  }

  abstract List<String> getAllVars(scala.collection.immutable.ListMap<String, Breed> breeds);

  List<String> getAllBreedVars() {
    return getAllVars(world.program().breeds());
  }

  List<String> getAllLinkBreedVars() {
    return getAllVars(world.program().linkBreeds());
  }

  Map<AgentKind, List<String>> specialVariables;

  //fill the specialVariables map with the strings of the headers that need special handling to have a
  //successful import
  Map<AgentKind, List<String>> fillSpecialVariables() {
    Map<AgentKind, List<String>> result =
        new HashMap<AgentKind, List<String>>();
    List<String> specialObserverVars = stringArrayToList(getSpecialObserverVariables());

    List<String> specialTurtleVars = stringArrayToList(getSpecialTurtleVariables());

    List<String> specialPatchVars = stringArrayToList(getSpecialPatchVariables());

    List<String> specialLinkVars = stringArrayToList(getSpecialLinkVariables());

    result.put(AgentKindJ.Observer(), specialObserverVars);
    result.put(AgentKindJ.Turtle(), specialTurtleVars);
    result.put(AgentKindJ.Patch(), specialPatchVars);
    result.put(AgentKindJ.Link(), specialLinkVars);

    return result;
  }

  private List<String> stringArrayToList(String[] vars) {
    List<String> list = new ArrayList<String>(vars.length);
    for (int i = 0; i < vars.length; i++) {
      list.add(vars[i]);
    }
    return list;
  }

  abstract String[] getSpecialObserverVariables();
  abstract String[] getSpecialTurtleVariables();
  abstract String[] getSpecialPatchVariables();
  abstract String[] getSpecialLinkVariables();

  boolean isSpecialVariable(AgentKind kind, String header) {
    return specialVariables.get(kind).contains(header);
  }

  Map<AgentKind, List<String>> essentialVarHeadersToImport;

  //fill the essentialVarHeadersToImport map with the strings of the headers that are essential to a
  //successful import
  Map<AgentKind, List<String>> fillEssentialVarsToImport() {
    Map<AgentKind, List<String>> result =
        new HashMap<AgentKind, List<String>>();
    List<String> essentialObserverVarHeaders;

    if (!convertTopology) {
      essentialObserverVarHeaders = stringArrayToList(getEssentialObserverVars());
    } else {
      essentialObserverVarHeaders = stringArrayToList(getEssentialObserverVarsOld());
    }

    List<String> essentialTurtleVarHeaders = stringArrayToList(getEssentialTurtleVariables());

    List<String> essentialPatchVarHeaders = stringArrayToList(getEssentialPatchVariables());

    List<String> essentialLinkVarHeaders = stringArrayToList(getEssentialLinkVariables());

    result.put(AgentKindJ.Observer(), essentialObserverVarHeaders);
    result.put(AgentKindJ.Turtle(), essentialTurtleVarHeaders);
    result.put(AgentKindJ.Patch(), essentialPatchVarHeaders);
    result.put(AgentKindJ.Link(), essentialLinkVarHeaders);

    return result;
  }

  String[] getEssentialObserverVars() {
    return new String[]
        {MIN_PXCOR_HEADER,
            MAX_PXCOR_HEADER,
            MIN_PYCOR_HEADER,
            MAX_PYCOR_HEADER};
  }

  String[] getEssentialObserverVarsOld() {
    return new String[]
        {SCREEN_EDGE_X_HEADER,
            SCREEN_EDGE_Y_HEADER};
  }

  String[] getEssentialTurtleVariables() {
    return new String[]
    {AgentVariables.getImplicitTurtleVariables()[Turtle.VAR_WHO]};
  }

  String[] getEssentialPatchVariables() {
    String[] vars = AgentVariables.getImplicitPatchVariables();
    return new String[]{
      vars[Patch.VAR_PXCOR], vars[Patch.VAR_PYCOR]};
  }

  String[] getEssentialLinkVariables() {
    String[] vars = AgentVariables.getImplicitLinkVariables();
    return new String[]{
      vars[Link.VAR_END1], vars[Link.VAR_END2]};
  }

  //if essentialHeaders is true, if this returns successfully, then
  //all the essential headers for the variables for the agent kind
  //have been imported.  if essentialHeaders is false, if this returns
  //successfully, either all headers for the variables for the agent
  //kind are there or the user doesn't care if the variables for the
  //missing headers are set to an appropriate default
  void varHeadersImported(AgentKind kind, String[] headers, boolean essentialHeaders) {
    List<String> headersToCheckFor =
        essentialHeaders
            ? essentialVarHeadersToImport.get(kind)
            : Arrays.asList(builtInVars);
    List<String> optionalHeaders = getOptionalHeaders(kind);

    for (int i = 0; i < headersToCheckFor.size(); i++) {
      String header = headersToCheckFor.get(i);
      boolean foundHeader = false;
      for (int j = 0; j < headers.length; j++) {
        if (header.equals(headers[j])) {
          foundHeader = true;
          break;
        }
      }
      if (!foundHeader) {
        if (essentialHeaders) {
          String abortingError = header + " is not in the list of variables to be imported " +
              "from the import file in the " + printSectionName() + " section. " +
              "This variable is essential to a model.";
          throw new AbortingImportException
              (ImportError.UNDECLARED_ESSENTIAL_VAR_ERROR, abortingError);
        } else if (optionalHeaders == null || !optionalHeaders.contains(header)) {
          showError(
              new ImportException(
                  ImportError.UNDECLARED_AGENT_VAR_ERROR,
                  "Implicit Variable Not Declared",
                  "the " + printName(kind) + " variable " +
                      header + " was not declared.",
                  "the import will continue but all agents with this " +
                      "variable will have it set to an appropriate default."));
        }
      }
    }
  }

  abstract String[] getImplicitVariables(AgentKind kind);

  //code to handle peek for the StringTokenizer

  int lineNum = 0;
  String nextLine;  //the next line of input from the import file
  private String[] nextLineFields;
  private static final int REQUIRED_SECTIONS = 3;
  private final String[] sentinels = {"GLOBALS", "TURTLES", "PATCHES", "LINKS", "DRAWING", "OUTPUT", "PLOTS", "EXTENSIONS", "DONE",};
  private final int numSentinels = sentinels.length - 1;
  private int currentSentinel = 0;

  java.io.BufferedReader lines;

  /**
   * @return whether there more lines in the current section, that
   *         is, if we have not yet reached the next sentinel (or reached end
   *         of file)
   */
  public boolean hasMoreLines(boolean returnBlankLines)
      throws java.io.IOException {
    nextLine = lines.readLine();
    lineNum++;
    if (nextLine == null)  // needed to cope with cross-platform line terminator weirdness
    {
      if (currentSentinel != numSentinels) {
        // older export files are not going to have a drawing section
        // it's ok.  just move on. ev 6/20/05
        // Also, not all exports are going to have an output area, CLB /7/15/05
        // old exports will not have LINKS sections either -- CLB 12/28/2005
        // ooh, plots too. ev 7/7/06
        if (sentinels[currentSentinel].equals("DRAWING") ||
            sentinels[currentSentinel].equals("LINKS") ||
            sentinels[currentSentinel].equals("OUTPUT") ||
            sentinels[currentSentinel].equals("PLOTS") ||
            sentinels[currentSentinel].equals("EXTENSIONS")) {
          currentSentinel++;
          return false;
        }
        String abortingError = "No " + sentinels[currentSentinel] +
            " have been imported.  Globals, Turtles, and Patches " +
            "must be in the same import file.";
        throw new AbortingImportException
            (ImportError.UNEXPECTED_EOF_ERROR, abortingError);
      }
      return false;
    }
    if (nextLine.equals(""))  // eat up blank lines
    {
      // changing the rules a little it's possible
      // to use blank lines as subsection delimiters
      // if you choose
      if (returnBlankLines) {
        return false;
      }
      return hasMoreLines(false);
    }
    try {
      nextLineFields = ImportLexer.lex(nextLine);
    } catch (ImportLexer.LexerException le) {
      throw new AbortingImportException
          (ImportError.CSV_LEXING_ERROR,
              "At line " + lineNum + ": " + le.getMessage());
    }
    if (nextLineFields.length <= 0) {
      if (returnBlankLines) {
        return true;
      } else {
        return hasMoreLines(returnBlankLines);
      }
    }
    if (nextLineFields[0].toUpperCase().startsWith(sentinels[currentSentinel])) {
      currentSentinel++;
      return false;
    } else {
      if (anotherSentinelEquals(nextLineFields[0].toUpperCase()) && lineNum > 3) {
        // only throw an error if we are still in the required sections
        // phase of the import file
        if (currentSentinel < REQUIRED_SECTIONS) {
          String abortingError = "The agents are in the wrong order in the import file. " +
              "The global variables should be first, followed by the turtles, " +
              "followed by the patches.  Found " + nextLineFields[0] +
              " but needed " + sentinels[currentSentinel];
          throw new AbortingImportException
              (ImportError.FILE_STRUCTURE_ERROR, abortingError);
        }
        return false;
      }
      return true;
    }
  }

  boolean anotherSentinelEquals(String line) {
    for (int i = 0; i < currentSentinel; i++) {
      if (line.equals(sentinels[i])) {
        return true;
      }
    }
    for (int i = currentSentinel + 1; i < numSentinels; i++) {
      if (line.equals(sentinels[i])) {
        return true;
      }
    }
    return false;
  }

  String[] nextLine() {
    return nextLineFields;
  }

  public String next() {
    return nextLine;
  }

  //misc functions
  String printSectionName() {
    return ((currentSentinel > 0) ? sentinels[currentSentinel - 1] : "UNKNOWN");
  }

  String printName(AgentKind kind) {
    if (kind == AgentKindJ.Observer()) {
      return "Global";
    }
    if (kind == AgentKindJ.Turtle()) {
      return "Turtle";
    }
    if (kind == AgentKindJ.Patch()) {
      return "Patch";
    }
    if (kind == AgentKindJ.Link()) {
      return "Link";
    }
    // there are no other agents
    return "";
  }

  void checkForBlankTurtles() {
    for (AgentIterator iter = world.turtles().iterator(); iter.hasNext();) {
      Turtle turtle = (Turtle) iter.next();
      if (turtle.getBreed() == null) {
        String abortingError = turtle.toString() + " was referenced in an agentset or agent " +
            "but was not defined in the TURTLES section.";
        throw new AbortingImportException
            (ImportError.BLANK_TURTLE_ERROR, abortingError);
      }
    }
  }

  static enum ImportError {
    // nonfatal
    ILLEGAL_AGENT_VAR_ERROR,
    ILLEGAL_SHAPE_ERROR,
    ILLEGAL_BREED_ERROR,
    PARSING_ERROR,
    SETTING_VAR_ERROR,
    UNDECLARED_AGENT_VAR_ERROR,
    TOO_MANY_VALUES_ERROR,
    // pseudo-error, used for separating into fatal and nonfatal
    LAST_NONFATAL_ERROR,
    // fatal
    ILLEGAL_CLASS_CAST_ERROR,
    UNEXPECTED_EOF_ERROR,
    ERROR_GIVEN,
    FILE_STRUCTURE_ERROR,
    UNDECLARED_ESSENTIAL_VAR_ERROR,
    UNIMPORTED_ESSENTIAL_VAR_ERROR,
    BLANK_TURTLE_ERROR,
    CSV_LEXING_ERROR,
    ILLEGAL_PCOR_ERROR,
    UNKNOWN_ERROR
  }

  strictfp class ImportException extends RuntimeException {
    public ImportError type;
    public String message;
    public String action;
    public String title;

    public ImportException(ImportError errorType, String errorTitle,
                           String errorMessage, String defaultAction) {
      super(errorTitle + "- Error Type: " + errorType);
      type = errorType;
      title = errorTitle;
      message = errorMessage;
      action = defaultAction;
    }

    public ImportException(ImportError errorType, String errorTitle, String
        errorMessage, String defaultAction, String additionalInfo) {
      this(errorType, errorTitle, errorMessage, defaultAction);
      message += "\n\nAdditional Information: " + additionalInfo;
    }
  }

  static final String NO_DETAILS = "";

  static class AbortingImportException extends RuntimeException {
    ImportError errorType;
    public String title;
    public String details;

    public AbortingImportException(ImportError errorType, String details) {
      super("Fatal Error Type:" + errorType);
      this.errorType = errorType;
      title = "Fatal Error- " + getErrorMessage();
      this.details = details + "\n\nThe import will now abort.";
    }

    String getErrorMessage() {
      String message;
      switch (errorType) {
        case ERROR_GIVEN:
          message = "Error Already Given";
          break;
        case ILLEGAL_CLASS_CAST_ERROR:
          message = "Illegal Type Cast";
          break;
        case UNEXPECTED_EOF_ERROR:
          message = "Unexpected End of File";
          break;
        case FILE_STRUCTURE_ERROR:
          message = "Incorrect Structure For Import File";
          break;
        case UNDECLARED_ESSENTIAL_VAR_ERROR:
          message = "Essential Variable Not Declared";
          break;
        case UNIMPORTED_ESSENTIAL_VAR_ERROR:
          message = "Essential Variable Not Imported";
          break;
        case BLANK_TURTLE_ERROR:
          message = "Referenced Turtle Not Defined";
          break;
        case CSV_LEXING_ERROR:
          message = "Invalid CSV File";
          break;
        default:
          message = "Unknown Fatal Error";
      }
      return message;
    }
  }

  //displaying errors of some kind
  void showError(ImportException ix) {
    if (ix.type.compareTo(ImportError.LAST_NONFATAL_ERROR) > 0) {
      throw new AbortingImportException
          (ImportError.UNKNOWN_ERROR, "An unknown error has occurred. The import will now abort.");
    }
    if (!errorHandler.showError("Warning: " + ix.title,
        "Error Importing at Line " + lineNum + ": " + ix.message +
            "\n\nAction to be Taken: " + ix.action, false)) {
      throw new AbortingImportException
          (ImportError.ERROR_GIVEN, NO_DETAILS);
    }
  }

  public void showError(String title, String message, String defaultAction) {
    showError
        (new ImportException(ImportError.PARSING_ERROR, title, message, defaultAction));
  }

  void showError(AbortingImportException aix) {
    errorHandler.showError(aix.title, aix.details, true);
  }


  ///

  Object readFromString(String s)
      throws StringReaderException {
    return stringReader.readFromString(s);
  }

  public interface StringReader {
    Object readFromString(String s)
        throws StringReaderException;
  }

  public static strictfp class StringReaderException
      extends Exception {
    public StringReaderException(String message) {
      super(message);
    }
  }

  public interface ErrorHandler {
    /**
     * display an error to the user in an appropriate manner.
     *
     * @param title        indicates the type of error.
     * @param errorDetails indicates the details of the error and what
     *                     course of action will be pursued.
     * @param fatalError   <code>true</code> if this error is considered a
     *                     fata error. <code>false</code> otherwise.
     * @return <code>true</code> if the user wishes to continue with the
     *         action suggested in the <code>errorDetails</code>. <code>false</code>
     *         if the user doesn't want to continue. if <code>fatalError</code> is
     *         <code>true</code>, the return value will be ignored.
     */
    boolean showError(String title, String errorDetails,
                      boolean fatalError);
  }

  // default access for unit testing
  static strictfp class Junk {
  }

  ///

  static class InvalidDataException
      extends java.io.IOException {
    public InvalidDataException(String message) {
      super(message);
    }
  }

  ///

  private static int[] fromHexString(String s)
      throws InvalidDataException {
    int stringLength = s.length();
    int[] ints = new int[stringLength / 8];

    // if it isn't a multiple of four we can't convert it.
    if ((stringLength % 8) == 0) {
      for (int i = 0, j = 0; j < (stringLength / 8); j++) {
        ints[j] = charToNibble(s.charAt(i++)) << 28;
        ints[j] |= charToNibble(s.charAt(i++)) << 24;
        ints[j] |= charToNibble(s.charAt(i++)) << 20;
        ints[j] |= charToNibble(s.charAt(i++)) << 16;
        ints[j] |= charToNibble(s.charAt(i++)) << 12;
        ints[j] |= charToNibble(s.charAt(i++)) << 8;
        ints[j] |= charToNibble(s.charAt(i++)) << 4;
        ints[j] |= charToNibble(s.charAt(i++));
      }
    } else {
      throw new InvalidDataException
          ("The data must be a multiple of 4 to covert from Hex string to ints");
    }

    return ints;
  }

  /**
   * convert a single char to corresponding nibble.
   *
   * @param c char to convert. must be 0-9 a-f A-F, no
   *          spaces, plus or minus signs.
   * @return corresponding integer
   */
  private static int charToNibble(char c) {
    if ('0' <= c && c <= '9') {
      return c - '0';
    } else if ('a' <= c && c <= 'f') {
      return c - 'a' + 0xa;
    } else if ('A' <= c && c <= 'F') {
      return c - 'A' + 0xa;
    } else {
      throw new IllegalArgumentException("Invalid hex character: " + c);
    }
  }

}
