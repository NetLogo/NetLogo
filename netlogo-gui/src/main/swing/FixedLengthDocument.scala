// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.util.Locale

/**
 * enforces a given maximum length; also enforces all caps (that could easily be made optional)
 */

class FixedLengthDocument(maxLength: Int) extends javax.swing.text.PlainDocument {
  override def insertString(offset: Int, str: String, a: javax.swing.text.AttributeSet): Unit = {
    if(getLength + str.length <= maxLength)
      super.insertString(offset, str.toUpperCase(Locale.ENGLISH), a)
  }
  override def replace(offset: Int, length: Int, str: String, a: javax.swing.text.AttributeSet): Unit = {
    if(getLength - length + str.length <= maxLength )
      super.replace(offset, length, str.toUpperCase(Locale.ENGLISH), a)
  }
}
