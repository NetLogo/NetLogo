// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui;

import org.nlogo.sdm.Stock;

public strictfp class WrappedStock
    implements org.jhotdraw.util.Storable {
  final Stock stock;

  public WrappedStock() {
    stock = new Stock();
  }

  public WrappedStock(Stock stock) {
    this.stock = stock;
  }

  public void write(org.jhotdraw.util.StorableOutput dw) {
    dw.writeString(stock.getName());
    dw.writeString(stock.getInitialValueExpression());
    dw.writeBoolean(stock.isNonNegative());
  }

  public void read(org.jhotdraw.util.StorableInput dr)
      throws java.io.IOException {
    stock.setName(dr.readString());
    stock.setInitialValueExpression(dr.readString());
    stock.setNonNegative(dr.readBoolean());
  }

}
