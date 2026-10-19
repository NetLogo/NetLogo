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
