package org.nlogo.extensions.nw.prim

import org.nlogo.api
import org.nlogo.core.Syntax._
import org.nlogo.extensions.nw.NetworkExtension

class Version(extension: NetworkExtension)
  extends api.Reporter {
  override def getSyntax = reporterSyntax(ret = StringType)
  override def report(args: Array[api.Argument], context: api.Context): AnyRef = extension.version
}
