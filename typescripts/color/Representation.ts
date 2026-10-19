import { colorToRGB, nearestColorNumberOfRGB, rgbToWordPair } from "./ColorModel.js"
import { colorLiterals                                      } from "./ColorLiteral.js"

import type { Num, Num2, Num3, Num4, Str, Str3 } from "../common/Types.js"

import type { ColorLiteral } from "./ColorLiteral.js"

const round = (x: Num): Num => Math.round(x * 10) / 10

const calcHueDegrees = (hue: Num): Num => round(360 * (hue / 100))

interface Representation {

  equals(x: any): boolean
  toString():     Str

  toNLNumber   (): NLNumber
  toNLWord     (): NLWord
  toRGB        (): RGB
  toRGBA       (): RGBA
  toHSB        (): HSB
  toHSBA       (): HSBA
  toHSL        (): HSL
  toHSLA       (): HSLA
  toHexadecimal(): Hexadecimal
  toGUI_HSLA   (): GUI_HSLA

  withAlpha(alpha: Num): Representation

}

interface RGBLike {
  readonly red:   Num
  readonly green: Num
  readonly blue:  Num
}

interface HasAlpha {
  readonly alpha: Num
}

interface HasBrightness {
  readonly brightness: Num
}

interface HasSaturation {
  readonly saturation: Num
}

interface HasHue {
  readonly hue: Num
}

interface HasLightness {
  readonly lightness: Num
}

interface HSBLike extends HasHue, HasSaturation, HasBrightness {}

interface HSLLike extends HasHue, HasSaturation, HasLightness {}

class NLNumber implements Representation {

  public readonly number: Num

  private readonly proxy: RGBA

  constructor(num: Num) {
    this.number = num
    this.proxy  = this.toRGBA()
  }

  equals(x: any): boolean {
    if ((typeof x) === "object" && "toNLNumber" in x) {
      const other = x.toNLNumber()
      return this.number === other.number
    } else {
      return false
    }
  }

  toString(): Str {
    return `NLNumber(${this.number})`
  }

  toNLNumber(): NLNumber {
    return this
  }

  toNLWord(): NLWord {
    return this.proxy.toNLWord()
  }

  toRGB(): RGB {
    const [r, g, b] = colorToRGB(this.number)
    return new RGB(r, g, b)
  }

  toRGBA(): RGBA {
    return this.toRGB().toRGBA()
  }

  toHSB(): HSB {
    return this.proxy.toHSB()
  }

  toHSBA(): HSBA {
    return this.proxy.toHSBA()
  }

  toHSL(): HSL {
    return this.proxy.toHSL()
  }

  toHSLA(): HSLA {
    return this.proxy.toHSLA()
  }

  toHexadecimal(): Hexadecimal {
    return this.proxy.toHexadecimal()
  }

  toGUI_HSLA(): GUI_HSLA {
    return this.proxy.toGUI_HSLA()
  }

  withAlpha(alpha: Num): Representation {
    return this.proxy.withAlpha(alpha)
  }

}

class NLWord implements Representation {

  public readonly literal:  ColorLiteral
  public readonly modifier: Num

  private readonly proxy: RGBA

  constructor(literal: ColorLiteral, modifier: Num = 0) {
    this.literal  = literal
    this.modifier = modifier
    this.proxy    = this.toRGBA()
  }

  static parse(text: Str): NLWord {

    const [word, operator, value] = text.split(" ") as Str3
    const matchingLiteral         = colorLiterals.find((l) => l.name === word)

    if (matchingLiteral !== undefined) {
      const diff    = ((operator !== "+" && operator !== "-") || isNaN(Number(value))) ? 0 :
                       (operator === "+") ? Number(value) : -Number(value)
      return new NLWord(matchingLiteral, diff)
    } else {
      throw new Error(`Invalid NL color word: ${word}`)
    }

  }

  equals(x: any): boolean {
    if ((typeof x) === "object" && "toNLWord" in x) {
      const other = x.toNLWord()
      return this.literal === other.literal && this.modifier === other.modifier
    } else {
      return false
    }
  }

  toString(): Str {
    return `NLWord(${this.toText()})`
  }

  toNLNumber(): NLNumber {
    return new NLNumber(this.literal.value + this.modifier)
  }

  toNLWord(): NLWord {
    return this
  }

  toRGB(): RGB {
    return this.proxy.toRGB()
  }

  toRGBA(): RGBA {
    return this.toNLNumber().toRGBA()
  }

  toHSB(): HSB {
    return this.proxy.toHSB()
  }

  toHSBA(): HSBA {
    return this.proxy.toHSBA()
  }

  toHSL(): HSL {
    return this.proxy.toHSL()
  }

  toHSLA(): HSLA {
    return this.proxy.toHSLA()
  }

  toHexadecimal(): Hexadecimal {
    return this.proxy.toHexadecimal()
  }

  toGUI_HSLA(): GUI_HSLA {
    return this.proxy.toGUI_HSLA()
  }

  toText(): Str {

    const suffix =
      (this.modifier === 0) ? "" :
      (() => {
        const sign      = (this.modifier > 0) ? "+" : "-"
        const magnitude = Math.abs(this.modifier)
        return ` ${sign} ${magnitude}`
      })()

    return `${this.literal.name}${suffix}`

  }

  withAlpha(alpha: Num): Representation {
    return this.proxy.withAlpha(alpha)
  }

}

class RGB implements Representation, RGBLike {

  public readonly red:   Num
  public readonly green: Num
  public readonly blue:  Num

  private readonly proxy: RGBA

  constructor(r: Num, g: Num, b: Num) {
    this.red   = r
    this.green = g
    this.blue  = b
    this.proxy = this.toRGBA()
  }

  equals(x: any): boolean {
    if ((typeof x) === "object" && "toRGB" in x) {
      const other = x.toRGB()
      return this.red === other.red && this.green === other.green && this.blue === other.blue
    } else {
      return false
    }
  }

  toString(): Str {
    return `RGB(${this.red}, ${this.green}, ${this.blue})`
  }

  toNLNumber(): NLNumber {
    return new NLNumber(nearestColorNumberOfRGB(this.red, this.green, this.blue))
  }

  toNLWord(): NLWord {
    const [lit, mod] = rgbToWordPair(this.red, this.green, this.blue)
    return new NLWord(lit, mod)
  }

  toRGB(): RGB {
    return this
  }

  toRGBA(): RGBA {
    return new RGBA(this.red, this.green, this.blue, 100)
  }

  toHSB(): HSB {
    return this.proxy.toHSB()
  }

  toHSBA(): HSBA {
    return this.proxy.toHSBA()
  }

  toHSL(): HSL {
    return this.proxy.toHSL()
  }

  toHSLA(): HSLA {
    return this.proxy.toHSLA()
  }

  toHexadecimal(): Hexadecimal {
    return this.proxy.toHexadecimal()
  }

  toGUI_HSLA(): GUI_HSLA {
    return this.proxy.toGUI_HSLA()
  }

  withAlpha(alpha: Num): Representation {
    return this.proxy.withAlpha(alpha)
  }

}

class RGBA implements Representation, RGBLike, HasAlpha {

  public readonly red:   Num
  public readonly green: Num
  public readonly blue:  Num
  public readonly alpha: Num

  constructor(r: Num, g: Num, b: Num, a: Num) {
    this.red   = r
    this.green = g
    this.blue  = b
    this.alpha = a
  }

  equals(x: any): boolean {
    if ((typeof x) === "object" && "toRGBA" in x) {
      const other = x.toRGBA()
      return this.red === other.red && this.green === other.green && this.blue === other.blue && this.alpha === other.alpha
    } else {
      return false
    }
  }

  toString(): Str {
    return `RGBA(${this.red}, ${this.green}, ${this.blue}, ${this.alpha})`
  }

  toNLNumber(): NLNumber {
    return this.toRGB().toNLNumber()
  }

  toNLWord(): NLWord {
    return this.toRGB().toNLWord()
  }

  toRGB(): RGB {
    return new RGB(this.red, this.green, this.blue)
  }

  toRGBA(): RGBA {
    return this
  }

  toHSB(): HSB {
    return this.toHSBA().toHSB()
  }

  toHSBA(): HSBA {

    // Courtesy of Angelos at https://www.30secondsofcode.org/js/s/rgb-hex-hsl-hsb-color-format-conversion/
    const rgbToHSB = (red: Num, green: Num, blue: Num): [Num, Num, Num] => {

      const rx = red   / 255
      const gx = green / 255
      const bx = blue  / 255

      const v = Math.max(rx, gx, bx)
      const n = v - Math.min(rx, gx, bx)

      let h = null
      if (n === 0) {
        h = 0
      } else if (n && v === rx) {
        h =     (gx - bx) / n
      } else if (v === gx) {
        h = 2 + (bx - rx) / n
      } else {
        h = 4 + (rx - gx) / n
      }

      const hue        = 60 * ((h < 0) ? (h + 6) : h)
      const saturation = v && (n / v) * 100
      const brightness = v * 100

      return [hue, saturation, brightness].map(round) as Num3

    }

    const [h, s, b] = rgbToHSB(this.red, this.green, this.blue)

    return new HSBA(h, s, b, this.alpha)

  }

  toHSL(): HSL {
    return this.toHSLA().toHSL()
  }

  toHSLA(): HSLA {

    // Courtesy of Kamil Kiełczewski at https://stackoverflow.com/a/54071699/5288538
    const rgbToHSL = (red: Num, green: Num, blue: Num): [Num, Num, Num] => {

      const rx = red   / 255
      const gx = green / 255
      const bx = blue  / 255

      const v = Math.max(rx, gx, bx)
      const c = v - Math.min(rx, gx, bx)
      const f = 1 - Math.abs(v + v - c - 1)

      const subH  = (c && (
                       (v === rx) ? ((gx - bx)     / c) :
                       (v === gx) ? (2 + (bx - rx) / c) :
                                    (4 + (rx - gx) / c)
                    ))
      const hue   = 60 * (subH < 0 ? subH + 6 : subH)

      const subS  = (f !== 0) ? (c / f) : 0
      const sat   = subS * 100

      const subL  = (v + v - c) / 2
      const light = subL * 100

      return [hue, sat, light].map(round) as Num3

    }

    const [h, s, l] = rgbToHSL(this.red, this.green, this.blue)

    return new HSLA(h, s, l, this.alpha)

  }

  toHexadecimal(): Hexadecimal {
    return new Hexadecimal(this.red, this.green, this.blue, this.alpha)
  }

  toGUI_HSLA(): GUI_HSLA {
    return this.toHSLA().toGUI_HSLA()
  }

  withAlpha(alpha: Num): Representation {
    return new RGBA(this.red, this.green, this.blue, alpha)
  }

}

class HSB implements Representation, HSBLike {

  public readonly hue:        Num
  public readonly saturation: Num
  public readonly brightness: Num

  private readonly proxy: HSBA

  constructor(h: Num, s: Num, b: Num) {
    this.hue        = h
    this.saturation = s
    this.brightness = b
    this.proxy      = this.toHSBA()
  }

  equals(x: any): boolean {
    if ((typeof x) === "object" && "toHSB" in x) {
      const other = x.toHSB()
      return this.hue === other.hue && this.saturation === other.saturation && this.brightness === other.brightness
    } else {
      return false
    }
  }

  toString(): Str {
    return `HSB(${this.hue}, ${this.saturation}, ${this.brightness})`
  }

  toNLNumber(): NLNumber {
    return this.proxy.toNLNumber()
  }

  toNLWord(): NLWord {
    return this.proxy.toNLWord()
  }

  toRGB(): RGB {
    return this.proxy.toRGB()
  }

  toRGBA(): RGBA {
    return this.proxy.toRGBA()
  }

  toHSB(): HSB {
    return this
  }

  toHSBA(): HSBA {
    return new HSBA(this.hue, this.saturation, this.brightness, 100)
  }

  toHSL(): HSL {
    return this.proxy.toHSL()
  }

  toHSLA(): HSLA {
    return this.proxy.toHSLA()
  }

  toHexadecimal(): Hexadecimal {
    return this.proxy.toHexadecimal()
  }

  toGUI_HSLA(): GUI_HSLA {
    return this.proxy.toGUI_HSLA()
  }

  withAlpha(alpha: Num): Representation {
    return this.proxy.withAlpha(alpha)
  }

}

class HSBA implements Representation, HSBLike, HasAlpha {

  public readonly hue:        Num
  public readonly saturation: Num
  public readonly brightness: Num
  public readonly alpha:      Num

  constructor(h: Num, s: Num, b: Num, a: Num) {
    this.hue        = h
    this.saturation = s
    this.brightness = b
    this.alpha      = a
  }

  equals(x: any): boolean {
    if ((typeof x) === "object" && "toHSBA" in x) {
      const other = x.toHSBA()
      return this.hue === other.hue && this.saturation === other.saturation && this.brightness === other.brightness &&
             this.alpha === other.alpha
    } else {
      return false
    }
  }

  toString(): Str {
    return `HSBA(${this.hue}, ${this.saturation}, ${this.brightness}, ${this.alpha})`
  }

  toNLNumber(): NLNumber {
    return this.toRGBA().toNLNumber()
  }

  toNLWord(): NLWord {
    return this.toRGBA().toNLWord()
  }

  toRGB(): RGB {
    return this.toRGBA().toRGB()
  }

  toRGBA(): RGBA {

    // Courtesy of some bot at https://labex.io/tutorials/javascript-exploring-hsb-to-rgb-color-conversion-28377
    const extract = (n: Num) => {
      const g    = (x: Num) => (x + this.hue / 60) % 6
      const comp = (this.brightness / 100) * (1 - (this.saturation / 100) * Math.max(0, Math.min(g(n), 4 - g(n), 1)))
      return Math.round(255 * comp)
    }

    const red   = extract(5)
    const green = extract(3)
    const blue  = extract(1)

    return new RGBA(red, green, blue, this.alpha)

  }

  toHSB(): HSB {
    return new HSB(this.hue, this.saturation, this.brightness)
  }

  toHSBA(): HSBA {
    return this
  }

  toHSL(): HSL {
    return this.toHSLA().toHSL()
  }

  toHSLA(): HSLA {

    // Courtesy of Roko C. Buljan at https://stackoverflow.com/a/31322636/5288538
    const getSLAsHSL = (s: Num, b: Num): [Num, Num] => {
      const l          = (2 - s / 100) * b / 2
      const saturation = (l === 0) ? 0 : (s * b / ((l < 50) ? (l * 2) : (200 - (l * 2))))
      const lightness  = l
      return [saturation, lightness].map(round) as Num2
    }

    const [s, l] = getSLAsHSL(this.saturation, this.brightness)

    return new HSLA(this.hue, s, l, this.alpha)

  }

  toHexadecimal(): Hexadecimal {
    return this.toRGBA().toHexadecimal()
  }

  toGUI_HSLA(): GUI_HSLA {
    return this.toHSLA().toGUI_HSLA()
  }

  withAlpha(alpha: Num): Representation {
    return new HSBA(this.hue, this.saturation, this.brightness, alpha)
  }

}

class HSL implements Representation, HSLLike {

  public readonly hue:        Num
  public readonly saturation: Num
  public readonly lightness:  Num

  private readonly proxy: HSLA

  constructor(h: Num, s: Num, l: Num) {
    this.hue        = h
    this.saturation = s
    this.lightness  = l
    this.proxy      = this.toHSLA()
  }

  equals(x: any): boolean {
    if ((typeof x) === "object" && "toHSL" in x) {
      const other = x.toHSL()
      return this.hue === other.hue && this.saturation === other.saturation && this.lightness === other.lightness
    } else {
      return false
    }
  }

  toString(): Str {
    return `HSL(${this.hue}, ${this.saturation}, ${this.lightness})`
  }

  toNLNumber(): NLNumber {
    return this.proxy.toNLNumber()
  }

  toNLWord(): NLWord {
    return this.proxy.toNLWord()
  }

  toRGB(): RGB {
    return this.proxy.toRGB()
  }

  toRGBA(): RGBA {
    return this.proxy.toRGBA()
  }

  toHSB(): HSB {
    return this.proxy.toHSB()
  }

  toHSBA(): HSBA {
    return this.proxy.toHSBA()
  }

  toHSL(): HSL {
    return this
  }

  toHSLA(): HSLA {
    return new HSLA(this.hue, this.saturation, this.lightness, 100)
  }

  toHexadecimal(): Hexadecimal {
    return this.proxy.toHexadecimal()
  }

  toGUI_HSLA(): GUI_HSLA {
    return this.proxy.toGUI_HSLA()
  }

  withAlpha(alpha: Num): Representation {
    return this.proxy.withAlpha(alpha)
  }

}

class HSLA implements Representation, HSLLike, HasAlpha {

  public readonly hue:        Num
  public readonly saturation: Num
  public readonly lightness:  Num
  public readonly alpha:      Num

  constructor(h: Num, s: Num, l: Num, a: Num) {
    this.hue        = h
    this.saturation = s
    this.lightness  = l
    this.alpha      = a
  }

  equals(x: any): boolean {
    if ((typeof x) === "object" && "toHSLA" in x) {
      const other = x.toHSLA()
      return this.hue === other.hue && this.saturation === other.saturation && this.lightness === other.lightness &&
             this.alpha === other.alpha
    } else {
      return false
    }
  }

  toString(): Str {
    return `HSLA(${this.hue}, ${this.saturation}, ${this.lightness}, ${this.alpha})`
  }

  toNLNumber(): NLNumber {
    return this.toRGBA().toNLNumber()
  }

  toNLWord(): NLWord {
    return this.toRGBA().toNLWord()
  }

  toRGB(): RGB {
    return this.toRGBA().toRGB()
  }

  toRGBA(): RGBA {

    // Courtesy of Kamil Kiełczewski at https://stackoverflow.com/a/64090995/5288538
    const asRGB = (hue: Num, saturation: Num, lightness: Num): [Num, Num, Num] => {
      const l     = lightness / 100
      const a     = (saturation / 100) * Math.min(l, 1 - l)
      const morph = (n: Num): Num => {
        const k     = (n + hue / 30) % 12
        const value = l - (a * Math.max(-1, Math.min(k - 3, 9 - k, 1)))
        return Math.round(value * 255)
      }
      return [morph(0), morph(8), morph(4)]
    }

    const [r, g, b] = asRGB(this.hue, this.saturation, this.lightness)

    return new RGBA(r, g, b, this.alpha)

  }

  toHSB(): HSB {
    const hsba = this.toHSBA()
    return new HSB(hsba.hue, hsba.saturation, hsba.brightness)
  }

  toHSBA(): HSBA {

    // Courtesy of Roko C. Buljan at https://stackoverflow.com/a/31322636/5288538
    const getSBAsHSB = (s: Num, l: Num): [Num, Num] => {
      const temp  = s * ((l < 50) ? l : (100 - l)) / 100
      const semiB = l + temp
      const hsbS = (semiB !== 0) ? round(200 * temp / semiB) : 0
      const hsbB = round(semiB)
      return [hsbS, hsbB]
    }

    const [s, b] = getSBAsHSB(this.saturation, this.lightness)

    return new HSBA(this.hue, s, b, this.alpha)

  }

  toHSL(): HSL {
    return new HSL(this.hue, this.saturation, this.lightness)
  }

  toHSLA(): HSLA {
    return this
  }

  toHexadecimal(): Hexadecimal {
    return this.toRGBA().toHexadecimal()
  }

  toGUI_HSLA(): GUI_HSLA {

    const scalingFactor = 1 + (this.saturation / 100)
    const y             = 100 - Math.min(100, this.lightness * scalingFactor)
    const h             = this.hue / 360 * 100

    return new GUI_HSLA(h, this.saturation, y, this.alpha)

  }

  withAlpha(alpha: Num): HSLA {
    return new HSLA(this.hue, this.saturation, this.lightness, alpha)
  }

}

class Hexadecimal implements Representation, RGBLike, HasAlpha {

  public readonly red:   Num
  public readonly green: Num
  public readonly blue:  Num
  public readonly alpha: Num

  private readonly proxy: RGBA

  constructor(red: Num, green: Num, blue: Num, alpha: Num) {
    this.red   = red
    this.green = green
    this.blue  = blue
    this.alpha = alpha
    this.proxy = new RGBA(red, green, blue, alpha)
  }

  static parse(hexStr: Str): Hexadecimal {

    const hex = hexStr.slice(1)

    if (hex.length === 6 || hex.length === 8) {

      const [red, green, blue, a] = [0, 2, 4, 6].map((n) => parseInt(hex.slice(n, n + 2), 16)) as Num4
      const alpha                 = (!isNaN(a)) ? Math.round(a / 255 * 100) : 100

      return new Hexadecimal(red, green, blue, alpha)

    } else {
      throw new Error(`Unparseable hexadecimal: ${hexStr}`)
    }

  }

  hex(): Str {
    const hex   = (x: Num): Str => x.toString(16).padStart(2, "0")
    const rgba  = this.toRGBA()
    const alpha = (rgba.alpha < 100) ? hex(Math.round(rgba.alpha / 100 * 255)) : ""
    return `#${hex(rgba.red)}${hex(rgba.green)}${hex(rgba.blue)}${alpha}`
  }

  equals(x: any): boolean {
    if ((typeof x) === "object" && "toHexadecimal" in x) {
      const other = x.toHexadecimal()
      return this.red === other.red && this.green === other.green && this.blue === other.blue && this.alpha === other.alpha
    } else {
      return false
    }
  }

  toString(): Str {
    return `Hexadecimal(${this.hex()})`
  }

  toNLNumber(): NLNumber {
    return this.proxy.toNLNumber()
  }

  toNLWord(): NLWord {
    return this.proxy.toNLWord()
  }

  toRGB(): RGB {
    return this.proxy.toRGB()
  }

  toRGBA(): RGBA {
    return this.proxy
  }

  toHSB(): HSB {
    return this.proxy.toHSB()
  }

  toHSBA(): HSBA {
    return this.proxy.toHSBA()
  }

  toHSL(): HSL {
    return this.proxy.toHSL()
  }

  toHSLA(): HSLA {
    return this.proxy.toHSLA()
  }

  toHexadecimal(): Hexadecimal {
    return this
  }

  toGUI_HSLA(): GUI_HSLA {
    return this.proxy.toGUI_HSLA()
  }

  withAlpha(alpha: Num): Representation {
    return this.proxy.withAlpha(alpha)
  }

}

class GUI_HSLA implements Representation, HasAlpha {

  public readonly hue:        Num
  public readonly saturation: Num
  public readonly lightness:  Num
  public readonly alpha:      Num

  private readonly proxy: HSLA

  constructor(h: Num, s: Num, l: Num, a: Num) {

    this.hue        = h
    this.saturation = s
    this.lightness  = l
    this.alpha      = a

    this.proxy      = this.toHSLA()

  }

  equals(x: any): boolean {
    if ((typeof x) === "object" && "toGUI_HSLA" in x) {
      const other = x.toGUI_HSLA()
      return this.hue === other.hue && this.saturation === other.saturation && this.lightness === other.lightness &&
             this.alpha === other.alpha
    } else {
      return false
    }
  }

  toString(): Str {
    return `GUI_HSLA(${this.hue}, ${this.saturation}, ${this.lightness}, ${this.alpha})`
  }

  toNLNumber(): NLNumber {
    return this.proxy.toNLNumber()
  }

  toNLWord(): NLWord {
    return this.proxy.toNLWord()
  }

  toRGB(): RGB {
    return this.proxy.toRGB()
  }

  toRGBA(): RGBA {
    return this.proxy.toRGBA()
  }

  toHSB(): HSB {
    return this.proxy.toHSB()
  }

  toHSBA(): HSBA {
    return this.proxy.toHSBA()
  }

  toHSL(): HSL {
    return this.proxy.toHSL()
  }

  toHSLA(): HSLA {

    const hue        = calcHueDegrees(this.hue)
    const saturation = this.saturation

    const mult          = 100 - this.lightness
    const scalingFactor = 1 + (this.saturation / 100)
    const lightness     = mult / scalingFactor

    const alpha = this.alpha

    return new HSLA(hue, saturation, lightness, alpha)

  }

  toHexadecimal(): Hexadecimal {
    return this.proxy.toHexadecimal()
  }

  toGUI_HSLA(): GUI_HSLA {
    return this
  }

  withHue(hue: Num): GUI_HSLA {
    return new GUI_HSLA(hue, this.saturation, this.lightness, this.alpha)
  }

  withSaturation(saturation: Num): GUI_HSLA {
    return new GUI_HSLA(this.hue, saturation, this.lightness, this.alpha)
  }

  withLightness(lightness: Num): GUI_HSLA {
    return new GUI_HSLA(this.hue, this.saturation, lightness, this.alpha)
  }

  withAlpha(alpha: Num): Representation {
    return new GUI_HSLA(this.hue, this.saturation, this.lightness, alpha)
  }

}


export { calcHueDegrees, GUI_HSLA, NLNumber, NLWord, RGB, RGBA, HSB, HSBA, HSL, HSLA, Hexadecimal }

export type { HasAlpha, Representation, RGBLike }
