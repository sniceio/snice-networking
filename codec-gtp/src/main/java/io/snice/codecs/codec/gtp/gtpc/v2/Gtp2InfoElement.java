package io.snice.codecs.codec.gtp.gtpc.v2;

/**
 * This file has been auto generated. Do not manually edit.
 * Please see the readme file in the codegen directory
 * for how to update and generate this file.
 *
 * @author jonas@jonasborjesson.com
 */
public enum Gtp2InfoElement {

    RESERVED(0, "Reserved", false, -1),
    IMSI(1, "International Mobile Subscriber Identity (IMSI)", false, -1),
    CAUSE(2, "Cause", false, -1),
    RECOVERY(3, "Recovery (Restart Counter)", false, -1),
    STN_SR(51, "STN-SR", false, -1),
    APN(71, "Access Point Name (APN)", false, -1),
    AMBR(72, "Aggregate Maximum Bit Rate (AMBR)", false, 8),
    EBI(73, "EPS Bearer ID (EBI)", true, 1),
    IP_ADDRESS(74, "IP Address", false, -1),
    MEI(75, "Mobile Equipment Identity (MEI)", false, -1),
    MSISDN(76, "MSISDN", false, -1),
    INDICATION(77, "Indication", true, 2),
    PCO(78, "Protocol Configuration Options (PCO)", false, -1),
    PAA(79, "PDN Address Allocation (PAA)", false, -1),
    BEARER_QOS(80, "Bearer LevelQuality of Service (Bearer QoS)", true, 22),
    FLOW_QOS(81, "FlowQuality of Service (Flow QoS)", true, 21),
    RAT_TYPE(82, "RAT Type", true, 1),
    SERVING_NETWORK(83, "Serving Network", true, 3),
    BEARER_TFT(84, "EPS Bearer LevelTraffic Flow Template (Bearer TFT)", false, -1),
    TAD(85, "Traffic Aggregation Description (TAD)", false, -1),
    ULI(86, "User Location Information (ULI)", true, -1),
    F_TEID(87, "Fully Qualified Tunnel Endpoint Identifier (F-TEID)", true, -1),
    TMSI(88, "TMSI", false, -1),
    GLOBAL_CN_ID(89, "Global CN-Id", false, -1),
    S103PDF(90, "S103 PDN Data Forwarding Info (S103PDF)", false, -1),
    S1UDF(91, "S1-U Data Forwarding Info (S1UDF)", false, -1),
    DELAY_VALUE(92, "Delay Value", true, 1),
    BEARER_CONTEXT(93, "Bearer Context", true, -1),
    CHARGING_ID(94, "Charging ID", true, 4),
    CHARGING_CHARACTERISTICS(95, "Charging Characteristics", true, 2),
    TRACE_INFORMATION(96, "Trace Information", false, -1),
    BEARER_FLAGS(97, "Bearer Flags", true, 1),
    PDN_TYPE(99, "PDN Type", true, 1),
    PROCEDURE_TRANSACTION_ID(100, "Procedure Transaction ID", true, 1),
    MM_CONTEXT_GSM_KEY_TRIPLETS(103, "MM Context (GSM Key and Triplets)", true, -1),
    MM_CONTEXT_UMTS_KEY_USED_CIPHER_QUINTUPLETS(104, "MM Context (UMTS Key, Used Cipher and Quintuplets)", true, -1),
    MM_CONTEXT_GSM_KEY_USED_CIPHER_QUINTUPLETS(105, "MM Context (GSM Key,Used Cipher and Quintuplets)", true, -1),
    MM_CONTEXT_UMTS_KEY_QUINTUPLETS(106, "MM Context (UMTS Key and Quintuplets)", true, -1),
    MM_CONTEXT_EPS_SECURITY_CONTEXT_QUADRUPLETS_QUINTUPLETS(107, "MM Context (EPS Security Context,Quadruplets and Quintuplets)", true, -1),
    MM_CONTEXT_UMTS_KEY_QUADRUPLETS_QUINTUPLETS(108, "MM Context (UMTS Key, Quadruplets and Quintuplets)", true, -1),
    PDN_CONNECTION(109, "PDN Connection", true, -1),
    PDU_NUMBERS(110, "PDU Numbers", true, 9),
    P_TMSI(111, "P-TMSI", false, -1),
    P_TMSI_SIGNATURE(112, "P-TMSI Signature", false, -1),
    HOP_COUNTER(113, "Hop Counter", true, 1),
    UE_TIME_ZONE(114, "UE Time Zone", true, 2),
    TRACE_REFERENCE(115, "Trace Reference", false, 6),
    COMPLETE_REQUEST_MESSAGE(116, "Complete Request Message", false, -1),
    GUTI(117, "GUTI", false, -1),
    F_CONTAINER(118, "F-Container", false, -1),
    F_CAUSE(119, "F-Cause", false, -1),
    PLMN_ID(120, "PLMN ID", false, -1),
    TARGET_IDENTIFICATION(121, "Target Identification", false, -1),
    PACKET_FLOW_ID(123, "Packet Flow ID", false, -1),
    RAB_CONTEXT(124, "RAB Context", false, 9),
    SOURCE_RNC_PDCP_CONTEXT_INFO(125, "Source RNC PDCP Context Info", false, -1),
    PORT_NUMBER(126, "Port Number", true, 2),
    APN_RESTRICTION(127, "APN Restriction", true, 1),
    SELECTION_MODE(128, "Selection Mode", true, 1),
    SOURCE_IDENTIFICATION(129, "Source Identification", false, -1),
    CHANGE_REPORTING_ACTION(131, "Change Reporting Action", false, -1),
    FQ_CSID(132, "Fully Qualified PDN Connection Set Identifier (FQ-CSID)", true, -1),
    CHANNEL_NEEDED(133, "Channel needed", false, -1),
    EMLPP_PRIORITY(134, "eMLPP Priority", false, -1),
    NODE_TYPE(135, "Node Type", true, 1),
    FQDN(136, "Fully Qualified Domain Name (FQDN)", false, -1),
    TI(137, "Transaction Identifier (TI)", false, -1),
    MBMS_SESSION_DURATION(138, "MBMS Session Duration", true, 3),
    MBMS_SERVICE_AREA(139, "MBMS Service Area", false, -1),
    MBMS_SESSION_IDENTIFIER(140, "MBMS Session Identifier", true, 1),
    MBMS_FLOW_IDENTIFIER(141, "MBMS Flow Identifier", true, 2),
    MBMS_IP_MULTICAST_DISTRIBUTION(142, "MBMS IP Multicast Distribution", true, -1),
    MBMS_DISTRIBUTION_ACKNOWLEDGE(143, "MBMS Distribution Acknowledge", true, 1),
    RFSP_INDEX(144, "RFSP Index", false, 2),
    UCI(145, "User CSG Information (UCI)", true, 8),
    CSG_INFORMATION_REPORTING_ACTION(146, "CSG Information Reporting Action", true, 1),
    CSG_ID(147, "CSG ID", true, 4),
    CMI(148, "CSG Membership Indication (CMI)", true, 1),
    SERVICE_INDICATOR(149, "Service indicator", false, 1),
    DETACH_TYPE(150, "Detach Type", false, 1),
    LDN(151, "Local Distiguished Name (LDN)", false, -1),
    NODE_FEATURES(152, "Node Features", true, 1),
    MBMS_TIME_TO_DATA_TRANSFER(153, "MBMS Time to Data Transfer", true, 1),
    THROTTLING(154, "Throttling", true, 2),
    ARP(155, "Allocation/Retention Priority (ARP)", true, 1),
    EPC_TIMER(156, "EPC Timer", true, 1),
    SIGNALLING_PRIORITY_INDICATION(157, "Signalling Priority Indication", true, 1),
    TMGI(158, "Temporary Mobile Group Identity (TMGI)", true, 6),
    ADDITIONAL_MM_CONTEXT_FOR_SRVCC(159, "Additional MM context for SRVCC", true, -1),
    ADDITIONAL_FLAGS_FOR_SRVCC(160, "Additional flags for SRVCC", true, 1),
    MDT_CONFIGURATION(162, "MDT Configuration", true, -1),
    APCO(163, "Additional Protocol Configuration Options (APCO)", true, -1),
    ABSOLUTE_TIME_OF_MBMS_DATA_TRANSFER(164, "Absolute Time of MBMS Data Transfer", true, 8),
    E(165, "H(e)NB Information Reporting", true, 1),
    IP4CP(166, "IPv4 Configuration Parameters (IP4CP)", true, 5),
    CHANGE_TO_REPORT_FLAGS(167, "Change to Report Flags", true, 1),
    ACTION_INDICATION(168, "Action Indication", true, 1),
    TWAN_IDENTIFIER(169, "TWAN Identifier", true, -1),
    ULI_TIMESTAMP(170, "ULI Timestamp", true, 4),
    MBMS_FLAGS(171, "MBMS Flags", true, 1),
    RAN_NAS_CAUSE(172, "RAN/NAS Cause", true, -1),
    CN_OPERATOR_SELECTION_ENTITY(173, "CN Operator Selection Entity", true, 1),
    TRUSTED_WLAN_MODE_INDICATION(174, "Trusted WLAN Mode Indication", true, 1),
    NODE_NUMBER(175, "Node Number", true, -1),
    NODE_IDENTIFIER(176, "Node Identifier", true, -1),
    PRESENCE_REPORTING_AREA_ACTION(177, "Presence Reporting Area Action", true, -1),
    PRESENCE_REPORTING_AREA_INFORMATION(178, "Presence Reporting Area Information", true, 4),
    TWAN_IDENTIFIER_TIMESTAMP(179, "TWAN Identifier Timestamp", true, 4),
    OVERLOAD_CONTROL_INFORMATION(180, "Overload Control Information", true, -1),
    LOAD_CONTROL_INFORMATION(181, "Load Control Information", true, -1),
    METRIC(182, "Metric", false, 1),
    SEQUENCE_NUMBER(183, "Sequence Number", false, 4),
    APN_AND_RELATIVE_CAPACITY(184, "APN and Relative Capacity", true, -1),
    WLAN_OFFLOADABILITY_INDICATION(185, "WLAN Offloadability Indication", true, 1),
    PAGING_AND_SERVICE_INFORMATION(186, "Paging and Service Information", true, -1),
    INTEGER_NUMBER(187, "Integer Number", false, -1),
    MILLISECOND_TIME_STAMP(188, "Millisecond Time Stamp", true, 6),
    MONITORING_EVENT_INFORMATION(189, "Monitoring Event Information", true, -1),
    ECGI_LIST(190, "ECGI List", true, -1),
    REMOTE_UE_CONTEXT(191, "Remote UE Context", true, -1),
    REMOTE_USER_ID(192, "Remote User ID", true, -1),
    REMOTE_UE_IP_INFORMATION(193, "Remote UE IP information", false, -1),
    CIOT_OPTIMIZATIONS_SUPPORT_INDICATION(194, "CIoT Optimizations Support Indication", true, 1),
    SCEF_PDN_CONNECTION(195, "SCEF PDN Connection", true, -1),
    HEADER_COMPRESSION_CONFIGURATION(196, "Header Compression Configuration", true, 4),
    EPCO(197, "Extended Protocol Configuration Options (ePCO)", false, -1),
    SERVING_PLMN_RATE_CONTROL(198, "Serving PLMN Rate Control", true, 4),
    COUNTER(199, "Counter", true, 5),
    MAPPED_UE_USAGE_TYPE(200, "Mapped UE Usage Type", true, 2),
    SECONDARY_RAT_USAGE_DATA_REPORT(201, "Secondary RAT Usage Data Report", true, 27),
    UP_FUNCTION_SELECTION_INDICATION_FLAGS(202, "UP Function Selection Indication Flags", true, 1),
    MAXIMUM_PACKET_LOSS_RATE(203, "Maximum Packet Loss Rate", true, 1),
    APN_RATE_CONTROL_STATUS(204, "APN Rate Control Status", true, 20),
    EXTENDED_TRACE_INFORMATION(205, "Extended Trace Information", true, -1),
    MONITORING_EVENT_EXTENSION_INFORMATION(206, "Monitoring Event Extension Information", true, -1),
    EXTENSION(254, "Special IE type for IE Type Extension", false, -1),
    PRIVATE_EXTENSION(255, "Private Extension", false, -1);

    private final int typeAsDecimal;
    private final byte type;

    private final String friendlyName;

    private final boolean isFixed;
    private final boolean isExtendable;

    Gtp2InfoElement(final int type, final String friendlyName, final boolean isExtendable, final int octets) {
        this.typeAsDecimal = type;
        this.type = (byte) type;
        this.friendlyName = friendlyName;
        this.isFixed = octets > 0;
        this.isExtendable = isExtendable;
    }

    public int getTypeAsDecimal() {
        return typeAsDecimal;
    }

    public byte getType() {
        return type;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public boolean isFixed() {
        return isFixed;
    }

    public boolean isVariable() {
        return !isFixed;
    }

    public boolean isExtendable() {
        return isExtendable;
    }
}
