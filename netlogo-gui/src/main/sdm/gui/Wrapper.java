// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui;

import org.jhotdraw.util.Storable;
import org.nlogo.sdm.Converter;
import org.nlogo.sdm.ModelElement;
import org.nlogo.sdm.Rate;
import org.nlogo.sdm.Reservoir;
import org.nlogo.sdm.Stock;

import java.util.HashMap;
import java.util.Map;

strictfp final class Wrapper {
  // private constructor to enforce noninstantiability
  private Wrapper() {
  }

  static Map<ModelElement, Storable> map =
      new HashMap<ModelElement, Storable>();

  static Storable wrap(ModelElement o) {
    Storable result = map.get(o);
    if (result != null) {
      return result;
    }
    if (o instanceof Reservoir) {
      result = new WrappedReservoir((Reservoir) o);
    } else if (o instanceof Stock) {
      result = new WrappedStock((Stock) o);
    } else if (o instanceof Converter) {
      result = new WrappedConverter((Converter) o);
    } else if (o instanceof Rate) {
      result = new WrappedRate((Rate) o);
    } else {
      throw new IllegalArgumentException();
    }
    map.put(o, result);
    return result;
  }

  static void reset() {
    map = new HashMap<ModelElement, Storable>();
  }

}
