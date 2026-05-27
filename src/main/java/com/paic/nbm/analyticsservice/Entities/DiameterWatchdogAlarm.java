package com.paic.nbm.analyticsservice.Entities;

import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigInteger;

@Data
@Entity(name = "diameter_watchdog_alarms")
public class DiameterWatchdogAlarm {
    @Id
    public BigInteger id;
    public BigInteger time_epoch;
    public Integer useconds_epoch;
    public String src_ip;
    public String dst_ip;
    public BigInteger hop_by_hop_id;
    public BigInteger end_to_end_id;
    public Integer command_code;
    public String alarm_type;
    public String pcap_filename;
}