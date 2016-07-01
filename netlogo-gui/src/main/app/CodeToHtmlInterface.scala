// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

trait CodeToHtmlInterface {
  def convert(code: String): String
}
