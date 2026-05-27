package com.paic.nbm.analyticsservice.Entities;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.math.BigInteger;

@Data
@Entity(name = "smpp")
public class SmppData {
    @Id
    public BigInteger id;
    public String frames_list;
    public BigInteger time_epoch;
    @Column(name = "useconds_epoch")
    public Integer u_seconds_epoch;
    public String src_ip;
    public String dst_ip;
    public String command_id;
    public Integer sequence_number;
    public String source_addr;
    public String destination_addr;
    public String command_status;
    public String pcap_filename;
    @Transient
    public String real_src_ip;
    @Transient
    public String real_dst_ip;

    public SmppData() {

    }

    public SmppData(String src_ip, String dst_ip, Integer sequence_number) {
        this.src_ip = src_ip;
        this.dst_ip = dst_ip;
        this.sequence_number = sequence_number;
    }
}
