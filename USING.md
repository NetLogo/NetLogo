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
