package com.paic.nbm.analyticsservice.ProtocolBuilder;

import com.paic.nbm.analyticsservice.Entities.SipData;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import com.paic.nbm.analyticsservice.Entities.ConsolidatedDiagram;
import com.paic.nbm.analyticsservice.Service.SipService;
import com.paic.nbm.analyticsservice.Utils.UtilityFunctions;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component("SIP")

public class SIPAnalytics {
  @Autowired
  SipService sipService;

  @Getter
  @Setter
  private String timezone;

  public HashMap<String,Object> buildSIPGroupingCondition(
      String startDate,
      String endDate,
      String timezone,
      String ipSrc,
      String ipDst,
      List<String> msisdn) {
    HashMap<String,Object> filterGroupingCondition = new HashMap<>();
    filterGroupingCondition.put("startDate", startDate);
    filterGroupingCondition.put("endDate", endDate);

    List<String> resultGrouping = sipService.getGroupingCondition(startDate, endDate, timezone, ipSrc, ipDst, msisdn);
    resultGrouping.removeAll(Collections.singleton(null));

    if (!resultGrouping.isEmpty())
      filterGroupingCondition.put("call_id-list", resultGrouping);

    return filterGroupingCondition;
  }

  public int processSIPQueryCount(HashMap<String, Object> filter) {
    return sipService.getSequenceDiagramQueryCount(filter, timezone);
  }

  public  HashMap<String, Object> processSIPQuery(HashMap<String, Object> filter, int limit, int page) {
    HashMap<String, Object> response = new HashMap<>();
    int offset = 0;
    if (page > 1)
      offset = (page - 1) * limit;
    
    List<SipData> resultData = sipService.getSequenceDiagramQuery(filter, timezone, limit, offset);
    response.put("data", resultData);
    response.put("tsharkQuery" , "");
    return response;
  }

  public HashMap<String, Object> processSipResult(List<SipData> resultData) {
    HashMap<String, Object> response = new HashMap<>();
    response.put("data", resultData);
    response.put("tsharkQuery" , "");
    return response;
  }

  public List<ConsolidatedDiagram> buildDiagram(List<SipData> result) {
    List<ConsolidatedDiagram> consolidatedDiagramList = new ArrayList<>();
    try {
      StringBuilder html;
      for (SipData item : result) {
        html = new StringBuilder();
        ConsolidatedDiagram consolidatedDiagram = new ConsolidatedDiagram();
        consolidatedDiagram.setTimestampToOrder(new BigDecimal(item.getTime_epoch()));
        consolidatedDiagram.setUSecondsToOrder(item.getU_seconds_epoch());
        consolidatedDiagram.setTimestamp(UtilityFunctions.getDateFormat(item.getTime_epoch(), item.getU_seconds_epoch(), timezone));
        consolidatedDiagram.setFrom(item.getSrc_ip());
        consolidatedDiagram.setTo(item.getDst_ip());
        consolidatedDiagram.setSeparator(true);
        consolidatedDiagram.setType((item.getMethod() != null) ? item.getMethod() : item.getStatus_line());
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
        consolidatedDiagram.setProtocol("sip");
        consolidatedDiagram.setModal(html.toString());
        consolidatedDiagramList.add(consolidatedDiagram);
      }

    }catch (Exception ex) {
      log.error("Error on try to create the SIP Diagram " + ex.getMessage());
    }
    return consolidatedDiagramList;
  }
}
