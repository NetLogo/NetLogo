package org.nlogo.sdm

import java.io.StreamTokenizer
import org.nlogo.api.CompilerServices

class AggregateManagerLite extends org.nlogo.api.AggregateManagerInterface {

  private val validTokens = Map(
    "MODEL"            -> "org.nlogo.sdm.gui.AggregateDrawing",
    "STOCK"            -> "org.nlogo.sdm.gui.WrappedStock",
    "RATE"             -> "org.nlogo.sdm.gui.WrappedRate",
    "CONVERTER"        -> "org.nlogo.sdm.gui.WrappedConverter",
    "RESERVOIR_FIGURE" -> "org.nlogo.sdm.gui.ReservoirFigure",
    "STOCK_FIGURE"     -> "org.nlogo.sdm.gui.StockFigure",
    "RATE_CONN"        -> "org.nlogo.sdm.gui.RateConnection",
    "CHOP_DIAMOND"     -> "org.jhotdraw.contrib.ChopDiamondConnector",
    "CHOP_BOX"         -> "org.jhotdraw.standard.ChopBoxConnector",  
    "RESERVOIR"        -> "org.nlogo.sdm.gui.WrappedReservoir", 
    "CONVERTER_FIGURE" -> "org.nlogo.sdm.gui.ConverterFigure",
    "BINDING_CONN"     -> "org.nlogo.sdm.gui.BindingConnection",
    "CHOP_ELLIPSE"     -> "org.jhotdraw.figures.ChopEllipseConnector",  
    "CHOP_RATE"        -> "org.nlogo.sdm.gui.ChopRateConnector"
 )

  private def unsupported = throw new UnsupportedOperationException

  /// implementations of SourceOwner methods
  var source = ""
  def innerSource = source
  def innerSource(s: String) = unsupported
  def classDisplayName = "System Dynamics"
  def agentClass = unsupported
  def headerSource = ""

  /// these AggregateManagerInterface methods aren't relevant when running headless
  def showEditor() = unsupported
  def save() = unsupported

  /// code for loading

  // in the GUI we load and save the diagram using JHotdraw's built-in facilities for that.
  // headless, we can't use the JHotdraw stuff because it depends on AWT and Swing, so we have to
  // roll our own loading stuff - ST 1/24/05, 11/11/09

  def load(lines: String, compiler: CompilerServices) {
    if(lines.trim.nonEmpty) {
      val lines2 = org.nlogo.sdm.Model.mungeClassNames(lines)
      // parse out dt first, since StreamTokenizer doesn't handle scientific notation
      val br = new java.io.BufferedReader(new java.io.StringReader(lines2))
      val dt = br.readLine().toDouble
      val str = br.readLine()
      val lines3 = lines2.substring(lines.indexOf(str))
      val tokenizer = new StreamTokenizer(new java.io.StringReader(lines3))
      tokenizer.eolIsSignificant(true)
      source = new Translator(buildModel(tokenizer, dt), compiler).source
    }
  }
  
  private def buildModel(tokenizer: StreamTokenizer, dt: Double): Model = {
    // lineMap keeps track of the objects we create with keys corresponding to the valid line in the
    // file so we can find them later to connect rates. - EV
    // not every line results in an object, so we use a map instead of an ArrayList (an ArrayList
    // with some nulls in it would have worked too) - ST 1/25/05
    val lineMap = collection.mutable.Map[Int,ModelElement]()
    var model: Model = null
    var validLines = 0
    while(tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
      // ignore blank lines
      if(tokenizer.ttype != StreamTokenizer.TT_EOL) {
        // translate pre-4.1 save format
        tokenizer.sval = tokenizer.sval.replaceAll(
          "org.nlogo.aggregate.gui", "org.nlogo.sdm.gui")
        if(!validTokens.valuesIterator.contains(tokenizer.sval))
          throw new java.io.IOException(
            "invalid token: \"" + tokenizer.sval + "\"")
        validLines += 1
        val me = processElement(tokenizer)
        if(me != null) {
          if(me.isInstanceOf[Model]) {
            if(model != null)
              throw new IllegalStateException
            model = me.asInstanceOf[Model]
            model.setDt(dt.toDouble)
          }
          else if(me.isInstanceOf[Stock]) {
            lineMap(validLines) = me
            // I don't understand why we have to do this - ev 7/22/09
            validLines += 1
          }
          else if(me.isInstanceOf[Rate])
            setSourceSink(me.asInstanceOf[Rate], tokenizer, lineMap)
          model.addElement(me)
        }
        // skip GUI-only stuff, until eol - ST 1/25/05
        while(tokenizer.ttype != StreamTokenizer.TT_EOL &&
              tokenizer.ttype != StreamTokenizer.TT_EOF)
          tokenizer.nextToken()
      }
    }
    model
  }

  private def processElement(st: StreamTokenizer): ModelElement = {
    if(st.ttype != StreamTokenizer.TT_WORD)
      null
    else if(st.sval == validTokens("STOCK")) {
      val stock = new Stock ()
      stock.setName(readString(st))
      stock.setInitialValueExpression(readString(st))
      stock.setNonNegative(readBoolean(st))
      stock
    }
    else if(st.sval == validTokens("RATE")) {
      val rate = new Rate()
      rate.setExpression(readString(st))
      rate.setName(readString(st))
      rate
    }
    else if(st.sval == validTokens("CONVERTER")) {
      val converter = new Converter()                     
      converter.setExpression(readString(st))
      converter.setName(readString(st))
      converter
    }
    else if(st.sval == validTokens("MODEL"))
      new Model("Test Model", 1)
    else null
  }

  private def setSourceSink(rate: Rate, st: StreamTokenizer, lineMap: collection.Map[Int,ModelElement]) {
    if(st.nextToken() == StreamTokenizer.TT_WORD && st.sval.equals("REF")) {
      rate.setSource(getSourceOrSink(st, lineMap))
      rate.setSink(
        if(st.nextToken() == StreamTokenizer.TT_WORD && st.sval.equals("REF"))
          getSourceOrSink(st, lineMap)
        else
          new Reservoir
       )
    }
    else {
      st.nextToken()
      st.nextToken()
      rate.setSink(getSourceOrSink(st, lineMap))
      rate.setSource(getSourceOrSink(st, lineMap))
    }
  }

  private def getSourceOrSink(st: StreamTokenizer, lineMap: collection.Map[Int,ModelElement]): Stock = {
    // the ref numbers actually point to the valid line that contains the the declaration of the
    // StockFigure object the Stock object will always be in the line directly following the
    // StockFigure.  Thus, when we put it in the hash table the key (corresponding to the valid
    // line number) is the ref num + 1 - ev 1/31/05
    val key = readInt(st) + 1
    if(lineMap.contains(key))
      lineMap(key).asInstanceOf[Stock]
    else
      new Reservoir
  }

  /// helpers
  private def readString(st: StreamTokenizer): String =
    if(st.nextToken() == '"')
      st.sval
    else
      throw new java.io.IOException("expected string token")
  private def readBoolean(st: StreamTokenizer): Boolean =
    if(st.nextToken() == StreamTokenizer.TT_NUMBER)
      st.nval == 1
    else 
      throw new java.io.IOException("expected integer (boolean)")
  private def readInt(st: StreamTokenizer) =
    if(st.nextToken() == StreamTokenizer.TT_NUMBER)
      st.nval.toInt
    else
      throw new java.io.IOException("expected integer")

}
