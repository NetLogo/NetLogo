package org.nlogo.hubnet.protocol

import org.nlogo.api.WidgetIO.{PlotSpec, ChooserSpec, ViewSpec, WidgetSpec}
import org.nlogo.api.{WidgetIO, CompilerServices, LogoList, Shape}

@SerialVersionUID(0)
case class ClientInterface(widgets: Iterable[WidgetSpec],
                           turtleShapes: Iterable[Shape],
                           linkShapes: Iterable[Shape]) {
  def containsWidget(tag: String) = {
    def widgetNames(widgetSpecs: Iterable[WidgetSpec]): Iterable[String] = {
      import WidgetIO._
      (for (w <- widgetSpecs.toList) yield w match {
        case b: ButtonSpec => b.displayName.getOrElse(b.source)
        case m: MonitorSpec =>
          m.displayName.getOrElse(m.source.getOrElse(throw new IllegalStateException("bad monitor")))
        case s: SliderSpec => s.name
        case s: SwitchSpec => s.name
        case c: ChooserSpec => c.name
        case i: InputBoxSpec => i.name
        case o: OutputSpec => "OUTPUT"
        case n: NoteSpec => "NOTE"
        case p: PlotSpec => p.name
        case v: ViewSpec => "VIEW"
      })
    }
    (tag == "ALL PLOTS" &&  widgets.exists(_.isInstanceOf[PlotSpec])) || widgetNames(widgets).exists(_==tag)
  }
  def containsViewWidget = widgets.exists(_.isInstanceOf[ViewSpec])

  def chooserChoices(compiler: CompilerServices): Map[String, LogoList] = {
    widgets.collect{ case c: ChooserSpec => c }.map{ c =>
      (c.name, LogoList.fromIterator(parseChoicesFromString(c.choices).iterator))
    }.toMap
  }

  // This is a HACK. We will want to find a better solution than this.
  //
  // This is meant to parse a string representation of a chooser's choices, which is something
  // like "42 \"zoo\" [1 2 3]", and return a list containing the items {"42", "zoo", "[1 2 3]"}.
  //
  // A better way to do this would be to call compiler.readFromString("[" + chooser.choices + "]").
  // This would work in the HubNet client, but not in the ClientApplet because the applet does
  // not have access to the compiler (in fact, if you tried calling compiler.readFromString() when
  // running in an applet, the call would get routed to ClientApplet.DummyCompilerServices.readFromString(),
  // which doesn't actually do anything).
  //
  // Since we don't want the ClientApplet to depend on the compiler, but we still want to be able
  // to parse choosers, we need to write our own parsing routine. A better solution would be to
  // fix how widgets are represented in the server's handshake message (and in nlogo files, for that
  // matter) so that they're easier to deal with, but this will have to do for now.
  def parseChoicesFromString(choicesString: String): List[AnyRef] = {
    def continueParsing(remainingChoicesString: String, parsedTokens: List[AnyRef]): List[AnyRef] = {
      val remaining = remainingChoicesString.trim.replaceAll("\n", "")
      if (remaining.length== 0)
        parsedTokens.reverse
      else {
        val (token: AnyRef, indexWhereToBeginLookingForNextToken) = remaining.head match {
          case '\"' =>
            // The quotes themselves ARE part of the token
            val closingDoubleQuote = remaining.indexOf("\"", 1)
            (remaining.substring(0, closingDoubleQuote + 1), closingDoubleQuote + 1)
          case '[' =>
            // This completely ignores nested lists!
            // The braces ARE part of the token
            val closingBrace = remaining.indexOf("]", 1)
            (remaining.substring(0, closingBrace + 1), closingBrace + 1)
          case somethingElse =>
            // This is probably a number. Look for the start of the next token in order to
            // find out where this one ends.
            val tokenEnd = remaining.indexWhere(c => (c == ' ' || c == '\n' || c == '\"' || c == '['), 1) match {
              case -1 => remaining.length - 1
              case index => index
            }
            (remaining.substring(0, tokenEnd), tokenEnd + 1)
        }
        continueParsing(remaining.substring(indexWhereToBeginLookingForNextToken), token :: parsedTokens)
      }
    }
    continueParsing(choicesString, Nil)
  }
}


