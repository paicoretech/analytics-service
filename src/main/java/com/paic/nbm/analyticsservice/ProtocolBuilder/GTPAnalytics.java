package com.paic.nbm.analyticsservice.ProtocolBuilder;

import com.paic.nbm.analyticsservice.Entities.GtpData;
import com.paic.nbm.analyticsservice.Entities.ConsolidatedDiagram;
import com.paic.nbm.analyticsservice.Service.GtpService;
import com.paic.nbm.analyticsservice.Utils.UtilityFunctions;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Component("GTP")
public class GTPAnalytics {
    @Autowired
    GtpService gtpService;

    @Getter
    @Setter
    private String timezone;

    public  HashMap<String,Object> buildGTPGroupingCondition(String startDate, String endDate, String timezone, List<String> imsi, List<String> msisdn) {

        HashMap<String,Object> filterGroupingCondition = new HashMap<>();
        filterGroupingCondition.put("startDate", startDate);
        filterGroupingCondition.put("endDate", endDate);

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

        if (!imsi.isEmpty())
            filterGroupingCondition.put("imsi", imsi);

        if (!msisdn.isEmpty())
            filterGroupingCondition.put("msisdn", msisdn);

        return filterGroupingCondition;
    }

    public int processGtpQueryCount(HashMap<String, Object> filter) {
        return gtpService.getSequenceDiagramQueryCount(filter, timezone);
    }

    public HashMap<String, Object> processGtpQuery(HashMap<String, Object> filter, int limit, int page) {
        HashMap<String, Object> response = new HashMap<>();
        int offset = 0;
        if (page > 1)
            offset = (page - 1) * limit;

        List<GtpData> resultData = gtpService.getSequenceDiagramQuery(filter, timezone, limit, offset);
        response.put("data", resultData);
        response.put("tsharkQuery" , "");
        return response;
    }

    public HashMap<String, Object> processGtpResult(List<GtpData> resultData) {
        HashMap<String, Object> response = new HashMap<>();
        response.put("data", resultData);
        response.put("tsharkQuery" , "");
        return response;
    }

    public  List<ConsolidatedDiagram> buildDiagram(List<GtpData> result) {
        List<ConsolidatedDiagram> consolidatedDiagramList = new ArrayList<>();
        try {
            StringBuilder html;
            for (GtpData item : result) {
                html = new StringBuilder();
                ConsolidatedDiagram consolidatedDiagram = new ConsolidatedDiagram();
                consolidatedDiagram.setTimestampToOrder(new BigDecimal(item.getTime_epoch()));
                consolidatedDiagram.setUSecondsToOrder(item.getU_seconds_epoch());
                consolidatedDiagram.setTimestamp(UtilityFunctions.getDateFormat(item.getTime_epoch(), item.getU_seconds_epoch(), timezone));
                consolidatedDiagram.setFrom(item.getSrc_ip());
                consolidatedDiagram.setTo(item.getDst_ip());
                consolidatedDiagram.setSeparator(true);
                consolidatedDiagram.setType(item.getGtp_message());
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
                consolidatedDiagram.setProtocol("gtp");
                consolidatedDiagram.setModal(html.toString());
                consolidatedDiagramList.add(consolidatedDiagram);
            }

        }catch (Exception ex) {
            log.error("Error on try to create the GTP Diagram {}", ex.getMessage());
        }
        return consolidatedDiagramList;
    }
}
