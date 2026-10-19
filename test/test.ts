import { expect } from "@jest/globals"

import { Black, Blue, Cyan, Gray, Lime, Magenta, Orange, Pink, Red, Violet, White, Yellow } from "picker/color/ColorLiteral.js"
import { GUI_HSLA, Hexadecimal, HSB, HSBA, HSL, HSLA, NLNumber, NLWord, RGB, RGBA         } from "picker/color/Representation.js"

import type { Representation } from "picker/color/Representation.js"

// Test data

const testColors =
  [
    [ Hexadecimal.parse("#b8860b")
    , new RGB(184, 134, 11)
    , new RGBA(184, 134, 11, 100)
    , new HSB(42.7, 94, 72.2)
    , new HSBA(42.7, 94, 72.2, 100)
    , new HSL(42.7, 88.7, 38.2)
    , new HSLA(42.7, 88.7, 38.2, 100)
    , new NLNumber(43.2)
    , new NLWord(Yellow, -1.8)
    ]
  , [ Hexadecimal.parse("#b8860b2b")
    , new RGB(184, 134, 11)
    , new RGBA(184, 134, 11, 17)
    , new HSB(42.7, 94, 72.2)
    , new HSBA(42.7, 94, 72.2, 17)
    , new HSL(42.7, 88.7, 38.2)
    , new HSLA(42.7, 88.7, 38.2, 17)
    , new NLNumber(43.2)
    , new NLWord(Yellow, -1.8)
    ]
  , [ Hexadecimal.parse("#ff5733")
    , new RGB(255, 87, 51)
    , new RGBA(255, 87, 51, 100)
    , new HSB(10.6, 80, 100)
    , new HSBA(10.6, 80, 100, 100)
    , new HSL(10.6, 100, 60)
    , new HSLA(10.6, 100, 60, 100)
    , new NLNumber(25)
    , new NLWord(Orange)
    ]
  , [ Hexadecimal.parse("#33ff57")
    , new RGB(51, 255, 87)
    , new RGBA(51, 255, 87, 100)
    , new HSB(130.6, 80, 100)
    , new HSBA(130.6, 80, 100, 100)
    , new HSL(130.6, 100, 60)
    , new HSLA(130.6, 100, 60, 100)
    , new NLNumber(65.6)
    , new NLWord(Lime, 0.6)
    ]
  , [ Hexadecimal.parse("#3357ff")
    , new RGB(51, 87, 255)
    , new RGBA(51, 87, 255, 100)
    , new HSB(229.4, 80, 100)
    , new HSBA(229.4, 80, 100, 100)
    , new HSL(229.4, 100, 60)
    , new HSLA(229.4, 100, 60, 100)
    , new NLNumber(105.3)
    , new NLWord(Blue, 0.3)
    ]
  , [ Hexadecimal.parse("#ff33a8")
    , new RGB(255, 51, 168)
    , new RGBA(255, 51, 168, 100)
    , new HSB(325.6, 80, 100)
    , new HSBA(325.6, 80, 100, 100)
    , new HSL(325.6, 100, 60)
    , new HSLA(325.6, 100, 60, 100)
    , new NLNumber(126.2)
    , new NLWord(Magenta, 1.2)
    ]
  , [ Hexadecimal.parse("#a833ff")
    , new RGB(168, 51, 255)
    , new RGBA(168, 51, 255, 100)
    , new HSB(274.4, 80, 100)
    , new HSBA(274.4, 80, 100, 100)
    , new HSL(274.4, 100, 60)
    , new HSLA(274.4, 100, 60, 100)
    , new NLNumber(115.5)
    , new NLWord(Violet, 0.5)
    ]
  , [ Hexadecimal.parse("#33fff6")
    , new RGB(51, 255, 246)
    , new RGBA(51, 255, 246, 100)
    , new HSB(177.4, 80, 100)
    , new HSBA(177.4, 80, 100, 100)
    , new HSL(177.4, 100, 60)
    , new HSLA(177.4, 100, 60, 100)
    , new NLNumber(85.2)
    , new NLWord(Cyan, 0.2)
    ]
  , [ Hexadecimal.parse("#f6ff33")
    , new RGB(246, 255, 51)
    , new RGBA(246, 255, 51, 100)
    , new HSB(62.6, 80, 100)
    , new HSBA(62.6, 80, 100, 100)
    , new HSL(62.6, 100, 60)
    , new HSLA(62.6, 100, 60, 100)
    , new NLNumber(45.3)
    , new NLWord(Yellow, 0.3)
    ]
  , [ Hexadecimal.parse("#ff8f33")
    , new RGB(255, 143, 51)
    , new RGBA(255, 143, 51, 100)
    , new HSB(27.1, 80, 100)
    , new HSBA(27.1, 80, 100, 100)
    , new HSL(27.1, 100, 60)
    , new HSLA(27.1, 100, 60, 100)
    , new NLNumber(25.9)
    , new NLWord(Orange, 0.9)
    ]
  ]

const nameToType =
  { "Hexadecimal": Hexadecimal
  , "HSB":         HSB
  , "HSBA":        HSBA
  , "HSL":         HSL
  , "HSLA":        HSLA
  , "NLNumber":    NLNumber
  , "NLWord":      NLWord
  , "RGB":         RGB
  , "RGBA":        RGBA
  }

// Test helper functions

const pairsOf = (testData: Array<Array<Representation>>) => (TypeA, TypeB): Representation => {
  return testData.map((bundle) => [bundle.find((x) => x instanceof TypeA), bundle.find((x) => x instanceof TypeB)])
}

const checkEquality = (a: Representation, b: Representation): void => {

  const check = (converter: (x: Representation) => Representation, keys: Array<string>): void => {

    const converted = converter(a)

    const target =
      keys.reduce(
        (acc, key) => {
          const value = (key !== "alpha" || "alpha" in a) ? b[key] : 100
          acc[key]    = value
          return acc
        }
      , {})

    expect(converted).toMatchObject(target)

  }

  if (b instanceof Hexadecimal) {
    check(((x) => x.toHexadecimal()), ["red", "green", "blue", "alpha"])
  } else if (b instanceof HSB) {
    check(((x) => x.toHSB()), ["hue", "saturation", "brightness"])
  } else if (b instanceof HSBA) {
    check(((x) => x.toHSBA()), ["hue", "saturation", "brightness", "alpha"])
  } else if (b instanceof HSL) {
    check(((x) => x.toHSL()), ["hue", "saturation", "lightness"])
  } else if (b instanceof HSLA) {
    check(((x) => x.toHSLA()), ["hue", "saturation", "lightness", "alpha"])
  } else if (b instanceof NLNumber) {
    check(((x) => x.toNLNumber()), ["number"])
  } else if (b instanceof NLWord) {
    check(((x) => x.toNLWord()), ["literal", "modifier"])
  } else if (b instanceof RGB) {
    check(((x) => x.toRGB()), ["red", "green", "blue"])
  } else if (b instanceof RGBA) {
    check(((x) => x.toRGBA()), ["red", "green", "blue", "alpha"])
  } else {
    throw new Error(`Unknown representation: ${JSON.stringify(b)}`)
  }

}

// Test Suite #1
//
// These tests test converting A => B, for every A and B (except that we don't test when A is NLWord
// or NLNumber, because these colors were not chosen as things that map well to and from those
// formats; that is handled in test suites #2 and #3). --Jason B. (7/2/25)

Object.entries(nameToType).forEach(
  ([name1, typ1]) => {
    if (typ1 !== NLWord && typ1 !== NLNumber) {
      Object.entries(nameToType).forEach(
        ([name2, typ2]) => {
          pairsOf(testColors)(typ1, typ2).forEach(([a, b]) => {

            const floor = (n: Num) => Math.floor(n * 10) / 10

            const isAnnoying =
              (
                ((typ1 === HSB || typ1 === HSBA) && ((typ2 === HSL || typ2 === HSLA) && b.lightness === 38.2)) ||
                ((typ2 === HSB || typ2 === HSBA) && ((typ1 === HSL || typ1 === HSLA) && a.lightness === 38.2))
              )

            const deannoyify = (t: Type, r: Representation): Representation => {

              const nudge = 0.1

              let out = null
              if (t === HSL) {
                out = new HSL(r.hue, r.saturation, floor(r.lightness + nudge))
              } else if (t === HSLA) {
                out = new HSLA(r.hue, r.saturation, floor(r.lightness + nudge), r.alpha)
              } else if (t === HSB) {
                out = new HSB(r.hue, r.saturation, floor(r.brightness + nudge))
              } else if (t === HSBA) {
                out = new HSBA(r.hue, r.saturation, floor(r.brightness + nudge), r.alpha)
              } else {
                throw new Error(`Unpossible annoying value: ${JSON.stringify(r)}`)
              }

              return out

            }

            const x = (!isAnnoying) ? a : deannoyify(typ1, a)
            const y = (!isAnnoying) ? b : deannoyify(typ2, b)

            test(`General: ${x.toString()} converts to ${name1 === name2 ? "itself" : y.toString()}`, () => {
              checkEquality(x, y)
            })

          })
        }
      )
    }
  }
)

// Test Suite #2
// NLWord => X, NLNumber => X, and vice versa

const testColors2 =
  [
    [ Hexadecimal.parse("#903f0c")
    , new RGB(144, 63, 12)
    , new RGBA(144, 63, 12, 100)
    , new HSB(23.2, 91.7, 56.5)
    , new HSBA(23.2, 91.7, 56.5, 100)
    , new HSL(23.2, 84.6, 30.6)
    , new HSLA(23.2, 84.6, 30.6, 100)
    , new NLNumber(22.9)
    , new NLWord(Orange, -2.1)
    ]
  , [ Hexadecimal.parse("#c6c6c6")
    , new RGB(198, 198, 198)
    , new RGBA(198, 198, 198, 100)
    , new HSB(0, 0, 77.6)
    , new HSBA(0, 0, 77.6, 100)
    , new HSL(0, 0, 77.6)
    , new HSLA(0, 0, 77.6, 100)
    , new NLNumber(7.5)
    , new NLWord(Gray, 2.5)
    ]
  , [ Hexadecimal.parse("#0a0607")
    , new RGB(10, 6, 7)
    , new RGBA(10, 6, 7, 100)
    , new HSB(345, 40, 3.9)
    , new HSBA(345, 40, 3.9, 100)
    , new HSL(345, 25, 3.1)
    , new HSLA(345, 25, 3.1, 100)
    , new NLNumber(130.1)
    , new NLWord(Pink, -4.9)
    ]
  , [ Hexadecimal.parse("#040909")
    , new RGB(4, 9, 9)
    , new RGBA(4, 9, 9, 100)
    , new HSB(180, 55.6, 3.5)
    , new HSBA(180, 55.6, 3.5, 100)
    , new HSL(180, 38.5, 2.5)
    , new HSLA(180, 38.5, 2.5, 100)
    , new NLNumber(80.1)
    , new NLWord(Cyan, -4.9)
    ]
  , [ Hexadecimal.parse("#ffffff")
    , new RGB(255, 255, 255)
    , new RGBA(255, 255, 255, 100)
    , new HSB(0, 0, 100)
    , new HSBA(0, 0, 100, 100)
    , new HSL(0, 0, 100)
    , new HSLA(0, 0, 100, 100)
    , new NLNumber(9.9)
    , new NLWord(White, 0)
    ]
  , [ Hexadecimal.parse("#000000")
    , new RGB(0, 0, 0)
    , new RGBA(0, 0, 0, 100)
    , new HSB(0, 0, 0)
    , new HSBA(0, 0, 0, 100)
    , new HSL(0, 0, 0)
    , new HSLA(0, 0, 0, 100)
    , new NLNumber(0)
    , new NLWord(Black, 0)
    ]
  , [ Hexadecimal.parse("#e07f96")
    , new RGB(224, 127, 150)
    , new RGBA(224, 127, 150, 100)
    , new HSB(345.8, 43.3, 87.8)
    , new HSBA(345.8, 43.3, 87.8, 100)
    , new HSL(345.8, 61, 68.8)
    , new HSLA(345.8, 61, 68.8, 100)
    , new NLNumber(135)
    , new NLWord(Pink, 0)
    ]
  , [ Hexadecimal.parse("#f5f590")
    , new RGB(245, 245, 144)
    , new RGBA(245, 245, 144, 100)
    , new HSB(60, 41.2, 96.1)
    , new HSBA(60, 41.2, 96.1, 100)
    , new HSL(60, 83.5, 76.3)
    , new HSLA(60, 83.5, 76.3, 100)
    , new NLNumber(47.3)
    , new NLWord(Yellow, 2.3)
    ]
  , [ Hexadecimal.parse("#f4f483")
    , new RGB(244, 244, 131)
    , new RGBA(244, 244, 131, 100)
    , new HSB(60, 46.3, 95.7)
    , new HSBA(60, 46.3, 95.7, 100)
    , new HSL(60, 83.7, 73.5)
    , new HSLA(60, 83.7, 73.5, 100)
    , new NLNumber(47)
    , new NLWord(Yellow, 2)
    ]
  , [ Hexadecimal.parse("#eded31")
    , new RGB(237, 237, 49)
    , new RGBA(237, 237, 49, 100)
    , new HSB(60, 79.3, 92.9)
    , new HSBA(60, 79.3, 92.9, 100)
    , new HSL(60, 83.9, 56.1)
    , new HSLA(60, 83.9, 56.1, 100)
    , new NLNumber(45)
    , new NLWord(Yellow, 0)
    ]
  , [ Hexadecimal.parse("#eca19d")
    , new RGB(236, 161, 157)
    , new RGBA(236, 161, 157, 100)
    , new HSB(3, 33.5, 92.5)
    , new HSBA(3, 33.5, 92.5, 100)
    , new HSL(3, 67.5, 77.1)
    , new HSLA(3, 67.5, 77.1)
    , new NLNumber(17.7)
    , new NLWord(Red, 2.7)
    ]
  , [ Hexadecimal.parse("#2f1e3e")
    , new RGB(47, 30, 62)
    , new RGBA(47, 30, 62, 100)
    , new HSB(271.9, 51.6, 24.3)
    , new HSBA(271.9, 51.6, 24.3, 100)
    , new HSL(271.9, 34.8, 18)
    , new HSLA(271.9, 34.8, 18, 100)
    , new NLNumber(111.8)
    , new NLWord(Violet, -3.2)
    ]
  , [ Hexadecimal.parse("#edd2e1")
    , new RGB(237, 210, 225)
    , new RGBA(237, 210, 225, 100)
    , new HSB(326.7, 11.4, 92.9)
    , new HSBA(326.7, 11.4, 92.9, 100)
    , new HSL(326.7, 42.9, 87.6)
    , new HSLA(326.7, 42.9, 87.6, 100)
    , new NLNumber(129)
    , new NLWord(Magenta, 4)
    ]
  ]

Object.entries(nameToType).forEach(
  ([name1, typ1]) => {
    Object.entries(nameToType).forEach(
      ([name2, typ2]) => {
        if (typ1 === NLWord || typ2 === NLWord || typ1 === NLNumber || typ2 === NLNumber) {
          pairsOf(testColors2)(typ1, typ2).forEach(([a, b]) => {
            test(`NL: ${a.toString()} converts to ${name1 === name2 ? "itself" : b.toString()}`, () => {
              checkEquality(a, b)
            })
          })
        }
      }
    )
  }
)

// Test Suite #3
// NLWord => X, NLNumber => X

const testColors3 =
  [
    [ Hexadecimal.parse("#020505")
    , new RGB(2, 5, 5)
    , new RGBA(2, 5, 5, 100)
    , new HSB(180, 60, 2)
    , new HSBA(180, 60, 2, 100)
    , new HSL(180, 42.9, 1.4)
    , new HSLA(180, 42.9, 1.4, 100)
    , new NLNumber(80)
    , new NLWord(Cyan, -5)
    ]
  , [ Hexadecimal.parse("#fcfdfd")
    , new RGB(252, 253, 253)
    , new RGBA(252, 253, 253, 100)
    , new HSB(180, 0.4, 99.2)
    , new HSBA(180, 0.4, 99.2, 100)
    , new HSL(180, 20, 99)
    , new HSLA(180, 20, 99, 100)
    , new NLNumber(89.9)
    , new NLWord(Cyan, 4.9)
    ]
  , [ Hexadecimal.parse("#edd2e1")
    , new RGB(237, 210, 225)
    , new RGBA(237, 210, 225, 100)
    , new HSB(326.7, 11.4, 92.9)
    , new HSBA(326.7, 11.4, 92.9, 100)
    , new HSL(326.7, 42.9, 87.6)
    , new HSLA(326.7, 42.9, 87.6, 100)
    , new NLNumber(129)
    , new NLWord(Cyan, 44) // Same as `magenta + 4`
    ]
  ]

Object.entries(nameToType).forEach(
  ([name1, typ1]) => {
    Object.entries(nameToType).forEach(
      ([name2, typ2]) => {
        if ((typ1 === NLWord || typ1 === NLNumber) && !(typ2 === NLWord || typ2 === NLNumber)) {
          pairsOf(testColors3)(typ1, typ2).forEach(([a, b]) => {
            test(`NLFrom: ${a.toString()} converts to ${name1 === name2 ? "itself" : b.toString()}`, () => {
              checkEquality(a, b)
            })
          })
        }
      }
    )
  }
)

// Test Suite #4
// Hue-based conversions

const testColors4 =
  [
    [ new HSB(1, 0, 0)
    , new HSBA(1, 0, 0, 100)
    , new HSL(1, 0, 0)
    , new HSLA(1, 0, 0, 100)
    ]
  ]

const nameToTypeH =
  Object.fromEntries(
    Object.
      keys(nameToType).
      filter((n) => n.startsWith("HS")).
      map((n) => [n, nameToType[n]])
  )

Object.entries(nameToTypeH).forEach(
  ([name1, typ1]) => {
    Object.entries(nameToTypeH).forEach(
      ([name2, typ2]) => {
        pairsOf(testColors4)(typ1, typ2).forEach(([a, b]) => {
          test(`Hue: ${a.toString()} converts to ${name1 === name2 ? "itself" : b.toString()}`, () => {
            checkEquality(a, b)
          })
        })
      }
    )
  }
)
