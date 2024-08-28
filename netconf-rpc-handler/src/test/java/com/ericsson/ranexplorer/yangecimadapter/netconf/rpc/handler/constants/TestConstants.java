/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.constants;

import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener.AdapterListenerTest;

public class TestConstants {

    private static final String DATA_START_TAG = "<data>";
    private static final String DATA_END_TAG = "</data>";
    private static final String RPC_REPLY_END_TAG = "</rpc-reply>";
    private static final String NETCONF_STATE_END_TAG = "</netconf-state>";
    public static final String LOG_ERROR_MSG = "Unexpected exception occurred in this test:[{}] with error [{}]";
    private static final String RPC_REPLY_TAG_WITH_MESSAGE_ID = "<rpc-reply message-id=\"" + AdapterListenerTest.MESSAGE_ID + "\" ";
    private static final String NETCONF_BASE_NAMESPACE = "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"";
    private static final String MANAGED_ELEMENT_START_TAG = "<ManagedElement xmlns=\"urn:com:ericsson:ecim:ComTop\">";
    private static final String MANAGED_ELEMENT_END_TAG = "</ManagedElement>";

    public static final String EXPECTED_HELLO_RESULT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<hello xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
            "<capabilities><capability>urn:ietf:params:netconf:base:1.0</capability>" +
            "<capability>urn:ietf:params:netconf:base:1.1</capability>" +
            "<capability>urn:ietf:params:netconf:capability:writable-running:1.0</capability>" +
            "<capability>urn:ietf:params:netconf:capability:validate:1.0</capability>" +
            "<capability>urn:ietf:params:netconf:capability:rollback-on-error:1.0</capability>" +
            "<capability>urn:ietf:params:xml:ns:yang:ietf-netconf-monitoring?module=ietf-netconf-monitoring&amp;revision=2010-10-04</capability>" +
            "<capability>urn:rdns:com:ericsson:oammodel:ericsson-yang-extensions?module=ericsson-yang-extensions&amp;revision=2018-03-12</capability>" +
            "<capability>urn:rdns:com:ericsson:oammodel:ericsson-lrat-enb-adapter?module=ericsson-lrat-enb-adapter&amp;revision=2018-02-21</capability></capabilities>" +
            "<session-id>1</session-id>" +
            "</hello>]]>]]>";

    public static final String EXPECTED_CAPABILITIES =
            "<capabilities><capability>urn:ietf:params:netconf:base:1.0</capability>" +
                    "<capability>urn:ietf:params:netconf:base:1.1</capability>" +
                    "<capability>urn:ietf:params:netconf:capability:writable-running:1.0</capability>" +
                    "<capability>urn:ietf:params:netconf:capability:validate:1.0</capability>" +
                    "<capability>urn:ietf:params:netconf:capability:rollback-on-error:1.0</capability>" +
                    "<capability>urn:ietf:params:xml:ns:yang:ietf-netconf-monitoring?module=ietf-netconf-monitoring&amp;revision=2010-10-04</capability>" +
                    "<capability>urn:rdns:com:ericsson:oammodel:ericsson-yang-extensions?module=ericsson-yang-extensions&amp;revision=2018-03-12</capability>" +
                    "<capability>urn:rdns:com:ericsson:oammodel:ericsson-lrat-enb-adapter?module=ericsson-lrat-enb-adapter&amp;revision=2018-02-21</capability>" +
                    "</capabilities>";

    public static final String EXPECTED_DATASTORES =
            "<datastores><datastore><name>running</name></datastore></datastores>";

    public static final String EXPECTED_SCHEMA =
            "<schemas><schema><identifier>ericsson-adapter-yang-extensions</identifier><version>2018-03-12</version><format>yang</format>" +
                    "<namespace>urn:rdns:com:ericsson:oammodel:ericsson-adapter-yang-extensions</namespace><location>NETCONF</location>" +
                    "</schema><schema><identifier>ericsson-lrat-enb-adapter</identifier><version>2018-02-21</version><format>yang</format>" +
                    "<namespace>urn:rdns:com:ericsson:oammodel:ericsson-lrat-enb-adapter</namespace><location>NETCONF</location></schema><schema>" +
                    "<identifier>ietf-netconf</identifier><version>2011-06-01</version><format>yang</format>" +
                    "<namespace>urn:ietf:params:xml:ns:netconf:base:1.0</namespace><location>NETCONF</location></schema>" +
                    "<schema><identifier>ietf-netconf-monitoring</identifier><version>2010-10-04</version><format>yang</format>" +
                    "<namespace>urn:ietf:params:xml:ns:yang:ietf-netconf-monitoring</namespace><location>NETCONF</location></schema>" +
                    "</schemas>";

    public static final String EXPECTED_EVENT_STREAMS =
            "<netconf xmlns=\"urn:ietf:params:xml:ns:netmod:notification\">" +
                    "<streams><stream>" +
                    "<name>NETCONF</name>" +
                    "<description>default NETCONF event stream</description>" +
                    "<replaySupport>false</replaySupport>" +
                    "</stream></streams>" +
                    "</netconf>";

    public static final String EXPECTED_ENODEB_FUNCTION_REPLY = "<enodeb-function xmlns=\"urn:rdns:com:ericsson:oammodel:ericsson-lrat-enb-adapter\"><enb-id>result</enb-id></enodeb-function>";
    public static final String EXPECTED_NULL_FILTER_RESULT =  RPC_REPLY_TAG_WITH_MESSAGE_ID + NETCONF_BASE_NAMESPACE +">" +
            DATA_START_TAG +
            EXPECTED_ENODEB_FUNCTION_REPLY +
            "<netconf-state xmlns=\"urn:ietf:params:xml:ns:yang:ietf-netconf-monitoring\">" +
            EXPECTED_CAPABILITIES + EXPECTED_DATASTORES + EXPECTED_SCHEMA +
            NETCONF_STATE_END_TAG + EXPECTED_EVENT_STREAMS + DATA_END_TAG + RPC_REPLY_END_TAG;

    public static final String EXPECTED_NULL_FILTER_CONFIG_DATA_RESULT =  RPC_REPLY_TAG_WITH_MESSAGE_ID + NETCONF_BASE_NAMESPACE +">" +
            DATA_START_TAG + EXPECTED_ENODEB_FUNCTION_REPLY + DATA_END_TAG + RPC_REPLY_END_TAG;

    public static final String EXPECTED_ENODEBFUNCTION_FILTER_RESULT =  RPC_REPLY_TAG_WITH_MESSAGE_ID + NETCONF_BASE_NAMESPACE +">" +
            DATA_START_TAG + EXPECTED_ENODEB_FUNCTION_REPLY + DATA_END_TAG + RPC_REPLY_END_TAG;

    public static final String EXPECTED_ECIM_EQUIPMENT_RESULT = MANAGED_ELEMENT_START_TAG +
            "\t<managedElementId>1</managedElementId>\n" +
            "\t<Equipment xmlns=\"urn:com:ericsson:ecim:ReqEquipment\">\n" +
            "\t\t<equipmentId>1</equipmentId>\n" +
            "\t\t<AntennaUnitGroup xmlns=\"urn:com:ericsson:ecim:ReqAntennaSystem\">\n" +
            "\t\t\t<antennaUnitGroupId>1</antennaUnitGroupId>\n" +
            "\t\t\t<AntennaNearUnit>\n" +
            "\t\t\t\t<antennaNearUnitId>2</antennaNearUnitId>\n" +
            "\t\t\t\t<RetSubUnit>\n" +
            "\t\t\t\t\t<retSubUnitId>3</retSubUnitId>\n" +
            "\t\t\t\t\t<calibrationStatus>UNKNOWN</calibrationStatus>\n" +
            "\t\t\t\t</RetSubUnit>\n" +
            "\t\t\t</AntennaNearUnit>\n" +
            "\t\t\t<RfBranch/>\n" +
            "\t\t</AntennaUnitGroup>\n" +
            "\t</Equipment>\n" +
            MANAGED_ELEMENT_END_TAG;

    private static final String EXPECTED_YANG_EQUIPMENT_RESULT =
            "<equipment xmlns=\"urn:rdns:com:ericsson:oammodel:ericsson-req-equip-enb-adapter\">\n" +
                    "   <antenna-unit-group xmlns=\"urn:rdns:com:ericsson:oammodel:ericsson-req-antenna-enb-adapter\">\n" +
                    "      <id>1</id>\n" +
                    "      <antenna-near-unit>\n" +
                    "           <id>2</id>\n" +
                    "           <ret-sub-unit>\n" +
                    "                <id>3</id>\n" +
                    "                <calibration-status>unknown</calibration-status>\n"+
                    "           </ret-sub-unit>\n" +
                    "      </antenna-near-unit>\n"+
                    "   </antenna-unit-group>\n" +
                    "</equipment>";

    public static final String EXPECTED_EMPTY_EQUIPMENT_FILTER_RESULT = RPC_REPLY_TAG_WITH_MESSAGE_ID + NETCONF_BASE_NAMESPACE + ">" +
            DATA_START_TAG + EXPECTED_YANG_EQUIPMENT_RESULT + DATA_END_TAG + RPC_REPLY_END_TAG;

    public static final String EXPECTED_EMPTY_FILTER_RESULT = RPC_REPLY_TAG_WITH_MESSAGE_ID + NETCONF_BASE_NAMESPACE +">" +
            "<data></data></rpc-reply>";

    public static final String VALID_FILTER =  "<enodeb-function>\n" +
            "<enb-id>1</enb-id>\n" +
            "<enodeb-plmn-id>\n" +
            "<mcc/>\n" +
            "</enodeb-plmn-id>\n" +
            "</enodeb-function>";

    public static final String EXPECTED_VALID_FILTER_RESULT = RPC_REPLY_TAG_WITH_MESSAGE_ID + NETCONF_BASE_NAMESPACE +">" +
            "<data><enodeb-function xmlns=\"urn:rdns:com:ericsson:oammodel:ericsson-lrat-enb-adapter\">\n" +
            "  <enb-id>1</enb-id>\n" +
            "  <enodeb-plmn-id>\n" +
            "    <mcc>111</mcc>\n" +
            "  </enodeb-plmn-id>\n" +
            "</enodeb-function></data></rpc-reply>";

    public static final String INVALID_FILTER = MANAGED_ELEMENT_START_TAG;

    public static final String RPC_ERROR = "<rpc-error>";

    public static final String VALID_FILTER_INVALID_DATA = "<enodeb-function>\n" +
            "                <qci-table>\n" +
            "                    <qci-profile-predefined>\n" +
            "                        <id>unexpectedValue</id>\n" +
            "                        <qci-subscription-quanta/>\n" +
            "                    </qci-profile-predefined>\n" +
            "                </qci-table>\n" +
            "            </enodeb-function>";

    public static final String EXPECTED_INVALID_DATA_RESULT = RPC_REPLY_TAG_WITH_MESSAGE_ID + NETCONF_BASE_NAMESPACE +">" +
            "<rpc-error><error-type>application</error-type><error-tag>data-missing</error-tag><error-severity>error"+
            "</error-severity><error-message xml:lang=\"en\">Invalid value for qciProfilePredefinedId</error-message>"+
            "</rpc-error></rpc-reply>";

    public static final String ECIM_NULL_FILTER_RESULT = MANAGED_ELEMENT_START_TAG +
            "    <managedElementId>LTE01dg2ERBS00001</managedElementId>\n" +
            "    <ENodeBFunction xmlns=\"urn:com:ericsson:ecim:Lrat\" unexpectedAttr=\"value1\">\n" +
            "        <eNodeBFunctionId>1</eNodeBFunctionId>\n" +
            "        <emptyElement/>\n"+
            "        <eNBId>result</eNBId>\n" +
            "    </ENodeBFunction>\n" +
            MANAGED_ELEMENT_END_TAG;

    public static final String ECIM_RESULT_WITH_MCC = MANAGED_ELEMENT_START_TAG +
            "    <managedElementId>LTE01dg2ERBS00001</managedElementId>\n" +
            "    <ENodeBFunction xmlns=\"urn:com:ericsson:ecim:Lrat\" unexpectedAttr=\"value1\">\n" +
            "        <eNodeBFunctionId>1</eNodeBFunctionId>\n" +
            "        <eNBId>1</eNBId>\n" +
            "        <eNodeBPlmnId struct=\"PlmnIdentity\">\n" +
            "            <mcc>111</mcc>\n" +
            "        </eNodeBPlmnId>\n" +
            "    </ENodeBFunction>\n" +
            MANAGED_ELEMENT_END_TAG;

    public static final String FILTER_WITH_EMPTY_NETCONFSTATE =
            "<netconf-state xmlns=\"urn:ietf:params:xml:ns:yang:ietf-netconf-monitoring\"/>";

    public static final String FILTER_WITH_NETCONFSTATE_CAPABILITIES =
            "<netconf-state xmlns=\"urn:ietf:params:xml:ns:yang:ietf-netconf-monitoring\">\n" +
                    "        <capabilities/>\n" +
                    NETCONF_STATE_END_TAG;

    public static final String EMPTY_ENODEB_FUNCTION_FILTER = "<enodeb-function/>";

    public static final String FILTER_WITH_EVENT_STREAMS = "<netconf xmlns=\"urn:ietf:params:xml:ns:netmod:notification\"><streams/></netconf>";

    public static final String FILTER_WITH_NETCONFSTATE_CAPABILITIES_AND_ENODEBFUNCTION = FILTER_WITH_NETCONFSTATE_CAPABILITIES + EMPTY_ENODEB_FUNCTION_FILTER;

    public static final String FILTER_WITH_EVENT_STREAMS_AND_ENODEBFUNCTION = FILTER_WITH_EVENT_STREAMS + EMPTY_ENODEB_FUNCTION_FILTER;

    public static final String FILTER_WITH_EVENT_STREAMS_NETCONF_STATE_AND_ENODEBFUNCTION = FILTER_WITH_EVENT_STREAMS + FILTER_WITH_EMPTY_NETCONFSTATE + EMPTY_ENODEB_FUNCTION_FILTER;

    public static final String EMPTY_ENODEB_FUNCTION_RESULT = "<enodeb-function xmlns=\"urn:rdns:com:ericsson:oammodel:ericsson-lrat-enb-adapter\"><enb-id>result</enb-id></enodeb-function>";

    public static final String EXPECTED_CAPABILITIRS_EMPTY_ENODEBFUNCTION_RESULT = RPC_REPLY_TAG_WITH_MESSAGE_ID + NETCONF_BASE_NAMESPACE + ">" +
            DATA_START_TAG +EMPTY_ENODEB_FUNCTION_RESULT +
            "<netconf-state xmlns=\"urn:ietf:params:xml:ns:yang:ietf-netconf-monitoring\">" +
            EXPECTED_CAPABILITIES + NETCONF_STATE_END_TAG +
            DATA_END_TAG + RPC_REPLY_END_TAG;

    public static final String EXPECTED_NETCONF_STATE_REPLY = RPC_REPLY_TAG_WITH_MESSAGE_ID + NETCONF_BASE_NAMESPACE + ">" +
            DATA_START_TAG +
            "<netconf-state xmlns=\"urn:ietf:params:xml:ns:yang:ietf-netconf-monitoring\">%s</netconf-state>" +
            DATA_END_TAG + RPC_REPLY_END_TAG;

    public static final String EXPECTED_EVENT_STREAMS_RESULT = RPC_REPLY_TAG_WITH_MESSAGE_ID + NETCONF_BASE_NAMESPACE + ">" +
            DATA_START_TAG +
            EXPECTED_EVENT_STREAMS +
            DATA_END_TAG + RPC_REPLY_END_TAG;

    public static final String EXPECTED_EVENT_STREAMS_NETCONF_STATE_EMPTY_ENODEBFUNCTION_RESULT = RPC_REPLY_TAG_WITH_MESSAGE_ID + NETCONF_BASE_NAMESPACE + ">" +
            DATA_START_TAG + EMPTY_ENODEB_FUNCTION_RESULT +
            EXPECTED_EVENT_STREAMS +
            "<netconf-state xmlns=\"urn:ietf:params:xml:ns:yang:ietf-netconf-monitoring\">%s</netconf-state>" +
            DATA_END_TAG + RPC_REPLY_END_TAG;

    public static final String EXPECTED_EVENT_STREAMS_EMPTY_ENODEBFUNCTION_RESULT = RPC_REPLY_TAG_WITH_MESSAGE_ID + NETCONF_BASE_NAMESPACE + ">" +
            DATA_START_TAG + EMPTY_ENODEB_FUNCTION_RESULT +
            EXPECTED_EVENT_STREAMS +
            DATA_END_TAG + RPC_REPLY_END_TAG;

    private TestConstants(){

    }
}
