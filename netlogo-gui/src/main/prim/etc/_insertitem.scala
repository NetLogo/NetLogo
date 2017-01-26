/**
  * Created by Eric on 1/25/17.
  */

package org.nlogo.prim.etc

import org.nlogo.api.Dump
import org.nlogo.core.{I18N, LogoList, Syntax}
import org.nlogo.nvm._

class _insertitem extends Reporter with Pure {
  override def report(context: Context): AnyRef = {
    val index : Int = argEvalIntValue(context, 0)
    val obj = args(1).report(context)
    val elt = args(2).report(context)

    if (index < 0)
      throw new RuntimePrimitiveException(context, this, I18N.errors.getN("org.nlogo.prim.etc.$common.negativeIndex", Int.box(index)))

    obj match {
      case l: LogoList =>
        if (index > l.size)
          throw new RuntimePrimitiveException(context, this, I18N.errors.getN("org.nlogo.prim.etc.$common.indexExceedsListSize", Int.box(index), Dump.logoObject(l), Int.box(l.size)))

        l.insertItem(index, obj, elt)

      case s: String =>
        if (! elt.isInstanceOf[String])
          throw new ArgumentTypeException(context, this, 2, Syntax.StringType, elt)
        else if (index > s.length)
          throw new RuntimePrimitiveException(context, this, I18N.errors.getN("org.nlogo.prim.etc.$common.indexExceedsStringSize", Int.box(index), Dump.logoObject(s), Int.box(s.length)))

        s.slice(0, index) + elt + s.slice(index, s.length)

      case _ =>
        throw new ArgumentTypeException(context, this, 1, Syntax.ListType | Syntax.StringType, obj)
    }
  }

}
