package net.skcomms.dtc.server.requesthandler;

import net.skcomms.dtc.server.util.DtcHelper;

public class DtcJsonResult implements DtcResult {

  private String charset;

  private String result;

  public DtcJsonResult(String result, String charset) {
    this.result = result;
    this.charset = charset;
  }

  @Override
  public String getResult() {
    return DtcHelper.getHtmlFromJson(this.result, this.charset);
  }

}
