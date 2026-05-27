package com.paic.nbm.analyticsservice.ProtocolBuilder;

import com.paic.nbm.analyticsservice.Entities.CommandCodeCatalog;
import com.paic.nbm.analyticsservice.Entities.ConsolidatedDiagram;
import com.paic.nbm.analyticsservice.Entities.DiameterData;
import com.paic.nbm.analyticsservice.Entities.DiameterDictionary;
import com.paic.nbm.analyticsservice.Entities.IpNamesData;
import com.paic.nbm.analyticsservice.Entities.PeersDRA;
import com.paic.nbm.analyticsservice.Service.DiameterService;
import com.paic.nbm.analyticsservice.Utils.CommandCode;
import com.paic.nbm.analyticsservice.Utils.UtilityFunctions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Component("DIAMETER")
@RequiredArgsConstructor
public class DiameterAnalytics {
    @Autowired
    DiameterService diameterService;

    @Getter
    @Setter
    private String timezone;

    public Map<String, CommandCode> loadCommandCodes() {
        Map<String, CommandCode> response = new HashMap<>();
        List<DiameterDictionary> diameterDictionaryList = diameterService.getAllDiameterDictionary();
        try {
            diameterDictionaryList.forEach(dic -> {
                CommandCode cmd = new CommandCode();
                cmd.setApplicationId(dic.getApplicationId());
                cmd.setApplicationName(dic.getApplicationName());
                cmd.setAvpCode(dic.getAvpCode());
                cmd.setAvpGrouped(dic.getAvpGrouped());
                cmd.setAvpName(dic.getAvpName());
                cmd.setAvpType(dic.getAvpType());
                cmd.setCmdCode(dic.getCmdCode());
                cmd.setCmdName(dic.getCmdName());
                cmd.setCmdRequest(dic.getCmdRequest());
                cmd.setVendorId(dic.getVendorId());
                String cmdId = cmd.buildObjectId();
                if (!cmdId.isEmpty()) {
                    response.put(UtilityFunctions.generateMD5Hash(cmdId), cmd);
                }
            });
        } catch (Exception ex) {
            log.warn("No data found in the COMMAND_CODE table on db");
        }
        return response;
    }

    public Map<String, String> loadDraPeers() {
        Map<String, String> response = new HashMap<>();
        List<PeersDRA> peersDRAList = diameterService.getAllPeersDRA();
        try {
            peersDRAList.forEach(dic -> response.put(dic.getIp_addr(), dic.getFriendly_name()));
        } catch (Exception ex) {
            log.warn("No friendly name found in the COMMAND_CODE table on db");
        }
        return response;
    }

    public Map<Integer, CommandCodeCatalog> loadCommand() {
        Map<Integer, CommandCodeCatalog> response = new HashMap<>();
        List<CommandCodeCatalog> list = diameterService.getAllCommandCode();
        try {
            list.forEach(dic -> response.put(dic.getCmd_code(), dic));
        } catch (Exception ex) {
            log.warn("No data found in the COMMAND_CODE");
        }
        return response;
    }

    public Map<String, String> loadIpNames() {
        Map<String, String> response = new HashMap<>();
        List<IpNamesData> ipNamesDataList = diameterService.getAllIpNames();
        try {
            ipNamesDataList.forEach(dic -> response.put(dic.getIp_addr(), dic.getFriendly_name()));
        } catch (Exception ex) {
            log.warn("No data found in the IP table on db");
        }
        return response;
    }

    public HashMap<String, Object> buildGroupingCondition(String startDate, String endDate, String timezone, List<String> imsi, List<String> msisdn) {
        HashMap<String, Object> filterGroupingCondition = new HashMap<>();
        List<String> msisdn_filter = new ArrayList<>();
        List<String> imsi_filter = new ArrayList<>();

        filterGroupingCondition.put("startDate", startDate);
        filterGroupingCondition.put("endDate", endDate);

        if (!msisdn.isEmpty() || !imsi.isEmpty()) {
            if (!msisdn.isEmpty())
                filterGroupingCondition.put("msisdn", msisdn);
            if (!imsi.isEmpty())
                filterGroupingCondition.put("imsi", imsi);
        } else {
            List<DiameterData> resultGrouping = diameterService.getGroupingCondition(startDate, endDate, timezone, imsi, msisdn);
            resultGrouping.forEach(data -> {
                if (data.getMsisdn() != null) {
                    if (!msisdn_filter.contains(data.getMsisdn())) {
                        msisdn_filter.add(data.getMsisdn());
                    }
                }
                if (data.getImsi() != null) {
                    if (!imsi_filter.contains(data.getImsi())) {
                        imsi_filter.add(data.getImsi());
                    }
                }
            });

            if (!msisdn_filter.isEmpty())
                filterGroupingCondition.put("msisdn-list", msisdn_filter);

            if (!imsi_filter.isEmpty())
                filterGroupingCondition.put("imsi-list", imsi_filter);
        }

        return filterGroupingCondition;
    }

    public HashMap<String, Object> processDiameterQuery( HashMap<String, Object> filter, int limit, int page) {
        HashMap<String, Object> response = new HashMap<>();
        int offset = 0;
        if (page > 1)
            offset = (page - 1) * limit;

        List<DiameterData> resultData =  diameterService.getSequenceDiagramQuery(filter, timezone, limit, offset);
        response.put("data", resultData);
        //
        // response.put("tsharkQuery" , getFrameQuery(resultData));
        //
        response.put("tsharkQuery", "");
        return response;
    }

    public HashMap<String, Object> processDiameterResult(List<DiameterData> diameterList) {
        HashMap<String, Object> response = new HashMap<>();

        List<DiameterData> resultData =  diameterService.getSequenceDiagram(diameterList);
        response.put("data", resultData);
        response.put("tsharkQuery" , "");
        return response;
    }

    public int processDiameterQueryCount(HashMap<String, Object> filter) {
        return diameterService.getSequenceDiagramQueryCount(filter, timezone);
    }

    public  List<ConsolidatedDiagram> buildDiagram(List<DiameterData> result) {
        List<ConsolidatedDiagram> consolidatedDiagramList = new ArrayList<>();
        try {
            StringBuilder html;
            for (DiameterData item : result) {
                html = new StringBuilder();
                ConsolidatedDiagram consolidatedDiagram = new ConsolidatedDiagram();
                consolidatedDiagram.setTimestampToOrder(new BigDecimal(item.getTime_epoch()));
                consolidatedDiagram.setUSecondsToOrder(item.getU_seconds_epoch());
                consolidatedDiagram.setTimestamp(UtilityFunctions.getDateFormat(item.getTime_epoch(), item.getU_seconds_epoch(), timezone));
                consolidatedDiagram.setFrom(item.getOriginRealmValue());
                consolidatedDiagram.setTo(item.getDestinationRealmValue());
                consolidatedDiagram.setSeparator(item.getCommand().contains("Request"));
                consolidatedDiagram.setType(item.getCommand());
                String safeStringValue;
                Class<?> clazz = item.getClass();
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
                        // Ignoring some non required columns
                        html.append("<tr>");
                        html.append("<td>").append(name).append("</td>");
                        html.append("<td>").append(safeStringValue).append("</td>");
                        html.append("</tr>");
                    }
                }
                consolidatedDiagram.setProtocol("diameter");
                consolidatedDiagram.setModal(html.toString());
                consolidatedDiagramList.add(consolidatedDiagram);

            }
        } catch (Exception ex){
            log.error("Error on try to create the Diameter Diagram {}", ex.getMessage());
        }
        return consolidatedDiagramList;
    }
}
