package net.skcomms.dtc.server.requesthandler;

public class DefaultDtcResult implements DtcResult {

  private String result;

  public DefaultDtcResult(String result) {
    this.result = result;
  }

  @Override
  public String getResult() {
    return this.result;
  }

}
