// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core
package prim.etc

case class _breedat(breedName: String) extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType, Syntax.NumberType),
      ret = Syntax.TurtlesetType,
      agentClassString = "-TP-")
}
case class _breedhere(breedName: String) extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.TurtlesetType,
      agentClassString = "-TP-")
}
case class _breedon(breedName: String) extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.TurtleType | Syntax.PatchType | Syntax.TurtlesetType | Syntax.PatchsetType),
      ret = Syntax.TurtlesetType)
}
case class _breedsingular(breedName: String) extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType),
      ret = Syntax.TurtleType | Syntax.NobodyType)
}
case class _inlinkfrom(breedName: String) extends Reporter {
  def this() = this(null)
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.AgentType),
      ret = Syntax.AgentType,
      agentClassString = "-T--")
}
case class _inlinkneighbor(breedName: String) extends Reporter {
  def this() = this(null)
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.AgentType),
      ret = Syntax.BooleanType,
      agentClassString = "-T--")
}
case class _inlinkneighbors(breedName: String) extends Reporter {
  def this() = this(null)
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.AgentsetType,
      agentClassString = "-T--")
}
case class _isbreed(breedName: String) extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.WildcardType),
      ret = Syntax.BooleanType)
}
case class _linkbreed(breedName: String) extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.LinksetType)
}
case class _linkbreedsingular(breedName: String) extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType, Syntax.NumberType),
      ret = Syntax.LinkType | Syntax.NobodyType)
}
case class _linkneighbor(breedName: String) extends Reporter {
  def this() = this(null)
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.AgentType),
      ret = Syntax.BooleanType,
      agentClassString = "-T--")
}
case class _linkneighbors(breedName: String) extends Reporter {
  def this() = this(null)
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.AgentsetType,
      agentClassString = "-T--")
}
case class _linkwith(breedName: String) extends Reporter {
  def this() = this(null)
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.AgentType),
      ret = Syntax.LinkType,
      agentClassString = "-T--")
}
case class _myinlinks(breedName: String) extends Reporter {
  def this() = this(null)
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.LinksetType,
      agentClassString = "-T--")
}
case class _mylinks(breedName: String) extends Reporter {
  def this() = this(null)
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.LinksetType,
      agentClassString = "-T--")
}
case class _myoutlinks(breedName: String) extends Reporter {
  def this() = this(null)
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.LinksetType,
      agentClassString = "-T--")
}
case class _outlinkneighbor(breedName: String) extends Reporter {
  def this() = this(null)
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.AgentType),
      ret = Syntax.BooleanType,
      agentClassString = "-T--")
}
case class _outlinkneighbors(breedName: String) extends Reporter {
  def this() = this(null)
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.AgentsetType,
      agentClassString = "-T--")
}
case class _outlinkto(breedName: String) extends Reporter {
  def this() = this(null)
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.AgentType),
      ret = Syntax.AgentType,
      agentClassString = "-T--")
}
