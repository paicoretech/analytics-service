package com.paic.nbm.analyticsservice.Entities;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity(name = "command_code")
public class CommandCodeCatalog {
    @Id
    private Long id;
    private Integer cmd_code;
    private String cmd_request;
    private String cmd_response;
    private String origin_packet;
}
