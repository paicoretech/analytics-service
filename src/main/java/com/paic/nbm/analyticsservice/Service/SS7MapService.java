package com.paic.nbm.analyticsservice.Service;

import com.paic.nbm.analyticsservice.Entities.SS7MapData;
import com.paic.nbm.analyticsservice.Utils.UtilityFunctions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SS7MapService {

    @Autowired
    EntityManager entityManager;

    public void TCAPDialogsPairing(String startDate, String endDate, String timezone, List<String> imsi, List<String> msisdn) {
        BigInteger epochStartDate = UtilityFunctions.parseStringDateToEpoch(startDate, timezone);
        BigInteger epochEndDate = UtilityFunctions.parseStringDateToEpoch(endDate, timezone);
        String msisdnStr = (msisdn != null) ? String.join(",", msisdn) : "";
        String imsiStr = (imsi != null) ? String.join(",", imsi) : "";
        Query nativeQuery = entityManager.createNativeQuery(
                "select public.tcap_dialogs_pairing(cast(:msisdn as text), cast(:imsi as text), cast(:start_time_epoch as bigint), cast(:end_time_epoch as bigint))");
        nativeQuery.setParameter("msisdn", msisdnStr);
        nativeQuery.setParameter("imsi", imsiStr);
        nativeQuery.setParameter("start_time_epoch", epochStartDate);
        nativeQuery.setParameter("end_time_epoch", epochEndDate);
        try {
            String functionLogs = (String) nativeQuery.getSingleResult();
            log.info("logs from the tcap_dialogs_pairing function {}", functionLogs);
        } catch (Exception e) {
            log.error("Exception when executing the tcap_dialogs_pairing function", e.fillInStackTrace());
        }
    }

    public int getSequenceDiagramQueryCount(HashMap<String, Object> filters, String timezone) {
        return getData(filters, timezone, 0, 0).size();
    }

    private List<SS7MapData> getData(HashMap<String, Object> filters, String timezone, int totalPerPage, int offSet) {
        if (filters.get("tcap_otid-list") == null && filters.get("tcap_dtid-list") == null)
            return new ArrayList<>();
        HashMap<String, Object> filters_final = new HashMap<>(filters);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<SS7MapData> query = cb.createQuery(SS7MapData.class);
        BigInteger epochStartDate = UtilityFunctions.parseStringDateToEpoch(filters_final.remove("startDate").toString(), timezone);
        BigInteger epochEndDate = UtilityFunctions.parseStringDateToEpoch(filters_final.remove("endDate").toString(), timezone);
        Root<SS7MapData> ss7MapQuery = query.from(SS7MapData.class);
        List<Predicate> listFilter = new ArrayList<>();
        List<Predicate> imsiMsisdnFilter = new ArrayList<>();
        List<Predicate> listCustomFilterOr = new ArrayList<>();
        Predicate dateFilter =  cb.between(ss7MapQuery.get("time_epoch"), epochStartDate, epochEndDate);
        listFilter.add(dateFilter);

        for (String key: filters_final.keySet()) {
            if (key.split("-").length >= 2) {
                String[] data_key = key.split("-");
                switch (data_key[1]) {
                    case "list":
                        Expression<String> exp = ss7MapQuery.get(data_key[0]);
                        List<BigInteger> list_otid_dtid = (List<BigInteger>) filters_final.get(key);
                        listCustomFilterOr.add(exp.in(list_otid_dtid));
                        break;

                    case "custom":
                        HashMap<String,Object> customFilter = (HashMap<String, Object>) filters_final.get(key);
                        String filter = customFilter.get("filter").toString();
                        String value = customFilter.get("value").toString();
                        String type = customFilter.get("type").toString();
                        if (filter.equals("AND")) {
                            switch (type) {
                                case "String":
                                    listFilter.add(cb.like(ss7MapQuery.get(data_key[0]), value));
                                    break;
                            }

                        } else {
                            switch (type) {
                                case "String":
                                    listCustomFilterOr.add(cb.like(ss7MapQuery.get(data_key[0]), value));
                                    break;
                            }

                        }
                        break;
                }
            } else {
                Object value = filters_final.get(key);
                if (value.toString().isBlank() || value.toString().equals("NA") || value.toString().equals("[]"))
                    continue;
                if (key.equals("gsm_op_code")) {
                    listFilter.add(cb.equal(ss7MapQuery.get(key), Integer.parseInt(value.toString())));
                }
                else if ("msisdn".equals(key) || "imsi".equals(key)) {
                    imsiMsisdnFilter.add(ss7MapQuery.get(key).in(value));
                } else {
                    listFilter.add(cb.like(ss7MapQuery.get(key), filters_final.get(key).toString()));
                }
            }
        }

        if (!imsiMsisdnFilter.isEmpty())
            listFilter.add(cb.or(imsiMsisdnFilter.toArray(Predicate[]::new)));

        Predicate filterOrPredicate = cb.or(listCustomFilterOr.toArray(Predicate[]::new));
        Predicate filterPredicate = cb.and(listFilter.toArray(Predicate[]::new));

        if (!listCustomFilterOr.isEmpty()) {
            query.where(ss7MapQuery.get("gsm_component").isNotNull(), ss7MapQuery.get("gsm_op_code").isNotNull(), filterPredicate, filterOrPredicate);
        } else {
            query.where(ss7MapQuery.get("gsm_component").isNotNull(), ss7MapQuery.get("gsm_op_code").isNotNull(), filterPredicate);
        }

        if (totalPerPage > 0) {
            return entityManager.createQuery(query)
                    .setMaxResults(totalPerPage)
                    .setFirstResult(offSet)
                    .getResultList();
        } else {
            return entityManager.createQuery(query).getResultList();
        }
    }

    public List<SS7MapData> getSequenceDiagramQuery(HashMap<String, Object> filters_final, String timezone, int totalPerPage, int offSet) {
        List<SS7MapData> result = getData(filters_final, timezone, totalPerPage, offSet);
        result.forEach(r -> {
            r.setMap_component("");
            r.setMap_operation("");
            r.setMap_error_code("");
            r.setOperation_type("");

            if (r.getGsm_component() != null) {
                r.setMap_component(getMapComponent(r.getGsm_component()));
            }

            if (r.getGsm_component() != null && r.getGsm_op_code() != null) {
                r.setMap_operation(getMapOperation(r.getGsm_op_code(), r.getGsm_component()));
            }

            if (r.getGsm_component() != null && r.getGsm_component() == 3) {
                r.setMap_error_code(getErrorCode(r.getGsm_op_code(), r.getGsm_component()));
                r.setOperation_type(getOperationType(r.getGsm_op_code(), r.getGsm_component()));
            }


        });
        return result;
    }
    private String getMapComponent(int gsm_component) {
        String result = "";
        switch (gsm_component) {
            case 1:
                result = "invoke";
                break;
            case 2:
                result = "returnResultLast";
                break;
            case 3:
                result = "returnError";
                break;
            case 4:
                result = "reject";
                break;
            case 7:
                result = "returnResultNotLast";
                break;

            default:
                result = "";
                break;
        }
        return result;
    }

    private String getMapOperation(int gsm_op_code, int gsm_component) {
        String result = "";
        if (gsm_component != 3) {
            switch (gsm_op_code) {
                case 2:
                    result = "updateLocation";
                    break;
                case 3:
                    result = "cancelLocation";
                    break;
                case 4:
                    result = "provideRoamingNumber";
                    break;
                case 5:
                    result = "noteSubscriberDataModified";
                    break;
                case 6:
                    result = "resumeCallHandling";
                    break;
                case 7:
                    result = "insertSubscriberData";
                    break;
                case 8:
                    result = "deleteSubscriberData";
                    break;
                case 9:
                    result = "sendParameters";
                    break;
                case 10:
                    result = "registerSS";
                    break;
                case 11:
                    result = "eraseSS";
                    break;
                case 12:
                    result = "activateSS";
                    break;
                case 13:
                    result = "deactivateSS";
                    break;
                case 14:
                    result = "interrogateSS";
                    break;
                case 15:
                    result = "authenticationFailureReport";
                    break;
                case 16:
                    result = "SS-protocol notifySS";
                    break;
                case 17:
                    result = "registerPassword";
                    break;
                case 18:
                    result = "getPassword";
                    break;
                case 19:
                    result = "SS-Protocol processUnstructuredSS-Data (19)";
                    break;
                case 20:
                    result = "releaseResources";
                    break;
                case 21:
                    result = "mt-ForwardSM-VGCS";
                    break;
                case 22:
                    result = "sendRoutingInfo";
                    break;
                case 23:
                    result = "updateGprsLocation";
                    break;
                case 24:
                    result = "sendRoutingInfoForGprs";
                    break;
                case 25:
                    result = "failureReport";
                    break;
                case 26:
                    result = "noteMsPresentForGprs";
                    break;
                case 29:
                    result = "sendEndSignal";
                    break;
                case 31:
                    result = "provideSIWFSNumber";
                    break;

                case 32:
                    result = "sIWFSSignallingModify";
                    break;

                case 33:
                    result = "processAccessSignalling";
                    break;

                case 34:
                    result = "forwardAccessSignalling";
                    break;

                case 36:
                    result = "cancelVcsgLocation";
                    break;
                case 37:
                    result = "reset";
                    break;
                case 38:
                    result = "forwardCheckSS-Indication";
                    break;
                case 39:
                    result = "prepareGroupCall";
                    break;
                case 40:
                    result = "sendGroupCallEndSignal";
                    break;
                case 41:
                    result = "processGroupCallSignalling";
                    break;
                case 42:
                    result = "forwardGroupCallSignalling";
                    break;
                case 43:
                    result = "checkIMEI";
                    break;
                case 44:
                    result = "MTForwardSM";
                    break;
                case 45:
                    result = "sendRoutingInfoForSM";
                    break;
                case 46:
                    result = "MOForwardSM";
                    break;
                case 47:
                    result = "reportSM-DeliveryStatus";
                    break;
                case 48:
                    result = "noteSubscriberPresent";
                    break;
                case 50:
                    result = "activateTraceMode";
                    break;
                case 51:
                    result = "deactivateTraceMode";
                    break;
                case 53:
                    result = "UpdateVcsgLocation 53";
                    break;
                case 54:
                    result = "beginSubscriberActivity";
                    break;
                case 55:
                    result = "sendIdentification";
                    break;
                case 56:
                    result = "sendAuthenticationInfo";
                    break;
                case 57:
                    result = "restoreData";
                    break;
                case 58:
                    result = "sendIMSI";
                    break;
                case 59:
                    result = "processUnstructuredSS-Request";
                    break;
                case 60:
                    result = "unstructuredSS-Request";
                    break;
                case 61:
                    result = "unstructuredSS-Notify";
                    break;
                case 62:
                    result = "AnyTimeSubscriptionInterrogation";
                    break;
                case 63:
                    result = "informServiceCentre";
                    break;
                case 64:
                    result = "alertServiceCentre";
                    break;
                case 65:
                    result = "AnyTimeModification";
                    break;
                case 66:
                    result = "readyForSM";
                    break;
                case 67:
                    result = "purgeMS";
                    break;
                case 68:
                    result = "prepareHandover";
                    break;
                case 69:
                    result = "prepareSubsequentHandover";
                    break;
                case 70:
                    result = "provideSubscriberInfo";
                    break;
                case 71:
                    result = "anyTimeInterrogation";
                    break;
                case 72:
                    result = "ss-InvocationNotificatio";
                    break;
                case 73:
                    result = "setReportingState";
                    break;
                case 74:
                    result = "statusReport";
                    break;
                case 75:
                    result = "remoteUserFree";
                    break;
                case 76:
                    result = "registerCC-Entry";
                    break;
                case 77:
                    result = "eraseCC-Entry";
                    break;
                case 78:
                case 79:
                case 80:
                case 81:
                    result = "secureTransportClass1";
                    break;
                case 83:
                    result = "provideSubscriberLocation";
                    break;
                case 84:
                    result = "sendGroupCallInfo";
                    break;
                case 85:
                    result = "sendRoutingInfoForLCS";
                    break;
                case 86:
                    result = "subscriberLocationReport";
                    break;
                case 87:
                    result = "ist-Alert";
                    break;
                case 88:
                    result = "ist-Command";
                    break;
                case 89:
                    result = "noteMM-Event";
                    break;
                case 108:
                    result = "SS-protocol lcs-PeriodicTriggeredInvoke";
                    break;
                case 109:
                    result = "SS-protocol lcs-PeriodicLocationCancellation";
                    break;
                case 110:
                    result = "SS-protocol lcs-LocationUpdate";
                    break;
                case 111:
                    result = "SS-protocol lcs-PeriodicLocationRequest";
                    break;
                case 112:
                    result = "SS-protocol lcs-AreaEventCancellation";
                    break;
                case 113:
                    result = "SS-protocol lcs-AreaEventReport";
                    break;
                case 114:
                    result = "SS-protocol lcs-AreaEventRequest";
                    break;
                case 115:
                    result = "SS-protocol lcs-MOLR";
                    break;
                case 116:
                    result = "SS-protocol lcs-LocationNotification";
                    break;
                case 117:
                    result = "SS-protocol callDeflection";
                    break;
                case 118:
                    result = "SS-protocol userUserService";
                    break;
                case 119:
                    result = "SS-protocol accessRegisterCCEntry";
                    break;
                case 120:
                    result = "SS-protocol forwardCUG-Info";
                    break;
                case 121:
                    result = "SS-protocol splitMPTY no Argument";
                    break;
                case 122:
                    result = "SS-protocol retrieveMPTY no Argument";
                    break;
                case 123:
                    result = "SS-protocol holdMPTY no Argument";
                    break;
                case 124:
                    result = "SS-protocol buildMPTY no Argument";
                    break;
                case 125:
                    result = "SS-protocol forwardChargeAdvice";
                    break;
                case 126:
                    result = "SS-protocol explicitCT no Argument";
                    break;

                default:
                    result = "";
                    break;
            }
        }

        return result;
    }

    private String getErrorCode(int gsm_op_code, int gsm_component) {
        String result = "";
        if (gsm_component == 3) {
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
                    result = "";
                    break;
            }
        }
        return result;
    }

    private String getOperationType(int gsm_op_code, int gsm_component) {
        String result = "";
        if (gsm_component != 3) {
            switch (gsm_op_code) {
                case 2:
                case 3:
                case 55:
                case 67:
                    result = "Location Registration Operations";
                    break;

                case 4:
                case 6:
                case 22:
                case 31:
                case 74:
                case 75:
                case 87:
                case 88:
                    result = "Call Handling Operations";
                    break;

                case 5:
                    result = "Subscriber Data Modification Notification Operations";
                    break;

                case 7:
                case 8:
                case 9:
                    result = "Subscriber Management Operations";
                    break;

                case 10:
                case 11:
                case 12:
                case 13:
                case 14:
                case 17:
                case 18:
                case 54:
                    result = "Supplementary Service Operations";
                    break;

                case 15:
                case 56:
                    result = "Authentication Management Operations";
                    break;

                case 23:
                    result = "GPRS Location Registration Operations";
                    break;

                case 24:
                    result = "GPRS Location Information Retrieval Operations";
                    break;

                case 25:
                    result = "Failure Reporting Operations";
                    break;

                case 26:
                    result = "GPRS Notification Operations";
                    break;

                case 29:
                case 68:
                case 69:
                    result = "Handover Operations";
                    break;

                case 37:
                case 57:
                    result = "Fault Recovery Management Operations";
                    break;

                case 39:
                case 40:
                case 84:
                    result = "Group Call Operations";
                    break;

                case 43:
                    result = "IMEI Management Operations";
                    break;

                case 45:
                case 46:
                case 47:
                case 48:
                case 63:
                case 64:
                case 66:
                    result = "Short Message Service Operations";
                    break;

                case 50:
                case 51:
                case 58:
                    result = "Operation and Maintenance Operation";
                    break;

                case 62:
                case 65:
                    result = "Any Time Information Handling Operations";
                    break;

                case 70:
                    result = "Subscriber Information Enquiry Operations";
                    break;

                case 71:
                    result = "Any Time Information Enquiry Operations";
                    break;


                case 81:
                case 80:
                case 79:
                case 78:
                    result = "Secure Transport Operations";
                    break;

                case 83:
                case 85:
                case 86:
                    result = "Location Service Operations";
                    break;

                default:
                    result = "";
                    break;
            }
        }
        return result;
    }
}
