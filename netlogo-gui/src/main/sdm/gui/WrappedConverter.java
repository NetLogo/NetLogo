// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui;

import org.nlogo.sdm.Converter;

public strictfp class WrappedConverter
    implements org.jhotdraw.util.Storable {
  final Converter converter;

  public WrappedConverter() {
    converter = new Converter();
  }

  public WrappedConverter(Converter converter) {
    this.converter = converter;
  }

  public void write(org.jhotdraw.util.StorableOutput dw) {
    dw.writeString(converter.getExpression());
    dw.writeString(converter.getName());
  }

  public void read(org.jhotdraw.util.StorableInput dr)
      throws java.io.IOException {
    converter.setExpression(dr.readString());
    converter.setName(dr.readString());
  }

}
