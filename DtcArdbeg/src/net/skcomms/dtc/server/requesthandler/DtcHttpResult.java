package net.skcomms.dtc.server.requesthandler;


public class DtcHttpResult implements DtcResult {

  private String html;

  public DtcHttpResult(String html) {
    this.html = html;
  }

  @Override
  public String getResult() {
    return this.html;
  }

}
