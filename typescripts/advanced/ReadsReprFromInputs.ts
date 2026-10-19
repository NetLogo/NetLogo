import { Hexadecimal, HSB, HSBA, HSL, HSLA, NLNumber, NLWord, RGB, RGBA } from "../color/Representation.js"

import { DOMManager } from "./DOMManager.js"

import type { Representation               } from "../color/Representation.js"

import type { Num3, Num4, Str1, Str3, Str4 } from "../common/Types.js"

type SetReprF = (repr: Representation) => void

export class ReadsReprFromInputs {

  constructor() {}

  read(dom: DOMManager, setRepr: SetReprF): void {

    const dropdown    = dom.findReprDropdown()
    const inputValues = dom.findInputValues()

    switch (dropdown.value) {
      case "nl-number": {
        const [colorNumber] = inputValues as Str1
        setRepr(new NLNumber(parseFloat(colorNumber)))
        break
      }
      case "nl-word": {
        const [colorWord] = inputValues as Str1
        setRepr(NLWord.parse(colorWord))
        break
      }
      case "hsb": {
        const [hue, saturation, brightness] = (inputValues as Str3).map((v) => parseInt(v)) as Num3
        setRepr(new HSB(hue, saturation, brightness))
        break
      }
      case "hsba": {
        const [hue, saturation, brightness, alpha] = (inputValues as Str4).map((v) => parseInt(v)) as Num4
        setRepr(new HSBA(hue, saturation, brightness, alpha))
        break
      }
      case "hsl": {
        const [hue, saturation, lightness] = (inputValues as Str3).map((v) => parseInt(v)) as Num3
        setRepr(new HSL(hue, saturation, lightness))
        break
      }
      case "hsla": {
        const [hue, saturation, lightness, alpha] = (inputValues as Str4).map((v) => parseInt(v)) as Num4
        setRepr(new HSLA(hue, saturation, lightness, alpha))
        break
      }
      case "rgb": {
        const [red, green, blue] = (inputValues as Str3).map((v) => parseInt(v)) as Num3
        setRepr(new RGB(red, green, blue))
        break
      }
      case "rgba": {
        const [red, green, blue, alpha] = (inputValues as Str4).map((v) => parseInt(v)) as Num4
        setRepr(new RGBA(red, green, blue, alpha))
        break
      }
      case "hex": {
        const [hex] = inputValues as Str1
        setRepr(Hexadecimal.parse(hex))
        break
      }
      default: {
        alert(`Invalid representation dropdown value: ${dropdown.value}`)
      }
    }

  }

}
