package com.paic.nbm.analyticsservice.Entities;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.math.BigInteger;
import java.sql.Date;

@Data
@Entity(name = "http_ocs")
public class HttpOcsData {
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
    public Boolean http_is_request;
    public BigInteger http_response_in;
    public String http_request_method;
    public String http_request_uri;
    public String http_content_type;
    public Integer http_content_length;
    public Integer http_response_code;
    public String type;
    public Integer operation_id;
    public String cdpa;
    public String msisdn;
    public String rdn;
    public Integer period_duration;
    public Boolean call_active;
    public String start_time;
    public String end_time;
    public String status;
    public Integer status_code;
    public Integer max_call_period_duration;
    public String pcap_filename;
    public String dtmf_route;
    public String req_type;
    public String shadow_number;
    public String called;
    public String calling;
    public String msrn;
    public String phone;
    public Integer code;
    public Integer result;
    public String temp_cdpa;
    public String dual_num;
    public Integer mcc;
    public Integer mnc;
    public String imsi;
    @Transient
    public String real_src_ip;
    @Transient
    public String real_dst_ip;

    public HttpOcsData() {
    }

    public HttpOcsData(String src_ip, Integer src_port, String dst_ip, Integer dst_port) {
        this.src_ip = src_ip;
        this.src_port = src_port;
        this.dst_ip = dst_ip;
        this.dst_port = dst_port;
    }

    public HttpOcsData(BigInteger http_response_in) {
        this.http_response_in = http_response_in;
    }
}

