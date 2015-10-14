// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.{ Syntax, LogoList, LogoListBuilder }
import org.nlogo.nvm.{ Context, Reporter, Pure }

class _sentence extends Reporter with Pure {

  override def syntax =
    Syntax.reporterSyntax(
      Array(Syntax.RepeatableType | Syntax.WildcardType),
      Syntax.ListType, dfault = 2, minimum = 0)

  // for fear of https://issues.scala-lang.org/browse/SI-7725
  // we must never do (Vector ++ Vector) - ST 8/7/13
  override def report(context: Context): LogoList =
    args.size match {
      case 0 =>
        LogoList.Empty
      case 1 =>
        args(0).report(context) match {
          case list: LogoList =>
            list
          case x =>
            LogoList(x)
        }
      case 2 =>
        (args(0).report(context), args(1).report(context)) match {
          case (list1: LogoList, list2: LogoList) =>
            if (list2.size > list1.size)
              list1.foldRight(list2)((item, result) => result.fput(item))
            else
              list2.foldLeft(list1)(_.lput(_))
          case (list: LogoList, item: AnyRef) =>
            list.lput(item)
          case (item: AnyRef, list: LogoList) =>
            list.fput(item)
          case (item1: AnyRef, item2: AnyRef) =>
            LogoList(item1, item2)
        }
      case _ =>
        val builder = new LogoListBuilder
        var i = 0
        while(i < args.length) {
          args(i).report(context) match {
            case list: LogoList =>
              builder.addAll(list)
            case x =>
              builder.add(x)
          }
          i += 1
        }
        builder.toLogoList
    }

}
