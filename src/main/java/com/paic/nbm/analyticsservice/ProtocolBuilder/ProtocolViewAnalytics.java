package com.paic.nbm.analyticsservice.ProtocolBuilder;

import com.paic.nbm.analyticsservice.Entities.*;
import com.paic.nbm.analyticsservice.Service.*;
import com.paic.nbm.analyticsservice.Utils.UtilityFunctions;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component("PROTOCOLS")
public class ProtocolViewAnalytics {
    @Autowired
    ProtocolViewService viewService;
    @Autowired
    HttpService httpService;
    @Autowired
    HttpOcsService httpOcsService;
    @Autowired
    HttpSS7Service httpSS7Service;
    @Autowired
    SipService sipService;
    @Autowired
    CamelService capService;
    @Autowired
    GtpService gtpService;
    @Autowired
    SS7MapService mapService;

    @Getter
    @Setter
    private String timezone;

    public HashMap<String, Object> buildGroupingCondition(
            HashMap<String, Boolean> protocols, String startDate, String endDate
            , String timezone, List<String> imsi, List<String> msisdn) {
        boolean wasImsiEmpty = false;
        HashMap<String,Object> filterGroupingCondition = new HashMap<>();
        filterGroupingCondition.put("startDate", startDate);
        filterGroupingCondition.put("endDate", endDate);

        if (!msisdn.isEmpty() || !imsi.isEmpty()) {
            if (!msisdn.isEmpty())
                filterGroupingCondition.put("msisdn", msisdn);
            if (!imsi.isEmpty()) {
                filterGroupingCondition.put("imsi", imsi);
            } else {
                wasImsiEmpty = true;
            }

            if (protocols.containsKey("SIP") && protocols.get("SIP") && !imsi.isEmpty() && msisdn.isEmpty()) {
                protocols.put("SIP", false);
            } else if (protocols.containsKey("SIP") && protocols.get("SIP")) {
                List<String> sipGrouping = sipService.getGroupingCondition(startDate, endDate, timezone, "", "", msisdn);
                sipGrouping.removeAll(Collections.singleton(null));
                if (!sipGrouping.isEmpty())
                    filterGroupingCondition.put("call_id-list-sip", sipGrouping);
            }

            if (protocols.containsKey("MAP") && protocols.get("MAP")) {
                mapService.TCAPDialogsPairing(startDate, endDate, timezone, imsi, msisdn);
            }

            if (protocols.containsKey("CAMEL") && protocols.get("CAMEL")) {
                List<CamelData> camelGrouping = capService.getGroupingCondition(startDate, endDate, timezone, imsi, msisdn);
                List<BigInteger> cap_tid_list = camelGrouping.stream().map(CamelData::getTcap_tid).collect(Collectors.toList());
                UtilityFunctions.cleanList(cap_tid_list);

                if (!cap_tid_list.isEmpty()) {
                    filterGroupingCondition.put("camel_tcaptid-list", cap_tid_list);
                }
            }

            if (protocols.containsKey("GTP") && protocols.get("GTP")) {
                List<BigInteger> gtp_teid_list = gtpService.getGroupingConditionTunnelList(startDate, endDate, timezone, imsi, msisdn);
                UtilityFunctions.cleanList(gtp_teid_list);

                if (!gtp_teid_list.isEmpty()) {
                    filterGroupingCondition.put("gtp_teid-list", gtp_teid_list);
                }

                List<BigInteger> gtp_sequence_list = gtpService.getGroupingConditionSequenceList(startDate, endDate, timezone, imsi, msisdn);
                UtilityFunctions.cleanList(gtp_sequence_list);

                if (!gtp_sequence_list.isEmpty()) {
                    filterGroupingCondition.put("gtp_sequence-list", gtp_sequence_list);
                }
            }

            if (protocols.containsKey("HTTP") && protocols.get("HTTP")) {
                List<HttpData> httpGrouping = httpService.getGroupingCondition(startDate, endDate, timezone, imsi, msisdn);
                List<BigInteger> http_response_in_list = httpGrouping.stream().map(HttpData::getHttp_response_in).collect(Collectors.toList());
                UtilityFunctions.cleanList(http_response_in_list);

                if (!http_response_in_list.isEmpty())
                    filterGroupingCondition.put("http_response_in-list", http_response_in_list);
            }

            if (protocols.containsKey("HTTP-OCS") && protocols.get("HTTP-OCS")) {
                List<HttpOcsData> httpOcsGrouping = httpOcsService.getGroupingCondition(startDate, endDate, timezone, imsi, msisdn);
                List<BigInteger> http_ocs_response_in_list = httpOcsGrouping.stream().map(HttpOcsData::getHttp_response_in).collect(Collectors.toList());
                UtilityFunctions.cleanList(http_ocs_response_in_list);

                if (!http_ocs_response_in_list.isEmpty())
                    filterGroupingCondition.put("http_response_in-list-ocs", http_ocs_response_in_list);
            }

            if (protocols.containsKey("HTTP-SS7") && protocols.get("HTTP-SS7")) {
                List<HttpSS7Data> httpSS7Grouping = new ArrayList<>();
                List<BigInteger> http_ss7_id_list = new ArrayList<>();
                List<String> http_ss7_imsi_list;

                // Offnet scenario, trying to get the Offnet IMSI
                // if (!msisdn.isEmpty() && imsi.isEmpty()) {
                if (!msisdn.isEmpty() && wasImsiEmpty) {
                    List<String> imsiOffnetList = httpSS7Service.getOffnetImsi(startDate, endDate, timezone, msisdn);
                    if (imsiOffnetList != null && !imsiOffnetList.isEmpty()) {
                        log.info("IMSI Offnet found in HTTP-SS7 {}", imsiOffnetList);
                        imsi.addAll(imsiOffnetList);
                        UtilityFunctions.cleanList(imsi);
                        filterGroupingCondition.put("imsi", imsi);
                    } else {
                        // Otherwise this is not an Offnet scenario, this could be an alphanumeric or short code number sending a campaign of messages
                        // getGroupingCondition: getting the list of IMSI destinations from MT messages
                        // getGroupingCondition2: getting the list of Requests Ids and Responses Ids of the HTTP_SS7 table
                        httpSS7Grouping = httpSS7Service.getGroupingCondition(startDate, endDate, timezone, imsi, msisdn);
                        if (!httpSS7Grouping.isEmpty()) {
                            List<HttpSS7Data> httpSS7Grouping2 = httpSS7Service.getGroupingCondition2(httpSS7Grouping);
                            http_ss7_id_list = httpSS7Grouping2.stream().map(HttpSS7Data:: getId).collect(Collectors.toList());
                            http_ss7_id_list.addAll(httpSS7Grouping2.stream().map(HttpSS7Data:: getHttp_response_in).collect(Collectors.toList()));
                            UtilityFunctions.cleanList(http_ss7_id_list);
                        }
                    }
                }
                // Offnet scenario, trying to get the Offnet MSISDN
                else if (msisdn.isEmpty() && !imsi.isEmpty()) {
                    List<String> msisdnOffnetList = httpSS7Service.getOffnetMsisdn(startDate, endDate, timezone, imsi);
                    if (msisdnOffnetList != null && !msisdnOffnetList.isEmpty()) {
                        msisdn.addAll(msisdnOffnetList);
                    }
                }

                if (!httpSS7Grouping.isEmpty()) {
                    http_ss7_imsi_list = httpSS7Grouping.stream().map(HttpSS7Data:: getImsi).collect(Collectors.toList());
                    UtilityFunctions.cleanList(http_ss7_imsi_list);
                    filterGroupingCondition.put("http_ss7_imsi-list", http_ss7_imsi_list);
                }
                if (!http_ss7_id_list.isEmpty())
                    filterGroupingCondition.put("http_ss7_id-list", http_ss7_id_list);
            }
        }

        return filterGroupingCondition;
    }

    public int processQueryCount(HashMap<String, Object> filter, HashMap<String, Boolean> protocols) {
        return viewService.getSequenceDiagramQueryCount(filter, protocols, timezone);
    }

    public List<ProtocolViewData> processQuery(HashMap<String, Object> filter, HashMap<String, Boolean> protocols, int limit, int page) {
        int offset = 0;
        if (page > 1)
            offset = (page - 1) * limit;

        return viewService.getSequenceDiagramQuery(filter, protocols, limit, offset, timezone);
    }
}
