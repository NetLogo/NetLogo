package org.nlogo.extensions.vid

import org.nlogo.api.{ Argument, Command, PrimitiveManager, Reporter }
import org.nlogo.core.Primitive

import util.FakeArgument

import scala.language.dynamics

class CommandPrimitiveLoader extends PrimitiveManager with Dynamic {
  var commands  = Map[String, Command]()
  var reporters = Map[String, Reporter]()

  def addPrimitive(name: String, prim: Primitive) = {
    prim match {
      case c: Command  => commands  += (name -> c)
      case r: Reporter => reporters += (name -> r)
    }
  }

  def applyDynamic(name: String)(args: AnyRef*): AnyRef = {
    val arguments = args.map(v => new FakeArgument(v).asInstanceOf[Argument]).toArray
    val context = new FakeContext()
    commands.get(name).map(cmd => { cmd.perform(arguments, context); null }).orElse(
      reporters.get(name).map(rep => rep.report(arguments, context))).getOrElse(
        throw new Exception(s"could not find command or reporter named $name"))
  }

  def autoImportPrimitives: Boolean = false
  def autoImportPrimitives_=(value: Boolean) = {}
}

trait WithLoadedVidExtension {
  def movieFactory: MovieFactory

  def cameraFactory: CameraFactory

  def selector: Selector

  def player: Player

  def recorder: Recorder

  lazy val (vidExtension, vid) = {
    val ve     = new VidExtension(movieFactory, cameraFactory, player, selector, recorder)
    ve.runOnce(null)
    val loader = new CommandPrimitiveLoader()
    ve.load(loader)
    (ve, loader)
  }
}
