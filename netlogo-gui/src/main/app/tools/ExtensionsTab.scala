// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.nio.file.Path
import java.util.Locale

import org.nlogo.api.LibraryManager
import org.nlogo.core.{ Token, TokenType }

object ExtensionsTab {
  // static for testing (Isaac B 10/28/26)
  def addExtsToSource(source: String, requiredExts: Set[String],
                      tokenizeSource: String => Iterator[Token]): Option[String] = {
    val (prefix, rest) = tokenizeSource(source).span(token => token.tpe != TokenType.Keyword ||
                                                              token.text.toUpperCase(Locale.US) != "EXTENSIONS")

    if (rest.nextOption.isDefined) {
      val (beforeList, list) = rest.span(token => token.tpe == TokenType.Comment ||
                                                  token.tpe == TokenType.Whitespace)

      if (list.nextOption.exists(_.tpe == TokenType.OpenBracket)) {
        val (contents, suffix) = list.span(token => token.tpe == TokenType.Ident ||
                                                    token.tpe == TokenType.Comment ||
                                                    token.tpe == TokenType.Whitespace)

        if (suffix.nextOption.exists(_.tpe == TokenType.CloseBracket)) {
          val existing = contents.toSeq
          val extNames = requiredExts -- existing.filter(_.tpe == TokenType.Ident)
                                                 .map(_.text.toLowerCase(Locale.US)).toSet

          val newExts = {
            if (extNames.isEmpty) {
              existing.map(_.text).mkString
            } else {
              extNames.toSeq.sorted.mkString(" ", " ", " ") + existing.map(_.text).mkString.stripLeading
            }
          }

          Some(prefix.map(_.text).mkString + "extensions" + beforeList.map(_.text).mkString + "[" + newExts + "]" +
               suffix.map(_.text).mkString)
        } else {
          None
        }
      } else {
        None
      }
    } else {
      Some(s"extensions [ ${requiredExts.toSeq.sorted.mkString(" ")} ]\n$source")
    }
  }
}

class ExtensionsTab(manager: LibraryManager, updateStatus: String => Unit, recompile: () => Unit,
                    tokenizeSource: String => Iterator[Token], updateSource: (String => String) => Unit,
                    extPathMappings: Map[String, Path])
  extends LibrariesTab(manager, manager.getExtensionInfos, updateStatus, recompile, updateSource, extPathMappings) {

  override def statusName: String =
    "extensions"

  // this may look overly complex, but when rewriting the user's source code, we need to be absolutely sure
  // that we're doing the right thing, otherwise we might break things. the following code tokenizes the source,
  // preserving all whitespace and comments. it then tries to find an existing valid extensions directive. if it
  // can't find an extensions directive regardless of its validity, it adds a new extensions directive to the top
  // of the source. if it finds an extensions directive that is in valid, it rejects the operation. if it finds a
  // valid extensions directive, it adds any new extensions from the specified list to that directive.
  // (Isaac B 7/25/25)
  override def addLibrariesToSource(source: String, requiredLibs: Set[String]): Option[String] =
    ExtensionsTab.addExtsToSource(source, requiredLibs, tokenizeSource)
}
