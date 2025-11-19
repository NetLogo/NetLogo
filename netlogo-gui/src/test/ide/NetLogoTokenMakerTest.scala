// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.ide

import javax.swing.text.Segment

import org.fife.ui.rsyntaxtextarea.{ Token => RstaToken, TokenTypes }

import org.nlogo.core.TokenType
import org.nlogo.api.DummyExtensionManager
import org.nlogo.nvm.ExtensionManager
import org.nlogo.util.AnyFunSuiteEx

class NetLogoTokenMakerTest extends AnyFunSuiteEx {
  trait Helper {
    var text: String = ""
    val offset = 0
    def seg = new Segment(text.toCharArray, 0, text.length)
    val extensionManager = new DummyExtensionManager with ExtensionManager {
      override def cachedType(name: String): Option[TokenType] =
        name match {
          case "ARRAY:ITEM" => Some(TokenType.Reporter)
          case "ARRAY:SET"  => Some(TokenType.Command)
          case _            => None
        }
    }
    val nlTokenMaker = new NetLogoTwoDTokenMaker(Some(extensionManager))
    def tokens: RstaToken = nlTokenMaker.getTokenList(seg, TokenTypes.NULL, offset)
    def tokenToSeq(t: RstaToken, acc: Seq[RstaToken]): Seq[RstaToken] =
      t match {
        case null => acc
        case tok  => tokenToSeq(t.getNextToken, acc :+ tok)
      }
    def tokenSeq = tokenToSeq(tokens, Seq())
  }

  test("NetLogoTokenMaker: empty") { new Helper {
    assert(tokens.getNextToken == null)
  } }

  test("NetLogoTokenMaker: single token") { new Helper {
    text = "123"
    val t = tokens
    assert(t.getType == TokenTypes.LITERAL_NUMBER_FLOAT)
    assert(t.getLexeme == "123")
    assert(t.getNextToken == null)
  } }

  test("NetLogoTokenMaker: whitespace") { new Helper {
    text = " "
    val t = tokens
    assert(t.getType == TokenTypes.WHITESPACE)
    assert(t.getLexeme == " ")
    assert(t.getNextToken == null)
  } }

  test("NetLogoTokenMaker: single token followed by whitespace") { new Helper {
    text = "123 "
    assert(tokenSeq(1) != null)
    assert(tokenSeq(1).getType == TokenTypes.WHITESPACE)
    assert(tokenSeq(1).getLexeme == " ")
    assert(tokenSeq(1).getNextToken == null)
  } }

  test("NetLogoTokenMaker: two tokens separated by whitespace") { new Helper {
    text = "123 \"456\""
    assert(tokenSeq.length == 3)
    assert(tokenSeq(2).getType == TokenTypes.LITERAL_STRING_DOUBLE_QUOTE)
    assert(tokenSeq(2).getLexeme == "\"456\"")
    assert(tokenSeq(2).getNextToken == null)
  } }

  test("NetLogoTokenMaker: open literal string") { new Helper {
    text = "\""
    assert(tokenSeq.length == 1)
    assert(tokenSeq(0).getLexeme == "\"")
  } }

  test("NetLogoTokenMaker: non-zero offset") { new Helper {
    override val offset = 5
    text = "abc"
    assert(tokenSeq(0).getOffset == 5)
    assert(tokenSeq(0).getEndOffset == 8)
    assert(tokenSeq(0).containsPosition(7))
  } }

  test("NetLogoTokenMaker: non-zero offset bracket") { new Helper {
    override val offset = 5
    text = "]"
    val t = tokens
    assert(t.getOffset == 5)
    assert(t.getEndOffset == 6)
    assert(t.containsPosition(5))
  } }

  test("NetLogoTokenMaker: ident and number separated by whitespace") { new Helper {
    text = "abc 123"
    assert(tokenSeq.length == 3)
    assert(tokenSeq(0).getType == TokenTypes.IDENTIFIER)
    assert(tokenSeq(2).getType == TokenTypes.LITERAL_NUMBER_FLOAT)
    assert(tokenSeq(2).getLexeme == "123")
    assert(tokenSeq(2).getNextToken == null)
  } }

  test("breed tokenizes as keyword if it's the first word on a line, as a reporter otherwise") { new Helper {
    text = "breed breed"
    assert(tokenSeq(0).getType == TokenTypes.RESERVED_WORD)
    assert(tokenSeq(2).getType == TokenTypes.FUNCTION)
  } }


  def testTokenType(tokenText: String, expectedType: Int): Unit = {
    test(s"token type of $tokenText") { new Helper {
      text = tokenText
      assert(tokens.getType === expectedType)
    } }
  }

  testTokenType("to", TokenTypes.RESERVED_WORD)
  testTokenType("false", TokenTypes.LITERAL_BOOLEAN)
  testTokenType("true", TokenTypes.LITERAL_BOOLEAN)
  testTokenType("ask", TokenTypes.OPERATOR)
  testTokenType("fput", TokenTypes.FUNCTION)
  testTokenType("xcor", TokenTypes.FUNCTION)
  testTokenType("nobody", TokenTypes.LITERAL_BACKQUOTE)
  testTokenType("array:set", TokenTypes.OPERATOR)
  testTokenType("array:item", TokenTypes.FUNCTION)
}
