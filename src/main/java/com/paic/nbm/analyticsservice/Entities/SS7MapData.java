package com.paic.nbm.analyticsservice.Entities;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.math.BigInteger;

@Data
@Entity(name = "ss7map")
public class SS7MapData {
    @Id
    public BigInteger id;
    public String frames_list;
    public BigInteger time_epoch;
    @Column(name = "useconds_epoch")
    public Integer u_seconds_epoch;
    public String src_ip;
    public String dst_ip;
    public Integer mtp3_opc;
    public Integer mtp3_dpc;
    public String tcap_mess_type;
    public BigInteger tcap_tid;
    public BigInteger tcap_otid;
    public BigInteger tcap_dtid;
    public Integer tcap_result;
    public Integer gsm_op_code;
    public Integer gsm_component;
    public Integer gsm_error_code;
    public String msisdn_orig_address;
    public String msisdn_dest_address;
    public String imsi;
    public String pcap_filename;
    @Transient
    public String map_component;
    @Transient
    public String map_operation;
    @Transient
    public String map_error_code;
    @Transient
    public String operation_type;
    @Transient
    public Long count;

    public SS7MapData() {
    }

    public SS7MapData(BigInteger tcap_tid) {
        this.tcap_tid = tcap_tid;
    }

    public SS7MapData(String imsi) {
        this.imsi = imsi;
    }

    public SS7MapData(BigInteger time_epoch, Integer u_seconds_epoch, BigInteger tcap_tid) {
        this.time_epoch = time_epoch;
        this.u_seconds_epoch = u_seconds_epoch;
        this.tcap_tid = tcap_tid;
    }

    public SS7MapData(BigInteger tcap_tid, String imsi, BigInteger time_epoch) {
        this.tcap_tid = tcap_tid;
        this.imsi = imsi;
        this.time_epoch = time_epoch;
    }

    public SS7MapData(BigInteger time_epoch, Integer u_seconds_epoch, BigInteger tcap_otid, Integer gsm_op_code, String imsi, Long count) {
        this.time_epoch = time_epoch;
        this.u_seconds_epoch = u_seconds_epoch;
        this.tcap_otid = tcap_otid;
        this.gsm_op_code = gsm_op_code;
        this.imsi = imsi;
        this.count = count;
    }
}
