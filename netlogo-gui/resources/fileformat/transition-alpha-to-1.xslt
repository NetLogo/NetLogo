<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:old="http://ccl.northwestern.edu/netlogo/netlogox/6.1"
  xmlns:nlogox="http://ccl.northwestern.edu/netlogo/netlogox/1"
  xmlns="http://ccl.northwestern.edu/netlogo/netlogox/1"
  exclude-result-prefixes="nlogox"
  version="1.0">

  <!-- This is an example conversion between formats. In this case, we convert
       from the alpha, never-released 6.1 format to the to-be-released 1 format.
       This is mostly re-namespacing, but we have one semantic change
       around pen line modes. Note that running this script will prefix all
       elements with nlogox: . Running the ModelResaver will then remove these and remove
       them by using a default namespace. This approach is recommended to reduce file "churn",
       that is, to make the same file more easily comparable with different versions of itself. -->
  <xsl:output method="xml" indent="yes" cdata-section-elements="nlogox:version nlogox:code nlogox:info nlogox:jhotdraw6
    nlogox:tickCounterLabel nlogox:maximum nlogox:minimum nlogox:step nlogox:units nlogox:variable nlogox:compiled
    nlogox:display nlogox:display nlogox:variable nlogox:stringChoice nlogox:string nlogox:display nlogox:xAxis
    nlogox:yAxis nlogox:setup nlogox:update nlogox:setup nlogox:update nlogox:display nlogox:source nlogox:display
    nlogox:source nlogox:display nlogox:display nlogox:setupCode nlogox:goCode nlogox:finalCode nlogox:stopConditionCode
    nlogox:metric nlogox:string nlogox:title" />

  <xsl:template match="/old:model">
    <xsl:element name="nlogox:model" namespace="http://ccl.northwestern.edu/netlogo/netlogox/1">
      <xsl:apply-templates select="@*|node()" mode="upgrade" />
    </xsl:element>
  </xsl:template>

  <xsl:template match="/nlogox:model">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" />
    </xsl:copy>
  </xsl:template>

  <!-- this is our semantic pen-line mode change -->
  <xsl:template match="old:pen" mode="upgrade">
    <xsl:element name="nlogox:pen" namespace="http://ccl.northwestern.edu/netlogo/netlogox/1">
      <xsl:attribute name="mode">
        <xsl:choose>
          <xsl:when test="@mode='0'">line</xsl:when>
          <xsl:when test="@mode='1'">bar</xsl:when>
          <xsl:when test="@mode='2'">point</xsl:when>
        </xsl:choose>
      </xsl:attribute>
      <xsl:apply-templates select="@*[name(.) != 'mode'] | node()" mode="upgrade" />
    </xsl:element>
  </xsl:template>

  <xsl:template match="@*" mode="upgrade">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="upgrade" />
    </xsl:copy>
  </xsl:template>

  <xsl:template match="node()" mode="upgrade">
    <xsl:choose>
      <xsl:when test="namespace-uri() = 'http://ccl.northwestern.edu/netlogo/netlogox/6.1'">
        <xsl:element name="nlogox:{local-name()}" namespace="http://ccl.northwestern.edu/netlogo/netlogox/1">
          <xsl:apply-templates select="@*|node()" mode="upgrade" />
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy>
          <xsl:apply-templates select="@*|node()" mode="upgrade" />
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="node()|@*">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*" />
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
