// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import scala.collection.{ Iterable, IterableOnce, WithFilter }

object SymbolTable {
  def empty = new SymbolTable(Map[String, SymbolType]())
  def apply(pairs: (String, SymbolType)*) = new SymbolTable(Map[String, SymbolType](pairs: _*))
}

class SymbolTable(private val syms: Map[String, SymbolType], private val uniqueVarID: Int = 0) extends WithFilter[(String, SymbolType), Iterable] {
  def flatMap[B](f: ((String, SymbolType)) => IterableOnce[B]): Iterable[B] =
    syms.flatMap(f)

  def map[B](f: ((String, SymbolType)) => B): Iterable[B] =
    syms.map(f)

  def withFilter(p: ((String, SymbolType)) => Boolean): WithFilter[(String, SymbolType), Iterable] =
    new SymbolTable(syms.filter(p), uniqueVarID)

  def foreach[U](f: ((String, SymbolType)) => U): Unit =
    syms.foreach(f)

  def addSymbols(symNames: Iterable[String], tpe: SymbolType): SymbolTable =
    new SymbolTable(syms ++ symNames.map(_.toUpperCase -> tpe).toMap, uniqueVarID)

  def addSymbol(symName: String, tpe: SymbolType): SymbolTable =
    new SymbolTable(syms + (symName.toUpperCase -> tpe), uniqueVarID)

  def ++(other: SymbolTable): SymbolTable =
    new SymbolTable(syms ++ other.syms, uniqueVarID + other.uniqueVarID)

  def -(name: String): SymbolTable =
    new SymbolTable(syms - name.toUpperCase, uniqueVarID)

  def apply(name: String) = syms(name.toUpperCase)

  def contains(name: String) = syms.isDefinedAt(name.toUpperCase)

  def get(name: String): Option[SymbolType] = syms.get(name.toUpperCase)

  def withFreshSymbol(symType: SymbolType, hint: String = ""): (String, SymbolTable) = {
    var foundSymbolAndID = Option.empty[(String, Int)]
    var currentVarID = uniqueVarID

    while (foundSymbolAndID.isEmpty) {
      val potentialName = (hint + "_" + currentVarID.toString).toUpperCase
      if (contains(potentialName)) currentVarID += 1
      else foundSymbolAndID = Some((potentialName, currentVarID))
    }

    val (symbolName, symbolID) = foundSymbolAndID.get
    (symbolName, new SymbolTable(syms + (symbolName -> symType), symbolID + 1))
  }

  override def equals(that: Any): Boolean = {
    that match {
      case st: SymbolTable => st.syms == syms
      case _ => super.equals(that)
    }
  }

  override def toString: String =
    syms.mkString(", ")
}
