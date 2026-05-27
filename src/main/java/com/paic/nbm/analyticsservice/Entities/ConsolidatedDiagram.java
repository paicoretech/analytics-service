package com.paic.nbm.analyticsservice.Entities;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ConsolidatedDiagram {
    String protocol;
    String firstNode;
    String from;
    String to;
    BigDecimal timestampToOrder;
    Integer uSecondsToOrder;
    String timestamp;
    String type;
    boolean separator;
    String title;
    String modal;
}
