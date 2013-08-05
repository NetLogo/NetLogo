package org.nlogo.tortoise.engine

import scala.js.{ Any => AnyJS, Boolean => BooleanJS, Dynamic, Number => NumberJS, String => StringJS }

object JS2WrapperConverter {

  def apply(js: AnyJS): JSW =
    this.get(js) getOrElse (throw new IllegalArgumentException("Could not convert input to JS wrapper!"))

  // `js.isInstanceOf[BooleanJS]` crashes the compiler, so I can't pattern match. :( --JAB (8/1/13)
  // `js.asInstanceOf[BooleanJS]` makes a good-faith truthiness effort, rather than throwing an exception, so....  --JAB (8/1/13)
  def get(js: AnyJS): Option[JSW] =
    StringJS.toScalaString(Dynamic.global.typeOf(js).asInstanceOf[StringJS]) match {
      case "boolean" => Option(js.asInstanceOf[BooleanJS]: Boolean)
      case "number"  => Option(js.asInstanceOf[NumberJS]:  Double)
      case "string"  => Option(js.asInstanceOf[StringJS]:  String)
      case _         => None
    }

}
