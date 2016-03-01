// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core
package prim.etc

trait LinkCreationCommand extends Command {
  def breedName: String
  def inputType: Int
  override def syntax =
    Syntax.commandSyntax(
      right = List(inputType, Syntax.CommandBlockType | Syntax.OptionalType),
      agentClassString = "-T--",
      blockAgentClassString = Option("---L"))
}

trait Single extends LinkCreationCommand {
  override def inputType = Syntax.TurtleType
}

trait Multiple extends LinkCreationCommand {
  override def inputType = Syntax.TurtlesetType
}

trait Directed extends LinkCreationCommand
trait DirectedTo extends Directed
trait DirectedFrom extends Directed
trait Undirected extends LinkCreationCommand

case class _createlinkwith (breedName: String) extends Single   with Undirected   {
  def this() = this("") }
case class _createlinkto   (breedName: String) extends Single   with DirectedTo   {
  def this() = this("") }
case class _createlinkfrom (breedName: String) extends Single   with DirectedFrom {
  def this() = this("") }
case class _createlinkswith(breedName: String) extends Multiple with Undirected   {
  def this() = this("") }
case class _createlinksto  (breedName: String) extends Multiple with DirectedTo   {
  def this() = this("") }
case class _createlinksfrom(breedName: String) extends Multiple with DirectedFrom {
  def this() = this("") }
