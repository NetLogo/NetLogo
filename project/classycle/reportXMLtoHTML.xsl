<?xml version="1.0" encoding="ISO-8859-1"?>
<!--
   Copyright (c) 2003-2008, Franz-Josef Elmer, All rights reserved.

   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions are met:

   - Redistributions of source code must retain the above copyright notice,
     this list of conditions and the following disclaimer.
   - Redistributions in binary form must reproduce the above copyright notice,
     this list of conditions and the following disclaimer in the documentation
     and/or other materials provided with the distribution.

   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
   TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
   PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
   CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
   EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
   PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
   OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
   WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
   OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
   EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
-->

<!DOCTYPE classycle [
  <!-- ====================================================================
       Mapping of images onto files
       ==================================================================== -->
  <!ENTITY logoImg            "images/logo.png">
  <!ENTITY linkImg            "images/link.png">
  <!ENTITY mixedCycleLinkImg       "images/mixedCycleLink.png">
  <!ENTITY innerCycleLinkImg       "images/innerCycleLink.png">
  <!ENTITY mixImg             "images/mix.png">
  <!ENTITY packageImg         "images/package.png">
  <!ENTITY innerImg           "images/inner.png">
  <!ENTITY classImg           "images/class.png">
  <!ENTITY abstractImg        "images/abstract.png">
  <!ENTITY interfaceImg       "images/interface.png">
  <!ENTITY innerclassImg      "images/innerclass.png">
  <!ENTITY innerabstractImg   "images/innerabstract.png">
  <!ENTITY innerinterfaceImg  "images/innerinterface.png">
]>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">
  <xsl:strip-space elements="*"/>

  <!-- ====================================================================
       Definition of an explanation text appearing several times on the
       page.
       ==================================================================== -->
  <xsl:variable name="infoLine">
    Click on <img src="&linkImg;"/> behind a number and a window will pop up showing more details.
  </xsl:variable>

  <!-- ====================================================================
       Calculates the number of cycles.
       ==================================================================== -->
  <xsl:variable name="numberOfCycles" select="count(/classycle/cycles/cycle)"/>

  <!-- ====================================================================
       Calculates the number of layers.
       ==================================================================== -->
  <xsl:variable name="numberOfLayers">
    <xsl:for-each select="/classycle/classes/class">
      <xsl:sort select="@layer" data-type="number"/>
      <xsl:if test="position()=last()">
        <xsl:value-of select="./@layer + 1"/>
      </xsl:if>
    </xsl:for-each>
  </xsl:variable>

  <!-- ====================================================================
       Calculates the number of classes.
       ==================================================================== -->
  <xsl:variable name="numberOfClasses" select="count(/classycle/classes/class)"/>

  <!-- ====================================================================
       Calculates the number of package cycles.
       ==================================================================== -->
  <xsl:variable name="numberOfPackageCycles" select="count(/classycle/packageCycles/packageCycle)"/>

  <!-- ====================================================================
       Calculates the number of packages.
       ==================================================================== -->
  <xsl:variable name="numberOfPackages" select="count(/classycle/packages/package)"/>

  <!-- ====================================================================
       Matches root element <classycle>. Creates HTML page with headers,
       style sheets, JavaScript, and title.
       ==================================================================== -->
  <xsl:template match="classycle">
    <html>
      <head>
        <title>Classycle Analysis of <xsl:value-of select="/classycle/@title"/>
        </title>
        <style type="text/css">
          body { font-family:Helvetica,Arial,sans-serif; }
          th { background-color:#aaaaaa; }
		  td a img { border-width:0; margin-left:5pt; vertical-align:middle;}
        </style>
        <script type="text/javascript"><![CDATA[
        <!--
          var number = /^\d*$/;

          function showTable(title, headers, content) {
            text = "<h3>" + title + "</h3><p>";
            if (content.length > 0) {
              text += "<table border=\"1\" cellpadding=\"5\" cellspacing=\"0\">";
              if (headers.length > 0) {
                text += "<tr>";
                headerArray = headers.split(",");
                for (i = 0; i < headerArray.length; i++) {
                  text += "<th>" + headerArray[i] + "</th>";
                }
                text += "</tr>";
              }
              rows = content.split(";");
              for (i = 0; i < rows.length; i++) {
                if (rows[i].length > 0) {
                  columns = rows[i].split(",");
                  text += "<tr>";
                  for (j = 0; j < columns.length; j++) {
                    text += "<td" + (number.test(columns[j]) ? " align=\"right\">" : ">") + columns[j] + "</td>";
                  }
                  text += "</tr>";
                }
              }
              text += "</table>";
            }
            showText(text);
          }

          function showText(text) {
            list = window.open("", "list",
                "dependent=yes,location=no,menubar=yes,resizable=yes,toolbar=no,scrollbars=yes,width=500,height=400");
            list.document.close();
            list.document.open();
            list.document.write("<html><head><style type=\"text/css\">");
            list.document.write("body { font-family:Helvetica,Arial,sans-serif; } ");
            list.document.write(".link { cursor:pointer;text-decoration:underline; } ");
            list.document.writeln("th { background-color:#aaaaaa; } </style></head><body>");
            list.document.writeln(text);
            list.document.writeln("</body></html>");
            list.document.close();
            list.focus();
          }
        //-->
        ]]>
        </script>
      </head>
      <body>
        <h1><a href="http://classycle.sourceforge.net" alt="Classcyle Home Page">
              <img src="&logoImg;" alt="Classcyle" width="200"
                   height="135" border="0" align="middle" hspace="4"/>
            </a>
            Analysis of <xsl:value-of select="/classycle/@title"/></h1>
        Date: <xsl:value-of select="/classycle/@date"/>
        <xsl:call-template name="createSummary"/>
        <xsl:if test="$numberOfClasses > 0">
          <xsl:call-template name="createCyclesSection"/>
        </xsl:if>
        <xsl:call-template name="createPackageCyclesSection"/>
        <xsl:if test="$numberOfClasses > 0">
          <xsl:call-template name="createLayersTable"/>
        </xsl:if>
        <xsl:call-template name="createClassesAndPackagesSection"/>
      </body>
    </html>
  </xsl:template>

  <!-- ====================================================================
       Subroutine which creates a short summary.
       ==================================================================== -->
  <xsl:template name="createSummary">
    <h2>Summary</h2>
    <table border="0" cellpadding="0" cellspacing="0">
      <xsl:if test="$numberOfClasses > 0">
        <tr>
          <td align="right">
            <xsl:choose>
              <xsl:when test="$numberOfCycles = 0">no</xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="$numberOfCycles"/>
              </xsl:otherwise>
            </xsl:choose>
          </td>
          <td width="5"/>
          <td>
            <xsl:choose>
              <xsl:when test="$numberOfCycles = 0">class cycles</xsl:when>
              <xsl:when test="$numberOfCycles = 1">
                <a href="#cycles">class cycle</a>
              </xsl:when>
              <xsl:otherwise>
                <a href="#cycles">class cycles</a>
              </xsl:otherwise>
            </xsl:choose>
          </td>
        </tr>
        <tr><td align="right"><xsl:copy-of select="$numberOfLayers"/></td>
            <td width="5"/><td><a href="#layers">class layers</a></td>
        </tr>
        <tr><td align="right"><xsl:value-of select="$numberOfClasses"/></td>
            <td width="5"/>
            <td><a href="#classes">classes</a> (using
              <xsl:value-of select="/classycle/classes/@numberOfExternalClasses"/>
              external classes) grouped in <xsl:value-of select="$numberOfPackages"/> packages
            </td>
        </tr>
      </xsl:if>
      <tr>
        <td align="right">
          <xsl:choose>
            <xsl:when test="$numberOfPackageCycles = 0">no</xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$numberOfPackageCycles"/>
            </xsl:otherwise>
          </xsl:choose>
        </td>
        <td width="5"/>
        <td>
          <xsl:choose>
            <xsl:when test="$numberOfPackageCycles = 0">package cycles</xsl:when>
            <xsl:when test="$numberOfPackageCycles = 1">
              <a href="#packageCycles">package cycle</a>
            </xsl:when>
            <xsl:otherwise>
              <a href="#packageCycles">package cycles</a>
            </xsl:otherwise>
          </xsl:choose>
        </td>
      </tr>
      <xsl:if test="$numberOfClasses = 0">
        <tr>
          <td align="right">
            <xsl:choose>
              <xsl:when test="$numberOfPackages = 0">no</xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="$numberOfPackages"/>
              </xsl:otherwise>
            </xsl:choose>
          </td>
          <td width="5"/>
          <td>
            <xsl:choose>
              <xsl:when test="$numberOfPackages = 1">
                <a href="#packageCycles">package</a>
              </xsl:when>
              <xsl:otherwise>
                <a href="#packageCycles">packages</a>
              </xsl:otherwise>
            </xsl:choose>
          </td>
        </tr>
      </xsl:if>
    </table>
    <p/>
    <xsl:if test="$numberOfClasses > 0">
      <xsl:call-template name="createClassesSummary"/>
    </xsl:if>
  </xsl:template>

  <!-- ====================================================================
       Subroutine creating the summary table of class types.
       ==================================================================== -->
  <xsl:template name="createClassesSummary">
    <xsl:variable name="classes" select="/classycle/classes/class"/>
    <table border="1" cellpadding="5" cellspacing="0" width="770">
      <tr>
        <th>Type</th>
        <th>Number of classes</th>
        <th>Averaged (maximum) size in bytes</th>
        <th>Averaged (maximum) number of usage by other classes</th>
        <th>Averaged (maximum) number of used internal classes</th>
        <th>Averaged (maximum) number of used external clasess</th>
      </tr>
      <xsl:call-template name="summary">
        <xsl:with-param name="type">Interfaces</xsl:with-param>
        <xsl:with-param name="set" select="$classes[@type='interface']"/>
      </xsl:call-template>
      <xsl:call-template name="summary">
        <xsl:with-param name="type">Abstract classes</xsl:with-param>
        <xsl:with-param name="set" select="$classes[@type='abstract class']"/>
      </xsl:call-template>
      <xsl:call-template name="summary">
        <xsl:with-param name="type">Concrete classes</xsl:with-param>
        <xsl:with-param name="set" select="$classes[@type='class']"/>
      </xsl:call-template>
    </table>
  </xsl:template>

  <!-- ====================================================================
       Subroutine which creates the cycles table.
       ==================================================================== -->
  <xsl:template name="createCyclesSection">
    <xsl:if test="$numberOfCycles &gt; 0">
      <h2><a name="cycles">Cycles</a></h2>
      <xsl:copy-of select="$infoLine"/>
      <table border="1" cellpadding="5" cellspacing="0" width="770">
        <tr>
          <th>Name</th>
          <th>Number of classes</th>
          <th>Best Fragment Size</th>
          <th>Girth</th>
          <th>Radius</th>
          <th>Diameter</th>
          <th>Layer</th>
        </tr>
        <xsl:for-each select="/classycle/cycles/cycle">
	      <xsl:sort select="@size" data-type="number" order="descending"/>
          <xsl:call-template name="cycle"/>
        </xsl:for-each>
      </table>
    </xsl:if>
  </xsl:template>

  <!-- ====================================================================
       Subroutine which creates the package cycles table.
       ==================================================================== -->
  <xsl:template name="createPackageCyclesSection">
    <xsl:if test="$numberOfPackageCycles &gt; 0">
      <h2><a name="packageCycles">Package Cycles</a></h2>
      <xsl:copy-of select="$infoLine"/>
      <table border="1" cellpadding="5" cellspacing="0" width="770">
        <tr>
          <th>Name</th>
          <th>Number of packagess</th>
          <th>Best Fragment Size</th>
          <th>Girth</th>
          <th>Radius</th>
          <th>Diameter</th>
          <th>Layer</th>
        </tr>
        <xsl:for-each select="/classycle/packageCycles/packageCycle">
	      <xsl:sort select="@size" data-type="number" order="descending"/>
          <xsl:call-template name="packageCycle"/>
        </xsl:for-each>
      </table>
    </xsl:if>
  </xsl:template>

  <!-- ====================================================================
       Subroutine which creates the table with the layers statistics.
       ==================================================================== -->
  <xsl:template name="createLayersTable">
    <h2><a name="layers">Layers</a></h2>
    <xsl:variable name="dummyList" select="/classycle/classes/class[position() &lt;= $numberOfLayers]"/>
    <xsl:copy-of select="$infoLine"/>
    <table border="1" cellpadding="5" cellspacing="0" width="770">
      <tr>
        <td><b>Layer</b></td>
        <xsl:for-each select="$dummyList">
          <xsl:variable name="layer" select="position() - 1"/>
          <xsl:variable name="set" select="/classycle/classes/class[@layer=$layer]"/>
          <td align="center"><xsl:value-of select="$layer"/></td>
        </xsl:for-each>
      </tr>
      <tr>
        <td><b>Number of classes</b></td>
        <xsl:for-each select="$dummyList">
          <xsl:variable name="layer" select="position() - 1"/>
          <xsl:variable name="set" select="/classycle/classes/class[@layer=$layer]"/>
          <td align="center">
            <xsl:call-template name="createListPopupWithLink">
              <xsl:with-param name="set" select="$set"/>
              <xsl:with-param name="number" select="count($set)"/>
              <xsl:with-param name="text">Classes of layer <xsl:value-of select="$layer"/>:</xsl:with-param>
            </xsl:call-template>
          </td>
        </xsl:for-each>
      </tr>
    </table>
  </xsl:template>


  <!-- ====================================================================
       Subroutine creating the classes and packages section.
       ==================================================================== -->
  <xsl:template name="createClassesAndPackagesSection">
    <h2>
      <a name="classes">
        <xsl:if test="$numberOfClasses > 0">Classes and </xsl:if>Packages
      </a>
    </h2>
    Click on <img src="&mixedCycleLinkImg;" align="center"/> or 
    <img src="&innerCycleLinkImg;" align="center"/> to go to the cycle to
    which the class/package belongs.
    <br/>
    <xsl:copy-of select="$infoLine"/>
    <table cellpadding="3" cellspacing="0" border="1" width="770">
      <tr>
        <th>Class/Package</th>
        <th>Size</th>
        <th>Used by</th>
        <th>Uses internal</th>
        <th>Uses external</th>
        <th>Layer</th>
        <th>Source(s)</th>
      </tr>
      <xsl:for-each select="/classycle/classes/class|/classycle/packages/package">	
        <xsl:sort select="@name" case-order="upper-first" data-type="text"/>
        <tr>
          <td>
			<nobr>
			<xsl:call-template name="showIcon">
				<xsl:with-param name="type" select="@type"/>
				<xsl:with-param name="innerClass" select="@innerClass"/>
			</xsl:call-template>
            <xsl:element name="a">
              <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
              <xsl:value-of select="@name"/>
            </xsl:element>
			<xsl:variable name="name"><xsl:value-of select="@name"/></xsl:variable>
			<xsl:if test="string-length(@cycle) &gt; 0">
			  <a>
				<xsl:attribute name="href">#<xsl:value-of select="translate(@cycle,' ','_')"/></xsl:attribute>
				<xsl:attribute name="title">Member of cycle <xsl:value-of select="@cycle"/></xsl:attribute>
				<xsl:choose>
				  <xsl:when test="contains(@cycle, 'et al.')">
					<img src="&mixedCycleLinkImg;"/>
				  </xsl:when>
				  <xsl:otherwise>
					<img src="&innerCycleLinkImg;"/>
				  </xsl:otherwise>
				</xsl:choose>
              </a>
			</xsl:if>
			</nobr>
          </td>
          <td align="right"><xsl:value-of select="@size"/></td>
          <xsl:if test="boolean(@type)">
            <td align="right">
              <xsl:call-template name="createListPopupWithLink">
                <xsl:with-param name="set" select="classRef[@type='usedBy']"/>
                <xsl:with-param name="number" select="count(classRef[@type='usedBy'])"/>
                <xsl:with-param name="text">Classes using <xsl:value-of select="@name"/>:</xsl:with-param>
              </xsl:call-template>
            </td>
            <td align="right">
              <xsl:call-template name="createListPopupWithLink">
                <xsl:with-param name="set" select="classRef[@type='usesInternal']"/>
                <xsl:with-param name="number" select="count(classRef[@type='usesInternal'])"/>
                <xsl:with-param name="text"><xsl:value-of select="@name"/> uses:</xsl:with-param>
              </xsl:call-template>
            </td>
            <td align="right">
              <xsl:call-template name="createListPopupWithLink">
                <xsl:with-param name="noLink">true</xsl:with-param>
                <xsl:with-param name="set" select="classRef[@type='usesExternal']"/>
                <xsl:with-param name="number" select="count(classRef[@type='usesExternal'])"/>
                <xsl:with-param name="text"><xsl:value-of select="@name"/> uses:</xsl:with-param>
              </xsl:call-template>
            </td>
          </xsl:if>
          <xsl:if test="boolean(@type) = false">
            <td align="right">
              <xsl:call-template name="createListPopupWithLink">
                <xsl:with-param name="set" select="packageRef[@type='usedBy']"/>
                <xsl:with-param name="number" select="count(packageRef[@type='usedBy'])"/>
                <xsl:with-param name="text">Packages using <xsl:value-of select="@name"/>:</xsl:with-param>
              </xsl:call-template>
            </td>
            <td align="right">
              <xsl:call-template name="createListPopupWithLink">
                <xsl:with-param name="set" select="packageRef[@type='usesInternal']"/>
                <xsl:with-param name="number" select="count(packageRef[@type='usesInternal'])"/>
                <xsl:with-param name="text"><xsl:value-of select="@name"/> uses:</xsl:with-param>
              </xsl:call-template>
            </td>
            <td align="right">
              <xsl:call-template name="createListPopupWithLink">
                <xsl:with-param name="noLink">true</xsl:with-param>
                <xsl:with-param name="set" select="packageRef[@type='usesExternal']"/>
                <xsl:with-param name="number" select="count(packageRef[@type='usesExternal'])"/>
                <xsl:with-param name="text"><xsl:value-of select="@name"/> uses:</xsl:with-param>
              </xsl:call-template>
            </td>
          </xsl:if>
          <td align="right"><xsl:value-of select="@layer"/></td>
          <td align="left"><xsl:value-of select="@sources"/></td>
        </tr>
      </xsl:for-each>
    </table>
  </xsl:template>
  
  <xsl:template name="showIcon">
    <xsl:param name="type"/>
    <xsl:param name="innerClass"/>
    <xsl:choose>
      <xsl:when test="$type='class' and $innerClass='true'">
        <img src="&innerclassImg;" alt="inner class" width="20"
             height="20" align="middle" hspace="4"/>
      </xsl:when>
      <xsl:when test="$type='class' and $innerClass='false'">
        <img src="&classImg;" alt="class" width="20" height="20"
             align="middle" hspace="4"/>
      </xsl:when>
      <xsl:when test="$type='abstract class' and $innerClass='true'">
        <img src="&innerabstractImg;" alt="inner class" width="20"
             height="20" align="middle" hspace="4"/>
      </xsl:when>
      <xsl:when test="$type='abstract class' and $innerClass='false'">
        <img src="&abstractImg;" alt="class" width="20" height="20"
            align="middle" hspace="4"/>
      </xsl:when>
      <xsl:when test="$type='interface' and $innerClass='true'">
        <img src="&innerinterfaceImg;" alt="inner class" width="20"
             height="20" align="middle" hspace="4"/>
      </xsl:when>
      <xsl:when test="$type='interface' and $innerClass='false'">
        <img src="&interfaceImg;" alt="class" width="20" height="20"
             align="middle" hspace="4"/>
      </xsl:when>
      <xsl:otherwise>
        <img src="&packageImg;" alt="package" width="20"
             height="20" align="middle" hspace="4"/>
      </xsl:otherwise>
</xsl:choose>
		
  </xsl:template>

  <!-- ====================================================================
       Matches element <cycle>. Creates a row in the cycles table with
       JavaScript popups.
       ==================================================================== -->
  <xsl:template name="cycle">
    <tr>
      <td>
		<a><xsl:attribute name="name"><xsl:value-of select="translate(@name,' ','_')"/></xsl:attribute></a>
        <xsl:choose>
          <xsl:when test="contains(@name,'et al.')">
            <img src="&mixImg;" alt="class" width="20"
                 height="20" align="middle" hspace="4"/>
          </xsl:when>
          <xsl:when test="contains(@name,'inner classes')">
            <img src="&innerImg;" alt="inner class" width="20" height="20"
                 align="middle" hspace="4"/>
          </xsl:when>
        </xsl:choose>
        <xsl:value-of select="@name"/>
      </td>
      <td align="right">
        <xsl:call-template name="classRefWithEccentricityPopup">
          <xsl:with-param name="set" select="classes/classRef"/>
          <xsl:with-param name="text">Classes of cycle <xsl:value-of select="@name"/>:</xsl:with-param>
        </xsl:call-template>
      </td>
      <td align="right">
        <xsl:call-template name="createListPopupWithLink">
          <xsl:with-param name="set" select="bestFragmenters/classRef"/>
          <xsl:with-param name="number" select="@bestFragmentSize"/>
          <xsl:with-param name="text">Best fragmenter(s) of cycle <xsl:value-of select="@name"/>:</xsl:with-param>
        </xsl:call-template>
      </td>
      <td align="right"><xsl:value-of select="@girth"/></td>
      <td align="right">
        <xsl:call-template name="createListPopupWithLink">
          <xsl:with-param name="set" select="centerClasses/classRef"/>
          <xsl:with-param name="number" select="@radius"/>
          <xsl:with-param name="text">Center classes of cycle <xsl:value-of select="@name"/>:</xsl:with-param>
        </xsl:call-template>
      </td>
      <td align="right"><xsl:value-of select="@diameter"/></td>
      <td align="right"><xsl:value-of select="@longestWalk"/></td>
    </tr>
  </xsl:template>

  <!-- ====================================================================
       Matches element <packageCycle>. Creates a row in the cycles table with
       JavaScript popups.
       ==================================================================== -->
  <xsl:template name="packageCycle">
    <tr>
      <td>
		<a><xsl:attribute name="name"><xsl:value-of select="translate(@name,' ','_')"/></xsl:attribute></a>
        <img src="&mixImg;" alt="package" width="20" height="20" align="middle" hspace="4"/>
        <xsl:value-of select="@name"/>
      </td>
      <td align="right">
        <xsl:call-template name="classRefWithEccentricityPopup">
          <xsl:with-param name="set" select="packages/packageRef"/>
          <xsl:with-param name="text">Packages of cycle <xsl:value-of select="@name"/>:</xsl:with-param>
        </xsl:call-template>
      </td>
      <td align="right">
        <xsl:call-template name="createListPopupWithLink">
          <xsl:with-param name="set" select="bestFragmenters/packageRef"/>
          <xsl:with-param name="number" select="@bestFragmentSize"/>
          <xsl:with-param name="text">Best fragmenter(s) of cycle <xsl:value-of select="@name"/>:</xsl:with-param>
        </xsl:call-template>
      </td>
      <td align="right"><xsl:value-of select="@girth"/></td>
      <td align="right">
        <xsl:call-template name="createListPopupWithLink">
          <xsl:with-param name="set" select="centerPackages/packageRef"/>
          <xsl:with-param name="number" select="@radius"/>
          <xsl:with-param name="text">Center packages of cycle <xsl:value-of select="@name"/>:</xsl:with-param>
        </xsl:call-template>
      </td>
      <td align="right"><xsl:value-of select="@diameter"/></td>
      <td align="right"><xsl:value-of select="@longestWalk"/></td>
    </tr>
  </xsl:template>

  <!-- ====================================================================
       Subroutine which creates JavaScript popup listing the specified set
       and the appropriated link.

       parameters:

       set Set of elements with attribute "name".
       number Number show left of the link symbol.
       text Explaining header of the popup

       ==================================================================== -->
  <xsl:template name="createListPopupWithLink">
    <xsl:param name="set"/>
    <xsl:param name="number"/>
    <xsl:param name="text"/>
    <xsl:param name="noLink"/>
    <xsl:value-of select="$number"/>
    <xsl:element name="a">
      <xsl:attribute name="style">cursor:pointer;</xsl:attribute>
      <xsl:attribute name="onClick">
        <xsl:text>javascript:showTable(&quot;</xsl:text>
        <xsl:value-of select="$text"/><xsl:text>&quot;,&quot;&quot;,&quot;</xsl:text>
        <xsl:for-each select="$set">
          <xsl:sort select="@name"/>
          <xsl:if test="boolean($noLink)">
            <xsl:value-of select="@name"/>
          </xsl:if>
          <xsl:if test="boolean($noLink) = false">
            <xsl:call-template name="link">
              <xsl:with-param name="name" select="@name"/>
            </xsl:call-template>
          </xsl:if>
          <xsl:text>;</xsl:text>
        </xsl:for-each>
        <xsl:text>&quot;)</xsl:text>
      </xsl:attribute>
      <img src="&linkImg;" hspace="3"/>
    </xsl:element>
  </xsl:template>

  <!-- ====================================================================
       Subroutine which creates JavaScript popup class references with
       eccentricities.

       parameters:

       set Set of elements with the attributes "name",
           "maximumFragmentSize", and "eccentricity".
       text Explaining header of the popup

       ==================================================================== -->
  <xsl:template name="classRefWithEccentricityPopup">
    <xsl:param name="set"/>
    <xsl:param name="text"/>
    <xsl:value-of select="@size"/>
    <xsl:element name="a">
      <xsl:attribute name="style">cursor:pointer;</xsl:attribute>
      <xsl:attribute name="onClick">
        <xsl:text>javascript:showTable(&quot;</xsl:text>
        <xsl:value-of select="$text"/>
        <xsl:text>&quot;,&quot;Name,Maximum fragment size,Eccentricity&quot;,&quot;</xsl:text>
        <xsl:for-each select="$set">
          <xsl:sort select="@name"/>
          <xsl:call-template name="link">
            <xsl:with-param name="name" select="@name"/>
          </xsl:call-template>
          <xsl:text>,</xsl:text>
          <xsl:value-of select="@maximumFragmentSize"/><xsl:text>,</xsl:text>
          <xsl:value-of select="@eccentricity"/><xsl:text>;</xsl:text>
        </xsl:for-each>
        <xsl:text>&quot;)</xsl:text>
      </xsl:attribute>
      <img src="&linkImg;" hspace="3"/>
    </xsl:element>
  </xsl:template>

  <!-- ====================================================================
       Subroutine which creates a link onto the opener site.

       parameter:

       name of the element and link

       ==================================================================== -->
  <xsl:template name="link">
    <xsl:param name="name"/>
    <xsl:text>&lt;div class='link' onclick='javascript:window.opener.location.href=\"#</xsl:text>
    <xsl:value-of select="$name"/>
    <xsl:text>\"'&gt;</xsl:text>
    <xsl:value-of select="$name"/>
    <xsl:text>&lt;/div&gt;</xsl:text>
  </xsl:template>

  <!-- ====================================================================
       Subroutine which calculates the summary for the specified subset of
       classes and adds a row to the class summary table.

       parameters:

       type Text which will appear in the column "Type"
       set Subset of <class> elements

       ==================================================================== -->
  <xsl:template name="summary">
    <xsl:param name="type"/>
    <xsl:param name="set"/>
    <xsl:param name="totalNumber"/>
    <tr>
      <td>
        <nobr>
          <xsl:value-of select="round(100 * count($set) div $numberOfClasses)"/>% <xsl:value-of select="$type"/>
        </nobr>
      </td>
      <td align="right"><xsl:value-of select="count($set)"/></td>
      <td align="right">
        <xsl:value-of select="round(sum($set/@size) div count($set))"/>
        <xsl:variable name="maxSize">
          <xsl:for-each select="$set">
            <xsl:sort select="@size" data-type="number"/>
            <xsl:if test="position()=last()">
              <xsl:element name="a">
                <xsl:attribute name="href">#<xsl:value-of select="./@name"/></xsl:attribute>
                <xsl:value-of select="./@size"/>
              </xsl:element>
            </xsl:if>
          </xsl:for-each>
        </xsl:variable>
        (<xsl:copy-of select="$maxSize"/>)
      </td>
      <td align="right">
        <xsl:value-of select="round(10 * sum($set/@usedBy) div count($set)) div 10"/>
        <xsl:variable name="maxUsedBy">
          <xsl:for-each select="$set">
            <xsl:sort select="@usedBy" data-type="number"/>
            <xsl:if test="position()=last()">
              <xsl:element name="a">
                <xsl:attribute name="href">#<xsl:value-of select="./@name"/></xsl:attribute>
                <xsl:value-of select="./@usedBy"/>
              </xsl:element>
            </xsl:if>
          </xsl:for-each>
        </xsl:variable>
        (<xsl:copy-of select="$maxUsedBy"/>)
      </td>
      <td align="right">
        <xsl:value-of select="round(10 * sum($set/@usesInternal) div count($set)) div 10"/>
        <xsl:variable name="maxUsesInternal">
          <xsl:for-each select="$set">
            <xsl:sort select="@usesInternal" data-type="number"/>
            <xsl:if test="position()=last()">
              <xsl:element name="a">
                <xsl:attribute name="href">#<xsl:value-of select="./@name"/></xsl:attribute>
                <xsl:value-of select="./@usesInternal"/>
              </xsl:element>
            </xsl:if>
          </xsl:for-each>
        </xsl:variable>
        (<xsl:copy-of select="$maxUsesInternal"/>)
      </td>
      <td align="right">
        <xsl:value-of select="round(10 * sum($set/@usesExternal) div count($set)) div 10"/>
        <xsl:variable name="maxUsesExternal">
          <xsl:for-each select="$set">
            <xsl:sort select="@usesExternal" data-type="number"/>
            <xsl:if test="position()=last()">
              <xsl:element name="a">
                <xsl:attribute name="href">#<xsl:value-of select="./@name"/></xsl:attribute>
                <xsl:value-of select="./@usesExternal"/>
              </xsl:element>
            </xsl:if>
          </xsl:for-each>
        </xsl:variable>
        (<xsl:copy-of select="$maxUsesExternal"/>)
      </td>
    </tr>
  </xsl:template>
</xsl:stylesheet>




