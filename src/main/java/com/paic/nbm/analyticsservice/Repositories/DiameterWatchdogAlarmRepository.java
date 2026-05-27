package com.paic.nbm.analyticsservice.Repositories;

import com.paic.nbm.analyticsservice.Entities.DiameterWatchdogAlarm;
import org.springframework.data.jpa.repository.JpaRepository;
import java.math.BigInteger;

public interface DiameterWatchdogAlarmRepository
        extends JpaRepository<DiameterWatchdogAlarm, BigInteger> {
}