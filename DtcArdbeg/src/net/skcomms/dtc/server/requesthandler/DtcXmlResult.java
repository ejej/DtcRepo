package net.skcomms.dtc.server.requesthandler;

import net.skcomms.dtc.server.util.DtcHelper;

public class DtcXmlResult implements DtcResult {

  private String result;

  private String charset;

  public DtcXmlResult(String result, String charset) {
    this.result = result;
    this.charset = charset;
  }

  @Override
  public String getResult() {
    return DtcHelper.getHtmlFromXml(this.result, this.charset);
  }

}
