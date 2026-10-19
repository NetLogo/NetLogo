import type { Representation } from "../color/Representation.js"

import type { DOMManager } from "./DOMManager.js"

export class WritesReprToInputs {

  write(dom: DOMManager, repr: Representation): void {

    const activeControls = dom.findActiveControls()

    switch (activeControls.id) {
      case "netlogo-number-controls": {
        dom.setInputByID("netlogo-number", repr.toNLNumber().number)
        break
      }
      case "netlogo-word-controls": {
        dom.setInputByID("netlogo-word", repr.toNLWord().toText())
        break
      }
      case "hsb-controls": {
        const { hue, saturation, brightness } = repr.toHSB()
        dom.setInputByID("hsb-h",        hue)
        dom.setInputByID("hsb-s", saturation)
        dom.setInputByID("hsb-b", brightness)
        break
      }
      case "hsba-controls": {
        const { hue, saturation, brightness, alpha } = repr.toHSBA()
        dom.setInputByID("hsba-h",        hue)
        dom.setInputByID("hsba-s", saturation)
        dom.setInputByID("hsba-b", brightness)
        dom.setInputByID("hsba-a",      alpha)
        break
      }
      case "hsl-controls": {
        const { hue, saturation, lightness } = repr.toHSL()
        dom.setInputByID("hsl-h",        hue)
        dom.setInputByID("hsl-s", saturation)
        dom.setInputByID("hsl-l",  lightness)
        break
      }
      case "hsla-controls": {
        const { hue, saturation, lightness, alpha } = repr.toHSLA()
        dom.setInputByID("hsla-h",        hue)
        dom.setInputByID("hsla-s", saturation)
        dom.setInputByID("hsla-l",  lightness)
        dom.setInputByID("hsla-a",      alpha)
        break
      }
      case "rgb-controls": {
        const { red, green, blue } = repr.toRGB()
        dom.setInputByID("rgb-r",   red)
        dom.setInputByID("rgb-g", green)
        dom.setInputByID("rgb-b",  blue)
        break
      }
      case "rgba-controls": {
        const { red, green, blue, alpha } = repr.toRGBA()
        dom.setInputByID("rgba-r",   red)
        dom.setInputByID("rgba-g", green)
        dom.setInputByID("rgba-b",  blue)
        dom.setInputByID("rgba-a", alpha)
        break
      }
      case "hex-controls": {
        dom.setInputByID("hex", repr.toHexadecimal().hex())
        break
      }
      default: {
        alert(`Invalid representation dropdown ID: ${activeControls.id}`)
      }
    }

  }

}
