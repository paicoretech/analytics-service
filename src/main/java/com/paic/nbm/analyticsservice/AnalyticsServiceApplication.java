package com.paic.nbm.analyticsservice;

import com.paic.licenser.licenseValidator;
import com.paic.nbm.analyticsservice.Entities.CommandCodeCatalog;
import com.paic.nbm.analyticsservice.ProtocolBuilder.DiameterAnalytics;
import com.paic.nbm.analyticsservice.Utils.CommandCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@SpringBootApplication
public class AnalyticsServiceApplication {

    public static Map<String, CommandCode> _diameterCommandCodeMap = new HashMap<>();
    public static Map<String, String> _diameterDraPeersMap = new HashMap<>();
    public static Map<Integer, CommandCodeCatalog> _diameterCommandMap = new HashMap<>();

    public static Map<String, String> _ipNames = new HashMap<>();
    @Autowired
    DiameterAnalytics diameterAnalyticsService;
    @PostConstruct
    private void initCommandCodeMap() {
        _diameterCommandCodeMap = diameterAnalyticsService.loadCommandCodes();
        _diameterDraPeersMap = diameterAnalyticsService.loadDraPeers();
        _diameterCommandMap = diameterAnalyticsService.loadCommand();
        _ipNames = diameterAnalyticsService.loadIpNames();
    }
    public static void main(String[] args) {
        Thread checkLicense = new Thread(() -> {
            try {
                licenseValidator checker = new licenseValidator();
                checker.validate();
            } catch (Exception e) {
                log.error("Exception found during startup : {}", e.getMessage());
            }
        });
        checkLicense.start();
        SpringApplication.run(AnalyticsServiceApplication.class, args);
    }

}
