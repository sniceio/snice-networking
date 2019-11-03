package io.snice.networking.codec.gtp.gtpc.v2;

import io.snice.networking.codec.tgpp.ReferencePoint;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static io.snice.networking.codec.tgpp.ReferencePoint.N26;
import static io.snice.networking.codec.tgpp.ReferencePoint.S10;
import static io.snice.networking.codec.tgpp.ReferencePoint.S11;
import static io.snice.networking.codec.tgpp.ReferencePoint.S2a;
import static io.snice.networking.codec.tgpp.ReferencePoint.S2b;
import static io.snice.networking.codec.tgpp.ReferencePoint.S3;
import static io.snice.networking.codec.tgpp.ReferencePoint.S5;
import static io.snice.networking.codec.tgpp.ReferencePoint.S8;
import static io.snice.networking.codec.tgpp.ReferencePoint.S4;
import static io.snice.networking.codec.tgpp.ReferencePoint.S16;

/**
 * This file has been auto generated. Do not manually edit.
 *
 * @author jonas@jonasborjesson.com
 */
public enum Gtp2MessageType {

    RESERVED(0, false, false, "29.274", ""),
    ECHO_REQUEST(1, true, false, "29.274", ""),
    ECHO_RESPONSE(2, false, true, "29.274", ""),
    VERSION_NOT_SUPPORTED_INDICATION(3, false, true, "29.274", ""),
    CREATE_SESSION_REQUEST(32, true, false, "29.274", ""),
    CREATE_SESSION_RESPONSE(33, false, true, "29.274", ""),
    DELETE_SESSION_REQUEST(36, true, false, "29.274", ""),
    DELETE_SESSION_RESPONSE(37, false, true, "29.274", ""),
    MODIFY_BEARER_REQUEST(34, true, false, "29.274", ""),
    MODIFY_BEARER_RESPONSE(35, false, true, "29.274", ""),
    REMOTE_UE_REPORT_NOTIFICATION(40, true, false, "29.274", ""),
    REMOTE_UE_REPORT_ACKNOWLEDGE(41, false, true, "29.274", ""),
    CHANGE_NOTIFICATION_REQUEST(38, true, false, "29.274", ""),
    CHANGE_NOTIFICATION_RESPONSE(39, false, true, "29.274", ""),
    RESUME_NOTIFICATION(164, true, false, "29.274", ""),
    RESUME_ACKNOWLEDGE(165, false, true, "29.274", ""),
    MODIFY_BEARER_COMMAND(64, true, false, "29.274", "" ,S11,S4,S5,S8,S2a,S2b),
    MODIFY_BEARER_FAILURE_INDICATION(65, false, true, "29.274", "" ,S11,S4,S5,S8,S2a,S2b),
    DELETE_BEARER_COMMAND(66, true, false, "29.274", "" ,S11,S4,S5,S8),
    DELETE_BEARER_FAILURE_INDICATION(67, false, true, "29.274", "" ,S11,S4,S5,S8),
    BEARER_RESOURCE_COMMAND(68, true, false, "29.274", "" ,S11,S4,S5,S8,S2a,S2b),
    BEARER_RESOURCE_FAILURE_INDICATION(69, false, true, "29.274", "" ,S11,S4,S5,S8,S2a,S2b),
    DOWNLINK_DATA_NOTIFICATION_FAILURE_INDICATION(70, true, false, "29.274", "" ,S11,S4),
    TRACE_SESSION_ACTIVATION(71, true, false, "29.274", "" ,S11,S4,S5,S8,S2a,S2b),
    TRACE_SESSION_DEACTIVATION(72, true, false, "29.274", "" ,S11,S4),
    STOP_PAGING_INDICATION(73, true, false, "29.274", "" ,S11,S4),
    CREATE_BEARER_REQUEST(95, true, true, "29.274", ""),
    CREATE_BEARER_RESPONSE(96, false, true, "29.274", ""),
    UPDATE_BEARER_REQUEST(97, true, true, "29.274", ""),
    UPDATE_BEARER_RESPONSE(98, false, true, "29.274", ""),
    DELETE_BEARER_REQUEST(99, true, true, "29.274", ""),
    DELETE_BEARER_RESPONSE(100, false, true, "29.274", ""),
    DELETE_PDN_CONNECTION_SET_REQUEST(101, true, false, "29.274", ""),
    DELETE_PDN_CONNECTION_SET_RESPONSE(102, false, true, "29.274", ""),
    PGW_DOWNLINK_TRIGGERING_NOTIFICATION(103, true, false, "29.274", ""),
    PGW_DOWNLINK_TRIGGERING_ACKNOWLEDGE(104, false, true, "29.274", ""),
    IDENTIFICATION_REQUEST(128, true, false, "29.274", "" ,S3,S10,S16,N26),
    IDENTIFICATION_RESPONSE(129, false, true, "29.274", "" ,S3,S10,S16,N26),
    CONTEXT_REQUEST(130, true, false, "29.274", "" ,S3,S10,S16,N26),
    CONTEXT_RESPONSE(131, false, true, "29.274", "" ,S3,S10,S16,N26),
    CONTEXT_ACKNOWLEDGE(132, false, true, "29.274", "" ,S3,S10,S16,N26),
    FORWARD_RELOCATION_REQUEST(133, true, false, "29.274", "" ,S3,S10,S16,N26),
    FORWARD_RELOCATION_RESPONSE(134, false, true, "29.274", "" ,S3,S10,S16,N26),
    FORWARD_RELOCATION_COMPLETE_NOTIFICATION(135, true, false, "29.274", "" ,S3,S10,S16,N26),
    FORWARD_RELOCATION_COMPLETE_ACKNOWLEDGE(136, false, true, "29.274", "" ,S3,S10,S16,N26),
    FORWARD_ACCESS_CONTEXT_NOTIFICATION(137, true, false, "29.274", "" ,S10,S16),
    FORWARD_ACCESS_CONTEXT_ACKNOWLEDGE(138, false, true, "29.274", "" ,S10,S16),
    RELOCATION_CANCEL_REQUEST(139, true, false, "29.274", "" ,S3,S10,S16,N26),
    RELOCATION_CANCEL_RESPONSE(140, false, true, "29.274", "" ,S3,S10,S16,N26),
    CONFIGURATION_TRANSFER_TUNNEL(141, true, false, "29.274", "" ,S10,N26),
    RAN_INFORMATION_RELAY(152, true, false, "29.274", "" ,S3,S16),
    DETACH_NOTIFICATION(149, true, false, "29.274", ""),
    DETACH_ACKNOWLEDGE(150, false, true, "29.274", ""),
    CS_PAGING_INDICATION(151, true, false, "29.274", ""),
    ALERT_MME_NOTIFICATION(153, true, false, "29.274", ""),
    ALERT_MME_ACKNOWLEDGE(154, false, true, "29.274", ""),
    UE_ACTIVITY_NOTIFICATION(155, true, false, "29.274", ""),
    UE_ACTIVITY_ACKNOWLEDGE(156, false, true, "29.274", ""),
    ISR_STATUS_INDICATION(157, true, false, "29.274", ""),
    UE_REGISTRATION_QUERY_REQUEST(158, true, false, "29.274", ""),
    UE_REGISTRATION_QUERY_RESPONSE(159, false, true, "29.274", ""),
    SUSPEND_NOTIFICATION(162, true, false, "29.274", ""),
    SUSPEND_ACKNOWLEDGE(163, false, true, "29.274", ""),
    CREATE_FORWARDING_TUNNEL_REQUEST(160, true, false, "29.274", ""),
    CREATE_FORWARDING_TUNNEL_RESPONSE(161, false, true, "29.274", ""),
    CREATE_INDIRECT_DATA_FORWARDING_TUNNEL_REQUEST(166, true, false, "29.274", ""),
    CREATE_INDIRECT_DATA_FORWARDING_TUNNEL_RESPONSE(167, false, true, "29.274", ""),
    DELETE_INDIRECT_DATA_FORWARDING_TUNNEL_REQUEST(168, true, false, "29.274", ""),
    DELETE_INDIRECT_DATA_FORWARDING_TUNNEL_RESPONSE(169, false, true, "29.274", ""),
    RELEASE_ACCESS_BEARERS_REQUEST(170, true, false, "29.274", ""),
    RELEASE_ACCESS_BEARERS_RESPONSE(171, false, true, "29.274", ""),
    DOWNLINK_DATA_NOTIFICATION(176, true, false, "29.274", ""),
    DOWNLINK_DATA_NOTIFICATION_ACKNOWLEDGE(177, false, true, "29.274", ""),
    PGW_RESTART_NOTIFICATION(179, true, false, "29.274", ""),
    PGW_RESTART_NOTIFICATION_ACKNOWLEDGE(180, false, true, "29.274", ""),
    UPDATE_PDN_CONNECTION_SET_REQUEST(200, true, false, "29.274", ""),
    UPDATE_PDN_CONNECTION_SET_RESPONSE(201, false, true, "29.274", ""),
    MODIFY_ACCESS_BEARERS_REQUEST(211, true, false, "29.274", ""),
    MODIFY_ACCESS_BEARERS_RESPONSE(212, false, true, "29.274", ""),
    MBMS_SESSION_START_REQUEST(231, true, false, "29.274", ""),
    MBMS_SESSION_START_RESPONSE(232, false, true, "29.274", ""),
    MBMS_SESSION_UPDATE_REQUEST(233, true, false, "29.274", ""),
    MBMS_SESSION_UPDATE_RESPONSE(234, false, true, "29.274", ""),
    MBMS_SESSION_STOP_REQUEST(235, true, false, "29.274", ""),
    MBMS_SESSION_STOP_RESPONSE(236, false, true, "29.274", "");

    private static Map<Integer, Gtp2MessageType> byType = new HashMap<>();

    static {
        Arrays.stream(Gtp2MessageType.values()).forEach(e -> byType.put(e.getType(), e));
    }

    public static Gtp2MessageType lookup(final int type) {
        return byType.get(type);
    }

    private final int type;
    private final boolean isInitial;
    private final boolean isTriggered;

    Gtp2MessageType(final int type, final boolean isInitial, final boolean isTriggered, final String specification, final String section, final ReferencePoint ... refs) {
        this.type = type;
        this.isInitial = isInitial;
        this.isTriggered = isTriggered;
    }

    public int getType() {
        return type;
    }

    public boolean isInitial() {
        return isInitial;
    }

    public boolean isTriggered() {
        return isTriggered;
    }

}
