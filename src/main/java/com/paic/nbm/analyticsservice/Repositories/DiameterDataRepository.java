
package com.paic.nbm.analyticsservice.Repositories;

import com.paic.nbm.analyticsservice.Entities.DiameterData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigInteger;
import java.util.List;

public interface DiameterDataRepository extends JpaRepository<DiameterData, Long> {

}
