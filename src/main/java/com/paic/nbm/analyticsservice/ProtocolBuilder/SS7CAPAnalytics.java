package com.paic.nbm.analyticsservice.ProtocolBuilder;

import com.paic.nbm.analyticsservice.Entities.CamelData;
import com.paic.nbm.analyticsservice.Entities.ConsolidatedDiagram;
import com.paic.nbm.analyticsservice.Service.CamelService;
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
import java.util.stream.Collectors;

import static com.paic.nbm.analyticsservice.AnalyticsServiceApplication._ipNames;

@Slf4j
@Component("SS7CAP")
public class SS7CAPAnalytics {
    @Autowired
    CamelService camelService;

    @Getter
    @Setter
    private String timezone;

    public HashMap<String, Object> buildSS7CAPGroupingCondition(String startDate, String endDate, String timezone, List<String> imsi, List<String> msisdn) {
        HashMap<String,Object> filterGroupingCondition = new HashMap<>();
        filterGroupingCondition.put("startDate", startDate);
        filterGroupingCondition.put("endDate", endDate);

        List<BigInteger> tcap_otid_dtid_list = new ArrayList<>();

        List<CamelData> resultGrouping = camelService.getGroupingCondition(startDate, endDate, timezone, imsi, msisdn);

        tcap_otid_dtid_list.addAll(resultGrouping.stream().map(CamelData::getTcap_otid).collect(Collectors.toList()));
        tcap_otid_dtid_list.addAll(resultGrouping.stream().map(CamelData::getTcap_dtid).collect(Collectors.toList()));
        UtilityFunctions.cleanList(tcap_otid_dtid_list);

        if (!tcap_otid_dtid_list.isEmpty()) {
            filterGroupingCondition.put("tcap_otid-list", tcap_otid_dtid_list);
            filterGroupingCondition.put("tcap_dtid-list", tcap_otid_dtid_list);
        }

        return filterGroupingCondition;
    }

    public int processCamelQueryCount(HashMap<String, Object> filter) {
        return camelService.getSequenceDiagramQueryCount(filter, timezone);
    }

    public HashMap<String, Object> processCamelQuery(HashMap<String, Object> filter, int limit, int page) {
        HashMap<String, Object> response = new HashMap<>();
        int offset = 0;
        if (page > 1)
            offset = (page - 1) * limit;
        List<CamelData> resultData = camelService.getSequenceDiagramQuery(filter, timezone, limit, offset);
        response.put("data", resultData);
        response.put("tsharkQuery" , "");
        return response;
    }

    public HashMap<String, Object> processCamelResult(List<CamelData> resultData) {
        HashMap<String, Object> response = new HashMap<>();
        response.put("data", resultData);
        response.put("tsharkQuery" , "");
        return response;
    }

    public List<ConsolidatedDiagram> buildDiagram(List<CamelData> result) throws Exception {
        List<ConsolidatedDiagram> consolidatedDiagramList = new ArrayList<>();
        StringBuilder html;

        for (CamelData item : result) {
            html = new StringBuilder();
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
            consolidatedDiagram.setSeparator(true);
            consolidatedDiagram.setType(item.getTcap_mess_type());
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
            consolidatedDiagram.setProtocol("cap");
            consolidatedDiagram.setModal(html.toString());
            consolidatedDiagramList.add(consolidatedDiagram);
        }
        return consolidatedDiagramList;
    }
}
