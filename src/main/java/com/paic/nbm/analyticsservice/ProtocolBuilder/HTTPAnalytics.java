package com.paic.nbm.analyticsservice.ProtocolBuilder;

import com.paic.nbm.analyticsservice.Entities.HttpData;
import com.paic.nbm.analyticsservice.Entities.ConsolidatedDiagram;
import com.paic.nbm.analyticsservice.Service.HttpService;
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
@Component("HTTP")
public class HTTPAnalytics {
    @Autowired
    HttpService httpService;

    @Getter
    @Setter
    private String timezone;

    public HashMap<String,Object>  buildGroupingCondition(String startDate, String endDate, String timezone, List<String> imsi, List<String> msisdn) {
        HashMap<String,Object> filterGroupingCondition = new HashMap<>();
        filterGroupingCondition.put("startDate", startDate);
        filterGroupingCondition.put("endDate", endDate);
        if (!msisdn.isEmpty())
            filterGroupingCondition.put("msisdn", msisdn);
        if (!imsi.isEmpty())
            filterGroupingCondition.put("imsi", imsi);

        List<HttpData> resultGrouping = httpService.getGroupingCondition(startDate, endDate, timezone, imsi, msisdn);
        List<BigInteger> http_response_in_list = resultGrouping.stream().map(HttpData :: getHttp_response_in).collect(Collectors.toList());
        http_response_in_list.removeAll(Collections.singleton(null));
        Set<BigInteger> set_response_in = new HashSet<>(http_response_in_list);
        http_response_in_list.clear();
        http_response_in_list.addAll(set_response_in);

        if (!http_response_in_list.isEmpty())
            filterGroupingCondition.put("http_response_in-list", http_response_in_list);

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

        List<HttpData> resultData = httpService.getSequenceDiagramQuery(filter, timezone, limit, offset);
        response.put("data", resultData);
        //
        // response.put("tsharkQuery" , getFrameQuery(resultData));
        //
        response.put("tsharkQuery" , "");
        return response;
    }

    public HashMap<String, Object> processHttpResult(List<HttpData> resultData) {
        HashMap<String, Object> response = new HashMap<>();
        response.put("data", resultData);
        response.put("tsharkQuery" , "");
        return response;
    }

    public List<ConsolidatedDiagram> buildDiagram(List<HttpData> result)  {
        List<ConsolidatedDiagram> consolidatedDiagramList = new ArrayList<>();
        try {
            StringBuilder html;
            for (HttpData item : result) {
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
                consolidatedDiagram.setProtocol("http");
                consolidatedDiagram.setModal(html.toString());
                consolidatedDiagramList.add(consolidatedDiagram);
            }
        } catch (Exception ex) {
            log.error("Error on try to create the HTTP Diagram {}", ex.getMessage());
        }
        return consolidatedDiagramList;
    }
}
