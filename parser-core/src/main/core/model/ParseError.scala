// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core.model

import cats.kernel.Semigroup

object ParseError {
  implicit val parseErrorSemigroup = new Semigroup[ParseError] {
    def combine(x: ParseError, y: ParseError): ParseError = {
      (x, y) match {
        case (x: UnknownElementType, _) => x
        case (_, y: UnknownElementType) => y
        case (x: MissingKeys, y: MissingKeys) => new MissingKeys(x.path, (x.keys ++ y.keys).distinct)
        case (x: InvalidValue, y: InvalidValue) => x
        case (x: MissingValues, _) => x
        case (_, y: MissingValues) => y
      }
    }
  }
}

sealed trait ParseError {
  def message: String
  def atPath(p: String): ParseError = atPath(Seq(p))
  def atPath(additionalPath: Seq[String]): ParseError
}

trait MissingValues extends ParseError

case class MissingKeys(path: Seq[String], keys: Seq[String]) extends MissingValues {
  val message =
    if (keys.length == 1)
      s"${path.mkString("/")} is missing required attribute '${keys.head}'"
    else
      s"${path.mkString("/")} is missing required attributes ${keys.map(k => s"'$k'").mkString(", ")}"

  def atPath(additionalPath: Seq[String]): ParseError =
    copy(path = additionalPath ++ path, keys)
}

object MissingElement {
  def apply(path: Seq[String], elementName: String): MissingElement =
    new MissingElement(path, elementName)
  def apply(elementName: String): MissingElement =
    new MissingElement(Seq(), elementName)
}

class MissingElement(val path: Seq[String], val elementName: String) extends MissingValues {
  val message = s"expected ${path.mkString("/")} to contain child element $elementName"
  override def equals(other: Any): Boolean = {
    other match {
      case m: MissingElement => m.path == path && m.elementName == elementName
      case _ => false
    }
  }
  override def toString: String =
    s"MissingElement(${path.mkString("/")}, $elementName)"

  def atPath(additionalPath: Seq[String]): ParseError =
    new MissingElement(path = additionalPath ++ path, elementName)
}

case class TooFewElements(path: Seq[String], elementName: String, min: Int, actual: Int) extends MissingValues {
  val message = s"expected to find at least ${min} ${elementName} elements at ${path.mkString("/")}, but found only ${actual}"

  def atPath(additionalPath: Seq[String]): ParseError =
    TooFewElements(path = additionalPath ++ path, elementName, min, actual)
}

case class TooManyElements(path: Seq[String], elementName: String, max: Int, actual: Int) extends MissingValues {
  val message = s"expected to find at most ${max} ${elementName} elements at ${path.mkString("/")}, but found only ${actual}"

  def atPath(additionalPath: Seq[String]): ParseError =
    TooManyElements(path = additionalPath ++ path, elementName, max, actual)
}

class MissingOneOfElements(val path: Seq[String], val possibilities: Seq[String]) extends MissingValues {
  val message = s"expected ${path.mkString("/")} to contain one of ${possibilities.mkString(", ")}"

  override def equals(other: Any): Boolean = {
    other match {
      case m: MissingOneOfElements => m.path == path && m.possibilities == possibilities
      case _ => false
    }
  }
  override def toString: String =
    s"MissingOneOfElements(${path.mkString("/")}, $possibilities)"

  def atPath(additionalPath: Seq[String]): ParseError =
    new MissingOneOfElements(path = additionalPath ++ path, possibilities)
}

sealed trait InvalidValue extends ParseError

object InvalidAttribute {
  def apply(path: Seq[String], name: String, value: String): InvalidAttribute =
    new InvalidAttribute(path, name, value)
  def apply(name: String, value: String): InvalidAttribute =
    new InvalidAttribute(Seq(), name, value)
}

class InvalidAttribute(val path: Seq[String], val name: String, val value: String) extends InvalidValue {
  def message = s"${path.mkString("/")} has invalid value '${value}' for $name attribute"
  override def equals(other: Any): Boolean = {
    other match {
      case i: InvalidAttribute => i.path == path && i.name == name && i.value == value
      case _ => false
    }
  }

  override def toString =
    s"InvalidAttribute(${path.mkString("/")}, $name, $value)"

  def atPath(additionalPath: Seq[String]): ParseError =
    new InvalidAttribute(path = additionalPath ++ path, name, value)
}

class InvalidElement(val path: Seq[String], val name: String, val value: String) extends InvalidValue {
  def message = s"${path.mkString("/")} has invalid value '${value}' for element $name"

  override def equals(other: Any): Boolean = {
    other match {
      case i: InvalidElement => i.path == path && i.name == name && i.value == value
      case _ => false
    }
  }

  override def toString: String =
    s"InvalidElement(${path.mkString("/")}, $name, $value)"

  def atPath(additionalPath: Seq[String]): ParseError =
    new InvalidElement(path = additionalPath ++ path, name, value)
}

case class RequiredValue(_path: Seq[String], _name: String) extends InvalidAttribute(_path, _name, "") {
  override def message = s"${path.mkString("/")} is missing a required value for $name"

  override def atPath(additionalPath: Seq[String]): ParseError =
    new RequiredValue(additionalPath ++ path, _name)
}

class InvalidMultipleValues(val path: Seq[String], val possibilities: Seq[String]) extends InvalidValue {
  def message = s"Expected ${path.mkString("/")} to have a value for exactly one of ${possibilities.mkString(", ")}"
  override def equals(other: Any): Boolean = {
    other match {
      case i: InvalidMultipleValues => i.path == path && i.possibilities == possibilities
      case _ => false
    }
  }

  override def atPath(additionalPath: Seq[String]): ParseError =
    new InvalidMultipleValues(path = additionalPath ++ path, possibilities)
}

case class UnknownElementType(path: Seq[String]) extends ParseError {
  override def message = s"Unknown element type ${path.last} at ${path.init.mkString("/")}"

  override def atPath(additionalPath: Seq[String]): ParseError =
    new UnknownElementType(additionalPath ++ path)
}
