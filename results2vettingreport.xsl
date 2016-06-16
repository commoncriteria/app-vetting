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

  

  <!-- Top Level Template -->
  <xsl:template match="/av:report">


    <xsl:variable name="REPORT" select="/"/>

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


	  .tg  {border-collapse:collapse;border-spacing:0;border-color:#aabcfe;}
	  .tg td{font-family:Arial, sans-serif;font-size:14px;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;border-color:#aabcfe;color:black;}
	  .tg th{font-family:Arial, sans-serif;font-size:14px;font-weight:normal;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;border-color:#aabcfe;color:#039;background-color:#b9c9fe;}
	  .tg tr:nth-child(even) {background-color:#DDD}
	  .tg tr:nth-child(odd) {background-color:#FFF}
	  .tg .section { color: white; background-color: black }
	  .tg .subsection { color: white; background-color: #999 }

	</style>
        <script type="text/javascript">
	  function toggleColumn(clazz){
	     var els = document.getElementsByClassName(clazz);
	     for (aa = els.length-1; aa>=0; aa--){
	         if(els[aa].style.display == "none")
		   els[aa].style.display = "initial";
		 else
                   els[aa].style.display = "none";
             }
	  }
	  
	</script>

      </head>
      <body onLoad="init()">
	<h1> Assessment Report Details </h1>

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
	  <xsl:for-each select="av:niap-requirements">

	    <!-- 
		 file:// and http:// are the only URLs that seem to work with xsltproc.
		 https:// does NOT work and github only uses https.
		 This is why we copy the application.xml from the tagged versions onto
		 our rhcloud instance via wget which does understand https. We could
		 use an http-to-https proxy, but those of the stench of software privacy.
	    -->
	    <xsl:variable name="PpUrl"><!--
				       -->http://common-criteria.rhcloud.com/application-<!--
				       --><xsl:value-of select="@version"/><!--
				       -->.xml<!--
				       --></xsl:variable>
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


	    <table class="tg">
	      <tr>
		<th onClick='toggleColumn("section-div");'>Section</th>
		<th onClick='toggleColumn("subsection-div");'>Subsection</th>
		<th>Requirement</th>
		<th>Result</th>
		<th onClick='toggleColumn("title-div");'>Text</th>
		<th onClick='toggleColumn("note-div");'>Notes</th>
	      </tr>

	      <!-- Go through all the requirements in the Protection Profile -->
              <xsl:for-each select="document($PpUrl)//*/cc:f-element|document($PpUrl)//*/cc:a-element">
		<xsl:call-template name="newline"/>
		<tr>
		  <xsl:choose>
		    <xsl:when test="../cc:f-element">
		      <!--
			  If we don't have a preceding sibling that's an f-element, we're
			  the first for our subsection.
		      -->
		      <xsl:if test="not(preceding-sibling::cc:f-element)">
			<!--
			    If our parent doesn't have a preceding sibling that's not a f-component
			    we're the first for our section.
			-->
			
			<xsl:if test="not(../preceding-sibling::cc:f-component)">
			  <xsl:element name="td">
			    <xsl:attribute name="class">section</xsl:attribute>
			    <xsl:attribute name="rowspan">
			      <xsl:value-of select="count(../../cc:f-component/cc:f-element)"/>
			    </xsl:attribute>
			    <div class="section-div">
			      <xsl:value-of select="../../@id"/>
			    </div>
			  </xsl:element>
			</xsl:if>
			<xsl:element name="td">
			  <xsl:attribute name="class">subsection</xsl:attribute>
			  <xsl:attribute name="rowspan">
			    <xsl:value-of select="count(../cc:f-element)"/>
			  </xsl:attribute>
			  <div class="subsection-div">
			    <xsl:value-of select="../@id"/>
			  </div>
			</xsl:element>
		      </xsl:if>
		      <xsl:variable name="reqid"><xsl:value-of select="translate(@id,$upper,$lower)"/></xsl:variable>
		      <td class="req-id"><xsl:value-of select="@id"/></td>
		      <td class="result">
			<xsl:value-of select="$REPORT//av:requirement[@id=$reqid]/av:result/text()"/>
		      </td>
		      
		      <td class="req-text">
			<xsl:for-each select="cc:title">
			  <div class="title-div">
			    <xsl:apply-templates mode="reaper"/>
			  </div>
			</xsl:for-each>
		      </td>
		      <td class="req-note">
			<xsl:for-each select="cc:note">
			  <div class="note-div">
			    <xsl:apply-templates mode="reaper"/>
			  </div>
			</xsl:for-each>
		      </td>
		    </xsl:when>


		    <xsl:when test="../cc:a-element">
		      <xsl:if test="not(preceding-sibling::cc:a-element|../preceding-sibling::cc:group)">
			<xsl:if test="not(../../preceding-sibling::cc:a-component)">
			  <xsl:element name="td">
			    <xsl:attribute name="class">section</xsl:attribute>
			    <xsl:attribute name="rowspan">
			      <xsl:value-of select="count(../../../cc:a-component/cc:group/cc:a-element)"/>
			    </xsl:attribute>
			    <xsl:value-of select="../../../@id"/>
			  </xsl:element>
			</xsl:if>
			<xsl:element name="td">
			  <xsl:attribute name="class">subsection</xsl:attribute>
			  <xsl:attribute name="rowspan">
			    <xsl:value-of select="count(../../cc:group/cc:a-element)"/>
			  </xsl:attribute>
			  <xsl:value-of select="../../@id"/>
			</xsl:element>
		      </xsl:if>
		      <xsl:variable name="reqid"><xsl:value-of select="translate(@id,$upper,$lower)"/></xsl:variable>
		      <td class="req-id"><xsl:value-of select="@id"/></td>
		      <td class="result">
			<xsl:value-of select="$REPORT//av:requirement[@id=$reqid]/av:result/text()"/>
		      </td>
		      
		      <td class="req-text">
			<div class="title-div">
			  <xsl:for-each select="cc:title">
			    <xsl:apply-templates mode="reaper"/>
			  </xsl:for-each>
			</div>
		      </td>
		      <td class="req-note">
			<div class="note-div">
			  <xsl:for-each select="cc:note">
			    <xsl:apply-templates mode="reaper"/>
			  </xsl:for-each>
			</div>
		      </td>
		    </xsl:when>
		  </xsl:choose>
		</tr>
	      </xsl:for-each>
	    </table>
	  </xsl:for-each>
	</xsl:for-each>
	<!-- Here we look for unknown niap requirements -->
	<xsl:for-each select="//av:niap-requirements">
	  <xsl:variable name="PpUrl"><!--
	    -->http://common-criteria.rhcloud.com/application-<!--
	    --><xsl:value-of select="@version"/><!--
	    -->.xml<!--
	    --></xsl:variable>
	  <xsl:for-each select=".//av:requirement">
	    <xsl:variable name="reqid"><xsl:value-of select="@id"/></xsl:variable>
	    <xsl:if test="not(document($PpUrl)//*/cc:f-element[@id=$reqid])">
	      <br/>
		Unknown requirement:
	      <xsl:value-of select="$reqid"/>
	    </xsl:if>
	  </xsl:for-each>
	</xsl:for-each>
	
	  <table class="tg">
	    <xsl:for-each select="//av:other-requirements"> <tr>
	      <xsl:apply-templates mode="other"/>
	    </tr> </xsl:for-each>
	  </table>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="av:section" mode="other">
    <td><xsl:value-of select="@id"/></td>
    <td>
      <table><tr>
      <xsl:apply-templates mode="other"/>
      </tr></table>
    </td>
  </xsl:template>

  <xsl:template match="av:requirement" mode="other">
      <table>
	    <td>
	      <xsl:value-of select="@id"/>
	    </td>
	    <td>
	      <xsl:value-of select="av:result/text()"/>
	    </td>
	    <td>
	      <xsl:value-of select="av:activity/av:summary/text()"/>
	    </td>
      </table>
  </xsl:template>
  <xsl:template match="*" mode="other">
    <xsl:copy/>
  </xsl:template>
  
  <xsl:template match="*" mode="reaper">
    <xsl:for-each select="node()">
      <xsl:choose>
	<xsl:when test="self::text()"><xsl:copy/></xsl:when>
	<xsl:when test="self::*"><xsl:apply-templates mode="reaper"/></xsl:when>
      </xsl:choose>
    </xsl:for-each>
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
