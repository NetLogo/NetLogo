// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui;

import org.jhotdraw.util.Storable;
import org.jhotdraw.util.StorableInput;
import org.jhotdraw.util.StorableOutput;
import org.nlogo.sdm.Rate;

public strictfp class WrappedRate
    implements Storable {
  final Rate rate;

  public WrappedRate() {
    rate = new Rate();
  }

  public WrappedRate(Rate rate) {
    this.rate = rate;
  }

  public void write(StorableOutput dw) {
    dw.writeString(rate.getExpression());
    dw.writeString(rate.getName());
    dw.writeStorable(Wrapper.wrap(rate.getSource()));
    dw.writeStorable(Wrapper.wrap(rate.getSink()));
    dw.writeBoolean(rate.isBivalent());
  }

  public void read(StorableInput dr)
      throws java.io.IOException {
    rate.setExpression(dr.readString());
    rate.setName(dr.readString());
    Storable s = dr.readStorable();
    if (s instanceof WrappedStock) {
      rate.setSource(((WrappedStock) s).stock);
    } else {
      rate.setSource(((WrappedReservoir) s).reservoir);
    }
    s = dr.readStorable();
    if (s instanceof WrappedStock) {
      rate.setSink(((WrappedStock) s).stock);
    } else {
      rate.setSink(((WrappedReservoir) s).reservoir);
    }
    rate.setBivalent(dr.readBoolean());
  }

}
