// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.Dump
import org.nlogo.core.{ LocaleContext, LogoList, Pure, Syntax }
import org.nlogo.nvm.{ ArgumentTypeException, Context, Reporter, RuntimePrimitiveException }

class _insertitem extends Reporter with Pure {
  override def report(context: Context): AnyRef = {
    val index : Int = argEvalIntValue(context, 0)
    val obj = args(1).report(context)
    val elt = args(2).report(context)

    if (index < 0)
      throw new RuntimePrimitiveException(context, this, loc"$index isn't greater than or equal to zero.")

    obj match {
      case l: LogoList =>
        if (index > l.size)
          throw new RuntimePrimitiveException(context, this,
            loc"Can't find element $index of the list ${Dump.logoObject(l)}, which is only of length ${l.size}.")

        l.insertItem(index, obj, elt)

      case s: String =>
        if (! elt.isInstanceOf[String])
          throw new ArgumentTypeException(context, this, 2, Syntax.StringType, elt)
        else if (index > s.length)
          throw new RuntimePrimitiveException(context, this,
            loc"Can't find element $index of the string ${Dump.logoObject(s)}, which is only of length ${s.length}.")

        s.slice(0, index) + elt + s.slice(index, s.length)

      case _ =>
        throw new ArgumentTypeException(context, this, 1, Syntax.ListType | Syntax.StringType, obj)
    }
  }

}
