<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:a="urn:com:ericsson:ecim:ComTop"
                xmlns:b="urn:com:ericsson:ecim:Lrat"
                xmlns:c="urn:com:ericsson:ecim:RmeSupport"
                xmlns:d="urn:com:ericsson:ecim:RmeSectorEquipmentFunction"
                xmlns:e="urn:com:ericsson:ecim:ReqEquipment"
                xmlns:f="urn:com:ericsson:ecim:ReqAntennaSystem"
                xmlns:mf="my:functions"
                exclude-result-prefixes="a b c d e f">
    <xsl:output omit-xml-declaration="yes"/>
    <xsl:output indent="yes" />
    <xsl:strip-space elements="*"/>

    <xsl:variable name="lrat">urn:rdns:com:ericsson:oammodel:ericsson-lrat-enb-adapter</xsl:variable>
    <xsl:variable name="resf">urn:rdns:com:ericsson:oammodel:ericsson-rme-sef-enb-adapter</xsl:variable>
    <xsl:variable name="reqeqadpt">urn:rdns:com:ericsson:oammodel:ericsson-req-equip-enb-adapter</xsl:variable>
    <xsl:variable name="reqantadpt">urn:rdns:com:ericsson:oammodel:ericsson-req-antenna-enb-adapter</xsl:variable>

    <xsl:template match="/a:ManagedElement">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="/a:ManagedElement/b:ENodeBFunction
                        |/a:ManagedElement/b:ENodeBFunction/b:QciTable
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtraNetwork
                        |/a:ManagedElement/b:ENodeBFunction/b:CarrierAggregationFunction
                        |/a:ManagedElement/b:ENodeBFunction/b:LoadBalancingFunction
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:UeMeasControl/b:ReportConfigEUtraInterFreqLb">
        <xsl:call-template name="MOHandler"/>
    </xsl:template>
    <xsl:template match="/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:QciProfilePredefined
                        |/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:QciProfileOperatorDefined
                        |/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:LogicalChannelGroup
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtraNetwork/b:ExternalENodeBFunction
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtraNetwork/b:ExternalENodeBFunction/b:ExternalEUtranCellFDD
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:EUtranFreqRelation
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:EUtranFreqRelation/b:EUtranCellRelation">
        <xsl:call-template name="MOHandler">
            <xsl:with-param name="withId" select="true()"/>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD">
        <xsl:element name="{mf:yangString(local-name(), true())}" namespace="{$lrat}">
            <xsl:apply-templates select="*[local-name()= 'eUtranCellFDDId'], *[local-name()!= 'eUtranCellFDDId']"/>
            <xsl:call-template name="leafListHandler">
                <xsl:with-param name="leaflistName" select="'availabilityStatus'"/>
                <xsl:with-param name="leaflistConvert" select="true()"/>
            </xsl:call-template>
        </xsl:element>
    </xsl:template>
    <xsl:template match="/a:ManagedElement/c:NodeSupport">
        <xsl:call-template name="MOHandler">
            <xsl:with-param name="namespace" select="$resf"/>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="/a:ManagedElement/e:Equipment">
        <xsl:call-template name="MOHandler">
            <xsl:with-param name="namespace" select="$reqeqadpt"/>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="/a:ManagedElement/e:Equipment/f:AntennaUnitGroup
                        |/a:ManagedElement/e:Equipment/f:AntennaUnitGroup/f:RfBranch">
        <xsl:call-template name="MOHandler">
            <xsl:with-param name="namespace" select="$reqantadpt"/>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:QciProfilePredefined/b:qciProfilePredefinedId
                        |/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:QciProfileOperatorDefined/b:qciProfileOperatorDefinedId
                        |/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:LogicalChannelGroup/b:logicalChannelGroupId
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtraNetwork/b:ExternalENodeBFunction/b:externalENodeBFunctionId
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtraNetwork/b:ExternalENodeBFunction/b:ExternalEUtranCellFDD/b:externalEUtranCellFDDId
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:eUtranCellFDDId
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:EUtranFreqRelation/b:EUtranCellRelation/b:eUtranCellRelationId
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:EUtranFreqRelation/b:eUtranFreqRelationId
                        |/a:ManagedElement/b:ENodeBFunction/b:SectorCarrier/b:sectorCarrierId">
        <xsl:call-template name="changeToDefaultId"/>
    </xsl:template>
    <xsl:template match="/a:ManagedElement/c:NodeSupport/d:SectorEquipmentFunction/d:sectorEquipmentFunctionId">
        <xsl:call-template name="changeToDefaultId">
            <xsl:with-param name="useNamespace" select="$resf"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="/a:ManagedElement/e:Equipment/f:AntennaUnitGroup/f:antennaUnitGroupId
                        |/a:ManagedElement/e:Equipment/f:AntennaUnitGroup/f:AntennaNearUnit/f:antennaNearUnitId
                        |/a:ManagedElement/e:Equipment/f:AntennaUnitGroup/f:AntennaNearUnit/f:RetSubUnit/f:retSubUnitId
                        |/a:ManagedElement/e:Equipment/f:AntennaUnitGroup/f:RfBranch/f:rfBranchId">
        <xsl:call-template name="changeToDefaultId">
            <xsl:with-param name="useNamespace" select="$reqantadpt"/>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="/a:ManagedElement/b:ENodeBFunction/b:eNBId
                        |/a:ManagedElement/b:ENodeBFunction/b:eNodeBPlmnId
                        |/a:ManagedElement/b:ENodeBFunction/b:eNodeBPlmnId/b:mcc
                        |/a:ManagedElement/b:ENodeBFunction/b:eNodeBPlmnId/b:mnc
                        |/a:ManagedElement/b:ENodeBFunction/b:eNodeBPlmnId/b:mncLength
                        |/a:ManagedElement/b:ENodeBFunction/b:LoadBalancingFunction/b:lbThreshold
                        |/a:ManagedElement/b:ENodeBFunction/b:LoadBalancingFunction/b:lbCeiling
                        |/a:ManagedElement/b:ENodeBFunction/b:LoadBalancingFunction/b:lbRateOffsetLoadThreshold
                        |/a:ManagedElement/b:ENodeBFunction/b:LoadBalancingFunction/b:lbRateOffsetCoefficient
                        |/a:ManagedElement/b:ENodeBFunction/b:LoadBalancingFunction/b:lbCaThreshold
                        |/a:ManagedElement/b:ENodeBFunction/b:LoadBalancingFunction/b:lbDiffCaOffset
                        |/a:ManagedElement/b:ENodeBFunction/b:LoadBalancingFunction/b:lbCaCapHysteresis
                        |/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:QciProfilePredefined/b:qciSubscriptionQuanta
                        |/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:QciProfilePredefined/b:qci
                        |/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:QciProfilePredefined/b:dscp
                        |/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:QciProfilePredefined/b:priority
                        |/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:QciProfilePredefined/b:dlMaxWaitingTime
                        |/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:QciProfilePredefined/b:dlMinBitRate
                        |/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:QciProfilePredefined/b:pdb
                        |/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:QciProfilePredefined/b:relativePriority
                        |/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:QciProfilePredefined/b:ulMaxWaitingTime
                        |/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:QciProfilePredefined/b:ulMinBitRate
                        |/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:QciProfileOperatorDefined/b:qci
                        |/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:QciProfileOperatorDefined/b:qciSubscriptionQuanta
                        |/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:QciProfileOperatorDefined/b:dscp
                        |/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:QciProfileOperatorDefined/b:priority
                        |/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:QciProfileOperatorDefined/b:dlMaxWaitingTime
                        |/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:QciProfileOperatorDefined/b:dlMinBitRate
                        |/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:QciProfileOperatorDefined/b:pdb
                        |/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:QciProfileOperatorDefined/b:relativePriority
                        |/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:QciProfileOperatorDefined/b:ulMaxWaitingTime
                        |/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:QciProfileOperatorDefined/b:ulMinBitRate
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtraNetwork/b:ExternalENodeBFunction/b:eNBId
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtraNetwork/b:ExternalENodeBFunction/b:eSCellCapacityScaling
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtraNetwork/b:ExternalENodeBFunction/b:eNodeBPlmnId
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtraNetwork/b:ExternalENodeBFunction/b:eNodeBPlmnId/b:mcc
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtraNetwork/b:ExternalENodeBFunction/b:eNodeBPlmnId/b:mnc
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtraNetwork/b:ExternalENodeBFunction/b:eNodeBPlmnId/b:mncLength
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtraNetwork/b:ExternalENodeBFunction/b:ExternalEUtranCellFDD/b:localCellId
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:dlChannelBandwidth
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:ulChannelBandwidth
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:cellDownlinkCaCapacity
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:cellSubscriptionCapacity
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:earfcndl
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:earfcnul
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:cellId
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:caPrioThreshold
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:estCellCapUsableFraction
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:lbTpNonQualFraction
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:lbTpRankThreshMin
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:ulSCellPriority
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:physicalLayerCellIdGroup
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:physicalLayerSubCellId
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:tac
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:dlConfigurableFrequencyStart
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:dlInterferenceManagementActive
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:ulSrsEnable
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:UeMeasControl
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:UeMeasControl/b:sMeasure
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:UeMeasControl/b:ReportConfigEUtraInterFreqLb/b:a5Threshold1Rsrp
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:UeMeasControl/b:ReportConfigEUtraInterFreqLb/b:a5Threshold2Rsrp
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:UeMeasControl/b:ReportConfigEUtraInterFreqLb/b:a5Threshold2Rsrq
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:UeMeasControl/b:ReportConfigEUtraInterFreqLb/b:hysteresisA5
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:EUtranFreqRelation/b:caFreqPriority
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:EUtranFreqRelation/b:caFreqProportion
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:EUtranFreqRelation/b:caTriggeredRedirectionActive
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:EUtranFreqRelation/b:lbActivationThreshold
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:EUtranFreqRelation/b:lbA5Thr1RsrpFreqOffset
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:EUtranFreqRelation/b:qOffsetFreq
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:EUtranFreqRelation/b:eutranFreqToQciProfileRelation
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:EUtranFreqRelation/b:EUtranCellRelation/b:sCellPriority
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:EUtranFreqRelation/b:EUtranCellRelation/b:cellIndividualOffsetEUtran
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:EUtranFreqRelation/b:EUtranCellRelation/b:qOffsetCellEUtran
                        |/a:ManagedElement/b:ENodeBFunction/b:CarrierAggregationFunction/b:caUsageLimit
                        |/a:ManagedElement/b:ENodeBFunction/b:CarrierAggregationFunction/b:caPreemptionThreshold
                        |/a:ManagedElement/b:ENodeBFunction/b:CarrierAggregationFunction/b:sCellActDeactDataThres
                        |/a:ManagedElement/b:ENodeBFunction/b:CarrierAggregationFunction/b:sCellActDeactDataThresHyst
                        |/a:ManagedElement/b:ENodeBFunction/b:CarrierAggregationFunction/b:sCellScheduleSinrThres
                        |/a:ManagedElement/b:ENodeBFunction/b:CarrierAggregationFunction/b:waitForCaOpportunity
                        |/a:ManagedElement/b:ENodeBFunction/b:CarrierAggregationFunction/b:waitForBetterSCellRep
                        |/a:ManagedElement/b:ENodeBFunction/b:CarrierAggregationFunction/b:waitForAdditionalSCellOpportunity
                        |/a:ManagedElement/b:ENodeBFunction/b:CarrierAggregationFunction/b:caRateAdjustCoeff
                        |/a:ManagedElement/b:ENodeBFunction/b:CarrierAggregationFunction/b:sCellActDeactUlDataThresh
                        |/a:ManagedElement/b:ENodeBFunction/b:CarrierAggregationFunction/b:sCellActDeactUlDataThreshHyst
                        |/a:ManagedElement/b:ENodeBFunction/b:CarrierAggregationFunction/b:pdcchEnhancedLaForVolte
                        |/a:ManagedElement/b:ENodeBFunction/b:CarrierAggregationFunction/b:sCellActProhibitTimer
                        |/a:ManagedElement/b:ENodeBFunction/b:CarrierAggregationFunction/b:sCellDeactProhibitTimer
                        |/a:ManagedElement/b:ENodeBFunction/b:CarrierAggregationFunction/b:sCellDeactOutOfCoverageTimer
                        |/a:ManagedElement/b:ENodeBFunction/b:CarrierAggregationFunction/b:sCellDeactDelayTimer
                        |/a:ManagedElement/b:ENodeBFunction/b:CarrierAggregationFunction/b:enhancedSelectionOfMimoAndCa
                        |/a:ManagedElement/b:ENodeBFunction/b:CarrierAggregationFunction/b:fourLayerMimoPreferred
                        |/a:ManagedElement/b:ENodeBFunction/b:SectorCarrier/b:dlCalibrationData
                        |/a:ManagedElement/b:ENodeBFunction/b:SectorCarrier/b:noOfRxAntennas
                        |/a:ManagedElement/b:ENodeBFunction/b:SectorCarrier/b:noOfTxAntennas
                        |/a:ManagedElement/b:ENodeBFunction/b:SectorCarrier/b:maximumTransmissionPower
                        |/a:ManagedElement/b:ENodeBFunction/b:SectorCarrier/b:prsEnabled
                        |/a:ManagedElement/b:ENodeBFunction/b:SectorCarrier/b:ulForcedTimingAdvanceCommand
                        |/a:ManagedElement/b:ENodeBFunction/b:SectorCarrier/b:noOfMutedTxAntennas
                        |/a:ManagedElement/b:ENodeBFunction/b:SectorCarrier/b:configuredMaxTxPower
                        |/a:ManagedElement/b:ENodeBFunction/b:SectorCarrier/b:txPowerPersistentLock">
        <xsl:call-template name="defaultAttrHandler"/>
    </xsl:template>
    <xsl:template match="/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:QciProfilePredefined/b:resourceType
                        |/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:QciProfilePredefined/b:schedulingAlgorithm
                        |/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:QciProfilePredefined/b:absPrioOverride
                        |/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:QciProfilePredefined/b:dlResourceAllocationStrategy
                        |/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:QciProfilePredefined/b:resourceAllocationStrategy
                        |/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:QciProfilePredefined/b:serviceType
                        |/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:QciProfilePredefined/b:srsAllocationStrategy
                        |/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:QciProfileOperatorDefined/b:resourceType
                        |/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:QciProfileOperatorDefined/b:schedulingAlgorithm
                        |/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:QciProfileOperatorDefined/b:absPrioOverride
                        |/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:QciProfileOperatorDefined/b:dlResourceAllocationStrategy
                        |/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:QciProfileOperatorDefined/b:resourceAllocationStrategy
                        |/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:QciProfileOperatorDefined/b:serviceType
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:administrativeState
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:operationalState
                        |/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:QciProfileOperatorDefined/b:srsAllocationStrategy
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:EUtranFreqRelation/b:eutranFreqToQciProfileRelation/b:lbQciProfileHandling
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:EUtranFreqRelation/b:EUtranCellRelation/b:coverageIndicator
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:EUtranFreqRelation/b:EUtranCellRelation/b:loadBalancing
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:EUtranFreqRelation/b:EUtranCellRelation/b:sCellCandidate
                        |/a:ManagedElement/b:ENodeBFunction/b:CarrierAggregationFunction/b:sCellSelectionMode
                        |/a:ManagedElement/b:ENodeBFunction/b:CarrierAggregationFunction/b:dynamicSCellSelectionMethod
                        |/a:ManagedElement/b:ENodeBFunction/b:CarrierAggregationFunction/b:caPreference
                        |/a:ManagedElement/b:ENodeBFunction/b:SectorCarrier/b:operationalState
                        |/a:ManagedElement/b:ENodeBFunction/b:SectorCarrier/b:radioTransmitPerformanceMode
                        |/a:ManagedElement/b:ENodeBFunction/b:SectorCarrier/b:dlCalibrationData/b:dlCalibrationActiveMethod
                        |/a:ManagedElement/b:ENodeBFunction/b:SectorCarrier/b:dlCalibrationData/b:dlCalibrationStatus
                        |/a:ManagedElement/b:ENodeBFunction/b:SectorCarrier/b:dlCalibrationData/b:dlCalibrationSupportedMethods">
        <xsl:call-template name="defaultAttrHandler">
            <xsl:with-param name="convert" select="true()"/>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="/a:ManagedElement/b:ENodeBFunction/b:SectorCarrier">
        <xsl:element name="{mf:yangString(local-name(), true())}" namespace="{$lrat}">
            <xsl:apply-templates/>
            <xsl:call-template name="leafListHandler">
                <xsl:with-param name="namespace" select="$lrat"/>
                <xsl:with-param name="leaflistName" select="'availabilityStatus'"/>
                <xsl:with-param name="leaflistConvert" select="true()"/>
            </xsl:call-template>
        </xsl:element>
    </xsl:template>
    <xsl:template match="/a:ManagedElement/c:NodeSupport/d:SectorEquipmentFunction">
        <xsl:element name="{mf:yangString(local-name(), true())}" namespace="{$resf}">
            <xsl:apply-templates select="*[local-name()= 'sectorEquipmentFunctionId'], *[local-name()!= 'sectorEquipmentFunctionId']"/>
            <xsl:call-template name="leafListHandler">
                <xsl:with-param name="namespace" select="$resf"/>
                <xsl:with-param name="leaflistName" select="'availabilityStatus'"/>
                <xsl:with-param name="leaflistConvert" select="true()"/>
            </xsl:call-template>
        </xsl:element>
    </xsl:template>
    <xsl:template match="/a:ManagedElement/c:NodeSupport/d:SectorEquipmentFunction/d:eUtranFqBands
                        |/a:ManagedElement/c:NodeSupport/d:SectorEquipmentFunction/d:utranFddFqBands">
        <xsl:call-template name="defaultAttrHandler">
            <xsl:with-param name="useNamespace" select="$resf"/>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="/a:ManagedElement/c:NodeSupport/d:SectorEquipmentFunction/d:administrativeState
                        |/a:ManagedElement/c:NodeSupport/d:SectorEquipmentFunction/d:mixedModeRadio
                        |/a:ManagedElement/c:NodeSupport/d:SectorEquipmentFunction/d:operationalState
                        |/a:ManagedElement/c:NodeSupport/d:SectorEquipmentFunction/d:availableHwOutputPower">
        <xsl:call-template name="defaultAttrHandler">
            <xsl:with-param name="useNamespace" select="$resf"/>
            <xsl:with-param name="convert" select="true()"/>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="/a:ManagedElement/e:Equipment/f:AntennaUnitGroup/f:AntennaNearUnit
                        |/a:ManagedElement/e:Equipment/f:AntennaUnitGroup/f:AntennaNearUnit/f:RetSubUnit">
        <xsl:element name="{mf:yangString(local-name(), true())}" namespace="{$reqantadpt}">
            <xsl:variable name="id" select= "mf:getIdName(local-name())"/>
            <xsl:apply-templates select="*[lower-case(local-name())= $id], *[lower-case(local-name())!= $id]"/>
            <xsl:call-template name="leafListHandler">
                <xsl:with-param name="namespace" select="$reqantadpt"/>
                <xsl:with-param name="leaflistName" select="'availabilityStatus'"/>
                <xsl:with-param name="leaflistConvert" select="true()"/>
            </xsl:call-template>
        </xsl:element>
    </xsl:template>
    <xsl:template match="/a:ManagedElement/e:Equipment/f:AntennaUnitGroup/f:positionInformation
                        |/a:ManagedElement/e:Equipment/f:AntennaUnitGroup/f:AntennaNearUnit/f:hardwareVersion
                        |/a:ManagedElement/e:Equipment/f:AntennaUnitGroup/f:AntennaNearUnit/f:iuantDeviceType
                        |/a:ManagedElement/e:Equipment/f:AntennaUnitGroup/f:AntennaNearUnit/f:onUnitUniqueId
                        |/a:ManagedElement/e:Equipment/f:AntennaUnitGroup/f:AntennaNearUnit/f:productNumber
                        |/a:ManagedElement/e:Equipment/f:AntennaUnitGroup/f:AntennaNearUnit/f:serialNumber
                        |/a:ManagedElement/e:Equipment/f:AntennaUnitGroup/f:AntennaNearUnit/f:softwareVersion
                        |/a:ManagedElement/e:Equipment/f:AntennaUnitGroup/f:AntennaNearUnit/f:uniqueId
                        |/a:ManagedElement/e:Equipment/f:AntennaUnitGroup/f:AntennaNearUnit/f:RetSubUnit/f:electricalAntennaTilt
                        |/a:ManagedElement/e:Equipment/f:AntennaUnitGroup/f:AntennaNearUnit/f:RetSubUnit/f:iuantAntennaBearing
                        |/a:ManagedElement/e:Equipment/f:AntennaUnitGroup/f:AntennaNearUnit/f:RetSubUnit/f:iuantAntennaModelNumber
                        |/a:ManagedElement/e:Equipment/f:AntennaUnitGroup/f:AntennaNearUnit/f:RetSubUnit/f:iuantAntennaOperatingBand
                        |/a:ManagedElement/e:Equipment/f:AntennaUnitGroup/f:AntennaNearUnit/f:RetSubUnit/f:iuantAntennaSerialNumber
                        |/a:ManagedElement/e:Equipment/f:AntennaUnitGroup/f:AntennaNearUnit/f:RetSubUnit/f:iuantBaseStationId
                        |/a:ManagedElement/e:Equipment/f:AntennaUnitGroup/f:AntennaNearUnit/f:RetSubUnit/f:iuantInstallationDate
                        |/a:ManagedElement/e:Equipment/f:AntennaUnitGroup/f:AntennaNearUnit/f:RetSubUnit/f:iuantInstallersId
                        |/a:ManagedElement/e:Equipment/f:AntennaUnitGroup/f:AntennaNearUnit/f:RetSubUnit/f:iuantSectorId
                        |/a:ManagedElement/e:Equipment/f:AntennaUnitGroup/f:AntennaNearUnit/f:RetSubUnit/f:maxTilt
                        |/a:ManagedElement/e:Equipment/f:AntennaUnitGroup/f:AntennaNearUnit/f:RetSubUnit/f:minTilt
                        |/a:ManagedElement/e:Equipment/f:AntennaUnitGroup/f:AntennaNearUnit/f:RetSubUnit/f:subunitNumber">
        <xsl:call-template name="defaultAttrHandler">
            <xsl:with-param name="useNamespace" select="$reqantadpt"/>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="/a:ManagedElement/e:Equipment/f:AntennaUnitGroup/f:RfBranch/f:dlTrafficDelay
                        |/a:ManagedElement/e:Equipment/f:AntennaUnitGroup/f:RfBranch/f:ulTrafficDelay
                        |/a:ManagedElement/e:Equipment/f:AntennaUnitGroup/f:RfBranch/f:dlAttenuation
                        |/a:ManagedElement/e:Equipment/f:AntennaUnitGroup/f:RfBranch/f:ulAttenuation
                        |/a:ManagedElement/e:Equipment/f:AntennaUnitGroup/f:AntennaNearUnit/f:RetSubUnit/f:iuantAntennaOperatingGain">
        <xsl:call-template name="leafListToListHandler">
            <xsl:with-param name="namespace" select="$reqantadpt"/>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="/a:ManagedElement/e:Equipment/f:AntennaUnitGroup/f:AntennaNearUnit/f:operationalState
                        |/a:ManagedElement/e:Equipment/f:AntennaUnitGroup/f:AntennaNearUnit/f:selfTestStatus
                        |/a:ManagedElement/e:Equipment/f:AntennaUnitGroup/f:AntennaNearUnit/f:administrativeState
                        |/a:ManagedElement/e:Equipment/f:AntennaUnitGroup/f:AntennaNearUnit/f:RetSubUnit/f:calibrationStatus
                        |/a:ManagedElement/e:Equipment/f:AntennaUnitGroup/f:AntennaNearUnit/f:RetSubUnit/f:operationalState">
        <xsl:call-template name="defaultAttrHandler">
            <xsl:with-param name="useNamespace" select="$reqantadpt"/>
            <xsl:with-param name="convert" select="true()"/>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:QciProfilePredefined/b:logicalChannelGroupRef
                        |/a:ManagedElement/b:ENodeBFunction/b:QciTable/b:QciProfileOperatorDefined/b:logicalChannelGroupRef
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:sectorCarrierRef
                        |/a:ManagedElement/b:ENodeBFunction/b:SectorCarrier/b:sectorFunctionRef">
        <xsl:call-template name="refAttrHandler">
            <xsl:with-param name="type" select="'leafref'"/>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:EUtranFreqRelation/b:eutranFreqToQciProfileRelation/b:qciProfileRef
                        |/a:ManagedElement/b:ENodeBFunction/b:EUtranCellFDD/b:EUtranFreqRelation/b:EUtranCellRelation/b:neighborCellRef">
        <xsl:call-template name="refAttrHandler">
            <xsl:with-param name="type" select="'leaf'"/>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="/a:ManagedElement/b:ENodeBFunction/b:SectorCarrier/b:rfBranchRxRef
                        |/a:ManagedElement/b:ENodeBFunction/b:SectorCarrier/b:rfBranchTxRef">
        <xsl:call-template name="refAttrHandler">
            <xsl:with-param name="type" select="'list'"/>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="*"/>
    <xsl:template match="/a:ManagedElement/c:NodeSupport/d:SectorEquipmentFunction/d:rfBranchRef">
        <xsl:element name="{mf:yangString(local-name(), false())}" namespace="{$resf}">
            <xsl:namespace name="{'reqeqadpt'}" select="$reqeqadpt"/>
            <xsl:namespace name="{'reqantadpt'}" select="$reqantadpt"/>
            <xsl:for-each select="tokenize(.,',')">
                <xsl:value-of select="mf:fdnToXpath(., 'reqantadpt')"/>
            </xsl:for-each>
        </xsl:element>
    </xsl:template>

    <xsl:template name="changeToDefaultId">
        <xsl:param name="useNamespace" select="$lrat"/>
        <xsl:element name="id" namespace="{$useNamespace}">
            <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>

    <xsl:template name="MOHandler">
        <xsl:param name="namespace" select="$lrat"/>
        <xsl:param name="withId" select="false()"/>
        <xsl:variable name="nodeName" select="local-name()"/>
        <xsl:variable name="nodeString" select="mf:yangString($nodeName, true())"/>
        <xsl:element name="{$nodeString}" namespace="{$namespace}">
            <xsl:choose>
                <xsl:when test="$withId">
                    <xsl:variable name="id" select= "mf:getIdName($nodeName)"/>
                    <xsl:apply-templates select="*[lower-case(local-name())= $id], *[lower-case(local-name())!= $id]"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:element>
    </xsl:template>
    <xsl:template name="leafListHandler">
        <xsl:param name="namespace" select="$lrat"/>
        <xsl:param name="leaflistName"/>
        <xsl:param name="leaflistConvert" select="false()"/>
        <xsl:for-each select="distinct-values(./*[local-name() = $leaflistName]/text())">
            <xsl:call-template name="defaultAttrHandler">
                <xsl:with-param name="useNamespace" select="$namespace"/>
                <xsl:with-param name="convert" select="$leaflistConvert"/>
                <xsl:with-param name="value" select="mf:yangString($leaflistName, false())"/>
            </xsl:call-template>
        </xsl:for-each>
    </xsl:template>
    <xsl:template name="leafListToListHandler">
        <xsl:param name="namespace" select="$lrat"/>
        <xsl:param name="leaflistName" select="local-name()"/>
        <xsl:param name="leaflistConvert" select="false()"/>
        <xsl:element name="{mf:yangString($leaflistName, false())}" namespace="{$namespace}">
            <xsl:if test="not(./@unset)">
                <xsl:element name="{'index'}" namespace="{$namespace}"><xsl:value-of select="count(preceding-sibling::*[local-name()=$leaflistName])"/></xsl:element>
                <xsl:call-template name="defaultAttrHandler">
                    <xsl:with-param name="useNamespace" select="$namespace"/>
                    <xsl:with-param name="convert" select="$leaflistConvert"/>
                    <xsl:with-param name="value" select="'value'"/>
                </xsl:call-template>
            </xsl:if>
        </xsl:element>
    </xsl:template>
    <xsl:template name="defaultAttrHandler">
        <xsl:param name="useNamespace" select="$lrat"/>
        <xsl:param name="value" select="local-name()"/>
        <xsl:param name="convert" select="false()"/>
        <xsl:variable name="nodeString" select="mf:yangString($value, false())"/>
        <xsl:element name="{$nodeString}" namespace="{$useNamespace}">
            <xsl:choose>
                <xsl:when test="$convert">
                    <xsl:value-of select="mf:valueConvert(.)"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:element>
    </xsl:template>

    <xsl:template name="refAttrHandler">
        <xsl:param name="namespace" select="$lrat"/>
        <xsl:param name="prefix" select="'lrtadpt'"/>
        <xsl:param name="type"/>
        <xsl:variable name="yangString" select="mf:yangString(local-name(), false())"/>
        <xsl:element name="{$yangString}" namespace="{$namespace}">
            <xsl:choose>
                <xsl:when test="$type='leafref'">
                    <xsl:call-template name="getValueOfLastIdFromFdn"/>
                </xsl:when>
                <xsl:when test="$type='leaf'">
                    <xsl:namespace name="{$prefix}" select="$namespace"/>
                    <xsl:for-each select="tokenize(.,',')">
                        <xsl:value-of select="mf:fdnToXpath(., $prefix)"/>
                    </xsl:for-each>
                </xsl:when>
                <xsl:when test="'list'">
                    <xsl:if test="not(./@unset)">
                        <xsl:call-template name="refToListHandler">
                            <xsl:with-param name="level" select="2"/>
                            <xsl:with-param name="namespace" select="$namespace"/>
                        </xsl:call-template>
                    </xsl:if>
                </xsl:when>
            </xsl:choose>
        </xsl:element>
    </xsl:template>

    <xsl:template name="refToListHandler">
        <xsl:param name="namespace"/>
        <xsl:param name="level"/>
        <xsl:param name="in" select="reverse(tokenize(.,','))"/>
        <xsl:variable name="trimRefFromLocalName" select="substring-before(local-name(), 'Ref')"/>
        <xsl:for-each select="1 to $level">
            <xsl:variable name="position" select="position()"/>
            <xsl:variable name="inString" select="$in[$position]"/>
            <xsl:variable name="refNameInEcim" select="concat($trimRefFromLocalName, substring-before($inString, '='), 'Ref')"/>
            <xsl:element name="{mf:yangString($refNameInEcim, false())}" namespace="{$namespace}">
                <xsl:value-of select="substring-after($inString, '=')"/>
            </xsl:element>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="getValueOfLastIdFromFdn">
        <xsl:value-of select="substring-after(tokenize(.,',')[last()],'=')"/>
    </xsl:template>

    <xsl:function name="mf:fdnToXpath" as="xs:string">
        <xsl:param name="in" as="xs:string"/>
        <xsl:param name="prefix" as="xs:string"/>
        <xsl:variable name="apos">&apos;</xsl:variable>
        <xsl:variable name="name" select="substring-before($in , '=')"/>
        <xsl:variable name="value" select="substring-after($in , '=')"/>
        <xsl:variable name="yangName" select="mf:yangString($name, true())"/>
        <xsl:choose>
            <xsl:when test="$name='ManagedElement'"><xsl:value-of select="''"/></xsl:when>
            <xsl:when test="$name='ENodeBFunction' or $name='QciTable' or $name='EUtraNetwork'">
                <xsl:value-of select="concat('/',$prefix,':',$yangName)"/>
            </xsl:when>
            <xsl:when test="$name='Equipment'">
                <xsl:value-of select="concat('/','reqeqadpt',':',$yangName)"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="concat('/',$prefix,':',$yangName,'[',$prefix,':','id=',$apos,$value,$apos,']')"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <xsl:function name="mf:valueConvert" as="xs:string">
        <xsl:param name="value" as="xs:string"/>
        <xsl:value-of select="translate($value, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ_', 'abcdefghijklmnopqrstuvwxyz-')"/>
    </xsl:function>

    <xsl:function name="mf:getIdName" as="xs:string" >
        <xsl:param name="moName" as="xs:string"/>
        <xsl:value-of select="concat(lower-case($moName), 'id')"/>
    </xsl:function>

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