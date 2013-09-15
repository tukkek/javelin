<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">

 <html><head><title>Tyrant Library Items</title></head><body>
   <xsl:apply-templates />
  </body></html>

</xsl:template>

<xsl:template match="TyrantLibrary">
  <h1>
    <a href="http://tyrant.sourceforge.net">Tyrant</a> &#x20;
    <xsl:value-of select="@ProgramVersion" /> Library Items
  </h1>
  <br /><br />
  <table border="0" width="80%" cellspacing="0" cellpadding="0">
    <xsl:apply-templates />
  </table>
</xsl:template>

<xsl:template match="Item">

    <tr bgcolor="#C0C0C0">
      <td width="40%"><b>Property</b></td>
      <td width="60%"><b>Value</b></td>
    </tr>
    <xsl:for-each select="*">
      <tr>
        <td><b><xsl:value-of select="local-name(.)"/></b></td>
        <td><xsl:value-of select="."  /></td>
      </tr>
    </xsl:for-each>

</xsl:template>

</xsl:stylesheet>