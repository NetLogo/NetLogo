// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.core.Let

import scala.annotation.tailrec

object Binding {
  trait ChildBinding {
    val let: Let // may be null
    val binding: LetBinding
    def value = binding.value
    def next: ChildBinding
    def withNext(newNext: ChildBinding): ChildBinding
    def concat(other: ChildBinding): ChildBinding
  }

  case class BoundLet(let: Let, val binding: LetBinding, val next: ChildBinding) extends ChildBinding {
    def withNext(newNext: ChildBinding): ChildBinding =
      copy(next = newNext)

    def concat(other: ChildBinding): ChildBinding =
      addBindings(other, this)

    @tailrec
    private def addBindings(toAdd: ChildBinding, acc: ChildBinding): ChildBinding = {
      if (toAdd eq EmptyBinding)
        acc
      else
        addBindings(toAdd.next, toAdd.withNext(acc))
    }
  }

  case object EmptyBinding extends ChildBinding {
    val let = null
    val binding: LetBinding = null
    val next = this
    def withNext(newNext: ChildBinding): ChildBinding =
      newNext match {
        case EmptyBinding => this
        case boundLet: BoundLet => boundLet.withNext(this)
        case _ => throw new IllegalStateException
      }
    def concat(other: ChildBinding): ChildBinding =
      other
  }
}

import Binding._

class Binding(var head: ChildBinding, val parent: Binding, var size: Int = 0) {
  def this(parent: Binding) =
    this(EmptyBinding, parent)

  def this() =
    this(EmptyBinding, null)

  def enterScope: Binding = {
    new Binding(EmptyBinding, this)
  }

  def enterScope(lets: Array[Let], values: Array[AnyRef]): Binding = {
    var childBinding: ChildBinding = EmptyBinding
    // Note that `values.size` is intentionally allowed to be greater than
    // `lets.size` for anonymous procedures. -- BCH 06/28/2017
    var i = 0
    while (i < lets.length) {
      val let = lets(i)
      childBinding = BoundLet(let, LetBinding(let, values(i)), childBinding)
      i += 1
    }
    new Binding(childBinding, this)
  }

  def exitScope: Binding = {
    if (parent == null)
      throw new IllegalStateException("Attempting to exit top-level scope!")
    parent
  }

  @tailrec
  private def removeLet(b: ChildBinding, let: Let, removed: ChildBinding): (ChildBinding, Boolean) = {
    if (b.let eq let)
      (removed.concat(b.next), true)
    else if (b.next ne b)
      removeLet(b.next, let, b.withNext(removed))
    else
      (removed, false)
  }

  def let(let: Let, value: AnyRef): Unit = {
    val (removedBindings, didRemove) = removeLet(head, let, EmptyBinding)
    if (! didRemove) { size += 1 }
    head = BoundLet(let, LetBinding(let, value), removedBindings)
  }

  @tailrec
  private def updateWalk(b: ChildBinding, let: Let, value: AnyRef): Unit = {
    if (b.let eq let)
      b.binding.value = value
    else if (b.next ne b)
      updateWalk(b.next, let, value)
    else if (parent == null)
      throw new IllegalStateException(s"Attempted to set undefined variable ${let.name}")
    else
      parent.updateWalk(parent.head, let, value)
  }

  def setLet(let: Let, value: AnyRef): Unit = {
    updateWalk(head, let, value)
  }

  @tailrec
  private def findWalk(b: ChildBinding, let: Let): AnyRef = {
    if (b.let eq let)
      b.value
    else if (b.next ne b)
      findWalk(b.next, let)
    else if (parent == null)
      throw new NoSuchElementException(s"Could not find bound value for ${let.name}")
    else
      parent.findWalk(parent.head, let)
  }

  def getLet(let: Let): AnyRef = {
    findWalk(head, let)
  }

  /* This is for testing purposes only */
  def getLetByName(name: String): AnyRef = {
    var iter = this
    var found: AnyRef = null
    while (found == null && iter != null) {
      var b = iter.head
      while (b.next != b) {
        if (b.let.name == name)
          found = b.value
        else
          b = b.next
      }
      iter = iter.parent
    }
      if (found == null)
        throw new NoSuchElementException(s"Could not find bound value for ${name}")
    found
  }

  def allLets: List[(Let, AnyRef)] = {
    var ret = List.empty[(Let, AnyRef)]
    var iter = this
    while (iter != null) {
      var b = iter.head
      while (b.let != null) {
        ret = (b.let, b.value) :: ret
        b = b.next
      }
      iter = iter.parent
    }
    ret.reverse
  }

  def copy: Binding =
    new Binding(head, parent, size)
}
