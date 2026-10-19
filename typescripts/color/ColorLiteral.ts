import type { Num, Str } from "../common/Types.js"

class ColorLiteral {
  public readonly name:  Str
  public readonly value: Num
  constructor(name: Str, value: Num) {
    this.name  = name
    this.value = value
  }
}

const Black:     ColorLiteral = new ColorLiteral("black"    ,   0)
const White:     ColorLiteral = new ColorLiteral("white"    , 9.9)
const Gray:      ColorLiteral = new ColorLiteral("gray"     ,   5)
const Red:       ColorLiteral = new ColorLiteral("red"      ,  15)
const Orange:    ColorLiteral = new ColorLiteral("orange"   ,  25)
const Brown:     ColorLiteral = new ColorLiteral("brown"    ,  35)
const Yellow:    ColorLiteral = new ColorLiteral("yellow"   ,  45)
const Green:     ColorLiteral = new ColorLiteral("green"    ,  55)
const Lime:      ColorLiteral = new ColorLiteral("lime"     ,  65)
const Turquoise: ColorLiteral = new ColorLiteral("turquoise",  75)
const Cyan:      ColorLiteral = new ColorLiteral("cyan"     ,  85)
const Sky:       ColorLiteral = new ColorLiteral("sky"      ,  95)
const Blue:      ColorLiteral = new ColorLiteral("blue"     , 105)
const Violet:    ColorLiteral = new ColorLiteral("violet"   , 115)
const Magenta:   ColorLiteral = new ColorLiteral("magenta"  , 125)
const Pink:      ColorLiteral = new ColorLiteral("pink"     , 135)

const colorLiterals: Array<ColorLiteral> =
  [Black, White, Gray, Red, Orange, Brown, Yellow, Green, Lime, Turquoise, Cyan, Sky, Blue, Violet, Magenta, Pink]

export { Black, Blue, Brown, ColorLiteral, colorLiterals, Cyan, Gray, Green, Lime, Magenta, Orange, Pink, Red, Sky, Turquoise
       , Violet, White, Yellow }
