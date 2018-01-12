<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xsd="http://www.w3.org/2001/XMLSchema"
  xmlns:html="http://www.w3.org/1999/xhtml"
  xmlns:netlogox="http://ccl.northwestern.edu/netlogo/netlogox/6.1"
  version="1.0">

  <xsl:output
    method="html"
    version="5.0"
    doctype-system="about:legacy-compat"
    encoding="UTF-8"
    indent="yes" />

  <!-- Some ways to enhance this documentation:
       * Add expand / collapse for elements
       * Generate examples for composite types
  -->

  <xsl:template match="/">
    <html>
      <head>
        <title>The NetLogo File Format</title>
        <link rel="stylesheet" href="netlogo.css" type="text/css" />
        <link rel="stylesheet" href="nlogox.css"  type="text/css" />
      </head>
      <body>
        <div class="content">
          <h1>The <span class="mono">nlogox</span> file format</h1>
          <div class="topic" id="about">
            <h2>About</h2>
            <xsl:copy-of select="document('autogen/docs/intro-about.xml')" />
            <h3>Table of Contents</h3>
            <ul class="toc">
              <li>
                <a class="toc-topic" href="#file-sections">File Sections</a>
                <ul class="toc-subtopics">
                  <xsl:apply-templates select="xsd:schema/xsd:complexType[@name='Model']/xsd:all/xsd:element" mode="toc-links">
                    <xsl:with-param name="sectionType">section</xsl:with-param>
                  </xsl:apply-templates>
                </ul>
              </li>
              <li>
                <a class="toc-topic" href="#composite-types">Composite Types</a>
                <ul class="toc-subtopics">
                  <li>
                    <a class="toc-subtopic" href="#legend">Legend (How to Use)</a>
                  </li>
                  <xsl:apply-templates select="xsd:schema/xsd:complexType[@name != 'Model']" mode="toc-links">
                    <xsl:sort select="@name" />
                    <xsl:with-param name="sectionType">type</xsl:with-param>
                  </xsl:apply-templates>
                </ul>
              </li>
              <li>
                <a class="toc-topic" href="#atomic-types">Atomic Types</a>
                <ul class="toc-subtopics">
                  <xsl:apply-templates select="document('autogen/docs/atomic-types-legend.xml')/html:div/html:div" mode="toc-atomic-links">
                    <xsl:sort select="./html:h3" />
                  </xsl:apply-templates>
                </ul>
              </li>
              <li>
                <a class="toc-subtopic" href="#enumeration-types">Enumerations</a>
                <ul class="toc-subtopics">
                  <xsl:apply-templates
                    select="/xsd:schema/xsd:complexType/xsd:attribute[xsd:simpleType/xsd:restriction/xsd:enumeration] | /xsd:schema/xsd:complexType/xsd:simpleContent/xsd:extension/xsd:attribute[xsd:simpleType/xsd:restriction/xsd:enumeration]" mode="toc-enum-links">
                    <xsl:sort select="@name" />
                  </xsl:apply-templates>
                </ul>
              </li>
            </ul>
          </div>
          <div class="topic" id="file-sections">
            <h2>File Sections</h2>
            <xsl:apply-templates select="xsd:schema/xsd:complexType[@name='Model']/xsd:all/xsd:element" mode="file-section" />
          </div>
          <div class="topic" id="composite-types">
            <h2>Composite Types</h2>
            <xsl:copy-of select="document('autogen/docs/complex-types-legend.xml')" />
            <xsl:apply-templates select="xsd:schema/xsd:complexType[@name != 'Model']" mode="type-section">
              <xsl:sort select="@name" />
            </xsl:apply-templates>
          </div>
          <xsl:copy-of select="document('autogen/docs/atomic-types-legend.xml')" />
          <div class="topic" id="enumerations">
            <h2>Enumerations</h2>
            <p>Enumerations are special types which can take on one of only a few
              possible values, each with a distinct and special meaning.</p>
            <xsl:apply-templates
              select="/xsd:schema/xsd:complexType/xsd:attribute[xsd:simpleType/xsd:restriction/xsd:enumeration] | /xsd:schema/xsd:complexType/xsd:simpleContent/xsd:extension/xsd:attribute[xsd:simpleType/xsd:restriction/xsd:enumeration]"
              mode="enum-section" />
          </div>
        </div>
      </body>
    </html>
  </xsl:template>


  <xsl:template match="xsd:element | xsd:complexType" mode="toc-links">
    <xsl:param name="sectionType" />
    <xsl:variable name="sectionName" select="@name" />
    <li>
      <a class="toc-subtopic">
        <xsl:attribute name="href">#<xsl:value-of select="$sectionType" />-<xsl:value-of select="$sectionName" /></xsl:attribute>
        <xsl:value-of select="$sectionName" />
      </a>
    </li>
  </xsl:template>

  <xsl:template match="html:div" mode="toc-atomic-links">
    <li>
      <a class="toc-subtopic">
        <xsl:attribute name="href">#<xsl:value-of select="@id" /></xsl:attribute>
        <xsl:value-of select="html:h3" />
      </a>
    </li>
  </xsl:template>

  <xsl:template match="xsd:attribute" mode="toc-enum-links">
    <xsl:variable name="effectiveName">
      <xsl:choose>
        <xsl:when test="string-length(xsd:annotation/xsd:documentation[@netlogox:docType = 'enumName']) > 0">
          <xsl:value-of select="xsd:annotation/xsd:documentation[@netlogox:docType = 'enumName']" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="@name" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <li>
      <a class="toc-subtopic">
        <xsl:attribute name="href">#enum-<xsl:value-of select="$effectiveName" /></xsl:attribute>
        <xsl:value-of select="$effectiveName" />
      </a>
    </li>
  </xsl:template>

  <xsl:template match="xsd:element" mode="file-section">
    <div class="section" id="section-{@name}">
      <h3 class="section-name"><xsl:value-of select="@name" /></h3>
      <span class="section-type">
        Type:
        <xsl:choose>
          <xsl:when test="starts-with(@type, 'xsd:')">
            <xsl:apply-templates select="." mode="construct-type-link" />
          </xsl:when>
          <xsl:when test="starts-with(@type, 'netlogox:')">
            <xsl:variable name="typeName" select="substring-after(@type, 'netlogox:')" />
            <xsl:variable name="maxOccursOfType"
              select="string(/xsd:schema/xsd:complexType[@name=$typeName]/xsd:choice/@maxOccurs)" />
            <xsl:variable name="prefix">
              <xsl:if test="$maxOccursOfType != '' and $maxOccursOfType != '1'">List of </xsl:if>
            </xsl:variable>
            <xsl:variable name="suffix">
              <xsl:if test="$maxOccursOfType != '' and $maxOccursOfType != '1'">s</xsl:if>
            </xsl:variable>
            <xsl:value-of select="$prefix" />
            <xsl:apply-templates select="." mode="construct-type-link" />
            <xsl:value-of select="$suffix" />
          </xsl:when>
          <xsl:when test="count(xsd:complexType/xsd:sequence) > 0 and starts-with(xsd:complexType/xsd:sequence/xsd:element/@type, 'netlogox:')">
            List of <xsl:apply-templates select="xsd:complexType/xsd:sequence/xsd:element" mode="construct-type-link" />s
          </xsl:when>
        </xsl:choose>
      </span>
      <p><xsl:copy-of select="xsd:annotation/xsd:documentation" /></p>
    </div>
  </xsl:template>

  <xsl:template match="xsd:complexType" mode="type-section-header">
    <xsl:param name="title" />
    <xsl:param name="icon" />
    <xsl:param name="description" />
    <xsl:param name="descriptionAnchor" />
    <xsl:variable name="descriptionHref">#legend-<xsl:value-of select="$descriptionAnchor" /></xsl:variable>
    <div class="element-type">
      <div class="element-type-title">
        <a class="element-description">
          <xsl:attribute name="href"><xsl:value-of select="$descriptionHref" /></xsl:attribute>
          <xsl:value-of select="$title" />
        </a>
      </div>
      <div class="element-type-description">
        <xsl:copy-of select="$description" />
      </div>
      <div class="element-icon">
        <a>
          <xsl:attribute name="href"><xsl:value-of select="$descriptionHref" /></xsl:attribute>
          <img class="element-icon">
            <xsl:attribute name="src">images/nlogox/<xsl:value-of select="$icon" /></xsl:attribute>
          </img>
        </a>
      </div>
    </div>
  </xsl:template>

  <xsl:template match="xsd:complexType" mode="type-section">
    <div class="type-section" id="type-{@name}">
      <h3 class="type-name"><xsl:value-of select="@name" /></h3>
      <p><xsl:copy-of select="xsd:annotation/xsd:documentation" /></p>
      <xsl:variable name="attributeGroups">
        <xsl:for-each select="xsd:attributeGroup"><xsl:value-of select="substring-after(@ref, 'netlogox:')" />,</xsl:for-each>
      </xsl:variable>
      <xsl:variable name="allAttributes"
        select="xsd:attribute | /xsd:schema/xsd:attributeGroup[contains($attributeGroups, @name)]/xsd:attribute" />
      <xsl:choose>
        <xsl:when test="count(xsd:choice/xsd:element) > 0">
          <xsl:apply-templates select="." mode="type-section-header">
            <xsl:with-param name="title">Choice</xsl:with-param>
            <xsl:with-param name="icon">Choice.svg</xsl:with-param>
            <xsl:with-param name="description">This type "is one of" the types listed below:</xsl:with-param>
            <xsl:with-param name="descriptionAnchor">choice</xsl:with-param>
          </xsl:apply-templates>
          <table class="cols2">
            <thead>
              <tr><th>name</th><th>type</th></tr>
            </thead>
            <tbody>
              <xsl:apply-templates select="xsd:choice/xsd:element" mode="type-elements">
                <xsl:sort select="@name" />
                <xsl:with-param name="tableMode">nameAndType</xsl:with-param>
              </xsl:apply-templates>
            </tbody>
          </table>
        </xsl:when>
        <xsl:when test="count(xsd:all/xsd:element) > 0">
          <xsl:apply-templates select="." mode="type-section-header">
            <xsl:with-param name="title">Unordered Elements</xsl:with-param>
            <xsl:with-param name="icon">UnorderedElements.svg</xsl:with-param>
            <xsl:with-param name="description">This type contains the child elements listed below, in any order:</xsl:with-param>
            <xsl:with-param name="descriptionAnchor">unordered-elements</xsl:with-param>
          </xsl:apply-templates>
          <table class="cols4">
            <thead>
              <tr><th>name</th><th>type</th><th>required?</th><th>default</th></tr>
            </thead>
            <tbody>
              <xsl:apply-templates select="xsd:all/xsd:element" mode="type-elements">
                <xsl:sort select="@name" />
              </xsl:apply-templates>
            </tbody>
          </table>
        </xsl:when>
        <xsl:when test="count(xsd:sequence) > 0 and count(xsd:sequence/xsd:choice | xsd:sequence/xsd:element) > 1">
          <xsl:apply-templates select="." mode="type-section-header">
            <xsl:with-param name="title">Sequential Elements</xsl:with-param>
            <xsl:with-param name="icon">SequentialElements.svg</xsl:with-param>
            <xsl:with-param name="description">This type contains the following elements in the given order:</xsl:with-param>
            <xsl:with-param name="descriptionAnchor">sequential-elements</xsl:with-param>
          </xsl:apply-templates>
          <table class="cols4">
            <thead>
              <tr><th>name</th><th>type</th><th>occurs at least</th><th>occurs at most</th></tr>
            </thead>
            <tbody>
              <xsl:apply-templates select="xsd:sequence/xsd:element | xsd:sequence/xsd:choice" mode="type-elements">
                <xsl:with-param name="tableMode">withMinMax</xsl:with-param>
              </xsl:apply-templates>
            </tbody>
          </table>
        </xsl:when>
        <xsl:when test="count(xsd:sequence) > 0 and count(xsd:sequence/xsd:choice) = 1">
          <xsl:apply-templates select="." mode="type-section-header">
            <xsl:with-param name="title">Heterogenous Sequence</xsl:with-param>
            <xsl:with-param name="icon">HeterogenousSequence.svg</xsl:with-param>
            <xsl:with-param name="description">This type is &quot;an ordered list whose elements are&quot; these types:</xsl:with-param>
            <xsl:with-param name="descriptionAnchor">heterogenous-sequence</xsl:with-param>
          </xsl:apply-templates>
          <table class="cols2">
            <thead>
              <tr><th>name</th><th>type</th></tr>
            </thead>
            <tbody>
              <xsl:apply-templates select="xsd:sequence/xsd:choice/xsd:element" mode="type-elements">
                <xsl:sort select="@name" />
                <xsl:with-param name="tableMode">nameAndType</xsl:with-param>
              </xsl:apply-templates>
            </tbody>
          </table>
        </xsl:when>
        <xsl:when test="count(xsd:sequence) > 0 and count(xsd:sequence/xsd:element) = 1">
          <xsl:variable name="typeName" select="substring-after(xsd:sequence/xsd:element/@type, ':')" />
          <xsl:apply-templates select="." mode="type-section-header">
            <xsl:with-param name="title">Sequential List</xsl:with-param>
            <xsl:with-param name="icon">SequentialList.svg</xsl:with-param>
            <xsl:with-param name="description">This is "an ordered list of"
              <xsl:apply-templates select="xsd:sequence/xsd:element[1]" mode="construct-type-link" />
            </xsl:with-param>
            <xsl:with-param name="descriptionAnchor">sequential-list</xsl:with-param>
          </xsl:apply-templates>
        </xsl:when>
        <xsl:when test="count(xsd:simpleContent/xsd:extension) = 1">
          <xsl:variable name="typeName" select="substring-after(xsd:simpleContent/xsd:extension/@base, ':')" />
          <xsl:variable name="hasAttributes"
            select="count(xsd:simpleContent/xsd:extension/xsd:attribute | xsd:simpleContent/xsd:extension/xsd:attributeGroup) > 0" />
          <xsl:apply-templates select="." mode="type-section-header">
            <xsl:with-param name="title">Annotated Atom</xsl:with-param>
            <xsl:with-param name="icon">AnnotatedAtom.svg</xsl:with-param>
            <xsl:with-param name="description">This element contains a
              <a>
                <xsl:attribute name="href">#atomic-<xsl:value-of select="$typeName" /></xsl:attribute>
                <xsl:value-of select="$typeName" />
              </a>
              <xsl:if test="not($hasAttributes)">
                and has no attributes.
              </xsl:if>
              <xsl:if test="$hasAttributes">
                and is marked with the following attributes:
              </xsl:if>
            </xsl:with-param>
            <xsl:with-param name="descriptionAnchor">annotated-atom</xsl:with-param>
          </xsl:apply-templates>
          <xsl:if test="$hasAttributes">
            <table class="cols4">
              <thead>
                <tr><th>name</th><th>type</th><th>required?</th><th>default</th></tr>
              </thead>
              <tbody>
                <xsl:apply-templates select="xsd:simpleContent/xsd:extension/xsd:attribute" mode="type-attribute">
                  <xsl:sort select="@name" />
                </xsl:apply-templates>
              </tbody>
            </table>
          </xsl:if>
        </xsl:when>
        <xsl:when test="count($allAttributes) > 0">
          <xsl:apply-templates select="." mode="type-section-header">
            <xsl:with-param name="title">Annotated Element</xsl:with-param>
            <xsl:with-param name="icon">AnnotatedElement.svg</xsl:with-param>
            <xsl:with-param name="description">This element is defined by the following attributes:</xsl:with-param>
            <xsl:with-param name="descriptionAnchor">annotated-element</xsl:with-param>
          </xsl:apply-templates>
        </xsl:when>
        <xsl:when test="count($allAttributes) = 0">
          <xsl:apply-templates select="." mode="type-section-header">
            <xsl:with-param name="title">Empty Element</xsl:with-param>
            <xsl:with-param name="icon">EmptyElement.svg</xsl:with-param>
            <xsl:with-param name="description">This element has no children and no attributes.</xsl:with-param>
            <xsl:with-param name="descriptionAnchor">empty-element</xsl:with-param>
          </xsl:apply-templates>
        </xsl:when>
      </xsl:choose>
      <xsl:if test="count($allAttributes) > 0">
        <span class="attribute-heading">Attributes:</span>
        <table class="cols4">
          <thead>
            <tr><th>name</th><th>type</th><th>required?</th><th>default</th></tr>
          </thead>
          <tbody>
            <xsl:apply-templates select="$allAttributes" mode="type-attribute">
              <xsl:sort select="@name" />
            </xsl:apply-templates>
          </tbody>
        </table>
      </xsl:if>
    </div>
  </xsl:template>

  <xsl:template match="xsd:attribute | xsd:choice | xsd:element" mode="construct-type-link">
    <xsl:param name="classes"></xsl:param>
    <xsl:variable name="typeName" select="substring-after(@type, ':')" />
    <xsl:variable name="isAtom" select="count(/xsd:schema/xsd:complexType[@name=$typeName]) = 0" />
    <a>
      <xsl:if test="string-length($classes) > 0">
        <xsl:attribute name="class"><xsl:value-of select="$classes" /></xsl:attribute>
      </xsl:if>
      <xsl:choose>
        <xsl:when test="not($isAtom)">
          <xsl:attribute name="href">#type-<xsl:value-of select="$typeName" /></xsl:attribute>
          <xsl:value-of select="$typeName" />
        </xsl:when>
        <xsl:when test="$typeName">
          <xsl:attribute name="href">#atomic-<xsl:value-of select="$typeName" /></xsl:attribute>
          <xsl:value-of select="$typeName" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:variable name="effectiveName">
            <xsl:choose>
              <xsl:when test="string-length(xsd:annotation/xsd:documentation[@netlogox:docType = 'enumName']) > 0">
                <xsl:value-of select="xsd:annotation/xsd:documentation[@netlogox:docType = 'enumName']" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="@name" />
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
          <xsl:attribute name="href">#enum-<xsl:value-of select="$effectiveName" /></xsl:attribute>
          <xsl:value-of select="$effectiveName" />
        </xsl:otherwise>
      </xsl:choose>
    </a>
  </xsl:template>

  <xsl:template match="xsd:attribute" mode="type-attribute">
    <tr>
      <td><span class="mono"><xsl:value-of select="@name" /></span></td>
      <td>
        <xsl:apply-templates select="." mode="construct-type-link" />
      </td>
      <td>
        <xsl:choose>
          <xsl:when test="@use = 'required'">yes</xsl:when>
          <xsl:otherwise>no</xsl:otherwise>
        </xsl:choose>
      </td>
      <td>
        <xsl:choose>
          <xsl:when test="@use = 'required'">-</xsl:when>
          <xsl:when test="string(@default) = ''">""</xsl:when>
          <xsl:otherwise><xsl:value-of select="@default" /></xsl:otherwise>
        </xsl:choose>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="xsd:element | xsd:choice" mode="type-elements">
    <!-- tableMode should be one of 'nameAndType', 'withRequiredDefault', 'withMinMax' -->
    <xsl:param name="tableMode">withRequiredDefault</xsl:param>
    <xsl:variable name="tdOne">
      <xsl:choose>
        <xsl:when test="local-name(.) = 'choice'">
        <span class="choice-text">One of:</span>
        <ul>
          <xsl:for-each select="xsd:element">
            <li><span class="mono"><xsl:value-of select="@name" /></span></li>
          </xsl:for-each>
        </ul>
        </xsl:when>
        <xsl:otherwise>
          <span class="mono"><xsl:value-of select="@name" /></span>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="tdTwo">
      <xsl:choose>
        <xsl:when test="local-name(.) = 'choice'">
          <span class="choice-text">corresponding to:</span>
          <ul>
            <xsl:for-each select="xsd:element">
              <li>
                <xsl:apply-templates select="." mode="construct-type-link" />
              </li>
            </xsl:for-each>
          </ul>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="." mode="construct-type-link" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <tr>
      <td><xsl:copy-of select="$tdOne" /></td>
      <td><xsl:copy-of select="$tdTwo" /></td>
      <xsl:apply-templates select="." mode="type-required-entries">
        <xsl:with-param name="tableMode">
          <xsl:value-of select="$tableMode" />
        </xsl:with-param>
      </xsl:apply-templates>
    </tr>
  </xsl:template>

  <xsl:template match="xsd:element | xsd:choice" mode="type-required-entries">
    <xsl:param name="tableMode">nameAndType</xsl:param>
    <xsl:if test="$tableMode != 'nameAndType'">
      <xsl:if test="$tableMode = 'withRequiredDefault'">
        <td>
          <xsl:choose>
            <xsl:when test="@minOccurs = '0'">no</xsl:when>
            <xsl:otherwise>yes</xsl:otherwise>
          </xsl:choose>
        </td>
        <td>
          <xsl:choose>
            <xsl:when test="not(@minOccurs) or @minOccurs != '0'">-</xsl:when>
            <xsl:when test="@type='xsd:string' and (not(@default) or string(@default) = '')">""</xsl:when>
            <xsl:otherwise><xsl:value-of select="@default" /></xsl:otherwise>
          </xsl:choose>
        </td>
      </xsl:if>
      <xsl:if test="$tableMode = 'withMinMax'">
        <td>
          <xsl:choose>
            <xsl:when test="not(@minOccurs) or @minOccurs = ''">1 time</xsl:when>
            <xsl:otherwise><xsl:value-of select="@minOccurs" /> times</xsl:otherwise>
          </xsl:choose>
        </td>
        <td>
          <xsl:choose>
            <xsl:when test="not(@maxOccurs) or @maxOccurs = ''">1 time</xsl:when>
            <xsl:otherwise><xsl:value-of select="@maxOccurs" /> times</xsl:otherwise>
          </xsl:choose>
        </td>
      </xsl:if>
    </xsl:if>
  </xsl:template>

  <xsl:template match="xsd:attribute" mode="enum-section">
    <xsl:variable name="effectiveName">
      <xsl:choose>
        <xsl:when test="string-length(xsd:annotation/xsd:documentation[@netlogox:docType = 'enumName']) > 0">
          <xsl:value-of select="xsd:annotation/xsd:documentation[@netlogox:docType = 'enumName']" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="@name" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <div class="section">
      <xsl:attribute name="id">enum-<xsl:value-of select="$effectiveName"/></xsl:attribute>
      <h5><xsl:value-of select="$effectiveName" /></h5>
      <p><xsl:value-of select="xsd:annotation/xsd:documentation[@docType != 'enumName' and @docType != 'internal']" /></p>
      <table class="cols2">
        <thead>
          <tr><th>Value</th><th>Notes</th></tr>
        </thead>
        <tbody>
          <xsl:apply-templates select="xsd:simpleType/xsd:restriction/xsd:enumeration" mode="enum-section" />
        </tbody>
      </table>
    </div>
  </xsl:template>

  <xsl:template match="xsd:enumeration" mode="enum-section">
    <tr>
      <td><span class="mono"><xsl:value-of select="@value" /></span></td>
      <td><xsl:value-of select="xsd:annotation/xsd:documentation" /></td>
    </tr>
  </xsl:template>
</xsl:stylesheet>
