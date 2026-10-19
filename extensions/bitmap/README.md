
# Bitmap Extension for NetLogo

This package contains the NetLogo bitmap extension. It allows you to perform manipulations like scaling, converting to grayscale, and grabbing a single channel on images and import them into the patches or drawing.

## Building

Just run `sbt package` or start an sbt session with `sbt`, then run `package` inside of that.  If compilation succeeds, `bitmap.jar` will be created in the root of the repository.

## Using

The bitmap extension is pre-installed in NetLogo. For instructions on using it, or for more information about NetLogo extensions, see the NetLogo User Manual.

### What does the Bitmap Extension do?

The Bitmap Extension allows you to manipulate and import images into
the drawing and patches. It offers features not provided by the
NetLogo core primitives, such as: scaling, manipulation of different
color channels, and width and height reporters.

### Getting started

To import and manipulate images you will need to include the bitmap
extension in your NetLogo model.

```NetLogo
extensions[ bitmap ]
```

The image file formats supported are determined by your Java virtual
machine's imageio library. Typically this is PNG, JPG, GIF, and
BMP. PNG is a good, standard choice that is likely to work
everywhere.

If the image format supports transparency (alpha), that information
will be imported as well.

## Primitives

[`bitmap:average-color`](#bitmapaverage-color)
[`bitmap:channel`](#bitmapchannel)
[`bitmap:copy-to-drawing`](#bitmapcopy-to-drawing)
[`bitmap:copy-to-pcolors`](#bitmapcopy-to-pcolors)
[`bitmap:difference-rgb`](#bitmapdifference-rgb)
[`bitmap:export`](#bitmapexport)
[`bitmap:from-base64`](#bitmapfrom-base64)
[`bitmap:to-base64`](#bitmapto-base64)
[`bitmap:from-view`](#bitmapfrom-view)
[`bitmap:to-grayscale`](#bitmapto-grayscale)
[`bitmap:height`](#bitmapheight)
[`bitmap:import`](#bitmapimport)
[`bitmap:scaled`](#bitmapscaled)
[`bitmap:width`](#bitmapwidth)


### `bitmap:average-color`

```NetLogo
bitmap:average-color image
```


Reports a 3-element list describing the amount of R, G, and B in
*image*, by summing across all pixels, and normalizing each
component by the number of pixels in the image, so each component
ranges from 0 to 255.



### `bitmap:channel`

```NetLogo
bitmap:channel image channel
```


Extracts either the alpha, red, green, or blue channel from an image.
The input *channel* should be an integer 0-3 indicating the
channel to remove (alpha=0, red=1, green=2, blue=3). The resulting
image is a grayscale image representing specified channel.



### `bitmap:copy-to-drawing`

```NetLogo
bitmap:copy-to-drawing image x y
```


Imports the given image into the drawing without scaling the image at
the given pixel coordinates.



### `bitmap:copy-to-pcolors`

```NetLogo
bitmap:copy-to-pcolors image boolean
```


Imports the given image into the pcolors, scaled to fit the world.
The second input indicates whether the colors should be interpreted
as NetLogo colors or left as RGB colors. <code>false</code> means RGB
colors.



### `bitmap:difference-rgb`

```NetLogo
bitmap:difference-rgb image1 image2
```


Reports an image that is the absolute value of the pixel-wise RGB
difference between two images. Note that image1 and image2 MUST be
the same width and height as each other, or errors will ensue.



### `bitmap:export`

```NetLogo
bitmap:export image filename
```

Writes *image* to *filename*.


### `bitmap:from-base64`

```NetLogo
bitmap:from-base64 base64-string
```

Turns a base64 encoded string into a bitmap image for use by the extension.  The [Fetch](https://github.com/NetLogo/Fetch-Extension) and [Export-The](https://github.com/NetLogo/ExportThe-Extension) extensions would be common sources of these encoded strings.


### `bitmap:to-base64`

```NetLogo
bitmap:to-base64 base64-string
```

Turns a bitmap image into a base64 encoded string with a PNG format.


### `bitmap:from-view`

```NetLogo
bitmap:from-view
```

Reports an image of the current view.


### `bitmap:to-grayscale`

```NetLogo
bitmap:to-grayscale image
```

Converts the given image to grayscale.


### `bitmap:height`

```NetLogo
bitmap:height image
```

Reports the height of given *image*


### `bitmap:import`

```NetLogo
bitmap:import filename
```

Reports a LogoBitmap containing the image at *filename*.


### `bitmap:scaled`

```NetLogo
bitmap:scaled image width height
```

Reports a new image that is *image* scaled to the given *width* and *height*


### `bitmap:width`

```NetLogo
bitmap:width image
```

Reports the width of the given *image*


## Terms of Use

[![CC0](http://i.creativecommons.org/p/zero/1.0/88x31.png)](http://creativecommons.org/publicdomain/zero/1.0/)

The NetLogo bitmap extension is in the public domain.  To the extent possible under law, Uri Wilensky has waived all copyright and related or neighboring rights.
