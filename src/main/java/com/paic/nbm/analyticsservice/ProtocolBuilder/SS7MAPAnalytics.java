package com.paic.nbm.analyticsservice.ProtocolBuilder;

import com.paic.nbm.analyticsservice.Entities.ConsolidatedDiagram;
import com.paic.nbm.analyticsservice.Entities.SS7MapData;
import com.paic.nbm.analyticsservice.Service.SS7MapService;
import com.paic.nbm.analyticsservice.Utils.UtilityFunctions;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.paic.nbm.analyticsservice.AnalyticsServiceApplication._ipNames;

@Slf4j
@Component("SS7MAP")
public class SS7MAPAnalytics {
    @Autowired
    SS7MapService ss7MapService;

    @Getter
    @Setter
    private String timezone;

    public HashMap<String,Object> buildSS7MAPGroupingCondition(String startDate, String endDate, String timezone, List<String> imsi, List<String> msisdn, String sourceNumber, String destinationNumber) {
        HashMap<String,Object> filterGroupingCondition = new HashMap<>();
        filterGroupingCondition.put("startDate", startDate);
        filterGroupingCondition.put("endDate", endDate);
        if (!imsi.isEmpty())
            filterGroupingCondition.put("imsi", imsi);
        if (!msisdn.isEmpty())
            filterGroupingCondition.put("msisdn", msisdn);

        log.info("Grouping condition for SS7MAP");

        return filterGroupingCondition;
    }

    private String getFlowDiagramBuilderType(int gsm_component, int gsm_op_code) {
        String result = "";
        switch (gsm_component) {
            case 1:
                switch (gsm_op_code) {
                    case 2:
                        result =  "invoke updateLocation";
                        break;
                    case 3:
                        result =  "invoke cancelLocation";
                        break;
                    case 4:
                        result =  "invoke provideRoamingNumber";
                        break;
                    case 5:
                        result =  "invoke noteSubscriberDataModified";
                        break;
                    case 6:
                        result =  "invoke resumeCallHandling";
                        break;
                    case 7:
                        result =  "invoke insertSubscriberData";
                        break;
                    case 8:
                        result =  "invoke deleteSubscriberData";
                        break;
                    case 9:
                        result =  "invoke sendParameters";
                        break;
                    case 10:
                        result =  "invoke registerSS";
                        break;
                    case 11:
                        result =  "invoke eraseSS";
                        break;
                    case 12:
                        result =  "invoke activateSS";
                        break;
                    case 13:
                        result =  "invoke deactivateSS";
                        break;
                    case 14:
                        result =  "invoke interrogateSS";
                        break;
                    case 15:
                        result =  "invoke authenticationFailureReport";
                        break;
                    case 16:
                        result =  "invoke SS-protocol notifySS";
                        break;
                    case 17:
                        result =  "invoke registerPassword";
                        break;
                    case 18:
                        result =  "invoke getPassword";
                        break;
                    case 19:
                        result =  "invoke SS-Protocol processUnstructuredSS-Data  (19)";
                        break;
                    case 20:
                        result =  "invoke releaseResources";
                        break;
                    case 21:
                        result =  "invoke mt-ForwardSM-VGCS";
                        break;
                    case 22:
                        result =  "invoke sendRoutingInfo";
                        break;
                    case 23:
                        result =  "invoke updateGprsLocation";
                        break;
                    case 24:
                        result =  "invoke sendRoutingInfoForGprs";
                        break;
                    case 25:
                        result =  "invoke failureReport";
                        break;
                    case 26:
                        result =  "invoke noteMsPresentForGprs";
                        break;
                    case 29:
                        result =  "invoke sendEndSignal";
                        break;
                    case 31:
                        result =  "invoke provideSIWFSNumber";
                        break;
                    case 32:
                        result =  "invoke sIWFSSignallingModify";
                        break;
                    case 33:
                        result =  "invoke processAccessSignalling";
                        break;
                    case 34:
                        result =  "invoke forwardAccessSignalling";
                        break;
                    case 36:
                        result =  "invoke cancelVcsgLocation";
                        break;
                    case 37:
                        result =  "invoke reset";
                        break;
                    case 38:
                        result =  "invoke forwardCheckSS-Indication";
                        break;
                    case 39:
                        result =  "invoke prepareGroupCall";
                        break;
                    case 40:
                        result =  "invoke sendGroupCallEndSignal";
                        break;
                    case 41:
                        result =  "invoke processGroupCallSignalling";
                        break;
                    case 42:
                        result =  "invoke forwardGroupCallSignalling";
                        break;
                    case 43:
                        result =  "invoke checkIMEI";
                        break;
                    case 44:
                        result =  "invoke ForwardSM";
                        break;
                    case 45:
                        result =  "invoke sendRoutingInfoForSM";
                        break;
                    case 46:
                        result =  "invoke ForwardSM";
                        break;
                    case 47:
                        result =  "invoke reportSM-DeliveryStatus";
                        break;
                    case 48:
                        result =  "invoke noteSubscriberPresent";
                        break;
                    case 50:
                        result =  "invoke activateTraceMode";
                        break;
                    case 51:
                        result =  "invoke deactivateTraceMode";
                        break;
                    case 53:
                        result =  "invoke UpdateVcsgLocation 53";
                        break;
                    case 54:
                        result =  "invoke beginSubscriberActivity";
                        break;
                    case 55:
                        result =  "invoke sendIdentification";
                        break;
                    case 56:
                        result =  "invoke sendAuthenticationInfo";
                        break;
                    case 57:
                        result =  "invoke restoreData";
                        break;
                    case 58:
                        result =  "invoke sendIMSI";
                        break;
                    case 59:
                        result =  "invoke processUnstructuredSS-Request";
                        break;
                    case 60:
                        result =  "invoke unstructuredSS-Request";
                        break;
                    case 61:
                        result =  "invoke unstructuredSS-Notify";
                        break;
                    case 62:
                        result =  "invoke AnyTimeSubscriptionInterrogation";
                        break;
                    case 63:
                        result =  "invoke informServiceCentre";
                        break;
                    case 64:
                        result =  "invoke alertServiceCentre";
                        break;
                    case 65:
                        result =  "invoke AnyTimeModification";
                        break;
                    case 66:
                        result =  "invoke readyForSM";
                        break;
                    case 67:
                        result =  "invoke purgeMS";
                        break;
                    case 68:
                        result =  "invoke prepareHandover";
                        break;
                    case 69:
                        result =  "invoke prepareSubsequentHandover";
                        break;
                    case 70:
                        result =  "invoke provideSubscriberInfo";
                        break;
                    case 71:
                        result =  "invoke anyTimeInterrogation";
                        break;
                    case 72:
                        result =  "invoke ss-InvocationNotification";
                        break;
                    case 73:
                        result =  "invoke setReportingState";
                        break;
                    case 74:
                        result =  "invoke statusReport";
                        break;
                    case 75:
                        result =  "invoke remoteUserFree";
                        break;
                    case 76:
                        result =  "invoke registerCC-Entry";
                        break;
                    case 77:
                        result =  "invoke eraseCC-Entry";
                        break;
                    case 78:
                    case 79:
                    case 80:
                    case 81:
                        result =  "invoke secureTransportClass1" ;
                        break;
                    case 83:
                        result =  "invoke provideSubscriberLocation";
                        break;
                    case 84:
                        result =  "invoke sendGroupCallInfo";
                        break;
                    case 85:
                        result =  "invoke sendRoutingInfoForLCS";
                        break;
                    case 86:
                        result =  "invoke subscriberLocationReport";
                        break;
                    case 87:
                        result =  "invoke ist-Alert";
                        break;
                    case 88:
                        result =  "invoke ist-Command";
                        break;
                    case 89:
                        result =  "invoke noteMM-Event";
                        break;
                    case 108:
                        result =  "invoke SS-protocol lcs-PeriodicTriggeredInvoke";
                        break;
                    case 109:
                        result =  "invoke SS-protocol lcs-PeriodicLocationCancellation";
                        break;
                    case 110:
                        result =  "invoke SS-protocol lcs-LocationUpdate";
                        break;
                    case 111:
                        result =  "invoke SS-protocol lcs-PeriodicLocationRequest";
                        break;
                    case 112:
                        result =  "invoke SS-protocol lcs-AreaEventCancellation";
                        break;
                    case 113:
                        result =  "invoke SS-protocol lcs-AreaEventReport";
                        break;
                    case 114:
                        result =  "invoke SS-protocol lcs-AreaEventRequest";
                        break;
                    case 115:
                        result =  "invoke SS-protocol lcs-MOLR";
                        break;
                    case 116:
                        result =  "invoke SS-protocol lcs-LocationNotification";
                        break;
                    case 117:
                        result =  "invoke SS-protocol callDeflection";
                        break;
                    case 118:
                        result =  "invoke SS-protocol userUserService";
                        break;
                    case 119:
                        result =  "invoke SS-protocol accessRegisterCCEntry";
                        break;
                    case 120:
                        result =  "invoke SS-protocol forwardCUG-Info";
                        break;
                    case 121:
                        result =  "invoke SS-protocol splitMPTY no Argument";
                        break;
                    case 122:
                        result =  "invoke SS-protocol retrieveMPTY no Argument";
                        break;
                    case 123:
                        result =  "invoke SS-protocol holdMPTY no Argument";
                        break;
                    case 124:
                        result =  "invoke SS-protocol buildMPTY no Argument";
                        break;
                    case 125:
                        result =  "invoke SS-protocol forwardChargeAdvice";
                        break;
                    case 126:
                        result =  "invoke SS-protocol explicitCT no Argument";
                        break;
                    default:
                        result =  "invoke";
                        break;
                }
                break;

            case 2:
                switch (gsm_op_code) {
                    case 2:
                          result = "returnResultLast updateLocation";
                        break;
                    case 3:
                          result = "returnResultLast cancelLocation";
                        break;
                    case 4:
                          result = "returnResultLast provideRoamingNumber";
                        break;
                    case 5:
                          result = "returnResultLast noteSubscriberDataModified";
                        break;
                    case 6:
                          result = "returnResultLast resumeCallHandling";
                        break;
                    case 7:
                          result = "returnResultLast insertSubscriberData";
                        break;
                    case 8:
                          result = "returnResultLast deleteSubscriberData";
                        break;
                    case 9:
                          result = "returnResultLast sendParameters";
                        break;
                    case 10:
                          result = "returnResultLast registerSS";
                        break;
                    case 11:
                          result = "returnResultLast eraseSS";
                        break;
                    case 12:
                          result = "returnResultLast activateSS";
                        break;
                    case 13:
                          result = "returnResultLast deactivateSS";
                        break;
                    case 14:
                          result = "returnResultLast interrogateSS";
                        break;
                    case 15:
                          result = "returnResultLast authenticationFailureReport";
                        break;
                    case 16:
                          result = "returnResultLast SS-protocol notifySS";
                        break;
                    case 17:
                          result = "returnResultLast registerPassword";
                        break;
                    case 18:
                          result = "returnResultLast getPassword";
                        break;
                    case 19:
                          result = "returnResultLast SS-Protocol processUnstructuredSS-Data (19)";
                        break;
                    case 20:
                          result = "returnResultLast releaseResources";
                        break;
                    case 21:
                          result = "returnResultLast mt-ForwardSM-VGCS";
                        break;
                    case 22:
                          result = "returnResultLast sendRoutingInfo";
                        break;
                    case 23:
                          result = "returnResultLast updateGprsLocation";
                        break;
                    case 24:
                          result = "returnResultLast sendRoutingInfoForGprs";
                        break;
                    case 25:
                          result = "returnResultLast failureReport";
                        break;
                    case 26:
                          result = "returnResultLast noteMsPresentForGprs";
                        break;
                    case 29:
                          result = "returnResultLast sendEndSignal";
                        break;
                    case 31:
                          result = "returnResultLast provideSIWFSNumber";
                        break;
                    case 32:
                          result = "returnResultLast sIWFSSignallingModify";
                        break;
                    case 33:
                          result = "returnResultLast processAccessSignalling";
                        break;
                    case 34:
                          result = "returnResultLast forwardAccessSignalling";
                        break;
                    case 36:
                          result = "returnResultLast cancelVcsgLocation";
                        break;
                    case 37:
                          result = "returnResultLast reset";
                        break;
                    case 38:
                          result = "returnResultLast forwardCheckSS-Indication";
                        break;
                    case 39:
                          result = "returnResultLast prepareGroupCall";
                        break;
                    case 40:
                          result = "returnResultLast sendGroupCallEndSignal";
                        break;
                    case 41:
                          result = "returnResultLast processGroupCallSignalling";
                        break;
                    case 42:
                          result = "returnResultLast forwardGroupCallSignalling";
                        break;
                    case 43:
                          result = "returnResultLast checkIMEI";
                        break;
                    case 44:
                          result = "returnResultLast ForwardSM";
                        break;
                    case 45:
                          result = "returnResultLast sendRoutingInfoForSM";
                        break;
                    case 46:
                          result = "returnResultLast ForwardSM";
                        break;
                    case 47:
                          result = "returnResultLast reportSM-DeliveryStatus";
                        break;
                    case 48:
                          result = "returnResultLast noteSubscriberPresent";
                        break;
                    case 50:
                          result = "returnResultLast activateTraceMode";
                        break;
                    case 51:
                          result = "returnResultLast deactivateTraceMode";
                        break;
                    case 53:
                          result = "returnResultLast UpdateVcsgLocation 53";
                        break;
                    case 54:
                          result = "returnResultLast beginSubscriberActivity";
                        break;
                    case 55:
                          result = "returnResultLast sendIdentification";
                        break;
                    case 56:
                          result = "returnResultLast sendAuthenticationInfo";
                        break;
                    case 57:
                          result = "returnResultLast restoreData";
                        break;
                    case 58:
                          result = "returnResultLast sendIMSI";
                        break;
                    case 59:
                          result = "returnResultLast processUnstructuredSS-Request";
                        break;
                    case 60:
                          result = "returnResultLast unstructuredSS-Request";
                        break;
                    case 61:
                          result = "returnResultLast unstructuredSS-Notify";
                        break;
                    case 62:
                          result = "returnResultLast AnyTimeSubscriptionInterrogation";
                        break;
                    case 63:
                          result = "returnResultLast informServiceCentre";
                        break;
                    case 64:
                          result = "returnResultLast alertServiceCentre";
                        break;
                    case 65:
                          result = "returnResultLast AnyTimeModification";
                        break;
                    case 66:
                          result = "returnResultLast readyForSM";
                        break;
                    case 67:
                          result = "returnResultLast purgeMS";
                        break;
                    case 68:
                          result = "returnResultLast prepareHandover";
                        break;
                    case 69:
                          result = "returnResultLast prepareSubsequentHandover";
                        break;
                    case 70:
                          result = "returnResultLast provideSubscriberInfo";
                        break;
                    case 71:
                          result = "returnResultLast anyTimeInterrogation";
                        break;
                    case 72:
                          result = "returnResultLast ss-InvocationNotification";
                        break;
                    case 73:
                          result = "returnResultLast setReportingState";
                        break;
                    case 74:
                          result = "returnResultLast statusReport";
                        break;
                    case 75:
                          result = "returnResultLast remoteUserFree";
                        break;
                    case 76:
                          result = "returnResultLast registerCC-Entry";
                        break;
                    case 77:
                          result = "returnResultLast eraseCC-Entry";
                        break;
                    case 78:
                    case 79:
                    case 80:
                    case 81:
                          result = "returnResultLast secureTransportClass1";
                        break;
                        
                    case 83:
                          result = "returnResultLast provideSubscriberLocation";
                        break;
                    case 84:
                          result = "returnResultLast sendGroupCallInfo";
                        break;
                    case 85:
                          result = "returnResultLast sendRoutingInfoForLCS";
                        break;
                    case 86:
                          result = "returnResultLast subscriberLocationReport";
                        break;
                    case 87:
                          result = "returnResultLast ist-Alert";
                        break;
                    case 88:
                          result = "returnResultLast ist-Command";
                        break;
                    case 89:
                          result = "returnResultLast noteMM-Event";
                        break;
                    case 108:
                          result = "returnResultLast SS-protocol lcs-PeriodicTriggeredInvoke";
                        break;
                    case 109:
                          result = "returnResultLast SS-protocol lcs-PeriodicLocationCancellation";
                        break;
                    case 110:
                          result = "returnResultLast SS-protocol lcs-LocationUpdate";
                        break;
                    case 111:
                          result = "returnResultLast SS-protocol lcs-PeriodicLocationRequest";
                        break;
                    case 112:
                          result = "returnResultLast SS-protocol lcs-AreaEventCancellation";
                        break;
                    case 113:
                          result = "returnResultLast SS-protocol lcs-AreaEventReport";
                        break;
                    case 114:
                          result = "returnResultLast SS-protocol lcs-AreaEventRequest";
                        break;
                    case 115:
                          result = "returnResultLast SS-protocol lcs-MOLR";
                        break;
                    case 116:
                          result = "returnResultLast SS-protocol lcs-LocationNotification";
                        break;
                    case 117:
                          result = "returnResultLast SS-protocol callDeflection";
                        break;
                    case 118:
                          result = "returnResultLast SS-protocol userUserService";
                        break;
                    case 119:
                          result = "returnResultLast SS-protocol accessRegisterCCEntry";
                        break;
                    case 120:
                          result = "returnResultLast SS-protocol forwardCUG-Info";
                        break;
                    case 121:
                          result = "returnResultLast SS-protocol splitMPTY no Argument";
                        break;
                    case 122:
                          result = "returnResultLast SS-protocol retrieveMPTY no Argument";
                        break;
                    case 123:
                          result = "returnResultLast SS-protocol holdMPTY no Argument";
                        break;
                    case 124:
                          result = "returnResultLast SS-protocol buildMPTY no Argument";
                        break;
                    case 125:
                          result = "returnResultLast SS-protocol forwardChargeAdvice";
                        break;
                    case 126:
                          result = "returnResultLast SS-protocol explicitCT no Argument";
                        break;
                    default:
                          result = "returnResultLast";
                        break;
                }
                break;

            case 3:
                switch (gsm_op_code) {
                    case 1:
                        result = "unknownSubscriber";
                        break;
                    case 2:
                        result = "unknownBaseStation";
                        break;
                    case 3:
                        result = "unknownMSC";
                        break;
                    case 4:
                        result = "secureTransportError";
                        break;
                    case 5:
                        result = "unidentifiedSubscriber";
                        break;
                    case 6:
                        result = "absentSubscriberSM";
                        break;
                    case 7:
                        result = "unknownEquipment";
                        break;
                    case 8:
                        result = "roamingNotAllowed";
                        break;
                    case 9:
                        result = "illegalSubscriber";
                        break;
                    case 10:
                        result = "bearerServiceNotProvisioned";
                        break;
                    case 11:
                        result = "teleserviceNotProvisioned";
                        break;
                    case 12:
                        result = "illegalEquipment";
                        break;
                    case 13:
                        result = "callBarred";
                        break;
                    case 14:
                        result = "forwardingViolation";
                        break;
                    case 15:
                        result = "cug-Reject";
                        break;
                    case 16:
                        result = "illegalSS-Operation";
                        break;
                    case 17:
                        result = "ss-ErrorStatus";
                        break;
                    case 18:
                        result = "ss-NotAvailable";
                        break;
                    case 19:
                        result = "ss-SubscriptionViolation";
                        break;
                    case 20:
                        result = "ss-Incompatibility";
                        break;
                    case 21:
                        result = "facilityNotSupported";
                        break;
                    case 22:
                        result = "ongoingGroupCall";
                        break;
                    case 23:
                        result = "invalidTargetBaseStation";
                        break;
                    case 24:
                        result = "noRadioResourceAvailable";
                        break;
                    case 25:
                        result = "noHandoverNumberAvailable";
                        break;
                    case 26:
                        result = "subsequentHandoverFailure";
                        break;
                    case 27:
                        result = "absentSubscriber";
                        break;
                    case 28:
                        result = "incompatibleTerminal";
                        break;
                    case 29:
                        result = "shortTermDenial";
                        break;
                    case 30:
                        result = "longTermDenial";
                        break;
                    case 31:
                        result = "subscriberBusyForMT-SMS";
                        break;
                    case 32:
                        result = "sm-DeliveryFailure";
                        break;
                    case 33:
                        result = "messageWaitingListFull";
                        break;
                    case 34:
                        result = "systemFailure";
                        break;
                    case 35:
                        result = "dataMissing";
                        break;
                    case 36:
                        result = "unexpectedDataValue";
                        break;
                    case 37:
                        result = "pw-RegistrationFailure";
                        break;
                    case 38:
                        result = "negativePW-Check";
                        break;
                    case 39:
                        result = "noRoamingNumberAvailable";
                        break;
                    case 40:
                        result = "tracingBufferFull";
                        break;
                    case 42:
                        result = "targetCellOutsideGroupCallArea";
                        break;
                    case 43:
                        result = "numberOfPW-AttemptsViolation";
                        break;
                    case 44:
                        result = "numberChanged";
                        break;
                    case 45:
                        result = "busySubscriber";
                        break;
                    case 46:
                        result = "noSubscriberReply";
                        break;
                    case 47:
                        result = "forwardingFailed";
                        break;
                    case 48:
                        result = "or-NotAllowed";
                        break;
                    case 49:
                        result = "ati-NotAllowed";
                        break;
                    case 50:
                        result = "noGroupCallNumberAvailable";
                        break;
                    case 51:
                        result = "resourceLimitation";
                        break;
                    case 52:
                        result = "unauthorizedRequestingNetwork";
                        break;
                    case 53:
                        result = "unauthorizedLCSClient";
                        break;
                    case 54:
                        result = "positionMethodFailure";
                        break;
                    case 58:
                        result = "unknownOrUnreachableLCSClient";
                        break;
                    case 59:
                        result = "mm-EventNotSupported";
                        break;
                    case 60:
                        result = "atsi-NotAllowed";
                        break;
                    case 61:
                        result = "atm-NotAllowed";
                        break;
                    case 62:
                        result = "informationNotAvailable";
                        break;
                    case 71:
                        result = "unknownAlphabet";
                        break;
                    case 72:
                        result = "ussd-Busy";
                        break;
                    default:
                        result = "returnError";
                        break;
                }
                break;

            case 4:
                result = "rejected";
                break;

            case 7:
                switch (gsm_op_code) {
                    case 2:
                        result = "returnResultNotLast updateLocation";
                        break;
                    case 3:
                        result = "returnResultNotLast cancelLocation";
                        break;
                    case 4:
                        result = "returnResultNotLast provideRoamingNumber";
                        break;
                    case 5:
                        result = "returnResultNotLast noteSubscriberDataModified";
                        break;
                    case 6:
                        result = "returnResultNotLast resumeCallHandling";
                        break;
                    case 7:
                        result = "returnResultNotLast insertSubscriberData";
                        break;
                    case 8:
                        result = "returnResultNotLast deleteSubscriberData";
                        break;
                    case 9:
                        result = "returnResultNotLast sendParameters";
                        break;
                    case 10:
                        result = "returnResultNotLast registerSS";
                        break;
                    case 11:
                        result = "returnResultNotLast eraseSS";
                        break;
                    case 12:
                        result = "returnResultNotLast activateSS";
                        break;
                    case 13:
                        result = "returnResultNotLast deactivateSS";
                        break;
                    case 14:
                        result = "returnResultNotLast interrogateSS";
                        break;
                    case 15:
                        result = "returnResultNotLast authenticationFailureReport";
                        break;
                    case 16:
                        result = "returnResultNotLast SS-protocol notifySS";
                        break;
                    case 17:
                        result = "returnResultNotLast registerPassword";
                        break;
                    case 18:
                        result = "returnResultNotLast getPassword";
                        break;
                    case 19:
                        result = "returnResultNotLast SS-Protocol processUnstructuredSS-Data (19)";
                        break;
                    case 20:
                        result = "returnResultNotLast releaseResources";
                        break;
                    case 21:
                        result = "returnResultNotLast mt-ForwardSM-VGCS";
                        break;
                    case 22:
                        result = "returnResultNotLast sendRoutingInfo";
                        break;
                    case 23:
                        result = "returnResultNotLast updateGprsLocation";
                        break;
                    case 24:
                        result = "returnResultNotLast sendRoutingInfoForGprs";
                        break;
                    case 25:
                        result = "returnResultNotLast failureReport";
                        break;
                    case 26:
                        result = "returnResultNotLast noteMsPresentForGprs";
                        break;
                    case 29:
                        result = "returnResultNotLast sendEndSignal";
                        break;
                    case 31:
                        result = "returnResultNotLast provideSIWFSNumber";
                        break;
                    case 32:
                        result = "returnResultNotLast sIWFSSignallingModify";
                        break;
                    case 33:
                        result = "returnResultNotLast processAccessSignalling";
                        break;
                    case 34:
                        result = "returnResultNotLast forwardAccessSignalling";
                        break;
                    case 36:
                        result = "returnResultNotLast cancelVcsgLocation";
                        break;
                    case 37:
                        result = "returnResultNotLast reset";
                        break;
                    case 38:
                        result = "returnResultNotLast forwardCheckSS-Indication";
                        break;
                    case 39:
                        result = "returnResultNotLast prepareGroupCall";
                        break;
                    case 40:
                        result = "returnResultNotLast sendGroupCallEndSignal";
                        break;
                    case 41:
                        result = "returnResultNotLast processGroupCallSignalling";
                        break;
                    case 42:
                        result = "returnResultNotLast forwardGroupCallSignalling";
                        break;
                    case 43:
                        result = "returnResultNotLast checkIMEI";
                        break;
                    case 44:
                        result = "returnResultNotLast ForwardSM";
                        break;
                    case 45:
                        result = "returnResultNotLast sendRoutingInfoForSM";
                        break;
                    case 46:
                        result = "returnResultNotLast ForwardSM";
                        break;
                    case 47:
                        result = "returnResultNotLast reportSM-DeliveryStatus";
                        break;
                    case 48:
                        result = "returnResultNotLast noteSubscriberPresent";
                        break;
                    case 50:
                        result = "returnResultNotLast activateTraceMode";
                        break;
                    case 51:
                        result = "returnResultNotLast deactivateTraceMode";
                        break;
                    case 53:
                        result = "returnResultNotLast UpdateVcsgLocation 53";
                        break;
                    case 54:
                        result = "returnResultNotLast beginSubscriberActivity";
                        break;
                    case 55:
                        result = "returnResultNotLast sendIdentification";
                        break;
                    case 56:
                        result = "returnResultNotLast sendAuthenticationInfo";
                        break;
                    case 57:
                        result = "returnResultNotLast restoreData";
                        break;
                    case 58:
                        result = "returnResultNotLast sendIMSI";
                        break;
                    case 59:
                        result = "returnResultNotLast processUnstructuredSS-Request";
                        break;
                    case 60:
                        result = "returnResultNotLast unstructuredSS-Request";
                        break;
                    case 61:
                        result = "returnResultNotLast unstructuredSS-Notify";
                        break;
                    case 62:
                        result = "returnResultNotLast AnyTimeSubscriptionInterrogation";
                        break;
                    case 63:
                        result = "returnResultNotLast informServiceCentre";
                        break;
                    case 64:
                        result = "returnResultNotLast alertServiceCentre";
                        break;
                    case 65:
                        result = "returnResultNotLast AnyTimeModification";
                        break;
                    case 66:
                        result = "returnResultNotLast readyForSM";
                        break;
                    case 67:
                        result = "returnResultNotLast purgeMS";
                        break;
                    case 68:
                        result = "returnResultNotLast prepareHandover";
                        break;
                    case 69:
                        result = "returnResultNotLast prepareSubsequentHandover";
                        break;
                    case 70:
                        result = "returnResultNotLast provideSubscriberInfo";
                        break;
                    case 71:
                        result = "returnResultNotLast anyTimeInterrogation";
                        break;
                    case 72:
                        result = "returnResultNotLast ss-InvocationNotification";
                        break;
                    case 73:
                        result = "returnResultNotLast setReportingState";
                        break;
                    case 74:
                        result = "returnResultNotLast statusReport";
                        break;
                    case 75:
                        result = "returnResultNotLast remoteUserFree";
                        break;
                    case 76:
                        result = "returnResultNotLast registerCC-Entry";
                        break;
                    case 77:
                        result = "returnResultNotLast eraseCC-Entry";
                        break;
                    case 78:
                    case 79:
                    case 80:
                    case 81:
                        result = "returnResultNotLast secureTransportClass1";
                        break;
                    case 83:
                        result = "returnResultNotLast provideSubscriberLocation";
                        break;
                    case 84:
                        result = "returnResultNotLast sendGroupCallInfo";
                        break;
                    case 85:
                        result = "returnResultNotLast sendRoutingInfoForLCS";
                        break;
                    case 86:
                        result = "returnResultNotLast subscriberLocationReport";
                        break;
                    case 87:
                        result = "returnResultNotLast ist-Alert";
                        break;
                    case 88:
                        result = "returnResultNotLast ist-Command";
                        break;
                    case 89:
                        result = "returnResultNotLast noteMM-Event";
                        break;
                    case 108:
                        result = "returnResultNotLast SS-protocol lcs-PeriodicTriggeredInvoke";
                        break;
                    case 109:
                        result = "returnResultNotLast SS-protocol lcs-PeriodicLocationCancellation";
                        break;
                    case 110:
                        result = "returnResultNotLast SS-protocol lcs-LocationUpdate";
                        break;
                    case 111:
                        result = "returnResultNotLast SS-protocol lcs-PeriodicLocationRequest";
                        break;
                    case 112:
                        result = "returnResultNotLast SS-protocol lcs-AreaEventCancellation";
                        break;
                    case 113:
                        result = "returnResultNotLast SS-protocol lcs-AreaEventReport";
                        break;
                    case 114:
                        result = "returnResultNotLast SS-protocol lcs-AreaEventRequest";
                        break;
                    case 115:
                        result = "returnResultNotLast SS-protocol lcs-MOLR";
                        break;
                    case 116:
                        result = "returnResultNotLast SS-protocol lcs-LocationNotification";
                        break;
                    case 117:
                        result = "returnResultNotLast SS-protocol callDeflection";
                        break;
                    case 118:
                        result = "returnResultNotLast SS-protocol userUserService";
                        break;
                    case 119:
                        result = "returnResultNotLast SS-protocol accessRegisterCCEntry";
                        break;
                    case 120:
                        result = "returnResultNotLast SS-protocol forwardCUG-Info";
                        break;
                    case 121:
                        result = "returnResultNotLast SS-protocol splitMPTY no Argument";
                        break;
                    case 122:
                        result = "returnResultNotLast SS-protocol retrieveMPTY no Argument";
                        break;
                    case 123:
                        result = "returnResultNotLast SS-protocol holdMPTY no Argument";
                        break;
                    case 124:
                        result = "returnResultNotLast SS-protocol buildMPTY no Argument";
                        break;
                    case 125:
                        result = "returnResultNotLast SS-protocol forwardChargeAdvice";
                        break;
                    case 126:
                        result = "returnResultNotLast SS-protocol explicitCT no Argument";
                        break;
                    default:
                        result = "returnResultNotLast";
                        break;
                }
                break;
                
        }
        return result;
    }

    public int processMapQueryCount(HashMap<String, Object> filter) {
        return ss7MapService.getSequenceDiagramQueryCount(filter, timezone);
    }

    public  HashMap<String, Object> processMapQuery(HashMap<String, Object> filter, int limit, int page) {
        HashMap<String, Object> response = new HashMap<>();
        int offset = 0;
        if (page > 1)
            offset = (page - 1) * limit;

        List<SS7MapData> resultData = ss7MapService.getSequenceDiagramQuery(filter, timezone, limit, offset);
        response.put("data", resultData);
        //
        // response.put("tsharkQuery" , getFrameQuery(resultData));
        //
        response.put("tsharkQuery" , "");
        return response;
    }

    public HashMap<String, Object> processMapResult(List<SS7MapData> resultData) {
        HashMap<String, Object> response = new HashMap<>();
        response.put("data", resultData);
        response.put("tsharkQuery" , "");
        return response;
    }

    public List<ConsolidatedDiagram> buildDiagram(List<SS7MapData> result) {
        List<ConsolidatedDiagram> consolidatedDiagramList = new ArrayList<>();
        try {
            StringBuilder html;
            for (SS7MapData item : result) {
                html = new StringBuilder();
                //Names for nodes
                ConsolidatedDiagram consolidatedDiagram = new ConsolidatedDiagram();
                consolidatedDiagram.setTimestampToOrder(new BigDecimal(item.getTime_epoch()));
                consolidatedDiagram.setUSecondsToOrder(item.getU_seconds_epoch());
                consolidatedDiagram.setTimestamp(UtilityFunctions.getDateFormat(item.getTime_epoch(), item.getU_seconds_epoch(), timezone));
                consolidatedDiagram.setFrom(
                        _ipNames.containsKey(item.getMtp3_opc().toString()) ?
                                _ipNames.get((item.getMtp3_opc().toString())) :
                                item.getMtp3_opc().toString()
                );

                consolidatedDiagram.setTo(
                        _ipNames.containsKey(item.getMtp3_dpc().toString()) ?
                                _ipNames.get((item.getMtp3_dpc().toString())) :
                                item.getMtp3_dpc().toString()
                );
                item.setGsm_op_code(
                       item.getGsm_op_code()==null ? 0 : item.getGsm_op_code()
                );

                if (item.getGsm_component() != null && item.getGsm_op_code() != null) {
                    consolidatedDiagram.setSeparator(true);
                    consolidatedDiagram.setType(getFlowDiagramBuilderType(item.getGsm_component(), item.getGsm_op_code()));
                } else {
                    consolidatedDiagram.setType("TCAP_" + item.getTcap_mess_type());
                }

                Class<?> clazz = item.getClass();
                String safeStringValue;
                for (Field field : clazz.getDeclaredFields()) {
                    if (!field.getName().equals("id") &&
                            !field.getName().equals("time_epoch") &&
                            !field.getName().equals("u_seconds_epoch")) {

                        if (field.get(item) != null) {
                            safeStringValue = field.get(item).toString().replaceAll("<", "&lt;")
                                    .replaceAll(">", "&gt;");
                        } else {
                            safeStringValue = "";
                        }
                        String name = field.getName();
                        if (item.getGsm_op_code() != null && item.getGsm_op_code() == 45 && name.equals("msisdn_dest_address")) {
                            safeStringValue = "";
                        }

                        html.append("<tr>");
                        html.append("<td>").append(name).append("</td>");
                        html.append("<td>").append(safeStringValue).append("</td>");
                        html.append("</tr>");

                    }
                }
                consolidatedDiagram.setProtocol("map");
                consolidatedDiagram.setModal(html.toString());

                //if(item.getGsm_component() != null)
                consolidatedDiagramList.add(consolidatedDiagram);
            }

        } catch (Exception ex){
            log.error("Error on try to create the SS7MAP Diagram {}", ex.getMessage());
        }
        return consolidatedDiagramList;
    }
}
