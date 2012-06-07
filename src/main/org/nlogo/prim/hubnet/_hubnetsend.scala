// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.hubnet

import org.nlogo.api.{ Dump, LogoList, Syntax, TypeNames }
import org.nlogo.nvm.{ ArgumentTypeException, Command, Context, EngineException }

class _hubnetsend extends Command {
  override def syntax =
    Syntax.commandSyntax(
      Array(Syntax.StringType | Syntax.ListType,
            Syntax.StringType,
            Syntax.WildcardType))
  override def perform(context: Context) {
    val nodesArg = args(0).report(context)
    val tag = argEvalString(context, 1)
    val message = args(2).report(context)
    val hubnetManager = workspace.getHubNetManager
    val nodes = new collection.mutable.ArrayBuffer[String]
    nodesArg match {
      case list: LogoList =>
        for(node <- list.scalaIterator)
          node match {
            case s: String =>
              nodes += s
            case _ =>
              throw new EngineException(
                context, this,
                "HUBNET-SEND expected " + TypeNames.aName(Syntax.StringType | Syntax.ListType)
                + " of strings as the first input, but one item is the "
                + TypeNames.name(node) + " " + Dump.logoObject(node) + " instead")
          }
      case s: String =>
        nodes += s
      case _ =>
        throw new ArgumentTypeException(
          context, this, 0, Syntax.ListType | Syntax.StringType, nodesArg)
    }
    hubnetManager.send(nodes, tag, message)
    context.ip = next
  }
}
