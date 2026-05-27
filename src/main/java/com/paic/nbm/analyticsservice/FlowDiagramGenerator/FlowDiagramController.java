package com.paic.nbm.analyticsservice.FlowDiagramGenerator;

import com.paic.nbm.analyticsservice.ProtocolBuilder.ConsolidatedProtocolAnalytics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;

@Slf4j
@RestController
public class FlowDiagramController {

    ApplicationContext applicationContext;

    @Autowired
    ConsolidatedProtocolAnalytics consolidatedAnalytics;

    public FlowDiagramController(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @CrossOrigin(origins = "*")
    @PostMapping("buildDiagramAll/")
    public HashMap<String, Object> buildDiagramAllV2(@RequestBody Object fields) throws Exception {
        HashMap<String, Object> parameters = (HashMap<String, Object>) fields;
        return consolidatedAnalytics.buildSequenceDiagramAll(parameters);
    }
}