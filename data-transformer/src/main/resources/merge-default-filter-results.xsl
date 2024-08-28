<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="urn:rdns:com:ericsson:oammodel:ericsson-lrat-enb-adapter"
                xmlns:a="urn:rdns:com:ericsson:oammodel:ericsson-lrat-enb-adapter"
                xmlns:b="urn:rdns:com:ericsson:oammodel:ericsson-rme-sef-enb-adapter"
                xmlns:c="urn:rdns:com:ericsson:oammodel:ericsson-req-equip-enb-adapter"
                xmlns:d="urn:rdns:com:ericsson:oammodel:ericsson-req-antenna-enb-adapter"
                exclude-result-prefixes="a b c d">

    <xsl:strip-space elements="*"/>
    <xsl:output omit-xml-declaration="yes" indent="yes"/>

    <xsl:template match="/dummyRoot">
        <xsl:for-each-group select="a:enodeb-function" group-by="name()">
            <xsl:call-template name="enodeb-function"/>
        </xsl:for-each-group>
        <xsl:apply-templates/>
        <xsl:for-each-group select="c:equipment" group-by="name()">
            <xsl:call-template name="equipment"/>
        </xsl:for-each-group>
    </xsl:template>

    <xsl:template name="enodeb-function">
        <xsl:copy>
            <xsl:for-each-group select="current-group()/a:enb-id | current-group()/a:enodeb-plmn-id" group-by="name()">
                <xsl:copy>
                    <xsl:apply-templates select="node()"/>
                </xsl:copy>
            </xsl:for-each-group>
            <xsl:apply-templates select="current-group()/node()"/>
            <xsl:for-each-group select="current-group()/a:eutra-network" group-by="name()">
                <xsl:copy>

                    <xsl:for-each-group select="current-group()/a:external-enodeb-function" group-by="a:id">
                        <xsl:copy>
                            <xsl:apply-templates select="current-group()[a:enb-id][1]/*"/>
                            <xsl:for-each select="current-group()/a:external-eutran-cell-fdd">
                                <xsl:copy>
                                    <xsl:apply-templates select="node()"/>
                                </xsl:copy>
                            </xsl:for-each>
                        </xsl:copy>
                    </xsl:for-each-group>

                </xsl:copy>
            </xsl:for-each-group>

            <xsl:for-each-group select="current-group()/a:eutran-cell-fdd" group-by="a:id">
                <xsl:copy>
                    <xsl:apply-templates select="current-group()[a:dl-channel-bandwidth][1]/*"/>
                    <xsl:for-each select="current-group()/a:ue-meas-control[1]">
                        <xsl:copy>
                            <xsl:apply-templates select="node()"/>
                        </xsl:copy>
                    </xsl:for-each>

                    <xsl:for-each-group select="current-group()/a:eutran-freq-relation" group-by="a:id">
                        <xsl:copy>
                            <xsl:apply-templates select="current-group()[a:lb-activation-threshold][1]/*"/>

                            <xsl:for-each select="current-group()/a:eutran-cell-relation">
                                <xsl:copy>
                                    <xsl:apply-templates select="node()"/>
                                </xsl:copy>
                            </xsl:for-each>
                        </xsl:copy>
                    </xsl:for-each-group>

                </xsl:copy>
            </xsl:for-each-group>

        </xsl:copy>
    </xsl:template>

    <xsl:template name="equipment">
        <xsl:copy>
            <xsl:for-each-group select="current-group()/d:antenna-unit-group" group-by="d:id">
                <xsl:copy>
                    <xsl:apply-templates/>
                    <xsl:for-each select="current-group()/d:antenna-near-unit">
                        <xsl:copy>
                            <xsl:apply-templates select="node()"/>
                        </xsl:copy>
                    </xsl:for-each>
                    <xsl:for-each select="current-group()/d:rf-branch">
                        <xsl:copy>
                            <xsl:apply-templates select="node()"/>
                        </xsl:copy>
                    </xsl:for-each>
                </xsl:copy>
            </xsl:for-each-group>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="/dummyRoot/a:enodeb-function"/>
    <xsl:template match="/dummyRoot/a:enodeb-function/a:enb-id"/>
    <xsl:template match="/dummyRoot/a:enodeb-function/a:enodeb-plmn-id"/>
    <xsl:template match="/dummyRoot/a:enodeb-function/a:eutra-network"/>
    <xsl:template match="/dummyRoot/a:enodeb-function/a:eutran-cell-fdd"/>
    <xsl:template match="/dummyRoot/a:enodeb-function/a:eutran-cell-fdd/a:eutran-freq-relation"/>
    <xsl:template match="/dummyRoot/a:enodeb-function/a:eutran-cell-fdd/a:ue-meas-control"/>
    <xsl:template match="/dummyRoot/a:enodeb-function/a:eutran-cell-fdd/a:eutran-freq-relation/a:eutran-cell-relation"/>
    <xsl:template match="/dummyRoot/a:enodeb-function/a:eutra-network/a:external-enodeb-function"/>
    <xsl:template match="/dummyRoot/a:enodeb-function/a:eutra-network/a:external-enodeb-function/a:external-eutran-cell-fdd"/>
    <xsl:template match="/dummyRoot/c:equipment"/>
    <xsl:template match="/dummyRoot/c:equipment/d:antenna-unit-group"/>
    <xsl:template match="/dummyRoot/c:equipment/d:antenna-unit-group/d:antenna-near-unit"/>
    <xsl:template match="/dummyRoot/c:equipment/d:antenna-unit-group/d:rf-branch"/>
    <xsl:template match="@*|node()">
        <xsl:if test=".!='' or .!=normalize-space()">
            <xsl:copy>
                <xsl:apply-templates select="@*|node()"/>
            </xsl:copy>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
