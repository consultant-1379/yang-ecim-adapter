<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
        version="2.0"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:xs="http://www.w3.org/2001/XMLSchema"
        xmlns:t="urn:templates"
        xmlns:mf="my:functions"
        exclude-result-prefixes="t">

    <xsl:import href="classpath:enodeb-yang-filter-masking-templates.xsl"/>
    <xsl:strip-space elements="*"/>
    <xsl:output omit-xml-declaration="yes"/>

    <xsl:param name="ManagedElement"/>
    <xsl:param name="TemplateType"/>

    <xsl:variable name="LratNS">urn:com:ericsson:ecim:Lrat</xsl:variable>
    <xsl:variable name="ComTopNS">urn:com:ericsson:ecim:ComTop</xsl:variable>
    <xsl:variable name="RmeSupportNS">urn:com:ericsson:ecim:RmeSupport</xsl:variable>
    <xsl:variable name="ReqEquipmentNS">urn:com:ericsson:ecim:ReqEquipment</xsl:variable>
    <xsl:variable name="ReqAntennaSystemNS">urn:com:ericsson:ecim:ReqAntennaSystem</xsl:variable>
    <xsl:variable name="RmeSectorEquipmentFunctionNS">urn:com:ericsson:ecim:RmeSectorEquipmentFunction</xsl:variable>
    <xsl:variable name="templates" select="document('enodeb-yang-to-ecim.xsl')//t:templates" />

    <t:templates>
        <t:enodeb-function/>
        <t:load-balancing-function/>
        <t:qci-table/>
        <t:qci-profile-predefined/>
        <t:qci-profile-operator-defined/>
        <t:carrier-aggregation-function/>
        <t:eutran-cell-fdd/>
        <t:ue-meas-control/>
        <t:report-config-eutra-interfreq-lb/>
        <t:eutran-freq-relation/>
        <t:eutran-cell-relation/>
        <t:eutra-network/>
        <t:external-enodeb-function/>
        <t:external-eutran-cell-fdd/>
        <t:sector-carrier/>
        <t:node-support/>
        <t:sector-equipment-function/>
        <t:equipment/>
        <t:antenna-unit-group/>
        <t:antenna-near-unit/>
        <t:ret-sub-unit/>
        <t:rf-branch/>
    </t:templates>

    <xsl:template match="/">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="/dummyRoot">
        <xsl:element name="ManagedElement" namespace="{$ComTopNS}">
            <xsl:element name="managedElementId" namespace="{$ComTopNS}"><xsl:value-of select="$ManagedElement"/></xsl:element>
            <xsl:choose>
                <xsl:when test="*">
                    <xsl:apply-templates select="node()"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:if test="$TemplateType='subscription'">
                        <xsl:call-template name="ManagedElement"/>
                    </xsl:if>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:element>
    </xsl:template>

    <xsl:template match="/dummyRoot/enodeb-function
                        |/dummyRoot/enodeb-function/load-balancing-function
                        |/dummyRoot/enodeb-function/carrier-aggregation-function
                        |/dummyRoot/enodeb-function/eutra-network
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/ue-meas-control
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/ue-meas-control/report-config-eutra-interfreq-lb">
        <xsl:call-template name="defaultMoHandler"/>
    </xsl:template>

    <xsl:template match="/dummyRoot/enodeb-function/qci-table/qci-profile-predefined
                        |/dummyRoot/enodeb-function/qci-table/qci-profile-operator-defined
                        |/dummyRoot/enodeb-function/qci-table/logical-channel-group
                        |/dummyRoot/enodeb-function/eutra-network/external-enodeb-function
                        |/dummyRoot/enodeb-function/eutra-network/external-enodeb-function/external-eutran-cell-fdd
                        |/dummyRoot/enodeb-function/eutran-cell-fdd
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/eutran-freq-relation
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/eutran-freq-relation/eutran-freq-to-qci-profile-relation
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/eutran-freq-relation/eutran-cell-relation
                        |/dummyRoot/enodeb-function/sector-carrier">
        <xsl:call-template name="defaultMoHandler">
            <xsl:with-param name="withIdValue" select="false()"/>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="/dummyRoot/enodeb-function/qci-table">
        <xsl:call-template name="defaultMoHandler">
            <xsl:with-param name="withIdValue">default</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="/dummyRoot/node-support">
        <xsl:call-template name="defaultMoHandler">
            <xsl:with-param name="namespaceToUse" select="$RmeSupportNS"/>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="/dummyRoot/node-support/sector-equipment-function">
        <xsl:call-template name="defaultMoHandler">
            <xsl:with-param name="namespaceToUse" select="$RmeSectorEquipmentFunctionNS"/>
            <xsl:with-param name="withIdValue" select="false()"/>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="/dummyRoot/equipment">
        <xsl:call-template name="defaultMoHandler">
            <xsl:with-param name="namespaceToUse" select="$ReqEquipmentNS"/>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="/dummyRoot/equipment/antenna-unit-group
                        |/dummyRoot/equipment/antenna-unit-group/antenna-near-unit">
        <xsl:call-template name="defaultMoHandler">
            <xsl:with-param name="namespaceToUse" select="$ReqAntennaSystemNS"/>
            <xsl:with-param name="withIdValue" select="false()"/>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="/dummyRoot/enodeb-function/enb-id
                        |/dummyRoot/enodeb-function/enodeb-plmn-id
                        |/dummyRoot/enodeb-function/eutra-network/external-enodeb-function/enb-id
                        |/dummyRoot/enodeb-function/eutra-network/external-enodeb-function/es-cell-capacity-scaling
                        |/dummyRoot/enodeb-function/eutra-network/external-enodeb-function/enodeb-plmn-id
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/eutran-freq-relation/eutran-cell-relation/q-offset-cell-eutran
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/eutran-freq-relation/eutran-cell-relation/cell-individual-offset-eutran">
        <xsl:call-template name="convertTagWithSpecialChar"/>
    </xsl:template>
    <xsl:template match="/dummyRoot/enodeb-function/eutran-cell-fdd/id
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/eutran-freq-relation/id
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/eutran-freq-relation/eutran-cell-relation/id
                        |/dummyRoot/enodeb-function/eutra-network/external-enodeb-function/id
                        |/dummyRoot/enodeb-function/eutra-network/external-enodeb-function/external-eutran-cell-fdd/id
                        |/dummyRoot/enodeb-function/sector-carrier/id">
        <xsl:call-template name="convertTagWithSpecialChar">
            <xsl:with-param name="yangTag" select="string-join((local-name(..), '-', local-name()), '')"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="/dummyRoot/node-support/sector-equipment-function/id">
        <xsl:call-template name="convertTag">
            <xsl:with-param name="namespaceToUse" select="$RmeSectorEquipmentFunctionNS"/>
            <xsl:with-param name="yangTag" select="string-join((local-name(..), '-', local-name()), '')"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="/dummyRoot/enodeb-function/qci-table/logical-channel-group/id
                        |/dummyRoot/enodeb-function/qci-table/qci-profile-predefined/id
                        |/dummyRoot/enodeb-function/qci-table/qci-profile-operator-defined/id">
        <xsl:call-template name="convertTag">
            <xsl:with-param name="yangTag" select="string-join((local-name(..), '-', local-name()), '')"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="/dummyRoot/equipment/antenna-unit-group/id
                        |/dummyRoot/equipment/antenna-unit-group/rf-branch/id
                        |/dummyRoot/equipment/antenna-unit-group/antenna-near-unit/id
                        |/dummyRoot/equipment/antenna-unit-group/antenna-near-unit/ret-sub-unit/id">
        <xsl:call-template name="convertTag">
            <xsl:with-param name="namespaceToUse" select="$ReqAntennaSystemNS"/>
            <xsl:with-param name="yangTag" select="string-join((local-name(..), '-', local-name()), '')"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="/dummyRoot/enodeb-function/enodeb-plmn-id/mcc
                        |/dummyRoot/enodeb-function/enodeb-plmn-id/mnc
                        |/dummyRoot/enodeb-function/enodeb-plmn-id/mnc-length
                        |/dummyRoot/enodeb-function/eutra-network/external-enodeb-function/enodeb-plmn-id/mcc
                        |/dummyRoot/enodeb-function/eutra-network/external-enodeb-function/enodeb-plmn-id/mnc
                        |/dummyRoot/enodeb-function/eutra-network/external-enodeb-function/enodeb-plmn-id/mnc-length
                        |/dummyRoot/enodeb-function/eutra-network/external-enodeb-function/external-eutran-cell-fdd/local-cell-id
                        |/dummyRoot/enodeb-function/sector-carrier/no-of-rx-antennas
                        |/dummyRoot/enodeb-function/sector-carrier/no-of-tx-antennas
                        |/dummyRoot/enodeb-function/sector-carrier/maximum-transmission-power
                        |/dummyRoot/enodeb-function/sector-carrier/ul-forced-timing-advance-command
                        |/dummyRoot/enodeb-function/sector-carrier/no-of-muted-tx-antennas
                        |/dummyRoot/enodeb-function/sector-carrier/configured-max-tx-power
                        |/dummyRoot/enodeb-function/sector-carrier/tx-power-persistent-lock
                        |/dummyRoot/enodeb-function/sector-carrier/prs-enabled
                        |/dummyRoot/enodeb-function/qci-table/qci-profile-predefined/qci
                        |/dummyRoot/enodeb-function/qci-table/qci-profile-predefined/qci-subscription-quanta
                        |/dummyRoot/enodeb-function/qci-table/qci-profile-predefined/dscp
                        |/dummyRoot/enodeb-function/qci-table/qci-profile-predefined/priority
                        |/dummyRoot/enodeb-function/qci-table/qci-profile-predefined/dl-max-waiting-time
                        |/dummyRoot/enodeb-function/qci-table/qci-profile-predefined/dl-min-bit-rate
                        |/dummyRoot/enodeb-function/qci-table/qci-profile-predefined/pdb
                        |/dummyRoot/enodeb-function/qci-table/qci-profile-predefined/relative-priority
                        |/dummyRoot/enodeb-function/qci-table/qci-profile-predefined/ul-max-waiting-time
                        |/dummyRoot/enodeb-function/qci-table/qci-profile-predefined/ul-min-bit-rate
                        |/dummyRoot/enodeb-function/qci-table/qci-profile-operator-defined/qci
                        |/dummyRoot/enodeb-function/qci-table/qci-profile-operator-defined/qci-subscription-quanta
                        |/dummyRoot/enodeb-function/qci-table/qci-profile-operator-defined/dscp
                        |/dummyRoot/enodeb-function/qci-table/qci-profile-operator-defined/priority
                        |/dummyRoot/enodeb-function/qci-table/qci-profile-operator-defined/dl-max-waiting-time
                        |/dummyRoot/enodeb-function/qci-table/qci-profile-operator-defined/dl-min-bit-rate
                        |/dummyRoot/enodeb-function/qci-table/qci-profile-operator-defined/pdb
                        |/dummyRoot/enodeb-function/qci-table/qci-profile-operator-defined/relative-priority
                        |/dummyRoot/enodeb-function/qci-table/qci-profile-operator-defined/ul-max-waiting-time
                        |/dummyRoot/enodeb-function/qci-table/qci-profile-operator-defined/ul-min-bit-rate
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/ul-channel-bandwidth
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/dl-channel-bandwidth
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/cell-subscription-capacity
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/cell-downlink-ca-capacity
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/cell-id
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/ca-prio-threshold
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/earfcndl
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/earfcnul
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/est-cell-cap-usable-fraction
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/lb-tp-non-qual-fraction
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/lb-tp-rank-thresh-min
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/ul-s-cell-priority
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/physical-layer-cell-id-group
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/physical-layer-sub-cell-id
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/tac
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/dl-configurable-frequency-start
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/dl-interference-management-active
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/ul-srs-enable
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/ue-meas-control/s-measure
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/ue-meas-control/report-config-eutra-interfreq-lb/a5-threshold1-rsrp
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/ue-meas-control/report-config-eutra-interfreq-lb/a5-threshold2-rsrp
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/ue-meas-control/report-config-eutra-interfreq-lb/a5-threshold2-rsrq
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/ue-meas-control/report-config-eutra-interfreq-lb/hysteresis-a5
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/eutran-freq-relation/ca-freq-priority
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/eutran-freq-relation/ca-freq-proportion
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/eutran-freq-relation/ca-triggered-redirection-active
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/eutran-freq-relation/lb-a5-thr1-rsrp-freq-offset
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/eutran-freq-relation/lb-activation-threshold
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/eutran-freq-relation/q-offset-freq
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/eutran-freq-relation/eutran-cell-relation/s-cell-priority
                        |/dummyRoot/enodeb-function/load-balancing-function/lb-threshold
                        |/dummyRoot/enodeb-function/load-balancing-function/lb-ceiling
                        |/dummyRoot/enodeb-function/load-balancing-function/lb-rate-offset-coefficient
                        |/dummyRoot/enodeb-function/load-balancing-function/lb-rate-offset-load-threshold
                        |/dummyRoot/enodeb-function/load-balancing-function/lb-ca-threshold
                        |/dummyRoot/enodeb-function/load-balancing-function/lb-diff-ca-offset
                        |/dummyRoot/enodeb-function/load-balancing-function/lb-ca-cap-hysteresis
                        |/dummyRoot/enodeb-function/carrier-aggregation-function/ca-usage-limit
                        |/dummyRoot/enodeb-function/carrier-aggregation-function/ca-preemption-threshold
                        |/dummyRoot/enodeb-function/carrier-aggregation-function/enhanced-selection-of-mimo-and-ca
                        |/dummyRoot/enodeb-function/carrier-aggregation-function/four-layer-mimo-preferred
                        |/dummyRoot/enodeb-function/carrier-aggregation-function/s-cell-act-deact-data-thres
                        |/dummyRoot/enodeb-function/carrier-aggregation-function/s-cell-act-deact-data-thres-hyst
                        |/dummyRoot/enodeb-function/carrier-aggregation-function/s-cell-schedule-sinr-thres
                        |/dummyRoot/enodeb-function/carrier-aggregation-function/wait-for-ca-opportunity
                        |/dummyRoot/enodeb-function/carrier-aggregation-function/wait-for-better-s-cell-rep
                        |/dummyRoot/enodeb-function/carrier-aggregation-function/wait-for-additional-s-cell-opportunity
                        |/dummyRoot/enodeb-function/carrier-aggregation-function/ca-rate-adjust-coeff
                        |/dummyRoot/enodeb-function/carrier-aggregation-function/s-cell-act-deact-ul-data-thresh
                        |/dummyRoot/enodeb-function/carrier-aggregation-function/s-cell-act-deact-ul-data-thresh-hyst
                        |/dummyRoot/enodeb-function/carrier-aggregation-function/pdcch-enhanced-la-for-volte
                        |/dummyRoot/enodeb-function/carrier-aggregation-function/s-cell-act-prohibit-timer
                        |/dummyRoot/enodeb-function/carrier-aggregation-function/s-cell-deact-prohibit-timer
                        |/dummyRoot/enodeb-function/carrier-aggregation-function/s-cell-deact-out-of-coverage-timer
                        |/dummyRoot/enodeb-function/carrier-aggregation-function/s-cell-deact-delay-timer
                        |/dummyRoot/enodeb-function/sector-carrier/dl-calibration-data">
        <xsl:call-template name="convertTag"/>
    </xsl:template>

    <xsl:template match="/dummyRoot/enodeb-function/qci-table/qci-profile-predefined/resource-type
                        |/dummyRoot/enodeb-function/qci-table/qci-profile-predefined/scheduling-algorithm
                        |/dummyRoot/enodeb-function/qci-table/qci-profile-predefined/abs-prio-override
                        |/dummyRoot/enodeb-function/qci-table/qci-profile-predefined/dl-resource-allocation-strategy
                        |/dummyRoot/enodeb-function/qci-table/qci-profile-predefined/resource-allocation-strategy
                        |/dummyRoot/enodeb-function/qci-table/qci-profile-predefined/service-type
                        |/dummyRoot/enodeb-function/qci-table/qci-profile-predefined/srs-allocation-strategy
                        |/dummyRoot/enodeb-function/qci-table/qci-profile-operator-defined/resource-type
                        |/dummyRoot/enodeb-function/qci-table/qci-profile-operator-defined/scheduling-algorithm
                        |/dummyRoot/enodeb-function/qci-table/qci-profile-operator-defined/abs-prio-override
                        |/dummyRoot/enodeb-function/qci-table/qci-profile-operator-defined/dl-resource-allocation-strategy
                        |/dummyRoot/enodeb-function/qci-table/qci-profile-operator-defined/resource-allocation-strategy
                        |/dummyRoot/enodeb-function/qci-table/qci-profile-operator-defined/service-type
                        |/dummyRoot/enodeb-function/qci-table/qci-profile-operator-defined/srs-allocation-strategy
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/availability-status
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/administrative-state
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/operational-state
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/eutran-freq-relation/eutran-cell-relation/coverage-indicator
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/eutran-freq-relation/eutran-cell-relation/load-balancing
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/eutran-freq-relation/eutran-cell-relation/s-cell-candidate
                        |/dummyRoot/enodeb-function/carrier-aggregation-function/dynamic-s-cell-selection-method
                        |/dummyRoot/enodeb-function/carrier-aggregation-function/ca-preference
                        |/dummyRoot/enodeb-function/carrier-aggregation-function/s-cell-selection-mode
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/eutran-freq-relation/eutran-freq-to-qci-profile-relation/lb-qci-profile-handling
                        |/dummyRoot/enodeb-function/sector-carrier/dl-calibration-data/dl-calibration-active-method
                        |/dummyRoot/enodeb-function/sector-carrier/dl-calibration-data/dl-calibration-status
                        |/dummyRoot/enodeb-function/sector-carrier/dl-calibration-data/dl-calibration-supported-methods
                        |/dummyRoot/enodeb-function/sector-carrier/availability-status
                        |/dummyRoot/enodeb-function/sector-carrier/operational-state
                        |/dummyRoot/enodeb-function/sector-carrier/radio-transmit-performance-mode">
        <xsl:call-template name="convertTag">
            <xsl:with-param name="changeTextToUpperCase" select="true()"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="/dummyRoot/enodeb-function/eutran-cell-fdd/eutran-freq-relation/eutran-freq-to-qci-profile-relation/qci-profile-ref
                        |/dummyRoot/enodeb-function/eutran-cell-fdd/eutran-freq-relation/eutran-cell-relation/neighbor-cell-ref">
        <xsl:call-template name="handleRefElement"/>
    </xsl:template>

    <xsl:template match="/dummyRoot/node-support/sector-equipment-function/administrative-state
                        |/dummyRoot/node-support/sector-equipment-function/availability-status
                        |/dummyRoot/node-support/sector-equipment-function/available-hw-output-power
                        |/dummyRoot/node-support/sector-equipment-function/operational-state
                        |/dummyRoot/node-support/sector-equipment-function/utran-fdd-fq-bands">
        <xsl:call-template name="convertTag">
            <xsl:with-param name="namespaceToUse" select="$RmeSectorEquipmentFunctionNS"/>
            <xsl:with-param name="changeTextToUpperCase" select="true()"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="/dummyRoot/node-support/sector-equipment-function/eutran-fq-bands">
        <xsl:call-template name="convertTagWithSpecialChar">
            <xsl:with-param name="namespaceToUse" select="$RmeSectorEquipmentFunctionNS"/>
            <xsl:with-param name="changeTextToUpperCase" select="true()"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="/dummyRoot/node-support/sector-equipment-function/mixed-mode-radio">
        <xsl:call-template name="convertTag">
            <xsl:with-param name="namespaceToUse" select="$RmeSectorEquipmentFunctionNS"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="/dummyRoot/equipment/antenna-unit-group/rf-branch">
        <xsl:choose>
            <xsl:when test="$TemplateType='subscription'">
                <xsl:choose>
                    <xsl:when test="not(*) or count(*) = 1 and local-name(*) = 'id'">
                        <xsl:call-template name="handleSubscription">
                            <xsl:with-param name="namespaceToUse" select="$ReqAntennaSystemNS"/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:apply-templates/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:element name="{mf:convertYangToEcimWithSpecialCharacters(local-name(.), false())}" namespace="{$ReqAntennaSystemNS}">
                    <xsl:apply-templates select="@*"/>
                    <xsl:apply-templates select="*[local-name()='id']"/>
                    <xsl:call-template name="listToLeafList">
                        <xsl:with-param name="listName" select="'dl-attenuation'"/>
                    </xsl:call-template>
                    <xsl:call-template name="listToLeafList">
                        <xsl:with-param name="listName" select="'dl-traffic-delay'"/>
                    </xsl:call-template>
                    <xsl:call-template name="listToLeafList">
                        <xsl:with-param name="listName" select="'ul-attenuation'"/>
                    </xsl:call-template>
                    <xsl:call-template name="listToLeafList">
                        <xsl:with-param name="listName" select="'ul-traffic-delay'"/>
                    </xsl:call-template>
                </xsl:element>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="/dummyRoot/equipment/antenna-unit-group/antenna-near-unit/ret-sub-unit">
        <xsl:choose>
            <xsl:when test="$TemplateType='subscription'">
                <xsl:choose>
                    <xsl:when test="not(*) or count(*) = 1 and local-name(*) = 'id'">
                        <xsl:call-template name="handleSubscription">
                            <xsl:with-param name="namespaceToUse" select="$ReqAntennaSystemNS"/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:apply-templates/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:element name="{mf:convertYangToEcimWithSpecialCharacters(local-name(.), false())}" namespace="{$ReqAntennaSystemNS}">
                    <xsl:apply-templates select="@*"/>
                    <xsl:apply-templates select="*[(local-name()!='iuant-antenna-operating-gain')]"/>
                    <xsl:call-template name="listToLeafList">
                        <xsl:with-param name="listName" select="'iuant-antenna-operating-gain'"/>
                    </xsl:call-template>
                </xsl:element>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="/dummyRoot/equipment/antenna-unit-group/position-information
                        |/dummyRoot/equipment/antenna-unit-group/antenna-near-unit/hardware-version
                        |/dummyRoot/equipment/antenna-unit-group/antenna-near-unit/iuant-device-type
                        |/dummyRoot/equipment/antenna-unit-group/antenna-near-unit/on-unit-unique-id
                        |/dummyRoot/equipment/antenna-unit-group/antenna-near-unit/product-number
                        |/dummyRoot/equipment/antenna-unit-group/antenna-near-unit/serial-number
                        |/dummyRoot/equipment/antenna-unit-group/antenna-near-unit/software-version
                        |/dummyRoot/equipment/antenna-unit-group/antenna-near-unit/unique-id
                        |/dummyRoot/equipment/antenna-unit-group/antenna-near-unit/ret-sub-unit/electrical-antenna-tilt
                        |/dummyRoot/equipment/antenna-unit-group/antenna-near-unit/ret-sub-unit/iuant-antenna-bearing
                        |/dummyRoot/equipment/antenna-unit-group/antenna-near-unit/ret-sub-unit/iuant-antenna-model-number
                        |/dummyRoot/equipment/antenna-unit-group/antenna-near-unit/ret-sub-unit/iuant-antenna-operating-band
                        |/dummyRoot/equipment/antenna-unit-group/antenna-near-unit/ret-sub-unit/iuant-antenna-serial-number
                        |/dummyRoot/equipment/antenna-unit-group/antenna-near-unit/ret-sub-unit/iuant-base-station-id
                        |/dummyRoot/equipment/antenna-unit-group/antenna-near-unit/ret-sub-unit/iuant-installation-date
                        |/dummyRoot/equipment/antenna-unit-group/antenna-near-unit/ret-sub-unit/iuant-installers-id
                        |/dummyRoot/equipment/antenna-unit-group/antenna-near-unit/ret-sub-unit/iuant-sector-id
                        |/dummyRoot/equipment/antenna-unit-group/antenna-near-unit/ret-sub-unit/max-tilt
                        |/dummyRoot/equipment/antenna-unit-group/antenna-near-unit/ret-sub-unit/min-tilt
                        |/dummyRoot/equipment/antenna-unit-group/antenna-near-unit/ret-sub-unit/subunit-number">
        <xsl:call-template name="convertTag">
            <xsl:with-param name="namespaceToUse" select="$ReqAntennaSystemNS"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="/dummyRoot/equipment/antenna-unit-group/positionInformation
                        |/dummyRoot/equipment/antenna-unit-group/antenna-near-unit/availability-status
                        |/dummyRoot/equipment/antenna-unit-group/antenna-near-unit/operational-state
                        |/dummyRoot/equipment/antenna-unit-group/antenna-near-unit/self-test-status
                        |/dummyRoot/equipment/antenna-unit-group/antenna-near-unit/administrative-state
                        |/dummyRoot/equipment/antenna-unit-group/antenna-near-unit/ret-sub-unit/availability-status
                        |/dummyRoot/equipment/antenna-unit-group/antenna-near-unit/ret-sub-unit/calibration-status
                        |/dummyRoot/equipment/antenna-unit-group/antenna-near-unit/ret-sub-unit/operational-state">
        <xsl:call-template name="convertTag">
            <xsl:with-param name="namespaceToUse" select="$ReqAntennaSystemNS"/>
            <xsl:with-param name="changeTextToUpperCase" select="true()"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="/dummyRoot/enodeb-function/qci-table/qci-profile-predefined/logical-channel-group-ref
                        |/dummyRoot/enodeb-function/qci-table/qci-profile-operator-defined/logical-channel-group-ref">
        <xsl:variable name="elementInEcim" select="mf:stripDashCharacterAndChangeCase(local-name())"/>
        <xsl:element name="{mf:changeFirstCharacterToLowercase($elementInEcim)}" namespace="{$LratNS}">
            <xsl:apply-templates select="@*"/>
            <xsl:variable name="value" select="."/>
            <xsl:if test="$value!=''">
                <xsl:value-of select="concat('ManagedElement=',$ManagedElement,',ENodeBFunction=1,QciTable=default,LogicalChannelGroup=',$value)"/>
            </xsl:if>
        </xsl:element>
    </xsl:template>

    <xsl:template match="/dummyRoot/enodeb-function/eutran-cell-fdd/sector-carrier-ref">
        <xsl:element name="sectorCarrierRef" namespace="{$LratNS}">
            <xsl:apply-templates select="@*"/>
            <xsl:variable name="value" select="."/>
            <xsl:if test="$value!=''">
                <xsl:value-of select="concat('ManagedElement=',$ManagedElement,',ENodeBFunction=1,SectorCarrier=',$value)"/>
            </xsl:if>
        </xsl:element>
    </xsl:template>

    <xsl:template match="/dummyRoot/enodeb-function/sector-carrier/sector-function-ref">
        <xsl:element name="sectorFunctionRef" namespace="{$LratNS}">
            <xsl:apply-templates select="@*"/>
            <xsl:variable name="value" select="."/>
            <xsl:if test="$value!=''">
                <xsl:value-of select="concat('ManagedElement=',$ManagedElement,',NodeSupport=1,SectorEquipmentFunction=',$value)"/>
            </xsl:if>
        </xsl:element>
    </xsl:template>

    <xsl:template match="/dummyRoot/node-support/sector-equipment-function/rf-branch-ref">
        <xsl:call-template name="handleRefElement">
            <xsl:with-param name="namespaceToUse" select="$RmeSectorEquipmentFunctionNS"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="/dummyRoot/enodeb-function/sector-carrier/rf-branch-rx-ref
                        |/dummyRoot/enodeb-function/sector-carrier/rf-branch-tx-ref">
        <xsl:call-template name="listToRefHandler">
            <xsl:with-param name="parentTargetLeafNameInYang" select="'antenna-unit-group'"/>
            <xsl:with-param name="childTargetLeafNameInYang" select="'rf-branch'"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="handleRefElement">
        <xsl:param name="namespaceToUse" select="$LratNS"/>
        <xsl:variable name="elementInEcim" select="mf:stripDashCharacterAndChangeCase(local-name())"/>
        <xsl:element name="{mf:changeFirstCharacterToLowercase($elementInEcim)}" namespace="{$namespaceToUse}">
            <xsl:apply-templates select="@*"/>
            <xsl:for-each select="tokenize(.,'/')">
                <xsl:call-template name="convert-xpath-to-fdn">
                    <xsl:with-param name="in" select="."/>
                </xsl:call-template>
            </xsl:for-each>
        </xsl:element>
    </xsl:template>

    <xsl:template name="listToRefHandler">
        <xsl:param name="namespaceToUse" select="$LratNS"/>
        <xsl:param name="parentTargetLeafNameInYang"/>
        <xsl:param name="childTargetLeafNameInYang"/>
        <xsl:variable name="elementInEcim" select="mf:stripDashCharacterAndChangeCase(local-name())"/>
        <xsl:element name="{mf:changeFirstCharacterToLowercase($elementInEcim)}" namespace="{$namespaceToUse}">
            <xsl:apply-templates select="@*"/>
            <xsl:if test="*[text()]">
                <xsl:variable name="trimRefFromLocalName" select="substring-before(local-name(), 'ref')"/>
                <xsl:if test="$parentTargetLeafNameInYang='antenna-unit-group'">
                    <xsl:text>ManagedElement=1,Equipment=1,AntennaUnitGroup=</xsl:text>
                    <xsl:value-of select="concat(*[local-name()=mf:getLeafrefName($trimRefFromLocalName,$parentTargetLeafNameInYang)], ',')"/>
                </xsl:if>
                <xsl:if test="$childTargetLeafNameInYang='rf-branch'">
                    <xsl:text>RfBranch=</xsl:text>
                    <xsl:value-of select="*[local-name()= mf:getLeafrefName($trimRefFromLocalName,$childTargetLeafNameInYang)]"/>
                </xsl:if>
            </xsl:if>
        </xsl:element>
    </xsl:template>

    <xsl:function name="mf:getLeafrefName">
        <xsl:param name="trimRefFromLocalName" as="xs:string"/>
        <xsl:param name="leafNameInYang" as="xs:string"/>
        <xsl:value-of select="concat($trimRefFromLocalName, $leafNameInYang, '-ref')"/>
    </xsl:function>

    <xsl:template name="convert-xpath-to-fdn">
        <xsl:param name="in"/>
        <xsl:variable name="name" select="substring-before($in, '[')"/>
        <xsl:variable name="apos">&apos;</xsl:variable>
        <xsl:variable name="initialValue" select="translate($in, $apos, '')" />
        <xsl:variable name="value" select="substring-before(substring-after($initialValue, '='), ']')"/>
        <xsl:choose>
            <xsl:when test="$name=''">
                <xsl:choose>
                    <xsl:when test="$in=''">
                        <xsl:value-of select="concat('ManagedElement=',$ManagedElement)"/>
                    </xsl:when>
                    <xsl:when test="contains($in, 'enodeb-function')">
                        <xsl:text>,ENodeBFunction=1</xsl:text>
                    </xsl:when>
                    <xsl:when test="contains($in, 'equipment')">
                        <xsl:text>,Equipment=1</xsl:text>
                    </xsl:when>
                    <xsl:when test="contains($in, 'qci-table')">
                        <xsl:text>,QciTable=default</xsl:text>
                    </xsl:when>
                    <xsl:when test="contains($in, 'eutra-network')">
                        <xsl:text>,EUtraNetwork=1</xsl:text>
                    </xsl:when>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:choose>
                    <xsl:when test="contains($name, 'qci-profile-predefined')">
                        <xsl:value-of select="concat(',QciProfilePredefined=',$value)"/>
                    </xsl:when>
                    <xsl:when test="contains($name, 'qci-profile-operator-defined')">
                        <xsl:value-of select="concat(',QciProfileOperatorDefined=',$value)"/>
                    </xsl:when>
                    <xsl:when test="contains($name, 'logical-channel-group')">
                        <xsl:value-of select="concat(',LogicalChannelGroup=',$value)"/>
                    </xsl:when>
                    <xsl:when test="contains($name, 'external-enodeb-function')">
                        <xsl:value-of select="concat(',ExternalENodeBFunction=',$value)"/>
                    </xsl:when>
                    <xsl:when test="contains($name, 'external-eutran-cell-fdd')">
                        <xsl:value-of select="concat(',ExternalEUtranCellFDD=', $value)"/>
                    </xsl:when>
                    <xsl:when test="contains($name, 'eutran-cell-fdd')">
                        <xsl:value-of select="concat(',EUtranCellFDD=', $value)"/>
                    </xsl:when>
                    <xsl:when test="contains($name, 'antenna-unit-group')">
                        <xsl:value-of select="concat(',AntennaUnitGroup=', $value)"/>
                    </xsl:when>
                    <xsl:when test="contains($name, 'rf-branch')">
                        <xsl:value-of select="concat(',RfBranch=', $value)"/>
                    </xsl:when>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:function name="mf:lower-to-upper">
        <xsl:param name="characterSequence"/>
        <xsl:value-of select="translate($characterSequence, 'abcdefghijklmnopqrstuvwxyz-', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ_')"/>
    </xsl:function>

    <xsl:function name="mf:changeFirstCharacterToLowercase">
        <xsl:param name="characterSequence"/>
        <xsl:value-of select="lower-case(substring($characterSequence,1,1))"/>
        <xsl:value-of select="substring($characterSequence,2)"/>
    </xsl:function>

    <xsl:function name="mf:stripDashCharacterAndChangeCase">
        <xsl:param name="characterSequence"/>
        <xsl:variable name="tokens" select="for $token in tokenize($characterSequence,'-')
                                                return
                                                    concat(upper-case(substring($token,1,1)),
                                                    lower-case(substring($token,2)))"/>
        <xsl:value-of select="string-join($tokens,'')"/>
    </xsl:function>

    <!-- Special handling of specific string patterns -->
    <xsl:function name="mf:handleSpecialCharacterSequence">
        <xsl:param name="characterSequence"/>
        <xsl:sequence select="replace(
                              replace(
                              replace(
                              replace(
                              replace(
                              replace(
                              replace($characterSequence,
                                      'Enodeb', 'ENodeB'),
                                      'Eutra', 'EUtra'),
                                      'CellFdd', 'CellFDD'),
                                      'Enb', 'ENB'),
                                      'Es', 'ES'),
                                      'Interfreq', 'InterFreq'),
                                      'EUtranFreqToQciPro', 'eutranFreqToQciPro')"/>
    </xsl:function>

    <xsl:function name="mf:convertYangToEcimDefault">
        <xsl:param name="strToConvert"/>
        <xsl:variable name="ecimTag" select="mf:stripDashCharacterAndChangeCase($strToConvert)"/>
        <xsl:value-of select="lower-case(substring($ecimTag, 1, 1))"/>
        <xsl:value-of select="substring($ecimTag, 2)"/>
    </xsl:function>

    <xsl:function name="mf:convertYangToEcimWithSpecialCharacters">
        <xsl:param name="strToConvert"/>
        <xsl:param name="lowercaseFirstTagCharacter"/>
        <xsl:variable name="firstConversion" select="mf:stripDashCharacterAndChangeCase($strToConvert)"/>
        <xsl:variable name="secondConversion" select="mf:handleSpecialCharacterSequence($firstConversion)"/>
        <xsl:choose>
            <xsl:when test="$lowercaseFirstTagCharacter">
                <xsl:value-of select="lower-case(substring($secondConversion, 1, 1))"/>
                <xsl:value-of select="substring($secondConversion, 2)"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$secondConversion"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <xsl:template name="convertTag">
        <xsl:param name="namespaceToUse" select="$LratNS"/>
        <xsl:param name="yangTag" select="local-name()"/>
        <xsl:param name="changeTextToUpperCase" select="false()"/>
        <xsl:variable name="ecimTag" select="mf:convertYangToEcimDefault($yangTag)"/>
        <xsl:element name="{$ecimTag}" namespace="{$namespaceToUse}">
            <xsl:apply-templates select="@*"/>
            <xsl:choose>
                <xsl:when test="$changeTextToUpperCase">
                    <xsl:value-of select="mf:lower-to-upper(text())"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates select="node()"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:element>
    </xsl:template>

    <xsl:template name="convertTagWithSpecialChar">
        <xsl:param name="namespaceToUse" select="$LratNS"/>
        <xsl:param name="yangTag" select="local-name()"/>
        <xsl:param name="changeTextToUpperCase" select="false()"/>
        <xsl:variable name="ecimTag" select="mf:convertYangToEcimWithSpecialCharacters($yangTag, true())"/>
        <xsl:element name="{$ecimTag}" namespace="{$namespaceToUse}">
            <xsl:apply-templates select="@*"/>
            <xsl:choose>
                <xsl:when test="$changeTextToUpperCase">
                    <xsl:value-of select="mf:lower-to-upper(text())"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates select="node()"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:element>
    </xsl:template>

    <xsl:template name="createManagedObjectTag">
        <xsl:param name="namespaceToUse" select="$LratNS"/>
        <xsl:param name="withIdValue" select="false()"/>

        <xsl:variable name="ecimTag" select="mf:convertYangToEcimWithSpecialCharacters(local-name(.), false())"/>

        <xsl:element name="{$ecimTag}" namespace="{$namespaceToUse}">
            <xsl:apply-templates select="@*"/>
            <xsl:if test="$withIdValue">
                <xsl:element name="{mf:changeFirstCharacterToLowercase(string-join(($ecimTag, 'Id'), ''))}" namespace="{$namespaceToUse}">
                    <xsl:value-of select="$withIdValue"/>
                </xsl:element>
            </xsl:if>
            <xsl:apply-templates select="node()"/>
        </xsl:element>
    </xsl:template>

    <xsl:template name="defaultMoHandler">
        <xsl:param name="namespaceToUse" select="$LratNS"/>
        <xsl:param name="withIdValue" select="1"/>
        <xsl:choose>
            <xsl:when test="$TemplateType='subscription'">
                <xsl:choose>
                    <xsl:when test="not(*) or count(*) = 1 and local-name(*) = 'id'">
                        <xsl:call-template name="handleSubscription">
                            <xsl:with-param name="namespaceToUse" select="$namespaceToUse"/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="createManagedObjectTag">
                            <xsl:with-param name="namespaceToUse" select="$namespaceToUse"/>
                            <xsl:with-param name="withIdValue" select="false()"/>
                        </xsl:call-template>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>

            <xsl:otherwise>
                <xsl:call-template name="createManagedObjectTag">
                    <xsl:with-param name="namespaceToUse" select="$namespaceToUse"/>
                    <xsl:with-param name="withIdValue" select="$withIdValue"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="handleSubscription">
        <xsl:param name="namespaceToUse"/>
        <xsl:param name="ecimTag" select="mf:convertYangToEcimWithSpecialCharacters(local-name(.), false())"/>
        <xsl:element name="{$ecimTag}" namespace="{$namespaceToUse}">
            <xsl:apply-templates select="@*|node()"/>
            <xsl:variable name="template-name" select="local-name()" />
            <xsl:apply-templates select="$templates/t:*[local-name() = $template-name]" mode="call-template"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="@*">
        <xsl:copy/>
    </xsl:template>

    <xsl:template name="listToLeafList">
        <xsl:param name="listName"/>
        <xsl:for-each select="*[local-name()=$listName]">
            <xsl:sort select="index/text()" data-type="number"/>
            <xsl:element name="{mf:convertYangToEcimDefault(local-name())}" namespace="{$ReqAntennaSystemNS}">
                <xsl:apply-templates select="@*"/>
                <xsl:if test="index/text()=(position()-1)">
                    <xsl:value-of select="value"/>
                </xsl:if>
            </xsl:element>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="*"/>
</xsl:stylesheet>