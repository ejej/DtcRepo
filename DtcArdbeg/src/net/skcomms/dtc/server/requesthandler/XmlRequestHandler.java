package net.skcomms.dtc.server.requesthandler;


public class XmlRequestHandler extends HttpRequestHandler {

  @Override
  protected DtcResult createResult(String result, String charset) {
    return new DtcXmlResult(result, charset);
  }

}
