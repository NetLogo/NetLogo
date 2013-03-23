// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.nlogo.compiler.Fail.{ cAssert, exception }
import org.nlogo.agent.{ AgentSet, AgentSetBuilder, Link, Observer, Patch, Turtle, World }
import org.nlogo.nvm.Reporter
import org.nlogo.prim._
import org.nlogo.api
import api.{ ExtensionManager, LogoList, Nobody, Token, TokenType }

/**
 * The literal parser.
 * This class contains methods which are used to parse literal NetLogo values
 * from a Iterator[Token]. It implements all the complicated stuff surrounding
 * literal agents and literal agentsets, when necessary.
 */
private object LiteralParser {
  def makeLiteralReporter(value: AnyRef): Reporter =
    value match {
      case b: java.lang.Boolean => new _constboolean(b)
      case d: java.lang.Double => new _constdouble(d)
      case l: LogoList => new _constlist(l)
      case s: String => new _conststring(s)
      case Nobody => new _nobody
      case _ => throw new IllegalArgumentException(value.getClass.getName)
    }
}

private class LiteralParser(world: World = null, extensionManager: ExtensionManager = null) {

  /// all error messages used in this class
  private val BAD_AGENT = "Not an agent"
  private val BAD_PATCH_SET_ARGS = "Patch agentsets are given by a set of 2 element integer lists containing a patch's pxcor and pycor"
  private val BAD_PATCH_ARGS = "pxcor and pycor must be floats"
  private val BAD_LINK_ARGS = "end1 and end2 must be floats"
  private val BAD_TURTLE_SET_ARGS = "Turtle agentsets are given by a set of turtles' who numbers"
  private val BAD_LINK_SET_ARGS = "Link agentsets are given by a set of links' endpoints and breeds"
  private val BAD_TURTLE_ARG = "a turtle's who number must be an integer"
  private val EXPECTED_BREED = "Expected breed"
  private val EXPECTED_CLOSE_BRACE = "Expected closing brace."
  private val EXPECTED_CLOSE_PAREN = "Expected a closing parenthesis."
  private val EXPECTED_LITERAL = "Expected a literal value."
  private val EXPECTED_NUMBER = "Expected a number."
  private val EXPECTED_INT_ETC = "Expected number, list, string or boolean"
  private val EXPECTED_OPEN_BRACE = "Expected open brace."
  private val EXPECTED_OPEN_BRACKET = "Internal error: Expected an opening bracket here."
  private val EXTRA_STUFF_AFTER_LITERAL = "Extra characters after literal."
  private val EXTRA_STUFF_AFTER_NUMBER = "Extra characters after number."
  private val ILLEGAL_AGENT_LITERAL = "Can only have literal agents and agentsets if importing."
  private val MISSING_CLOSE_BRACKET = "No closing bracket for this open bracket."
  private val NOT_AN_AGENTSET = " is not an agentset"
  private val NOT_A_BREED = " is not a breed"
  /// magic keys used to identify agent set types.
  private val SET_TYPE_ALLPATCHES = "ALL-PATCHES"
  private val SET_TYPE_ALLTURTLES = "ALL-TURTLES"
  private val SET_TYPE_ALLLINKS   = "ALL-LINKS"
  private val SET_TYPE_BREED      = "BREED"
  private val SET_TYPE_OBSERVER   = "OBSERVER"
  // First group: extension name; second group: extension type name; last group: all the data
  private val EXTENSION_TYPE_PATTERN = java.util.regex.Pattern.compile("\\{\\{(\\S*):(\\S*)\\s(.*)\\}\\}");

  /**
  * reads a literal value from a token vector. The entire vector must denote a single literal
  * value; extra garbage at the end is illegal. Used for read-from-string and other things.
  * @param tokens the input tokens
  * @param world  the current world. It's OK for this to be null, and if it
  *               is, literal agents and agentsets will cause an error.
  */
  def getLiteralValue(tokens: Iterator[Token]): AnyRef = {
    val result = readLiteralPrefix(tokens.next(), tokens)
    // make sure there's no extra stuff at the end...
    val extra = tokens.next()
    cAssert(extra.tpe == TokenType.EOF, EXTRA_STUFF_AFTER_LITERAL, extra)
    result
  }

  // Reads in a literal from a File.
  def getLiteralFromFile(tokens: Iterator[Token]): AnyRef =
    readLiteralPrefix(tokens.next(), tokens)

  def getNumberValue(tokens: Iterator[Token]) = {
    val token = tokens.next()
    if(token.tpe != TokenType.CONSTANT || !token.value.isInstanceOf[java.lang.Double])
      exception(EXPECTED_NUMBER, token)
    val extra = tokens.next()
    cAssert(extra.tpe == TokenType.EOF, EXTRA_STUFF_AFTER_NUMBER, extra)
    token.value.asInstanceOf[java.lang.Double]
  }

  /**
  * reads a literal value from the beginning of a token vector. This
  * method leaves the rest of the token vector intact (i.e., extra garbage
  * after the literal is OK).
  *
  * @param tokens the input tokens
  * @param world  the current world. It's OK for this to be null, and if it
  *               is, literal agents and agentsets will cause an error.
  */
  private def readLiteralPrefix(token: Token, tokens: Iterator[Token]): AnyRef = {
    token.tpe match {
      case TokenType.LITERAL =>
        parseSimpleLiteral(token)
      case TokenType.CONSTANT =>
        token.value
      case TokenType.OPEN_BRACKET =>
        parseLiteralList(token, tokens)
      case TokenType.OPEN_BRACE =>
        parseLiteralAgentOrAgentSet(token, tokens)
      case TokenType.OPEN_PAREN =>
        val result = readLiteralPrefix(tokens.next(), tokens)
        // if next is anything else other than ), we complain and point to the next token
        // itself. since we don't do syntax highlighting, it doesn't matter so much what the token
        // is, and we use a message which doesn't rely on that context.
        val closeParen = tokens.next()
        cAssert(closeParen.tpe == TokenType.CLOSE_PAREN, EXPECTED_CLOSE_PAREN, closeParen)
        result
      case TokenType.COMMENT =>
        // just skip comments when reading a literal - ev 7/10/07
        readLiteralPrefix(tokens.next(), tokens)
      case _ =>
        exception(EXPECTED_LITERAL, token)
    }
  }

  /**
  * parses a literal list. Assumes the open bracket was already eaten.  Eats the list
  * contents and the close bracket.
  */
  def parseLiteralList(openBracket: Token, tokens: Iterator[Token]) = {
    var list = LogoList()
    var done = false
    while(!done) {
      val token = tokens.next()
      token.tpe match {
        case TokenType.CLOSE_BRACKET => done = true
        case TokenType.EOF => exception(MISSING_CLOSE_BRACKET, openBracket)
        case _ => list = list.lput(readLiteralPrefix(token, tokens))
      }
    }
    list
  }

  private def parseSimpleLiteral(token: Token): AnyRef = {
    // we shouldn't get here if we aren't importing, but check just in case
    cAssert(world != null, ILLEGAL_AGENT_LITERAL, token)
    val matcher = EXTENSION_TYPE_PATTERN.matcher(token.value.asInstanceOf[String])
    if(matcher.matches)
      extensionManager.readExtensionObject(matcher.group(1), matcher.group(2), matcher.group(3))
    // if we can't deconstruct it, then return the whole LITERAL
    else token.value
  }

  /**
  * parses a literal agent (e.g. "{turtle 3}" or "{patch 1 2}" or "{link 5 6}"
  */
  private def parseLiteralAgent(token: Token, tokens: Iterator[Token]) = {
    // we shouldn't get here if we aren't importing, but check just in case
    cAssert(world != null, ILLEGAL_AGENT_LITERAL, token)
    val agentKind = token.value
    if(agentKind.isInstanceOf[org.nlogo.prim._patch]) {
      val pxcor = parsePcor(tokens)
      val pycor = parsePcor(tokens)
      try { world.getPatchAt(pxcor, pycor) }
      catch { case _: org.nlogo.api.AgentException =>
                exception("Invalid patch coordinates ( " + pxcor + " , " + pycor + " ) ", token) }
    }
    else if(agentKind.isInstanceOf[org.nlogo.prim._turtle]) {
      val token = tokens.next()
      if(token.tpe != TokenType.CONSTANT || !token.value.isInstanceOf[java.lang.Double])
        exception(BAD_TURTLE_ARG, token)
      world.getOrCreateTurtle(token.value.asInstanceOf[java.lang.Double].longValue)
    }
    else if(agentKind.isInstanceOf[org.nlogo.prim._link]) {
      world.getOrCreateLink(
        parseEnd(tokens),
        parseEnd(tokens),
        world.links)
    }
    else exception(BAD_AGENT, token)
  }

  /**
   * parses a double. This is a helper method for parseLiteralAgent().
   */
  private def parsePcor(tokens: Iterator[Token]): Double = {
    val token = tokens.next()
    cAssert(token.tpe == TokenType.CONSTANT && token.value.isInstanceOf[java.lang.Double],
            BAD_PATCH_ARGS, token)
    token.value.asInstanceOf[Double].doubleValue
  }

  private def parseEnd(tokens: Iterator[Token]): java.lang.Double = {
    val token = tokens.next()
    cAssert(token.tpe == TokenType.CONSTANT && token.value.isInstanceOf[java.lang.Double],
            BAD_LINK_ARGS, token)
    token.value.asInstanceOf[java.lang.Double]
  }

  /**
   * parses a literal agent or agentset. It recognizes a number of forms:
   *   {turtle 4} {patch 0 1} {breed-singular 2} {all-turtles} {all-patches} {observer}
   *   {breed some-breed} {turtles 1 2 3 4 5} {patches [1 2] [3 4] [5 6]} {links [0 1] [1 2]}
   * To parse the turtle and patch forms, it uses parseLiteralAgent().
   */
  private def parseLiteralAgentOrAgentSet(braceToken: Token, tokens: Iterator[Token]): AnyRef = {  // returns Agent or AgentSet
    // we shouldn't get here if we aren't importing, but check just in case
    cAssert(world != null, ILLEGAL_AGENT_LITERAL, braceToken)
    val token = tokens.next()
    // next token should be an identifier or reporter. reporter is a special case because "turtles"
    // and "patches" end up getting turned into Reporters when tokenizing, which is kind of ugly.
    cAssert(List(TokenType.VARIABLE, TokenType.IDENT, TokenType.REPORTER).contains(token.tpe),
            EXPECTED_BREED, token)
    if(token.tpe == TokenType.VARIABLE || token.tpe == TokenType.IDENT) {
      val agentsetTypeString = token.value.asInstanceOf[String]
      if(agentsetTypeString.equalsIgnoreCase(SET_TYPE_BREED)) {
        // we have a breed agentset
        val breedToken = tokens.next()
        cAssert(breedToken.tpe == TokenType.IDENT, EXPECTED_BREED, breedToken)
        val closeBrace = tokens.next()
        cAssert(closeBrace.tpe == TokenType.CLOSE_BRACE, EXPECTED_CLOSE_BRACE, closeBrace)
        // this is safe since it must be an IDENT.
        val breedString = breedToken.value.asInstanceOf[String]
        val breed = {
          val b = world.getBreed(breedString)
          if(b != null) b
          else world.getLinkBreed(breedString)
        }
        cAssert(breed != null, breedString + NOT_A_BREED, token)
        breed
      }
      else if(List(SET_TYPE_ALLTURTLES, SET_TYPE_ALLPATCHES, SET_TYPE_ALLLINKS)
              .contains(agentsetTypeString.toUpperCase)) {
        // we have the turtles or patches agentset. make sure that's
        // all we have...
        val closeBrace = tokens.next()
        cAssert(closeBrace.tpe == TokenType.CLOSE_BRACE, EXPECTED_CLOSE_BRACE, closeBrace)
        agentsetTypeString.toUpperCase match {
          case SET_TYPE_ALLTURTLES => world.turtles
          case SET_TYPE_ALLLINKS => world.links
          case SET_TYPE_ALLPATCHES => world.patches
        }
      }
      else if(agentsetTypeString.equalsIgnoreCase(SET_TYPE_OBSERVER)) {
        // we have the observer agentset. make sure that's all we have...
        val closeBrace = tokens.next()
        cAssert(closeBrace.tpe == TokenType.CLOSE_BRACE, EXPECTED_CLOSE_BRACE, closeBrace)
        AgentSet.fromAgent(world.observer)
      }
      else if(world.program.breeds.values.exists(_.singular == agentsetTypeString.toUpperCase)) {
        val token = tokens.next()
        if(token.tpe != TokenType.CONSTANT || !token.value.isInstanceOf[java.lang.Double])
          exception(BAD_TURTLE_ARG, token)
        val closeBrace = tokens.next()
        cAssert(closeBrace.tpe == TokenType.CLOSE_BRACE, EXPECTED_CLOSE_BRACE, closeBrace)
        world.getOrCreateTurtle(token.value.asInstanceOf[java.lang.Double].intValue)
      }
      else if(world.program.linkBreeds.values.exists(_.singular == agentsetTypeString.toUpperCase)) {
        val end1 = parseEnd(tokens)
        val end2 = parseEnd(tokens)
        val closeBrace = tokens.next()
        cAssert(closeBrace.tpe == TokenType.CLOSE_BRACE, EXPECTED_CLOSE_BRACE, closeBrace)
        world.getOrCreateLink(
          end1, end2,
          world.getLinkBreed(
            world.program.linkBreeds.values.find(
              _.singular == agentsetTypeString.toUpperCase).get.name))
      }
      else exception(agentsetTypeString + NOT_AN_AGENTSET, token)
    }
    else if(token.value.isInstanceOf[_turtles]) {
      // we have an agentset of turtles. parse arguments...
      val builder = new AgentSetBuilder(api.AgentKind.Turtle)
      var token = tokens.next()
      while(token.tpe != TokenType.CLOSE_BRACE) {
        val value = readLiteralPrefix(token, tokens)
        cAssert(value.isInstanceOf[java.lang.Double], BAD_TURTLE_SET_ARGS, token)
        builder.add(world.getOrCreateTurtle(value.asInstanceOf[java.lang.Double].intValue))
        token = tokens.next()
      }
      builder.build()
    }
    else if(token.value.isInstanceOf[_links]) {
      // we have an agentset of links. parse arguments...
      val builder = new AgentSetBuilder(api.AgentKind.Link)
      var token = tokens.next()
      while(token.tpe != TokenType.CLOSE_BRACE) {
        cAssert(token.tpe == TokenType.OPEN_BRACKET, BAD_LINK_SET_ARGS, token)
        val listVal = readLiteralPrefix(token, tokens).asInstanceOf[LogoList]
        cAssert(listVal.size == 3 &&
                listVal.get(0).isInstanceOf[java.lang.Double] &&
                listVal.get(1).isInstanceOf[java.lang.Double] &&
                listVal.get(2).isInstanceOf[AgentSet],
                BAD_LINK_SET_ARGS, token)
        val link = world.getOrCreateLink(listVal.get(0).asInstanceOf[java.lang.Double],
                                         listVal.get(1).asInstanceOf[java.lang.Double],
                                         listVal.get(2).asInstanceOf[AgentSet])
        if(link != null) builder.add(link)
        token = tokens.next()
      }
      builder.build()
    }
    else if(token.value.isInstanceOf[_patches]) {
      // we have an agentset of patches. parse arguments...
      val builder = new AgentSetBuilder(api.AgentKind.Patch)
      var token = tokens.next()
      while(token.tpe != TokenType.CLOSE_BRACE) {
        cAssert(token.tpe == TokenType.OPEN_BRACKET, BAD_PATCH_SET_ARGS, token)
        val listVal = readLiteralPrefix(token, tokens).asInstanceOf[LogoList]
        cAssert(listVal.size == 2 && listVal.scalaIterator.forall(_.isInstanceOf[java.lang.Double]),
                BAD_PATCH_SET_ARGS, token)
        try
          builder.add(
            world.getPatchAt(listVal.get(0).asInstanceOf[java.lang.Double].intValue,
              listVal.get(1).asInstanceOf[java.lang.Double].intValue))
        catch {
          case _: org.nlogo.api.AgentException =>
            exception("Invalid patch coordinates in one of the agents of this set.", token)
        }
        token = tokens.next()
      }
      builder.build()
    }
    else if(List(classOf[_turtle], classOf[_patch], classOf[_link])
            .contains(token.value.getClass)) {
      // we have a single agent, turtle patch or link
      val result = parseLiteralAgent(token, tokens)
      val closeBrace = tokens.next()
      cAssert(closeBrace.tpe == TokenType.CLOSE_BRACE, EXPECTED_CLOSE_BRACE, closeBrace)
      result
    }
    else exception(token.name + NOT_AN_AGENTSET, token)
  }

}
