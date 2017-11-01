### Paikkatietoikkuna Terrain Profile

Terrain Profile Backend Service. You send a 2D GeoJSON LineString in EPSG:3067, I respond with a 3D MultiPoint. Each Point resides on your LineString and has the altitude as the third dimension value.

The logic:

1. Client ==> GeoJSON Feature/LineString ==> ActionHandler
2. ActionHandler ==> WCS Request ==> WCS Service
3. WCS Service ==> DEM / GeoTIFF ==> ActionHandler
4. ActionHandler ==> GeoJSON Feature/MultiPoint ==> Client

Requires following properties:

property | description
-------- | -----------
`terrain.profile.wcs.endPoint` | URL of the WCS service, query string is NOT ALLOED (e.g. don't specify ?, otherwise it won't work)
`terrain.profile.wcs.demCoverageId`| id of the DEM coverage in the WCS service

Available parameters per request feature.properties.$key:

property | description
-------- | -----------
numPoints | Number of points you want back (default 100). If your LineString has more coordinates than this value, we will use that number. Maximum number of points is 1000 (even if your LineString has more coordinates than that).
resolution | *Ignored at the moment*. Used for describing the level-of-detail you're interested in.

Response properties in feature.properties.$key:

property | description
-------- | -----------
numPoints | Number of points.
distanceFromStart | Array of numbers, each describing the distance from the begin of the LineString. Numbers are ordered and evenly spaced, unless requested numPoints was less than number of coordinates in the requested LineString (see previous table)
resolution | *Ignored at the moment*.