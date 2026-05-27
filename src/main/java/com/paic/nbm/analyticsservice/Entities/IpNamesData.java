package com.paic.nbm.analyticsservice.Entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity(name = "ip_names")
@AllArgsConstructor
@NoArgsConstructor
public class IpNamesData {
  @Id
  public Long id;
  private String friendly_name;
  private String ip_addr;
  private Integer type;
}

