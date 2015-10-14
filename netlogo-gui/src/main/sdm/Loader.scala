// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm

// in the GUI we load and save the diagram using JHotDraw's built-in facilities for that.  headless,
// we can't use the JHotDraw stuff because it depends on AWT and Swing, so we have to roll our own
// loading stuff. we also use this in the applet since we don't want to have to include JHotDraw in
// the lite jar - ST 1/24/05, 11/11/09, 11/23/11

import org.nlogo.api.CompilerServices

object Loader {

  def load(input: String, compiler: CompilerServices): String = {
    val lines =
      io.Source.fromString(mungeClassNames(input))
        .getLines
        .map(_.trim)
        .filter(_.nonEmpty)
        .toSeq
    // get dt first, since StreamTokenizer doesn't know scientific notation
    lines.headOption match {
      case None => ""
      case Some(dt) =>
        val model = buildModel(new Tokenizer(lines.tail.mkString("", "\n", "\n")),
                               dt.toDouble)
        new Translator(model, compiler).source
    }
  }

  // here comes kludginess!  we don't want the classes in org.nlogo.sdm to depend on JHotDraw, but
  // if they don't depend on JHotDraw, then JHotDraw's StorableInput stuff can't read them.  But in
  // our old save format, the org.nlogo.sdm class names are hardcoded, so to speak.  In order to
  // resolve this dilemma, we're going to preprocess the saved data and replace the names of the
  // org.nlogo.sdm classes with wrapper classes that implement Storable.  The wrapper objects will
  // have the real objects, the org.nlogo.sdm objects, stored inside them, and the read methods on
  // the GUI classes can do the unwrapping.  When saving, now, we output the wrapper class names
  // instead of the original class names. - ST 1/27/05
  def mungeClassNames(input: String) =
    input.replaceAll(" *org.nlogo.sdm.Stock ",
                     "org.nlogo.sdm.gui.WrappedStock ")
         .replaceAll(" *org.nlogo.sdm.Rate ",
                     "org.nlogo.sdm.gui.WrappedRate ")
         .replaceAll(" *org.nlogo.sdm.Reservoir ",
                     "org.nlogo.sdm.gui.WrappedReservoir")
         .replaceAll(" *org.nlogo.sdm.Converter ",
                     "org.nlogo.sdm.gui.WrappedConverter")
         // also translate pre-4.1 save format
         .replaceAll("org.nlogo.aggregate.gui",
                     "org.nlogo.sdm.gui")

  private type LineMap = collection.Map[Int, ModelElement]

  private def buildModel(tokens: Tokenizer, dt: Double): Model = {
    // lineMap keeps track of the objects we create with keys corresponding to the line numbers
    // file so we can find them later to connect rates. - EV
    // not every line results in an object, so we use a map - ST 1/25/05
    val lineMap = collection.mutable.Map[Int, ModelElement]()
    var model: Model = null
    while(tokens.hasNext) {
      tokens.next() match {
        case WordToken(name) =>
          for(me <- readElement(name, tokens)) {
            me match {
              case m: Model =>
                require(model == null, tokens.diagnostics)
                model = m
                model.setDt(dt.toDouble)
              case s: Stock =>
                lineMap(tokens.lineNumber) = s
              case r: Rate =>
                setSourceSink(r, tokens, lineMap)
              case _ =>
            }
            model.addElement(me)
          }
        case _ => throw new Exception("Invalid SDM Model")
      }
      // skip GUI-only stuff, until eol - ST 1/25/05
      while(tokens.hasNext && tokens.next() != EOLToken)
      { }
    }
    model
  }

  private def readElement(name: String, tokens: Tokenizer): Option[ModelElement] =
    name match {
      case "org.nlogo.sdm.gui.WrappedStock" =>
        val stock = new Stock
        stock.setName(tokens.string())
        stock.setInitialValueExpression(tokens.string())
        stock.setNonNegative(tokens.boolean())
        Some(stock)
      case "org.nlogo.sdm.gui.WrappedRate" =>
        val rate = new Rate
        rate.setExpression(tokens.string())
        rate.setName(tokens.string())
        Some(rate)
      case "org.nlogo.sdm.gui.WrappedConverter" =>
        val converter = new Converter
        converter.setExpression(tokens.string())
        converter.setName(tokens.string())
        Some(converter)
      case "org.nlogo.sdm.gui.AggregateDrawing" =>
        val model = new Model("Test Model", 1)
        Some(model)
      case "org.nlogo.sdm.gui.ReservoirFigure" |
           "org.nlogo.sdm.gui.StockFigure" |
           "org.nlogo.sdm.gui.RateConnection" |
           "org.jhotdraw.contrib.ChopDiamondConnector" |
           "org.jhotdraw.standard.ChopBoxConnector" |
           "org.nlogo.sdm.gui.WrappedReservoir" |
           "org.nlogo.sdm.gui.ConverterFigure" |
           "org.nlogo.sdm.gui.BindingConnection" |
           "org.jhotdraw.figures.ChopEllipseConnector" |
           "org.nlogo.sdm.gui.ChopRateConnector" =>
        None
    }

  private def setSourceSink(rate: Rate, tokens: Tokenizer, lineMap: LineMap) {
    tokens.next() match {
      case WordToken("REF") =>
        rate.setSource(getSourceOrSink(tokens, lineMap))
        rate.setSink(
          tokens.next() match {
            case WordToken("REF") =>
              getSourceOrSink(tokens, lineMap)
            case _ =>
              new Reservoir
          })
      case _ =>
        tokens.next()
        tokens.next()
        rate.setSink(getSourceOrSink(tokens, lineMap))
        rate.setSource(getSourceOrSink(tokens, lineMap))
    }
  }

  private def getSourceOrSink(tokens: Tokenizer, lineMap: LineMap): Stock =
    // the REF number points to the line containing the StockFigure. the Stock is on the following line,
    // hence the + 1 - EV 1/31/05, ST 11/23/11
    lineMap.get(tokens.number() + 1)
      .map(_.asInstanceOf[Stock])
      .getOrElse(new Reservoir)

}

// java.io.StreamTokenizer is weird, so we wrap it.  parser combinators or some such might be nicer
// but I don't want to bloat the applet jar. - ST 11/23/11

sealed trait Token
case class WordToken(s: String) extends Token
case class StringToken(s: String) extends Token
case class NumberToken(n: Int) extends Token
case object EOLToken extends Token

class Tokenizer(input: String) {
  import java.io.StreamTokenizer
  import StreamTokenizer.{ TT_EOF, TT_EOL, TT_WORD, TT_NUMBER }
  val st = new StreamTokenizer(new java.io.StringReader(input))
  st.eolIsSignificant(true)
  def diagnostics = st.toString
  def lineNumber = st.lineno
  def hasNext = {
    st.nextToken()
    val tokenType = st.ttype
    st.pushBack()
    st.ttype != TT_EOF
  }
  def next() = st.nextToken() match {
    case '"' => StringToken(st.sval)
    case TT_WORD => WordToken(st.sval)
    case TT_NUMBER => NumberToken(st.nval.toInt)
    case TT_EOL => EOLToken
    case TT_EOF => sys.error("unexpected end of file: " + diagnostics)
  }
  def word() = next() match {
    case WordToken(s) => s
    case _ => sys.error("expected word: " + diagnostics)
  }
  def string() = next() match {
    case StringToken(s) => s
    case _ => sys.error("expected string: " + diagnostics)
  }
  def number() = next() match {
    case NumberToken(n) => n
    case _ => sys.error("expected number: " + diagnostics)
  }
  def boolean() = next() match {
    case NumberToken(0) => false
    case NumberToken(1) => true
    case _ => sys.error("expected boolean (0 or 1): " + diagnostics)
  }
}
