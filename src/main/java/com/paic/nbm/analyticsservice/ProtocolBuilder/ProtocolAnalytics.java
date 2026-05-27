package com.paic.nbm.analyticsservice.ProtocolBuilder;

import com.paic.nbm.analyticsservice.PcapGenerator.PcapSlice;

import java.util.ArrayList;
import java.util.HashMap;

public interface ProtocolAnalytics {
    ArrayList<String> buildSequenceDiagram(HashMap<String, String> parameters) throws Exception;
    String buildFrameQuery(PcapSlice pcapSlice);
}
