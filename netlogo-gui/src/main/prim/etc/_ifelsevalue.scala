// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.LogoException
import org.nlogo.core.Pure
import org.nlogo.nvm.{Context, Reporter, RuntimePrimitiveException}

final class _ifelsevalue extends Reporter with Pure {
  @throws[LogoException]
  override def report(context: Context): AnyRef = {
    var i = 0
    while (i < args.length - 1) {
      if (argEvalBooleanValue(context, i)) {
        return args(i + 1).report(context)
      }
      i += 2
    }
    if (i < args.length)
      args(args.length - 1).report(context)
    else
      throw new RuntimePrimitiveException(context, this, "IFELSE-VALUE found no true conditions and no else branch. If you don't wish to error when no conditions are true, add a final else branch.")
  }
}
