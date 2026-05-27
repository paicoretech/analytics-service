package com.paic.nbm.analyticsservice.Utils;

public class CommandCode {
   int applicationId;
  private String applicationName;
  private int avpCode;
  private String avpName;
  private String avpType;
  private Boolean avpGrouped;
  private int cmdCode;
  private String cmdName;
  private Boolean cmdRequest;
  private String vendorId;

  public CommandCode() {
  }

  public CommandCode(int applicationId, String applicationName, int avpCode, String avpName, Boolean avpGrouped, int cmdCode, String cmdName, Boolean cmdRequest, String vendorId, String avpType) {
    this.applicationId = applicationId;
    this.applicationName = applicationName;
    this.avpCode = avpCode;
    this.avpName = avpName;
    this.avpGrouped = avpGrouped;
    this.cmdCode = cmdCode;
    this.cmdName = cmdName;
    this.cmdRequest = cmdRequest;
    this.vendorId = vendorId;
    this.avpType = avpType;
  }

  public int getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(int applicationId) {
    this.applicationId = applicationId;
  }

  public String getApplicationName() {
    return applicationName;
  }

  public void setApplicationName(String applicationName) {
    this.applicationName = applicationName;
  }

  public int getAvpCode() {
    return avpCode;
  }

  public void setAvpCode(int avpCode) {
    this.avpCode = avpCode;
  }

  public String getAvpName() {
    return avpName;
  }

  public void setAvpName(String avpName) {
    this.avpName = avpName;
  }

  public Boolean getAvpGrouped() {
    return avpGrouped;
  }

  public void setAvpGrouped(Boolean avpGrouped) {
    this.avpGrouped = avpGrouped;
  }

  public String getCmdName() {
    return cmdName;
  }

  public void setCmdName(String cmdName) {
    this.cmdName = cmdName;
  }

  public Boolean getCmdRequest() {
    return cmdRequest;
  }

  public void setCmdRequest(Boolean cmdRequest) {
    this.cmdRequest = cmdRequest;
  }

  public String getVendorId() {
    return vendorId;
  }

  public void setVendorId(String vendorId) {
    this.vendorId = vendorId;
  }

  public int getCmdCode() {
    return cmdCode;
  }

  public void setCmdCode(int cmdCode) {
    this.cmdCode = cmdCode;
  }

  public String getAvpType() {
    return avpType;
  }

  public void setAvpType(String avpType) {
    this.avpType = avpType;
  }
  public String buildObjectId() {
    StringBuilder builder = new StringBuilder();
    builder.append(getApplicationId());
    builder.append("+");
    builder.append(getCmdCode());
    builder.append("+");
    builder.append(getAvpCode());
    return builder.toString();
  }

  public String buildCmdData() {
    StringBuilder builder = new StringBuilder();
    builder.append("AVP: ");
    builder.append(this.getAvpName());
    builder.append("(");
    builder.append(this.getAvpCode());
    builder.append(")");
    return builder.toString();
  }
}
