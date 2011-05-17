#!/usr/bin/python
#
# This script needs to be run from the base "netlogo" folder, so that it can find
# the file "docs/dictionary.html", and also put files in the proper output folders.
#
# The outputs are:  everything in the "docs/dict/*" folder
#                   resources/system/dict.txt
#



import re, os

def unescapeHTML(text):
    # NOTE: this list doesn't cover nearly all of the special symbols,
    #       but it should be good enough for this purpose.
    HTML_ESCAPE_CHARS = (('&', '&amp;'),
                    ('<', '&lt;'),
                    ('>', '&gt;'),
                    ('"', '&quot;'),
                    (' ', '&nbsp;'))
    for c, cEscape in HTML_ESCAPE_CHARS:
        text = text.replace(cEscape, c)
    return text

def stripHTMLTags(text):
    finished = 0
    while not finished:
        finished = 1
        # check if there is an open tag left
        start = text.find("<")
        if start >= 0:
            # if there is, check if the tag gets closed
            stop = text[start:].find(">")
            if stop >= 0:
                # if it does, strip it, and continue loop
                if (text[start+1:start+stop].startswith( "breed" ) ):
                    text = text[:start] + text[start+1:start+stop] + text[start+stop+1:]
                else:
                    text = text[:start] + text[start+stop+1:]
                finished = 0
    return text


def makeValidFileName(s):
    s = unescapeHTML(stripHTMLTags(s.lower()))
    validChars = '-0123456789abcdefghijklmnopqrstuvwxyz'
    newS = ""
    for i in range(len(s)):
        if (s[i] in validChars):
            newS = newS + s[i]
    s = newS
    ### s = ''.join(c in validChars and c or '' for c in s)
    s = s.strip('-')
    #s = s.replace('-','_') 
    return s

def isOkayPrimName(s):
    s = unescapeHTML(s.lower())
    # special case for operators
    if (len(s) <= 2):
        if s in "+|-|*|/|^|<|>|=|!=|<=|>=" and (not '|' in s):
            return True
    
    validChars = '_?-0123456789abcdefghijklmnopqrstuvwxyz'
    newS = ""
    for i in range(len(s)):
        if (s[i] in validChars):
            newS = newS + s[i]
    #scrubbedS = ''.join(c in validChars and c or '' for c in s)
    return s == newS
    
def makeHTMLFile(anchorName, snippet):
    # Grab the title out of the <h3> block, if there is one.
    try:
        myregex = re.compile(r'<h3>(.*?)</h3>', re.DOTALL | re.IGNORECASE)
        title = "NetLogo Help: " + stripHTMLTags(myregex.findall(snippet)[0])
    except:
        title = "NetLogo Help"
    
    #fix links to anchors
    snippet = re.sub(r'href[\s]*=[\s]*"#', 'href="dictionary.html#', snippet)
    #fix all links to use directory above
    snippet = re.sub(r'href[\s]*=[\s]*"', 'href="../', snippet)
    #fix any absolute links we messed up with the regex on the line above
    snippet = snippet.replace('"../http:','"http:')
    #fix references to images
    snippet = re.sub(r'src[\s]*=[\s]*"images', 'src="../images', snippet)
    return '''<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN"
"http://www.w3.org/TR/html4/strict.dtd">
<head>
<title>''' + title + '''</title>

    <link rel="stylesheet" href="../netlogo.css" type="text/css">
    <meta http-equiv="Content-Type" content="text/html; charset=us-ascii">
<style type="text/css">
 p { margin-left: 1.5em ; }
 h3 { font-size: 115% ; }
 h4 { font-size: 100% ; margin-left: 1.5em ; background: white ; }
</style>
</head>
<body>''' + snippet + '''
<p>Take me to the full <a href="../index2.html">NetLogo Dictionary</a>
</body>
</html>
'''

# For each <div> chunk, we pull out the anchor names that are used to
# refer to this chunk, e.g. "clear-patches" and "cp" both belong
# to the same <div> chunk.
# We put both the original chunk and this list of anchors into a
# dictionary (hash) that is indexed by a file name, which is created by
# taking the first anchor-name (e.g. "clear-patches"), and turning it into
# a file name (e.g. "clear_patches.html").
def findEntrySnippets(primEntryChecks, suffix):
        entrySnippets = {}
        for i in range(len(primEntryChunks)):
                # get whatever is inside the <a>XXXX</a> tags.
                myregex = re.compile(r'<a.*?name[\w]*=.*?>(.*?)</a>', re.DOTALL | re.IGNORECASE)
                l = myregex.findall(primEntryChunks[i])
                # trim whitespace from each item
                l = [ unescapeHTML(x.strip()) for x in l ]  
                if (len(l) > 0):
                    validFileName = makeValidFileName(l[0])
                    if (len(validFileName) > 0):
                        entrySnippets[validFileName + suffix] = [ primEntryChunks[i] , l ]
        return entrySnippets

# we create a file that we contains the information to
# match each anchor name to the html file that
# contains information about it
def createIndex(indexFile, entrySnippets):
        sortedKeys = entrySnippets.keys()
        sortedKeys.sort()
        for fname in sortedKeys:
            for anchor in entrySnippets[fname][1]:
                if ( isOkayPrimName( anchor ) ) :
                    indexFile.write(anchor + " " + fname + "\n")
        indexFile.close()
        return sortedKeys

# now we create each of the HTML files, one for each <div> chunk.
def createWeeFiles(sortedKeys, entrySnippets):
        for fname in sortedKeys:
                f = file("docs/dict/"+fname, "w")
                f.write(makeHTMLFile(entrySnippets[fname][1][0], entrySnippets[fname][0]))
                f.close()

#
# start main program
#

# We read in the dictionary.html file, and pull out each of the
# "primitive" entry chunks, which are defined by <div> markers.
f = file("docs/dictionary.html","r")
htmlStr = f.read()
f.close()

myregex = re.compile(r'<div class="dict_entry">(.*?)</div>', re.DOTALL | re.IGNORECASE)
primEntryChunks = myregex.findall( htmlStr )
entries = findEntrySnippets( primEntryChunks, ".html" )

sortedKeys = createIndex(file("resources/system/dict.txt", "w"), entries)

try:
        os.mkdir("docs/dict")
except OSError:
        pass

createWeeFiles(sortedKeys, entries)

f = file("docs/3d.html","r")
htmlStr = f.read()
f.close()

primEntryChunks = myregex.findall(htmlStr)
entries = findEntrySnippets(primEntryChunks, "3d.html")

sortedKeys = createIndex(file("resources/system/dict3d.txt", "w"), entries)

createWeeFiles(sortedKeys, entries)
