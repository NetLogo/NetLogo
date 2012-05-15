// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui;

import org.jhotdraw.framework.Figure;
import org.jhotdraw.standard.ChopBoxConnector;

public strictfp class ChopRateConnector
    extends ChopBoxConnector {
  public ChopRateConnector() {
    // only used for Storable implementation
  }

  ChopRateConnector(Figure owner) {
    super(owner);
  }


  /**
   * Return an appropriate connection point
   */
  @Override
  protected java.awt.Point chop(Figure target, java.awt.Point from) {
    return ((RateConnection) owner()).pointAt(1);
  }

}
