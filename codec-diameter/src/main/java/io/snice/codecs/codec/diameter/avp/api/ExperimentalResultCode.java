package io.snice.codecs.codec.diameter.avp.api;

import io.snice.buffer.Buffer;
import io.snice.buffer.WritableBuffer;
import io.snice.codecs.codec.diameter.avp.Avp;
import io.snice.codecs.codec.diameter.avp.AvpMandatory;
import io.snice.codecs.codec.diameter.avp.AvpParseException;
import io.snice.codecs.codec.diameter.avp.AvpProtected;
import io.snice.codecs.codec.diameter.avp.FramedAvp;
import io.snice.codecs.codec.diameter.avp.Vendor;
import io.snice.codecs.codec.diameter.avp.impl.DiameterEnumeratedAvp;
import io.snice.codecs.codec.diameter.avp.type.Enumerated;

import java.util.Optional;

/**
 * 
 */
public interface ExperimentalResultCode extends Avp<Enumerated<ExperimentalResultCode.Code>> {

    int CODE = 298;
    
    ExperimentalResultCode DiameterFirstRegistration2001 = ExperimentalResultCode.of(2001);
    ExperimentalResultCode DiameterSubsequentRegistration2002 = ExperimentalResultCode.of(2002);
    ExperimentalResultCode DiameterUnregisteredService2003 = ExperimentalResultCode.of(2003);
    ExperimentalResultCode DiameterSuccessServerNameNotStored2004 = ExperimentalResultCode.of(2004);
    ExperimentalResultCode DiameterServerSelectionDeprecatedValue2005 = ExperimentalResultCode.of(2005);
    ExperimentalResultCode DiameterPdpContextDeletionIndication2021 = ExperimentalResultCode.of(2021);
    ExperimentalResultCode DiameterUserDataNotAvailable4100 = ExperimentalResultCode.of(4100);
    ExperimentalResultCode DiameterPriorUpdateInProgress4101 = ExperimentalResultCode.of(4101);
    ExperimentalResultCode DiameterErrorOutOfResources4121 = ExperimentalResultCode.of(4121);
    ExperimentalResultCode DiameterPccBearerEvent4141 = ExperimentalResultCode.of(4141);
    ExperimentalResultCode DiameterBearerEvent4142 = ExperimentalResultCode.of(4142);
    ExperimentalResultCode DiameterAnGwFailed4143 = ExperimentalResultCode.of(4143);
    ExperimentalResultCode DiameterPendingTransaction4144 = ExperimentalResultCode.of(4144);
    ExperimentalResultCode DiameterAuthenticationDataUnavailable4181 = ExperimentalResultCode.of(4181);
    ExperimentalResultCode DiameterErrorCamelSubscriptionPresent4182 = ExperimentalResultCode.of(4182);
    ExperimentalResultCode DiameterErrorAbsentUser4201 = ExperimentalResultCode.of(4201);
    ExperimentalResultCode DiameterErrorUnreachableUser4221 = ExperimentalResultCode.of(4221);
    ExperimentalResultCode DiameterErrorSuspendedUser4222 = ExperimentalResultCode.of(4222);
    ExperimentalResultCode DiameterErrorDetachedUser4223 = ExperimentalResultCode.of(4223);
    ExperimentalResultCode DiameterErrorPositioningDenied4224 = ExperimentalResultCode.of(4224);
    ExperimentalResultCode DiameterErrorPositioningFailed4225 = ExperimentalResultCode.of(4225);
    ExperimentalResultCode DiameterErrorUnknownUnreachableLcsClient4226 = ExperimentalResultCode.of(4226);
    ExperimentalResultCode DiameterErrorNoAvailablePolicyCountersLcsClient4241 = ExperimentalResultCode.of(4241);
    ExperimentalResultCode RequestedServiceTemporarilyNotAuthorized4261 = ExperimentalResultCode.of(4261);
    ExperimentalResultCode DiameterErrorUserUnknown5001 = ExperimentalResultCode.of(5001);
    ExperimentalResultCode DiameterErrorIdentitiesDontMatch5002 = ExperimentalResultCode.of(5002);
    ExperimentalResultCode DiameterErrorIdentityNotRegistered5003 = ExperimentalResultCode.of(5003);
    ExperimentalResultCode DiameterErrorRoamingNotAllowed5004 = ExperimentalResultCode.of(5004);
    ExperimentalResultCode DiameterErrorIdentityAlreadyRegistered5005 = ExperimentalResultCode.of(5005);
    ExperimentalResultCode DiameterErrorAuthSchemeNotSupported5006 = ExperimentalResultCode.of(5006);
    ExperimentalResultCode DiameterErrorInAssignmentType5007 = ExperimentalResultCode.of(5007);
    ExperimentalResultCode DiameterErrorTooMuchData5008 = ExperimentalResultCode.of(5008);
    ExperimentalResultCode DiameterErrorNotSupportedUserData5009 = ExperimentalResultCode.of(5009);
    ExperimentalResultCode DiameterMissingUserId5010 = ExperimentalResultCode.of(5010);
    ExperimentalResultCode DiameterErrorFeatureUnsupported5011 = ExperimentalResultCode.of(5011);
    ExperimentalResultCode DiameterErrorServingNodeFeatureUnsupported5012 = ExperimentalResultCode.of(5012);
    ExperimentalResultCode DiameterErrorUserNoWlanSubscription5041 = ExperimentalResultCode.of(5041);
    ExperimentalResultCode DiameterErrorWApnUnusedByUser5042 = ExperimentalResultCode.of(5042);
    ExperimentalResultCode DiameterErrorWDiameterErrorNoAccessIndependentSubscription5043 = ExperimentalResultCode.of(5043);
    ExperimentalResultCode DiameterErrorUserNoWApnSubscription5044 = ExperimentalResultCode.of(5044);
    ExperimentalResultCode DiameterErrorUnsuitableNetwork5045 = ExperimentalResultCode.of(5045);
    ExperimentalResultCode InvalidServiceInformation5061 = ExperimentalResultCode.of(5061);
    ExperimentalResultCode FilterRestrictions5062 = ExperimentalResultCode.of(5062);
    ExperimentalResultCode RequestedServiceNotAuthorized5063 = ExperimentalResultCode.of(5063);
    ExperimentalResultCode DuplicatedAfSession5064 = ExperimentalResultCode.of(5064);
    ExperimentalResultCode IpCanSessionNotAvailable5065 = ExperimentalResultCode.of(5065);
    ExperimentalResultCode UnauthorizedNonEmergencySession5066 = ExperimentalResultCode.of(5066);
    ExperimentalResultCode UnauthorizedSponsoredDataConnectivity5067 = ExperimentalResultCode.of(5067);
    ExperimentalResultCode TemporaryNetworkFailure5068 = ExperimentalResultCode.of(5068);
    ExperimentalResultCode DiameterErrorUserDataNotRecognized5100 = ExperimentalResultCode.of(5100);
    ExperimentalResultCode DiameterErrorOperationNotAllowed5101 = ExperimentalResultCode.of(5101);
    ExperimentalResultCode DiameterErrorUserDataCannotBeRead5102 = ExperimentalResultCode.of(5102);
    ExperimentalResultCode DiameterErrorUserDataCannotBeModified5103 = ExperimentalResultCode.of(5103);
    ExperimentalResultCode DiameterErrorUserDataCannotBeNotified5104 = ExperimentalResultCode.of(5104);
    ExperimentalResultCode DiameterErrorTransparentDataOutOfSync5105 = ExperimentalResultCode.of(5105);
    ExperimentalResultCode DiameterErrorSubsDataAbsent5106 = ExperimentalResultCode.of(5106);
    ExperimentalResultCode DiameterErrorNoSubscriptionToData5107 = ExperimentalResultCode.of(5107);
    ExperimentalResultCode DiameterErrorDsaiNotAvailable5108 = ExperimentalResultCode.of(5108);
    ExperimentalResultCode DiameterErrorStartIndication5120 = ExperimentalResultCode.of(5120);
    ExperimentalResultCode DiameterErrorStopIndication5121 = ExperimentalResultCode.of(5121);
    ExperimentalResultCode DiameterErrorUnknownMbmsBearerService5122 = ExperimentalResultCode.of(5122);
    ExperimentalResultCode DiameterErrorServiceArea5123 = ExperimentalResultCode.of(5123);
    ExperimentalResultCode DiameterErrorInitialParameters5140 = ExperimentalResultCode.of(5140);
    ExperimentalResultCode DiameterErrorTriggerEvent5141 = ExperimentalResultCode.of(5141);
    ExperimentalResultCode DiameterPccRuleEvent5142 = ExperimentalResultCode.of(5142);
    ExperimentalResultCode DiameterErrorBearerNotAuthorized5143 = ExperimentalResultCode.of(5143);
    ExperimentalResultCode DiameterErrorTrafficMappingInfoRejected5144 = ExperimentalResultCode.of(5144);
    ExperimentalResultCode DiameterQosRuleEvent5145 = ExperimentalResultCode.of(5145);
    ExperimentalResultCode DiameterErrorTrafficMappingInfoRejected5146 = ExperimentalResultCode.of(5146);
    ExperimentalResultCode DiameterErrorConflictingRequest5147 = ExperimentalResultCode.of(5147);
    ExperimentalResultCode DiameterAdcRuleEvent5148 = ExperimentalResultCode.of(5148);
    ExperimentalResultCode DiameterErrorNbifomNotAuthorized5149 = ExperimentalResultCode.of(5149);
    ExperimentalResultCode DiameterErrorImpiUnknown5401 = ExperimentalResultCode.of(5401);
    ExperimentalResultCode DiameterErrorNotAuthorized5402 = ExperimentalResultCode.of(5402);
    ExperimentalResultCode DiameterErrorTransactionIdentifierInvalid5403 = ExperimentalResultCode.of(5403);
    ExperimentalResultCode DiameterErrorUnknownEpsSubscription5420 = ExperimentalResultCode.of(5420);
    ExperimentalResultCode DiameterErrorRatNotAllowed5421 = ExperimentalResultCode.of(5421);
    ExperimentalResultCode DiameterErrorEquipmentUnknown5422 = ExperimentalResultCode.of(5422);
    ExperimentalResultCode DiameterErrorUnknownServingNode5423 = ExperimentalResultCode.of(5423);
    ExperimentalResultCode DiameterErrorUserNoNon3gppSubscription5450 = ExperimentalResultCode.of(5450);
    ExperimentalResultCode DiameterErrorUserNoApnSubscription5451 = ExperimentalResultCode.of(5451);
    ExperimentalResultCode DiameterErrorRatTypeNotAllowed5452 = ExperimentalResultCode.of(5452);
    ExperimentalResultCode DiameterErrorLateOverlappingRequest5453 = ExperimentalResultCode.of(5453);
    ExperimentalResultCode DiameterErrorTimedOutRequest5454 = ExperimentalResultCode.of(5454);
    ExperimentalResultCode DiameterErrorSubsession5470 = ExperimentalResultCode.of(5470);
    ExperimentalResultCode DiameterErrorOngoingSessionEstablishment5471 = ExperimentalResultCode.of(5471);
    ExperimentalResultCode DiameterErrorUnauthorizedRequestingNetwork5490 = ExperimentalResultCode.of(5490);
    ExperimentalResultCode DiameterErrorUnauthorizedRequestingEntity5510 = ExperimentalResultCode.of(5510);
    ExperimentalResultCode DiameterErrorUnauthorizedService5511 = ExperimentalResultCode.of(5511);
    ExperimentalResultCode DiameterErrorRequestedRangeIsNotAllowed5512 = ExperimentalResultCode.of(5512);
    ExperimentalResultCode DiameterErrorConfigurationEventStorageNotSuccessful5513 = ExperimentalResultCode.of(5513);
    ExperimentalResultCode DiameterErrorConfigurationEventNonExistant5514 = ExperimentalResultCode.of(5514);
    ExperimentalResultCode DiameterErrorScefReferenceIdUnknown5515 = ExperimentalResultCode.of(5515);
    ExperimentalResultCode DiameterErrorInvalidSmeAddress5530 = ExperimentalResultCode.of(5530);
    ExperimentalResultCode DiameterErrorScCongestion5531 = ExperimentalResultCode.of(5531);
    ExperimentalResultCode DiameterErrorSmProtocol5532 = ExperimentalResultCode.of(5532);
    ExperimentalResultCode DiameterErrorTriggerReplaceFailure5533 = ExperimentalResultCode.of(5533);
    ExperimentalResultCode DiameterErrorTriggerRecallFailure5534 = ExperimentalResultCode.of(5534);
    ExperimentalResultCode DiameterErrorOriginalMessageNotPending5535 = ExperimentalResultCode.of(5535);
    ExperimentalResultCode DiameterErrorAbsentUser5550 = ExperimentalResultCode.of(5550);
    ExperimentalResultCode DiameterErrorUserBusyForMtSms5551 = ExperimentalResultCode.of(5551);
    ExperimentalResultCode DiameterErrorFacilityNotSupported5552 = ExperimentalResultCode.of(5552);
    ExperimentalResultCode DiameterErrorIllegalUser5553 = ExperimentalResultCode.of(5553);
    ExperimentalResultCode DiameterErrorIllegalEquipment5554 = ExperimentalResultCode.of(5554);
    ExperimentalResultCode DiameterErrorSmDeliveryFailure5555 = ExperimentalResultCode.of(5555);
    ExperimentalResultCode DiameterErrorServiceNotSubscribed5556 = ExperimentalResultCode.of(5556);
    ExperimentalResultCode DiameterErrorServiceBarred5557 = ExperimentalResultCode.of(5557);
    ExperimentalResultCode DiameterErrorMwdListFull5558 = ExperimentalResultCode.of(5558);
    ExperimentalResultCode DiameterErrorUnknownPolicyCounters5570 = ExperimentalResultCode.of(5570);
    ExperimentalResultCode DiameterErrorOriginAluidUnknown5590 = ExperimentalResultCode.of(5590);
    ExperimentalResultCode DiameterErrorTargetAluidUnknown5591 = ExperimentalResultCode.of(5591);
    ExperimentalResultCode DiameterErrorPfidUnknown5592 = ExperimentalResultCode.of(5592);
    ExperimentalResultCode DiameterErrorAppRegisterReject5593 = ExperimentalResultCode.of(5593);
    ExperimentalResultCode DiameterErrorProseMapRequestDisallowed5594 = ExperimentalResultCode.of(5594);
    ExperimentalResultCode DiameterErrorMapRequestReject5595 = ExperimentalResultCode.of(5595);
    ExperimentalResultCode DiameterErrorRequestingRpauidUnknown5596 = ExperimentalResultCode.of(5596);
    ExperimentalResultCode DiameterErrorUnknownOrInvalidTargetSet5597 = ExperimentalResultCode.of(5597);
    ExperimentalResultCode DiameterErrorMissingApplicationData5598 = ExperimentalResultCode.of(5598);
    ExperimentalResultCode DiameterErrorAuthorizationReject5599 = ExperimentalResultCode.of(5599);
    ExperimentalResultCode DiameterErrorDiscoveryNotPermitted5600 = ExperimentalResultCode.of(5600);
    ExperimentalResultCode DiameterErrorTargetRpauidUnknown5601 = ExperimentalResultCode.of(5601);
    ExperimentalResultCode DiameterErrorInvalidApplicationData5602 = ExperimentalResultCode.of(5602);
    ExperimentalResultCode DiameterErrorUnknownProseSubscription5610 = ExperimentalResultCode.of(5610);
    ExperimentalResultCode ProseNotAllowed5611 = ExperimentalResultCode.of(5611);
    ExperimentalResultCode DiameterErrorUeLocationUnknown5612 = ExperimentalResultCode.of(5612);
    ExperimentalResultCode DiameterErrorNoAssociatedDiscoveryFilter5630 = ExperimentalResultCode.of(5630);
    ExperimentalResultCode DiameterErrorAnnouncingUnauthorizedInPlmn5631 = ExperimentalResultCode.of(5631);
    ExperimentalResultCode DiameterErrorInvalidApplicationCode5632 = ExperimentalResultCode.of(5632);
    ExperimentalResultCode DiameterErrorProximityUnauthorized5633 = ExperimentalResultCode.of(5633);
    ExperimentalResultCode DiameterErrorProximityRejected5634 = ExperimentalResultCode.of(5634);
    ExperimentalResultCode DiameterErrorNoProximityRequest5635 = ExperimentalResultCode.of(5635);
    ExperimentalResultCode DiameterErrorUnauthorizedServiceInThisPlmn5636 = ExperimentalResultCode.of(5636);
    ExperimentalResultCode DiameterErrorProximityCancelled5637 = ExperimentalResultCode.of(5637);
    ExperimentalResultCode DiameterErrorInvalidTargetPduid5638 = ExperimentalResultCode.of(5638);
    ExperimentalResultCode DiameterErrorInvalidTargetRpauid5639 = ExperimentalResultCode.of(5639);
    ExperimentalResultCode DiameterErrorNoAssociatedRestrictedCode5640 = ExperimentalResultCode.of(5640);
    ExperimentalResultCode DiameterErrorInvalidDiscoveryType5641 = ExperimentalResultCode.of(5641);
    ExperimentalResultCode DiameterErrorRequestedLocationNotServed5650 = ExperimentalResultCode.of(5650);
    ExperimentalResultCode DiameterErrorInvalidEpsBearer5651 = ExperimentalResultCode.of(5651);
    ExperimentalResultCode DiameterErrorNiddConfigurationNotAvailable5652 = ExperimentalResultCode.of(5652);
    ExperimentalResultCode DiameterErrorUserTemporarilyUnreachable5653 = ExperimentalResultCode.of(5653);
    ExperimentalResultCode DiameterErrorUnknkownData5670 = ExperimentalResultCode.of(5670);
    ExperimentalResultCode DiameterErrorRequiredKeyNotProvided5671 = ExperimentalResultCode.of(5671);
    ExperimentalResultCode DiameterErrorUnknownV2xSubscription5690 = ExperimentalResultCode.of(5690);
    ExperimentalResultCode DiameterErrorV2xNotAllowed5691 = ExperimentalResultCode.of(5691);

    @Override
    default long getCode() {
        return CODE;
    }

    @Override
    default ExperimentalResultCode toExperimentalResultCode() {
        return this;
    }

    @Override
    default boolean isExperimentalResultCode() {
        return true;
    }

    @Override
    default void writeValue(final WritableBuffer buffer) {
        buffer.write(getValue().getValue());
    }

    static ExperimentalResultCode of(final int code) {
        final Optional<Code> c = Code.lookup(code);
        final EnumeratedHolder enumerated = new EnumeratedHolder(code, c);
        final Avp<Enumerated> avp = Avp.ofType(Enumerated.class)
                .withValue(enumerated)
                .withAvpCode(CODE)
                .isMandatory(AvpMandatory.MUST.isMandatory())
                .isProtected(AvpProtected.MUST_NOT.isProtected())
                .withVendor(Vendor.NONE)
                .build();
        return new DefaultExperimentalResultCode(avp, enumerated);
    }

    enum Code { 
        DIAMETER_FIRST_REGISTRATION_2001("DIAMETER_FIRST_REGISTRATION", 2001),
        DIAMETER_SUBSEQUENT_REGISTRATION_2002("DIAMETER_SUBSEQUENT_REGISTRATION", 2002),
        DIAMETER_UNREGISTERED_SERVICE_2003("DIAMETER_UNREGISTERED_SERVICE", 2003),
        DIAMETER_SUCCESS_SERVER_NAME_NOT_STORED_2004("DIAMETER_SUCCESS_SERVER_NAME_NOT_STORED", 2004),
        DIAMETER_SERVER_SELECTION_Deprecated_value__2005("DIAMETER_SERVER_SELECTION_Deprecated_value_", 2005),
        DIAMETER_PDP_CONTEXT_DELETION_INDICATION_2021("DIAMETER_PDP_CONTEXT_DELETION_INDICATION", 2021),
        DIAMETER_USER_DATA_NOT_AVAILABLE_4100("DIAMETER_USER_DATA_NOT_AVAILABLE", 4100),
        DIAMETER_PRIOR_UPDATE_IN_PROGRESS_4101("DIAMETER_PRIOR_UPDATE_IN_PROGRESS", 4101),
        DIAMETER_ERROR_OUT_OF_RESOURCES_4121("DIAMETER_ERROR_OUT_OF_RESOURCES", 4121),
        DIAMETER_PCC_BEARER_EVENT_4141("DIAMETER_PCC_BEARER_EVENT", 4141),
        DIAMETER_BEARER_EVENT_4142("DIAMETER_BEARER_EVENT", 4142),
        DIAMETER_AN_GW_FAILED_4143("DIAMETER_AN_GW_FAILED", 4143),
        DIAMETER_PENDING_TRANSACTION_4144("DIAMETER_PENDING_TRANSACTION", 4144),
        DIAMETER_AUTHENTICATION_DATA_UNAVAILABLE_4181("DIAMETER_AUTHENTICATION_DATA_UNAVAILABLE", 4181),
        DIAMETER_ERROR_CAMEL_SUBSCRIPTION_PRESENT_4182("DIAMETER_ERROR_CAMEL_SUBSCRIPTION_PRESENT", 4182),
        DIAMETER_ERROR_ABSENT_USER_4201("DIAMETER_ERROR_ABSENT_USER", 4201),
        DIAMETER_ERROR_UNREACHABLE_USER_4221("DIAMETER_ERROR_UNREACHABLE_USER", 4221),
        DIAMETER_ERROR_SUSPENDED_USER_4222("DIAMETER_ERROR_SUSPENDED_USER", 4222),
        DIAMETER_ERROR_DETACHED_USER_4223("DIAMETER_ERROR_DETACHED_USER", 4223),
        DIAMETER_ERROR_POSITIONING_DENIED_4224("DIAMETER_ERROR_POSITIONING_DENIED", 4224),
        DIAMETER_ERROR_POSITIONING_FAILED_4225("DIAMETER_ERROR_POSITIONING_FAILED", 4225),
        DIAMETER_ERROR_UNKNOWN_UNREACHABLE_LCS_CLIENT_4226("DIAMETER_ERROR_UNKNOWN_UNREACHABLE_LCS_CLIENT", 4226),
        DIAMETER_ERROR_NO_AVAILABLE_POLICY_COUNTERS_LCS_CLIENT_4241("DIAMETER_ERROR_NO_AVAILABLE_POLICY_COUNTERS_LCS_CLIENT", 4241),
        REQUESTED_SERVICE_TEMPORARILY_NOT_AUTHORIZED_4261("REQUESTED_SERVICE_TEMPORARILY_NOT_AUTHORIZED", 4261),
        DIAMETER_ERROR_USER_UNKNOWN_5001("DIAMETER_ERROR_USER_UNKNOWN", 5001),
        DIAMETER_ERROR_IDENTITIES_DONT_MATCH_5002("DIAMETER_ERROR_IDENTITIES_DONT_MATCH", 5002),
        DIAMETER_ERROR_IDENTITY_NOT_REGISTERED_5003("DIAMETER_ERROR_IDENTITY_NOT_REGISTERED", 5003),
        DIAMETER_ERROR_ROAMING_NOT_ALLOWED_5004("DIAMETER_ERROR_ROAMING_NOT_ALLOWED", 5004),
        DIAMETER_ERROR_IDENTITY_ALREADY_REGISTERED_5005("DIAMETER_ERROR_IDENTITY_ALREADY_REGISTERED", 5005),
        DIAMETER_ERROR_AUTH_SCHEME_NOT_SUPPORTED_5006("DIAMETER_ERROR_AUTH_SCHEME_NOT_SUPPORTED", 5006),
        DIAMETER_ERROR_IN_ASSIGNMENT_TYPE_5007("DIAMETER_ERROR_IN_ASSIGNMENT_TYPE", 5007),
        DIAMETER_ERROR_TOO_MUCH_DATA_5008("DIAMETER_ERROR_TOO_MUCH_DATA", 5008),
        DIAMETER_ERROR_NOT_SUPPORTED_USER_DATA_5009("DIAMETER_ERROR_NOT_SUPPORTED_USER_DATA", 5009),
        DIAMETER_MISSING_USER_ID_5010("DIAMETER_MISSING_USER_ID", 5010),
        DIAMETER_ERROR_FEATURE_UNSUPPORTED_5011("DIAMETER_ERROR_FEATURE_UNSUPPORTED", 5011),
        DIAMETER_ERROR_SERVING_NODE_FEATURE_UNSUPPORTED_5012("DIAMETER_ERROR_SERVING_NODE_FEATURE_UNSUPPORTED", 5012),
        DIAMETER_ERROR_USER_NO_WLAN_SUBSCRIPTION_5041("DIAMETER_ERROR_USER_NO_WLAN_SUBSCRIPTION", 5041),
        DIAMETER_ERROR_W_APN_UNUSED_BY_USER_5042("DIAMETER_ERROR_W_APN_UNUSED_BY_USER", 5042),
        DIAMETER_ERROR_W_DIAMETER_ERROR_NO_ACCESS_INDEPENDENT_SUBSCRIPTION_5043("DIAMETER_ERROR_W_DIAMETER_ERROR_NO_ACCESS_INDEPENDENT_SUBSCRIPTION", 5043),
        DIAMETER_ERROR_USER_NO_W_APN_SUBSCRIPTION_5044("DIAMETER_ERROR_USER_NO_W_APN_SUBSCRIPTION", 5044),
        DIAMETER_ERROR_UNSUITABLE_NETWORK_5045("DIAMETER_ERROR_UNSUITABLE_NETWORK", 5045),
        INVALID_SERVICE_INFORMATION_5061("INVALID_SERVICE_INFORMATION", 5061),
        FILTER_RESTRICTIONS_5062("FILTER_RESTRICTIONS", 5062),
        REQUESTED_SERVICE_NOT_AUTHORIZED_5063("REQUESTED_SERVICE_NOT_AUTHORIZED", 5063),
        DUPLICATED_AF_SESSION_5064("DUPLICATED_AF_SESSION", 5064),
        IP_CAN_SESSION_NOT_AVAILABLE_5065("IP_CAN_SESSION_NOT_AVAILABLE", 5065),
        UNAUTHORIZED_NON_EMERGENCY_SESSION_5066("UNAUTHORIZED_NON_EMERGENCY_SESSION", 5066),
        UNAUTHORIZED_SPONSORED_DATA_CONNECTIVITY_5067("UNAUTHORIZED_SPONSORED_DATA_CONNECTIVITY", 5067),
        TEMPORARY_NETWORK_FAILURE_5068("TEMPORARY_NETWORK_FAILURE", 5068),
        DIAMETER_ERROR_USER_DATA_NOT_RECOGNIZED_5100("DIAMETER_ERROR_USER_DATA_NOT_RECOGNIZED", 5100),
        DIAMETER_ERROR_OPERATION_NOT_ALLOWED_5101("DIAMETER_ERROR_OPERATION_NOT_ALLOWED", 5101),
        DIAMETER_ERROR_USER_DATA_CANNOT_BE_READ_5102("DIAMETER_ERROR_USER_DATA_CANNOT_BE_READ", 5102),
        DIAMETER_ERROR_USER_DATA_CANNOT_BE_MODIFIED_5103("DIAMETER_ERROR_USER_DATA_CANNOT_BE_MODIFIED", 5103),
        DIAMETER_ERROR_USER_DATA_CANNOT_BE_NOTIFIED_5104("DIAMETER_ERROR_USER_DATA_CANNOT_BE_NOTIFIED", 5104),
        DIAMETER_ERROR_TRANSPARENT_DATA_OUT_OF_SYNC_5105("DIAMETER_ERROR_TRANSPARENT_DATA_OUT_OF_SYNC", 5105),
        DIAMETER_ERROR_SUBS_DATA_ABSENT_5106("DIAMETER_ERROR_SUBS_DATA_ABSENT", 5106),
        DIAMETER_ERROR_NO_SUBSCRIPTION_TO_DATA_5107("DIAMETER_ERROR_NO_SUBSCRIPTION_TO_DATA", 5107),
        DIAMETER_ERROR_DSAI_NOT_AVAILABLE_5108("DIAMETER_ERROR_DSAI_NOT_AVAILABLE", 5108),
        DIAMETER_ERROR_START_INDICATION_5120("DIAMETER_ERROR_START_INDICATION", 5120),
        DIAMETER_ERROR_STOP_INDICATION_5121("DIAMETER_ERROR_STOP_INDICATION", 5121),
        DIAMETER_ERROR_UNKNOWN_MBMS_BEARER_SERVICE_5122("DIAMETER_ERROR_UNKNOWN_MBMS_BEARER_SERVICE", 5122),
        DIAMETER_ERROR_SERVICE_AREA_5123("DIAMETER_ERROR_SERVICE_AREA", 5123),
        DIAMETER_ERROR_INITIAL_PARAMETERS_5140("DIAMETER_ERROR_INITIAL_PARAMETERS", 5140),
        DIAMETER_ERROR_TRIGGER_EVENT_5141("DIAMETER_ERROR_TRIGGER_EVENT", 5141),
        DIAMETER_PCC_RULE_EVENT_5142("DIAMETER_PCC_RULE_EVENT", 5142),
        DIAMETER_ERROR_BEARER_NOT_AUTHORIZED_5143("DIAMETER_ERROR_BEARER_NOT_AUTHORIZED", 5143),
        DIAMETER_ERROR_TRAFFIC_MAPPING_INFO_REJECTED_5144("DIAMETER_ERROR_TRAFFIC_MAPPING_INFO_REJECTED", 5144),
        DIAMETER_QOS_RULE_EVENT_5145("DIAMETER_QOS_RULE_EVENT", 5145),
        DIAMETER_ERROR_TRAFFIC_MAPPING_INFO_REJECTED_5146("DIAMETER_ERROR_TRAFFIC_MAPPING_INFO_REJECTED", 5146),
        DIAMETER_ERROR_CONFLICTING_REQUEST_5147("DIAMETER_ERROR_CONFLICTING_REQUEST", 5147),
        DIAMETER_ADC_RULE_EVENT_5148("DIAMETER_ADC_RULE_EVENT", 5148),
        DIAMETER_ERROR_NBIFOM_NOT_AUTHORIZED_5149("DIAMETER_ERROR_NBIFOM_NOT_AUTHORIZED", 5149),
        DIAMETER_ERROR_IMPI_UNKNOWN_5401("DIAMETER_ERROR_IMPI_UNKNOWN", 5401),
        DIAMETER_ERROR_NOT_AUTHORIZED_5402("DIAMETER_ERROR_NOT_AUTHORIZED", 5402),
        DIAMETER_ERROR_TRANSACTION_IDENTIFIER_INVALID_5403("DIAMETER_ERROR_TRANSACTION_IDENTIFIER_INVALID", 5403),
        DIAMETER_ERROR_UNKNOWN_EPS_SUBSCRIPTION_5420("DIAMETER_ERROR_UNKNOWN_EPS_SUBSCRIPTION", 5420),
        DIAMETER_ERROR_RAT_NOT_ALLOWED_5421("DIAMETER_ERROR_RAT_NOT_ALLOWED", 5421),
        DIAMETER_ERROR_EQUIPMENT_UNKNOWN_5422("DIAMETER_ERROR_EQUIPMENT_UNKNOWN", 5422),
        DIAMETER_ERROR_UNKNOWN_SERVING_NODE_5423("DIAMETER_ERROR_UNKNOWN_SERVING_NODE", 5423),
        DIAMETER_ERROR_USER_NO_NON_3GPP_SUBSCRIPTION_5450("DIAMETER_ERROR_USER_NO_NON_3GPP_SUBSCRIPTION", 5450),
        DIAMETER_ERROR_USER_NO_APN_SUBSCRIPTION_5451("DIAMETER_ERROR_USER_NO_APN_SUBSCRIPTION", 5451),
        DIAMETER_ERROR_RAT_TYPE_NOT_ALLOWED_5452("DIAMETER_ERROR_RAT_TYPE_NOT_ALLOWED", 5452),
        DIAMETER_ERROR_LATE_OVERLAPPING_REQUEST_5453("DIAMETER_ERROR_LATE_OVERLAPPING_REQUEST", 5453),
        DIAMETER_ERROR_TIMED_OUT_REQUEST_5454("DIAMETER_ERROR_TIMED_OUT_REQUEST", 5454),
        DIAMETER_ERROR_SUBSESSION_5470("DIAMETER_ERROR_SUBSESSION", 5470),
        DIAMETER_ERROR_ONGOING_SESSION_ESTABLISHMENT_5471("DIAMETER_ERROR_ONGOING_SESSION_ESTABLISHMENT", 5471),
        DIAMETER_ERROR_UNAUTHORIZED_REQUESTING_NETWORK_5490("DIAMETER_ERROR_UNAUTHORIZED_REQUESTING_NETWORK", 5490),
        DIAMETER_ERROR_UNAUTHORIZED_REQUESTING_ENTITY_5510("DIAMETER_ERROR_UNAUTHORIZED_REQUESTING_ENTITY", 5510),
        DIAMETER_ERROR_UNAUTHORIZED_SERVICE_5511("DIAMETER_ERROR_UNAUTHORIZED_SERVICE", 5511),
        DIAMETER_ERROR_REQUESTED_RANGE_IS_NOT_ALLOWED_5512("DIAMETER_ERROR_REQUESTED_RANGE_IS_NOT_ALLOWED", 5512),
        DIAMETER_ERROR_CONFIGURATION_EVENT_STORAGE_NOT_SUCCESSFUL_5513("DIAMETER_ERROR_CONFIGURATION_EVENT_STORAGE_NOT_SUCCESSFUL", 5513),
        DIAMETER_ERROR_CONFIGURATION_EVENT_NON_EXISTANT_5514("DIAMETER_ERROR_CONFIGURATION_EVENT_NON_EXISTANT", 5514),
        DIAMETER_ERROR_SCEF_REFERENCE_ID_UNKNOWN_5515("DIAMETER_ERROR_SCEF_REFERENCE_ID_UNKNOWN", 5515),
        DIAMETER_ERROR_INVALID_SME_ADDRESS_5530("DIAMETER_ERROR_INVALID_SME_ADDRESS", 5530),
        DIAMETER_ERROR_SC_CONGESTION_5531("DIAMETER_ERROR_SC_CONGESTION", 5531),
        DIAMETER_ERROR_SM_PROTOCOL_5532("DIAMETER_ERROR_SM_PROTOCOL", 5532),
        DIAMETER_ERROR_TRIGGER_REPLACE_FAILURE_5533("DIAMETER_ERROR_TRIGGER_REPLACE_FAILURE", 5533),
        DIAMETER_ERROR_TRIGGER_RECALL_FAILURE_5534("DIAMETER_ERROR_TRIGGER_RECALL_FAILURE", 5534),
        DIAMETER_ERROR_ORIGINAL_MESSAGE_NOT_PENDING_5535("DIAMETER_ERROR_ORIGINAL_MESSAGE_NOT_PENDING", 5535),
        DIAMETER_ERROR_ABSENT_USER_5550("DIAMETER_ERROR_ABSENT_USER", 5550),
        DIAMETER_ERROR_USER_BUSY_FOR_MT_SMS_5551("DIAMETER_ERROR_USER_BUSY_FOR_MT_SMS", 5551),
        DIAMETER_ERROR_FACILITY_NOT_SUPPORTED_5552("DIAMETER_ERROR_FACILITY_NOT_SUPPORTED", 5552),
        DIAMETER_ERROR_ILLEGAL_USER_5553("DIAMETER_ERROR_ILLEGAL_USER", 5553),
        DIAMETER_ERROR_ILLEGAL_EQUIPMENT_5554("DIAMETER_ERROR_ILLEGAL_EQUIPMENT", 5554),
        DIAMETER_ERROR_SM_DELIVERY_FAILURE_5555("DIAMETER_ERROR_SM_DELIVERY_FAILURE", 5555),
        DIAMETER_ERROR_SERVICE_NOT_SUBSCRIBED_5556("DIAMETER_ERROR_SERVICE_NOT_SUBSCRIBED", 5556),
        DIAMETER_ERROR_SERVICE_BARRED_5557("DIAMETER_ERROR_SERVICE_BARRED", 5557),
        DIAMETER_ERROR_MWD_LIST_FULL_5558("DIAMETER_ERROR_MWD_LIST_FULL", 5558),
        DIAMETER_ERROR_UNKNOWN_POLICY_COUNTERS_5570("DIAMETER_ERROR_UNKNOWN_POLICY_COUNTERS", 5570),
        DIAMETER_ERROR_ORIGIN_ALUID_UNKNOWN_5590("DIAMETER_ERROR_ORIGIN_ALUID_UNKNOWN", 5590),
        DIAMETER_ERROR_TARGET_ALUID_UNKNOWN_5591("DIAMETER_ERROR_TARGET_ALUID_UNKNOWN", 5591),
        DIAMETER_ERROR_PFID_UNKNOWN_5592("DIAMETER_ERROR_PFID_UNKNOWN", 5592),
        DIAMETER_ERROR_APP_REGISTER_REJECT_5593("DIAMETER_ERROR_APP_REGISTER_REJECT", 5593),
        DIAMETER_ERROR_PROSE_MAP_REQUEST_DISALLOWED_5594("DIAMETER_ERROR_PROSE_MAP_REQUEST_DISALLOWED", 5594),
        DIAMETER_ERROR_MAP_REQUEST_REJECT_5595("DIAMETER_ERROR_MAP_REQUEST_REJECT", 5595),
        DIAMETER_ERROR_REQUESTING_RPAUID_UNKNOWN_5596("DIAMETER_ERROR_REQUESTING_RPAUID_UNKNOWN", 5596),
        DIAMETER_ERROR_UNKNOWN_OR_INVALID_TARGET_SET_5597("DIAMETER_ERROR_UNKNOWN_OR_INVALID_TARGET_SET", 5597),
        DIAMETER_ERROR_MISSING_APPLICATION_DATA_5598("DIAMETER_ERROR_MISSING_APPLICATION_DATA", 5598),
        DIAMETER_ERROR_AUTHORIZATION_REJECT_5599("DIAMETER_ERROR_AUTHORIZATION_REJECT", 5599),
        DIAMETER_ERROR_DISCOVERY_NOT_PERMITTED_5600("DIAMETER_ERROR_DISCOVERY_NOT_PERMITTED", 5600),
        DIAMETER_ERROR_TARGET_RPAUID_UNKNOWN_5601("DIAMETER_ERROR_TARGET_RPAUID_UNKNOWN", 5601),
        DIAMETER_ERROR_INVALID_APPLICATION_DATA_5602("DIAMETER_ERROR_INVALID_APPLICATION_DATA", 5602),
        DIAMETER_ERROR_UNKNOWN_PROSE_SUBSCRIPTION_5610("DIAMETER_ERROR_UNKNOWN_PROSE_SUBSCRIPTION", 5610),
        PROSE_NOT_ALLOWED_5611("PROSE_NOT_ALLOWED", 5611),
        DIAMETER_ERROR_UE_LOCATION_UNKNOWN_5612("DIAMETER_ERROR_UE_LOCATION_UNKNOWN", 5612),
        DIAMETER_ERROR_NO_ASSOCIATED_DISCOVERY_FILTER_5630("DIAMETER_ERROR_NO_ASSOCIATED_DISCOVERY_FILTER", 5630),
        DIAMETER_ERROR_ANNOUNCING_UNAUTHORIZED_IN_PLMN_5631("DIAMETER_ERROR_ANNOUNCING_UNAUTHORIZED_IN_PLMN", 5631),
        DIAMETER_ERROR_INVALID_APPLICATION_CODE_5632("DIAMETER_ERROR_INVALID_APPLICATION_CODE", 5632),
        DIAMETER_ERROR_PROXIMITY_UNAUTHORIZED_5633("DIAMETER_ERROR_PROXIMITY_UNAUTHORIZED", 5633),
        DIAMETER_ERROR_PROXIMITY_REJECTED_5634("DIAMETER_ERROR_PROXIMITY_REJECTED", 5634),
        DIAMETER_ERROR_NO_PROXIMITY_REQUEST_5635("DIAMETER_ERROR_NO_PROXIMITY_REQUEST", 5635),
        DIAMETER_ERROR_UNAUTHORIZED_SERVICE_IN_THIS_PLMN_5636("DIAMETER_ERROR_UNAUTHORIZED_SERVICE_IN_THIS_PLMN", 5636),
        DIAMETER_ERROR_PROXIMITY_CANCELLED_5637("DIAMETER_ERROR_PROXIMITY_CANCELLED", 5637),
        DIAMETER_ERROR_INVALID_TARGET_PDUID_5638("DIAMETER_ERROR_INVALID_TARGET_PDUID", 5638),
        DIAMETER_ERROR_INVALID_TARGET_RPAUID_5639("DIAMETER_ERROR_INVALID_TARGET_RPAUID", 5639),
        DIAMETER_ERROR_NO_ASSOCIATED_RESTRICTED_CODE_5640("DIAMETER_ERROR_NO_ASSOCIATED_RESTRICTED_CODE", 5640),
        DIAMETER_ERROR_INVALID_DISCOVERY_TYPE_5641("DIAMETER_ERROR_INVALID_DISCOVERY_TYPE", 5641),
        DIAMETER_ERROR_REQUESTED_LOCATION_NOT_SERVED_5650("DIAMETER_ERROR_REQUESTED_LOCATION_NOT_SERVED", 5650),
        DIAMETER_ERROR_INVALID_EPS_BEARER_5651("DIAMETER_ERROR_INVALID_EPS_BEARER", 5651),
        DIAMETER_ERROR_NIDD_CONFIGURATION_NOT_AVAILABLE_5652("DIAMETER_ERROR_NIDD_CONFIGURATION_NOT_AVAILABLE", 5652),
        DIAMETER_ERROR_USER_TEMPORARILY_UNREACHABLE_5653("DIAMETER_ERROR_USER_TEMPORARILY_UNREACHABLE", 5653),
        DIAMETER_ERROR_UNKNKOWN_DATA_5670("DIAMETER_ERROR_UNKNKOWN_DATA", 5670),
        DIAMETER_ERROR_REQUIRED_KEY_NOT_PROVIDED_5671("DIAMETER_ERROR_REQUIRED_KEY_NOT_PROVIDED", 5671),
        DIAMETER_ERROR_UNKNOWN_V2X_SUBSCRIPTION_5690("DIAMETER_ERROR_UNKNOWN_V2X_SUBSCRIPTION", 5690),
        DIAMETER_ERROR_V2X_NOT_ALLOWED_5691("DIAMETER_ERROR_V2X_NOT_ALLOWED", 5691);

        private final String name;
        private final int code;

        Code(final String name, final int code) {
            this.name = name;
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        static Optional<Code> lookup(final int code) {
            switch (code) { 
                case 2001: return Optional.of(DIAMETER_FIRST_REGISTRATION_2001);
                case 2002: return Optional.of(DIAMETER_SUBSEQUENT_REGISTRATION_2002);
                case 2003: return Optional.of(DIAMETER_UNREGISTERED_SERVICE_2003);
                case 2004: return Optional.of(DIAMETER_SUCCESS_SERVER_NAME_NOT_STORED_2004);
                case 2005: return Optional.of(DIAMETER_SERVER_SELECTION_Deprecated_value__2005);
                case 2021: return Optional.of(DIAMETER_PDP_CONTEXT_DELETION_INDICATION_2021);
                case 4100: return Optional.of(DIAMETER_USER_DATA_NOT_AVAILABLE_4100);
                case 4101: return Optional.of(DIAMETER_PRIOR_UPDATE_IN_PROGRESS_4101);
                case 4121: return Optional.of(DIAMETER_ERROR_OUT_OF_RESOURCES_4121);
                case 4141: return Optional.of(DIAMETER_PCC_BEARER_EVENT_4141);
                case 4142: return Optional.of(DIAMETER_BEARER_EVENT_4142);
                case 4143: return Optional.of(DIAMETER_AN_GW_FAILED_4143);
                case 4144: return Optional.of(DIAMETER_PENDING_TRANSACTION_4144);
                case 4181: return Optional.of(DIAMETER_AUTHENTICATION_DATA_UNAVAILABLE_4181);
                case 4182: return Optional.of(DIAMETER_ERROR_CAMEL_SUBSCRIPTION_PRESENT_4182);
                case 4201: return Optional.of(DIAMETER_ERROR_ABSENT_USER_4201);
                case 4221: return Optional.of(DIAMETER_ERROR_UNREACHABLE_USER_4221);
                case 4222: return Optional.of(DIAMETER_ERROR_SUSPENDED_USER_4222);
                case 4223: return Optional.of(DIAMETER_ERROR_DETACHED_USER_4223);
                case 4224: return Optional.of(DIAMETER_ERROR_POSITIONING_DENIED_4224);
                case 4225: return Optional.of(DIAMETER_ERROR_POSITIONING_FAILED_4225);
                case 4226: return Optional.of(DIAMETER_ERROR_UNKNOWN_UNREACHABLE_LCS_CLIENT_4226);
                case 4241: return Optional.of(DIAMETER_ERROR_NO_AVAILABLE_POLICY_COUNTERS_LCS_CLIENT_4241);
                case 4261: return Optional.of(REQUESTED_SERVICE_TEMPORARILY_NOT_AUTHORIZED_4261);
                case 5001: return Optional.of(DIAMETER_ERROR_USER_UNKNOWN_5001);
                case 5002: return Optional.of(DIAMETER_ERROR_IDENTITIES_DONT_MATCH_5002);
                case 5003: return Optional.of(DIAMETER_ERROR_IDENTITY_NOT_REGISTERED_5003);
                case 5004: return Optional.of(DIAMETER_ERROR_ROAMING_NOT_ALLOWED_5004);
                case 5005: return Optional.of(DIAMETER_ERROR_IDENTITY_ALREADY_REGISTERED_5005);
                case 5006: return Optional.of(DIAMETER_ERROR_AUTH_SCHEME_NOT_SUPPORTED_5006);
                case 5007: return Optional.of(DIAMETER_ERROR_IN_ASSIGNMENT_TYPE_5007);
                case 5008: return Optional.of(DIAMETER_ERROR_TOO_MUCH_DATA_5008);
                case 5009: return Optional.of(DIAMETER_ERROR_NOT_SUPPORTED_USER_DATA_5009);
                case 5010: return Optional.of(DIAMETER_MISSING_USER_ID_5010);
                case 5011: return Optional.of(DIAMETER_ERROR_FEATURE_UNSUPPORTED_5011);
                case 5012: return Optional.of(DIAMETER_ERROR_SERVING_NODE_FEATURE_UNSUPPORTED_5012);
                case 5041: return Optional.of(DIAMETER_ERROR_USER_NO_WLAN_SUBSCRIPTION_5041);
                case 5042: return Optional.of(DIAMETER_ERROR_W_APN_UNUSED_BY_USER_5042);
                case 5043: return Optional.of(DIAMETER_ERROR_W_DIAMETER_ERROR_NO_ACCESS_INDEPENDENT_SUBSCRIPTION_5043);
                case 5044: return Optional.of(DIAMETER_ERROR_USER_NO_W_APN_SUBSCRIPTION_5044);
                case 5045: return Optional.of(DIAMETER_ERROR_UNSUITABLE_NETWORK_5045);
                case 5061: return Optional.of(INVALID_SERVICE_INFORMATION_5061);
                case 5062: return Optional.of(FILTER_RESTRICTIONS_5062);
                case 5063: return Optional.of(REQUESTED_SERVICE_NOT_AUTHORIZED_5063);
                case 5064: return Optional.of(DUPLICATED_AF_SESSION_5064);
                case 5065: return Optional.of(IP_CAN_SESSION_NOT_AVAILABLE_5065);
                case 5066: return Optional.of(UNAUTHORIZED_NON_EMERGENCY_SESSION_5066);
                case 5067: return Optional.of(UNAUTHORIZED_SPONSORED_DATA_CONNECTIVITY_5067);
                case 5068: return Optional.of(TEMPORARY_NETWORK_FAILURE_5068);
                case 5100: return Optional.of(DIAMETER_ERROR_USER_DATA_NOT_RECOGNIZED_5100);
                case 5101: return Optional.of(DIAMETER_ERROR_OPERATION_NOT_ALLOWED_5101);
                case 5102: return Optional.of(DIAMETER_ERROR_USER_DATA_CANNOT_BE_READ_5102);
                case 5103: return Optional.of(DIAMETER_ERROR_USER_DATA_CANNOT_BE_MODIFIED_5103);
                case 5104: return Optional.of(DIAMETER_ERROR_USER_DATA_CANNOT_BE_NOTIFIED_5104);
                case 5105: return Optional.of(DIAMETER_ERROR_TRANSPARENT_DATA_OUT_OF_SYNC_5105);
                case 5106: return Optional.of(DIAMETER_ERROR_SUBS_DATA_ABSENT_5106);
                case 5107: return Optional.of(DIAMETER_ERROR_NO_SUBSCRIPTION_TO_DATA_5107);
                case 5108: return Optional.of(DIAMETER_ERROR_DSAI_NOT_AVAILABLE_5108);
                case 5120: return Optional.of(DIAMETER_ERROR_START_INDICATION_5120);
                case 5121: return Optional.of(DIAMETER_ERROR_STOP_INDICATION_5121);
                case 5122: return Optional.of(DIAMETER_ERROR_UNKNOWN_MBMS_BEARER_SERVICE_5122);
                case 5123: return Optional.of(DIAMETER_ERROR_SERVICE_AREA_5123);
                case 5140: return Optional.of(DIAMETER_ERROR_INITIAL_PARAMETERS_5140);
                case 5141: return Optional.of(DIAMETER_ERROR_TRIGGER_EVENT_5141);
                case 5142: return Optional.of(DIAMETER_PCC_RULE_EVENT_5142);
                case 5143: return Optional.of(DIAMETER_ERROR_BEARER_NOT_AUTHORIZED_5143);
                case 5144: return Optional.of(DIAMETER_ERROR_TRAFFIC_MAPPING_INFO_REJECTED_5144);
                case 5145: return Optional.of(DIAMETER_QOS_RULE_EVENT_5145);
                case 5146: return Optional.of(DIAMETER_ERROR_TRAFFIC_MAPPING_INFO_REJECTED_5146);
                case 5147: return Optional.of(DIAMETER_ERROR_CONFLICTING_REQUEST_5147);
                case 5148: return Optional.of(DIAMETER_ADC_RULE_EVENT_5148);
                case 5149: return Optional.of(DIAMETER_ERROR_NBIFOM_NOT_AUTHORIZED_5149);
                case 5401: return Optional.of(DIAMETER_ERROR_IMPI_UNKNOWN_5401);
                case 5402: return Optional.of(DIAMETER_ERROR_NOT_AUTHORIZED_5402);
                case 5403: return Optional.of(DIAMETER_ERROR_TRANSACTION_IDENTIFIER_INVALID_5403);
                case 5420: return Optional.of(DIAMETER_ERROR_UNKNOWN_EPS_SUBSCRIPTION_5420);
                case 5421: return Optional.of(DIAMETER_ERROR_RAT_NOT_ALLOWED_5421);
                case 5422: return Optional.of(DIAMETER_ERROR_EQUIPMENT_UNKNOWN_5422);
                case 5423: return Optional.of(DIAMETER_ERROR_UNKNOWN_SERVING_NODE_5423);
                case 5450: return Optional.of(DIAMETER_ERROR_USER_NO_NON_3GPP_SUBSCRIPTION_5450);
                case 5451: return Optional.of(DIAMETER_ERROR_USER_NO_APN_SUBSCRIPTION_5451);
                case 5452: return Optional.of(DIAMETER_ERROR_RAT_TYPE_NOT_ALLOWED_5452);
                case 5453: return Optional.of(DIAMETER_ERROR_LATE_OVERLAPPING_REQUEST_5453);
                case 5454: return Optional.of(DIAMETER_ERROR_TIMED_OUT_REQUEST_5454);
                case 5470: return Optional.of(DIAMETER_ERROR_SUBSESSION_5470);
                case 5471: return Optional.of(DIAMETER_ERROR_ONGOING_SESSION_ESTABLISHMENT_5471);
                case 5490: return Optional.of(DIAMETER_ERROR_UNAUTHORIZED_REQUESTING_NETWORK_5490);
                case 5510: return Optional.of(DIAMETER_ERROR_UNAUTHORIZED_REQUESTING_ENTITY_5510);
                case 5511: return Optional.of(DIAMETER_ERROR_UNAUTHORIZED_SERVICE_5511);
                case 5512: return Optional.of(DIAMETER_ERROR_REQUESTED_RANGE_IS_NOT_ALLOWED_5512);
                case 5513: return Optional.of(DIAMETER_ERROR_CONFIGURATION_EVENT_STORAGE_NOT_SUCCESSFUL_5513);
                case 5514: return Optional.of(DIAMETER_ERROR_CONFIGURATION_EVENT_NON_EXISTANT_5514);
                case 5515: return Optional.of(DIAMETER_ERROR_SCEF_REFERENCE_ID_UNKNOWN_5515);
                case 5530: return Optional.of(DIAMETER_ERROR_INVALID_SME_ADDRESS_5530);
                case 5531: return Optional.of(DIAMETER_ERROR_SC_CONGESTION_5531);
                case 5532: return Optional.of(DIAMETER_ERROR_SM_PROTOCOL_5532);
                case 5533: return Optional.of(DIAMETER_ERROR_TRIGGER_REPLACE_FAILURE_5533);
                case 5534: return Optional.of(DIAMETER_ERROR_TRIGGER_RECALL_FAILURE_5534);
                case 5535: return Optional.of(DIAMETER_ERROR_ORIGINAL_MESSAGE_NOT_PENDING_5535);
                case 5550: return Optional.of(DIAMETER_ERROR_ABSENT_USER_5550);
                case 5551: return Optional.of(DIAMETER_ERROR_USER_BUSY_FOR_MT_SMS_5551);
                case 5552: return Optional.of(DIAMETER_ERROR_FACILITY_NOT_SUPPORTED_5552);
                case 5553: return Optional.of(DIAMETER_ERROR_ILLEGAL_USER_5553);
                case 5554: return Optional.of(DIAMETER_ERROR_ILLEGAL_EQUIPMENT_5554);
                case 5555: return Optional.of(DIAMETER_ERROR_SM_DELIVERY_FAILURE_5555);
                case 5556: return Optional.of(DIAMETER_ERROR_SERVICE_NOT_SUBSCRIBED_5556);
                case 5557: return Optional.of(DIAMETER_ERROR_SERVICE_BARRED_5557);
                case 5558: return Optional.of(DIAMETER_ERROR_MWD_LIST_FULL_5558);
                case 5570: return Optional.of(DIAMETER_ERROR_UNKNOWN_POLICY_COUNTERS_5570);
                case 5590: return Optional.of(DIAMETER_ERROR_ORIGIN_ALUID_UNKNOWN_5590);
                case 5591: return Optional.of(DIAMETER_ERROR_TARGET_ALUID_UNKNOWN_5591);
                case 5592: return Optional.of(DIAMETER_ERROR_PFID_UNKNOWN_5592);
                case 5593: return Optional.of(DIAMETER_ERROR_APP_REGISTER_REJECT_5593);
                case 5594: return Optional.of(DIAMETER_ERROR_PROSE_MAP_REQUEST_DISALLOWED_5594);
                case 5595: return Optional.of(DIAMETER_ERROR_MAP_REQUEST_REJECT_5595);
                case 5596: return Optional.of(DIAMETER_ERROR_REQUESTING_RPAUID_UNKNOWN_5596);
                case 5597: return Optional.of(DIAMETER_ERROR_UNKNOWN_OR_INVALID_TARGET_SET_5597);
                case 5598: return Optional.of(DIAMETER_ERROR_MISSING_APPLICATION_DATA_5598);
                case 5599: return Optional.of(DIAMETER_ERROR_AUTHORIZATION_REJECT_5599);
                case 5600: return Optional.of(DIAMETER_ERROR_DISCOVERY_NOT_PERMITTED_5600);
                case 5601: return Optional.of(DIAMETER_ERROR_TARGET_RPAUID_UNKNOWN_5601);
                case 5602: return Optional.of(DIAMETER_ERROR_INVALID_APPLICATION_DATA_5602);
                case 5610: return Optional.of(DIAMETER_ERROR_UNKNOWN_PROSE_SUBSCRIPTION_5610);
                case 5611: return Optional.of(PROSE_NOT_ALLOWED_5611);
                case 5612: return Optional.of(DIAMETER_ERROR_UE_LOCATION_UNKNOWN_5612);
                case 5630: return Optional.of(DIAMETER_ERROR_NO_ASSOCIATED_DISCOVERY_FILTER_5630);
                case 5631: return Optional.of(DIAMETER_ERROR_ANNOUNCING_UNAUTHORIZED_IN_PLMN_5631);
                case 5632: return Optional.of(DIAMETER_ERROR_INVALID_APPLICATION_CODE_5632);
                case 5633: return Optional.of(DIAMETER_ERROR_PROXIMITY_UNAUTHORIZED_5633);
                case 5634: return Optional.of(DIAMETER_ERROR_PROXIMITY_REJECTED_5634);
                case 5635: return Optional.of(DIAMETER_ERROR_NO_PROXIMITY_REQUEST_5635);
                case 5636: return Optional.of(DIAMETER_ERROR_UNAUTHORIZED_SERVICE_IN_THIS_PLMN_5636);
                case 5637: return Optional.of(DIAMETER_ERROR_PROXIMITY_CANCELLED_5637);
                case 5638: return Optional.of(DIAMETER_ERROR_INVALID_TARGET_PDUID_5638);
                case 5639: return Optional.of(DIAMETER_ERROR_INVALID_TARGET_RPAUID_5639);
                case 5640: return Optional.of(DIAMETER_ERROR_NO_ASSOCIATED_RESTRICTED_CODE_5640);
                case 5641: return Optional.of(DIAMETER_ERROR_INVALID_DISCOVERY_TYPE_5641);
                case 5650: return Optional.of(DIAMETER_ERROR_REQUESTED_LOCATION_NOT_SERVED_5650);
                case 5651: return Optional.of(DIAMETER_ERROR_INVALID_EPS_BEARER_5651);
                case 5652: return Optional.of(DIAMETER_ERROR_NIDD_CONFIGURATION_NOT_AVAILABLE_5652);
                case 5653: return Optional.of(DIAMETER_ERROR_USER_TEMPORARILY_UNREACHABLE_5653);
                case 5670: return Optional.of(DIAMETER_ERROR_UNKNKOWN_DATA_5670);
                case 5671: return Optional.of(DIAMETER_ERROR_REQUIRED_KEY_NOT_PROVIDED_5671);
                case 5690: return Optional.of(DIAMETER_ERROR_UNKNOWN_V2X_SUBSCRIPTION_5690);
                case 5691: return Optional.of(DIAMETER_ERROR_V2X_NOT_ALLOWED_5691);
                default:
                    return Optional.empty();
            }
        }
    }

    default Optional<Code> getAsEnum() {
        return getValue().getAsEnum();
    }

    static ExperimentalResultCode parse(final FramedAvp raw) {
        if (CODE != raw.getCode()) {
            throw new AvpParseException("AVP Code mismatch - unable to parse the AVP into a " + ExperimentalResultCode.class.getName());
        }
        final Buffer data = raw.getData();
        final int value = data.getInt(0);
        final Optional<Code> e = Code.lookup(value);
        final EnumeratedHolder holder = new EnumeratedHolder(value, e);
        return new DefaultExperimentalResultCode(raw, holder);
    }

    class DefaultExperimentalResultCode extends DiameterEnumeratedAvp<Code> implements ExperimentalResultCode {
        private DefaultExperimentalResultCode(final FramedAvp raw, final EnumeratedHolder value) {
            super(raw, value);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final DefaultExperimentalResultCode that = (DefaultExperimentalResultCode) o;
            return getValue().equals(that.getValue());
        }

        @Override
        public int hashCode() {
            return getValue().hashCode();
        }
    }

    /**
     * Ah! Must be a better way. I ran out of steam - getting late so it is what it is.
     */
    class EnumeratedHolder implements Enumerated<Code> {

        private final int code;
        private final Optional<Code> e;

        private EnumeratedHolder(final int code, final Optional<Code> e) {
            this.code = code;
            this.e = e;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final EnumeratedHolder that = (EnumeratedHolder) o;

            if (code != that.code) return false;
            return e.equals(that.e);
        }

        @Override
        public int hashCode() {
            int result = code;
            result = 31 * result + e.hashCode();
            return result;
        }

        @Override
        public Optional<Code> getAsEnum() {
            return e;
        }

        @Override
        public int getValue() {
            return code;
        }
    }

}
