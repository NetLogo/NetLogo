// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api, api.{ Token, TokenType }

// This is only used for importing worlds; the regular NetLogo language
// doesn't have literal agents and agentsets.

class LiteralAgentParser(_world: api.World,
  readLiteralPrefix: (Token, Iterator[Token]) => AnyRef,
  cAssert: (Boolean, =>String, Token) => Unit,
  exception: (String, Token) => Nothing) {

  // janky, but oh well - ST 5/2/13
  def world = _world.asInstanceOf[World]

  /// all error messages used in this class
  private val ERR_EXPECTED_BREED = "Expected breed"
  private val ERR_EXPECTED_CLOSEBRACE = "Expected closing brace."
  private val ERR_NOT_AN_AGENTSET = " is not an agentset"
  private val ERR_NOT_A_BREED = " is not a breed"
  private val ERR_BAD_AGENT = "Not an agent"
  private val ERR_BAD_PATCH_SET_ARGS = "Patch agentsets are given by a set of 2 element integer lists containing a patch's pxcor and pycor"
  private val ERR_BAD_PATCH_ARGS = "pxcor and pycor must be floats"
  private val ERR_BAD_TURTLE_ARG = "a turtle's who number must be an integer"
  private val ERR_BAD_TURTLE_SET_ARGS = "Turtle agentsets are given by a set of turtles' who numbers"
  private val ERR_BAD_LINK_ARGS = "end1 and end2 must be floats"
  private val ERR_BAD_LINK_SET_ARGS = "Link agentsets are given by a set of links' endpoints and breeds"
  private val ERR_ILLEGAL_AGENT_LITERAL = "Can only have literal agents and agentsets if importing."

  /// magic keys used to identify agent set types.
  private val SET_TYPE_ALLPATCHES = "ALL-PATCHES"
  private val SET_TYPE_ALLTURTLES = "ALL-TURTLES"
  private val SET_TYPE_ALLLINKS   = "ALL-LINKS"
  private val SET_TYPE_BREED      = "BREED"
  private val SET_TYPE_OBSERVER   = "OBSERVER"

  /**
   * parses a literal agent or agentset. It recognizes a number of forms:
   *   {turtle 4} {patch 0 1} {breed-singular 2} {all-turtles} {all-patches} {observer}
   *   {breed some-breed} {turtles 1 2 3 4 5} {patches [1 2] [3 4] [5 6]} {links [0 1] [1 2]}
   * To parse the turtle and patch forms, it uses parseLiteralAgent().
   */
  def parseLiteralAgentOrAgentSet(tokens: Iterator[Token]): AnyRef = {  // returns Agent or AgentSet
    val token = tokens.next()
    // next token should be an identifier or reporter. reporter is a special case because "turtles"
    // and "patches" end up getting turned into Reporters when tokenizing, which is kind of ugly.
    cAssert(List(TokenType.Ident, TokenType.Reporter).contains(token.tpe),
            ERR_EXPECTED_BREED, token)
    val agentsetTypeString = token.value.asInstanceOf[String]
    if(agentsetTypeString.equalsIgnoreCase(SET_TYPE_BREED)) {
      // we have a breed agentset
      val breedToken = tokens.next()
      cAssert(breedToken.tpe == TokenType.Ident, ERR_EXPECTED_BREED, breedToken)
      val closeBrace = tokens.next()
      cAssert(closeBrace.tpe == TokenType.CloseBrace, ERR_EXPECTED_CLOSEBRACE, closeBrace)
      // this is safe since it must be an IDENT.
      val breedString = breedToken.value.asInstanceOf[String]
      val breed = {
        val b = world.getBreed(breedString)
        if(b != null) b
        else world.getLinkBreed(breedString)
      }
      cAssert(breed != null, breedString + ERR_NOT_A_BREED, token)
      breed
    }
    else if(List(SET_TYPE_ALLTURTLES, SET_TYPE_ALLPATCHES, SET_TYPE_ALLLINKS)
            .contains(agentsetTypeString.toUpperCase)) {
      // we have the turtles or patches agentset. make sure that's
      // all we have...
      val closeBrace = tokens.next()
      cAssert(closeBrace.tpe == TokenType.CloseBrace, ERR_EXPECTED_CLOSEBRACE, closeBrace)
      agentsetTypeString.toUpperCase match {
        case SET_TYPE_ALLTURTLES => world.turtles
        case SET_TYPE_ALLLINKS => world.links
        case SET_TYPE_ALLPATCHES => world.patches
      }
    }
    else if(agentsetTypeString.equalsIgnoreCase(SET_TYPE_OBSERVER)) {
      // we have the observer agentset. make sure that's all we have...
      val closeBrace = tokens.next()
      cAssert(closeBrace.tpe == TokenType.CloseBrace, ERR_EXPECTED_CLOSEBRACE, closeBrace)
      AgentSet.fromAgent(world.observer)
    }
    else if(world.program.breeds.values.exists(_.singular == agentsetTypeString.toUpperCase)) {
      val token = tokens.next()
      cAssert(token.tpe == TokenType.Literal && token.value.isInstanceOf[java.lang.Double],
        ERR_BAD_TURTLE_ARG, token)
      val closeBrace = tokens.next()
      cAssert(closeBrace.tpe == TokenType.CloseBrace, ERR_EXPECTED_CLOSEBRACE, closeBrace)
      world.getOrCreateTurtle(token.value.asInstanceOf[java.lang.Double].intValue)
    }
    else if(world.program.linkBreeds.values.exists(_.singular == agentsetTypeString.toUpperCase)) {
      val end1 = parseEnd(tokens)
      val end2 = parseEnd(tokens)
      val closeBrace = tokens.next()
      cAssert(closeBrace.tpe == TokenType.CloseBrace, ERR_EXPECTED_CLOSEBRACE, closeBrace)
      world.getOrCreateLink(
        end1, end2,
        world.getLinkBreed(
          world.program.linkBreeds.values.find(
            _.singular == agentsetTypeString.toUpperCase).get.name))
    }
    else if(token.value == "TURTLES") {
      // we have an agentset of turtles. parse arguments...
      val builder = new AgentSetBuilder(api.AgentKind.Turtle)
      var token = tokens.next()
      while(token.tpe != TokenType.CloseBrace) {
        val value = readLiteralPrefix(token, tokens)
        cAssert(value.isInstanceOf[java.lang.Double], ERR_BAD_TURTLE_SET_ARGS, token)
        builder.add(world.getOrCreateTurtle(value.asInstanceOf[java.lang.Double].intValue))
        token = tokens.next()
      }
      builder.build()
    }
    else if(token.value == "LINKS") {
      // we have an agentset of links. parse arguments...
      val builder = new AgentSetBuilder(api.AgentKind.Link)
      var token = tokens.next()
      while(token.tpe != TokenType.CloseBrace) {
        cAssert(token.tpe == TokenType.OpenBracket, ERR_BAD_LINK_SET_ARGS, token)
        val listVal = readLiteralPrefix(token, tokens).asInstanceOf[api.LogoList]
        cAssert(listVal.size == 3 &&
                listVal.get(0).isInstanceOf[java.lang.Double] &&
                listVal.get(1).isInstanceOf[java.lang.Double] &&
                listVal.get(2).isInstanceOf[AgentSet],
                ERR_BAD_LINK_SET_ARGS, token)
        val link = world.getOrCreateLink(listVal.get(0).asInstanceOf[java.lang.Double],
                                         listVal.get(1).asInstanceOf[java.lang.Double],
                                         listVal.get(2).asInstanceOf[AgentSet])
        if(link != null) builder.add(link)
        token = tokens.next()
      }
      builder.build()
    }
    else if(token.value == "PATCHES") {
      // we have an agentset of patches. parse arguments...
      val builder = new AgentSetBuilder(api.AgentKind.Patch)
      var token = tokens.next()
      while(token.tpe != TokenType.CloseBrace) {
        cAssert(token.tpe == TokenType.OpenBracket, ERR_BAD_PATCH_SET_ARGS, token)
        val listVal = readLiteralPrefix(token, tokens).asInstanceOf[api.LogoList]
        cAssert(listVal.size == 2 && listVal.scalaIterator.forall(_.isInstanceOf[java.lang.Double]),
                ERR_BAD_PATCH_SET_ARGS, token)
        builder.add(
          getPatchAt(token,
            listVal.get(0).asInstanceOf[java.lang.Double].intValue,
            listVal.get(1).asInstanceOf[java.lang.Double].intValue))
        token = tokens.next()
      }
      builder.build()
    }
    else if (List("TURTLE", "PATCH", "LINK").contains(token.value)) {
      // we have a single agent
      val result = parseLiteralAgent(token, tokens)
      val closeBrace = tokens.next()
      cAssert(closeBrace.tpe == TokenType.CloseBrace, ERR_EXPECTED_CLOSEBRACE, closeBrace)
      result
    }
    else exception(agentsetTypeString + ERR_NOT_AN_AGENTSET, token)
  }

  /**
  * parses a literal agent (e.g. "{turtle 3}" or "{patch 1 2}" or "{link 5 6}"
  */
  private def parseLiteralAgent(token: Token, tokens: Iterator[Token]) = {
    // we shouldn't get here if we aren't importing, but check just in case
    cAssert(world != null, ERR_ILLEGAL_AGENT_LITERAL, token)
    token.value match {
      case "PATCH" =>
        getPatchAt(token,
          parsePcor(tokens), parsePcor(tokens))
      case "TURTLE" =>
        val token = tokens.next()
        cAssert(token.tpe == TokenType.Literal && token.value.isInstanceOf[java.lang.Double],
          ERR_BAD_TURTLE_ARG, token)
        world.getOrCreateTurtle(token.value.asInstanceOf[java.lang.Double].longValue)
      case "LINK" =>
        world.getOrCreateLink(
          parseEnd(tokens),
          parseEnd(tokens),
          world.links)
      case _ =>
        exception(ERR_BAD_AGENT, token)
    }
  }

  // put the try/catch in a separate method or we run afoul of SI-6191; that bug has trouble with
  // try/catch nested inside other constructs - ST 5/2/13
  private def getPatchAt(token: Token, pxcor: Double, pycor: Double) =
    try world.getPatchAt(pxcor, pycor)
    catch { case _: org.nlogo.api.AgentException =>
        exception("Invalid patch coordinates ( " + pxcor + " , " + pycor + " ) ", token) }

  /**
   * parses a double. This is a helper method for parseLiteralAgent().
   */
  private def parsePcor(tokens: Iterator[Token]): Double = {
    val token = tokens.next()
    cAssert(token.tpe == TokenType.Literal && token.value.isInstanceOf[java.lang.Double],
            ERR_BAD_PATCH_ARGS, token)
    token.value.asInstanceOf[Double].doubleValue
  }

  private def parseEnd(tokens: Iterator[Token]): java.lang.Double = {
    val token = tokens.next()
    cAssert(token.tpe == TokenType.Literal && token.value.isInstanceOf[java.lang.Double],
            ERR_BAD_LINK_ARGS, token)
    token.value.asInstanceOf[java.lang.Double]
  }

}
