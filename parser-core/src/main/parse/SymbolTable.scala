// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import scala.collection.immutable.{ Map => ImmutableMap }
import scala.collection.generic.{ CanBuildFrom, FilterMonadic }
import scala.collection.GenTraversableOnce

object SymbolTable {
  def empty = new SymbolTable(ImmutableMap[String, SymbolType]())
  def apply(pairs: (String, SymbolType)*) = new SymbolTable(ImmutableMap[String, SymbolType](pairs: _*))
}

class SymbolTable(private val syms: ImmutableMap[String, SymbolType], private val uniqueVarID: Int = 0) extends FilterMonadic[(String, SymbolType), SymbolTable] {
  def flatMap[B, That](f: ((String, SymbolType)) => GenTraversableOnce[B])(implicit bf: CanBuildFrom[SymbolTable,B,That]): That = {
    val builder = bf()
    syms.flatMap(f).foreach(builder += _)
    builder.result
  }

  def map[B, That](f: ((String, SymbolType)) => B)(implicit bf: CanBuildFrom[SymbolTable,B,That]): That = {
    val builder = bf()
    builder ++= syms.map(f)
    builder.result
  }

  def withFilter(p: ((String, SymbolType)) => Boolean): FilterMonadic[(String, SymbolType), SymbolTable] =
    new SymbolTable(syms.filter(p), uniqueVarID)

  def foreach[U](f: ((String, SymbolType)) => U): Unit = syms.foreach(f)

  def addSymbols(symNames: Iterable[String], tpe: SymbolType): SymbolTable =
    new SymbolTable(syms ++ symNames.map(_.toUpperCase -> tpe).toMap, uniqueVarID)

  def addSymbol(symName: String, tpe: SymbolType): SymbolTable =
    new SymbolTable(syms + (symName.toUpperCase -> tpe), uniqueVarID)

  def ++(other: SymbolTable): SymbolTable =
    new SymbolTable(syms ++ other.syms, uniqueVarID + other.uniqueVarID)

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

    val Some((symbolName, symbolID)) = foundSymbolAndID
    (symbolName, new SymbolTable(syms + (symbolName -> symType), symbolID + 1))
  }
}
