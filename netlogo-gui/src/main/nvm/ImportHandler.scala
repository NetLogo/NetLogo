// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.{ api, agent, core },
  core.{ LiteralImportHandler, Token },
  api.{ ExtensionManager => ApiExtensionManager , World },
  agent.AgentParserCreator

class ImportHandler(world: World, extensionManager: ApiExtensionManager) extends LiteralImportHandler {
  override def parseExtensionLiteral(token: Token): AnyRef = {
    val LiteralRegex = """\{\{(\S*):(\S*)\s(.*)\}\}""".r
    token.value.asInstanceOf[String] match {
      case LiteralRegex(extName, typeName, data) =>
        extensionManager.readExtensionObject(extName, typeName, data)
      case s => s
    }
  }

  override def parseLiteralAgentOrAgentSet(tokens: Iterator[Token], parser: LiteralImportHandler.Parser): AnyRef = {
    AgentParserCreator(world)(parser)(tokens)
  }
}
