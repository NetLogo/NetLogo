// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.ide

import org.fife.ui.rsyntaxtextarea.{ AbstractTokenMakerFactory, TokenMaker }
import org.fife.ui.rsyntaxtextarea.modes.PlainTextTokenMaker

import org.nlogo.nvm.ExtensionManager

class NetLogoTokenMakerFactory extends AbstractTokenMakerFactory {

  var extensionManager: Option[ExtensionManager] = None

  override def initTokenMakerMap(): Unit = {}

  override def getTokenMakerImpl(key: String): TokenMaker = {
    key match {
      case "netlogo"   => new NetLogoTwoDTokenMaker(extensionManager)
      case "netlogo3d" => new NetLogoThreeDTokenMaker(extensionManager)
      case _           => new PlainTextTokenMaker()
    }
  }
}
