// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.{ api, nvm, prim },
  api.{ AgentSet, Breed, Program, Token, TokenType },
  TokenType.{ Command, Reporter },
  prim._

object BreedIdentifierHandler {

  def process(token: Token, program: Program): Option[Token] =
    handlers.toStream.flatMap(_.process(token, program))
      .headOption

  // a bit nasty, but will go away when we cut the dependency on nvm package - ST 5/3/13
  import scala.language.existentials
  type PrimClass = Class[_ <: nvm.Instruction]

  case class Spec(
    patternString: String,
    tokenType: TokenType,
    singular: Boolean,
    primClass: PrimClass)

  def turtle(spec: Spec) =
    new Helper(spec, _.breeds, _ => true)

  def directedLink(spec: Spec) =
    new Helper(spec, _.linkBreeds, _.isDirected)

  def undirectedLink(spec: Spec) =
    new Helper(spec, _.linkBreeds, !_.isDirected)

  val handlers = List(
    // prims for turtle breeds
    turtle(Spec("CREATE-ORDERED-*" , Command , false, classOf[_createorderedturtles ])),
    turtle(Spec("CREATE-*"         , Command , false, classOf[_createturtles        ])),
    turtle(Spec("HATCH-*"          , Command , false, classOf[_hatch                ])),
    turtle(Spec("SPROUT-*"         , Command , false, classOf[_sprout               ])),
    turtle(Spec("*"                , Reporter, false, classOf[_breed                ])),
    turtle(Spec("*-AT"             , Reporter, false, classOf[_breedat              ])),
    turtle(Spec("*-HERE"           , Reporter, false, classOf[_breedhere            ])),
    turtle(Spec("*-ON"             , Reporter, false, classOf[_breedon              ])),
    turtle(Spec("*"                , Reporter, true , classOf[_breedsingular        ])),
    turtle(Spec("IS-*?"            , Reporter, true , classOf[_isbreed              ])),
    // prims for link breeds
    directedLink   ( Spec("CREATE-*-FROM"   , Command , true , classOf[_createlinkfrom    ])),
    directedLink   ( Spec("CREATE-*-FROM"   , Command , false, classOf[_createlinksfrom   ])),
    directedLink   ( Spec("CREATE-*-TO"     , Command , false, classOf[_createlinksto     ])),
    undirectedLink ( Spec("CREATE-*-WITH"   , Command , false, classOf[_createlinkswith   ])),
    directedLink   ( Spec("CREATE-*-TO"     , Command , true , classOf[_createlinkto      ])),
    undirectedLink ( Spec("CREATE-*-WITH"   , Command , true , classOf[_createlinkwith    ])),
    directedLink   ( Spec("IN-*-FROM"       , Reporter, true , classOf[_inlinkfrom        ])),
    directedLink   ( Spec("IN-*-NEIGHBOR?"  , Reporter, true , classOf[_inlinkneighbor    ])),
    directedLink   ( Spec("IN-*-NEIGHBORS"  , Reporter, true , classOf[_inlinkneighbors   ])),
    directedLink   ( Spec("IS-*?"           , Reporter, true , classOf[_isbreed           ])),
    undirectedLink ( Spec("IS-*?"           , Reporter, true , classOf[_isbreed           ])),
    directedLink   ( Spec("*"               , Reporter, false, classOf[_linkbreed         ])),
    undirectedLink ( Spec("*"               , Reporter, false, classOf[_linkbreed         ])),
    directedLink   ( Spec("*"               , Reporter, true , classOf[_linkbreedsingular ])),
    undirectedLink ( Spec("*"               , Reporter, true , classOf[_linkbreedsingular ])),
    undirectedLink ( Spec("*-NEIGHBOR?"     , Reporter, true , classOf[_linkneighbor      ])),
    undirectedLink ( Spec("*-NEIGHBORS"     , Reporter, true , classOf[_linkneighbors     ])),
    undirectedLink ( Spec("*-WITH"          , Reporter, true , classOf[_linkwith          ])),
    directedLink   ( Spec("MY-IN-*"         , Reporter, false, classOf[_myinlinks         ])),
    undirectedLink ( Spec("MY-*"            , Reporter, false, classOf[_mylinks           ])),
    directedLink   ( Spec("MY-OUT-*"        , Reporter, false, classOf[_myoutlinks        ])),
    directedLink   ( Spec("OUT-*-NEIGHBOR?" , Reporter, true , classOf[_outlinkneighbor   ])),
    directedLink   ( Spec("OUT-*-NEIGHBORS" , Reporter, true , classOf[_outlinkneighbors  ])),
    directedLink   ( Spec("OUT-*-TO"        , Reporter, true , classOf[_outlinkto         ]))
  )

  import java.util.regex.Pattern

  class Helper(spec: Spec, breeds: Program => collection.Map[String, Breed],
    isValidBreed: Breed => Boolean) {
    val pattern = Pattern.compile("\\A" +
      spec.patternString.replaceAll("\\?", "\\\\?")
      .replaceAll("\\*", "(.+)")+"\\Z")
    def process(tok: Token, program: Program): Option[Token] = {
      val matcher = pattern.matcher(tok.value.asInstanceOf[String])
      if(!matcher.matches())
        None
      else {
        val name = matcher.group(1)
        val breed =
          breeds(program).values
            .find{breed => name ==
              (if (spec.singular) breed.singular else breed.name)}
            .getOrElse(return None)
        if (!isValidBreed(breed)) return None
        val instr = Instantiator.newInstance[nvm.Instruction](
          spec.primClass, breed.name)
        val tok2 = new Token(tok.value.asInstanceOf[String], spec.tokenType, instr)(
          tok.startPos, tok.endPos, tok.fileName)
        instr.token(tok2)
        Some(tok2)
      }
    }
  }

}
