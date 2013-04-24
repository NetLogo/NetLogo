// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.api
import org.nlogo.util.Femto

object Parser {

  // tokenizer singletons
  val Tokenizer2D =
    Femto.scalaSingleton(classOf[api.TokenizerInterface],
      "org.nlogo.lex.Tokenizer2D")
  val Tokenizer3D =
    Femto.scalaSingleton(classOf[api.TokenizerInterface],
      "org.nlogo.lex.Tokenizer3D")
  val TokenMapper2D =
    Femto.scalaSingleton(classOf[api.TokenMapperInterface],
      "org.nlogo.lex.TokenMapper2D")

}
