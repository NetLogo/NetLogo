// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.core.Let

import java.util.LinkedHashSet

object Binding {
  trait ChildBinding {
    val let: Let // may be null
    var value: AnyRef // may be null
    def next: ChildBinding
    def next_=(newNext: ChildBinding): Unit
  }

  case class BoundLet(let: Let, var value: AnyRef, var next: ChildBinding) extends ChildBinding

  case object EmptyBinding extends ChildBinding {
    val let = null
    var value: AnyRef = null
    val next = this
    def next_=(newNext: ChildBinding): Unit = {}
  }
}

import Binding._

class Binding(var head: ChildBinding, val parent: Binding, val containedLets: LinkedHashSet[Let]) {
  def this(parent: Binding) =
    this(EmptyBinding, parent, new LinkedHashSet[Let]())

  def this() =
    this(EmptyBinding, null, new LinkedHashSet[Let]())

  def enterScope(): Binding = {
    new Binding(EmptyBinding, this, new LinkedHashSet[Let]())
  }

  def exitScope(): Binding = {
    if (parent == null)
      throw new IllegalStateException("Attempting to exit top-level scope!")
    parent
  }

  @scala.annotation.tailrec
  private def rebindLet(b: ChildBinding, let: Let, value: AnyRef): Unit = {
    if (b.next.let == let) // when let isn't first, point the prior BoundLet to it
      b.next = new BoundLet(let, value, b.next.next)
    else if (b.let == let) // when let is first, we don't need to repoint any `next`s
      head = new BoundLet(let, value, b.next)
    else if (b.next ne b)
      rebindLet(b.next, let, value)
  }

  def let(let: Let, value: AnyRef): Unit = {
    if (containedLets.contains(let)) {
      rebindLet(head, let, value)
    } else {
      containedLets.add(let)
      head = new BoundLet(let, value, head)
    }
  }

  @scala.annotation.tailrec
  private def updateWalk(b: ChildBinding, let: Let, value: AnyRef): Unit = {
    if (b.let == let)
      b.value = value
    else if (b.next == b)
      throw new IllegalStateException(s"Attempted to set undefined variable ${let.name}")
    else
      updateWalk(b.next, let, value)
  }

  def setLet(let: Let, value: AnyRef): Unit = {
    if (containedLets.contains(let))
      updateWalk(head, let, value)
    else {
      var updated: Boolean = false
      var iter = parent
      while (iter != null && !updated) {
        if (iter.containedLets.contains(let)) {
          updateWalk(iter.head, let, value)
          updated = true
        }
        iter = iter.parent
      }
      if (! updated)
        throw new IllegalStateException(s"Attempted to set undefined variable ${let.name}")
    }
  }

  @scala.annotation.tailrec
  private def findWalk(b: ChildBinding, let: Let): AnyRef = {
    if (b.let == let)
      b.value
    else if (b.next == b)
      throw new NoSuchElementException(s"Could not find bound value for ${let.name}")
    else
      findWalk(b.next, let)
  }

  def getLet(let: Let): AnyRef = {
    if (containedLets.contains(let))
      findWalk(head, let)
    else {
      var found: AnyRef = null
      var iter = parent
      while (iter != null && found == null) {
        if (iter.containedLets.contains(let)) {
          found = findWalk(iter.head, let)
        }
        iter = iter.parent
      }
      if (found == null)
        throw new NoSuchElementException(s"Could not find bound value for ${let.name}")
      found
    }
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
    new Binding(head, parent, containedLets.clone.asInstanceOf[LinkedHashSet[Let]])
}
