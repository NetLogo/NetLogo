// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core.model

import
  java.lang.{ Double => JDouble, Integer => JInteger }

import
  org.nlogo.core.{ Color, RgbColor }

import
  cats.{ Applicative, Traverse }

import
  cats.data.Validated,
    Validated.{Invalid, Valid}

object XmlReader {
  def bigDecimalReader(name: String): XmlReader[Element, BigDecimal] =
    validReader(name, BigDecimal(_))

  def booleanReader(name: String): XmlReader[Element, Boolean] =
    validReader(name, _.toBoolean)

  def characterReader(name: String): XmlReader[Element, Option[Char]] =
    new OptionalAttributeReader(name).map(_.flatMap(textToOption).flatMap(_.headOption))

  def colorReader(name: String): XmlReader[Element, RgbColor] =
    new AttributeReader(name).flatMap(hexColorToRgbColor(name))

  def pointsReader(name: String): XmlReader[Element, Seq[(Int, Int)]] =
    new AttributeReader(name).flatMap(textToPointsSequence(name))

  def dashArrayReader(name: String): XmlReader[Element, Seq[Float]] =
    new AttributeReader(name).flatMap(textToFloatSeq(name))

  def doubleReader(name: String): XmlReader[Element, Double] =
    validReader(name, _.toDouble)

  def boxedDoubleReader(name: String): XmlReader[Element, JDouble] =
    validReader(name, _.toDouble).map(Double.box _)

  def enumReader[A](options: Map[String, A])(name: String): XmlReader[Element, A] =
    validReader(name, options.apply)

  def intReader(name: String): XmlReader[Element, Int] =
    validReader(name, _.toInt)

  def stringReader(name: String): XmlReader[Element, String] =
    validReader(name, identity)

  def optionalElementReader(name: String): XmlReader[Element, Option[Element]] =
    new OptionalElementReader(name)

  def allElementReader(name: String): XmlReader[Element, Element] =
    new ChildElementReader(name)

  def choiceElementReader[A](readers: Seq[XmlReader[Element, _ <: A]]): XmlReader[Element, A] =
    new ChoiceElementReader[A](readers)

  def elemReader(tag: String): XmlReader[Element, Element] =
    new ElementReader(tag)

  def childrenElemReader: XmlReader[Element, Seq[Element]] =
    new ChildrenElementReader()

  def sequenceElementReader[A](tag: String, min: Int, reader: XmlReader[Element, A]): XmlReader[Element, List[A]] =
    new SequenceElementReader(tag, min, reader)

  // returns all elements read from the specified tag *and* all unread elements in the sequence
  def chainElementReader[A](min: Int, max: Option[Int], reader: XmlReader[Element, A]): XmlReader[Seq[Element], (Seq[A], Seq[Element])] =
    new ChainElementReader(min, max, reader)

  def validReader[A](name: String, f: String => A): XmlReader[Element, A] =
    new AttributeReader(name).flatMap { s =>
      try { Valid(f(s)) } catch { case e: Exception => Invalid(InvalidAttribute(Seq(), name, s)) }
    }

  def childText(xml: Element): String =
    xml.children.collect {
      case t: Text => t.text
    }.mkString("")

  def textToOption(s: String): Option[String] =
    if (s.isEmpty) None
    else Some(s)

  def validHead[A](s: Seq[A], name: String): Validated[ParseError, A] =
    if (s.isEmpty) Invalid(MissingElement(Seq(), name))
    else Valid(s.head)

  private def textToPointsSequence(name: String)(s: String): Validated[ParseError, Seq[(Int, Int)]] = {
    import cats.instances.list._
    try {
      val pointStrings = s.split(" ").toList
      Traverse[List].traverse[({ type l[A] = Validated[ParseError, A] })#l, String, (Int, Int)](pointStrings) { s =>
        val ps = s.split(",")
        if (ps.length == 2) {
          val x = ps(0).toInt
          val y = ps(1).toInt
          Valid((x, y))
        } else {
          Invalid(InvalidAttribute(Seq(), name, s))
        }
      }
    } catch {
      case e: Exception => Invalid(InvalidAttribute(Seq(), name, s))
    }
  }

  private def textToFloatSeq(name: String)(s: String): Validated[ParseError, Seq[Float]] = {
    try {
      Valid(s.split(",").map(_.toFloat))
    } catch {
      case e: Exception => Invalid(InvalidAttribute(Seq(), name, s))
    }
  }

  def rgbColorToDouble(color: RgbColor): Double =
    Color.getClosestColorNumberByARGB(Color.getRGBInt(color.red, color.green, color.blue))

  def doubleToRgbColor(d: Double): RgbColor = {
    val i = Color.getARGBbyPremodulatedColorNumber(d) & 0xffffff // strip off alpha channel
    RgbColor(i & 0xff << 16, i & 0xff << 8, i & 0xff, i & 0xff << 24)
  }

  def rgbColorToHex(c: RgbColor): String = {
    val i = Color.getARGBIntByRgbColor(c) & 0xffffff // strip off alpha channel
    val baseHexString = Integer.toHexString(i)
    val leadingZeros = 6 - baseHexString.length
    s"#${"0" * leadingZeros}${baseHexString}".toUpperCase
  }

  def dashArrayToString(a: Seq[Float]): String = {
    a.map(formatFloat _).mkString(",")
  }

  // get exact reproducibility between JVM and JS runtimes
  def formatDouble(d: Double): String = {
    if (d.toInt == d) "%.1f".format(d)
    else              d.toString
  }

  def formatFloat(f: Float): String = {
    if (f.toInt == f) "%.1f".format(f)
    else              f.toString
  }

  private def hexColorToRgbColor(keyName: String)(hexString: String): Validated[ParseError, RgbColor] = {
    if (hexString.length < 7)
      Invalid(InvalidAttribute(Seq(), keyName, hexString))
    else {
      try {
        val (rs, gs, bs) = (hexString.substring(1, 3), hexString.substring(3, 5), hexString.substring(5, 7))
        val r = JInteger.valueOf(rs, 16)
        val g = JInteger.valueOf(gs, 16)
        val b = JInteger.valueOf(bs, 16)
        Valid(RgbColor(r, g, b))
      } catch {
        case e: NumberFormatException =>
          Invalid(InvalidAttribute(Seq(), keyName, hexString))
      }
    }
  }

  class AttributeReader[A](val name: String) extends XmlReader[Element, String] {
    def read(elem: Element): Validated[ParseError, String] =
      elem.attributes.find(_.name == name)
        .map(a => Valid(a.value))
        .getOrElse(Invalid(MissingKeys(Seq(), Seq(name))))
  }

  class ChildElementReader(val name: String) extends XmlReader[Element, Element] {
    import cats.syntax.option._
    def read(elem: Element): Validated[ParseError, Element] =
      elem.children.collect {
        case e: Element if e.tag == name => e
      }.headOption
        .toValid(MissingElement(Seq(), name))
  }

  class ChoiceElementReader[A](choiceReaders: Seq[XmlReader[Element, _ <: A]]) extends XmlReader[Element, A] {
    val name = s"choice content"
    def read(elem: Element): Validated[ParseError, A] = {
      choiceReaders.map(_.read(elem))
        .reduce(_ orElse _)
        .bimap({
          case m: MissingElement => new MissingElement(m.path, choiceReaders.map(_.name).mkString(" or "))
          case other => other
        },
        identity)
    }
  }

  class ElementReader(tag: String) extends XmlReader[Element, Element] {
    val name = tag

    def read(elem: Element): Validated[ParseError, Element] =
      if (elem.tag == tag) Valid(elem) else Invalid(MissingElement(tag))
  }

  class OptionalAttributeReader[A](val name: String) extends XmlReader[Element, Option[String]] {
    def read(elem: Element): Validated[ParseError, Option[String]] =
      Valid(elem.attributes.find(_.name == name).map(_.value))
  }

  class OptionalElementReader(val name: String) extends XmlReader[Element, Option[Element]] {
    def read(elem: Element): Validated[ParseError, Option[Element]] =
      Valid(
      elem.children.collect {
        case e: Element if e.tag == name => e
      }.headOption)
  }

  class SequenceElementReader[A](tag: String, min: Int, reader: XmlReader[Element, A]) extends XmlReader[Element, List[A]] {
    import cats.instances.list._

    val name = s"$tag sequence content"

    def read(elem: Element): Validated[ParseError, List[A]] = {
      if (elem.tag != tag) {
        Invalid(new MissingElement(Seq(tag), reader.name))
      } else {
        val childElems = elem.children.collect {
          case e: Element => reader.read(e)
        }.toList
        if (childElems.length < min)
          Invalid(new MissingElement(Seq(tag), reader.name))
        else
          Applicative[({ type l[A] = Validated[ParseError, A] })#l].sequence(childElems)
            .bimap({
              case m: MissingElement => new MissingElement(Seq(tag), reader.name)
              case other => other
            }, identity)
      }
    }
  }

  class ChildrenElementReader extends XmlReader[Element, Seq[Element]] {
    val name = s"children"

    def read(elem: Element): Validated[ParseError, Seq[Element]] = {
      Valid(elem.children.collect { case e: Element => e })
    }
  }

  class ChainElementReader[A](min: Int, max: Option[Int], reader: XmlReader[Element, A]) extends XmlReader[Seq[Element], (Seq[A], Seq[Element])] {
    val name = s"${reader.name} sequence content"

    private def readyForReturn(elems: Seq[Element], acc: Seq[A]): Validated[ParseError, (Seq[A], Seq[Element])] =
      if (acc.length < min)
        Invalid(TooFewElements(Seq(), reader.name, min, acc.length))
      else if (max.exists(_ < acc.length))
        Invalid(TooManyElements(Seq(), reader.name, max.get, acc.length))
      else
        Valid((acc, elems))

    @scala.annotation.tailrec
    private def readRec(elems: Seq[Element], acc: Seq[A]): Validated[ParseError, (Seq[A], Seq[Element])] =
      if (elems.isEmpty)
        readyForReturn(elems, acc)
      else
        reader.read(elems.head) match {
          case Valid(a) => readRec(elems.tail, acc :+ a)
          case Invalid(m: MissingElement) => readyForReturn(elems, acc)
          case i: Invalid[ParseError] => i
        }

    def read(elems: Seq[Element]): Validated[ParseError, (Seq[A], Seq[Element])] =
      readRec(elems, Seq())
  }
}

trait XmlReader[A, +B] {
  def name: String
  def read(elem: A): Validated[ParseError, B]
  def map[C](f: B => C): XmlReader[A, C] = new WrappingXmlReader(this, (b: B) => Valid(f(b)))
  def flatMap[C](f: B => Validated[ParseError, C]): XmlReader[A, C] =
    new WrappingXmlReader(this, (b: B) => f(b))
  def path: Seq[String] = Seq()
  def withDefault[C >: B](default: C): XmlReader[A, C] = new DefaultXmlReader(this, default)
  def atPath(path: Seq[String]): XmlReader[A, B] = new PathedXmlReader(this, path)
  def atPath(path: String): XmlReader[A, B] = atPath(Seq(path))
}

// we want to be able to express reading sequences as a chain of readers
// [B](Seq[E] => (E => F[B]) => F[(Seq[B], Seq[E])])
//
// At the moment, all of our readers are [B](E => F[B])
//
// We need to introduce some new type of reader that still follows several basic rules:
// * It must be pathable
// * It must be mappable

class WrappingXmlReader[A, B, C](wrappedReader: XmlReader[A, B], f: B => Validated[ParseError, C]) extends XmlReader[A, C] {
  def name = wrappedReader.name

  def read(elem: A): Validated[ParseError, C] =
    wrappedReader.read(elem).andThen(f)
}

class DefaultXmlReader[A, B](wrappedReader: XmlReader[A, B], default: B) extends XmlReader[A, B] {
  def name = wrappedReader.name
  def read(elem: A): Validated[ParseError, B] =
    wrappedReader.read(elem)
      .fold(
        e => e match {
          case m: MissingElement => Valid(default)
          case k: MissingKeys => Valid(default)
          case other => Invalid(other)
        },
        b => Valid(b))
}

class PathedXmlReader[A, B](wrappedReader: XmlReader[A, B], override val path: Seq[String]) extends XmlReader[A, B] {
  def name = wrappedReader.name
  def read(elem: A): Validated[ParseError, B] =
    wrappedReader.read(elem).bimap(_.atPath(path), identity)
  override def atPath(newPath: Seq[String]): XmlReader[A, B] = new PathedXmlReader(this, newPath ++ path)
}
