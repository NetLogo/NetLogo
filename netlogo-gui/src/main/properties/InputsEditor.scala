// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import org.nlogo.api.Options
import org.nlogo.sdm.gui.{ ConverterFigure, RateConnection }
import org.nlogo.api.Editable

class InputsEditor[T](accessor: PropertyAccessor[Options[T]], target: Editable, propertyEditors: collection.mutable.ArrayBuffer[PropertyEditor[_]])
  extends OptionsEditor(accessor)
{
    override def changed() {
      if (get.isDefined) {
        if (true) {
          apply()

          if (target.isInstanceOf[ConverterFigure]
              || target.isInstanceOf[RateConnection]) {
            if (accessor.get.isInstanceOf[Options[_]]) {
              val inputChoice = accessor.get.asInstanceOf[Options[_]].chosenName
              if (inputChoice != "Select") {
                // should get editor <- propertyEditors => editor.accessor.displayName == "Expression"
                val exprAcc = propertyEditors.last.accessor
                if (exprAcc.displayName == "Expression") {
                  exprAcc.asInstanceOf[PropertyAccessor[String]].set(exprAcc.get.asInstanceOf[String].trim + " " + (inputChoice) + " ")
                  propertyEditors.last.refresh
                }
              }
            }
          }
        }
      }
    }


}
