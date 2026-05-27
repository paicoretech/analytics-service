package com.paic.nbm.analyticsservice.ProtocolBuilder;

import com.paic.nbm.analyticsservice.Entities.HttpSS7Data;
import com.paic.nbm.analyticsservice.Entities.ConsolidatedDiagram;
import com.paic.nbm.analyticsservice.Service.HttpSS7Service;
import com.paic.nbm.analyticsservice.Utils.UtilityFunctions;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component("HTTP_SS7")
public class HTTPSS7Analytics {
    @Autowired
    HttpSS7Service httpService;

    @Getter
    @Setter
    private String timezone;

    public HashMap<String,Object>  buildGroupingCondition(String startDate, String endDate, String timezone, List<String> imsi, List<String> msisdn) {
        HashMap<String,Object> filterGroupingCondition = new HashMap<>();
        List<HttpSS7Data> httpSS7Grouping = new ArrayList<>();
        List<BigInteger> http_ss7_id_list = new ArrayList<>();
        List<String> http_ss7_imsi_list;

        // Offnet scenario, trying to get the Offnet IMSI
        if (!msisdn.isEmpty() && imsi.isEmpty()) {
            List<String> imsiOffnetList = httpService.getOffnetImsi(startDate, endDate, timezone, msisdn);
            if (imsiOffnetList != null && !imsiOffnetList.isEmpty()) {
                imsi.addAll(imsiOffnetList);
            } else {
                // Otherwise this is not an Offnet scenario, this could be an alphanumeric or short code number sending a campaign of messages
                // getGroupingCondition: getting the list of IMSI destinations from MT messages
                // getGroupingCondition2: getting the list of Requests Ids and Responses Ids of the HTTP_SS7 table
                httpSS7Grouping = httpService.getGroupingCondition(startDate, endDate, timezone, imsi, msisdn);
                if (!httpSS7Grouping.isEmpty()) {
                    List<HttpSS7Data> httpSS7Grouping2 = httpService.getGroupingCondition2(httpSS7Grouping);
                    http_ss7_id_list = httpSS7Grouping2.stream().map(HttpSS7Data:: getId).collect(Collectors.toList());
                    http_ss7_id_list.addAll(httpSS7Grouping2.stream().map(HttpSS7Data:: getHttp_response_in).collect(Collectors.toList()));
                    UtilityFunctions.cleanList(http_ss7_id_list);
                }
            }
        }
        // Offnet scenario, trying to get the Offnet MSISDN
        else if (msisdn.isEmpty() && !imsi.isEmpty()) {
            List<String> msisdnOffnetList = httpService.getOffnetMsisdn(startDate, endDate, timezone, imsi);
            if (msisdnOffnetList != null && !msisdnOffnetList.isEmpty()) {
                msisdn.addAll(msisdnOffnetList);
            }
        }

        filterGroupingCondition.put("startDate", startDate);
        filterGroupingCondition.put("endDate", endDate);
        if (!msisdn.isEmpty())
            filterGroupingCondition.put("msisdn", msisdn);
        if (!imsi.isEmpty())
            filterGroupingCondition.put("imsi", imsi);

        if (!httpSS7Grouping.isEmpty()) {
            http_ss7_imsi_list = httpSS7Grouping.stream().map(HttpSS7Data:: getImsi).collect(Collectors.toList());
            UtilityFunctions.cleanList(http_ss7_imsi_list);
            filterGroupingCondition.put("http_ss7_imsi-list", http_ss7_imsi_list);
        }
        if (!http_ss7_id_list.isEmpty())
            filterGroupingCondition.put("http_ss7_id-list", http_ss7_id_list);

        return filterGroupingCondition;
    }

    public int processHttpQueryCount(HashMap<String, Object> filter) {
        return httpService.getSequenceDiagramQueryCount(filter, timezone);
    }

    public HashMap<String, Object> processHTTPQuery(HashMap<String, Object> filter, int limit, int page) {
        HashMap<String, Object> response = new HashMap<>();
        int offset = 0;
        if (page > 1)
            offset = (page - 1) * limit;

        List<HttpSS7Data> resultData = httpService.getSequenceDiagramQuery(filter, timezone, limit, offset);
        response.put("data", resultData);
        //
        // response.put("tsharkQuery" , getFrameQuery(resultData));
        //
        response.put("tsharkQuery" , "");
        return response;
    }

    public HashMap<String, Object> processHttpResult(List<HttpSS7Data> resultData) {
        HashMap<String, Object> response = new HashMap<>();
        response.put("data", resultData);
        response.put("tsharkQuery" , "");
        return response;
    }

    public List<ConsolidatedDiagram> buildDiagram(List<HttpSS7Data> result)  {
        List<ConsolidatedDiagram> consolidatedDiagramList = new ArrayList<>();
        try {
            StringBuilder html;
            for (HttpSS7Data item : result) {
                html = new StringBuilder();
                ConsolidatedDiagram consolidatedDiagram = new ConsolidatedDiagram();
                consolidatedDiagram.setTimestampToOrder(new BigDecimal(item.getTime_epoch()));
                consolidatedDiagram.setUSecondsToOrder(item.getU_seconds_epoch());
                consolidatedDiagram.setTimestamp(UtilityFunctions.getDateFormat(item.getTime_epoch(), item.getU_seconds_epoch(), timezone));
                consolidatedDiagram.setFrom(item.getSrc_ip());
                consolidatedDiagram.setTo(item.getDst_ip());
                consolidatedDiagram.setSeparator(item.getHttp_is_request());
                consolidatedDiagram.setType(item.getType());
                Class<?> clazz = item.getClass();
                String safeStringValue;
                for(Field field : clazz.getDeclaredFields()) {
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
                        html.append("<tr>");
                        html.append("<td>").append(name).append("</td>");
                        html.append("<td>").append(safeStringValue).append("</td>");
                        html.append("</tr>");

                    }
                }
                consolidatedDiagram.setProtocol("http-ss7");
                consolidatedDiagram.setModal(html.toString());
                consolidatedDiagramList.add(consolidatedDiagram);
            }
        } catch (Exception ex) {
            log.error("Error on try to create the HTTP-SS7 Diagram {}", ex.getMessage());
        }
        return consolidatedDiagramList;
    }
}
