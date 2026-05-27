package com.paic.nbm.analyticsservice.Entities;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.math.BigInteger;

@Data
@Entity(name = "gtp")
public class GtpData {
    @Id
    public BigInteger id;
    public String frames_list;
    public BigInteger time_epoch;
    @Column(name = "useconds_epoch")
    public Integer u_seconds_epoch;
    public String src_ip;
    public String dst_ip;
    public String gtp_version;
    public String gtp_message;
    public BigInteger gtp_teid;
    public String gtp_cause;
    public BigInteger gtp_seq_number;
    public String imsi;
    public String msisdn;
    public String pcap_filename;
    @Transient
    public String real_src_ip;
    @Transient
    public String real_dst_ip;

    public GtpData() {
    }

    public GtpData(String gtp_message, BigInteger gtp_seq_number) {
        this.gtp_message = gtp_message;
        this.gtp_seq_number = gtp_seq_number;
    }
}
