// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api.model

import org.nlogo.api

object ModelCreator {

  private val template = """|<<CODE SECTION>>
                            |@#$#@#$#@
                            |GRAPHICS-WINDOW
                            |0
                            |0
                            |0
                            |0
                            |<<MAX-PXCOR-OR-MINUS-ONE>>
                            |<<MAX-PYCOR-OR-MINUS-ONE>>
                            |12.0
                            |0
                            |10
                            |1
                            |1
                            |1
                            |0
                            |<<WRAPPING-ALLOWED-IN-X>>
                            |<<WRAPPING-ALLOWED-IN-Y>>
                            |1
                            |<<MIN-PXCOR>>
                            |<<MAX-PXCOR>>
                            |<<MIN-PYCOR>>
                            |<<MAX-PYCOR>>
                            |
                            |<<SLIDER SECTION>>
                            |<<SWITCH SECTION>>
                            |<<CHOOSER SECTION>>
                            |<<INPUTBOX SECTION>>
                            |<<PLOT SECTION>>
                            |
                            |@#$#@#$#@
                            |@#$#@#$#@
                            |<<TURTLE SHAPES SECTION>>
                            |@#$#@#$#@
                            |NetLogo 5.0
                            |@#$#@#$#@
                            |<<PREVIEW SECTION>>
                            |@#$#@#$#@
                            |@#$#@#$#@
                            |@#$#@#$#@
                            |@#$#@#$#@
                            |<<LINK SHAPES SECTION>>
                            |@#$#@#$#@""".stripMargin

  //
  // Code to create models
  //


}
