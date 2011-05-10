package org.nlogo.agent;

// table lookup of square roots

strictfp class RootsTable {

  private final double[] rootsTable;

  RootsTable(int worldWidth, int worldHeight) {
    rootsTable = new double[worldWidth * worldWidth + worldHeight * worldHeight];
    for (int i = 0; i < rootsTable.length; i++) {
      rootsTable[i] = StrictMath.sqrt(i);
    }
  }

  double gridRoot(double val) {
    int intVal = (int) val;
    if (val == intVal) {
      return gridRoot(intVal);
    } else {
      return StrictMath.sqrt(val);
    }
  }

  double gridRoot(int val) {
    return val < rootsTable.length
        ? rootsTable[val]
        : StrictMath.sqrt(val);
  }

}
