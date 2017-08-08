// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import
  org.nlogo.core.model.{ Element, ElementFactory, ParseError }

import
  cats.data.Validated

import
  cats.Traverse

import
  scala.util.{ Failure, Success, Try }

trait NLogoXBaseReader {
  def factory: ElementFactory

  def toSeqElement[A](name: String, data: Seq[A], toElem: A => Element): Element =
    factory.newElement(name)
      .withElementList(data.map(toElem))
      .build

  def parseChildren[A](e: Element, f: Element => Validated[ParseError, A]): Try[Seq[A]] = {
    parseElements[A](e.children.collect { case e: Element =>  e }, f)
  }

  def parseElements[A](elems: Seq[Element], f: Element => Validated[ParseError, A]): Try[Seq[A]] = {
    import cats.instances.list._
    Traverse[List].traverse[({ type l[A] = Validated[ParseError, A] })#l, Element, A](
      elems.toList)(f).fold(
        e => Failure(new NLogoXFormatException(e.message)),
        l => Success(l)
      )
  }
}
