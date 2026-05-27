package com.paic.nbm.analyticsservice.ProtocolBuilder;

import com.paic.nbm.analyticsservice.Entities.CamelData;
import com.paic.nbm.analyticsservice.Entities.GtpData;
import com.paic.nbm.analyticsservice.Entities.DiameterData;
import com.paic.nbm.analyticsservice.Entities.HttpData;
import com.paic.nbm.analyticsservice.Entities.HttpOcsData;
import com.paic.nbm.analyticsservice.Entities.HttpSS7Data;
import com.paic.nbm.analyticsservice.Entities.ProtocolViewData;
import com.paic.nbm.analyticsservice.Entities.SipData;
import com.paic.nbm.analyticsservice.Entities.SmppData;
import com.paic.nbm.analyticsservice.Entities.SS7MapData;
import com.paic.nbm.analyticsservice.PcapGenerator.FileFrameData;
import com.paic.nbm.analyticsservice.PcapGenerator.PcapFileFrame;
import com.paic.nbm.analyticsservice.PcapGenerator.PcapDownloaderInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class WorkerProtocolThread implements Runnable {

    SS7CAPAnalytics ss7CAPAnalytics;
    SIPAnalytics sipAnalytics;
    SMPPAnalytics smppAnalytics;
    GTPAnalytics gtpAnalytics;
    HTTPAnalytics httpAnalytics;
    HTTPOCSAnalytics httpOcsAnalytics;
    HTTPSS7Analytics httpSS7Analytics;
    DiameterAnalytics diameterAnalytics;
    SS7MAPAnalytics ss7MAPAnalytics;
    ProtocolViewAnalytics protocolViewAnalytics;
    HashMap<String, Boolean> protocols;
    private  String protocol;
    private HashMap<String, Object> mapFilter;
    private String type;
    private int page;
    private int limit;

    public WorkerProtocolThread() {
    }

    public WorkerProtocolThread(SS7CAPAnalytics ss7CAPAnalytics,
                                SIPAnalytics sipAnalytics,
                                SMPPAnalytics smppAnalytics,
                                GTPAnalytics gtpAnalytics,
                                HTTPAnalytics httpAnalytics,
                                DiameterAnalytics diameterAnalytics,
                                SS7MAPAnalytics ss7MAPAnalytics,
                                HTTPOCSAnalytics httpocsAnalytics,
                                HTTPSS7Analytics httpSS7Analytics
    ) {
        this.ss7CAPAnalytics = ss7CAPAnalytics;
        this.sipAnalytics = sipAnalytics;
        this.smppAnalytics = smppAnalytics;
        this.gtpAnalytics = gtpAnalytics;
        this.httpAnalytics = httpAnalytics;
        this.diameterAnalytics = diameterAnalytics;
        this.ss7MAPAnalytics = ss7MAPAnalytics;
        this.httpOcsAnalytics = httpocsAnalytics;
        this.httpSS7Analytics = httpSS7Analytics;
    }

    public void setProtocolValues(String protocol, HashMap<String, Object> filter, String type, String timezone, int page, int limit) {
        this.protocol = protocol;
        this.mapFilter = filter;
        this.type = type;
        this.page = page;
        this.limit = limit;

        this.ss7CAPAnalytics.setTimezone(timezone);
        this.sipAnalytics.setTimezone(timezone);
        this.smppAnalytics.setTimezone(timezone);
        this.gtpAnalytics.setTimezone(timezone);
        this.httpAnalytics.setTimezone(timezone);
        this.diameterAnalytics.setTimezone(timezone);
        this.ss7MAPAnalytics.setTimezone(timezone);
        this.httpOcsAnalytics.setTimezone(timezone);
        this.httpSS7Analytics.setTimezone(timezone);

        if (protocolViewAnalytics != null)
            this.protocolViewAnalytics.setTimezone(timezone);
    }

    public void setProtocolViewAnalytics(ProtocolViewAnalytics protocolViewAnalytics) {
        this.protocolViewAnalytics = protocolViewAnalytics;
    }

    public void setProtocols(HashMap<String, Boolean> protocols) {
        this.protocols = protocols;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName()+" Start. Protocol = " + protocol);
        processCommand();
        System.out.println(Thread.currentThread().getName()+" End.");
    }

    private void processCommand() {
        HashMap<String, Object> response = new HashMap<>();
        int size = 0;
        try {
            if (type.equals("COUNT")) {
                switch (protocol) {
                    case "CAMEL":
                        size = ss7CAPAnalytics.processCamelQueryCount(mapFilter);
                        break;
                    case "SIP":
                        size = sipAnalytics.processSIPQueryCount(mapFilter);
                        break;
                    case "SMPP":
                        size = smppAnalytics.processSmppQueryCount(mapFilter);
                        break;
                    case "GTP":
                        size = gtpAnalytics.processGtpQueryCount(mapFilter);
                        break;
                    case "HTTP":
                        size = httpAnalytics.processHttpQueryCount(mapFilter);
                        break;
                    case "HTTP-OCS":
                        size = httpOcsAnalytics.processHttpOcsQueryCount(mapFilter);
                        break;
                    case "HTTP-SS7":
                        size = httpSS7Analytics.processHttpQueryCount(mapFilter);
                        break;
                    case "DIAMETER":
                        size = diameterAnalytics.processDiameterQueryCount(mapFilter);
                        break;
                    case "MAP":
                        size = ss7MAPAnalytics.processMapQueryCount(mapFilter);
                        break;
                    case "VIEW":
                        size = protocolViewAnalytics.processQueryCount(mapFilter, protocols);
                        break;
                }
                if (size > 0) {
                    log.info("Total of records: {}", size);
                    ConsolidatedProtocolAnalytics.resultQueryCount.put(protocol, size);
                } else
                    log.info("No records found");
            } else {
                switch (protocol) {
                    case "CAMEL":
                        response = ss7CAPAnalytics.processCamelQuery(mapFilter, limit, page);
                        List<CamelData> resultCamel = (List<CamelData>) response.get("data");
                        if (!resultCamel.isEmpty()) {
                            ConsolidatedProtocolAnalytics.pcapDataToDownload.put(protocol, convertToPcapDownloaderInfo(response.get("tsharkQuery").toString(), resultCamel.stream().map(data -> new FileFrameData(data.getPcap_filename(), data.getFrames_list())).collect(Collectors.toList())));
                            ConsolidatedProtocolAnalytics.resultDiagram.put(protocol, ss7CAPAnalytics.buildDiagram(resultCamel));
                        }
                        break;

                    case "SIP":
                        response = sipAnalytics.processSIPQuery(mapFilter, limit, page);
                        List<SipData> resultSip = (List<SipData>) response.get("data");
                        if (!resultSip.isEmpty()) {
                            ConsolidatedProtocolAnalytics.pcapDataToDownload.put(protocol, convertToPcapDownloaderInfo(response.get("tsharkQuery").toString(), resultSip.stream().map(data -> new FileFrameData(data.getPcap_filename(), data.getFrames_list())).collect(Collectors.toList())));
                            ConsolidatedProtocolAnalytics.resultDiagram.put(protocol, sipAnalytics.buildDiagram(resultSip));
                        }
                        break;

                    case "SMPP":
                        response =  smppAnalytics.processSmppQuery(mapFilter, limit, page);
                        List<SmppData> resultSmpp = (List<SmppData>) response.get("data");
                        if (!resultSmpp.isEmpty()) {
                            ConsolidatedProtocolAnalytics.pcapDataToDownload.put(protocol, convertToPcapDownloaderInfo(response.get("tsharkQuery").toString(), resultSmpp.stream().map(data -> new FileFrameData(data.getPcap_filename(), data.getFrames_list())).collect(Collectors.toList())));
                            ConsolidatedProtocolAnalytics.resultDiagram.put(protocol, smppAnalytics.buildDiagram(resultSmpp));
                        }
                        break;

                    case "GTP":
                        response = gtpAnalytics.processGtpQuery(mapFilter, limit, page);
                        List<GtpData> resultGtp = (List<GtpData>) response.get("data");
                        if (!resultGtp.isEmpty()) {
                            ConsolidatedProtocolAnalytics.pcapDataToDownload.put(protocol, convertToPcapDownloaderInfo(response.get("tsharkQuery").toString(), resultGtp.stream().map(data -> new FileFrameData(data.getPcap_filename(), data.getFrames_list())).collect(Collectors.toList())));
                            ConsolidatedProtocolAnalytics.resultDiagram.put(protocol, gtpAnalytics.buildDiagram(resultGtp));
                        }
                        break;

                    case "HTTP":
                        response = httpAnalytics.processHTTPQuery(mapFilter, limit, page);
                        List<HttpData> resultHttp = (List<HttpData>) response.get("data");
                        if (!resultHttp.isEmpty()) {
                            ConsolidatedProtocolAnalytics.pcapDataToDownload.put(protocol, convertToPcapDownloaderInfo(response.get("tsharkQuery").toString(), resultHttp.stream().map(data -> new FileFrameData(data.getPcap_filename(), data.getFrames_list())).collect(Collectors.toList())));
                            ConsolidatedProtocolAnalytics.resultDiagram.put(protocol, httpAnalytics.buildDiagram(resultHttp));
                        }
                        break;

                    case "HTTP-OCS":
                        response = httpOcsAnalytics.processHTTPOCSQuery(mapFilter, limit, page);
                        List<HttpOcsData> resultHttpOcs = (List<HttpOcsData>) response.get("data");
                        if (!resultHttpOcs.isEmpty()) {
                            ConsolidatedProtocolAnalytics.pcapDataToDownload.put(protocol, convertToPcapDownloaderInfo(response.get("tsharkQuery").toString(), resultHttpOcs.stream().map(data -> new FileFrameData(data.getPcap_filename(), data.getFrames_list())).collect(Collectors.toList())));
                            ConsolidatedProtocolAnalytics.resultDiagram.put(protocol, httpOcsAnalytics.buildDiagram(resultHttpOcs));
                        }
                        break;

                    case "HTTP-SS7":
                        response = httpSS7Analytics.processHTTPQuery(mapFilter, limit, page);
                        List<HttpSS7Data> resultHttpSS7 = (List<HttpSS7Data>) response.get("data");
                        if (!resultHttpSS7.isEmpty()) {
                            ConsolidatedProtocolAnalytics.pcapDataToDownload.put(protocol, convertToPcapDownloaderInfo(response.get("tsharkQuery").toString(), resultHttpSS7.stream().map(data -> new FileFrameData(data.getPcap_filename(), data.getFrames_list())).collect(Collectors.toList())));
                            ConsolidatedProtocolAnalytics.resultDiagram.put(protocol, httpSS7Analytics.buildDiagram(resultHttpSS7));
                        }
                        break;

                    case "DIAMETER":
                        response = diameterAnalytics.processDiameterQuery(mapFilter, limit, page);
                        List<DiameterData> resultDiameter = (List<DiameterData>) response.get("data");
                         if (!resultDiameter.isEmpty()) {
                            ConsolidatedProtocolAnalytics.pcapDataToDownload.put(protocol, convertToPcapDownloaderInfo(response.get("tsharkQuery").toString(), resultDiameter.stream().map(data -> new FileFrameData(data.getPcap_filename(), data.getFrames_list())).collect(Collectors.toList())));
                            ConsolidatedProtocolAnalytics.resultDiagram.put(protocol, diameterAnalytics.buildDiagram(resultDiameter));
                        }
                        break;

                    case "MAP":
                        response = ss7MAPAnalytics.processMapQuery(mapFilter, limit, page);
                        List<SS7MapData> resultMap = (List<SS7MapData>) response.get("data");
                        if (!resultMap.isEmpty()) {
                            ConsolidatedProtocolAnalytics.pcapDataToDownload.put(protocol, convertToPcapDownloaderInfo(response.get("tsharkQuery").toString(), resultMap.stream().map(data -> new FileFrameData(data.getPcap_filename(), data.getFrames_list())).collect(Collectors.toList())));
                            ConsolidatedProtocolAnalytics.resultDiagram.put(protocol, ss7MAPAnalytics.buildDiagram(resultMap));
                        }
                        break;

                    case "VIEW": {
                        List<CamelData> camelList = new ArrayList<>();
                        List<DiameterData> diameterList = new ArrayList<>();
                        List<GtpData> gtpList = new ArrayList<>();
                        List<HttpData> httpList = new ArrayList<>();
                        List<HttpOcsData> httpOcsList = new ArrayList<>();
                        List<HttpSS7Data> httpSS7List = new ArrayList<>();
                        List<SS7MapData> mapList = new ArrayList<>();
                        List<SmppData> smppList = new ArrayList<>();
                        List<SipData> sipList = new ArrayList<>();
                        List<ProtocolViewData> viewData = protocolViewAnalytics.processQuery(mapFilter, protocols, limit, page);

                        for (ProtocolViewData record : viewData) {
                            String protocolName = record.protocol;
                            switch (protocolName) {
                                case "CAMEL":
                                    camelList.add(record.getCamelData());
                                    break;
                                case "DIAMETER":
                                    diameterList.add(record.getDiameterData());
                                    break;
                                case "GTP":
                                    gtpList.add(record.getGtpData());
                                    break;
                                case "HTTP":
                                    httpList.add(record.getHttpData());
                                    break;
                                case "HTTP-OCS":
                                    httpOcsList.add(record.getHttpOcsData());
                                    break;
                                case "HTTP-SS7":
                                    httpSS7List.add(record.getHttpSS7Data());
                                    break;
                                case "SIP":
                                    sipList.add(record.getSipData());
                                    break;
                                case "SMPP":
                                    smppList.add(record.getSmppData());
                                    break;
                                case "MAP":
                                    mapList.add(record.getSS7MapData());
                                    break;
                                default:
                                    break;
                            }
                        }

                        if (!camelList.isEmpty()) {
                            response = ss7CAPAnalytics.processCamelResult(camelList);
                            List<CamelData> responseData = (List<CamelData>) response.get("data");
                            ConsolidatedProtocolAnalytics.pcapDataToDownload.put("CAMEL", convertToPcapDownloaderInfo(response.get("tsharkQuery").toString(), responseData.stream().map(data -> new FileFrameData(data.getPcap_filename(), data.getFrames_list())).collect(Collectors.toList())));
                            ConsolidatedProtocolAnalytics.resultDiagram.put("CAMEL", ss7CAPAnalytics.buildDiagram(responseData));
                        }
                        if (!diameterList.isEmpty()) {
                            response = diameterAnalytics.processDiameterResult(diameterList);
                            List<DiameterData> responseData = (List<DiameterData>) response.get("data");
                            ConsolidatedProtocolAnalytics.pcapDataToDownload.put("DIAMETER", convertToPcapDownloaderInfo(response.get("tsharkQuery").toString(), responseData.stream().map(data -> new FileFrameData(data.getPcap_filename(), data.getFrames_list())).collect(Collectors.toList())));
                            ConsolidatedProtocolAnalytics.resultDiagram.put("DIAMETER", diameterAnalytics.buildDiagram(responseData));
                        }
                        if (!gtpList.isEmpty()) {
                            response = gtpAnalytics.processGtpResult(gtpList);
                            List<GtpData> responseData = (List<GtpData>) response.get("data");
                            ConsolidatedProtocolAnalytics.pcapDataToDownload.put("GTP", convertToPcapDownloaderInfo(response.get("tsharkQuery").toString(), responseData.stream().map(data -> new FileFrameData(data.getPcap_filename(), data.getFrames_list())).collect(Collectors.toList())));
                            ConsolidatedProtocolAnalytics.resultDiagram.put("GTP", gtpAnalytics.buildDiagram(responseData));
                        }
                        if (!httpList.isEmpty()) {
                            response = httpAnalytics.processHttpResult(httpList);
                            List<HttpData> responseData = (List<HttpData>) response.get("data");
                            ConsolidatedProtocolAnalytics.pcapDataToDownload.put("HTTP", convertToPcapDownloaderInfo(response.get("tsharkQuery").toString(), responseData.stream().map(data -> new FileFrameData(data.getPcap_filename(), data.getFrames_list())).collect(Collectors.toList())));
                            ConsolidatedProtocolAnalytics.resultDiagram.put("HTTP", httpAnalytics.buildDiagram(responseData));
                        }
                        if (!httpOcsList.isEmpty()) {
                            response = httpOcsAnalytics.processHttpResult(httpOcsList);
                            List<HttpOcsData> responseData = (List<HttpOcsData>) response.get("data");
                            ConsolidatedProtocolAnalytics.pcapDataToDownload.put("HTTP-OCS", convertToPcapDownloaderInfo(response.get("tsharkQuery").toString(), responseData.stream().map(data -> new FileFrameData(data.getPcap_filename(), data.getFrames_list())).collect(Collectors.toList())));
                            ConsolidatedProtocolAnalytics.resultDiagram.put("HTTP-OCS", httpOcsAnalytics.buildDiagram(responseData));
                        }
                        if (!httpSS7List.isEmpty()) {
                            response = httpSS7Analytics.processHttpResult(httpSS7List);
                            List<HttpSS7Data> responseData = (List<HttpSS7Data>) response.get("data");
                            ConsolidatedProtocolAnalytics.pcapDataToDownload.put("HTTP-SS7", convertToPcapDownloaderInfo(response.get("tsharkQuery").toString(), responseData.stream().map(data -> new FileFrameData(data.getPcap_filename(), data.getFrames_list())).collect(Collectors.toList())));
                            ConsolidatedProtocolAnalytics.resultDiagram.put("HTTP-SS7", httpSS7Analytics.buildDiagram(responseData));
                        }
                        if (!mapList.isEmpty()) {
                            response = ss7MAPAnalytics.processMapResult(mapList);
                            List<SS7MapData> responseData = (List<SS7MapData>) response.get("data");
                            ConsolidatedProtocolAnalytics.pcapDataToDownload.put("MAP", convertToPcapDownloaderInfo(response.get("tsharkQuery").toString(), responseData.stream().map(data -> new FileFrameData(data.getPcap_filename(), data.getFrames_list())).collect(Collectors.toList())));
                            ConsolidatedProtocolAnalytics.resultDiagram.put("MAP", ss7MAPAnalytics.buildDiagram(responseData));
                        }
                        if (!smppList.isEmpty()) {
                            response = smppAnalytics.processSmppResult(smppList);
                            List<SmppData> responseData = (List<SmppData>) response.get("data");
                            ConsolidatedProtocolAnalytics.pcapDataToDownload.put("SMPP", convertToPcapDownloaderInfo(response.get("tsharkQuery").toString(), responseData.stream().map(data -> new FileFrameData(data.getPcap_filename(), data.getFrames_list())).collect(Collectors.toList())));
                            ConsolidatedProtocolAnalytics.resultDiagram.put("SMPP", smppAnalytics.buildDiagram(responseData));
                        }
                        if (!sipList.isEmpty()) {
                            response = sipAnalytics.processSipResult(sipList);
                            List<SipData> responseData = (List<SipData>) response.get("data");
                            ConsolidatedProtocolAnalytics.pcapDataToDownload.put("SIP", convertToPcapDownloaderInfo(response.get("tsharkQuery").toString(), responseData.stream().map(data -> new FileFrameData(data.getPcap_filename(), data.getFrames_list())).collect(Collectors.toList())));
                            ConsolidatedProtocolAnalytics.resultDiagram.put("SIP", sipAnalytics.buildDiagram(responseData));
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private PcapDownloaderInfo convertToPcapDownloaderInfo(String tsharkQuery, List<FileFrameData> data) {
        HashMap<String, List<String>> framesByFiles = new HashMap<>();
        PcapDownloaderInfo pcapDownloaderInfo = new PcapDownloaderInfo();
        data.forEach(fileFrameData -> {
            if (framesByFiles.containsKey(fileFrameData.getPcapName())) {
                framesByFiles.get(fileFrameData.getPcapName()).add(fileFrameData.getFrames());
            } else {
                List<String> frames = new ArrayList<>();
                frames.add(fileFrameData.getFrames());
                framesByFiles.put(fileFrameData.getPcapName(), frames);
            }
        });
        pcapDownloaderInfo.setTsharkQuery(tsharkQuery);
        pcapDownloaderInfo.setPcapFileFrameList(
                framesByFiles.keySet().stream().map(file -> new PcapFileFrame(file, framesByFiles.get(file))).collect(Collectors.toList())
        );
        return pcapDownloaderInfo;
    }
}
