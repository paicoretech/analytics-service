package com.paic.nbm.analyticsservice.ProtocolBuilder;

import com.paic.nbm.analyticsservice.Entities.SmppData;
import com.paic.nbm.analyticsservice.Entities.ConsolidatedDiagram;
import com.paic.nbm.analyticsservice.Service.SmppService;
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
import java.util.stream.Collectors;

@Slf4j
@Component("SMPP")
public class SMPPAnalytics {
    @Autowired
    SmppService smppService;

    @Getter
    @Setter
    private String timezone;

    public HashMap<String, Object> buildHashMapGroupingCondition(String startDate, String endDate, String timezone, List<String> msisdn) {

        HashMap<String,Object> filterGroupingCondition = new HashMap<>();
        filterGroupingCondition.put("startDate", startDate);
        filterGroupingCondition.put("endDate", endDate);
        List<Integer> secuenceNumberList;
        List<String> ipSrcList;
        List<String> ipDstList;

        List<SmppData> smppDataList =  smppService.getGroupingCondition(startDate, endDate, timezone, msisdn);

        secuenceNumberList = UtilityFunctions.cleanList(smppDataList.stream().map(SmppData :: getSequence_number).collect(Collectors.toList()));
        ipSrcList = UtilityFunctions.cleanList(smppDataList.stream().map(SmppData :: getSrc_ip).collect(Collectors.toList()));
        ipDstList = UtilityFunctions.cleanList(smppDataList.stream().map(SmppData :: getDst_ip).collect(Collectors.toList()));

        if (!secuenceNumberList.isEmpty())
            filterGroupingCondition.put("sequence_number-list", secuenceNumberList);

        if (!ipSrcList.isEmpty())
            filterGroupingCondition.put("src_ip-list", ipSrcList);

        if (!ipDstList.isEmpty())
            filterGroupingCondition.put("dst_ip-list", ipDstList);

        return filterGroupingCondition;
    }

    public int processSmppQueryCount(HashMap<String, Object> filter) {
        return smppService.getSequenceDiagramQueryCount(filter, timezone);
    }

    public  HashMap<String, Object> processSmppQuery(HashMap<String, Object> filter, int limit, int page) {
        HashMap<String, Object> response = new HashMap<>();
        int offset = 0;
        if (page > 1)
            offset = (page - 1) * limit;

        List<SmppData> resultData = smppService.getSequenceDiagramQuery(filter, timezone, limit, offset);
        response.put("data", resultData);
        //
        // response.put("tsharkQuery" , getFrameQuery(resultData));
        //
        response.put("tsharkQuery" , "");
        return response;
    }

    public HashMap<String, Object> processSmppResult(List<SmppData> dataList) {
        HashMap<String, Object> response = new HashMap<>();
        response.put("data", dataList);
        response.put("tsharkQuery" , "");
        return response;
    }

    public List<ConsolidatedDiagram> buildDiagram(List<SmppData> result)  {
        List<ConsolidatedDiagram> consolidatedDiagramList = new ArrayList<>();
        try {
            StringBuilder html;
            for (SmppData item : result) {
                html = new StringBuilder();
                ConsolidatedDiagram consolidatedDiagram = new ConsolidatedDiagram();
                consolidatedDiagram.setTimestampToOrder(new BigDecimal(item.getTime_epoch()));
                consolidatedDiagram.setUSecondsToOrder(item.getU_seconds_epoch());
                consolidatedDiagram.setTimestamp(UtilityFunctions.getDateFormat(item.getTime_epoch(), item.getU_seconds_epoch(), timezone));
                consolidatedDiagram.setFrom(item.getSrc_ip());
                consolidatedDiagram.setTo(item.getDst_ip());
                consolidatedDiagram.setSeparator(item.getCommand_status() != null);
                consolidatedDiagram.setType(item.getCommand_id());
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
                consolidatedDiagram.setProtocol("smpp");
                consolidatedDiagram.setModal(html.toString());
                consolidatedDiagramList.add(consolidatedDiagram);
            }

        } catch (Exception ex) {
            log.error("Error on try to create the SMPP Diagram {}", ex.getMessage());
        }
        return consolidatedDiagramList;
    }
}
