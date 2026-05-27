package com.paic.nbm.analyticsservice.Entities;

import lombok.Data;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.math.BigInteger;

@Data
@Entity(name = "protocol_view")
public class ProtocolViewData {

    // General
    public String protocol;

    @Id
    public BigInteger id;
    public BigInteger time_epoch;
    public Integer useconds_epoch;
    public String src_ip;
    public String dst_ip;
    public String frames_list;
    public String imsi;
    public String msisdn;
    public String pcap_filename;

    // Diameter
    public Integer command_code;
    public String destination_host;
    public String destination_realm;
    public BigInteger end_to_end_id;
    public Integer exp_result_code;
    public BigInteger hop_by_hop_id;
    public String origin_host;
    public String origin_realm;
    public Boolean request;
    public Integer result_code;

    // MAP
    public String msisdn_orig_address;
    public String msisdn_dest_address;
    public Integer gsm_component;
    public Integer gsm_error_code;
    public Integer gsm_op_code;
    public Integer mtp3_dpc;
    public Integer mtp3_opc;
    public Integer tcap_result;
    public BigInteger tcap_tid;
    public BigInteger tcap_dtid;
    public BigInteger tcap_otid;
    public String tcap_mess_type;

    // SMPP
    public String command_id;
    public String command_status;
    public String destination_addr;
    public Integer sequence_number;
    public String source_addr;

    // GTP
    public String gtp_cause;
    public String gtp_message;
    public BigInteger gtp_seq_number;
    public BigInteger gtp_teid;
    public String gtp_version;

    // SIP
    public String call_id;
    public String from_user;
    public String from_original;
    public String to_user;
    public String to_original;
    public String method;
    public String require;
    public String status_line;
    public String supported;
    public String sdp_o_sessionid;
    public String sdp_o_version;

    // CAP
    public String called_party_number_digits;
    public String calling_party_number_digits;
    public String camel_called_party_number;
    public String camel_calling_party_number;
    public Integer camel_local;
    public String gsm_cld_party_bcd_num;

    // HTTP
    public String camel_dest_address;
    public String camel_orig_address;
    public String diam_destination_host;
    public String diam_destination_realm;
    public BigInteger diam_e2e_id;
    public String diam_origin_host;
    public String diam_origin_realm;
    public Integer diam_result_code;
    public String diam_session_id;
    public Integer dst_port;
    public Integer http_content_length;
    public String http_content_type;
    public Boolean http_is_request;
    public String http_request_method;
    public String http_request_uri;
    public Integer http_response_code;
    public BigInteger http_response_in;
    public String smpp_dst_addr;
    public BigInteger smpp_seq_number;
    public String smpp_src_addr;
    public Integer src_port;
    public BigInteger tcp_acknowledge;
    public BigInteger tcp_sequence;
    public String type;

    // HTTP-OCS
    @Nullable
    public Boolean call_active;
    public String called;
    public String calling;
    public String cdpa;
    public Integer code;
    public String dtmf_route;
    public String dual_num;
    public String end_time;
    public Integer max_call_period_duration;
    public Integer mcc;
    public Integer mnc;
    public String msrn;
    public Integer operation_id;
    public Integer period_duration;
    public String phone;
    public String rdn;
    public String req_type;
    public Integer result;
    public String shadow_number;
    public String start_time;
    public String status;
    public Integer status_code;
    public String temp_cdpa;

    // HTTP-SS7
    public String msisdn_orig;
    public String msisdn_dest;
    public String msc;
    public String sccp_cd_adr;
    public String session_id;
    public String text;
    @Nullable
    public Boolean udhi;

    public ProtocolViewData() {
    }

    public ProtocolViewData(String protocol, BigInteger id, BigInteger time_epoch, Integer useconds_epoch, String frames_list, String msisdn,
                            String imsi, String pcap_filename, Integer gsm_component, Integer gsm_op_code, Integer mtp3_dpc,
                            Integer mtp3_opc, Integer tcap_result, BigInteger tcap_tid, BigInteger tcap_dtid, BigInteger tcap_otid,
                            String  tcap_mess_type) {

        this.protocol = protocol;
        this.id = id;
        this.time_epoch = time_epoch;
        this.useconds_epoch = useconds_epoch;
        this.frames_list = frames_list;
        this.msisdn = msisdn;
        this.imsi = imsi;
        this.pcap_filename = pcap_filename;
        this.gsm_component = gsm_component;
        this.gsm_op_code = gsm_op_code;
        this.mtp3_dpc = mtp3_dpc;
        this.mtp3_opc = mtp3_opc;
        this.tcap_result = tcap_result;
        this.tcap_tid = tcap_tid;
        this.tcap_dtid = tcap_dtid;
        this.tcap_otid = tcap_otid;
        this.tcap_mess_type = tcap_mess_type;
    }

    public CamelData getCamelData() {
        var data = new CamelData();
        data.setId(this.id);
        data.setTime_epoch(this.time_epoch);
        data.setU_seconds_epoch(this.useconds_epoch);
        data.setSrc_ip(this.src_ip);
        data.setDst_ip(this.dst_ip);
        data.setFrames_list(this.frames_list);
        data.setMsisdn(this.msisdn);
        data.setImsi(this.imsi);
        data.setPcap_filename(this.pcap_filename);
        data.setMtp3_opc(this.mtp3_opc);
        data.setMtp3_dpc(this.mtp3_dpc);
        data.setTcap_tid(this.tcap_tid);
        data.setTcap_otid(this.tcap_otid);
        data.setTcap_dtid(this.tcap_dtid);
        data.setTcap_mess_type(this.tcap_mess_type);
        data.setGsm_cld_party_bcd_num(this.gsm_cld_party_bcd_num);
        data.setCalled_party_number_digits(this.called_party_number_digits);
        data.setCalling_party_number_digits(this.calling_party_number_digits);
        data.setCamel_called_party_number(this.camel_called_party_number);
        data.setCamel_calling_party_number(this.camel_calling_party_number);
        data.setCamel_local(this.camel_local);
        data.setGsm_cld_party_bcd_num(this.gsm_cld_party_bcd_num);

        return data;
    }

    public DiameterData getDiameterData() {
        var data = new DiameterData();
        data.setId(this.id);
        data.setTime_epoch(this.time_epoch);
        data.setU_seconds_epoch(this.useconds_epoch);
        data.setSrc_ip(this.src_ip);
        data.setDst_ip(this.dst_ip);
        data.setFrames_list(this.frames_list);
        data.setMsisdn(this.msisdn);
        data.setImsi(this.imsi);
        data.setPcap_filename(this.pcap_filename);
        data.setRequest(this.request);
        data.setCommand_code(this.command_code);
        data.setHop_by_hop_id(this.hop_by_hop_id);
        data.setEnd_to_end_id(this.end_to_end_id);
        data.setResult_code(this.result_code);
        data.setExp_result_code(this.exp_result_code);
        data.setOrigin_host(this.origin_host);
        data.setOrigin_realm(this.origin_realm);
        data.setDestination_host(this.destination_host);
        data.setDestination_realm(this.destination_realm);
        data.setOriginRealmValue(this.origin_realm);
        data.setDestinationRealmValue(this.destination_realm);
        //data.setCommand(this.command);

        return data;
    }

    public GtpData getGtpData() {
        var data = new GtpData();
        data.setId(this.id);
        data.setTime_epoch(this.time_epoch);
        data.setU_seconds_epoch(this.useconds_epoch);
        data.setSrc_ip(this.src_ip);
        data.setDst_ip(this.dst_ip);
        data.setReal_src_ip(this.src_ip);
        data.setReal_dst_ip(this.dst_ip);
        data.setFrames_list(this.frames_list);
        data.setMsisdn(this.msisdn);
        data.setImsi(this.imsi);
        data.setPcap_filename(this.pcap_filename);
        data.setGtp_version(this.gtp_version);
        data.setGtp_message(this.gtp_message);
        data.setGtp_teid(this.gtp_teid);
        data.setGtp_cause(this.gtp_cause);
        data.setGtp_seq_number(this.gtp_seq_number);
        data.setGtp_cause(this.gtp_cause);

        return data;
    }

    public HttpData getHttpData() {
        var data = new HttpData();
        data.setId(this.id);
        data.setTime_epoch(this.time_epoch);
        data.setU_seconds_epoch(this.useconds_epoch);
        data.setSrc_ip(this.src_ip);
        data.setSrc_port(this.src_port);
        data.setDst_ip(this.dst_ip);
        data.setDst_port(this.dst_port);
        data.setReal_src_ip(this.src_ip);
        data.setReal_dst_ip(this.dst_ip);
        data.setFrames_list(this.frames_list);
        data.setMsisdn(this.msisdn);
        data.setImsi(this.imsi);
        data.setPcap_filename(this.pcap_filename);
        data.setTcp_sequence(this.tcp_sequence);
        data.setTcp_acknowledge(this.tcp_acknowledge);
        data.setType(this.type);
        data.setHttp_is_request(this.http_is_request);
        data.setHttp_response_in(this.http_response_in);
        data.setHttp_request_method(this.http_request_method);
        data.setHttp_request_uri(this.http_request_uri);
        data.setHttp_content_type(this.http_content_type);
        data.setHttp_content_length(this.http_content_length);
        data.setHttp_response_code(this.http_response_code);
        data.setSmpp_src_addr(this.smpp_src_addr);
        data.setSmpp_dst_addr(this.smpp_dst_addr);
        data.setSmpp_seq_number(this.smpp_seq_number);
        data.setTcap_tid(this.tcap_tid);
        data.setTcap_otid(this.tcap_otid);
        data.setTcap_dtid(this.tcap_dtid);
        data.setCamel_dest_address(this.camel_dest_address);
        data.setCamel_orig_address(this.camel_orig_address);
        data.setDiam_e2e_id(this.diam_e2e_id);
        data.setDiam_session_id(this.diam_session_id);
        data.setDiam_result_code(this.diam_result_code);
        data.setDiam_origin_host(this.diam_origin_host);
        data.setDiam_origin_realm(this.diam_origin_realm);
        data.setDiam_destination_host(this.diam_destination_host);
        data.setDiam_destination_realm(this.diam_destination_realm);

        return data;
    }

    public HttpOcsData getHttpOcsData() {
        var data = new HttpOcsData();
        data.setId(this.id);
        data.setTime_epoch(this.time_epoch);
        data.setU_seconds_epoch(this.useconds_epoch);
        data.setSrc_ip(this.src_ip);
        data.setSrc_port(this.src_port);
        data.setDst_ip(this.dst_ip);
        data.setDst_port(this.dst_port);
        data.setReal_src_ip(this.src_ip);
        data.setReal_dst_ip(this.dst_ip);
        data.setFrames_list(this.frames_list);
        data.setMsisdn(this.msisdn);
        data.setImsi(this.imsi);
        data.setPcap_filename(this.pcap_filename);
        data.setTcp_sequence(this.tcp_sequence);
        data.setTcp_acknowledge(this.tcp_acknowledge);
        data.setType(this.type);
        data.setHttp_is_request(this.http_is_request);
        data.setHttp_request_method(this.http_request_method);
        data.setHttp_request_uri(this.http_request_uri);
        data.setHttp_content_type(this.http_content_type);
        data.setHttp_content_length(this.http_content_length);
        data.setHttp_response_code(this.http_response_code);
        data.setType(this.type);
        data.setOperation_id(this.operation_id);
        data.setRdn(this.rdn);
        data.setPeriod_duration(this.period_duration);
        data.setCall_active(this.call_active);
        data.setStart_time(this.start_time);
        data.setEnd_time(this.end_time);
        data.setStatus(this.status);
        data.setStatus_code(this.status_code);
        data.setMax_call_period_duration(this.max_call_period_duration);
        data.setDtmf_route(this.dtmf_route);
        data.setReq_type(this.req_type);
        data.setShadow_number(this.shadow_number);
        data.setCalled(this.called);
        data.setCalling(this.calling);
        data.setMsrn(this.msrn);
        data.setPhone(this.phone);
        data.setCode(this.code);
        data.setResult(this.result);
        data.setTemp_cdpa(this.temp_cdpa);
        data.setDual_num(this.dual_num);
        data.setMcc(this.mcc);
        data.setMnc(this.mnc);

        return data;
    }

    public HttpSS7Data getHttpSS7Data() {
        var data = new HttpSS7Data();
        data.setId(this.id);
        data.setTime_epoch(this.time_epoch);
        data.setU_seconds_epoch(this.useconds_epoch);
        data.setSrc_ip(this.src_ip);
        data.setSrc_port(this.src_port);
        data.setDst_ip(this.dst_ip);
        data.setDst_port(this.dst_port);
        data.setReal_src_ip(this.src_ip);
        data.setReal_dst_ip(this.dst_ip);
        data.setFrames_list(this.frames_list);
        data.setMsisdn_orig(this.msisdn_orig);
        data.setMsisdn_dest(this.msisdn_dest);
        data.setImsi(this.imsi);
        data.setPcap_filename(this.pcap_filename);
        data.setTcp_sequence(this.tcp_sequence);
        data.setTcp_acknowledge(this.tcp_acknowledge);
        data.setType(this.type);
        data.setHttp_is_request(this.http_is_request);
        data.setHttp_response_in(this.http_response_in);
        data.setHttp_request_method(this.http_request_method);
        data.setHttp_request_uri(this.http_request_uri);
        data.setHttp_content_type(this.http_content_type);
        data.setHttp_content_length(this.http_content_length);
        data.setHttp_response_code(this.http_response_code);
        data.setMsc(this.msc);
        data.setSccp_cd_adr(this.sccp_cd_adr);
        data.setSession_id(this.session_id);
        data.setText(this.text);
        data.setUdhi(this.udhi);

        return data;
    }

    public SipData getSipData() {
        var data = new SipData();
        data.setId(this.id);
        data.setTime_epoch(this.time_epoch);
        data.setU_seconds_epoch(this.useconds_epoch);
        data.setSrc_ip(this.src_ip);
        data.setDst_ip(this.dst_ip);
        data.setReal_src_ip(this.src_ip);
        data.setReal_dst_ip(this.dst_ip);
        data.setFrames_list(this.frames_list);
        data.setPcap_filename(this.pcap_filename);
        data.setMethod(this.method);
        data.setStatus_code(this.status_code);
        data.setStatus_line(this.status_line);
        data.setCall_id(this.call_id);
        data.setFrom_user(this.from_user);
        data.setFrom_original(this.from_original);
        data.setTo_user(this.to_user);
        data.setTo_original(this.to_original);
        data.setSupported(this.supported);
        data.setRequire(this.require);
        data.setSdp_o_sessionid(this.sdp_o_sessionid);
        data.setSdp_o_version(this.sdp_o_version);

        return data;
    }

    public SmppData getSmppData() {
        var data = new SmppData();
        data.setId(this.id);
        data.setTime_epoch(this.time_epoch);
        data.setU_seconds_epoch(this.useconds_epoch);
        data.setSrc_ip(this.src_ip);
        data.setDst_ip(this.dst_ip);
        data.setReal_src_ip(this.src_ip);
        data.setReal_dst_ip(this.dst_ip);
        data.setFrames_list(this.frames_list);
        data.setPcap_filename(this.pcap_filename);
        data.setSequence_number(this.sequence_number);
        data.setSource_addr(this.source_addr);
        data.setDestination_addr(this.destination_addr);
        data.setCommand_id(this.command_id);
        data.setCommand_status(this.command_status);

        return data;
    }

    public SS7MapData getSS7MapData() {
        var data = new SS7MapData();
        data.setId(this.id);
        data.setTime_epoch(this.time_epoch);
        data.setU_seconds_epoch(this.useconds_epoch);
        data.setSrc_ip(this.src_ip);
        data.setDst_ip(this.dst_ip);
        data.setFrames_list(this.frames_list);
        data.setMsisdn_orig_address(this.msisdn_orig_address);
        data.setMsisdn_dest_address(this.msisdn_dest_address);
        data.setImsi(this.imsi);
        data.setPcap_filename(this.pcap_filename);
        data.setMtp3_opc(this.mtp3_opc);
        data.setMtp3_dpc(this.mtp3_dpc);
        data.setTcap_mess_type(this.tcap_mess_type);
        data.setTcap_tid(this.tcap_tid);
        data.setTcap_otid(this.tcap_otid);
        data.setTcap_dtid(this.tcap_dtid);
        data.setTcap_result(this.tcap_result);
        data.setGsm_op_code(this.gsm_op_code);
        data.setGsm_component(this.gsm_component);
        data.setGsm_error_code(this.gsm_error_code);

        return data;
    }
}
