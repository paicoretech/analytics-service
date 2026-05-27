package com.paic.nbm.analyticsservice.Entities;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.Transient;
import java.math.BigInteger;


@Data
@Entity(name = "sip")
public class SipData {
    @Id
    public BigInteger id;
    public String frames_list;
    public BigInteger time_epoch;
    @Column(name = "useconds_epoch")
    public Integer u_seconds_epoch;
    public String src_ip;
    public String dst_ip;
    public String method;
    public Integer status_code;
    public String status_line;
    public String call_id;
    public String from_user;
    public String from_original;
    public String to_user;
    public String to_original;
    public String supported;
    public String require;
    public String sdp_o_sessionid;
    public String sdp_o_version;
    public String pcap_filename;

    @Transient
    public String real_src_ip;
    @Transient
    public String real_dst_ip;

    public SipData() {
    }
}

