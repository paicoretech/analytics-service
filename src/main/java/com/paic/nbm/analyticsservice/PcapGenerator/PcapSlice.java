package com.paic.nbm.analyticsservice.PcapGenerator;

import com.paic.nbm.analyticsservice.ProtocolBuilder.ProtocolAnalytics;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class PcapSlice {
    @Getter
    @Setter
    String fileIndex;
    @Getter
    @Setter
    HashMap<String, String> parameters;
    @Getter
    @Setter
    String protocolName;
    @Getter
    @Setter
    List<String> frameList;

    public PcapSlice() {
        frameList = new ArrayList<>();
        parameters = new HashMap<>();
    }

    public String getFrameQuery(ApplicationContext applicationContext) {
        String delimiter = " ";
        String frameQuery = null;

        if (applicationContext != null) {
            ProtocolAnalytics protocolBuilder = applicationContext.getBean(protocolName.toUpperCase(), ProtocolAnalytics.class);
            frameQuery = protocolBuilder.buildFrameQuery(this);
        }

        if (frameQuery == null) {
            String framesGroup = frameList.stream().collect(Collectors.joining(delimiter));

            frameQuery = "\"frame.number in {" + framesGroup + "\"}";
        }

        return frameQuery;
    }

}
