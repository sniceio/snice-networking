package io.snice.networking.codec.diameter.avp.api;

import io.snice.buffer.Buffer;
import io.snice.buffer.WritableBuffer;

import io.snice.networking.codec.diameter.avp.Avp;
import io.snice.networking.codec.diameter.avp.AvpMandatory;
import io.snice.networking.codec.diameter.avp.AvpParseException;
import io.snice.networking.codec.diameter.avp.AvpProtected;
import io.snice.networking.codec.diameter.avp.FramedAvp;
import io.snice.networking.codec.diameter.avp.Vendor;

import io.snice.networking.codec.diameter.avp.impl.DiameterEnumeratedAvp;
import io.snice.networking.codec.diameter.avp.type.Enumerated;

import java.util.Optional;
import static io.snice.preconditions.PreConditions.assertNotNull;

/**
 * 
 */
public interface ResultCode extends Avp<Enumerated<ResultCode.Code>> {

    int CODE = 268;
    
    ResultCode DiameterMultiRoundAuth1001 = ResultCode.of(1001);
    ResultCode DiameterSuccess2001 = ResultCode.of(2001);
    ResultCode DiameterLimitedSuccess2002 = ResultCode.of(2002);
    ResultCode DiameterFirstRegistration2003 = ResultCode.of(2003);
    ResultCode DiameterSubsequentRegistration2004 = ResultCode.of(2004);
    ResultCode DiameterUnregisteredService2005 = ResultCode.of(2005);
    ResultCode DiameterSuccessServerNameNotStored2006 = ResultCode.of(2006);
    ResultCode DiameterServerSelection2007 = ResultCode.of(2007);
    ResultCode DiameterSuccessAuthSentServerNotStored2008 = ResultCode.of(2008);
    ResultCode DiameterSuccessRelocateHa2009 = ResultCode.of(2009);
    ResultCode DiameterCommandUnsupported3001 = ResultCode.of(3001);
    ResultCode DiameterUnableToDeliver3002 = ResultCode.of(3002);
    ResultCode DiameterRealmNotServed3003 = ResultCode.of(3003);
    ResultCode DiameterTooBusy3004 = ResultCode.of(3004);
    ResultCode DiameterLoopDetected3005 = ResultCode.of(3005);
    ResultCode DiameterRedirectIndication3006 = ResultCode.of(3006);
    ResultCode DiameterApplicationUnsupported3007 = ResultCode.of(3007);
    ResultCode DiameterInvalidHdrBits3008 = ResultCode.of(3008);
    ResultCode DiameterInvalidAvpBits3009 = ResultCode.of(3009);
    ResultCode DiameterUnknownPeer3010 = ResultCode.of(3010);
    ResultCode DiameterRealmRedirectIndication3011 = ResultCode.of(3011);
    ResultCode DiameterAuthenticationRejected4001 = ResultCode.of(4001);
    ResultCode DiameterOutOfSpace4002 = ResultCode.of(4002);
    ResultCode DiameterElectionLost4003 = ResultCode.of(4003);
    ResultCode DiameterErrorMipReplyFailure4005 = ResultCode.of(4005);
    ResultCode DiameterErrorHaNotAvailable4006 = ResultCode.of(4006);
    ResultCode DiameterErrorBadKey4007 = ResultCode.of(4007);
    ResultCode DiameterErrorMipFilterNotSupported4008 = ResultCode.of(4008);
    ResultCode DiameterEndUserServiceDenied4010 = ResultCode.of(4010);
    ResultCode DiameterCreditControlNotApplicable4011 = ResultCode.of(4011);
    ResultCode DiameterCreditLimitReached4012 = ResultCode.of(4012);
    ResultCode DiameterUserNameRequired4013 = ResultCode.of(4013);
    ResultCode ResourceFailure4014 = ResultCode.of(4014);
    ResultCode DiameterEndUserServiceDenied4241 = ResultCode.of(4241);
    ResultCode DiameterAvpUnsupported5001 = ResultCode.of(5001);
    ResultCode DiameterUnknownSessionId5002 = ResultCode.of(5002);
    ResultCode DiameterAuthorizationRejected5003 = ResultCode.of(5003);
    ResultCode DiameterInvalidAvpValue5004 = ResultCode.of(5004);
    ResultCode DiameterMissingAvp5005 = ResultCode.of(5005);
    ResultCode DiameterResourcesExceeded5006 = ResultCode.of(5006);
    ResultCode DiameterContradictingAvps5007 = ResultCode.of(5007);
    ResultCode DiameterAvpNotAllowed5008 = ResultCode.of(5008);
    ResultCode DiameterAvpOccursTooManyTimes5009 = ResultCode.of(5009);
    ResultCode DiameterNoCommonApplication5010 = ResultCode.of(5010);
    ResultCode DiameterUnsupportedVersion5011 = ResultCode.of(5011);
    ResultCode DiameterUnableToComply5012 = ResultCode.of(5012);
    ResultCode DiameterInvalidBitInHeader5013 = ResultCode.of(5013);
    ResultCode DiameterInvalidAvpLength5014 = ResultCode.of(5014);
    ResultCode DiameterInvalidMessageLength5015 = ResultCode.of(5015);
    ResultCode DiameterInvalidAvpBitCombo5016 = ResultCode.of(5016);
    ResultCode DiameterNoCommonSecurity5017 = ResultCode.of(5017);
    ResultCode DiameterRadiusAvpUntranslatable5018 = ResultCode.of(5018);
    ResultCode DiameterErrorNoForeignHaService5024 = ResultCode.of(5024);
    ResultCode DiameterErrorEndToEndMipKeyEncryption5025 = ResultCode.of(5025);
    ResultCode DiameterUserUnknown5030 = ResultCode.of(5030);
    ResultCode DiameterRatingFailed5031 = ResultCode.of(5031);
    ResultCode DiameterErrorUserUnknown5032 = ResultCode.of(5032);
    ResultCode DiameterErrorIdentitiesDontMatch5033 = ResultCode.of(5033);
    ResultCode DiameterErrorIdentityNotRegistered5034 = ResultCode.of(5034);
    ResultCode DiameterErrorRoamingNotAllowed5035 = ResultCode.of(5035);
    ResultCode DiameterErrorIdentityAlreadyRegistered5036 = ResultCode.of(5036);
    ResultCode DiameterErrorAuthSchemeNotSupported5037 = ResultCode.of(5037);
    ResultCode DiameterErrorInAssignmentType5038 = ResultCode.of(5038);
    ResultCode DiameterErrorTooMuchData5039 = ResultCode.of(5039);
    ResultCode DiameterErrorNotSupportedUserData5040 = ResultCode.of(5040);
    ResultCode DiameterErrorMip6AuthMode5041 = ResultCode.of(5041);
    ResultCode UnknownBindingTemplateName5042 = ResultCode.of(5042);
    ResultCode BindingFailure5043 = ResultCode.of(5043);
    ResultCode MaxBindingsSetFailure5044 = ResultCode.of(5044);
    ResultCode MaximumBindingsReachedForEndpoint5045 = ResultCode.of(5045);
    ResultCode SessionExists5046 = ResultCode.of(5046);
    ResultCode InsufficientClassifiers5047 = ResultCode.of(5047);
    ResultCode DiameterErrorEapCodeUnknown5048 = ResultCode.of(5048);
    ResultCode DiameterEndUserNotFound5241 = ResultCode.of(5241);

    @Override
    default long getCode() {
        return CODE;
    }

    default ResultCode toResultCode() {
        return this;
    }

    default boolean isResultCode() {
        return true;
    }

    @Override
    default void writeValue(final WritableBuffer buffer) {
        buffer.write(getValue().getValue());
    }

    static ResultCode of(final int code) {
        final Optional<Code> c = Code.lookup(code);
        final EnumeratedHolder enumerated = new EnumeratedHolder(code, c);
        final Avp<Enumerated> avp = Avp.ofType(Enumerated.class)
                .withValue(enumerated)
                .withAvpCode(CODE)
                .isMandatory(AvpMandatory.MUST.isMandatory())
                .isProtected(AvpProtected.MUST_NOT.isProtected())
                .withVendor(Vendor.NONE)
                .build();
        return new DefaultResultCode(avp, enumerated);
    }

    enum Code { 
        DIAMETER_MULTI_ROUND_AUTH_1001("DIAMETER_MULTI_ROUND_AUTH", 1001),
        DIAMETER_SUCCESS_2001("DIAMETER_SUCCESS", 2001),
        DIAMETER_LIMITED_SUCCESS_2002("DIAMETER_LIMITED_SUCCESS", 2002),
        DIAMETER_FIRST_REGISTRATION_2003("DIAMETER_FIRST_REGISTRATION", 2003),
        DIAMETER_SUBSEQUENT_REGISTRATION_2004("DIAMETER_SUBSEQUENT_REGISTRATION", 2004),
        DIAMETER_UNREGISTERED_SERVICE_2005("DIAMETER_UNREGISTERED_SERVICE", 2005),
        DIAMETER_SUCCESS_SERVER_NAME_NOT_STORED_2006("DIAMETER_SUCCESS_SERVER_NAME_NOT_STORED", 2006),
        DIAMETER_SERVER_SELECTION_2007("DIAMETER_SERVER_SELECTION", 2007),
        DIAMETER_SUCCESS_AUTH_SENT_SERVER_NOT_STORED_2008("DIAMETER_SUCCESS_AUTH_SENT_SERVER_NOT_STORED", 2008),
        DIAMETER_SUCCESS_RELOCATE_HA_2009("DIAMETER_SUCCESS_RELOCATE_HA", 2009),
        DIAMETER_COMMAND_UNSUPPORTED_3001("DIAMETER_COMMAND_UNSUPPORTED", 3001),
        DIAMETER_UNABLE_TO_DELIVER_3002("DIAMETER_UNABLE_TO_DELIVER", 3002),
        DIAMETER_REALM_NOT_SERVED_3003("DIAMETER_REALM_NOT_SERVED", 3003),
        DIAMETER_TOO_BUSY_3004("DIAMETER_TOO_BUSY", 3004),
        DIAMETER_LOOP_DETECTED_3005("DIAMETER_LOOP_DETECTED", 3005),
        DIAMETER_REDIRECT_INDICATION_3006("DIAMETER_REDIRECT_INDICATION", 3006),
        DIAMETER_APPLICATION_UNSUPPORTED_3007("DIAMETER_APPLICATION_UNSUPPORTED", 3007),
        DIAMETER_INVALID_HDR_BITS_3008("DIAMETER_INVALID_HDR_BITS", 3008),
        DIAMETER_INVALID_AVP_BITS_3009("DIAMETER_INVALID_AVP_BITS", 3009),
        DIAMETER_UNKNOWN_PEER_3010("DIAMETER_UNKNOWN_PEER", 3010),
        DIAMETER_REALM_REDIRECT_INDICATION_3011("DIAMETER_REALM_REDIRECT_INDICATION", 3011),
        DIAMETER_AUTHENTICATION_REJECTED_4001("DIAMETER_AUTHENTICATION_REJECTED", 4001),
        DIAMETER_OUT_OF_SPACE_4002("DIAMETER_OUT_OF_SPACE", 4002),
        DIAMETER_ELECTION_LOST_4003("DIAMETER_ELECTION_LOST", 4003),
        DIAMETER_ERROR_MIP_REPLY_FAILURE_4005("DIAMETER_ERROR_MIP_REPLY_FAILURE", 4005),
        DIAMETER_ERROR_HA_NOT_AVAILABLE_4006("DIAMETER_ERROR_HA_NOT_AVAILABLE", 4006),
        DIAMETER_ERROR_BAD_KEY_4007("DIAMETER_ERROR_BAD_KEY", 4007),
        DIAMETER_ERROR_MIP_FILTER_NOT_SUPPORTED_4008("DIAMETER_ERROR_MIP_FILTER_NOT_SUPPORTED", 4008),
        DIAMETER_END_USER_SERVICE_DENIED_4010("DIAMETER_END_USER_SERVICE_DENIED", 4010),
        DIAMETER_CREDIT_CONTROL_NOT_APPLICABLE_4011("DIAMETER_CREDIT_CONTROL_NOT_APPLICABLE", 4011),
        DIAMETER_CREDIT_LIMIT_REACHED_4012("DIAMETER_CREDIT_LIMIT_REACHED", 4012),
        DIAMETER_USER_NAME_REQUIRED_4013("DIAMETER_USER_NAME_REQUIRED", 4013),
        RESOURCE_FAILURE_4014("RESOURCE_FAILURE", 4014),
        DIAMETER_END_USER_SERVICE_DENIED_4241("DIAMETER_END_USER_SERVICE_DENIED", 4241),
        DIAMETER_AVP_UNSUPPORTED_5001("DIAMETER_AVP_UNSUPPORTED", 5001),
        DIAMETER_UNKNOWN_SESSION_ID_5002("DIAMETER_UNKNOWN_SESSION_ID", 5002),
        DIAMETER_AUTHORIZATION_REJECTED_5003("DIAMETER_AUTHORIZATION_REJECTED", 5003),
        DIAMETER_INVALID_AVP_VALUE_5004("DIAMETER_INVALID_AVP_VALUE", 5004),
        DIAMETER_MISSING_AVP_5005("DIAMETER_MISSING_AVP", 5005),
        DIAMETER_RESOURCES_EXCEEDED_5006("DIAMETER_RESOURCES_EXCEEDED", 5006),
        DIAMETER_CONTRADICTING_AVPS_5007("DIAMETER_CONTRADICTING_AVPS", 5007),
        DIAMETER_AVP_NOT_ALLOWED_5008("DIAMETER_AVP_NOT_ALLOWED", 5008),
        DIAMETER_AVP_OCCURS_TOO_MANY_TIMES_5009("DIAMETER_AVP_OCCURS_TOO_MANY_TIMES", 5009),
        DIAMETER_NO_COMMON_APPLICATION_5010("DIAMETER_NO_COMMON_APPLICATION", 5010),
        DIAMETER_UNSUPPORTED_VERSION_5011("DIAMETER_UNSUPPORTED_VERSION", 5011),
        DIAMETER_UNABLE_TO_COMPLY_5012("DIAMETER_UNABLE_TO_COMPLY", 5012),
        DIAMETER_INVALID_BIT_IN_HEADER_5013("DIAMETER_INVALID_BIT_IN_HEADER", 5013),
        DIAMETER_INVALID_AVP_LENGTH_5014("DIAMETER_INVALID_AVP_LENGTH", 5014),
        DIAMETER_INVALID_MESSAGE_LENGTH_5015("DIAMETER_INVALID_MESSAGE_LENGTH", 5015),
        DIAMETER_INVALID_AVP_BIT_COMBO_5016("DIAMETER_INVALID_AVP_BIT_COMBO", 5016),
        DIAMETER_NO_COMMON_SECURITY_5017("DIAMETER_NO_COMMON_SECURITY", 5017),
        DIAMETER_RADIUS_AVP_UNTRANSLATABLE_5018("DIAMETER_RADIUS_AVP_UNTRANSLATABLE", 5018),
        DIAMETER_ERROR_NO_FOREIGN_HA_SERVICE_5024("DIAMETER_ERROR_NO_FOREIGN_HA_SERVICE", 5024),
        DIAMETER_ERROR_END_TO_END_MIP_KEY_ENCRYPTION_5025("DIAMETER_ERROR_END_TO_END_MIP_KEY_ENCRYPTION", 5025),
        DIAMETER_USER_UNKNOWN_5030("DIAMETER_USER_UNKNOWN", 5030),
        DIAMETER_RATING_FAILED_5031("DIAMETER_RATING_FAILED", 5031),
        DIAMETER_ERROR_USER_UNKNOWN_5032("DIAMETER_ERROR_USER_UNKNOWN", 5032),
        DIAMETER_ERROR_IDENTITIES_DONT_MATCH_5033("DIAMETER_ERROR_IDENTITIES_DONT_MATCH", 5033),
        DIAMETER_ERROR_IDENTITY_NOT_REGISTERED_5034("DIAMETER_ERROR_IDENTITY_NOT_REGISTERED", 5034),
        DIAMETER_ERROR_ROAMING_NOT_ALLOWED_5035("DIAMETER_ERROR_ROAMING_NOT_ALLOWED", 5035),
        DIAMETER_ERROR_IDENTITY_ALREADY_REGISTERED_5036("DIAMETER_ERROR_IDENTITY_ALREADY_REGISTERED", 5036),
        DIAMETER_ERROR_AUTH_SCHEME_NOT_SUPPORTED_5037("DIAMETER_ERROR_AUTH_SCHEME_NOT_SUPPORTED", 5037),
        DIAMETER_ERROR_IN_ASSIGNMENT_TYPE_5038("DIAMETER_ERROR_IN_ASSIGNMENT_TYPE", 5038),
        DIAMETER_ERROR_TOO_MUCH_DATA_5039("DIAMETER_ERROR_TOO_MUCH_DATA", 5039),
        DIAMETER_ERROR_NOT_SUPPORTED_USER_DATA_5040("DIAMETER_ERROR_NOT_SUPPORTED_USER_DATA", 5040),
        DIAMETER_ERROR_MIP6_AUTH_MODE_5041("DIAMETER_ERROR_MIP6_AUTH_MODE", 5041),
        UNKNOWN_BINDING_TEMPLATE_NAME_5042("UNKNOWN_BINDING_TEMPLATE_NAME", 5042),
        BINDING_FAILURE_5043("BINDING_FAILURE", 5043),
        MAX_BINDINGS_SET_FAILURE_5044("MAX_BINDINGS_SET_FAILURE", 5044),
        MAXIMUM_BINDINGS_REACHED_FOR_ENDPOINT_5045("MAXIMUM_BINDINGS_REACHED_FOR_ENDPOINT", 5045),
        SESSION_EXISTS_5046("SESSION_EXISTS", 5046),
        INSUFFICIENT_CLASSIFIERS_5047("INSUFFICIENT_CLASSIFIERS", 5047),
        DIAMETER_ERROR_EAP_CODE_UNKNOWN_5048("DIAMETER_ERROR_EAP_CODE_UNKNOWN", 5048),
        DIAMETER_END_USER_NOT_FOUND_5241("DIAMETER_END_USER_NOT_FOUND", 5241);

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
                case 1001: return Optional.of(DIAMETER_MULTI_ROUND_AUTH_1001);
                case 2001: return Optional.of(DIAMETER_SUCCESS_2001);
                case 2002: return Optional.of(DIAMETER_LIMITED_SUCCESS_2002);
                case 2003: return Optional.of(DIAMETER_FIRST_REGISTRATION_2003);
                case 2004: return Optional.of(DIAMETER_SUBSEQUENT_REGISTRATION_2004);
                case 2005: return Optional.of(DIAMETER_UNREGISTERED_SERVICE_2005);
                case 2006: return Optional.of(DIAMETER_SUCCESS_SERVER_NAME_NOT_STORED_2006);
                case 2007: return Optional.of(DIAMETER_SERVER_SELECTION_2007);
                case 2008: return Optional.of(DIAMETER_SUCCESS_AUTH_SENT_SERVER_NOT_STORED_2008);
                case 2009: return Optional.of(DIAMETER_SUCCESS_RELOCATE_HA_2009);
                case 3001: return Optional.of(DIAMETER_COMMAND_UNSUPPORTED_3001);
                case 3002: return Optional.of(DIAMETER_UNABLE_TO_DELIVER_3002);
                case 3003: return Optional.of(DIAMETER_REALM_NOT_SERVED_3003);
                case 3004: return Optional.of(DIAMETER_TOO_BUSY_3004);
                case 3005: return Optional.of(DIAMETER_LOOP_DETECTED_3005);
                case 3006: return Optional.of(DIAMETER_REDIRECT_INDICATION_3006);
                case 3007: return Optional.of(DIAMETER_APPLICATION_UNSUPPORTED_3007);
                case 3008: return Optional.of(DIAMETER_INVALID_HDR_BITS_3008);
                case 3009: return Optional.of(DIAMETER_INVALID_AVP_BITS_3009);
                case 3010: return Optional.of(DIAMETER_UNKNOWN_PEER_3010);
                case 3011: return Optional.of(DIAMETER_REALM_REDIRECT_INDICATION_3011);
                case 4001: return Optional.of(DIAMETER_AUTHENTICATION_REJECTED_4001);
                case 4002: return Optional.of(DIAMETER_OUT_OF_SPACE_4002);
                case 4003: return Optional.of(DIAMETER_ELECTION_LOST_4003);
                case 4005: return Optional.of(DIAMETER_ERROR_MIP_REPLY_FAILURE_4005);
                case 4006: return Optional.of(DIAMETER_ERROR_HA_NOT_AVAILABLE_4006);
                case 4007: return Optional.of(DIAMETER_ERROR_BAD_KEY_4007);
                case 4008: return Optional.of(DIAMETER_ERROR_MIP_FILTER_NOT_SUPPORTED_4008);
                case 4010: return Optional.of(DIAMETER_END_USER_SERVICE_DENIED_4010);
                case 4011: return Optional.of(DIAMETER_CREDIT_CONTROL_NOT_APPLICABLE_4011);
                case 4012: return Optional.of(DIAMETER_CREDIT_LIMIT_REACHED_4012);
                case 4013: return Optional.of(DIAMETER_USER_NAME_REQUIRED_4013);
                case 4014: return Optional.of(RESOURCE_FAILURE_4014);
                case 4241: return Optional.of(DIAMETER_END_USER_SERVICE_DENIED_4241);
                case 5001: return Optional.of(DIAMETER_AVP_UNSUPPORTED_5001);
                case 5002: return Optional.of(DIAMETER_UNKNOWN_SESSION_ID_5002);
                case 5003: return Optional.of(DIAMETER_AUTHORIZATION_REJECTED_5003);
                case 5004: return Optional.of(DIAMETER_INVALID_AVP_VALUE_5004);
                case 5005: return Optional.of(DIAMETER_MISSING_AVP_5005);
                case 5006: return Optional.of(DIAMETER_RESOURCES_EXCEEDED_5006);
                case 5007: return Optional.of(DIAMETER_CONTRADICTING_AVPS_5007);
                case 5008: return Optional.of(DIAMETER_AVP_NOT_ALLOWED_5008);
                case 5009: return Optional.of(DIAMETER_AVP_OCCURS_TOO_MANY_TIMES_5009);
                case 5010: return Optional.of(DIAMETER_NO_COMMON_APPLICATION_5010);
                case 5011: return Optional.of(DIAMETER_UNSUPPORTED_VERSION_5011);
                case 5012: return Optional.of(DIAMETER_UNABLE_TO_COMPLY_5012);
                case 5013: return Optional.of(DIAMETER_INVALID_BIT_IN_HEADER_5013);
                case 5014: return Optional.of(DIAMETER_INVALID_AVP_LENGTH_5014);
                case 5015: return Optional.of(DIAMETER_INVALID_MESSAGE_LENGTH_5015);
                case 5016: return Optional.of(DIAMETER_INVALID_AVP_BIT_COMBO_5016);
                case 5017: return Optional.of(DIAMETER_NO_COMMON_SECURITY_5017);
                case 5018: return Optional.of(DIAMETER_RADIUS_AVP_UNTRANSLATABLE_5018);
                case 5024: return Optional.of(DIAMETER_ERROR_NO_FOREIGN_HA_SERVICE_5024);
                case 5025: return Optional.of(DIAMETER_ERROR_END_TO_END_MIP_KEY_ENCRYPTION_5025);
                case 5030: return Optional.of(DIAMETER_USER_UNKNOWN_5030);
                case 5031: return Optional.of(DIAMETER_RATING_FAILED_5031);
                case 5032: return Optional.of(DIAMETER_ERROR_USER_UNKNOWN_5032);
                case 5033: return Optional.of(DIAMETER_ERROR_IDENTITIES_DONT_MATCH_5033);
                case 5034: return Optional.of(DIAMETER_ERROR_IDENTITY_NOT_REGISTERED_5034);
                case 5035: return Optional.of(DIAMETER_ERROR_ROAMING_NOT_ALLOWED_5035);
                case 5036: return Optional.of(DIAMETER_ERROR_IDENTITY_ALREADY_REGISTERED_5036);
                case 5037: return Optional.of(DIAMETER_ERROR_AUTH_SCHEME_NOT_SUPPORTED_5037);
                case 5038: return Optional.of(DIAMETER_ERROR_IN_ASSIGNMENT_TYPE_5038);
                case 5039: return Optional.of(DIAMETER_ERROR_TOO_MUCH_DATA_5039);
                case 5040: return Optional.of(DIAMETER_ERROR_NOT_SUPPORTED_USER_DATA_5040);
                case 5041: return Optional.of(DIAMETER_ERROR_MIP6_AUTH_MODE_5041);
                case 5042: return Optional.of(UNKNOWN_BINDING_TEMPLATE_NAME_5042);
                case 5043: return Optional.of(BINDING_FAILURE_5043);
                case 5044: return Optional.of(MAX_BINDINGS_SET_FAILURE_5044);
                case 5045: return Optional.of(MAXIMUM_BINDINGS_REACHED_FOR_ENDPOINT_5045);
                case 5046: return Optional.of(SESSION_EXISTS_5046);
                case 5047: return Optional.of(INSUFFICIENT_CLASSIFIERS_5047);
                case 5048: return Optional.of(DIAMETER_ERROR_EAP_CODE_UNKNOWN_5048);
                case 5241: return Optional.of(DIAMETER_END_USER_NOT_FOUND_5241);
                default:
                    return Optional.empty();
            }
        }
    }

    default Optional<Code> getAsEnum() {
        return getValue().getAsEnum();
    }

    static ResultCode parse(final FramedAvp raw) {
        if (CODE != raw.getCode()) {
            throw new AvpParseException("AVP Code mismatch - unable to parse the AVP into a " + ResultCode.class.getName());
        }
        final Buffer data = raw.getData();
        final int value = data.getInt(0);
        final Optional<Code> e = Code.lookup(value);
        final EnumeratedHolder holder = new EnumeratedHolder(value, e);
        return new DefaultResultCode(raw, holder);
    }

    class DefaultResultCode extends DiameterEnumeratedAvp<Code> implements ResultCode {
        private DefaultResultCode(final FramedAvp raw, final EnumeratedHolder value) {
            super(raw, value);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final DefaultResultCode that = (DefaultResultCode) o;
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
