package com.paic.nbm.analyticsservice.PcapGenerator;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PcapDownloaderInfo implements Serializable {

    @JsonProperty("tsharkQuery")
    private String tsharkQuery;

    @JsonProperty("pcapFileFrameList")
    private List<PcapFileFrame> pcapFileFrameList;
}
