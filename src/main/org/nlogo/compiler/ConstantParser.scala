// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler
import org.nlogo.compiler.CompilerExceptionThrowers.{ cAssert, exception }
import org.nlogo.agent.{ AgentSet, ArrayAgentSet, Link, Observer, Patch, Turtle, World }
import org.nlogo.api.{ ExtensionManager, LogoList, Nobody, Token, TokenType}
import org.nlogo.nvm.Reporter
import org.nlogo.prim._

/**
 * The constant parser.
 * This class contains methods which are used to parse constant NetLogo values
 * from a Iterator[Token]. It implements all the complicated stuff surrounding
 * constant agents and constant agentsets, when necessary.
 */
private object ConstantParser {
  def makeConstantReporter(value: AnyRef): Reporter =
    value match {
      case b: java.lang.Boolean => new _constboolean(b)
      case d: java.lang.Double => new _constdouble(d)
      case l: LogoList => new _constlist(l)
      case s: String => new _conststring(s)
      case Nobody => new _nobody
      case _ => throw new IllegalArgumentException(value.getClass.getName)
    }
}

private class ConstantParser(world: World = null, extensionManager: ExtensionManager = null) {

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
  private val EXPECTED_CONSTANT = "Expected a constant."
  private val EXPECTED_NUMBER = "Expected a number."
  private val EXPECTED_INT_ETC = "Expected number, list, string or boolean"
  private val EXPECTED_OPEN_BRACE = "Expected open brace."
  private val EXPECTED_OPEN_BRACKET = "Internal error: Expected an opening bracket here."
  private val EXTRA_STUFF_AFTER_CONSTANT = "Extra characters after constant."
  private val EXTRA_STUFF_AFTER_NUMBER = "Extra characters after number."
  private val ILLEGAL_AGENT_CONSTANT = "Can only have constant agents and agentsets if importing."
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
  * reads a constant value from a token vector. The entire vector must denote a single constant
  * value; extra garbage at the end is illegal. Used for read-from-string and other things.
  * @param tokens the input tokens
  * @param world  the current world. It's OK for this to be null, and if it
  *               is, constant agents and agentsets will cause an error.
  */
  def getConstantValue(tokens: Iterator[Token]): AnyRef = {
    val result = readConstantPrefix(tokens.next(), tokens)
    // make sure there's no extra stuff at the end...
    val extra = tokens.next()
    cAssert(extra.tyype == TokenType.EOF, EXTRA_STUFF_AFTER_CONSTANT, extra)
    result
  }

  // Reads in a constant from a File.
  def getConstantFromFile(tokens: Iterator[Token]): AnyRef =
    readConstantPrefix(tokens.next(), tokens)

  def getNumberValue(tokens: Iterator[Token]) = {
    val token = tokens.next()
    if(token.tyype != TokenType.CONSTANT || !token.value.isInstanceOf[java.lang.Double])
      exception(EXPECTED_NUMBER, token)
    val extra = tokens.next()
    cAssert(extra.tyype == TokenType.EOF, EXTRA_STUFF_AFTER_NUMBER, extra)
    token.value.asInstanceOf[java.lang.Double]
  }

  /**
  * reads a constant value from the beginning of a token vector. This
  * method leaves the rest of the token vector intact (i.e., extra garbage
  * after the constant is OK).
  *
  * @param tokens the input tokens
  * @param world  the current world. It's OK for this to be null, and if it
  *               is, constant agents and agentsets will cause an error.
  */
  private def readConstantPrefix(token: Token, tokens: Iterator[Token]): AnyRef = {
    token.tyype match {
      case TokenType.LITERAL =>
        parseConstantLiteral(token)
      case TokenType.CONSTANT =>
        token.value
      case TokenType.OPEN_BRACKET =>
        parseConstantList(token, tokens)
      case TokenType.OPEN_BRACE =>
        parseConstantAgentOrAgentSet(token, tokens)
      case TokenType.OPEN_PAREN =>
        val result = readConstantPrefix(tokens.next(), tokens)
        // if next is anything else other than ), we complain and point to the next token
        // itself. since we don't do syntax highlighting, it doesn't matter so much what the token
        // is, and we use a message which doesn't rely on that context.
        val closeParen = tokens.next()
        cAssert(closeParen.tyype == TokenType.CLOSE_PAREN, EXPECTED_CLOSE_PAREN, closeParen)
        result
      case TokenType.COMMENT =>
        // just skip comments when reading a constant - ev 7/10/07
        readConstantPrefix(tokens.next(), tokens)
      case _ =>
        exception(EXPECTED_CONSTANT, token)
    }
  }

  /**
  * parses a constant list. Assumes the open bracket was already eaten.  Eats the list
  * contents and the close bracket.
  */
  def parseConstantList(openBracket: Token, tokens: Iterator[Token]) = {
    var list = LogoList()
    var done = false
    while(!done) {
      val token = tokens.next()
      token.tyype match {
        case TokenType.CLOSE_BRACKET => done = true
        case TokenType.EOF => exception(MISSING_CLOSE_BRACKET, openBracket)
        case _ => list = list.lput(readConstantPrefix(token, tokens))
      }
    }
    list
  }

  private def parseConstantLiteral(token: Token): AnyRef = {
    // we shouldn't get here if we aren't importing, but check just in case
    cAssert(world != null, ILLEGAL_AGENT_CONSTANT, token)
    val matcher = EXTENSION_TYPE_PATTERN.matcher(token.value.asInstanceOf[String])
    if(matcher.matches)
      extensionManager.readExtensionObject(matcher.group(1), matcher.group(2), matcher.group(3))
    // if we can't deconstruct it, then return the whole LITERAL
    else token.value
  }

  /**
  * parses a constant agent (e.g. "{turtle 3}" or "{patch 1 2}" or "{link 5 6}"
  */
  private def parseConstantAgent(token: Token, tokens: Iterator[Token]) = {
    // we shouldn't get here if we aren't importing, but check just in case
    cAssert(world != null, ILLEGAL_AGENT_CONSTANT, token)
    val agentType = token.value
    if(agentType.isInstanceOf[org.nlogo.prim._patch]) {
      val pxcor = parsePcor(tokens)
      val pycor = parsePcor(tokens)
      try { world.getPatchAt(pxcor, pycor) }
      catch { case _: org.nlogo.api.AgentException =>
                exception("Invalid patch coordinates ( " + pxcor + " , " + pycor + " ) ", token) }
    }
    else if(agentType.isInstanceOf[org.nlogo.prim._turtle]) {
      val token = tokens.next()
      if(token.tyype != TokenType.CONSTANT || !token.value.isInstanceOf[java.lang.Double])
        exception(BAD_TURTLE_ARG, token)
      world.getOrCreateTurtle(token.value.asInstanceOf[java.lang.Double].longValue)
    }
    else if(agentType.isInstanceOf[org.nlogo.prim._link]) {
      world.getOrCreateLink(
        parseEnd(tokens),
        parseEnd(tokens),
        world.links)
    }
    else exception(BAD_AGENT, token)
  }

  /**
   * parses a double. This is a helper method for parseConstantAgent().
   */
  private def parsePcor(tokens: Iterator[Token]): Double = {
    val token = tokens.next()
    cAssert(token.tyype == TokenType.CONSTANT && token.value.isInstanceOf[java.lang.Double],
            BAD_PATCH_ARGS, token)
    token.value.asInstanceOf[Double].doubleValue
  }

  private def parseEnd(tokens: Iterator[Token]): java.lang.Double = {
    val token = tokens.next()
    cAssert(token.tyype == TokenType.CONSTANT && token.value.isInstanceOf[java.lang.Double],
            BAD_LINK_ARGS, token)
    token.value.asInstanceOf[java.lang.Double]
  }

  /**
   * parses a constant agent or agentset. It recognizes a number of forms:
   *   {turtle 4} {patch 0 1} {breed-singular 2} {all-turtles} {all-patches} {observer}
   *   {breed some-breed} {turtles 1 2 3 4 5} {patches [1 2] [3 4] [5 6]} {links [0 1] [1 2]}
   * To parse the turtle and patch forms, it uses parseConstantAgent().
   */
  private def parseConstantAgentOrAgentSet(braceToken: Token, tokens: Iterator[Token]): AnyRef = {  // returns Agent or AgentSet
    // we shouldn't get here if we aren't importing, but check just in case
    cAssert(world != null, ILLEGAL_AGENT_CONSTANT, braceToken)
    val token = tokens.next()
    // next token should be an identifier or reporter. reporter is a special case because "turtles"
    // and "patches" end up getting turned into Reporters when tokenizing, which is kind of ugly.
    cAssert(List(TokenType.VARIABLE, TokenType.IDENT, TokenType.REPORTER).contains(token.tyype),
            EXPECTED_BREED, token)
    if(token.tyype == TokenType.VARIABLE || token.tyype == TokenType.IDENT) {
      val agentsetTypeString = token.value.asInstanceOf[String]
      if(agentsetTypeString.equalsIgnoreCase(SET_TYPE_BREED)) {
        // we have a breed agentset
        val breedToken = tokens.next()
        cAssert(breedToken.tyype == TokenType.IDENT, EXPECTED_BREED, breedToken)
        val closeBrace = tokens.next()
        cAssert(closeBrace.tyype == TokenType.CLOSE_BRACE, EXPECTED_CLOSE_BRACE, closeBrace)
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
        cAssert(closeBrace.tyype == TokenType.CLOSE_BRACE, EXPECTED_CLOSE_BRACE, closeBrace)
        agentsetTypeString.toUpperCase match {
          case SET_TYPE_ALLTURTLES => world.turtles
          case SET_TYPE_ALLLINKS => world.links
          case SET_TYPE_ALLPATCHES => world.patches
        }
      }
      else if(agentsetTypeString.equalsIgnoreCase(SET_TYPE_OBSERVER)) {
        // we have the observer agentset. make sure that's all we have...
        val closeBrace = tokens.next()
        cAssert(closeBrace.tyype == TokenType.CLOSE_BRACE, EXPECTED_CLOSE_BRACE, closeBrace)
        val agentset = new ArrayAgentSet(classOf[Observer], 1, false, world)
        agentset.add(world.observer)
        agentset
      }
      else if(world.program.breeds.values.exists(_.singular == agentsetTypeString.toUpperCase)) {
        val token = tokens.next()
        if(token.tyype != TokenType.CONSTANT || !token.value.isInstanceOf[java.lang.Double])
          exception(BAD_TURTLE_ARG, token)
        val closeBrace = tokens.next()
        cAssert(closeBrace.tyype == TokenType.CLOSE_BRACE, EXPECTED_CLOSE_BRACE, closeBrace)
        world.getOrCreateTurtle(token.value.asInstanceOf[java.lang.Double].intValue)
      }
      else if(world.program.linkBreeds.values.exists(_.singular == agentsetTypeString.toUpperCase)) {
        val end1 = parseEnd(tokens)
        val end2 = parseEnd(tokens)
        val closeBrace = tokens.next()
        cAssert(closeBrace.tyype == TokenType.CLOSE_BRACE, EXPECTED_CLOSE_BRACE, closeBrace)
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
      val agentset = new ArrayAgentSet(classOf[Turtle], 1, false, world)
      var token = tokens.next()
      while(token.tyype != TokenType.CLOSE_BRACE) {
        val value = readConstantPrefix(token, tokens)
        cAssert(value.isInstanceOf[java.lang.Double], BAD_TURTLE_SET_ARGS, token)
        agentset.add(world.getOrCreateTurtle(value.asInstanceOf[java.lang.Double].intValue))
        token = tokens.next()
      }
      agentset
    }
    else if(token.value.isInstanceOf[_links]) {
      // we have an agentset of links. parse arguments...
      val agentset = new ArrayAgentSet(classOf[Link], 1, false, world)
      var token = tokens.next()
      while(token.tyype != TokenType.CLOSE_BRACE) {
        cAssert(token.tyype == TokenType.OPEN_BRACKET, BAD_LINK_SET_ARGS, token)
        val listVal = readConstantPrefix(token, tokens).asInstanceOf[LogoList]
        cAssert(listVal.size == 3 &&
                listVal.get(0).isInstanceOf[java.lang.Double] &&
                listVal.get(1).isInstanceOf[java.lang.Double] &&
                listVal.get(2).isInstanceOf[AgentSet],
                BAD_LINK_SET_ARGS, token)
        val link = world.getOrCreateLink(listVal.get(0).asInstanceOf[java.lang.Double],
                                         listVal.get(1).asInstanceOf[java.lang.Double],
                                         listVal.get(2).asInstanceOf[AgentSet])
        if(link != null) agentset.add(link)
        token = tokens.next()
      }
      agentset
    }
    else if(token.value.isInstanceOf[_patches]) {
      // we have an agentset of patches. parse arguments...
      val agentset = new ArrayAgentSet(classOf[Patch], 1, false, world)
      var token = tokens.next()
      while(token.tyype != TokenType.CLOSE_BRACE) {
        cAssert(token.tyype == TokenType.OPEN_BRACKET, BAD_PATCH_SET_ARGS, token)
        val listVal = readConstantPrefix(token, tokens).asInstanceOf[LogoList]
        cAssert(listVal.size == 2 && listVal.scalaIterator.forall(_.isInstanceOf[java.lang.Double]),
                BAD_PATCH_SET_ARGS, token)
        try {
          agentset.add(world.getPatchAt(listVal.get(0).asInstanceOf[java.lang.Double].intValue,
                                        listVal.get(1).asInstanceOf[java.lang.Double].intValue))
        }
        catch {
          case _: org.nlogo.api.AgentException =>
            exception("Invalid patch coordinates in one of the agents of this set.", token)
        }
        token = tokens.next()
      }
      agentset
    }
    else if(List(classOf[_turtle], classOf[_patch], classOf[_link])
            .contains(token.value.getClass)) {
      // we have a single agent, turtle patch or link
      val result = parseConstantAgent(token, tokens)
      val closeBrace = tokens.next()
      cAssert(closeBrace.tyype == TokenType.CLOSE_BRACE, EXPECTED_CLOSE_BRACE, closeBrace)
      result
    }
    else exception(token.name + NOT_AN_AGENTSET, token)
  }

}
