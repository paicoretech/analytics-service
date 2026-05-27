package com.paic.nbm.analyticsservice.PcapGenerator;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FileFrameData {
    @JsonProperty("pcapName")
    String pcapName;
    @JsonProperty("frame")
    String frames;

    public FileFrameData() {

    }

    public FileFrameData(String pcapName, String frames) {
        this.pcapName = pcapName;
        this.frames = frames;
    }
}
