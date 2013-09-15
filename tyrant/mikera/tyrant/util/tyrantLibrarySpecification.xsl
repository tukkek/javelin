<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
 <html><head><title>Tyrant Library Specification</title></head><body>
   <xsl:apply-templates />
  </body></html>
</xsl:template>

<xsl:template match="TyrantLibrarySpecification">
  <h1>
    <a href="http://tyrant.sourceforge.net">Tyrant</a> &#x20;
    <xsl:value-of select="@ProgramVersion" /> Library Specification
  </h1>
  <br /><br />
  <xsl:apply-templates />
</xsl:template>

<xsl:template name="PropertySpecificationProperty">
  <tr>
    <td><xsl:value-of select="Name"/></td>
    <td><xsl:value-of select="Type" /></td>
    <td><xsl:value-of select="Description"/></td>
  </tr>
</xsl:template>

<xsl:template match="PropertySpecification">
  <h2>1. Property Specification</h2>
  <table border="0" width="100%" cellspacing="0" cellpadding="0">
    <tr bgcolor="#C0C0C0">
      <td width="20%"><b>Property</b></td>
      <td width="30%"><b>Type</b></td>
      <td width="50%"><b>Description</b></td>
    </tr>
    <xsl:for-each select="Property">
      <xsl:call-template name="PropertySpecificationProperty" />
      <xsl:for-each select="Property">
        <xsl:call-template name="PropertySpecificationProperty" />
      </xsl:for-each>
    </xsl:for-each>
  </table>
</xsl:template>

<xsl:template name="ItemSpecificationProperty">
  <td><xsl:value-of select="Name"/></td>
  <td><xsl:value-of select="IsMandatory" /></td>
  <td><xsl:value-of select="Value"/></td>
  <td><xsl:value-of select="ValueCondition"/></td>  
</xsl:template>

<xsl:template match="ItemSpecification">
  <h2>2. Item Specification</h2>  
  <xsl:for-each select="Item">
    <h3>Item: <xsl:value-of select="Name"/></h3>
    <table border="0" width="100%" cellspacing="0" cellpadding="0">
      <tr bgcolor="#C0C0C0">
        <td width="20%"><b>Property</b></td>
        <td width="20%"><b>Mandatory</b></td>
        <td width="20%"><b>Value</b></td>
        <td width="20%"><b>Value condition</b></td>
        <td width="20%"><b>Valid values</b></td>
    </tr>
    <xsl:for-each select="Property">
      <tr>
        <xsl:call-template name="ItemSpecificationProperty" />
        <td>
          <xsl:for-each select="ValidValues/Value">
            <xsl:value-of select="."/>&#160;
          </xsl:for-each>
	</td>
      </tr>
      <xsl:for-each select="ValidValues/MetaData">
        <tr>
          <td></td>
          <td colspan="4">
          <table border="0" width="100%" cellspacing="0" cellpadding="0">
            <tr>
              <td width="20%" bgcolor="#C0C0C0">Property</td>
              <td width="20%" bgcolor="#C0C0C0">Mandatory</td>
              <td width="20%" bgcolor="#C0C0C0">Value</td>
              <td width="20%" bgcolor="#C0C0C0">Value condition</td>
	    </tr>
            <xsl:for-each select="Property">
              <tr>
                <xsl:call-template name="ItemSpecificationProperty" />
              </tr>
            </xsl:for-each>
          </table><br />
          </td>
        </tr> 
      </xsl:for-each>
    </xsl:for-each>
    </table>
  </xsl:for-each>
</xsl:template>

</xsl:stylesheet>