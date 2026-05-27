package com.paic.nbm.analyticsservice.Entities;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.math.BigInteger;

@Data
@Entity(name = "http")
public class HttpData {
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
    public String smpp_src_addr;
    public String smpp_dst_addr;
    public BigInteger smpp_seq_number;
    @Transient
    public BigInteger tcap_tid;
    public BigInteger tcap_otid;
    public BigInteger tcap_dtid;
    public String camel_orig_address;
    public String camel_dest_address;
    public String msisdn;
    public String imsi;
    public BigInteger diam_e2e_id;
    public String diam_session_id;
    public Integer diam_result_code;
    public String diam_origin_host;
    public String diam_origin_realm;
    public String diam_destination_host;
    public String diam_destination_realm;
    public String pcap_filename;
    @Transient
    public String real_src_ip;
    @Transient
    public String real_dst_ip;

    public HttpData(String src_ip, Integer src_port, String dst_ip, Integer dst_port) {
        this.src_ip = src_ip;
        this.src_port = src_port;
        this.dst_ip = dst_ip;
        this.dst_port = dst_port;
    }

    public HttpData(BigInteger http_response_in) {
        this.http_response_in = http_response_in;
    }

    public HttpData() {

    }
}
