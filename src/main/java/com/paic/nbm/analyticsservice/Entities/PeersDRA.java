package com.paic.nbm.analyticsservice.Entities;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Data
@Entity(name = "dra_peers")
public class PeersDRA {
    @Id
    private Long id;
    private String friendly_name;
    private String ip_addr;
}
