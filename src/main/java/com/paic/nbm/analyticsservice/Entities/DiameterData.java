package com.paic.nbm.analyticsservice.Entities;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.math.BigInteger;

@Data
@Entity(name = "diameter")
public class DiameterData {
    @Id
    public BigInteger id;
    public String frames_list;
    public BigInteger time_epoch;
    @Column(name = "useconds_epoch")
    public Integer u_seconds_epoch;
    public String src_ip;
    public String dst_ip;
    public Boolean request;
    public Integer command_code;
    public BigInteger hop_by_hop_id;
    public BigInteger end_to_end_id;
    public Integer result_code;
    public Integer exp_result_code;
    public String origin_host;
    public String origin_realm;
    public String destination_host;
    public String destination_realm;
    public String msisdn;
    public String imsi;
    public String pcap_filename;
    @Transient
    public String originRealmValue;
    @Transient
    public String destinationRealmValue;
    @Transient
    public String command;

    public DiameterData() {
    }

    public DiameterData(Integer command_code, BigInteger end_to_end_id) {
        this.command_code = command_code;
        this.end_to_end_id = end_to_end_id;
    }

    public DiameterData(String msisdn, String imsi) {
        this.msisdn = msisdn;
        this.imsi = imsi;
    }
}
