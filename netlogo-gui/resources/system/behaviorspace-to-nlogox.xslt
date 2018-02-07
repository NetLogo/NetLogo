<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="xml" indent="yes" />

  <xsl:template match="/experiments">
    <experiments>
      <xsl:apply-templates select="experiment" />
    </experiments>
  </xsl:template>

  <xsl:template match="experiment">
    <experiment name="{@name}">
      <xsl:if test="@runMetricsEveryStep = 'false'">
        <xsl:attribute name="runMetricsEveryStep">false</xsl:attribute>
      </xsl:if>
      <xsl:if test="string-length(timeLimit/@steps) > 0">
        <xsl:attribute name="iterationLimit">
          <xsl:value-of select="string(timeLimit/@steps)" />
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates select="setup" />
      <xsl:apply-templates select="go" />
      <xsl:apply-templates select="final" />
      <xsl:apply-templates select="exitCondition" />
      <metrics>
        <xsl:apply-templates select="metric" />
      </metrics>
      <parameterSet>
        <cartesianProduct>
          <xsl:if test="@repetitions != '1'">
            <xsl:attribute name="repetitionsPerCombo">
              <xsl:value-of select="@repetitions" />
            </xsl:attribute>
          </xsl:if>
          <xsl:if test="@sequentialRunOrder = 'false'">
            <xsl:attribute name="sequentialRunOrder">false</xsl:attribute>
          </xsl:if>
          <xsl:apply-templates select="./*[self::enumeratedValueSet or self::steppedValueSet]" />
        </cartesianProduct>
      </parameterSet>
    </experiment>
  </xsl:template>

  <xsl:template match="setup">
    <setupCode><xsl:apply-templates select="@*|node()" /></setupCode>
  </xsl:template>

  <xsl:template match="go">
    <goCode><xsl:apply-templates select="@*|node()" /></goCode>
  </xsl:template>

  <xsl:template match="final">
    <finalCode><xsl:apply-templates select="@*|node()" /></finalCode>
  </xsl:template>

  <xsl:template match="exitCondition">
    <stopConditionCode><xsl:apply-templates select="@*|node()" /></stopConditionCode>
  </xsl:template>

  <xsl:template match="enumeratedValueSet">
    <discreteValues variable="{@variable}">
      <xsl:apply-templates select="value" />
    </discreteValues>
  </xsl:template>

  <xsl:template match="value">
    <xsl:choose>
      <xsl:when test="starts-with(@value, '&quot;') and substring(@value, string-length(@value) - 2, 1) = '&quot;'">
        <string><xsl:value-of select="substring-before(substring-after(@value, '&quot;'), '&quot;')" /></string>
      </xsl:when>
      <xsl:when test="@value = 'true' or @value = 'false'">
        <boolean><xsl:value-of select="@value" /></boolean>
      </xsl:when>
      <xsl:otherwise>
        <number><xsl:value-of select="@value" /></number>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="steppedValueSet">
    <range variable="{@variable}" min="{@first}" interval="{@step}" max="{@last}" />
  </xsl:template>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" />
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
