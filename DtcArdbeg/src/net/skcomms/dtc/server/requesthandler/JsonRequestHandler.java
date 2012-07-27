package net.skcomms.dtc.server.requesthandler;


public class JsonRequestHandler extends HttpRequestHandler {

  @Override
  protected DtcResult createResult(String result, String charset) {
    return new DtcJsonResult(result, charset);
  }

}
