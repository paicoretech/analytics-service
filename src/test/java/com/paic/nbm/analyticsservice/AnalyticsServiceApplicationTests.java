package com.paic.nbm.analyticsservice;

import com.paic.nbm.analyticsservice.FlowDiagramGenerator.FlowDiagramController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AnalyticsServiceApplicationTests {

    @Autowired
    FlowDiagramController flowDiagramController;

    @Test
    void contextLoads() throws Exception {
        //flowDiagramController.DRA_DATA("2022-02-01T00:00:00.000", "2022-03-04T23:59:00.000", "425019310002441","NA","NA","NA","NA");
    }
}
