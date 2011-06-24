#!/bin/sh
exec bin/scala -classpath bin -deprecation -nocompdaemon "$0" "$@" 
!# 
// Local Variables:
// mode: scala
// End:

// use 'tidy' to tidy up docs/*.html
// (see tidy man page at: http://tidy.sourceforge.net/docs/tidy_man.html)

import Scripting.{shell,read}

shell("tidy -version").foreach(println(_))
for{file <- shell("""find docs -name \*.html""")
    if !file.containsSlice("/scaladoc/")
    if !file.containsSlice("/dict")}
{
  println(file + ":")
  val doctype =
    if(file.endsWith("/headings.html") || file.endsWith("/primitives.html"))
      "loose"
    else "strict"
  // -m: modify file in place
  // -q: suppress inessential output
  shell("tidy -m -q --clean no --indent yes --hide-endtags yes --doctype " + doctype +
        " --ncr no --break-before-br yes --wrap-sections no --ascii-chars yes --gnu-emacs yes --tidy-mark no --quote-marks yes --wrap 76 " +
        file,false)
  // we want double quotes as &quot; so as not to confuse the HTML syntax highlighter in Emacs, but
  // we don't want single quotes as &#39; because that's annoying
  shell("""perl -pi -e "s/&#39;/\'/g" """ + file)
}
