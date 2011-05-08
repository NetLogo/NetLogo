package org.nlogo.util

import org.scalatest.FunSuite
import HexString._

class HexStringTests extends FunSuite {
  test("bytes0") {
    assert("" === toHexString(Array[Byte]())) }
  test("bytes1") {
    assert("007F8180" === toHexString(Array(0: Byte, 127: Byte, -127: Byte, -128: Byte))) }
  test("bytes2") {
    assert("000102030405060708090A0B0C0D0E0F" === toHexString((0 to 15).map(_.toByte).toArray)) }
  test("ints0") {
    assert("" === toHexString(Array[Int]())) }
  test("ints1") {
    assert("00000000800000007FFFFFFF" === toHexString(Array(0, Integer.MIN_VALUE, Integer.MAX_VALUE))) }
}


// now let's throw ScalaCheck at it.

import org.scalacheck.{Properties, Prop}
import Prop._

object HexStringTests2 extends Properties("HexString") {
  property("8 characters per Int") =
    forAll((ns: Array[Int]) =>
      ns.size * 8 == toHexString(ns).size)
  property("2 characters per Byte") =
    forAll((ns: Array[Byte]) =>
      ns.size * 2 == toHexString(ns).size)

  property("output for Int parses as hex string") =
    forAll((ns: Array[Int]) =>
      ns.nonEmpty ==> { BigInt(toHexString(ns), 16); true })
  property("output for Byte parses as hex string") =
    forAll((ns: Array[Byte]) =>
      ns.nonEmpty ==> { BigInt(toHexString(ns), 16); true })

  // parseInt/toByte does the right thing with negative numbers
  def parseByte(s: String) = java.lang.Integer.parseInt(s, 16).toByte
  // parseLong/toInt, ditto
  def parseInt(s: String) = java.lang.Long.parseLong(s, 16).toInt
  def chunks(s: String, n: Int): List[String] =
    if(s.isEmpty) Nil
    else s.take(n) :: chunks(s.drop(n), n)

  property("Bytes survive conversion there and back unchanged") =
    forAll((ns: Array[Byte]) =>
      ns sameElements chunks(toHexString(ns), 2).map(parseByte))
  property("Ints survive conversion there and back unchanged") =
    forAll((ns: Array[Int]) =>
      ns sameElements chunks(toHexString(ns), 8).map(parseInt))

}
