package org.nlogo.engine

import scala.js.{ Boolean => BooleanJS, Number => NumberJS, String => StringJS }

trait JS2ScalaConverter[T] {
  def asScala: T
}

object JS2ScalaConverters {
  // Coming soon. --JAB (8/3/13)
}

trait Dynamic2ScalaConverter[T] {
  def asScala: T
}

object Dynamic2ScalaConverters {

  implicit def num2Boolean(that: Dynamic) = new Dynamic2ScalaConverter[Boolean] {
    override def asScala: Boolean = BooleanJS.toBoolean(that.asInstanceOf[BooleanJS])
  }

  implicit def num2Double(that: Dynamic) = new Dynamic2ScalaConverter[Double] {
    override def asScala: Double = NumberJS.toDouble(that.asInstanceOf[NumberJS])
  }

  implicit def num2String(that: Dynamic) = new Dynamic2ScalaConverter[String] {
    override def asScala: String = StringJS.toScalaString(that.asInstanceOf[StringJS])
  }

  implicit def num2Byte(that: Dynamic) = new Dynamic2ScalaConverter[Byte] {
    override def asScala: Byte = NumberJS.toDouble(that.asInstanceOf[NumberJS]).toByte
  }

  implicit def num2Float(that: Dynamic) = new Dynamic2ScalaConverter[Float] {
    override def asScala: Float = NumberJS.toDouble(that.asInstanceOf[NumberJS]).toFloat
  }

  implicit def num2Int(that: Dynamic) = new Dynamic2ScalaConverter[Int] {
    override def asScala: Int = NumberJS.toDouble(that.asInstanceOf[NumberJS]).toInt
  }

  implicit def num2Long (that: Dynamic) = new Dynamic2ScalaConverter[Long] {
    override def asScala: Long = NumberJS.toDouble(that.asInstanceOf[NumberJS]).toLong
  }

  implicit def num2Short(that: Dynamic) = new Dynamic2ScalaConverter[Short] {
    override def asScala: Short = NumberJS.toDouble(that.asInstanceOf[NumberJS]).toShort
  }

}
