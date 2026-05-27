package com.paic.nbm.analyticsservice.ExternalRequest;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "imsi-response")
public class ImsiResponse {
    private String msisdn;
    private List<String> imsiList;

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    @XmlElement(name = "imsi")
    public List<String> getImsiList() {
        return imsiList;
    }

    public void setImsiList(List<String> imsiList) {
        this.imsiList = imsiList;
    }

    @Override
    public String toString() {
        return "{msisdn: " + msisdn + ", imsiList: " + (imsiList == null ? "NULL" : imsiList.size()) + "}";
    }
}
