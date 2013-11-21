// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mirror

object WidgetKinds {

  case object Monitor extends Kind {
    object Variables extends Enumeration {
      val ValueString = Value("valueString")
    }
  }

  case object Switch extends Kind {
    object Variables extends Enumeration {
      val IsOn = Value("isOn")
    }
  }

  case object Chooser extends Kind {
    object Variables extends Enumeration {
      val ValueObject = Value("valueObject")
    }
  }

  case object Slider extends Kind {
    object Variables extends Enumeration {
      val SliderValue = Value("sliderValue")
      val Minimum = Value("minimum")
      val Increment = Value("increment")
      val Maximum = Value("maximum")
    }
  }
}
