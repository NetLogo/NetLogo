// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.hubnet

import java.io.{ Serializable => JSerializable }
import org.nlogo.api.{ Dump, TypeNames }
import org.nlogo.core.Syntax
import org.nlogo.core.LogoList
import org.nlogo.nvm.{ ArgumentTypeException, Command, Context}
import org.nlogo.nvm.RuntimePrimitiveException

class _hubnetsend extends Command with HubNetPrim {

  override def perform(context: Context) {
    val nodesArg = args(0).report(context)
    val tag = argEvalString(context, 1)
    val message = args(2).report(context)
    val hubnetManager = hubNetManager.get
    val nodes = new collection.mutable.ArrayBuffer[String]
    nodesArg match {
      case list: LogoList =>
        for(node <- list.scalaIterator)
          node match {
            case s: String =>
              nodes += s
            case _ =>
              throw new RuntimePrimitiveException(
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
    message match {
      case m: JSerializable => hubnetManager.send(nodes, tag, m)
      case _                =>
        throw new RuntimePrimitiveException(
          context, this,
          s"""|HUBNET-SEND is unable to send the message $message
              |of type ${TypeNames.name(message)} because it could not be
              |transmitted over the network""".stripMargin.lines.mkString(" "))
    }
    context.ip = next
  }
}
