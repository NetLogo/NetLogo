// (C) 2011 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm

// in the GUI we load and save the diagram using JHotDraw's built-in facilities for that.
// headless, we can't use the JHotDraw stuff because it depends on AWT and Swing, so we have to
// roll our own loading stuff - ST 1/24/05, 11/11/09, 11/23/11

import org.nlogo.api.CompilerServices
import java.io.StreamTokenizer
import StreamTokenizer.{ TT_EOF, TT_EOL, TT_WORD, TT_NUMBER }

object Loader {
  
  def load(lines: String, compiler: CompilerServices): String = {
    if(lines.trim.isEmpty)
      ""
    else {
      val lines2 = mungeClassNames(lines)
      // parse out dt first, since StreamTokenizer doesn't handle scientific notation
      val br = new java.io.BufferedReader(new java.io.StringReader(lines2))
      val dt = br.readLine().toDouble
      val str = br.readLine()
      val lines3 = lines2.substring(lines.indexOf(str))
      val tokenizer = new StreamTokenizer(new java.io.StringReader(lines3))
      tokenizer.eolIsSignificant(true)
      new Translator(buildModel(tokenizer, dt), compiler).source
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
  def mungeClassNames(text: String) =
    text.replaceAll(" *org.nlogo.sdm.Stock ",
                    "org.nlogo.sdm.gui.WrappedStock ")
        .replaceAll(" *org.nlogo.sdm.Rate ",
                    "org.nlogo.sdm.gui.WrappedRate ")
        .replaceAll(" *org.nlogo.sdm.Reservoir ",
                    "org.nlogo.sdm.gui.WrappedReservoir")
        .replaceAll(" *org.nlogo.sdm.Converter ",
                    "org.nlogo.sdm.gui.WrappedConverter")

  private type LineMap = collection.Map[Int, ModelElement]

  private def buildModel(tokenizer: StreamTokenizer, dt: Double): Model = {
    // lineMap keeps track of the objects we create with keys corresponding to the valid line in the
    // file so we can find them later to connect rates. - EV
    // not every line results in an object, so we use a map instead of an ArrayList (an ArrayList
    // with some nulls in it would have worked too) - ST 1/25/05
    val lineMap = collection.mutable.Map[Int, ModelElement]()
    var model: Model = null
    var validLines = 0
    while(tokenizer.nextToken() != TT_EOF) {
      // ignore blank lines
      if(tokenizer.ttype != TT_EOL) {
        // translate pre-4.1 save format
        tokenizer.sval = tokenizer.sval.replaceAll(
          "org.nlogo.aggregate.gui", "org.nlogo.sdm.gui")
        validLines += 1
        val me = processElement(tokenizer)
        me match {
          case m: Model =>
            require(model == null)
            model = m
            model.setDt(dt.toDouble)
          case s: Stock =>
            lineMap(validLines) = s
            // I don't understand why we have to do this - ev 7/22/09
            validLines += 1
          case r: Rate =>
            setSourceSink(r, tokenizer, lineMap)
          case _ =>
        }
        if(me != null)
          model.addElement(me)
        // skip GUI-only stuff, until eol - ST 1/25/05
        while(tokenizer.ttype != TT_EOL &&
              tokenizer.ttype != TT_EOF)
          tokenizer.nextToken()
      }
    }
    model
  }

  private def processElement(st: StreamTokenizer): ModelElement =
    st.sval match {
      case "org.nlogo.sdm.gui.WrappedStock" =>
        val stock = new Stock
        stock.setName(readString(st))
        stock.setInitialValueExpression(readString(st))
        stock.setNonNegative(readBoolean(st))
        stock
      case "org.nlogo.sdm.gui.WrappedRate" =>
        val rate = new Rate
        rate.setExpression(readString(st))
        rate.setName(readString(st))
        rate
      case "org.nlogo.sdm.gui.WrappedConverter" =>
        val converter = new Converter
        converter.setExpression(readString(st))
        converter.setName(readString(st))
        converter
      case "org.nlogo.sdm.gui.AggregateDrawing" =>
        new Model("Test Model", 1)
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
        null
    }

  private def setSourceSink(rate: Rate, st: StreamTokenizer, lineMap: LineMap) {
    if(st.nextToken() == TT_WORD && st.sval.equals("REF")) {
      rate.setSource(getSourceOrSink(st, lineMap))
      rate.setSink(
        if(st.nextToken() == TT_WORD && st.sval.equals("REF"))
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

  private def getSourceOrSink(st: StreamTokenizer, lineMap: LineMap): Stock = {
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
    if(st.nextToken() == TT_NUMBER)
      st.nval == 1
    else 
      throw new java.io.IOException("expected integer (boolean)")
  private def readInt(st: StreamTokenizer) =
    if(st.nextToken() == TT_NUMBER)
      st.nval.toInt
    else
      throw new java.io.IOException("expected integer")

}
