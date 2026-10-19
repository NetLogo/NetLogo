The GIS extension comes bundled with NetLogo, but versions of the extension released outside of full NetLogo releases can be obtained using [the Extensions Manager](http://ccl.northwestern.edu/netlogo/docs/extension-manager.html) in the NetLogo app.

### 1.5.0

See [the README.md file](https://github.com/NetLogo/GIS-Extension/blob/main/README.md) for more information on using the new and updated primitives.

- Add `gis:load-dataset-from-string` to load a dataset from a string instead of a file, so environments without a file system (like NetLogo Web) can load data from embedded resources or remote sources. Shapefiles can be provided either as the base64-encoded content of a zip archive of their parts or as a list of the parts as separate strings.
- Add `gis:store-dataset-to-string` to export a dataset as a GeoJSON or ASCII grid string, or as a base64-encoded zip archive string for shapefiles.
- Add `gis:store-dataset-to-strings` to export a shapefile's parts as separate strings.

### 1.3.0

See [the README.md file](https://github.com/NetLogo/GIS-Extension/blob/main/README.md) for more information on using the new and updated primitives.

- Add `gis:create-turtles-from-points` to quickly make turtles from a point dataset.
- Add `gis:random-point-inside` to get a random point from inside a polygon shape.
- Add `gis:create-turtles-inside-polygon` to create a turtle at a random point inside a polygon shape.
- Allow `gis:find-one-feature` and `gis-find-features` to search with numbers.
- General improvements in error/warning messages to better indicate probable issues with data.
- Allow importing elevation with other data as the `_Z` field.

### 1.2.0

See [the README.md file](https://github.com/NetLogo/GIS-Extension/blob/main/README.md) for more information on using the new and updated primitives.

- Add the ability to use GeoJSON with `gis:load-dataset` and `gis:store-dataset`
- Add `gis:set-property-value` to change values in a dataset.
- Add `gis:project-lat-lon` and `gis:project-lat-lon-ellipsoid` primitives to give the NetLogo coordinates of a latitude and longitude pair.
- Scientific notation in gridfiles is now correctly parsed.

### 1.1.2

This version was bundled with NetLogo 6.2.0.
