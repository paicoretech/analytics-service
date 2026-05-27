package com.paic.nbm.analyticsservice.PcapGenerator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PcapFileFrame implements Serializable {
    String fileName;
    List<String> frames;
}
