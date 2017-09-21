// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

class WorldJ {
  boolean _patchColorsDirty = false;
  boolean _patchesAllBlack = false;

  int _worldWidth = 0;
  int _worldHeight = 0;
  int _minPxcor = 0;
  int _maxPxcor = 0;
  int _minPycor = 0;
  int _maxPycor = 0;
  double _patchSize = 12.0;
}
