<?xml version="1.0" encoding="utf-8" standalone="yes"?>
<xsl:stylesheet version="1.0" 
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
		xmlns:xhtml="http://www.w3.org/1999/xhtml"
		xmlns:av="http://common-criteria.rhcloud.com/app-vetting/1.0"
		xmlns:cc="http://common-criteria.rhcloud.com/ns/cc"
		>

  <!-- very important, for special characters and umlauts iso8859-1-->
  <xsl:output method="html" encoding="UTF-8" indent="yes" />

  <xsl:variable name="lower" select="'abcdefghijklmnopqrstuvwxyz'"/>
  <xsl:variable name="upper" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'"/>
  <!-- http://github.com/commoncriteria/application/raw/v-->
  <xsl:variable name="AppPpUrl"><!--
  -->file://home/kevin/work/protection-profiles/app-vetting/v<!--
  --><xsl:value-of select="/av:report/av:evaluation/av:niap-requirements/@version"/><!--
  -->/input/application.xml</xsl:variable>
  <xsl:template match="/av:report">
    <xsl:message><xsl:value-of select="$AppPpUrl"/></xsl:message>

    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
	<title>
	  App Vetting Reciprocity Report for 
	  <xsl:value-of select="/av:report/av:app-info/av:name"/>
	</title>

        <style type="text/css">
	  table, th, tr{
	     border: 1px black;
	  }
	</style>

      </head>
      <body onLoad="init()">
	<h1> Assessment Report Details </h1>
      </body>
      <table>
	<tr>
	  <td>Application Name: </td>
	  <td><xsl:value-of select="/av:report/av:app-info/av:name"/></td>
	</tr>


	<xsl:call-template name="add-row-if-exists">
	  <xsl:with-param name="desc">Description</xsl:with-param>
	  <xsl:with-param name="value"><xsl:value-of select="/av:report/av:app-info/av:description"/></xsl:with-param>
	</xsl:call-template>

	<xsl:call-template name="add-row-if-exists">
	  <xsl:with-param name="desc">Version</xsl:with-param>
	  <xsl:with-param name="value"><xsl:value-of select="/av:report/av:app-info/av:version"/></xsl:with-param>
	</xsl:call-template>

	<xsl:if test="/av:report/av:app-info/av:vendor">
	  <tr>
	    <td>Vendor</td>
	    <td>
	      <xsl:value-of select="/av:report/av:app-info/av:vendor/@name"/><br/>
	      <xsl:value-of select="/av:report/av:app-info/av:vendor/@url"/><br/>
	    </td>
	  </tr>
	</xsl:if>
      </table>

      <xsl:for-each select="/av:report/av:evaluation">
	<table>
	  <tr>
	    <td> Tester(s)</td>
	    <td> 
	      <xsl:for-each select="av:tester">
		<xsl:value-of select="av:name"/>
		(<xsl:for-each select="av:org"/>)
	      </xsl:for-each>
	    </td>
	  </tr>
	</table>

      </xsl:for-each>
      <xsl:apply-templates/>
    </html>

  </xsl:template>

  <xsl:template match="av:niap-requirements">
    <table>
      <tr>
	<th>Section</th><th>Subsection</th><th>Requirement</th><th>Result</th><th>Text</th><th>Notes</th>
      </tr>
      <xsl:apply-templates/>
    </table>
  </xsl:template>

  <xsl:template match="av:niap-requirements/av:section/av:subsection/av:requirement">

<!--
    <xsl:variable name="URL"><xsl:value-of select="../../../@url"/></xsl:variable>
    <xsl:message>URL is <xsl:value-of select="$URL"/></xsl:message>
-->
    <tr>
      <xsl:if test="not(preceding-sibling::*)">
	<xsl:if test="not(../preceding-sibling::*)">
	  <xsl:element name="td">
	    <xsl:attribute name="rowspan">
	      <xsl:value-of select="count(../../av:subsection/av:requirement)"/>
	    </xsl:attribute>
	    <xsl:value-of select="../../@id"/>
	  </xsl:element>
	</xsl:if>
	<xsl:element name="td">
	  <xsl:attribute name="rowspan">
	    <xsl:value-of select="count(../av:requirement)"/>
	  </xsl:attribute>
	  <xsl:value-of select="../@id"/>
	</xsl:element>
      </xsl:if>
      <xsl:variable name="reqid"><xsl:value-of select="translate(@id,$upper,$lower)"/></xsl:variable>
      <td><xsl:value-of select="@id"/></td>
      <td><xsl:value-of select="av:result"/></td>
      <td>
	<xsl:copy>
<!-- This works -->
<!-- http://common-criteria.rhcloud.com/application/input/application.xml-->

<!-- These do not work -->
<!--  http://github.com/commoncriteria/application/raw/v +
     <xsl:value-of select="/av:report/av:evaluation/av:niap-requirements/@version"/> + 
     /input/application.xml</xsl:variable>-->



          <xsl:copy-of select="document('http://common-criteria.rhcloud.com/application/input/application.xml')//*/cc:f-element[@id=$reqid]/cc:title//text()"/>
	</xsl:copy>
      </td>
      <td>
	<xsl:copy>
          <xsl:copy-of select="document('https://common-criteria.rhcloud.com/application/input/application.xml')//*/cc:f-element[@id=$reqid]/cc:note//text()"/>
<!--          <xsl:copy-of select="document($AppPpUrl)//*/cc:f-element[@id=$reqid]/cc:note//text()"/>-->
	</xsl:copy>
      </td>
    </tr>
  </xsl:template>

  <!-- 
       Adds a table row (with $desc in the first cell and $value in the second) 
       if $value is not empty. Does nothing
       if value is empty.
  -->
  <xsl:template name="add-row-if-exists">
    <xsl:param name="desc"/>
    <xsl:param name="value"/>
    <xsl:if test="not($value='')">
      <tr>
	<td><xsl:value-of select="$desc"/></td>
	<td><xsl:value-of select="$value"/></td>
      </tr>
    </xsl:if>
  </xsl:template>

  <!--
      Adds a newline character.
  -->
  <xsl:template name="newline">
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>
</xsl:stylesheet>
