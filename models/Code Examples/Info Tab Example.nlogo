; The purpose of ths model is to show how to write model documentation in the Info tab.
; Click the Info tab for more information.

to startup
  giant-arrow
end

to giant-arrow
  clear-all
  create-ordered-turtles 1 [ set size 10 set color red setxy -7.25 10 ]
  create-turtles 1 [ set color red setxy -7.25 -5 ]
  ask turtle 0 [ create-link-with turtle 1 [ set color red set thickness 1.5 ] ]
  create-turtles 1 [ set color black setxy 8 12 set label "Click the info tab!" ]
end
@#$#@#$#@
GRAPHICS-WINDOW
210
10
649
470
16
16
13.0
1
20
1
1
1
0
1
1
1
-16
16
-16
16
0
0
1
ticks
30

TEXTBOX
16
84
191
134
Click the Info tab!
20
0.0
1

@#$#@#$#@
## WHAT IS IT?

This model shows you how to use the Info tab.

You edit the Info tab as unformatted plain text.  When you're done editing, the plain text you entered is displayed in a more attractive format.

To control how the formatted display looks, you use a "markup language" called Markdown. You may have encountered Markdown elsewhere; it is used on a number of web sites.  (There are other markup languages in use on the web; for example, Wikipedia used a markup language called MediaWiki.  Different markup languages differ in their details.)

The remainder of this document shows how to use Markdown.

## Headings

Headings are produced by placing one or more hash marks (`#`) at the start of a line. First level headings get one hash, second level headings get two, and so on up to a maximum of four levels.

#### Example

    # First-level heading
    ## Second-level heading
    ### Third-level heading
    #### Fourth-level heading


## Paragraphs

#### Example

    This is a paragraph. There are no spaces before the word 'This'.
    
    This is another paragraph. The first line has two sentences.
    The entire paragraph has two lines and three sentences.

    You don't have to put blank lines around paragraphs.
    You can simply move to the next line.
    It's that easy.

#### Output

This is a paragraph. There are no spaces before the word 'This'.

This is another paragraph. The first line has two sentences. The entire paragraph has two lines and three sentences.

You don't have to put blank lines around paragraphs.
You can simply move to the next line.
It's that easy.

## Italicized and bold text

#### Example

    For italics, surround  text with underscores:
    _hello, world_.

    For bold, surround text with two asterisks:
    **hello, world**.

    You can also combine them:
    _**hello**_ and **_goodbye_**

#### Output

For italics, surround  text with underscores:
_hello, world_.

For bold, surround text with two asterisks:
**hello, world**.

You can also combine them:
_**hello**_ and **_goodbye_**


## Ordered lists

#### Example

    We are about to start an ordered list.

      1. Ordered lists are indented 2 spaces.
        1. Subitems are indented 2 more spaces (4 in all).
      2. The next item in the list starts with the next number.
      3. And so on...

#### Output

We are about to start an ordered list.

  1. Ordered lists are indented 2 spaces.
    1. Subitems are indented 2 more spaces (4 in all for a second level item).
  2. The next item in the list starts with the next number.
  3. And so on...


## Unordered lists

#### Example

    We are about to start an unordered list.

      * Like ordered lists, unordered lists are also indented 2 spaces.
      * Unlike ordered lists, unordered lists use stars instead of numbers.
        * Sub items are indented 2 more spaces.
        * Here's another sub item.

#### Output

We are about to start an unordered list.

  * Like ordered lists, unordered lists are also indented 2 spaces.
  * Unlike ordered lists, unordered lists use stars instead of numbers.
    * Sub items are indented 2 more spaces.
    * Here's another sub item.

## Links

### Automatic links

The simplest way to create a link is to just type it in:

#### Example

    http://ccl.northwestern.edu/netlogo

#### Output

http://ccl.northwestern.edu/netlogo

### Links with text

If you want to use your own text for the link, here's how:

    [link text here](link.address.here)

#### Example

    [NetLogo](http://ccl.northwestern.edu/netlogo)

#### Output

[NetLogo](http://ccl.northwestern.edu/netlogo)

### Local Links

It is also possible to link to a page on your computer, instead of a page somewhere on the Internet. 

Local links have this form:

    [alt text](page-path)

Where page-path has this form:

    file:path/relative/to/model/directory/index.html

The path is relative to the directory that the model file is in.

Any spaces in the name of the file or the path must be converted to %20. For example, this:

    file:path relative/to model directory/the page.html

must be converted to:

    file:path%20relative/to%20model%20directory/the%20page.html

#### Example

The easiest way to link to files on your computer is to put them into the same directory as your model. Assuming you have a file named `index.html` in the same directory as your model, the link would look like this:

    [Home](file:index.html)

#### Example

Here is another example where the file lives in a directory called docs, and docs is in the same directory as your model:

    [Home](file:docs/index.html)

## Images

Images are very similar to links, but have an exclamation point in front:

    ![alt text](http://location/of/image)

(The alternate text is the text that gets displayed if the image is not found.)

#### Example

    ![NetLogo](http://ccl.northwestern.edu/netlogo/images/netlogo-title-new.jpg)

#### Output

![NetLogo](http://ccl.northwestern.edu/netlogo/images/netlogo-title-new.jpg)

### Local Images

Also very similar to links, it is possible to display an image on your computer instead of an image somewhere on the Internet. Assuming you have an image named `image.jpg`, local images look like this:

    ![Alt Text](image-path)

Where image-path has this form:

    file:path/relative/to/model/directory/image.jpg

The path is relative to the directory that the model file is in.

As with local links, any spaces in the name of the file or the path must be converted to %20. For example, this:

    file:path relative/to model directory/the image.jpg

must be converted to:

    file:path%20relative/to%20model%20directory/the%20image.jpg

#### Example

Like local links, the easiest way to display images on your computer is to put them into the same directory as your model. This example displays the image "Perspective Example.png", which resides in the same directory as this model (Info Tab Example).

    ![Example](file:Perspective%20Example.png)

#### Output

![Example](file:Perspective%20Example.png)

## Block quotations

Consecutive lines starting with > will become block quotations.
You can put whatever text you like inside of it and you can also style it.

#### Example

    > Let me see: four times five is twelve, and four times six is thirteen,
    > and four times seven is --- _oh dear!_
    > I shall never get to twenty at that rate!

#### Output

> Let me see: four times five is twelve, and four times six is thirteen,
> and four times seven is --- _oh dear!_
> I shall never get to twenty at that rate!

## Code

To put code in a sentence, simply surround it with backticks (`).

#### Example

    You can create a single turtle with the `crt 1` command.

#### Output

You can create a single turtle with the `crt 1` command.


## Code blocks (Preformatted text)

It is also possible to have blocks of code. To create a code block, indent every line of the block by 4 spaces. This is also useful for diagrams and formulas.

#### Example

    About to start the code block.
    Leave a blank line after this one, and then indent four spaces:

        ; a typical go procedure
        to go
          ask turtles
            [ fd 1 ]
          tick
        end

#### Output

About to start the code block.
Leave a blank line after this one, and then indent four spaces:

    ; a typical go procedure
    to go
      ask turtles
        [ fd 1 ]
      tick
    end


## Superscripts and subscripts

Superscripts and subscripts are useful for writing formulas, equations, footnotes and more. Subscripts appear half a character below the baseline, and are written using the HTML tag `<sub>`. Superscripts appear half a character above the baseline, and are written using the HTML tag `<sup>`.

#### Example

    H<sub>2</sub>O

    2x<sup>4</sup> + x<sup>2</sup>

    WWW<sup>[1]</sup>

#### Output

H<sub>2</sub>O

2x<sup>4</sup> + x<sup>2</sup> + 42

WWW<sup>[1]</sup>

## Notes on usage

 * Paragraphs, lists, code blocks and other features should be separated from each other with a blank line.  If you find that something isn't formatted the way you expected, it might be because you need to add a blank lines before or after it.

 * If you need to prevent a special character from being treated as a markup, put a backslash (`\`) before it.

 * We use GitHub flavored newlines (http://github.github.com/github-flavored-markdown/) instead of traditional Markdown handling of newlines. This means that newlines are treated as real line breaks, instead of being being combined with the previous line into a single paragraph. We believe this is more intuitive.

## Other features

Markdown has additional features that we have not shown here.

We have tested the features shown above on a variety of systems.  If you use other Markdown features, you may find that they work on your computer, or not.  Even a feature that works on your computer might work differently, or not work at all, for someone with a different operating system or Java virtual machine.

If you want all NetLogo users to be able to read your writing, use only the features shown above.

More information about Markdown is at http://daringfireball.net/projects/markdown/. For rendering Markdown, NetLogo uses [pegdown](http://github.com/sirthias/pegdown).

[netlogo-link]: http://ccl.northwestern.edu/netlogo
@#$#@#$#@
default
true
0
Polygon -7500403 true true 150 5 40 250 150 205 260 250

airplane
true
0
Polygon -7500403 true true 150 0 135 15 120 60 120 105 15 165 15 195 120 180 135 240 105 270 120 285 150 270 180 285 210 270 165 240 180 180 285 195 285 165 180 105 180 60 165 15

arrow
true
0
Polygon -7500403 true true 150 0 0 150 105 150 105 293 195 293 195 150 300 150

box
false
0
Polygon -7500403 true true 150 285 285 225 285 75 150 135
Polygon -7500403 true true 150 135 15 75 150 15 285 75
Polygon -7500403 true true 15 75 15 225 150 285 150 135
Line -16777216 false 150 285 150 135
Line -16777216 false 150 135 15 75
Line -16777216 false 150 135 285 75

bug
true
0
Circle -7500403 true true 96 182 108
Circle -7500403 true true 110 127 80
Circle -7500403 true true 110 75 80
Line -7500403 true 150 100 80 30
Line -7500403 true 150 100 220 30

butterfly
true
0
Polygon -7500403 true true 150 165 209 199 225 225 225 255 195 270 165 255 150 240
Polygon -7500403 true true 150 165 89 198 75 225 75 255 105 270 135 255 150 240
Polygon -7500403 true true 139 148 100 105 55 90 25 90 10 105 10 135 25 180 40 195 85 194 139 163
Polygon -7500403 true true 162 150 200 105 245 90 275 90 290 105 290 135 275 180 260 195 215 195 162 165
Polygon -16777216 true false 150 255 135 225 120 150 135 120 150 105 165 120 180 150 165 225
Circle -16777216 true false 135 90 30
Line -16777216 false 150 105 195 60
Line -16777216 false 150 105 105 60

car
false
0
Polygon -7500403 true true 300 180 279 164 261 144 240 135 226 132 213 106 203 84 185 63 159 50 135 50 75 60 0 150 0 165 0 225 300 225 300 180
Circle -16777216 true false 180 180 90
Circle -16777216 true false 30 180 90
Polygon -16777216 true false 162 80 132 78 134 135 209 135 194 105 189 96 180 89
Circle -7500403 true true 47 195 58
Circle -7500403 true true 195 195 58

circle
false
0
Circle -7500403 true true 0 0 300

circle 2
false
0
Circle -7500403 true true 0 0 300
Circle -16777216 true false 30 30 240

cow
false
0
Polygon -7500403 true true 200 193 197 249 179 249 177 196 166 187 140 189 93 191 78 179 72 211 49 209 48 181 37 149 25 120 25 89 45 72 103 84 179 75 198 76 252 64 272 81 293 103 285 121 255 121 242 118 224 167
Polygon -7500403 true true 73 210 86 251 62 249 48 208
Polygon -7500403 true true 25 114 16 195 9 204 23 213 25 200 39 123

cylinder
false
0
Circle -7500403 true true 0 0 300

dot
false
0
Circle -7500403 true true 90 90 120

face happy
false
0
Circle -7500403 true true 8 8 285
Circle -16777216 true false 60 75 60
Circle -16777216 true false 180 75 60
Polygon -16777216 true false 150 255 90 239 62 213 47 191 67 179 90 203 109 218 150 225 192 218 210 203 227 181 251 194 236 217 212 240

face neutral
false
0
Circle -7500403 true true 8 7 285
Circle -16777216 true false 60 75 60
Circle -16777216 true false 180 75 60
Rectangle -16777216 true false 60 195 240 225

face sad
false
0
Circle -7500403 true true 8 8 285
Circle -16777216 true false 60 75 60
Circle -16777216 true false 180 75 60
Polygon -16777216 true false 150 168 90 184 62 210 47 232 67 244 90 220 109 205 150 198 192 205 210 220 227 242 251 229 236 206 212 183

fish
false
0
Polygon -1 true false 44 131 21 87 15 86 0 120 15 150 0 180 13 214 20 212 45 166
Polygon -1 true false 135 195 119 235 95 218 76 210 46 204 60 165
Polygon -1 true false 75 45 83 77 71 103 86 114 166 78 135 60
Polygon -7500403 true true 30 136 151 77 226 81 280 119 292 146 292 160 287 170 270 195 195 210 151 212 30 166
Circle -16777216 true false 215 106 30

flag
false
0
Rectangle -7500403 true true 60 15 75 300
Polygon -7500403 true true 90 150 270 90 90 30
Line -7500403 true 75 135 90 135
Line -7500403 true 75 45 90 45

flower
false
0
Polygon -10899396 true false 135 120 165 165 180 210 180 240 150 300 165 300 195 240 195 195 165 135
Circle -7500403 true true 85 132 38
Circle -7500403 true true 130 147 38
Circle -7500403 true true 192 85 38
Circle -7500403 true true 85 40 38
Circle -7500403 true true 177 40 38
Circle -7500403 true true 177 132 38
Circle -7500403 true true 70 85 38
Circle -7500403 true true 130 25 38
Circle -7500403 true true 96 51 108
Circle -16777216 true false 113 68 74
Polygon -10899396 true false 189 233 219 188 249 173 279 188 234 218
Polygon -10899396 true false 180 255 150 210 105 210 75 240 135 240

house
false
0
Rectangle -7500403 true true 45 120 255 285
Rectangle -16777216 true false 120 210 180 285
Polygon -7500403 true true 15 120 150 15 285 120
Line -16777216 false 30 120 270 120

leaf
false
0
Polygon -7500403 true true 150 210 135 195 120 210 60 210 30 195 60 180 60 165 15 135 30 120 15 105 40 104 45 90 60 90 90 105 105 120 120 120 105 60 120 60 135 30 150 15 165 30 180 60 195 60 180 120 195 120 210 105 240 90 255 90 263 104 285 105 270 120 285 135 240 165 240 180 270 195 240 210 180 210 165 195
Polygon -7500403 true true 135 195 135 240 120 255 105 255 105 285 135 285 165 240 165 195

line
true
0
Line -7500403 true 150 0 150 300

line half
true
0
Line -7500403 true 150 0 150 150

pentagon
false
0
Polygon -7500403 true true 150 15 15 120 60 285 240 285 285 120

person
false
0
Circle -7500403 true true 110 5 80
Polygon -7500403 true true 105 90 120 195 90 285 105 300 135 300 150 225 165 300 195 300 210 285 180 195 195 90
Rectangle -7500403 true true 127 79 172 94
Polygon -7500403 true true 195 90 240 150 225 180 165 105
Polygon -7500403 true true 105 90 60 150 75 180 135 105

plant
false
0
Rectangle -7500403 true true 135 90 165 300
Polygon -7500403 true true 135 255 90 210 45 195 75 255 135 285
Polygon -7500403 true true 165 255 210 210 255 195 225 255 165 285
Polygon -7500403 true true 135 180 90 135 45 120 75 180 135 210
Polygon -7500403 true true 165 180 165 210 225 180 255 120 210 135
Polygon -7500403 true true 135 105 90 60 45 45 75 105 135 135
Polygon -7500403 true true 165 105 165 135 225 105 255 45 210 60
Polygon -7500403 true true 135 90 120 45 150 15 180 45 165 90

sheep
false
0
Rectangle -7500403 true true 151 225 180 285
Rectangle -7500403 true true 47 225 75 285
Rectangle -7500403 true true 15 75 210 225
Circle -7500403 true true 135 75 150
Circle -16777216 true false 165 76 116

square
false
0
Rectangle -7500403 true true 30 30 270 270

square 2
false
0
Rectangle -7500403 true true 30 30 270 270
Rectangle -16777216 true false 60 60 240 240

star
false
0
Polygon -7500403 true true 151 1 185 108 298 108 207 175 242 282 151 216 59 282 94 175 3 108 116 108

target
false
0
Circle -7500403 true true 0 0 300
Circle -16777216 true false 30 30 240
Circle -7500403 true true 60 60 180
Circle -16777216 true false 90 90 120
Circle -7500403 true true 120 120 60

tree
false
0
Circle -7500403 true true 118 3 94
Rectangle -6459832 true false 120 195 180 300
Circle -7500403 true true 65 21 108
Circle -7500403 true true 116 41 127
Circle -7500403 true true 45 90 120
Circle -7500403 true true 104 74 152

triangle
false
0
Polygon -7500403 true true 150 30 15 255 285 255

triangle 2
false
0
Polygon -7500403 true true 150 30 15 255 285 255
Polygon -16777216 true false 151 99 225 223 75 224

truck
false
0
Rectangle -7500403 true true 4 45 195 187
Polygon -7500403 true true 296 193 296 150 259 134 244 104 208 104 207 194
Rectangle -1 true false 195 60 195 105
Polygon -16777216 true false 238 112 252 141 219 141 218 112
Circle -16777216 true false 234 174 42
Rectangle -7500403 true true 181 185 214 194
Circle -16777216 true false 144 174 42
Circle -16777216 true false 24 174 42
Circle -7500403 false true 24 174 42
Circle -7500403 false true 144 174 42
Circle -7500403 false true 234 174 42

turtle
true
0
Polygon -10899396 true false 215 204 240 233 246 254 228 266 215 252 193 210
Polygon -10899396 true false 195 90 225 75 245 75 260 89 269 108 261 124 240 105 225 105 210 105
Polygon -10899396 true false 105 90 75 75 55 75 40 89 31 108 39 124 60 105 75 105 90 105
Polygon -10899396 true false 132 85 134 64 107 51 108 17 150 2 192 18 192 52 169 65 172 87
Polygon -10899396 true false 85 204 60 233 54 254 72 266 85 252 107 210
Polygon -7500403 true true 119 75 179 75 209 101 224 135 220 225 175 261 128 261 81 224 74 135 88 99

wheel
false
0
Circle -7500403 true true 3 3 294
Circle -16777216 true false 30 30 240
Line -7500403 true 150 285 150 15
Line -7500403 true 15 150 285 150
Circle -7500403 true true 120 120 60
Line -7500403 true 216 40 79 269
Line -7500403 true 40 84 269 221
Line -7500403 true 40 216 269 79
Line -7500403 true 84 40 221 269

x
false
0
Polygon -7500403 true true 270 75 225 30 30 225 75 270
Polygon -7500403 true true 30 75 75 30 270 225 225 270

@#$#@#$#@
NetLogo 5.0beta2
@#$#@#$#@
need-to-manually-make-preview-for-this-model
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
default
0.0
-0.2 0 1.0 0.0
0.0 1 1.0 0.0
0.2 0 1.0 0.0
link direction
true
0
Line -7500403 true 150 150 90 180
Line -7500403 true 150 150 210 180

@#$#@#$#@
0
@#$#@#$#@
