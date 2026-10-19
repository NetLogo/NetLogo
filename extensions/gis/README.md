
# GIS Extension for NetLogo

This package contains the NetLogo GIS extension.

## Building

```
sbt package
```

If compilation succeeds, `gis.jar` will be created.

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

## Primitives

### RasterDataset Primitives

[`gis:width-of`](#giswidth-of)
[`gis:height-of`](#gisheight-of)
[`gis:raster-value`](#gisraster-value)
[`gis:set-raster-value`](#gisset-raster-value)
[`gis:minimum-of`](#gisminimum-of)
[`gis:maximum-of`](#gismaximum-of)
[`gis:sampling-method-of`](#gissampling-method-of)
[`gis:set-sampling-method`](#gisset-sampling-method)
[`gis:raster-sample`](#gisraster-sample)
[`gis:raster-world-envelope`](#gisraster-world-envelope)
[`gis:create-raster`](#giscreate-raster)
[`gis:resample`](#gisresample)
[`gis:convolve`](#gisconvolve)
[`gis:apply-raster`](#gisapply-raster)

### Dataset Primitives

[`gis:load-dataset`](#gisload-dataset)
[`gis:load-dataset-from-string`](#gisload-dataset-from-string)
[`gis:store-dataset`](#gisstore-dataset)
[`gis:store-dataset-to-string`](#gisstore-dataset-to-string)
[`gis:store-dataset-to-strings`](#gisstore-dataset-to-strings)
[`gis:type-of`](#gistype-of)
[`gis:patch-dataset`](#gispatch-dataset)
[`gis:turtle-dataset`](#gisturtle-dataset)
[`gis:link-dataset`](#gislink-dataset)

### VectorDataset Primitives

[`gis:shape-type-of`](#gisshape-type-of)
[`gis:property-names`](#gisproperty-names)
[`gis:feature-list-of`](#gisfeature-list-of)
[`gis:vertex-lists-of`](#gisvertex-lists-of)
[`gis:centroid-of`](#giscentroid-of)
[`gis:random-point-inside`](#gisrandom-point-inside)
[`gis:location-of`](#gislocation-of)
[`gis:set-property-value`](#gisset-property-value)
[`gis:property-value`](#gisproperty-value)
[`gis:find-features`](#gisfind-features)
[`gis:find-one-feature`](#gisfind-one-feature)
[`gis:find-less-than`](#gisfind-less-than)
[`gis:find-greater-than`](#gisfind-greater-than)
[`gis:find-range`](#gisfind-range)
[`gis:property-minimum`](#gisproperty-minimum)
[`gis:property-maximum`](#gisproperty-maximum)
[`gis:apply-coverage`](#gisapply-coverage)
[`gis:create-turtles-from-points`](#giscreate-turtles-from-points)
[`gis:create-turtles-from-points-manual`](#giscreate-turtles-from-points-manual)
[`gis:create-turtles-inside-polygon`](#giscreate-turtles-inside-polygon)
[`gis:create-turtles-inside-polygon-manual`](#giscreate-turtles-inside-polygon-manual)
[`gis:coverage-minimum-threshold`](#giscoverage-minimum-threshold)
[`gis:set-coverage-minimum-threshold`](#gisset-coverage-minimum-threshold)
[`gis:coverage-maximum-threshold`](#giscoverage-maximum-threshold)
[`gis:set-coverage-maximum-threshold`](#gisset-coverage-maximum-threshold)
[`gis:intersects?`](#gisintersects?)
[`gis:contains?`](#giscontains?)
[`gis:contained-by?`](#giscontained-by?)
[`gis:have-relationship?`](#gishave-relationship?)
[`gis:relationship-of`](#gisrelationship-of)
[`gis:intersecting`](#gisintersecting)

### Coordinate System Primitives

[`gis:set-transformation`](#gisset-transformation)
[`gis:set-transformation-ds`](#gisset-transformation-ds)
[`gis:set-world-envelope`](#gisset-world-envelope)
[`gis:set-world-envelope-ds`](#gisset-world-envelope-ds)
[`gis:world-envelope`](#gisworld-envelope)
[`gis:envelope-of`](#gisenvelope-of)
[`gis:envelope-union-of`](#gisenvelope-union-of)
[`gis:load-coordinate-system`](#gisload-coordinate-system)
[`gis:set-coordinate-system`](#gisset-coordinate-system)
[`gis:project-lat-lon`](#gisproject-lat-lon)
[`gis:project-lat-lon-from-ellipsoid`](#gisproject-lat-lon-from-ellipsoid)

### Drawing Primitives

[`gis:drawing-color`](#gisdrawing-color)
[`gis:set-drawing-color`](#gisset-drawing-color)
[`gis:draw`](#gisdraw)
[`gis:fill`](#gisfill)
[`gis:paint`](#gispaint)
[`gis:import-wms-drawing`](#gisimport-wms-drawing)



### `gis:set-transformation`

```NetLogo
gis:set-transformation *gis-envelope* *netlogo-envelope*
```


Defines a mapping between GIS coordinates and NetLogo coordinates.
The *gis-envelope* and *netlogo-envelope* parameters must
each be four-element lists consisting of:

```
[minimum-x maximum-x minimum-y maximum-y]
```

The scale of the transformation will be equal to the minimum of the
scale necessary to make the mapping between the ranges of x values
and the scale necessary to make the mapping between the ranges of y
values. The GIS space will be centered in NetLogo space.

For example, the following two lists would map all of geographic
(latitude and longitude) space in degrees to NetLogo world space,
regardless of the current dimensions of the NetLogo world:

```
(list -180 180 -90 90)
(list min-pxcor max-pxcor min-pycor max-pycor)
```

However, if you're setting the envelope of the NetLogo world,
you should probably be using [set-world-envelope](#gisset-world-envelope).



### `gis:set-transformation-ds`

```NetLogo
gis:set-transformation-ds *gis-envelope* *netlogo-envelope*
```


Does the same thing as [set-transformation](#gisset-transformation) above, except that
it allows the scale for mapping the range of x values to be
different than the scale for y values. The "-ds" on the
end stands for "different scales". Using different scales
will cause distortion of the shape of GIS features, and so it is
generally not recommended, but it may be useful for some models.

Here is an example of the difference between [set-transformation](#gisset-transformation) and [set-transformation-ds](#gisset-transformation-ds):

<table width="80%" border="1" rules="cols" style="border-collaps: separate; border-spacing: 4px; text-align: center; margin: 0 auto;">
<tr>
  <td style="padding: 8px">
    <img alt="" src="images/set-transformation.png" width="200">
  <td style="padding: 8px">
    <img alt="" src="images/set-transformation-ds.png" width="200">
  <tr>
  <td style="padding: 8px">
    Using [set-transformation](#gisset-transformation),
    the scale along the x and y axis is the same, preserving the
    round shape of the Earth in this Orthographic projection.
  <td style="padding: 8px">
    Using [set-transformation-ds](#gisset-transformation-ds), the
    scale along the x axis is stretched so that the earth covers
    the entire NetLogo View, which in this case distorts the shape
    of the Earth.
</table>



### `gis:set-world-envelope`

```NetLogo
gis:set-world-envelope *gis-envelope*
```


A shorthand for setting the transformation by mapping the envelope
of the NetLogo world to the given envelope in GIS space, while
keeping the scales along the x and y axis the same. It is
equivalent to:

```
set-transformation gis-envelope (list min-pxcor max-pxcor min-pycor max-pycor)
```

This primitive is supplied because most of the time you'll want
to set the envelope of the entire NetLogo world, rather than just a
part of it.



### `gis:set-world-envelope-ds`

```NetLogo
gis:set-world-envelope-ds *gis-envelope*
```


A shorthand for setting the transformation by mapping the envelope
of the NetLogo world to the given envelope in GIS space, using
different scales along the x and y axis if necessary. It is
equivalent to:

```
set-transformation-ds gis-envelope (list min-pxcor max-pxcor min-pycor max-pycor)
```

See the [pictures](#transformation-example) above for
the difference between using equal scales for x and y coordinates
and using different scales.



### `gis:world-envelope`

```NetLogo
gis:world-envelope
```


Reports the envelope (bounding rectangle) of the NetLogo world,
transformed into GIS space. An envelope consists of a four-element
list of the form:

```
[minimum-x maximum-x minimum-y maximum-y]
```




### `gis:envelope-of`

```NetLogo
gis:envelope-of *thing*
```


Reports the envelope (bounding rectangle) of *thing* in GIS
coordinates. The *thing* may be an Agent, an AgentSet, a
RasterDataset, a VectorDataset, or a VectorFeature. An envelope
consists of a four-element list of the form:

```
[minimum-x maximum-x minimum-y maximum-y]
```




### `gis:envelope-union-of`

```NetLogo
gis:envelope-union-of *envelope1* *envelope2*
(gis:envelope-union-of *envelope1...*)
```


Reports an envelope (bounding rectangle) that entirely contains the
given envelopes. An envelope consists of a four-element list of the
form

```
[minimum-x maximum-x minimum-y maximum-y]
```

No assumption is made about the coordinate system of the arguments,
though if they are not in the same coordinate system, results will
be unpredictable.



### `gis:load-coordinate-system`

```NetLogo
gis:load-coordinate-system *file*
```


Loads a new global projection used for projecting or re- projecting
GIS data as it is loaded from a file. The file must contain a valid
<a href="https://www.geoapi.org/3.0/javadoc/org/opengis/referencing/doc-files/WKT.html" target="_blank">
Well-Known Text (WKT)</a> projection description.

WKT projection files are frequently distributed alongside GIS data
files, and usually have a ".prj" filename extension.

Relative paths are resolved relative to the location of the current
model, or the user's home directory if the current model
hasn't been saved yet.

The GIS extension does not support all WKT coordinate systems and
projections. Only geographic (`"GEOGCS"`) and
projected (`"PROJCS"`) coordinate systems are
supported. For projected coordinate systems, only the following
projections are supported:

* Albers_Conic_Equal_Area
* Lambert_Conformal_Conic_2SP
* Polyconic
* Lambert_Azimuthal_Equal_Area
* Mercator_1SP
* Robinson
* Azimuthal_Equidistant
* Miller
* Stereographic
* Cylindrical_Equal_Area
* Oblique_Mercator
* Transverse_Mercator
* Equidistant_Conic
* hotine_oblique_mercator
* Gnomonic
* Orthographic

See <a href="http://remotesensing.org/geotiff/proj_list/" target="_blank">remotesensing.org</a>
for a complete list of WKT projections and their parameters.



### `gis:set-coordinate-system`

```NetLogo
gis:set-coordinate-system *system*
```


Sets the global projection used for projecting or re- projecting
GIS data as it is loaded. The *system* must be either a string
in <a href="http://www.geoapi.org/3.0/javadoc/org/opengis/referencing/doc-files/WKT.html" target="_blank">
Well-Known Text (WKT) format</a>, or a NetLogo list that consists
of WKT converted to a list by moving each keyword inside its
associated brackets and putting quotes around it. The latter is
preferred because it makes the code much more readable.

The same limitations on WKT support apply as described above in the
documentation for [load-coordinate-system](#gisload-coordinate-system)



### `gis:project-lat-lon`

```NetLogo
gis:project-lat-lon *latitude* *longitude*
```


Report the position, in NetLogo space, of the given latitude
and longitude pair according to the current map projection and
transformation.

Like the `location-of` primitive, the reported xcor and ycor
values are reported in a two-item list of `[xcor ycor]`
and an empty list if the specified point is outside of
the bounds of the netlogo world.
For instance:
```
let location-of-abbey-road-studios gis:project-lat-lon 51.5320787 -0.1802646
let abbey-road-xcor item 0 location-of-abbey-road-studios
let abbey-road-ycor item 1 location-of-abbey-road-studios
```

Note that this primitive assumes that the given lat/lon pair
are relative to the WGS84 datum/ellipsoid. If your
data is based on GPS observations or GeoJson files, then your
data is already relative to WGS84. If you are unsure about
what datum your data is, then you should probably just assume
it is WGS84 and use this primitive. However, if you do know
that your data is relative to a different datum and that
extra degree of precision is important to you (if you are,
say, comparing values from location-of and project-lat-lon)
then you should use `project-lat-lon-from-ellipsoid` and
specify the desired datum's ellipsoid.



### `gis:project-lat-lon-from-ellipsoid`

```NetLogo
gis:project-lat-lon-from-ellipsoid *latitude* *longitude* *ellipsoid-radius* *ellipsoid-inverse-flattening*
```


Report the position, in NetLogo space, of the given latitude
and longitude pair according to the current map projection and
transformation and the given ellipsoid parameters.

Like the `location-of` primitive, the reported xcor and ycor
values are reported in a two-item list of `[xcor ycor]`
and an empty list if the specified point is outside of
the bounds of the netlogo world.

The two defining parameters of a  ellipsoid for
the purposes of this primitive are the radius and the
inverse flattening metric. These parameters can be
easily found by examining either the WKT definition
of a given projection/datum pair or the .prj file for
the desired datum. For example, if you open the .prj file
for a shapefile exported with the WGS66 datum in a text editor,
you will see, somewhere in the file, this bit of text:
`DATUM["D_WGS_1966",SPHEROID["NWL_9D",6378145,298.25]]`.
If you look at the `SPHEROID` section of that text, the
first number is the radius of that ellipoid and the
second is the inverse flattening.

Once we have these numbers, we can project data that is
relative to WGS66 like so:
```
let location gis:project-lat-lon my-lat my-lon 6378145 298.25
```

For more on earth ellipoids, see: https://en.wikipedia.org/wiki/Earth_ellipsoid



### `gis:load-dataset`

```NetLogo
gis:load-dataset *file*
```


Loads the given data file, re-projecting the data as necessary. 

Relative paths are resolved relative to the location of the current
model, or the user's home directory if the current model
hasn't been saved yet.

For ESRI shapefiles and ESRI grid files, if there is a ".prj" file 
associated with the file, then `load-datset` will consult that file
and re-project to the current global projection if needed. If no ".prj"
file is found, then the data is assumed to use the same projection as
the current global coordinate system.

For GeoJSON files, as per the most-recent specification (RFC 7946), 
the coordinate system for GeoJSON files is always WGS84 and will be 
imported accordingly. 

Currently, three types of data file are supported:

* "**.shp**" (ESRI shapefile): contains vector data,
  consisting of points, lines, or polygons. When the target file is a
  shapefile, `load-dataset` reports a VectorDataset.
* "**.asc**" or "**.grd**" (ESRI ASCII grid):
  contains raster data, consisting of a grid of values. When
  the target file is an ASCII grid file, `load-dataset`
  reports a RasterDataset.
* "**.geojson**" or "**.json**" (GeoJSON): contains vector data 
  similar to shapefiles and similarly reports a VectorDataset. 

Note that not all aspects of the GeoJSON standard are supported. 
In particular, to be properly imported, a GeoJSON file must 
satisfy the following: 

* It only contain numeric or string data within the properties. 
  all other json data will be stringified. 
* All "Features" within a "FeatureCollection" must be of the same
  shape type ("Point", "LineString", etc.) Additionally, if not all
  the "Features" in the "FeatureCollection" have the same set of 
  property names, default values will be supplied where there
  are missing entries (0 for numbers and "" for strings.)
* It must not use "GeometryCollection", which is not supported

Elevation/Z data is partially supported. For both .geojson and
.shp files, single points with Z data will have their Z
coordinate moved to a new "_Z" property that can be accessed
with `gis:property-value` like any other property. Any Z
information for other shape types will be discarded upon import.



### `gis:load-dataset-from-string`

```NetLogo
gis:load-dataset-from-string *format* *data*
```


Like `load-dataset`, but reads the dataset from the given string
instead of from a file. This is primarily intended for NetLogo Web,
which has no file system; there, data can come from an embedded
resource in the model or from a remote source using the fetch
extension.

The format argument determines how the data is interpreted:

* "**shp**" (ESRI shapefile): `data` can take either of two forms.
  As a string, it must be the base64-encoded content of a zip
  archive holding the shapefile's parts. The zip must contain
  exactly one ".shp" file, along with a ".dbf" file with the same
  base name (so "cities.shp" and "cities.dbf"). A ".prj" file with
  the same base name is optional; when present, the data is
  re-projected to the current global projection as needed, just as
  `load-dataset` does with an associated ".prj" file. Any other
  files (like ".shx") are ignored, as are the hidden metadata files
  that macOS adds to zip archives. If the files sit inside a folder
  within the zip, as often happens when a zip is made by compressing
  a folder, they are still found. As a list, `data` must hold
  two-element [extension content] lists: the "shp" and "dbf" entries
  are required and contain the base64-encoded content of the
  corresponding files, an optional "prj" entry holds plain
  projection text, and any other entries (like "shx") are ignored.
  Either form reports a VectorDataset.
* "**asc**" or "**grd**" (ESRI ASCII grid): `data` is the text
  content of an ASCII grid file. The data is assumed to use the same
  projection as the current global coordinate system. Reports a
  RasterDataset.
* "**geojson**" or "**json**" (GeoJSON): `data` is GeoJSON text. As
  with `load-dataset`, the coordinate system is always WGS84.
  Reports a VectorDataset. The GeoJSON limitations described for
  `load-dataset` apply here as well.

For example, to load a shapefile from an embedded resource named
"cities.zip" using the resource extension:

```
let dataset gis:load-dataset-from-string "shp" resource:get "cities.zip"
```

Or to load one from separate embedded resources named "cities.shp",
"cities.dbf", and "cities.prj":

```
let dataset gis:load-dataset-from-string "shp" (map [ ext ->
  (list ext (resource:get (word "cities." ext)))
] ["shp" "dbf" "prj"])
```

To make a shapefile zip archive with your operating system's
built-in tools:

* On Windows, select the shapefile's files (".shp", ".dbf", and so
  on) in File Explorer, then right-click the selection and choose
  "Compress to ZIP file" (on Windows 10, "Send to" and then
  "Compressed (zipped) folder").
* On macOS, select the files in the Finder, then right-click
  (Control-click) the selection and choose "Compress".

Zipping a folder containing the files, rather than the files
themselves, also works.



### `gis:store-dataset`

```NetLogo
gis:store-dataset *dataset* *file*
```


Exports the given dataset to the given file. 

For VectorDatasets, two file formats are supported, ESRI shapefiles
and GeoJSON. If the given file name ends in ".geojson" or ".json", 
then the file will be exported as a GeoJSON file. If the file name
ends in any other extension or no extension at all, the dataset
will be exported as a shapefile and the associated file extensions
will be supplied (".shp", ".prj", etc.)

For RasterDatasets, only ESRI ASCII grid files are supported and
the associated file extensions will be automatically supplied.

Relative paths are resolved relative to the location of the current
model, or the user's home directory if the current model hasn't
been saved yet.



### `gis:store-dataset-to-string`

```NetLogo
gis:store-dataset-to-string *dataset* *format*
```


Like `store-dataset`, but reports the dataset's contents as a string
instead of writing to a file. The formats supported for
VectorDatasets are "geojson" (or "json"), which reports GeoJSON
text, and "shp", which reports the base64-encoded content of a zip
archive holding the files `store-dataset` would have written:
"dataset.shp", "dataset.shx", and "dataset.dbf", plus "dataset.prj"
if a global projection is set. To get a shapefile's parts as
separate strings instead of a single zip, use
`store-dataset-to-strings`. For RasterDatasets, "asc" (or "grd")
reports the text content of an ESRI ASCII grid file. A reported
string can be loaded back with `load-dataset-from-string` using
the same format.

This is primarily intended for NetLogo Web, which has no file
system. For example, the send-to extension can be used there to
download the result as a file:

```
send-to:file "cities.geojson" (gis:store-dataset-to-string dataset "geojson")
```



### `gis:store-dataset-to-strings`

```NetLogo
gis:store-dataset-to-strings *dataset* *format*
```


Like `store-dataset-to-string`, but reports the parts of a
multi-file format as separate strings instead of a single zip
archive string. The only such format currently supported is "shp"
(ESRI shapefile), for VectorDatasets.

Reports a list of two-element [extension content] lists, one for
each file that `store-dataset` would have written: "shp", "shx",
and "dbf" entries with base64-encoded content, plus a "prj" entry
with plain projection text if a global projection is set. The
reported list can be loaded back with `load-dataset-from-string`.

For example, on NetLogo Web the send-to extension can be used to
download each part of the shapefile as a file:

```
foreach (gis:store-dataset-to-strings dataset "shp") [ part ->
  send-to:file (word "cities." (first part)) (last part)
]
```



### `gis:type-of`

```NetLogo
gis:type-of *dataset*
```

Reports the type of the given GIS dataset: either "VECTOR" or "RASTER"


### `gis:patch-dataset`

```NetLogo
gis:patch-dataset *patch-variable*
```


Reports a new raster whose cells correspond directly to NetLogo
patches, and whose cell values consist of the values of the given
patch variable. This primitive is basically the inverse of [apply-raster](#gisapply-raster);
`apply-raster` copies values from a raster dataset to a patch variable, while this
primitive copies values from a patch variable to a raster dataset.



### `gis:turtle-dataset`

```NetLogo
gis:turtle-dataset *turtle-set*
```


Reports a new, point VectorDataset built from the turtles in the
given agentset. The points are located at locations of the turtles,
translated from NetLogo space into GIS space using the current
coordinate transformation. And the dataset's properties consist
of all of the turtle variables common to every turtle in the
agentset.



### `gis:link-dataset`

```NetLogo
gis:link-dataset *link-set*
```


Reports a new, line VectorDataset built from the links in the given
agentset. The endpoints of each line are at the location of the
turtles connected by each link, translated from NetLogo space into
GIS space using the current coordinate transformation. And the
dataset's properties consist of all of the link variables
common to every link in the agentset.



### `gis:shape-type-of`

```NetLogo
gis:shape-type-of *VectorDataset*
```


Reports the shape type of the given dataset. The possible output
values are "POINT", "LINE", and "POLYGON".


### `gis:property-names`

```NetLogo
gis:property-names *VectorDataset*
```


Reports a list of strings where each string is the name of a
property possessed by each VectorFeature in the given
VectorDataset, suitable for use in [gis:property-value](#gisproperty-value).



### `gis:feature-list-of`

```NetLogo
gis:feature-list-of *VectorDataset*
```

Reports a list of all VectorFeatures in the given dataset.


### `gis:vertex-lists-of`

```NetLogo
gis:vertex-lists-of *VectorFeature*
```


Reports a list of lists of Vertex values. For point datasets, each
vertex list will contain exactly one vertex: the location of a
point. For line datasets, each vertex list will contain at least
two points, and will represent a "polyline", connecting
each adjacent pair of vertices in the list. For polygon datasets,
each vertex list will contain at least three points, representing a
polygon connecting each vertex, and the first and last vertices in
the list will be the same.



### `gis:centroid-of`

```NetLogo
gis:centroid-of *VectorFeature*
```


Reports a single Vertex representing the centroid (center of
gravity) of the given feature. For point datasets, the centroid is
defined as the average location of all points in the feature. For
line datasets, the centroid is defined as the average of the
locations of the midpoints of all line segments in the feature,
weighted by segment length. For polygon datasets, the centroid is
defined as the weighted sum of the centroids of a decomposition of
the area into (possibly overlapping) triangles. See <a href="http://www.faqs.org/faqs/graphics/algorithms-faq/" target="_blank">this FAQ</a>
for more details on the polygon centroid algorithm.



### `gis:random-point-inside`

```NetLogo
gis:random-point-inside *VectorFeature*
```


Reports a single randomly-generated Vertex that lies within the
given feature polygon. Generated points are uniformly
distributed within the polygon and both polygon holes and 
multi-polygon features are supported.

```
; create 100 turtles randomly distributed throught a VectorFeature `vf`
crt 100 [
  let loc gis:location-of gis:random-point-inside vf
  set xcor item 0 loc
  set ycor item 1 loc
]
```



### `gis:location-of`

```NetLogo
gis:location-of *Vertex*
```


Reports a two-element list containing the x and y values (in that
order) of the given vertex translated into NetLogo world space
using the current transformation, or an empty list if the given
vertex lies outside the NetLogo world.



### `gis:set-property-value`

```NetLogo
gis:set-property-value *VectorFeature* *property-name* *value*
```


Sets the value of the given property of the given VectorFeature. The 
type of the given value (string or number) must match the property
type of the VectorFeature. This command may be used in conjunction 
with store-dataset to make changes to VectorFeatures and export 
them back as GIS datasets. 



### `gis:property-value`

```NetLogo
gis:property-value *VectorFeature* *property-name*
```


Reports the value of the property with the given name for the given
VectorDataset. The reported value may be a number, a string, or a
boolean value, depending on the type of the field in the underlying
data file.

For shapefiles, values from dBase `CHARACTER` and
`DATE` fields are returned as strings, values from
`NUMBER` and `FLOAT` fields are returned as numbers,
and values from `LOGICAL` fields are returned as boolean
values. `MEMO` fields are not supported. `DATE`
values are converted to strings using ISO 8601 format
(`YYYY-MM-DD`).



### `gis:find-features`

```NetLogo
gis:find-features *VectorDataset* *property-name* *specified-value*
```


Reports a list of all VectorFeatures in the given dataset
whose value for the property *property-name* matches
*specified-value* (a string or number).

For strings, value comparison is not case sensitive, and the
wildcard character "*" will match any number of occurrences
(including zero) of any character.

For numbers, beware that there are some numbers that are too
big to be represented as an integer in NetLogo, so integer
data imported into NetLogo may start to lose precision
if they are larger than around 10^14.



### `gis:find-one-feature`

```NetLogo
gis:find-one-feature *VectorDataset* *property-name* *specified-value*
```


Reports the first VectorFeature in the dataset whose value
for the property *property-name* matches the given string or
number.  Reports `nobody` if no matching VectorFeature is
found.

For strings, Value comparison is not case sensitive, and the
wildcard character "*" will match any number of occurrences
(including zero) of any character.  Features are searched in
the order that they appear in the data file that was the
source of the dataset, and searching stops as soon as a
match is found.

For numbers, beware that there are some numbers that are too
big to be represented as an integer in NetLogo, so if you
want to be able to identify features based on a unique ID,
keep the IDs you use on the relatively small side to play it
safe. Any number that can fit into a 32 bit unsigned integer
will work, but any larger than 10^14 and you could run into
issues. (Helpfully, the NetLogo app will warn you if you try
to type one of these too-large numbers into the editor if
you want to check.)



### `gis:find-less-than`

```NetLogo
gis:find-less-than *VectorDataset* *property-name* *value*
```


Reports a list of all VectorFeatures in the given dataset whose
value for the property *property-name* is less than the given
*value*. String values are compared using case-sensitive
lexicographic order as defined in the <a href="http://docs.oracle.com/javase/1.5.0/docs/api/java/lang/String.html#compareTo(java.lang.String)" target="_blank">
Java Documentation</a>. Using a string value for a numeric property
or a numeric value for a string property will cause an error.



### `gis:find-greater-than`

```NetLogo
gis:find-greater-than *VectorDataset* *property-name* *value*
```


Reports a list of all VectorFeatures in the given dataset whose
value for the property *property-name* is greater than the
given *value*. String values are compared using case-sensitive
lexicographic order as defined in the <a href="http://docs.oracle.com/javase/1.5.0/docs/api/java/lang/String.html#compareTo(java.lang.String)" target="_blank">
Java Documentation</a>. Using a string value for a numeric property
or a numeric value for a string property will cause an error.



### `gis:find-range`

```NetLogo
gis:find-range *VectorDataset* *property-name* *minimum-value* *maximum-value*
```


Reports a list of all VectorFeatures in the given dataset whose
value for the property *property-name* is strictly greater
than *minimum-value* and strictly less than
*maximum-value*. String values are compared using
case-sensitive lexicographic order as defined in the <a href="http://docs.oracle.com/javase/1.5.0/docs/api/java/lang/String.html#compareTo(java.lang.String)" target="_blank">
Java Documentation</a>. Using a string value for a numeric property
or a numeric value for a string property will cause an error.



### `gis:property-minimum`

```NetLogo
gis:property-minimum *VectorDataset* *property-name*
```


Reports the smallest value for the given property over all of the
VectorFeatures in the given dataset. String values are compared
using case-sensitive lexicographic order as defined in the <a href="http://docs.oracle.com/javase/1.5.0/docs/api/java/lang/String.html#compareTo(java.lang.String)" target="_blank">
Java Documentation</a>.



### `gis:property-maximum`

```NetLogo
gis:property-maximum *VectorDataset* *property-name*
```


Reports the largest value for the given property over all of the
VectorFeatures in the given dataset. String values are compared
using case-sensitive lexicographic order as defined in the <a href="http://docs.oracle.com/javase/1.5.0/docs/api/java/lang/String.html#compareTo(java.lang.String)" target="_blank">
Java Documentation</a>.



### `gis:apply-coverage`

```NetLogo
gis:apply-coverage *VectorDataset* *property-name* *patch-variable*
```


Copies values from the given property of the VectorDataset's
features to the given patch variable. The dataset must be a
`polygon` dataset; points and lines are not supported.

For each patch, it finds all VectorFeatures that intersect that
patch. Then, if the property is a string property, it computes the
majority value by computing the total area of the patch covered by
VectorFeatures having each possible value of the property, then
returning the value which represents the largest proportion of the
patch area. If the property is a numeric property, it computes a
weighted average of property values from all VectorFeatures which
intersect the patch, weighted by the proportion of the patch area
they cover.

There are two exceptions to this default behavior:

* If a percentage of a patches' area greater than the
  coverage-maximum-threshold is covered by a single VectorFeature,
  then the property value from that VectorFeature is copied directly.
  If more than one VectorFeature covers a percentage of area greater
  than the threshold, only the first will be used.

* If the total percentage of a patches' area covered by
  VectorFeatures is less than the coverage-minimum-threshold, the
  target patch variable is set to Not A Number.

By default, the minimum threshold is 10% and the maximum threshold
is 33%. These values may be modified using the four primitives that
follow.



### `gis:create-turtles-from-points`

```NetLogo
gis:create-turtles-from-points *VectorDataset* *breed* *commands*
```


  For each point in a VectorDataset of points, create a turtle of
  the specified breed at the point's location. For each agent variable
  (as defined in `<breeds>-own`), if there is a property with the same
  name in the dataset, set that variable's value to be the value of
  that property. Finally, execute any commands in the optional
  command block. To use generic turtles as the chosen breed, simply
  supply `turtles` as the breed argument.

  Property names and variable names are compared case-insensitively.
  Keep in mind that when importing shapefiles, property names may be
  modified for backwards compatibility reasons. The names given by
  `gis:property-names` can always be trusted as authoritative. For
  manually specifying a mapping between property names and variable
  names, see the `create-turtles-from-points-manual` primitive.

  For multi-point datasets, a turtle is created at each point of
  multi-point feature, each with the same set of variable values.

  Built-in variables such as "label" and "heading" are supported.
  NetLogo color numeric representations are supported for setting
  "color" and "label-color", as well as the 15 default color string
  representations ("red", "blue", "black", etc.).

  As an example: say you wanted to create a turtle of breed
  "cities/city" for each city in a dataset of cities
  like the one included in the "GIS General Examples" model from
  the models library. The cities dataset has four properties,
  "NAME", "COUNTRY", "POPULATION", and "CAPITAL". To map them all
  to NetLogo turtle variables and set their shapes to circles, you
  could do this:

```
extensions [gis]
breed [cities city]
cities-own [name country population capital]
globals [cities-dataset]

to setup
  set cities-dataset gis:load-dataset "cities.shp"
  gis:create-turtles-from-points cities-dataset cities [
    set shape "circle"
  ]
end
```
    


### `gis:create-turtles-from-points-manual`

```NetLogo
gis:create-turtles-from-points-manual *VectorDataset* *breed* *property-mapping* *commands*
```


Like `create-turtles-from-points`, creates a turtle for each point in a
VectorDataset of points and populates their variables with the values of
corresponding gis properties.

This primitive can be used to specify
additional mappings between gis property names and NetLogo variable names.
These mappings are specified as a list of lists of strings like so:
`[["property-name" "turtle-variable-name"] ["property-name" "turtle-variable-name"] (etc.)]`

These manual mappings modify the automatic mapping process that takes
place in the `create-turtles-from-points` primitive, so you only need
to specify the changes you want to make to the default mappings, and
the rest of the mappings will be untouched.

To return to the cities example from the
`create-turtles-from-points` entry, the variable name "capital" is not very
descriptive. something like "is-capital?" fits the NetLogo style much better.
To make that change, you would modify the example like so.

```
extensions [gis]
breed [cities city]
cities-own [name country population is-capital?]
globals [cities-dataset]

to setup
  set cities-dataset gis:load-dataset "cities.shp"
  ;; Since we only want to change how the "CAPITAL" property is mapped, we only need to specify that one change.
  gis:create-turtles-from-points-manual cities-dataset cities [["CAPITAL" "is-capital?"]] [
    set shape "circle"
  ]
  ;; Each city turtle still has a name, country, and population set just like the non-manual version.
end
```
    


### `gis:create-turtles-inside-polygon`

```NetLogo
gis:create-turtles-inside-polygon *VectorFeature* *breed* *n* *commands*
```


Randomly create "n" turtles of the given breed within
the given VectorFeature and for each agent variable
(as defined in <breeds>-own), if there is a property with the same
name in the dataset, set that variable's value to be the value of
that property. Finally, execute any commands in the optional
command block. To use generic turtles as the chosen breed, simply
supply `turtles` as the breed argument.

Property names and variable names are compared case-insensitively.
Keep in mind that when importing shapefiles, property names may be
modified for backwards compatibility reasons. The names given by
`gis:property-names` can always be trusted as authoritative. For
manually specifying a mapping between property names and variable
names, see the `create-turtles-inside-polygon-manual` primitive.

Built-in variables such as "label" and "heading" are supported.
NetLogo color numeric representations are supported for setting
"color" and "label-color", as well as the 15 default color string
representations ("red", "blue", "black", etc.).

As an example: say you had a VectorDataset of polygons representing
different zip codes within a state and you want to create 100
different turtles within each zip code and have each turtle
know which zip code it originated in. The VectorDataset
has a field named "zip", so you should add a variable
named "zip" to the turtles with `turtles-own`. Then,
loop through each VectorFeature in the VectorDataset and use
the `create-turtles-inside-polygon` primitive to create 100
new turtles.

```
extensions [gis]
globals [dataset]
turtles-own [zip]

to setup
  set dataset gis:load-dataset "dataset.shp"
  gis:set-world-envelope envelope-of dataset
  gis:set-drawing-color red
  gis:draw dataset 1

  foreach gis:feature-list-of dataset [ this-vector-feature ->
    gis:create-turtles-inside this-vector-feature turtles 100 [
      set shape "person"
    ]
  ]
end
```
        


### `gis:create-turtles-inside-polygon-manual`

```NetLogo
gis:create-turtles-inside-polygon-manual *VectorFeature* *breed* *n* *property-mapping* *commands*
```


Like `create-turtles-inside-polygon`, creates "n" different turtles within the given VectorFeature and populates their
agent variables with values corresponding to the property values of the VectorFeature.

This primitive can be used to specify
additional mappings between gis property names and NetLogo variable names.
These mappings are specified as a list of lists of strings like so:
`[["property-name" "turtle-variable-name"] ["property-name" "turtle-variable-name"] (etc.)]`

These manual mappings modify the automatic mapping process that takes
place in the `create-turtles-inside-polygon` primitive, so you only need
to specify the changes you want to make to the default mappings, and
the rest of the mappings will be untouched.

See the `create-turtles-from-points-manual` entry for an example of
how to override default mappings with manual ones.
      


### `gis:coverage-minimum-threshold`

```NetLogo
gis:coverage-minimum-threshold
```

Reports the current coverage minimum threshold used by [gis:apply-coverage](#gisapply-coverage).


### `gis:set-coverage-minimum-threshold`

```NetLogo
gis:set-coverage-minimum-threshold *new-threshold*
```

Sets the current coverage minimum threshold to be used by [gis:apply-coverage](#gisapply-coverage).


### `gis:coverage-maximum-threshold`

```NetLogo
gis:coverage-maximum-threshold
```

Reports the current coverage maximum threshold used by [gis:apply-coverage](#gisapply-coverage).


### `gis:set-coverage-maximum-threshold`

```NetLogo
gis:set-coverage-maximum-threshold *new-threshold*
```

Sets the current coverage maximum threshold to be used by [gis:apply-coverage](#gisapply-coverage).


### `gis:intersects?`

```NetLogo
gis:intersects? *x* *y*
```


Reports true if the given objects' spatial representations
share at least one point in common, and false otherwise. The
objects x and y may be any one of:

* a VectorDataset, in which case the object's spatial
    representation is the union of all the points, lines, or polygons
    the dataset contains.
* a VectorFeature, in which case the object's spatial
    representation is defined by the point, line, or polygon the
    feature contains.
* A turtle, in which case the spatial representation is a point.
* A link, whose spatial representation is a line segment
  connecting the two points represented by the turtles the link is
  connecting.
* A patch, whose spatial representation is a rectangular polygon.
* An agentset, whose spatial representation is the union of the
  representations of all of the agents it contains.
* A list containing of any of the items listed here, including
  another list. The spatial representation of such a list is the
  union of the spatial representations of its contents.



### `gis:contains?`

```NetLogo
gis:contains? *x* *y*
```


Reports true if every point of *y*'s spatial
representation is also a part of *x*'s spatial
representation. Note that this means that polygons do contain their
boundaries. The objects x and y may be any one of

* a VectorDataset, in which case the object's spatial
  representation is the union of all the points, lines, or polygons
  the dataset contains.
* a VectorFeature, in which case the object's spatial
  representation is defined by the point, line, or polygon the
  feature contains.
* A turtle, in which case the spatial representation is a point.
* A link, whose spatial representation is a line segment
  connecting the two points represented by the turtles the link is
  connecting.
* A patch, whose spatial representation is a rectangular polygon.
* An agentset, whose spatial representation is the union of the
  representations of all of the agents it contains.
* A list containing of any of the items listed here, including
  another list. The spatial representation of such a list is the
  union of the spatial representations of its contents.



### `gis:contained-by?`

```NetLogo
gis:contained-by? *x* *y*
```


Reports true if every point of *x*'s spatial
representation is also a part of *y*'s spatial
representation. The objects x and y may be any one of:

* a VectorDataset, in which case the object's spatial
  representation is the union of all the points, lines, or polygons
  the dataset contains.
* a VectorFeature, in which case the object's spatial
  representation is defined by the point, line, or polygon the
  feature contains.
* A turtle, in which case the spatial representation is a point.
* A link, whose spatial representation is a line segment
  connecting the two points represented by the turtles the link is
  connecting.
* A patch, whose spatial representation is a rectangular polygon.
* An agentset, whose spatial representation is the union of the
  representations of all of the agents it contains.
* A list containing of any of the items listed here, including
  another list. The spatial representation of such a list is the
  union of the spatial representations of its contents.



### `gis:have-relationship?`

```NetLogo
gis:have-relationship? *x* *y*
```


Reports true if the spatial representations of the two objects have
the given spatial relationship, and false otherwise. The spatial
relationship is specified using a **Dimensionally Extended Nine-
Intersection Model (DE-9IM)** matrix. The matrix consists of 9
elements, each of which specifies the required relationship between
the two objects' interior space, boundary space, or exterior
space. The elements must have one of six possible values:

* "T", meaning the spaces must intersect in some way
* "F", meaning the spaces must not intersect in any way
* "0", meaning the dimension of the spaces'
  intersection must be zero (i.e., it must be a point or non-empty
  set of points).
* "1", meaning the dimension of the spaces'
  intersection must be one (i.e., it must be a line or non-empty set
  of line segments).
* "2", meaning the dimension of the spaces'
  intersection must be two (i.e., it must be a polygon or set of
  polygons whose area is greater than zero).
* "*", meaning that the two spaces may have any
  relationship.

For example, this matrix:
      <table width="50%" border="1" style="text-align: center; margin: 0 auto;">
<tr>
  <td rowspan="2" colspan="2">
  <td colspan="3">
    x
  <tr>
  <td> Interior <td> Boundary <td> Exterior <tr>
  <td rowspan="3">
    y
  <td> Interior <td> T <td> * <td> * <tr>
  <td> Boundary <td> * <td> * <td> * <tr>
  <td> Exterior <td> F <td> F <td> * </table>

would return true if and only if some part of object *x*'s
interior lies inside object *y*'s interior, and no part of
object *x*'s interior or boundary intersects object
*y*'s exterior. This is essentially a more restrictive
form of the `contains?` primitive; one in which polygons are
not considered to contain their boundaries.

The matrix is given to the `have-relationship?` primitive as
a string, whose elements are given in the following order:
      <table width="25%" border="1" style="text-align: center; margin: 0 auto;" align="center">
<tr>
  <td> 1 <td> 2 <td> 3 <tr>
  <td> 4 <td> 5 <td> 6 <tr>
  <td> 7 <td> 8 <td> 9 </table>

So to use the example matrix above, you would write:

```
gis:have-relationship? x y "T*****FF*"
```

A much more detailed and formal description of the DE-9IM matrix
and the associated point-set theory can be found in the <a href="http://www.opengeospatial.org/standards/sfs" target="_blank">OpenGIS Simple
Features Specification for SQL</a>.

The objects x and y may be any one of:

* a VectorDataset, in which case the object's spatial
  representation is the union of all the points, lines, or polygons
  the dataset contains.
* a VectorFeature, in which case the object's spatial
  representation is defined by the point, line, or polygon the
  feature contains.
* A turtle, in which case the spatial representation is a point.
* A link, whose spatial representation is a line segment
  connecting the two points represented by the turtles the link is
  connecting.
* A patch, whose spatial representation is a rectangular polygon.
* An agentset, whose spatial representation is the union of the
  representations of all of the agents it contains.
* A list containing of any of the items listed here, including
  another list. The spatial representation of such a list is the
  union of the spatial representations of its contents.



### `gis:relationship-of`

```NetLogo
gis:relationship-of *x* *y*
```


Reports the **Dimensionally Extended Nine-Intersection Model
(DE-9IM)** matrix that describes the spatial relationship of the
two objects. The matrix consists of 9 elements, each of which
describes the relationship between the two objects' interior
space, boundary space, or exterior space. Each element will
describe the dimension of the intersection of two spaces, meaning
that it may have one of four possible values:

* "-1", meaning the spaces do not intersect
* "0", meaning the dimension of the spaces'
  intersection is zero (i.e., they intersect at a point or set of
  points).
* "1", meaning the dimension of the spaces'
  intersection is one (i.e., they intersect along one or more lines).
* "2", meaning the dimension of the spaces'
  intersection is two (i.e., their intersection is a non-empty
  polygon).


For example, the two polygons x and y shown here:
<center>
  <img alt="" src="images/intersecting-polygons.png">
</center>

have the following DE-9IM matrix:
<table width="50%" border="1" style="text-align: center; margin: 0 auto;" align="center">
  <tr>
  <td rowspan="2" colspan="2">
  <td colspan="3">
    x
  <tr>
  <td> Interior <td> Boundary <td> Exterior <tr>
  <td rowspan="3">
    y
  <td> Interior <td> 2 <td> 1 <td> 2 <tr>
  <td> Boundary <td> 1 <td> 0 <td> 1 <tr>
  <td> Exterior <td> 2 <td> 1 <td> 2 </table>

Which would be reported by the `relationship-of` primitive
as the string "212101212".

A much more detailed and formal description of the DE-9IM matrix
and the associated point-set theory can be found in the <a href="http://www.opengeospatial.org/standards/sfs" target="_blank">OpenGIS Simple
Features Specification for SQL</a>.

The objects x and y may be any one of:

* a VectorDataset, in which case the object's spatial
  representation is the union of all the points, lines, or polygons
  the dataset contains.
* a VectorFeature, in which case the object's spatial
  representation is defined by the point, line, or polygon the
  feature contains.
* A turtle, in which case the spatial representation is a point.
* A link, whose spatial representation is a line segment
  connecting the two points represented by the turtles the link is
  connecting.
* A patch, whose spatial representation is a rectangular polygon.
* An agentset, whose spatial representation is the union of the
  representations of all of the agents it contains.
* A list containing of any of the items listed here, including
  another list. The spatial representation of such a list is the
  union of the spatial representations of its contents.



### `gis:intersecting`

```NetLogo
gis:intersecting *patch-set* *data*
```


Reports a new agent set containing only those members of the given
agent set which intersect given GIS *data*, which may be any
one of: a VectorDataset, a VectorFeature, an Agent, an Agent Set,
or a list containing any of the above.



### `gis:width-of`

```NetLogo
gis:width-of *RasterDataset*
```


Reports the number of columns in the dataset. Note that this is the
number of cells from left to right, not the width of the dataset in
GIS space.



### `gis:height-of`

```NetLogo
gis:height-of *RasterDataset*
```


Reports the number of rows in the dataset. Note that this is the
number of cells from top to bottom, not the height of the dataset
in GIS space.



### `gis:raster-value`

```NetLogo
gis:raster-value *RasterDataset* *x* *y*
```


Reports the value of the given raster dataset in the given cell.
Cell coordinates are numbered from left to right, and from top to
bottom, beginning with zero. So the upper left cell is (0, 0), and
the bottom right cell is (`gis:width-of dataset` - 1,
`gis:height-of dataset` - 1).



### `gis:set-raster-value`

```NetLogo
gis:set-raster-value *RasterDataset* *x* *y* *value*
```


Sets the value of the given raster dataset at the given cell to a
new value. Cell coordinates are numbered from left to right, and
from top to bottom, beginning with zero. So the upper left cell is
(0, 0), and the bottom right cell is (`gis:width-of dataset`
- 1, `gis:height-of dataset` - 1).



### `gis:minimum-of`

```NetLogo
gis:minimum-of *RasterDataset*
```

Reports the highest value in the given raster dataset.


### `gis:maximum-of`

```NetLogo
gis:maximum-of *RasterDataset*
```

Reports the lowest value in the given raster dataset.


### `gis:sampling-method-of`

```NetLogo
gis:sampling-method-of *RasterDataset*
```


Reports the sampling method used to compute the value of the given
raster dataset at a single point, or over an area smaller than a
single raster cell. Sampling is performed by the GIS extension
primitives [raster-sample](#gisraster-sample), [resample](#gisresample), [convolve](#gisconvolve),
and [apply-raster](#gisapply-raster). The sampling
method will be one of the following:

* `"NEAREST_NEIGHBOR"`: the value of the cell
  nearest the sampling location is used.
* `"BILINEAR"`: the value of the four nearest
  cells are sampled by linear weighting, according to their
  proximity to the sampling site.
* `"BICUBIC"`: the value of the sixteen nearest
  cells are sampled, and their values are combined by weight
  according to a piecewise cubic polynomial recommended by Rifman
  (see *Digital Image Warping*, George Wolberg, 1990, pp
  129-131, IEEE Computer Society Press).
* `"BICUBIC_2"`: the value is sampled using the
  same procedure and the same polynomial as with `BICUBIC`
  above, but using a different coefficient. This method may produce
  somewhat sharper results than `BICUBIC`, but that result
  is data dependent.

For more information on these sampling methods and on raster
sampling in general, see <a href="https://en.wikipedia.org/wiki/Image_scaling" target="_blank">this wikipedia
article</a>.



### `gis:set-sampling-method`

```NetLogo
gis:set-sampling-method *RasterDataset* *sampling-method*
```


Sets the sampling method used by the given raster dataset at a
single point, or over an area smaller than a single raster cell.
Sampling is performed by the GIS extension primitives [raster-sample](#gisraster-sample), [resample](#gisresample), [convolve](#gisconvolve),
and [apply-raster](#gisapply-raster). The sampling
method must be one of the following:

*  `"NEAREST_NEIGHBOR"`
*  `"BILINEAR"`
*  `"BICUBIC"`
*  `"BICUBIC_2"`

See [sampling-method-of](#gissampling-method-of) above
for a more specific description of each sampling method.



### `gis:raster-sample`

```NetLogo
gis:raster-sample *RasterDataset* *sample-location*
```


Reports the value of the given raster over the given location. The
location may be any of the following:

* A list of length 2, which is taken to represent a point in
  netlogo space (`[xcor ycor]`) of the sort reported by
  [location-of](#gislocation-of) Vertex. The raster
  dataset is sampled at the point of that location.
* A list of length 4, which is taken to represent an envelope in
  GIS space, of the sort reported by [envelope-of](#gisenvelope-of). The raster dataset is sampled
  over the area of that envelope.
* A patch, in which case the raster dataset is sampled over the
  area of the patch.
* A turtle, in which case the raster dataset is sampled at the
  location of that turtle.
* A Vertex, in which case the raster dataset is sampled at the
  location of that Vertex.

If the requested location is outside the area covered by the raster
dataset, this primitive reports the special value representing
"not a number", which is printed by NetLogo as
"NaN". Using the special "not a number" value
as an argument to primitives that expect a number may cause an
error, but you can test the value reported by this primitive to
filter out "not a number" values. A value that is not a
number will be neither less than nor greater than a number value,
so you can detect "not a number" values using the
following:

```
let value gis:raster-sample dataset turtle 0
; set color to blue if value is a number, red if value is "not a number"
ifelse (value <= 0) or (value >= 0)
[ set color blue ]
[ set color red ]
```

If the requested location is a point, the sample is always computed
using the method set by [set-sampling-method](#gisset-sampling-method). If the
requested location is an area (i.e., an envelope or patch), the
sample is computed by taking the average of all raster cells
covered by the requested area.



### `gis:raster-world-envelope`

```NetLogo
gis:raster-world-envelope *RasterDataset* *x* *y*
```


Reports the GIS envelope needed to match the boundaries of NetLogo
patches with the boundaries of cells in the given raster dataset.
This envelope could then be used as an argument to [set-transformation-ds](#gisset-transformation-ds).

There may be more cells in the dataset than there are patches in
the NetLogo world. In that case, you will need to select a subset
of cells in the dataset by specifying which cell in the dataset you
want to match with the upper-left corner of the NetLogo world.
Cells are numbered from left to right, and from top to bottom,
beginning with zero. So the upper left cell is (0, 0), and the
bottom right cell is (`gis:width-of dataset` - 1, `gis:height-of dataset` - 1).



### `gis:create-raster`

```NetLogo
gis:create-raster *width* *height* *envelope*
```


Creates and reports a new, empty raster dataset with the given
number of columns and rows, covering the given envelope.



### `gis:resample`

```NetLogo
gis:resample *RasterDataset* *envelope* *width* *height*
```


Reports a new dataset that consists of the given RasterDataset
resampled to cover the given envelope and to contain the given
number of columns and rows. If the new raster's cells are
smaller than the existing raster's cells, they will be
resampled using the method set by [set-sampling-method](#gisset-sampling-method). If the new
cells are larger than the original cells, they will be sampled
using the `"NEAREST_NEIGHBOR"` method.



### `gis:convolve`

```NetLogo
gis:convolve *RasterDataset* *kernel-rows* *kernel-columns* *kernel* *key-column* *key-row*
```


Reports a new raster whose data consists of the given raster
convolved with the given kernel.

A convolution is a mathematical operation that computes each output
cell by multiplying elements of a kernel with the cell values
surrounding a particular source cell. A kernel is a matrix of
values, with one particular value defined as the "key
element", the value that is centered over the source cell
corresponding to the destination cell whose value is being
computed.

The values of the kernel matrix are given as a list, which
enumerates the elements of the matrix from left to right, top to
bottom. So the elements of a 3-by-3 matrix would be listed in the
following order:
      <table width="25%" border="1" style="text-align: center; margin: 0 auto;" align="center">
<tr>
  <td> 1 <td> 2 <td> 3 <tr>
  <td> 4 <td> 5 <td> 6 <tr>
  <td> 7 <td> 8 <td> 9 </table>

The key element is specified by column and row within the matrix.
Columns are numbered from left to right, beginning with zero. Rows
are numbered from top to bottom, also beginning with zero. So, for
example, the kernel for the horizontal <a href="https://en.wikipedia.org/wiki/Sobel_operator" target="_blank">Sobel operator</a>,
which looks like this:
      <table width="25%" border="1" style="text-align: center; margin: 0 auto;" align="center">
<tr>
  <td> 1 <td> 0 <td> -1 <tr>
  <td> 2 <td> 0 <br> <small>(key)</small> <td> -2 <tr>
  <td> 1 <td> 0 <td> -1 </table>

would be specified as follows:

```
let horizontal-gradient gis:convolve dataset 3 3 [1 0 -1 2 0 -2 1 0 -1] 1 1
```



### `gis:apply-raster`

```NetLogo
gis:apply-raster *RasterDataset* *patch-variable*
```



Copies values from the given raster dataset to the given patch
variable, resampling the raster as necessary so that its cell
boundaries match up with NetLogo patch boundaries. This resampling
is done as if using [resample](#gisresample) rather
than [raster-sample](#gisraster-sample), for the sake
of efficiency. However, patches not covered by the raster are
assigned values of "not a number" in the same way that
[raster-sample](#gisraster-sample) reports values for
locations outside the raster.



### `gis:drawing-color`

```NetLogo
gis:drawing-color
```


Reports the color used by the GIS extension to draw vector features
into the NetLogo drawing layer. Color can be represented either as
a NetLogo color (a single number between zero and 140) or an RGB
color (a list of 3 numbers). See details in the [Colors](http://ccl.northwestern.edu/netlogo/docs/programming.html#colors) section of the
Programming Guide.



### `gis:set-drawing-color`

```NetLogo
gis:set-drawing-color *color*
```


Sets the color used by the GIS extension to draw vector features
into the NetLogo drawing layer. *Color* can be represented
either as a NetLogo color (a single number between zero and 140) or
an RGB color (a list of 3 numbers). See details in the [Colors](http://ccl.northwestern.edu/netlogo/docs/programming.html#colors) section of the Programming Guide.



### `gis:draw`

```NetLogo
gis:draw *vector-data* *line-thickness*
```


Draws the given vector data to the NetLogo drawing layer, using the
current GIS drawing color, with the given line thickness. The data
may consist either of an entire VectorDataset, or a single
VectorFeature. This primitive draws only the boundary of polygon
data, and for point data, it fills a circle with a radius equal to
the line thickness.



### `gis:fill`

```NetLogo
gis:fill *vector-data* *line-thickness*
```


Fills the given vector data in the NetLogo drawing layer using the
current GIS drawing color, using the given line thickness around
the edges. The data may consist either of an entire VectorDataset,
or a single VectorFeature. For point data, it fills a circle with a
radius equal to the line thickness.



### `gis:paint`

```NetLogo
gis:paint *RasterDataset* *transparency*
```


Paints the given raster data to the NetLogo drawing layer. The
highest value in the dataset is painted white, the lowest is
painted in black, and the other values are painted in shades of
gray scaled linearly between white and black.

The *transparency* input determines how transparent the new
image in the drawing will be. Valid inputs range from 0 (completely
opaque) to 255 (completely transparent).



### `gis:import-wms-drawing`

```NetLogo
gis:import-wms-drawing *server-url* *spatial-reference* *layers* *transparency*
```


Imports an image into the NetLogo drawing layer using the
<a href="http://www.opengeospatial.org/standards/wms" target="_blank">Web Mapping Service</a>
 protocol, as defined by the <a href="http://www.opengeospatial.org/" target="_blank">
Open Geospatial Consortium</a>.

The *spatial reference* and *layers* inputs should be
given as strings. The *spatial reference* input corresponds to
the **SRS** parameter to the **GetMap** request as defined in
section 7.2.3.5 of version 1.1.1 of the WMS standard. The
*layers* input corresponds to the **LAYERS** parameter to
the as defined in 7.2.3.3 of version 1.1.1 of the WMS standard.

You can find the list of valid spatial reference codes and layer
names by examining the response to a **GetCapabilities** request
to the WMS server. Consult the relevant standard for instructions
on how to issue a **GetCapabilities** request to the server and
how to interpret the results.

The *transparency* input determines how transparent the new
image in the drawing will be. Valid inputs range from 0 (completely
opaque) to 255 (completely transparent).



## Terms of Use

GIS Extension © 2001-2012 Eric Russell and Daniel Edelson.
All rights reserved.

The Java source code (files in the src directory whose names end in ".java") and binary object code (the file "gis.jar") of the GIS Extension are made available to you subject to the terms of the following license:

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
* The names of its contributors may not be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

<pre>

--------------------------------------------------------------------------------

The following data files included with the GIS Extension contain proprietary
and confidential property of Environmental Systems Research Institute, Inc.
(ESRI), Redlands, CA, and its licensor(s).  These files are:

  cities.dbf, cities.shp, cities.shx, cities.txt,
  countries.dbf, countries.shp, countries.shx, countries.xml
  rivers.dbf, rivers.shp, rivers.shp, rivers.txt

These files are made available under the following license:

Proprietary Rights and Copyright: Licensee acknowledges that the Data and
Related Materials contain proprietary and confidential property of ESRI and
its licensor(s). The Data and Related Materials are owned by ESRI and its
licensor(s) and are protected by United States copyright laws and applicable
international copyright treaties and/or conventions.

THE LICENSEE EXPRESSLY ACKNOWLEDGES THAT THE DATA CONTAIN
SOME NONCONFORMITIES, DEFECTS, OR ERRORS. ESRI DOES NOT
WARRANT THAT THE DATA WILL MEET LICENSEE'S NEEDS OR
EXPECTATIONS, THAT THE USE OF THE DATA WILL BE UNINTERRUPTED,
OR THAT ALL NONCONFORMITIES, DEFECTS, OR ERRORS CAN OR WILL
BE CORRECTED. ESRI IS NOT INVITING RELIANCE ON THESE DATA, AND
THE LICENSEE SHOULD ALWAYS VERIFY ACTUAL DATA.

THE DATA AND RELATED MATERIALS CONTAINED THEREIN ARE PROVIDED
"AS-IS," WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.

IN NO EVENT SHALL ESRI AND/OR ITS LICENSOR(S) BE LIABLE FOR COSTS
OF PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES, LOST PROFITS,
LOST SALES OR BUSINESS EXPENDITURES, INVESTMENTS, OR
COMMITMENTS IN CONNECTION WITH ANY BUSINESS, LOSS OF ANY
GOODWILL, OR FOR ANY INDIRECT, SPECIAL,  INCIDENTAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES ARISING OUT OF THIS AGREEMENT OR USE OF
THE DATA AND RELATED MATERIALS, HOWEVER CAUSED, ON ANY THEORY
OF LIABILITY, AND WHETHER OR NOT ESRI AND/OR ITS LICENSOR(S) HAVE
BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. THESE LIMITATIONS
SHALL APPLY NOTWITHSTANDING ANY FAILURE OF ESSENTIAL PURPOSE
OF ANY EXCLUSIVE REMEDY.

Third Party Beneficiary: ESRI's licensor(s) has (have) authorized ESRI to
(sub)distribute and (sub)license their data as incorporated into the Data and
Related Materials. As an intended third party beneficiary to this Agreement,
the ESRI licensor(s) is (are) entitled to directly enforce, in its own name,
the rights and obligations undertaken by the Licensee and to seek all legal
and equitable remedies as are afforded to ESRI.

ESRI is a trademark of Environmental Systems Research Institute, Inc.

--------------------------------------------------------------------------------

The Java Advanced Imaging libraries (the binary files jai_codec-1.1.3.jar and
jai_core-1.1.3.jar) are copyright (c) 2006 Sun Microsystems, and are made
available under the following license:

Sun Microsystems, Inc.
Binary Code License Agreement

JAVA ADVANCED IMAGING API, VERSION 1.1.3

READ THE TERMS OF THIS AGREEMENT AND ANY PROVIDED SUPPLEMENTAL LICENSE TERMS
(COLLECTIVELY "AGREEMENT") CAREFULLY BEFORE OPENING THE SOFTWARE MEDIA
PACKAGE.  BY OPENING THE SOFTWARE MEDIA PACKAGE, YOU AGREE TO THE TERMS OF
THIS AGREEMENT.  IF YOU ARE ACCESSING THE SOFTWARE ELECTRONICALLY, INDICATE
YOUR ACCEPTANCE OF THESE TERMS BY SELECTING THE "ACCEPT" BUTTON AT THE END OF
THIS AGREEMENT.  IF YOU DO NOT AGREE TO ALL THESE TERMS, PROMPTLY RETURN THE
UNUSED SOFTWARE TO YOUR PLACE OF PURCHASE FOR A REFUND OR, IF THE SOFTWARE IS
ACCESSED ELECTRONICALLY, SELECT THE "DECLINE" BUTTON AT THE END OF THIS
AGREEMENT.

1.  LICENSE TO USE.  Sun grants you a non-exclusive and non-transferable
license for the internal use only of the accompanying software and
documentation and any error corrections provided by Sun (collectively
"Software"), by the number of users and the class of computer hardware for
which the corresponding fee has been paid.

2.  RESTRICTIONS.  Software is confidential and copyrighted. Title to
Software and all associated intellectual property rights is retained by Sun
and/or its licensors.  Except as specifically authorized in any Supplemental
License Terms, you may not make copies of Software, other than a single copy
of Software for archival purposes.  Unless enforcement is prohibited by
applicable law, you may not modify, decompile, or reverse engineer Software.
Licensee acknowledges that Software is not designed or intended for use in
the design, construction, operation or maintenance of any nuclear facility.
Sun Microsystems, Inc. disclaims any express or implied warranty of fitness
for such uses.   No right, title or interest in or to any trademark, service
mark, logo or trade name of Sun or its licensors is granted under this
Agreement.

3.  LIMITED WARRANTY.  Sun warrants to you that for a period of ninety (90)
days from the date of purchase, as evidenced by a copy of the receipt, the
media on which Software is furnished (if any) will be free of defects in
materials and workmanship under normal use.  Except for the foregoing,
Software is provided "AS IS".  Your exclusive remedy and Sun's entire
liability under this limited warranty will be at Sun's option to replace
Software media or refund the fee paid for Software.

4.  DISCLAIMER OF WARRANTY.  UNLESS SPECIFIED IN THIS AGREEMENT, ALL EXPRESS
OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY IMPLIED
WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
NON-INFRINGEMENT ARE DISCLAIMED, EXCEPT TO THE EXTENT THAT THESE DISCLAIMERS
ARE HELD TO BE LEGALLY INVALID.

5.  LIMITATION OF LIABILITY.  TO THE EXTENT NOT PROHIBITED BY LAW, IN NO
EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR
DATA, OR FOR SPECIAL, INDIRECT, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE
DAMAGES, HOWEVER CAUSED REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF
OR RELATED TO THE USE OF OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN
ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.  In no event will Sun's liability
to you, whether in contract, tort (including negligence), or otherwise,
exceed the amount paid by you for Software under this Agreement.  The
foregoing limitations will apply even if the above stated warranty fails of
its essential purpose.

6.  Termination.  This Agreement is effective until terminated.  You may
terminate this Agreement at any time by destroying all copies of Software.
This Agreement will terminate immediately without notice from Sun if you fail
to comply with any provision of this Agreement.  Upon Termination, you must
destroy all copies of Software.

7.  Export Regulations. All Software and technical data delivered under this
Agreement are subject to US export control laws and may be subject to export
or import regulations in other countries.  You agree to comply strictly with
all such laws and regulations and acknowledge that you have the
responsibility to obtain such licenses to export, re-export, or import as may
be required after delivery to you.

8.  U.S. Government Restricted Rights.  If Software is being acquired by or
on behalf of the U.S. Government or by a U.S. Government prime contractor or
subcontractor (at any tier), then the Government's rights in Software and
accompanying documentation will be only as set forth in this Agreement; this
is in accordance with 48 CFR 227.7201 through 227.7202-4 (for Department of
Defense (DOD) acquisitions) and with 48 CFR 2.101 and 12.212 (for non-DOD
acquisitions).

9.  Governing Law.  Any action related to this Agreement will be governed by
California law and controlling U.S. federal law.  No choice of law rules of
any jurisdiction will apply.

10. Severability. If any provision of this Agreement is held to be
unenforceable, this Agreement will remain in effect with the provision
omitted, unless omission would frustrate the intent of the parties, in which
case this Agreement will immediately terminate.

11. Integration.  This Agreement is the entire agreement between you and Sun
relating to its subject matter.  It supersedes all prior or contemporaneous
oral or written communications, proposals, representations and warranties and
prevails over any conflicting or additional terms of any quote, order,
acknowledgment, or other communication between the parties relating to its
subject matter during the term of this Agreement.  No modification of this
Agreement will be binding, unless in writing and signed by an authorized
representative of each party.

                   JAVA ADVANCED IMAGING, VERSION 1.1.3
                        SUPPLEMENTAL LICENSE TERMS

These supplemental license terms ("Supplemental Terms") add to or modify the
terms of the Binary Code License Agreement (collectively, the "Agreement").
Capitalized terms not defined in these Supplemental Terms shall have the same
meanings ascribed to them in the Agreement. These Supplemental Terms shall
supersede any inconsistent or conflicting terms in the Agreement, or in any
license contained within the Software.

1. Software Internal Use and Development License Grant.  Subject to the terms
and conditions of this Agreement, including, but not limited to Section 3
(Java Technology Restrictions) of these Supplemental Terms, Sun grants you a
non-exclusive, non-transferable, limited license to reproduce internally and
use internally the binary form of the Software, complete and unmodified, for
the sole purpose of designing, developing and testing your Java applets and
applications ("Programs").

2. License to Distribute Software.  In addition to the license granted in
Section 1 (Software Internal Use and Development License Grant) of these
Supplemental Terms, subject to the terms and conditions of this Agreement,
including but not limited to, Section 3 (Java Technology Restrictions) of
these Supplemental Terms, Sun grants you a non-exclusive, non-transferable,
limited license to reproduce and distribute the Software in binary code form
only, provided that you (i) distribute the Software complete and unmodified
and only bundled as part of your Programs, (ii) do not distribute additional
software intended to replace any component(s) of the Software, (iii) do not
remove or alter any proprietary legends or notices contained in the Software,
(iv) only distribute the Software subject to a license agreement that
protects Sun's interests consistent with the terms contained in this
Agreement, and (v) agree to defend and indemnify Sun and its licensors from
and against any damages, costs, liabilities, settlement amounts and/or
expenses (including attorneys' fees) incurred in connection with any claim,
lawsuit or action by any third party that arises or results from the use or
distribution of any and all Programs and/or Software.

3. Java Technology Restrictions. You may not modify the Java Platform
Interface ("JPI", identified as classes contained within the "java" package
or any subpackages of the "java" package), by creating additional classes
within the JPI or otherwise causing the addition to or modification of the
classes in the JPI.  In the event that you create an additional class and
associated API(s) which (i) extends the functionality of the Java platform,
and (ii) is exposed to third party software developers for the purpose of
developing additional software which invokes such additional API, you must
promptly publish broadly an accurate specification for such API for free use
by all developers.  You may not create, or authorize your licensees to create
additional classes, interfaces, or subpackages that are in any way identified
as "java", "javax", "sun" or similar convention as specified by Sun in any
naming convention designation.

4.  Java Runtime Availability.  Refer to the appropriate version of the Java
Runtime Environment binary code license (currently located at
http://www.java.sun.com/jdk/index.html) for the availability of runtime code
which may be distributed with Java applets and applications.

5. Trademarks and Logos. You acknowledge and agree as between you and Sun
that Sun owns the SUN, SOLARIS, JAVA, JINI, FORTE, and iPLANET trademarks and
all SUN, SOLARIS, JAVA, JINI, FORTE, and iPLANET-related trademarks, service
marks, logos and other brand designations ("Sun Marks"), and you agree to
comply with the Sun Trademark and Logo Usage Requirements currently located
at http://www.sun.com/policies/trademarks. Any use you make of the Sun Marks
inures to Sun's benefit.

6. Source Code. Software may contain source code that is provided solely for
reference purposes pursuant to the terms of this Agreement.  Source code may
not be redistributed unless expressly provided for in this Agreement.

7. Termination for Infringement.  Either party may terminate this Agreement
immediately should any Software become, or in either party's opinion be
likely to become, the subject of a claim of infringement of any intellectual
property right.

8. Third Party Code. Additional copyright notices and license terms
applicable to portions of the Software are set forth in the
THIRDPARTYLICENSEREADME. In addition to any terms and conditions of any third
party open source/freeware license identified in the THIRDPARTYLICENSEREADME,
the disclaimer of warranty and limitation of liability provisions in
paragraphs 5 and 6 of the Binary Code License Agreement shall apply to all
Software in this distribution.

For inquiries please contact: Sun Microsystems, Inc., 4150 Network Circle,
Santa Clara, California 95054, U.S.A

The source code of the Java Advanced Imaging library is available at:

  https://jai.dev.java.net/

--------------------------------------------------------------------------------

The JTS library (the binary file "jts-1.9.jar") is copyright (c) Vivid
Solutions, and is made available under the following license:

                  GNU LESSER GENERAL PUBLIC LICENSE
                       Version 3, 29 June 2007

 Copyright (C) 2007 Free Software Foundation, Inc. <http://fsf.org/>
 Everyone is permitted to copy and distribute verbatim copies
 of this license document, but changing it is not allowed.


  This version of the GNU Lesser General Public License incorporates
the terms and conditions of version 3 of the GNU General Public
License, supplemented by the additional permissions listed below.

  0. Additional Definitions.

  As used herein, "this License" refers to version 3 of the GNU Lesser
General Public License, and the "GNU GPL" refers to version 3 of the GNU
General Public License.

  "The Library" refers to a covered work governed by this License,
other than an Application or a Combined Work as defined below.

  An "Application" is any work that makes use of an interface provided
by the Library, but which is not otherwise based on the Library.
Defining a subclass of a class defined by the Library is deemed a mode
of using an interface provided by the Library.

  A "Combined Work" is a work produced by combining or linking an
Application with the Library.  The particular version of the Library
with which the Combined Work was made is also called the "Linked
Version".

  The "Minimal Corresponding Source" for a Combined Work means the
Corresponding Source for the Combined Work, excluding any source code
for portions of the Combined Work that, considered in isolation, are
based on the Application, and not on the Linked Version.

  The "Corresponding Application Code" for a Combined Work means the
object code and/or source code for the Application, including any data
and utility programs needed for reproducing the Combined Work from the
Application, but excluding the System Libraries of the Combined Work.

  1. Exception to Section 3 of the GNU GPL.

  You may convey a covered work under sections 3 and 4 of this License
without being bound by section 3 of the GNU GPL.

  2. Conveying Modified Versions.

  If you modify a copy of the Library, and, in your modifications, a
facility refers to a function or data to be supplied by an Application
that uses the facility (other than as an argument passed when the
facility is invoked), then you may convey a copy of the modified
version:

   a) under this License, provided that you make a good faith effort to
   ensure that, in the event an Application does not supply the
   function or data, the facility still operates, and performs
   whatever part of its purpose remains meaningful, or

   b) under the GNU GPL, with none of the additional permissions of
   this License applicable to that copy.

  3. Object Code Incorporating Material from Library Header Files.

  The object code form of an Application may incorporate material from
a header file that is part of the Library.  You may convey such object
code under terms of your choice, provided that, if the incorporated
material is not limited to numerical parameters, data structure
layouts and accessors, or small macros, inline functions and templates
(ten or fewer lines in length), you do both of the following:

   a) Give prominent notice with each copy of the object code that the
   Library is used in it and that the Library and its use are
   covered by this License.

   b) Accompany the object code with a copy of the GNU GPL and this license
   document.

  4. Combined Works.

  You may convey a Combined Work under terms of your choice that,
taken together, effectively do not restrict modification of the
portions of the Library contained in the Combined Work and reverse
engineering for debugging such modifications, if you also do each of
the following:

   a) Give prominent notice with each copy of the Combined Work that
   the Library is used in it and that the Library and its use are
   covered by this License.

   b) Accompany the Combined Work with a copy of the GNU GPL and this license
   document.

   c) For a Combined Work that displays copyright notices during
   execution, include the copyright notice for the Library among
   these notices, as well as a reference directing the user to the
   copies of the GNU GPL and this license document.

   d) Do one of the following:

       0) Convey the Minimal Corresponding Source under the terms of this
       License, and the Corresponding Application Code in a form
       suitable for, and under terms that permit, the user to
       recombine or relink the Application with a modified version of
       the Linked Version to produce a modified Combined Work, in the
       manner specified by section 6 of the GNU GPL for conveying
       Corresponding Source.

       1) Use a suitable shared library mechanism for linking with the
       Library.  A suitable mechanism is one that (a) uses at run time
       a copy of the Library already present on the user's computer
       system, and (b) will operate properly with a modified version
       of the Library that is interface-compatible with the Linked
       Version.

   e) Provide Installation Information, but only if you would otherwise
   be required to provide such information under section 6 of the
   GNU GPL, and only to the extent that such information is
   necessary to install and execute a modified version of the
   Combined Work produced by recombining or relinking the
   Application with a modified version of the Linked Version. (If
   you use option 4d0, the Installation Information must accompany
   the Minimal Corresponding Source and Corresponding Application
   Code. If you use option 4d1, you must provide the Installation
   Information in the manner specified by section 6 of the GNU GPL
   for conveying Corresponding Source.)

  5. Combined Libraries.

  You may place library facilities that are a work based on the
Library side by side in a single library together with other library
facilities that are not Applications and are not covered by this
License, and convey such a combined library under terms of your
choice, if you do both of the following:

   a) Accompany the combined library with a copy of the same work based
   on the Library, uncombined with any other library facilities,
   conveyed under the terms of this License.

   b) Give prominent notice with the combined library that part of it
   is a work based on the Library, and explaining where to find the
   accompanying uncombined form of the same work.

  6. Revised Versions of the GNU Lesser General Public License.

  The Free Software Foundation may publish revised and/or new versions
of the GNU Lesser General Public License from time to time. Such new
versions will be similar in spirit to the present version, but may
differ in detail to address new problems or concerns.

  Each version is given a distinguishing version number. If the
Library as you received it specifies that a certain numbered version
of the GNU Lesser General Public License "or any later version"
applies to it, you have the option of following the terms and
conditions either of that published version or of any later version
published by the Free Software Foundation. If the Library as you
received it does not specify a version number of the GNU Lesser
General Public License, you may choose any version of the GNU Lesser
General Public License ever published by the Free Software Foundation.

  If the Library as you received it specifies that a proxy can decide
whether future versions of the GNU Lesser General Public License shall
apply, that proxy's public statement of acceptance of any version is
permanent authorization for you to choose that version for the
Library.

The source code of the JTS library is available at:

  http://sourceforge.net/projects/jts-topo-suite/

--------------------------------------------------------------------------------

The NGUnits library (the binary file ngunits-1.0.jar) is copyright (C) 2010-2012
National Geographic Society and is made available under the following license:

    Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

        Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
        Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
        Neither the name of the National Geographic Society nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The source code of the NGUnits library is available at:

  https://github.com/erussell/ngunits

--------------------------------------------------------------------------------

The Apache Commons codec, httpclient, and logging libraries (the binary
files commons-codec-1.3.jar, commons-httpclient-3.0.1.jar, and
commons-logging-1.1.jar) are copyrighted by The Apache Software Foundation,
and are made available under the following license:

                                 Apache License
                           Version 2.0, January 2004
                        http://www.apache.org/licenses/

   TERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION

   1. Definitions.

      "License" shall mean the terms and conditions for use, reproduction,
      and distribution as defined by Sections 1 through 9 of this document.

      "Licensor" shall mean the copyright owner or entity authorized by
      the copyright owner that is granting the License.

      "Legal Entity" shall mean the union of the acting entity and all
      other entities that control, are controlled by, or are under common
      control with that entity. For the purposes of this definition,
      "control" means (i) the power, direct or indirect, to cause the
      direction or management of such entity, whether by contract or
      otherwise, or (ii) ownership of fifty percent (50%) or more of the
      outstanding shares, or (iii) beneficial ownership of such entity.

      "You" (or "Your") shall mean an individual or Legal Entity
      exercising permissions granted by this License.

      "Source" form shall mean the preferred form for making modifications,
      including but not limited to software source code, documentation
      source, and configuration files.

      "Object" form shall mean any form resulting from mechanical
      transformation or translation of a Source form, including but
      not limited to compiled object code, generated documentation,
      and conversions to other media types.

      "Work" shall mean the work of authorship, whether in Source or
      Object form, made available under the License, as indicated by a
      copyright notice that is included in or attached to the work
      (an example is provided in the Appendix below).

      "Derivative Works" shall mean any work, whether in Source or Object
      form, that is based on (or derived from) the Work and for which the
      editorial revisions, annotations, elaborations, or other modifications
      represent, as a whole, an original work of authorship. For the purposes
      of this License, Derivative Works shall not include works that remain
      separable from, or merely link (or bind by name) to the interfaces of,
      the Work and Derivative Works thereof.

      "Contribution" shall mean any work of authorship, including
      the original version of the Work and any modifications or additions
      to that Work or Derivative Works thereof, that is intentionally
      submitted to Licensor for inclusion in the Work by the copyright owner
      or by an individual or Legal Entity authorized to submit on behalf of
      the copyright owner. For the purposes of this definition, "submitted"
      means any form of electronic, verbal, or written communication sent
      to the Licensor or its representatives, including but not limited to
      communication on electronic mailing lists, source code control systems,
      and issue tracking systems that are managed by, or on behalf of, the
      Licensor for the purpose of discussing and improving the Work, but
      excluding communication that is conspicuously marked or otherwise
      designated in writing by the copyright owner as "Not a Contribution."

      "Contributor" shall mean Licensor and any individual or Legal Entity
      on behalf of whom a Contribution has been received by Licensor and
      subsequently incorporated within the Work.

   2. Grant of Copyright License. Subject to the terms and conditions of
      this License, each Contributor hereby grants to You a perpetual,
      worldwide, non-exclusive, no-charge, royalty-free, irrevocable
      copyright license to reproduce, prepare Derivative Works of,
      publicly display, publicly perform, sublicense, and distribute the
      Work and such Derivative Works in Source or Object form.

   3. Grant of Patent License. Subject to the terms and conditions of
      this License, each Contributor hereby grants to You a perpetual,
      worldwide, non-exclusive, no-charge, royalty-free, irrevocable
      (except as stated in this section) patent license to make, have made,
      use, offer to sell, sell, import, and otherwise transfer the Work,
      where such license applies only to those patent claims licensable
      by such Contributor that are necessarily infringed by their
      Contribution(s) alone or by combination of their Contribution(s)
      with the Work to which such Contribution(s) was submitted. If You
      institute patent litigation against any entity (including a
      cross-claim or counterclaim in a lawsuit) alleging that the Work
      or a Contribution incorporated within the Work constitutes direct
      or contributory patent infringement, then any patent licenses
      granted to You under this License for that Work shall terminate
      as of the date such litigation is filed.

   4. Redistribution. You may reproduce and distribute copies of the
      Work or Derivative Works thereof in any medium, with or without
      modifications, and in Source or Object form, provided that You
      meet the following conditions:

      (a) You must give any other recipients of the Work or
          Derivative Works a copy of this License; and

      (b) You must cause any modified files to carry prominent notices
          stating that You changed the files; and

      (c) You must retain, in the Source form of any Derivative Works
          that You distribute, all copyright, patent, trademark, and
          attribution notices from the Source form of the Work,
          excluding those notices that do not pertain to any part of
          the Derivative Works; and

      (d) If the Work includes a "NOTICE" text file as part of its
          distribution, then any Derivative Works that You distribute must
          include a readable copy of the attribution notices contained
          within such NOTICE file, excluding those notices that do not
          pertain to any part of the Derivative Works, in at least one
          of the following places: within a NOTICE text file distributed
          as part of the Derivative Works; within the Source form or
          documentation, if provided along with the Derivative Works; or,
          within a display generated by the Derivative Works, if and
          wherever such third-party notices normally appear. The contents
          of the NOTICE file are for informational purposes only and
          do not modify the License. You may add Your own attribution
          notices within Derivative Works that You distribute, alongside
          or as an addendum to the NOTICE text from the Work, provided
          that such additional attribution notices cannot be construed
          as modifying the License.

      You may add Your own copyright statement to Your modifications and
      may provide additional or different license terms and conditions
      for use, reproduction, or distribution of Your modifications, or
      for any such Derivative Works as a whole, provided Your use,
      reproduction, and distribution of the Work otherwise complies with
      the conditions stated in this License.

   5. Submission of Contributions. Unless You explicitly state otherwise,
      any Contribution intentionally submitted for inclusion in the Work
      by You to the Licensor shall be under the terms and conditions of
      this License, without any additional terms or conditions.
      Notwithstanding the above, nothing herein shall supersede or modify
      the terms of any separate license agreement you may have executed
      with Licensor regarding such Contributions.

   6. Trademarks. This License does not grant permission to use the trade
      names, trademarks, service marks, or product names of the Licensor,
      except as required for reasonable and customary use in describing the
      origin of the Work and reproducing the content of the NOTICE file.

   7. Disclaimer of Warranty. Unless required by applicable law or
      agreed to in writing, Licensor provides the Work (and each
      Contributor provides its Contributions) on an "AS IS" BASIS,
      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
      implied, including, without limitation, any warranties or conditions
      of TITLE, NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A
      PARTICULAR PURPOSE. You are solely responsible for determining the
      appropriateness of using or redistributing the Work and assume any
      risks associated with Your exercise of permissions under this License.

   8. Limitation of Liability. In no event and under no legal theory,
      whether in tort (including negligence), contract, or otherwise,
      unless required by applicable law (such as deliberate and grossly
      negligent acts) or agreed to in writing, shall any Contributor be
      liable to You for damages, including any direct, indirect, special,
      incidental, or consequential damages of any character arising as a
      result of this License or out of the use or inability to use the
      Work (including but not limited to damages for loss of goodwill,
      work stoppage, computer failure or malfunction, or any and all
      other commercial damages or losses), even if such Contributor
      has been advised of the possibility of such damages.

   9. Accepting Warranty or Additional Liability. While redistributing
      the Work or Derivative Works thereof, You may choose to offer,
      and charge a fee for, acceptance of support, warranty, indemnity,
      or other liability obligations and/or rights consistent with this
      License. However, in accepting such obligations, You may act only
      on Your own behalf and on Your sole responsibility, not on behalf
      of any other Contributor, and only if You agree to indemnify,
      defend, and hold each Contributor harmless for any liability
      incurred by, or claims asserted against, such Contributor by reason
      of your accepting any such warranty or additional liability.

   END OF TERMS AND CONDITIONS

   APPENDIX: How to apply the Apache License to your work.

      To apply the Apache License to your work, attach the following
      boilerplate notice, with the fields enclosed by brackets "[]"
      replaced with your own identifying information. (Don't include
      the brackets!)  The text should be enclosed in the appropriate
      comment syntax for the file format. We also recommend that a
      file or class name and description of purpose be included on the
      same "printed page" as the copyright notice for easier
      identification within third-party archives.

   Copyright [yyyy] [name of copyright owner]

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

The source code of the Commons libraries are available at:

  http://commons.apache.org/codec/,
  http://hc.apache.org/httpclient-3.x/, and
  http://commons.apache.org/logging/
</pre>
