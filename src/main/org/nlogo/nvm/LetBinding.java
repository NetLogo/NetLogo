// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm;

import org.nlogo.api.Let;

strictfp public class LetBinding {
  public final Let let;
  public Object value;

  LetBinding(Let let, Object value) {
    this.let = let;
    this.value = value;
  }
}
