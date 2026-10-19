## Using

This extension adds GIS (Geographic Information Systems) support to
NetLogo. It provides the ability to load vector GIS data (points,
lines, and polygons), and raster GIS data (grids) into your model.

The extension supports vector data in the form of ESRI shapefiles and
GeoJSON files.  The shapefile (.shp) and GeoJSON (.geojson) formats
are the most common format for storing and exchanging vector GIS data.
The extension supports raster data in the form of ESRI ASCII Grid
files.  The ASCII grid file (.asc or .grd) is not as common as the
shapefile, but is supported as an interchange format by most GIS
platforms.

### How to use

In general, you first define a transformation between GIS data space
and NetLogo space, then load datasets and perform various operations
on them. The easiest way to define a transformation between GIS space
and NetLogo space is to take the union of the "envelopes"
or bounding rectangles of all of your datasets in GIS space and map
that directly to the bounds of the NetLogo world. See GIS General
Examples for an example of this technique.

You may also optionally define a projection for the GIS space, in
which case datasets will be re-projected to match that projection as
they are loaded, as long as each of your data files has an associated
.prj file that describes the projection or geographic coordinate
system of the data. If no associated .prj file is found, the
extension will assume that the dataset already uses the current
projection, regardless of what that projection is.

Once the coordinate system is defined, you can load datasets using
[gis:load-dataset](https://docs.netlogo.org/gis#load-dataset). This primitive
reports either a VectorDataset or a RasterDataset, depending on what
type of file you pass it.

A VectorDataset consists of a collection of VectorFeatures, each one
of which is a point, line, or polygon, along with a set of property
values. A single VectorDataset may contain only one of the three
possible types of features.

There are several things you can do with a VectorDataset: ask it for
the names of the properties of its features, ask it for its
"envelope" (bounding rectangle), ask for a list of all
VectorFeatures in the dataset, search for a single VectorFeature or
list of VectorFeatures whose value for a particular property is less
than or greater than a particular value, or lies within a given
range, or matches a given string using wildcard matching
("*", which matches any number of occurrences of any
characters). If the VectorFeatures are polygons, you can also apply
the values of a particular property of the dataset's features to
a given patch variable.

For the common use case of converting a VectorDataset of points into
a corresponding set of turtles with the same attributes, the
[gis:create-turtles-from-points](https://docs.netlogo.org/gis#create-turtles-from-points)
primitive should be used.

There are also several things you can do with a VectorFeature from a
VectorDataset: ask it for a list of vertex lists, ask it for a
property value by name, ask it for its centroid (center of gravity),
and ask for a subset of a given agentset whose agents intersect the
given VectorFeature. For point data, each vertex list will be a
one-element list. For line data, each vertex list will represent the
vertices of a line that makes up that feature. For polygon data, each
vertex list will represent one "ring" of the polygon, and
the first and last vertex of the list will be the same. The vertex
lists are made up of values of type Vertex, and the centroid will be
a value of type Vertex as well.

For the common use case of spawning a number of turtles inside the
bounds of a Polygon VectorFeature, the
[gis:create-turtles-inside-polygon](https://docs.netlogo.org/gis#create-turtles-inside-polygon)
primitive should be used. Though the
[gis:random-point-inside](https://docs.netlogo.org/gis#random-point-inside) primitive can
also be used if you don't want the spawned turtles to take on
the attributes of the Polygon.

There are a number of operations defined for RasterDatasets as well.
Mostly these involve sampling the values in the dataset, or
re-sampling a raster to a different resolution. You can also apply a
raster to a given patch variable, and convolve a raster using an
arbitrary convolution matrix.

>  **Code Example:** GIS General Examples has general examples of
>  how to use the extension

>  **Code Example:** GIS Gradient Example is a more advanced
>  example of raster dataset analysis.

### Known Issues

Values of type RasterDataset, VectorDataset, VectorFeature, and
Vertex are not handled properly by `export-world` and
`import-world`. To save datasets, you must use the
`gis:store-dataset` primitive.

There is currently no way to distinguish positive-area
"shell" polygons from negative-area "hole"
polygons, or to determine which holes are associated with which
shells.

### Credits

The primary developer of the GIS extension was Eric Russell.

Significant updates, features, and fixes were added by James Hovet in 2020 and 2021.

The GIS extension makes use of several open-source software
libraries. For copyright and license information on those, see the
[copyright](http://ccl.northwestern.edu/netlogo/docs/copyright.html) section of the manual. The
extension also contains elements borrowed from <a href="https://myworldgis.org" target="_blank">My World GIS</a>.

This documentation and the example NetLogo models are in the public
domain. The GIS extension itself is free and open source software.
See the README.md file in the extension/gis directory for details.

We would love to hear your suggestions on how to improve the GIS
extension, or just about what you're using it for. Post questions
and comments at the
<a href="https://groups.google.com/d/forum/netlogo-users" target="_blank">NetLogo Users Group</a>, or write directly to Eric Russell and the NetLogo team at
[ccl-gis@ccl.northwestern.edu](mailto:ccl-gis@ccl.northwestern.edu)
