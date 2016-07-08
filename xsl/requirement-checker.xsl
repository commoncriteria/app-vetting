<?xml version="1.0"?>
<xsl:stylesheet version="1.0" 
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
		xmlns:xhtml="http://www.w3.org/1999/xhtml"
		xmlns:av="http://common-criteria.rhcloud.com/app-vetting/1.0"
		xmlns:req="http://common-criteria.rhcloud.com/app-vetting/1.0-req"
		xmlns:cc="http://common-criteria.rhcloud.com/ns/cc"
		>

  <xsl:variable name="lower" select="'abcdefghijklmnopqrstuvwxyz'"/>
  <xsl:variable name="upper" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'"/>
  <xsl:output method="text"/>
  <xsl:param name="MAN_REQS_URL" select="../xml/conf/mandatory-requirements.xml"/>


  <xsl:template match="/av:*">


  <xsl:variable name="REPORT" select="/"/>
# REPORT FOR <xsl:value-of select="/av:report/av:app-info/av:name"/> <xsl:if test="/av:report/av:app-info/av:vendor">BY <xsl:value-of select="/av:report/av:app-info/av:vendor"/> </xsl:if>

## NONCONFORMANT REQUIREMENTS ##
    <xsl:for-each select="document($MAN_REQS_URL)//req:req">
      <xsl:variable name="REQID"><xsl:value-of select="translate(@id,$upper,$lower)"/></xsl:variable>
      <xsl:if test="$REPORT//av:requirement[@id=$REQID]/av:result and not($REPORT//av:requirement[@id=$REQID]/av:result/text()='PASSED')"><xsl:value-of select="$REQID"/>,</xsl:if>
    </xsl:for-each>
    

## MISSING REQUIREMENTS ##
    <xsl:for-each select="document($MAN_REQS_URL)//req:req">
      <xsl:variable name="REQID"><xsl:value-of select="translate(@id,$upper,$lower)"/></xsl:variable>
      <xsl:if test="not($REPORT//av:requirement[@id=$REQID])"><xsl:value-of select="$REQID"/>,</xsl:if>
    </xsl:for-each>


## CONFORMANT REQUIREMENTS ##
    <xsl:for-each select="document($MAN_REQS_URL)//req:req">
      <xsl:variable name="REQID"><xsl:value-of select="translate(@id,$upper,$lower)"/></xsl:variable>
      <xsl:if test="$REPORT//av:requirement[@id=$REQID]/av:result/text()='PASSED'"><xsl:value-of select="$REQID"/>,</xsl:if>
    </xsl:for-each>
    <xsl:apply-templates/>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="text()"/>
 

</xsl:stylesheet>
