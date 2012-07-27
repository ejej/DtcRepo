package net.skcomms.dtc.server.requesthandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import net.skcomms.dtc.server.model.DtcIni;
import net.skcomms.dtc.server.util.DtcHelper;
import net.skcomms.dtc.server.util.DtcRequestHttpAdapter;
import net.skcomms.dtc.shared.DtcRequest;

public class HttpRequestHandler implements RequestHandler {

  protected DtcResult createResult(String result, String charset) {
    return new DefaultDtcResult(result);
  }

  @Override
  public DtcResult handle(DtcRequest dtcRequest, DtcIni ini) throws IOException {
    String url = new DtcRequestHttpAdapter(dtcRequest).combineUrl();
    String charset = dtcRequest.getCharset();
    int TIMEOUT = 5000;

    URL conUrl = new URL(url);
    HttpURLConnection httpCon = (HttpURLConnection) conUrl.openConnection();
    httpCon.setConnectTimeout(TIMEOUT);
    httpCon.setReadTimeout(TIMEOUT);
    httpCon.setDoInput(true);
    httpCon.setRequestProperty("Content-Type", "text/html; charset=" + charset);

    InputStream is = httpCon.getInputStream();
    byte[] content = DtcHelper.readAllBytes(is);
    return this.createResult(new String(content, charset), charset);
  }

}
