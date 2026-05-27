package com.paic.nbm.analyticsservice.AnalyticsAPI;

import com.paic.nbm.analyticsservice.Entities.DiameterWatchdogAlarm;
import com.paic.nbm.analyticsservice.Service.DiameterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.paic.nbm.analyticsservice.AnalyticsServiceApplication._diameterDraPeersMap;

@RestController
public class DwrMonitorController {

    @Autowired
    DiameterService diameterService;

    @CrossOrigin(origins = "*")
    @PostMapping("dwr/unanswered/")
    public List<Map<String, Object>> getWatchdogAlarms(@RequestBody Object fields) throws Exception {
        HashMap<String, String> parameters = (HashMap<String, String>) fields;

        String startDate = parameters.get("startDate").replace(" ", "T");
        String endDate   = parameters.get("endDate").replace(" ", "T");
        String timezone  = parameters.getOrDefault("timezone", "UTC");

        List<DiameterWatchdogAlarm> raw = diameterService.getWatchdogAlarms(startDate, endDate, timezone);

        DateTimeFormatter fmt = DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.of(timezone));

        return raw.stream().map(d -> {
            Map<String, Object> record = new HashMap<>();
            record.put("alarmType",  d.alarm_type);
            record.put("timestamp",  fmt.format(Instant.ofEpochSecond(d.time_epoch.longValue())));
            record.put("srcPeer",    _diameterDraPeersMap.getOrDefault(d.src_ip, d.src_ip));
            record.put("dstPeer",    _diameterDraPeersMap.getOrDefault(d.dst_ip, d.dst_ip));
            record.put("hopByHopId", d.hop_by_hop_id);
            record.put("pcapFile",   d.pcap_filename);
            return record;
        }).collect(Collectors.toList());
    }

    @CrossOrigin(origins = "*")
    @GetMapping("dwr/trace/download")
    public void downloadTrace(@RequestParam String filename, HttpServletResponse response) throws Exception {
        File file = new File(filename);
        if (!file.exists() || !file.isFile()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Trace file not found: " + filename);
            return;
        }
        String contentType = filename.endsWith(".pcapng") ? "application/x-pcapng" : "application/vnd.tcpdump.pcap";
        response.setContentType(contentType);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
        response.setContentLengthLong(file.length());
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.transferTo(response.getOutputStream());
        }
        response.getOutputStream().flush();
    }
}