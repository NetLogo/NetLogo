import { OutputType } from "./OutputType.js"

import type { Num, Str } from "../common/Types.js"

const clamp = (percentage: Num): Num => Math.max(0, Math.min(100, percentage))

const optionValueToContainerID =
  { "nl-number": "netlogo-number-controls"
  , "nl-word":   "netlogo-word-controls"
  , "hsb":       "hsb-controls"
  , "hsba":      "hsba-controls"
  , "hsl":       "hsl-controls"
  , "hsla":      "hsla-controls"
  , "rgb":       "rgb-controls"
  , "rgba":      "rgba-controls"
  , "hex":       "hex-controls"
  } as Record<Str, Str>


const outputTypeToHTMLValue =
  new Map([
    [OutputType.NLNumber, "nl-number"]
  , [OutputType.NLWord  , "nl-word"  ]
  , [OutputType.RGB     , "rgb"      ]
  , [OutputType.RGBA    , "rgba"     ]
  , [OutputType.HSB     , "hsb"      ]
  , [OutputType.HSBA    , "hsba"     ]
  , [OutputType.HSL     , "hsl"      ]
  , [OutputType.HSLA    , "hsla"     ]
  ]) as Map<OutputType, Str>

export { clamp, optionValueToContainerID, outputTypeToHTMLValue }
