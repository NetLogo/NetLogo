#!/bin/sh
cd ../docs/
htmldoc \
  --strict \
  --duplex \
  --title \
  --titleimage images/title.jpg \
  --bodyfont helvetica \
  --footer c.1 \
  --color \
  --book \
  -f ../NetLogo\ User\ Manual.pdf \
  whatis.html \
  copyright.html \
  versions.html \
  requirements.html \
  contact.html \
  sample.html \
  tutorial1.html \
  tutorial2.html \
  tutorial3.html \
  interface.html \
  infotab.html \
  programming.html \
  transition.html \
  applet.html \
  shapes.html \
  behaviorspace.html \
  systemdynamics.html \
  hubnet.html \
  hubnet-authoring.html \
  modelingcommons.html \
  logging.html \
  controlling.html \
  mathematica.html \
  3d.html \
  extensions.html \
  arraystables.html \
  matrix.html \
  sound.html \
  netlogolab.html \
  profiler.html \
  gis.html \
  nw.html \
  palette.html \
  csv.html \
  faq.html \
  dictionary.html

code=$?

# htmldoc returns 14 when it can't find an external link (which it never can),
# but still generates the pdf just fine. We can safely ignore it. BCH 2/6/2015
if [ $code -eq 14 ]
then
  exit 0
  .
else
  exit $code
  .
fi
