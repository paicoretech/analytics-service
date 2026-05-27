package com.paic.nbm.analyticsservice.Entities;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity(name = "diameter_dictionary")
public class DiameterDictionary {
    @Id
    private int id;
    @Column(name = "aplication_id")
    private int applicationId;
    @Column(name = "aplication_name")
    private String applicationName;
    @Column(name = "avp_code")
    private int avpCode;
    @Column(name = "avp_name")
    private String avpName;
    @Column(name = "avp_type")
    private String avpType;
    @Column(name = "avp_grouped")
    private Boolean avpGrouped;
    @Column(name = "command_code")
    private int cmdCode;
    @Column(name = "command_name")
    private String cmdName;
    @Column(name = "command_request")
    private Boolean cmdRequest;
    @Column(name = "vendor_id")
    private String vendorId;
}
