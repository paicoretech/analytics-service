package com.paic.nbm.analyticsservice.Repositories;

import com.paic.nbm.analyticsservice.Entities.CommandCodeCatalog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommandCodeRepository extends JpaRepository<CommandCodeCatalog, Integer> {
}
