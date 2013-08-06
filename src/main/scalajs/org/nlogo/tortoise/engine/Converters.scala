package org.nlogo.tortoise.engine

trait JS2ScalaConverter[T] {
  def asScala: T
}

object JS2ScalaConverters {

  implicit def num2Boolean(that: NumberJS) = new JS2ScalaConverter[Boolean] {
    override def asScala: Boolean = BooleanJS.toBoolean(that.asInstanceOf[BooleanJS])
  }

  implicit def num2Double(that: NumberJS) = new JS2ScalaConverter[Double] {
    override def asScala: Double = NumberJS.toDouble(that)
  }

  implicit def num2String(that: NumberJS) = new JS2ScalaConverter[String] {
    override def asScala: String = StringJS.toScalaString(that.asInstanceOf[StringJS])
  }

  implicit def num2Byte(that: NumberJS) = new JS2ScalaConverter[Byte] {
    override def asScala: Byte = NumberJS.toDouble(that).toByte
  }

  implicit def num2Float(that: NumberJS) = new JS2ScalaConverter[Float] {
    override def asScala: Float = NumberJS.toDouble(that).toFloat
  }

  implicit def num2Int(that: NumberJS) = new JS2ScalaConverter[Int] {
    override def asScala: Int = NumberJS.toDouble(that).toInt
  }

  implicit def num2Long (that: NumberJS) = new JS2ScalaConverter[Long] {
    override def asScala: Long = NumberJS.toDouble(that).toLong
  }

  implicit def num2Short(that: NumberJS) = new JS2ScalaConverter[Short] {
    override def asScala: Short = NumberJS.toDouble(that).toShort
  }

}

// These should be avoided.  You should favor writing a ReifiedJS interface for your JS objects,
// and then using the above JS converters. --JAB (8/6/13)
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
