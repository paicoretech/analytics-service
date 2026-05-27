package com.paic.nbm.analyticsservice.Entities;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.math.BigInteger;

@Data
@Entity(name = "http_ss7")
public class HttpSS7Data {
    @Id
    public BigInteger id;
    public String frames_list;
    public BigInteger time_epoch;
    @Column(name = "useconds_epoch")
    public Integer u_seconds_epoch;
    public String src_ip;
    public Integer src_port;
    public String dst_ip;
    public Integer dst_port;
    public BigInteger tcp_sequence;
    public BigInteger tcp_acknowledge;
    public String type;
    public Boolean http_is_request;
    public BigInteger http_response_in;
    public String http_request_method;
    public String http_request_uri;
    public String http_content_type;
    public Integer http_content_length;
    public Integer http_response_code;
    public String msisdn_orig;
    public String msisdn_dest;
    public String msc;
    public String sccp_cd_adr;
    public String imsi;
    public String session_id;
    public String text;
    public Boolean udhi;
    public String pcap_filename;
    @Transient
    public String real_src_ip;
    @Transient
    public String real_dst_ip;

    public HttpSS7Data() {

    }

    public HttpSS7Data(String src_ip, Integer src_port, String dst_ip, Integer dst_port) {
        this.src_ip = src_ip;
        this.src_port = src_port;
        this.dst_ip = dst_ip;
        this.dst_port = dst_port;
    }

    public HttpSS7Data(BigInteger http_response_in) {
        this.http_response_in = http_response_in;
    }

    public HttpSS7Data(BigInteger id, BigInteger http_response_in) {
        this.id = id;
        this.http_response_in = http_response_in;
    }

    public HttpSS7Data(String imsi) {
        this.imsi = imsi;
    }

    public HttpSS7Data(BigInteger time_epoch, String imsi) {
        this.time_epoch = time_epoch;
        this.imsi = imsi;
    }

}
