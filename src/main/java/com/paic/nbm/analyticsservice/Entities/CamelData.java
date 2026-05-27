package com.paic.nbm.analyticsservice.Entities;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.math.BigInteger;

@Data
@Entity(name = "camel")
public class CamelData {
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

    public BigInteger tcap_tid;
    public BigInteger tcap_otid;
    public BigInteger tcap_dtid;
    public String tcap_mess_type;
    public String gsm_cld_party_bcd_num;
    public String called_party_number_digits;
    public String calling_party_number_digits;
    public String msisdn;
    public String imsi;
    public Integer camel_local;
    public String camel_calling_party_number;
    public String camel_called_party_number;
    public String pcap_filename;

    public CamelData() {

    }

    public CamelData(BigInteger tcap_otid, BigInteger tcap_dtid, String msisdn, String imsi) {
        this.tcap_otid = tcap_otid;
        this.tcap_dtid = tcap_dtid;
        this.msisdn = msisdn;
        this.imsi = imsi;
    }

    public CamelData(BigInteger tcap_tid) {
        this.tcap_tid = tcap_tid;
    }
}
