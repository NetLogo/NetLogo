
# NetLogo Palette Extension

## Building

Use the netlogo.jar.url environment variable to tell sbt which NetLogo.jar to compile against (defaults to NetLogo 5.3). For example:

    sbt -Dnetlogo.jar.url=file:///path/to/NetLogo/target/NetLogo.jar package

If compilation succeeds, `palette.jar` will be created.

## Using the Palette Extension

The NetLogo palette extension offers the user more control over their colors. The colors go beyond NetLogo colors, including ColorBrewer color schemes and arbitrary RGB colors. Additionally, users can control specific components of their color such as alpha, hue and red.

## Getting Started

To get started with palettes add to the top of your Code tab:

```
extensions [palette]
```

you can then call any of the primitives by adding `palette:` before the primitive:

```
palette:hue-of
palette:set-saturation

palette:scale-gradient
palette:scale-scheme
```

## Background
### Review of color representation in NetLogo

#### Colors can be represented as NetLogo colors or RGB or RGBA colors

* A NetLogo color (NLC or NL color) is a number in the range 0 to 140, with the exception of 140 itself, other values are ‘wrapped.'
    - NetLogo colors are shades of a base color which has units digit 5, and is in the middle of the shade range.
* An RGB color is a list of 3 numbers.
    - The numbers represent red, green and blue and are in the range 0 to 255.
* An RGBA color is a list of 4 numbers.
    - The fourth number is called alpha and is in the range 0 to 255.
    - Alpha represents transparency with 0 fully transparent, and 255 fully opaque.
* RGB/A will be used to denote a color that is either RGB or RGBA.

#### Transparency
The degree to which one can see through a color can also be specified by referring to _transparency_. Transparency is in the range 0 to 100, with 0 fully opaque and 100 fully transparent. Transparency is not stored as part of a color, but is mathematically converted to and from alpha.

#### HSB Color Specification
Colors can also be specified (but not stored as color values) by reference to HSB - hue, saturation and brightness.

* Hue ranges 0 to 360 and forms a cycle.
* Saturation ranges 0 to 100.
* Brightness ranges 0 to 100.
* For more information, search the web for 'hsb color system.'

### More control over the color

Palette primitives allow the user to:

* Report a specific component of a color
    - e.g. palette:hue-of
* Report the result of changing a specific component of a color
    - e.g. palette:with-hue
* Report a specific component of an agent's color
    - e.g. palette:hue
* Change a specific component of an agent's color
    - e.g. palette:set-hue

The user can also create color gradients and access ColorBrewer color schemes.

### Varying an Agent's Transparency or Color

You can make a turtle more transparent by decreasing alpha, or increasing transparency.
A similar approach can be used to adjust color components such as brightness.
Here are two examples:

```
ask turtles [ palette:set-transparency palette:transparency  + 10 ]
ask turtles [ palette:set-alpha .9 * palette:alpha ]
```
Because an error will result if the value passed to a set command is not in the correct range, a little additional code is sometimes needed.
```
ask turtles [ palette:set-transparency min list  100 (palette:transparency + 10) ]
ask turtles [ palette:set-alpha max list 0 (palette:alpha - 25.5) ]
```
### How do I choose a color scheme?

ColorBrewer has three kinds of color schemes: Sequential, Divergent and Qualitative. Although choosing a color palette is a design problem that does not have a single solution we offer the following guidelines for choosing colors for a NetLogo model:

#### Decide first whether to use a Sequential, Divergent or Qualitative color scheme
* Sequential color schemes are best for models of continuous natural phenomena such as as heat diffusion in physics or fire in earth sciences.
* Divergent color schemes are useful for highlighting a middle value in a model. Therefore they could be used with the heat diffusion model if the goal were to highlight regions with the middle temperature.
* Qualitative colors are best for choosing colors in models where color denotes category and not value.

#### Additional color considerations
* For agents that cover large areas avoid strong colors and try to use pastel colors. However, for a few small isolated agents try to use strong colors such as such an accent.
* The main goal is to avoid having a large area covered with agents with a bright color and or having small areas having a muted pastel color.
* If you are coloring both turtles and patches, make sure they have different ranges of hue, saturation and value. For example, use different hues of pastel for patches and accent for turtles.

You can learn more about the use of these color collections in the original ColorBrewer paper (Harrower, Brewer 2003), which focuses on their application to maps. See (Kornhauser, Wilensky, and Rand 1999) for design guidelines for visualization of Agent Based Models.

### Should I use a continuous color gradient or just a discrete color set?

 The answer depends on the focus of the user experience.

 For example, gradients are more aesthetic thus are more memorable than discrete colors. Consequently, a gradient can be a better choice for presentations where the main goal of the image is to be attractive and memorable. However, binning values in a discrete set of colors simplifies tasks such as estimation and counting by removing unnecessary detail and focusing on the big picture. Thus, discrete colors can be a better choice for a written document for which the user will have the time and interest to study the visualization.

 In order to see the difference you can turn on and off the gradient in the Heat Diffusion model. You can observe that turning gradient on makes the model more aesthetic, but it becomes harder to estimate the value of a patch at a given position.

### Example Models

There are a few examples of using the palette primitives in the Code Examples section of the models library:

* Palette Example
* Color Bubbles
* Color Painting
* Color Reveal

And one Sample Model that uses the extension:

* Heat Diffusion - Alternative Gradient

### Further Reading

* Be sure to check the [ColorBrewer web page](http://colorbrewer2.org/).
* To get a deeper understanding of how to use the color schemes read the ColorBrewer paper (Harrower, Brewer 2003).


## Primitives

[`palette:alpha-of`](#palettealpha-of)
[`palette:with-alpha`](#palettewith-alpha)
[`palette:alpha`](#palettealpha)
[`palette:set-alpha`](#paletteset-alpha)
[`palette:transparency-of`](#palettetransparency-of)
[`palette:with-transparency`](#palettewith-transparency)
[`palette:transparency`](#palettetransparency)
[`palette:set-transparency`](#paletteset-transparency)
[`palette:hue-of`](#palettehue-of)
[`palette:with-hue`](#palettewith-hue)
[`palette:hue`](#palettehue)
[`palette:set-hue`](#paletteset-hue)
[`palette:saturation-of`](#palettesaturation-of)
[`palette:with-saturation`](#palettewith-saturation)
[`palette:saturation`](#palettesaturation)
[`palette:set-saturation`](#paletteset-saturation)
[`palette:brightness-of`](#palettebrightness-of)
[`palette:with-brightness`](#palettewith-brightness)
[`palette:brightness`](#palettebrightness)
[`palette:set-brightness`](#paletteset-brightness)
[`palette:R-of`](#paletteR-of)
[`palette:with-R`](#palettewith-R)
[`palette:R`](#paletteR)
[`palette:set-R`](#paletteset-R)
[`palette:G-of`](#paletteG-of)
[`palette:with-G`](#palettewith-G)
[`palette:G`](#paletteG)
[`palette:set-G`](#paletteset-G)
[`palette:B-of`](#paletteB-of)
[`palette:with-B`](#palettewith-B)
[`palette:B`](#paletteB)
[`palette:set-B`](#paletteset-B)
[`palette:scale-gradient`](#palettescale-gradient)
[`palette:scale-gradient-hsb`](#palettescale-gradient-hsb)
[`palette:scheme-colors`](#palettescheme-colors)
[`palette:scale-scheme`](#palettescale-scheme)
[`palette:scheme-dialog`](#palettescheme-dialog)


### `palette:alpha-of`

```NetLogo
palette:alpha-of color
```


Reports a value in the range 0 to 255 representing the alpha of the given NL, RGB or RGBA *color*. The alpha value of an NL or RGB color is 255.

Example:
```
show palette:alpha-of red ;; 255
show palette:alpha-of [3 14 159 26] ;; 26
```



### `palette:with-alpha`

```NetLogo
palette:with-alpha color number
```


Reports an RGBA color with alpha component equal to *number* and the RGB content of *color*. An error message results if alpha is not in the range from 0 to 255.

Example:
```
show [12 34 55 6] palette:with-alpha 99 ;; [12 34 55 99]
```



### `palette:alpha`

```NetLogo
palette:alpha
```


Get the alpha value of the color of the agent. The alpha value of an NL or RGB color is 255.

Example:
```
show [palette:alpha] of turtles
```



### `palette:set-alpha`

```NetLogo
palette:set-alpha number
```


Set the color of the agent to the RGBA color with alpha value *number* and the RGB content of the color of the turtle or link (or patch in NetLogo3D). An error message results if *number* is not in the range 0 to 255.

Example:
```
ask turtles [palette:set-alpha 100]
```



### `palette:transparency-of`

```NetLogo
palette:transparency-of color
```


Reports a value in the range 0 to 100 representing the percent transparency of the given NL, RGB or RGBA *color*. The percent transparency of an NL or RGB color is 0.

Example:
```
show palette:transparency-of [100 100 100 100] ;; 60.7843137254902
show palette:transparency-of red ;; 0

```



### `palette:with-transparency`

```NetLogo
palette:with-transparency color number
```



Reports an RGBA color with an alpha value equivalent to the transparency *number* and the RGB content of *color*. An error message results if *number* is not in the range 0 to 100.

Example:
```
show red palette:with-transparency 100 ;; [215 50 41 0]
```



### `palette:transparency`

```NetLogo
palette:transparency
```



Reports the transparency equivalent of the agent's alpha.

Example:
```
show [palette:transparency] of turtles
```



### `palette:set-transparency`

```NetLogo
palette:set-transparency number
```



Set the color of the agent to the RGBA color with alpha value equivalent to the transparency *number* and the RGB content of the color of the turtle or link. An error message results if *number* is not in the range 0 to 100.

Example:
```
ask turtles [palette:set-transparency 30]
```



### `palette:hue-of`

```NetLogo
palette:hue-of color
```


Reports a value in the range 0 to 360 representing the hue component in HSB color space of the given NL, RGB or RGBA *color*.

Example:
```
show palette:hue-of red ;; 3.103
```



### `palette:with-hue`

```NetLogo
palette:with-hue color number
```



Reports an RGBA color with hue component in HSB color space equal to *number* and the other HSB components of the input *color* unchanged. An error message results if *number* is not in the range 0 to 360.

Example:
```
show red palette:with-hue 100 ;; [99 215 41]
```



### `palette:hue`

```NetLogo
palette:hue
```


Reports a number in the range 0 to 360 that represents the hue of an agent’s color or pcolor.

Example:
```
crt 1 [set color red]
[palette:hue] of turtle 0 ;; 3.103

```



### `palette:set-hue`

```NetLogo
palette:set-hue number
```


Changes an agent’s hue value to *number*, leaving the other HSB components unchanged. An error message results if *number* is not in the range from 0 to 360.

Example:
```
ask turtles [palette:set-hue 30]
```



### `palette:saturation-of`

```NetLogo
palette:saturation-of color
```


Reports a value in the range 0 to 100 representing the saturation component in HSB color space of the given NL, RGB or RGBA *color*.

Example:
```
show palette:saturation-of red ;; 80.93
```



### `palette:with-saturation`

```NetLogo
palette:with-saturation color number
```



Reports an RGBA color with saturation component in HSB color space equal to *number* and the other HSB components of the input *color* unchanged. An error message results if *number* is not in the range 0 to 100.

Example:
```
show red palette:with-saturation 50 ;; [215 113 108]
```



### `palette:saturation`

```NetLogo
palette:saturation
```


Reports a number in the range 0 to 100 that represents the saturation of an agent’s color or pcolor.

Example:
```
crt 1 [set color red]
[palette:saturation] of turtle 0 ;; 80.93

```



### `palette:set-saturation`

```NetLogo
palette:set-saturation number
```



Changes an agent’s saturation leaving the other HSB components unchanged. An error message results if *number* is not in the range from 0 to 100.

Example:
```
ask turtles [palette:set-saturation 30]
```



### `palette:brightness-of`

```NetLogo
palette:brightness-of color
```


Reports a value in the range 0 to 100 representing the brightness component in HSB color space of the given NL, RGB or RGBA *color*.

Example:
```
show palette:brightness-of red ;; 84.314
```



### `palette:with-brightness`

```NetLogo
palette:with-brightness color number
```



Reports an RGBA color with brightness component in HSB color space equal to *number* and the other HSB components of the input *color* unchanged. An error message results if *number* is not in the range 0 to 100.

Example:
```
show red palette:with-brightness 50 ;; [128 30 24]
```



### `palette:brightness`

```NetLogo
palette:brightness
```


Reports a number in the range 0 to 100 that represents the brightness of an agent’s color or pcolor.

Example:
```
crt 1 [set color red]
[palette:brightness] of turtle 0 ;; 84.314

```



### `palette:set-brightness`

```NetLogo
palette:set-brightness number
```



Changes an agent’s brightness leaving the other HSB components unchanged. An error message results if *number* is not in the range from 0 to 100.

Example:
```
ask turtles [palette:set-brightness 30]
```



### `palette:R-of`

```NetLogo
palette:R-of color
```


Reports a value in the range 0 to 255 representing the red component in the RGB color space of the given NL, RGB or RGBA *color*.

Example:
```
show palette:R-of red ;; 215
```



### `palette:with-R`

```NetLogo
palette:with-R color number
```



Reports an RGBA color with red component in RGB color space equal to *number* and the other RGB components of the input *color* unchanged. An error message results if *number* is not in the range 0 to 255.

Example:
```
show red palette:with-R 50 ;; [50 50 41]
```



### `palette:R`

```NetLogo
palette:R
```


Reports a number in the range 0 to 255 that represents the red of an agent’s color or pcolor.

Example:
```
crt 1 [set color red]
[palette:R] of turtle 0 ;; 215

```



### `palette:set-R`

```NetLogo
palette:set-R number
```



Changes an agent’s red component of its RGB/A color leaving the other RGB components unchanged. An error message results if *number* is not in the range from 0 to 255.

Example:
```
ask turtles [palette:set-R 30]
```



### `palette:G-of`

```NetLogo
palette:G-of color
```


Reports a value in the range 0 to 255 representing the green component in the RGB color space of the given NL, RGB or RGBA *color*.

Example:
```
show palette:G-of red ;; 50
```



### `palette:with-G`

```NetLogo
palette:with-G color number
```



Reports an RGBA color with green component in RGB color space equal to *number* and the other RGB components of the input *color* unchanged. An error message results if *number* is not in the range 0 to 255.

Example:
```
show red palette:with-G 56 ;; [215 56 41]
```



### `palette:G`

```NetLogo
palette:G
```


Reports a number in the range 0 to 255 that represents the green of an agent’s color or pcolor.

Example:
```
crt 1 [set color red]
[palette:G] of turtle 0 ;; 50

```



### `palette:set-G`

```NetLogo
palette:set-G number
```



Changes an agent’s green component of its RGB/A color leaving the other RGB components unchanged. An error message results if *number* is not in the range from 0 to 255.

Example:
```
ask turtles [palette:set-G 30]
```



### `palette:B-of`

```NetLogo
palette:B-of color
```


Reports a value in the range 0 to 255 representing the blue component in the RGB color space of the given NL, RGB or RGBA *color*.

Example:
```
show palette:B-of red ;; 41
```



### `palette:with-B`

```NetLogo
palette:with-B color number
```



Reports an RGBA color with blue component in RGB color space equal to *number* and the other RGB components of the input *color* unchanged. An error message results if *number* is not in the range 0 to 255.

Example:
```
show red palette:with-B 56 ;; [215 50 56]
```



### `palette:B`

```NetLogo
palette:B
```


Reports a number in the range 0 to 255 that represents the blue of an agent’s color or pcolor.

Example:
```
crt 1 [set color red]
[palette:B] of turtle 0 ;; 41

```



### `palette:set-B`

```NetLogo
palette:set-B number
```



Changes an agent’s blue component of its RGB/A color leaving the other RGB components unchanged. An error message results if *number* is not in the range from 0 to 255.

Example:
```
ask turtles [palette:set-B 30]
```



### `palette:scale-gradient`

```NetLogo
palette:scale-gradient rgb-color-list number range1 range2
```


Reports an RGB color proportional to *number* using a gradient generated with _rgb-color-list_. The _rgb-color-list_ is a list containing NL or RGB colors: e.g. [NL1 [r1 g1 b1]  [r2 g2 b2] [r3 g3 b3] NL2 …].

When *range1* is less than or equal to *range2*, the color will be directly mapped to gradient colors. When *range2* is less than *range1*, the color gradient is inverted.

Let *min-range* be the minimum of *range1* and *range2*. If *number* is less than or equal to *min-range*, then the result is the same as if *number* was equal to *min-range*.

Let *max-range* be the maximum of *range1* and *range2*. If *number* is greater than *max-range*, then the result is the same as if *number* was equal to *max-range*.

Example:

```
ask patches
[
  set pcolor palette:scale-gradient [[255 0 0] [0 0 255]] pxcor min-pxcor max-pxcor
]

;; colors each patch with a color proportional to the gradient
```



### `palette:scale-gradient-hsb`

```NetLogo
palette:scale-gradient-hsb rgb-color-list number range1 range2
```


Reports an RGB color equivalent to a color in HSB space proportional to *number* using a gradient generated with _hsb-list_. The _hsb-list_ is a list containing three-element lists of HSB colors: [[h1 s1 b1] [h2 s2 b2] [h3 s3 b3]  …].

When *range1* is less than or equal to *range2*, the HSB color will be directly mapped to gradient HSB colors. When range2 is less than range1, the color gradient is inverted.

Let *min-range* be the minimum of *range1* and *range2*. If *number* is less than or equal to *min-range*, then the result is the same as if *number* was equal to *min-range*.

Let *max-range* be the maximum of *range1* and *range2*. If *number* is greater than *max-range*, then the result is the same as if *number* was equal to *max-range*.


  Example:

  ```
  ask patches
  [
  ask patches [set pcolor palette:scale-gradient-hsb [[200 50 50] [100 60 70]] pxcor min-pxcor max-pxcor]
  ]

  ;; colors each patch with a color proportional to the gradient
  ```



### `palette:scheme-colors`

```NetLogo
palette:scheme-colors scheme-type scheme-color number-of-classes
```


Reports a list of RGB colors using ColorBrewer scheme of type *scheme-type* with name *scheme-color* containing *number-of-classes* colors. Scheme types are "Sequential," "Divergent" and "Qualitative." The choice of scheme names depends on the scheme type and includes "Reds", "Spectral" and "Set1". The minimum number of colors is 3 and a maximum is between 8 and 12 depending on the color scheme. For more information go to http://www.colorbrewer.org.

Example:

```
show palette:scheme-colors "Divergent" "Spectral" 3
=> [[252 141 89] [255 255 191] [153 213 148]]


; The scheme-colors primitive can be used with the scale-gradient primitive.
ask patches
  [set pcolor palette:scale-gradient palette:scheme-colors "Divergent" "Spectral" 9 pxcor min-pxcor max-pxcor]
```



### `palette:scale-scheme`

```NetLogo
palette:scale-scheme scheme-type scheme-color number-of-classes number range1 range2
```


Reports an RGB color from the ColorBrewer scheme of type *scheme-type* with name *scheme-color* containing *number-of-classes* colors. The color is chosen using the proportionality of *number* in the range specified by *range1* and *range2*.

When *range1* is less than or equal to *range2*, the colors are ordered as in the ColorBrewer scheme. When *range2* is less than *range1*, the color order is reversed.

Let *min-range* be the minimum of *range1* and *range2*. If *number* is less or equal to than *min-range*, then the result is the same as if *number* was equal to *min-range*. This will be the first color in the color list if *range1* is less than *range2*.

Let *max-range* be the maximum of *range1* and *range2*. If *number* is greater than *max-range*, then the result is the same as if *number* was equal to *max-range*. This will be the last color in the color list if *range1* is less than *range2*.

Example:

```
ask patches
[
  set pcolor palette:scale-scheme "Divergent" "Spectral" 8 pxcor min-pxcor max-pxcor
]

;; colors each patch with a color from the Color Brewer Schemes
```



### `palette:scheme-dialog`

```NetLogo
palette:scheme-dialog
```



Launches a dialog for previewing all the ColorBrewer color schemes.

Use the leftmost pulldown menu to select the scheme type: "Sequential," "Divergent" or "Qualitative." Use the next pulldown menu to select a named scheme. Finally select the number of colors. The maximum number depends on the color scheme. Use the *Copy* button to copy the scheme information into the clipboard. This information can be pasted into commands such as *palette:scheme-colors*. The *Close* button can be used to close the dialog.

Example:

```
  palette:scheme-dialog

  ;; In the dialog the user selects scheme type "Divergent," the scheme named "Spectral," and the number 3, and clicks the *Copy* button.
  ;; In the Command Center the user types "show palette:scheme-colors " and then pastes the clipboard contents, and hits *Enter*.

  show palette:scheme-colors "Divergent" "Spectral" 3

  => [[252 141 89] [255 255 191] [153 213 148]]
```



## References

ColorBrewer [www.colorbrewer.org](http://www.colorbrewer.org)

HARROWER, M. and C. BREWER (2003). [ColorBrewer: An online tool for selecting color schemes for maps](https://www.tandfonline.com/doi/abs/10.1179/000870403235002042). *The Cartographic Journal* 40(1): 27-37. )

HEALEY, C G (2006) [Perception in Visualization](http://www.csc.ncsu.edu/faculty/healey/PP/index.html), (comprehensive review updated regularly).

HEALEY, C G, BOOTH K S, and ENNS, J T (1995). [Visualizing Real-Time Multivariate Data Using Preattentive Processing](https://www.csc2.ncsu.edu/faculty/healey/download/tomacs.95.pdf). *ACM Transactions on Modeling and Computer Simulation* 5, 3, 190-221.

KORNHAUSER, D, WILENSKY, U and RAND, W (1999). [Design Guidelines for Agent Based Model Visualization](https://www.jasss.org/12/2/1.html). *Journal of Artificial Societies and Social Simulation* 12(2)1.

TUFTE, E (1983) The Visual Display of Quantitative Information, Graphics Press.

WARE, C (2004) Information Visualization, 2nd Ed., Morgan Kaufmann.

## Terms of Use

Copyright 1999-2015 by Uri Wilensky, Daniel Kornhauser.

The MIT License

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
