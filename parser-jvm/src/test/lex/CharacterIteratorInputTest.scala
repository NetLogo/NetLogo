// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import java.text.StringCharacterIterator

import org.nlogo.core.TokenType
import org.scalatest.FunSuite

class CharacterIteratorInputTest extends FunSuite {
  trait Helper {
    var text: String = ""
    lazy val seg = new StringCharacterIterator(text)
    lazy val input = new CharacterIteratorInput(seg, "")
  }

  test("iterator input - empty string") { new Helper {
    text = ""
    assert(! input.hasNext)
  } }

  test("iterator input - single character") { new Helper {
    text = "a"
    assert(input.hasNext)
    assert(input.offset == 0)
  } }

  test("iterator input - longest prefix") { new Helper {
    text = "a"
    val (res, nextInput) = input.longestPrefix(LexOperations.characterMatching(c => c == 'a'))
    assert(res == "a")
    assert(! nextInput.hasNext)
    assert(nextInput.offset == 1)
  } }

  test("iterator input - longest prefix of number") { new Helper {
    text = "123"
    val (res, nextInput) = input.longestPrefix(StandardLexer.numericLiteral._1)
    assert(res == "123")
    assert(! nextInput.hasNext)
    assert(nextInput.offset == 3)
  } }

  test("iterator input - assemble token") { new Helper {
    text = "a"
    val Some((res, nextInput)) =
      input.assembleToken(LexOperations.characterMatching(c => c == 'a'), (s) => Some((s, TokenType.Ident, null)))
    assert(res.start == 0)
    assert(res.end == 1)
    assert(res.tpe == TokenType.Ident)
    assert(! nextInput.hasNext)
    assert(nextInput.offset == 1)
  } }
}
