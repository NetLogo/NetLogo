// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

// We have WorldJ because comeUpForAir is checked in a tight loop.
// When it is a scala `var` the JIT doesn't seem to optimize it
// into a field access, so we do that work here.
// I dream of a World which doesn't have a flag totally unrelated
// to its purpose simply to serve the designs of an engine that
// it shouldn't know or care about. RG 6/16/17
class WorldJ {
  // This is a flag that the engine checks in its tightest innermost loops
  // to see if maybe it should stop running NetLogo code for a moment
  // and do something like halt or update the display.  It doesn't
  // particularly make sense to keep it in World, but since the check
  // occurs in inner loops, we want to put in a place where the engine
  // can get to it very quickly.  And since every Instruction has a
  // World object in it, the engine can always get to World quickly.
  //  - ST 1/10/07
  public volatile boolean comeUpForAir = false;  // NOPMD pmd doesn't like 'volatile'

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
