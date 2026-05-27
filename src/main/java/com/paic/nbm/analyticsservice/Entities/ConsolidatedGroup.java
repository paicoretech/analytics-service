package com.paic.nbm.analyticsservice.Entities;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class ConsolidatedGroup {
    private String data_source;
    @SerializedName("CAP_Operation")
    private String cap_operation;
    @SerializedName("e212.imsi")
    private String e212_imsi;
    @SerializedName("e164.msisdn")
    private String e164_msisdn;
    @SerializedName("m3ua.protocol_data_opc")
    private String m3ua_protocol_data_opc;
    @SerializedName("m3ua.protocol_data_dpc")
    private String m3ua_protocol_data_dpc;
    @SerializedName("tcap.otid")
    private String tcap_otid;
    @SerializedName("tcap.dtid")
    private String tcap_dtid;
    @SerializedName("sip.Call-ID")
    private String call_id;
    @SerializedName("smpp.sequence_number")
    private String smpp_sequence_number;
    @SerializedName("smpp.command_id")
    private String smpp_command_id;
    @SerializedName("tcp.ack_raw")
    private String tcp_ack_raw;
    @SerializedName("ip.src")
    private String ip_src;
    @SerializedName("ip.dst")
    private String ip_dst;
    @SerializedName("gtp.seq_number")
    private String gtp_seq_number;
    @SerializedName("gtp.message")
    private String gtp_message;
    @SerializedName("gtpv2.seq")
    private String gtpv2_seq;
    @SerializedName("gtpv2.message_type")
    private String gtpv2_message_type;
    @SerializedName("IP-Destination")
    private String ip_destination;
    @SerializedName("IP-Source")
    private String ip_source;
    @SerializedName("tcp.dstport")
    private String tcp_dstport;
    @SerializedName("tcp.srcport")
    private String tcp_srcport;
    @SerializedName("diameter.endtoendid")
    private String diameter_endtoendid;
    @SerializedName("diameter.cmd.code")
    private String diameter_cmd_code;
    @SerializedName("diameter.Session-Id")
    private String diameter_Session_Id;
}
