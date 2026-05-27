package com.paic.nbm.analyticsservice.Entities;

import java.math.BigInteger;

public class TcapFilter {
    private BigInteger tcapTid;
    private BigInteger timeEpoch;

    public TcapFilter(BigInteger tcapTid, BigInteger timeEpoch) {
        this.tcapTid = tcapTid;
        this.timeEpoch = timeEpoch;
    }

    public BigInteger getTcapTid() {
        return tcapTid;
    }

    public void setTcapTid(BigInteger tcapTid) {
        this.tcapTid = tcapTid;
    }

    public BigInteger getTimeEpoch() {
        return timeEpoch;
    }

    public void setTimeEpoch(BigInteger timeEpoch) {
        this.timeEpoch = timeEpoch;
    }
}
