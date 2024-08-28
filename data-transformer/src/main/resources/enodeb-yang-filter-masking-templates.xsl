<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
        version="2.0"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:t="urn:templates"
        xmlns="urn:com:ericsson:ecim:Lrat"
        exclude-result-prefixes="t">

    <xsl:template name="ManagedElement">
        <ENodeBFunction xmlns="urn:com:ericsson:ecim:Lrat">
            <xsl:call-template name="enodeb-function"/>
        </ENodeBFunction>
        <NodeSupport xmlns="urn:com:ericsson:ecim:RmeSupport">
            <xsl:call-template name="node-support"/>
        </NodeSupport>
        <Equipment xmlns="urn:com:ericsson:ecim:ReqEquipment">
            <xsl:call-template name="equipment"/>
        </Equipment>
    </xsl:template>

    <xsl:template name="enodeb-function" match="t:enodeb-function" mode="call-template">
        <eNodeBPlmnId/>
        <eNBId/>
        <LoadBalancingFunction>
            <xsl:call-template name="load-balancing-function"/>
        </LoadBalancingFunction>
        <QciTable>
            <xsl:call-template name="qci-table"/>
        </QciTable>
        <CarrierAggregationFunction>
            <xsl:call-template name="carrier-aggregation-function"/>
        </CarrierAggregationFunction>
        <EUtranCellFDD>
            <xsl:call-template name="eutran-cell-fdd"/>
        </EUtranCellFDD>
        <EUtraNetwork>
            <xsl:call-template name="eutra-network"/>
        </EUtraNetwork>
        <SectorCarrier>
            <xsl:call-template name="sector-carrier"/>
        </SectorCarrier>
    </xsl:template>

    <xsl:template name="load-balancing-function"  match="t:load-balancing-function" mode="call-template">
        <lbThreshold/>
        <lbCeiling/>
        <lbRateOffsetCoefficient/>
        <lbRateOffsetLoadThreshold/>
        <lbCaThreshold/>
        <lbDiffCaOffset/>
        <lbCaCapHysteresis/>
    </xsl:template>

    <xsl:template name="qci-table"  match="t:qci-table" mode="call-template">
        <QciProfilePredefined>
            <xsl:call-template name="qci-profile-predefined"/>
        </QciProfilePredefined>
        <QciProfileOperatorDefined>
            <xsl:call-template name="qci-profile-operator-defined"/>
        </QciProfileOperatorDefined>
    </xsl:template>

    <xsl:template name="qci-profile-predefined"  match="t:qci-profile-predefined" mode="call-template">
        <qciSubscriptionQuanta/>
        <dscp/>
        <logicalChannelGroupRef/>
        <priority/>
        <schedulingAlgorithm/>
        <absPrioOverride/>
        <dlMaxWaitingTime/>
        <dlMinBitRate/>
        <dlResourceAllocationStrategy/>
        <pdb/>
        <relativePriority/>
        <resourceAllocationStrategy/>
        <serviceType/>
        <srsAllocationStrategy/>
        <ulMaxWaitingTime/>
        <ulMinBitRate/>
    </xsl:template>
    <xsl:template name="qci-profile-operator-defined" match="t:qci-profile-operator-defined" mode="call-template">
        <qci/>
        <qciSubscriptionQuanta/>
        <dscp/>
        <logicalChannelGroupRef/>
        <priority/>
        <resourceType/>
        <schedulingAlgorithm/>
        <absPrioOverride/>
        <dlMaxWaitingTime/>
        <dlMinBitRate/>
        <dlResourceAllocationStrategy/>
        <pdb/>
        <relativePriority/>
        <resourceAllocationStrategy/>
        <serviceType/>
        <srsAllocationStrategy/>
        <ulMaxWaitingTime/>
        <ulMinBitRate/>
    </xsl:template>

    <xsl:template name="carrier-aggregation-function" match="t:carrier-aggregation-function" mode="call-template">
        <caUsageLimit/>
        <caPreemptionThreshold/>
        <sCellActDeactDataThres/>
        <sCellActDeactDataThresHyst/>
        <sCellScheduleSinrThres/>
        <waitForCaOpportunity/>
        <waitForBetterSCellRep/>
        <waitForAdditionalSCellOpportunity/>
        <caRateAdjustCoeff/>
        <sCellActDeactUlDataThresh/>
        <sCellActDeactUlDataThreshHyst/>
        <pdcchEnhancedLaForVolte/>
        <sCellSelectionMode/>
        <sCellActProhibitTimer/>
        <sCellDeactProhibitTimer/>
        <sCellDeactOutOfCoverageTimer/>
        <sCellDeactDelayTimer/>
        <dynamicSCellSelectionMethod/>
        <caPreference/>
        <enhancedSelectionOfMimoAndCa/>
        <fourLayerMimoPreferred/>
    </xsl:template>

    <xsl:template name="eutran-cell-fdd" match="t:eutran-cell-fdd" mode="call-template">
        <ulChannelBandwidth/>
        <cellSubscriptionCapacity/>
        <cellDownlinkCaCapacity/>
        <dlChannelBandwidth/>
        <cellId/>
        <caPrioThreshold/>
        <earfcndl/>
        <earfcnul/>
        <estCellCapUsableFraction/>
        <lbTpNonQualFraction/>
        <lbTpRankThreshMin/>
        <ulSCellPriority/>
        <physicalLayerCellIdGroup/>
        <physicalLayerSubCellId/>
        <tac/>
        <dlConfigurableFrequencyStart/>
        <dlInterferenceManagementActive/>
        <ulSrsEnable/>
        <sectorCarrierRef/>
        <administrativeState/>
        <UeMeasControl>
            <xsl:call-template name="ue-meas-control"/>
        </UeMeasControl>
        <EUtranFreqRelation>
            <xsl:call-template name="eutran-freq-relation"/>
        </EUtranFreqRelation>
    </xsl:template>
    <xsl:template name="ue-meas-control" match="t:ue-meas-control" mode="call-template">
        <sMeasure/>
        <ReportConfigEUtraInterFreqLb>
            <xsl:call-template name="report-config-eutra-interfreq-lb"/>
        </ReportConfigEUtraInterFreqLb>
    </xsl:template>
    <xsl:template name="report-config-eutra-interfreq-lb" match="t:report-config-eutra-interfreq-lb" mode="call-template">
        <a5Threshold1Rsrp/>
        <a5Threshold2Rsrp/>
        <a5Threshold2Rsrq/>
        <hysteresisA5/>
    </xsl:template>
    <xsl:template name="eutran-freq-relation" match="t:eutran-freq-relation" mode="call-template">
        <caFreqPriority/>
        <caFreqProportion/>
        <caTriggeredRedirectionActive/>
        <lbA5Thr1RsrpFreqOffset/>
        <lbActivationThreshold/>
        <eutranFreqToQciProfileRelation/>
        <qOffsetFreq/>
        <EUtranCellRelation>
            <xsl:call-template name="eutran-cell-relation"/>
        </EUtranCellRelation>
    </xsl:template>
    <xsl:template name="eutran-cell-relation" match="t:eutran-cell-relation" mode="call-template">
        <cellIndividualOffsetEUtran/>
        <qOffsetCellEUtran/>
        <coverageIndicator/>
        <loadBalancing/>
        <sCellCandidate/>
        <sCellPriority/>
        <neighborCellRef/>
    </xsl:template>
    <xsl:template name="eutra-network" match="t:eutra-network" mode="call-template">
        <ExternalENodeBFunction>
            <xsl:call-template name="external-enodeb-function"/>
        </ExternalENodeBFunction>
    </xsl:template>

    <xsl:template name="external-enodeb-function" match="t:external-enodeb-function" mode="call-template">
        <eNBId/>
        <eSCellCapacityScaling/>
        <ExternalEUtranCellFDD>
            <xsl:call-template name="external-eutran-cell-fdd"/>
        </ExternalEUtranCellFDD>
    </xsl:template>
    <xsl:template name="external-eutran-cell-fdd" match="t:external-eutran-cell-fdd" mode="call-template">
        <localCellId/>
    </xsl:template>

    <xsl:template name="sector-carrier" match="t:sector-carrier" mode="call-template">
        <sectorFunctionRef/>
        <noOfRxAntennas/>
        <noOfTxAntennas/>
        <prsEnabled/>
        <ulForcedTimingAdvanceCommand/>
        <radioTransmitPerformanceMode/>
        <configuredMaxTxPower/>
        <txPowerPersistentLock/>
        <rfBranchRxRef/>
        <rfBranchTxRef/>
    </xsl:template>
    <xsl:template name="node-support" match="t:node-support" mode="call-template">
        <xsl:element name="SectorEquipmentFunction" namespace="{$RmeSectorEquipmentFunctionNS}">
            <xsl:call-template name="sector-equipment-function"/>
        </xsl:element>
    </xsl:template>
    <xsl:template name="sector-equipment-function" match="t:sector-equipment-function" mode="call-template">
        <xsl:element name="administrativeState" namespace="{$RmeSectorEquipmentFunctionNS}"/>
        <xsl:element name="mixedModeRadio" namespace="{$RmeSectorEquipmentFunctionNS}"/>
        <xsl:element name="rfBranchRef" namespace="{$RmeSectorEquipmentFunctionNS}"/>
    </xsl:template>

    <xsl:template name="ret-sub-unit" match="t:ret-sub-unit" mode="call-template">
        <xsl:call-template name="createElements">
            <xsl:with-param name="commaSeperatedAttributeList"
                            select="'electricalAntennaTilt,
                                    iuantAntennaBearing,
                                    iuantAntennaModelNumber,
                                    iuantAntennaOperatingBand,
                                    iuantAntennaSerialNumber,
                                    iuantBaseStationId,
                                    iuantInstallationDate,
                                    iuantInstallersId,
                                    iuantSectorId,
                                    maxTilt,
                                    minTilt,
                                    iuantAntennaOperatingGain'"/>
            <xsl:with-param name="nameSpace" select="$ReqAntennaSystemNS"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="antenna-near-unit" match="t:antenna-near-unit" mode="call-template">
        <xsl:call-template name="createElements">
            <xsl:with-param name="commaSeperatedAttributeList"
                            select="'iuantDeviceType, uniqueId, administrativeState'"/>
            <xsl:with-param name="nameSpace" select="$ReqAntennaSystemNS"/>
        </xsl:call-template>
        <xsl:element name="RetSubUnit" namespace="{$ReqAntennaSystemNS}">
            <xsl:call-template name="ret-sub-unit"/>
        </xsl:element>
    </xsl:template>

    <xsl:template name="rf-branch" match="t:rf-branch" mode="call-template">
        <xsl:call-template name="createElements">
            <xsl:with-param name="commaSeperatedAttributeList"
                            select="'dlAttenuation, dlTrafficDelay, ulAttenuation, ulTrafficDelay'"/>
            <xsl:with-param name="nameSpace" select="$ReqAntennaSystemNS"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="antenna-unit-group" match="t:antenna-unit-group" mode="call-template">
        <xsl:element name="AntennaNearUnit" namespace="{$ReqAntennaSystemNS}">
            <xsl:call-template name="antenna-near-unit"/>
        </xsl:element>
        <xsl:element name="RfBranch" namespace="{$ReqAntennaSystemNS}">
            <xsl:call-template name="rf-branch"/>
        </xsl:element>
    </xsl:template>

    <xsl:template name="equipment" match="t:equipment" mode="call-template">
        <xsl:element name="AntennaUnitGroup" namespace="{$ReqAntennaSystemNS}">
            <xsl:call-template name="antenna-unit-group"/>
        </xsl:element>
    </xsl:template>

    <xsl:template name="createElements">
        <xsl:param name="commaSeperatedAttributeList"/>
        <xsl:param name="nameSpace"/>
        <xsl:for-each select="tokenize($commaSeperatedAttributeList, ',')">
            <xsl:element name="{.}" namespace="{$nameSpace}"/>
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>
