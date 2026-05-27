package com.paic.nbm.analyticsservice.ExternalRequest;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "msisdn-response")
public class MsisdnResponse {
    private String imsi;
    private List<String> msisdnList;

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    @XmlElement(name = "msisdn")
    public List<String> getMsisdnList() {
        return msisdnList;
    }

    public void setMsisdnList(List<String> msisdnList) {
        this.msisdnList = msisdnList;
    }

    @Override
    public String toString() {
        return "{imsi: " + imsi + ", msisdnList: " + (msisdnList == null ? "NULL" : msisdnList.size()) + "}";
    }
}
