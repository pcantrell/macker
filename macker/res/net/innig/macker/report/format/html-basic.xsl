<?xml version='1.0'?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:variable name="lowercase" select="'abcdefghijklmnopqrstuvwxyz'" />
    <xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />
    
    <xsl:template match="macker-report">
    <html>
    <head>
        <title>Macker report</title>
        <style type="text/css">@import "macker-report.css";</style>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    </head>
    <body>
    <div class="title">
        <xsl:variable name="maxseverity">
            <xsl:choose>
                <xsl:when test="count(descendant::*[@severity='error']) > 0">error</xsl:when>
                <xsl:when test="count(descendant::*[@severity='warning']) > 0">warning</xsl:when>
                <xsl:otherwise>ok</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <table cellpadding="0" cellspacing="0" border="0" align="right">
            <xsl:attribute name="class">event-severity-<xsl:value-of select="$maxseverity"/></xsl:attribute>
            <tr><td><xsl:value-of select="translate($maxseverity, $lowercase, $uppercase)"/></td></tr>
        </table>

        Macker Report
    </div>
    <xsl:apply-templates/>
    <br/><br/>
    </body>
    </html>
    </xsl:template>
    
    <xsl:template match="ruleset">
    <xsl:choose>
        <xsl:when test="@name">
            <div class="ruleset">
                <div class="ruleset-header">
                    <xsl:if test="count(descendant::*[@severity='error']) = 0 and count(descendant::*[@severity='warning']) = 0">
                        <table cellpadding="0" cellspacing="0" border="0" align="right">
                        <xsl:attribute name="class">event-severity-ok</xsl:attribute>
                        <tr><td>OK</td></tr>
                        </table>
                    </xsl:if>
    
                    <xsl:value-of select="@name"/>
                </div>
                <div class="ruleset-body">
                    <xsl:apply-templates/>
                </div>
            </div>
        </xsl:when>
        <xsl:otherwise>
            <xsl:apply-templates/>
        </xsl:otherwise>
    </xsl:choose>
    </xsl:template>
    
    <xsl:template match="access-rule-violation">
    <xsl:if test="@severity != 'debug'">
        <div class="event">
            <table cellpadding="0" cellspacing="0" border="0" align="right">
                <xsl:attribute name="class">event-severity-<xsl:value-of select="@severity" /></xsl:attribute>
                <tr><td><xsl:value-of select="translate(@severity, $lowercase, $uppercase)"/></td></tr>
            </table>
            <div class="event-header">
            <xsl:value-of select="message"/>
            </div>
            <div class="event-body">
            <table>
                <tr><th>From:</th>
                    <td><span class="package-name"><xsl:value-of select="from/package"/>.</span>
                        <span class=  "class-name"><xsl:value-of select="from/class"/></span></td></tr>
                <tr><th>  To:</th>
                    <td><span class="package-name"><xsl:value-of select=  "to/package"/>.</span>
                        <span class=  "class-name"><xsl:value-of select=  "to/class"/></span></td></tr>
            </table>
            </div>
        </div>
    </xsl:if>
    </xsl:template>
    
    <xsl:template match="foreach">
    <xsl:apply-templates/>
    </xsl:template>
    
    <xsl:template match="iteration">
    <div class="foreach">
        <div class="foreach-header">
            <span class="var"><xsl:value-of select="../@var"/>: </span>
            <span class="value"><xsl:value-of select="@value"/></span>
        </div>
        <div class="foreach-body">
            <xsl:apply-templates/>
        </div>
    </div>
    </xsl:template>

</xsl:stylesheet>
