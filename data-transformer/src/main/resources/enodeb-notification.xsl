<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:mf="my:functions"
                xmlns="urn:ietf:params:xml:ns:netconf:notification:1.0"
                xmlns:a="urn:ietf:params:xml:ns:netconf:notification:1.0"
                xmlns:b="urn:ericsson:com:netconf:notification:1.0"
                exclude-result-prefixes="a b">
    <xsl:output omit-xml-declaration="yes"/>
    <xsl:output indent="yes" />
    <xsl:strip-space elements="*"/>

    <xsl:variable name="lratPrefix">lrtadpt:</xsl:variable>
    <xsl:variable name="rsefPrefix">rsefadpt:</xsl:variable>
    <xsl:variable name="reqeqadptPrefix">reqeqadpt:</xsl:variable>
    <xsl:variable name="reqantadptPrefix">reqantadpt:</xsl:variable>

    <xsl:variable name="notificationNameSpace">urn:ietf:params:xml:ns:yang:ietf-netconf-notifications</xsl:variable>
    <xsl:variable name="reqeqadptNameSpace">urn:rdns:com:ericsson:oammodel:ericsson-req-equip-enb-adapter</xsl:variable>
    <xsl:variable name="reqantadptNamespace">urn:rdns:com:ericsson:oammodel:ericsson-req-antenna-enb-adapter</xsl:variable>
    <xsl:variable name="rsefadptNameSpace">urn:rdns:com:ericsson:oammodel:ericsson-rme-sef-enb-adapter</xsl:variable>
    <xsl:variable name="lrtadptNameSpace">urn:rdns:com:ericsson:oammodel:ericsson-lrat-enb-adapter</xsl:variable>

    <xsl:template match="/a:notification">
        <xsl:call-template name="copyElement"/>
    </xsl:template>

    <xsl:template match="/a:notification/a:eventTime">
        <xsl:call-template name="copyElement"/>
    </xsl:template>

    <xsl:template match="/a:notification/b:events">
        <xsl:element name="{'netconf-config-change'}" namespace="{$notificationNameSpace}">
            <xsl:call-template name="getConfigChangeContents"/>
        </xsl:element>
    </xsl:template>

    <xsl:template name="getConfigChangeContents">
        <xsl:element name="{'changed-by'}" namespace="{$notificationNameSpace}">
            <xsl:element name="{'server'}" namespace="{$notificationNameSpace}"/>
        </xsl:element>
        <xsl:element name="{'datastore'}" namespace="{$notificationNameSpace}">running</xsl:element>
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="/a:notification/b:events/b:AVC">
        <xsl:variable name="transformedDn">
            <xsl:call-template name="convertDnInCurrentElementToXpath"/>
        </xsl:variable>
        <xsl:call-template name="getEditTagsFromAvcTag">
            <xsl:with-param name="transformedDn" select="$transformedDn"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="/a:notification/b:events/b:objectCreated">
        <xsl:element name="{'edit'}" namespace="{$notificationNameSpace}">
            <xsl:call-template name="getManagedObjectTarget"/>
            <xsl:element name="{'operation'}" namespace="{$notificationNameSpace}">create</xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template match="/a:notification/b:events/b:objectDeleted">
        <xsl:element name="{'edit'}" namespace="{$notificationNameSpace}">
            <xsl:call-template name="getManagedObjectTarget"/>
            <xsl:element name="{'operation'}" namespace="{$notificationNameSpace}">delete</xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template name="copyElement">
        <xsl:copy>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="getManagedObjectTarget">
        <xsl:choose>
            <xsl:when test="contains(@dn, 'SectorEquipmentFunction')">
                <xsl:element name="{'target'}" namespace="{$notificationNameSpace}">
                    <xsl:namespace name="rsefadpt" select="$rsefadptNameSpace"/>
                    <xsl:call-template name="convertDnInCurrentElementToXpath"/>
                </xsl:element>
            </xsl:when>
            <xsl:when test="contains(@dn, 'AntennaUnitGroup')">
                <xsl:element name="{'target'}" namespace="{$notificationNameSpace}">
                    <xsl:namespace name="reqeqadpt" select="$reqeqadptNameSpace"/>
                    <xsl:namespace name="reqantadpt" select="$reqantadptNamespace"/>
                    <xsl:call-template name="convertDnInCurrentElementToXpath"/>
                </xsl:element>
            </xsl:when>
            <xsl:otherwise>
                <xsl:element name="{'target'}" namespace="{$notificationNameSpace}">
                    <xsl:namespace name="lrtadpt" select="$lrtadptNameSpace"/>
                    <xsl:call-template name="convertDnInCurrentElementToXpath"/>
                </xsl:element>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="convertDnInCurrentElementToXpath">
        <xsl:call-template name="convertDnToXpath">
            <xsl:with-param name="dn" select="@dn"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="convertDnToXpath">
        <xsl:param name="dn"/>
        <xsl:for-each select="tokenize($dn,',')">
            <xsl:call-template name="convertRdnToXpath">
                <xsl:with-param name="in" select="."/>
            </xsl:call-template>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="convertRdnToXpath">
        <xsl:param name="in"/>
        <xsl:variable name="name" select="substring-before($in , '=')"/>
        <xsl:variable name="value" select="substring-after($in , '=')"/>
        <xsl:variable name="yangName" select="mf:yangString($name, true())"/>
        <xsl:choose>
            <xsl:when test="$name='ManagedElement'"/>
            <xsl:when test="$name='NodeSupport'">
                <xsl:call-template name="getRdnWithoutIdAsXpath">
                    <xsl:with-param name="rdn_name" select="$yangName"/>
                    <xsl:with-param name="prefix" select="$rsefPrefix"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$name='Equipment'">
                <xsl:call-template name="getRdnWithoutIdAsXpath">
                    <xsl:with-param name="rdn_name" select="$yangName"/>
                    <xsl:with-param name="prefix" select="$reqeqadptPrefix"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$name='ENodeBFunction' or
                            $name='EUtraNetwork'or
                            $name='QciTable' or
                            $name='CarrierAggregationFunction' or
                            $name='LoadBalancingFunction' or
                            $name='UeMeasControl' or
                            $name='ReportConfigEUtraInterFreqLb'">
                <xsl:call-template name="getRdnWithoutIdAsXpath">
                    <xsl:with-param name="rdn_name" select="$yangName"/>
                    <xsl:with-param name="prefix" select="$lratPrefix"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$name='ExternalENodeBFunction' or
                            $name='ExternalEUtranCellFDD' or
                            $name='EUtranCellFDD' or
                            $name='EUtranFreqRelation' or
                            $name='EUtranCellRelation'">
                <xsl:call-template name="getRdnAsXpath">
                    <xsl:with-param name="rdn_name" select="$yangName"/>
                    <xsl:with-param name="rdn_value" select="$value"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$name='SectorEquipmentFunction'">
                <xsl:call-template name="getRdnAsXpath">
                    <xsl:with-param name="rdn_name" select="$yangName"/>
                    <xsl:with-param name="rdn_value" select="$value"/>
                    <xsl:with-param name="prefix" select="$rsefPrefix"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$name='AntennaUnitGroup' or
                            $name='AntennaNearUnit' or
                            $name='RfBranch' or
                            $name='RetSubUnit'">
                <xsl:call-template name="getRdnAsXpath">
                    <xsl:with-param name="rdn_name" select="$yangName"/>
                    <xsl:with-param name="rdn_value" select="$value"/>
                    <xsl:with-param name="prefix" select="$reqantadptPrefix"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise> <!-- default is standard name change and include id -->
                <xsl:variable name="transformedName" select="mf:transformStandardName($name)"/>
                <xsl:call-template name="getRdnAsXpath">
                    <xsl:with-param name="rdn_name" select="$transformedName"/>
                    <xsl:with-param name="rdn_value" select="$value"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="getRdnWithoutIdAsXpath">
        <xsl:param name="rdn_name"/>
        <xsl:param name="prefix" select="$lratPrefix"/>
        <xsl:value-of select="concat('/',$prefix, $rdn_name)"/>
    </xsl:template>

    <xsl:template name="getRdnAsXpath">
        <xsl:param name="rdn_name"/>
        <xsl:param name="rdn_value"/>
        <xsl:param name="prefix" select="$lratPrefix"/>
        <xsl:variable name="apos">&apos;</xsl:variable>
        <xsl:value-of select="concat('/',$prefix,$rdn_name,'[',$prefix,'id=',$apos,$rdn_value,$apos,']')"/>
    </xsl:template>

    <xsl:template name="getEditTagsFromAvcTag">
        <xsl:param name="transformedDn"/>
        <xsl:for-each select="b:attr">
            <xsl:element name="{'edit'}" namespace="{$notificationNameSpace}">
                <xsl:choose>
                    <xsl:when test="contains(../@dn, 'SectorEquipmentFunction')">
                        <xsl:element name="{'target'}" namespace="{$notificationNameSpace}">
                            <xsl:namespace name="rsefadpt" select="$rsefadptNameSpace"/>
                            <xsl:value-of select="$transformedDn"/>
                            <xsl:call-template name="getAttributeName"/>
                        </xsl:element>
                    </xsl:when>
                    <xsl:when test="contains(../@dn, 'AntennaUnitGroup')">
                        <xsl:element name="{'target'}" namespace="{$notificationNameSpace}">
                            <xsl:namespace name="reqeqadpt" select="$reqeqadptNameSpace"/>
                            <xsl:namespace name="reqantadpt" select="$reqantadptNamespace"/>
                            <xsl:value-of select="$transformedDn"/>
                            <xsl:call-template name="getAttributeName"/>
                        </xsl:element>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:element name="{'target'}" namespace="{$notificationNameSpace}">
                            <xsl:namespace name="lrtadpt" select="$lrtadptNameSpace"/>
                            <xsl:value-of select="$transformedDn"/>
                            <xsl:call-template name="getAttributeName"/>
                        </xsl:element>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:element name="{'operation'}" namespace="{$notificationNameSpace}">merge</xsl:element>
            </xsl:element>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="getAttributeName">
        <xsl:choose>
            <xsl:when test="contains(../@dn, 'SectorEquipmentFunction')">
                <xsl:call-template name="appendAttrName">
                    <xsl:with-param name="prefix" select="$rsefPrefix"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="contains(../@dn, 'AntennaUnitGroup')">
                <xsl:call-template name="appendAttrName">
                    <xsl:with-param name="prefix" select="$reqantadptPrefix"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="appendAttrName">
                    <xsl:with-param name="prefix" select="$lratPrefix"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="appendAttrName">
        <xsl:param name="attrName" select="@name"/>
        <xsl:param name="prefix"/>
        <xsl:variable name="transformedAttrName" select="mf:yangString($attrName, false())"/>
        <xsl:value-of select="concat('/',$prefix,$transformedAttrName)"/>
    </xsl:template>

    <xsl:function name="mf:yangString" as="xs:string">
        <xsl:param name="nodeName" as="xs:string"/>
        <xsl:param name="isMO" as="xs:boolean"/>
        <xsl:variable name="camelCase">
            <xsl:choose>
                <xsl:when test="$isMO">
                    <xsl:value-of select="replace(replace(replace(replace($nodeName, 'ENodeB','Enodeb'), 'EUtra', 'Eutra'),'FDD','Fdd'),'InterFreq', 'Interfreq')"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="replace(replace(replace(replace(replace(replace($nodeName,'EUtra', 'Eutra'),'FDD','Fdd'),'eUtra', 'Eutra'),'eSCell', 'esCell'), 'eNB', 'enb'), 'eNodeB', 'enodeb')"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:value-of select="mf:transformStandardName($camelCase)"/>
    </xsl:function>

    <xsl:function name="mf:transformStandardName" as="xs:string">
        <xsl:param name="standardName" as="xs:string"/>
        <xsl:variable name="standardName">
            <xsl:for-each select="1 to string-length($standardName)">
                <xsl:variable name="letter" select="substring($standardName,position(),1)"/>
                <xsl:choose>
                    <xsl:when test="position()=1">
                        <xsl:value-of select="lower-case($letter)"/>
                    </xsl:when>
                    <xsl:when test="contains('ABCDEFGHIJKLMNOPQRSTUVWXYZ', $letter)">
                        <xsl:value-of select="concat('-', lower-case($letter))"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="lower-case($letter)"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </xsl:variable>
        <xsl:value-of select="$standardName"/>
    </xsl:function>

</xsl:stylesheet>
