// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui;

import org.nlogo.sdm.Reservoir;

public strictfp class WrappedReservoir
    implements org.jhotdraw.util.Storable {
  final Reservoir reservoir;

  public WrappedReservoir() {
    reservoir = new Reservoir();
  }

  public WrappedReservoir(Reservoir reservoir) {
    this.reservoir = reservoir;
  }

  // Reservoirs are just decoration, so we don't write them to file
  public void write(org.jhotdraw.util.StorableOutput dw) {
  }

  public void read(org.jhotdraw.util.StorableInput dr) {
  }

}
