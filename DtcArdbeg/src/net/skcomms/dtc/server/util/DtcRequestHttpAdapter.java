package net.skcomms.dtc.server.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.skcomms.dtc.server.DtcIniFactory;
import net.skcomms.dtc.server.model.DtcIni;
import net.skcomms.dtc.server.model.DtcRequestProperty;
import net.skcomms.dtc.shared.DtcRequest;
import net.skcomms.dtc.shared.DtcRequestParameter;

public class DtcRequestHttpAdapter {

  public static DtcRequest createDtcRequest(Map<String, String> params)
      throws IOException, FileNotFoundException {
    DtcRequest request = new DtcRequest();

    request.setPath(params.get("path"));
    request.setEncoding(params.get("charset"));
    request.setAppName(params.get("appName"));
    request.setApiNumber(params.get("apiNumber"));

    List<DtcRequestParameter> requestParams = DtcRequestHttpAdapter.getListFromMap(params);
    request.setRequestParameters(requestParams);
    return request;
  }

  private static List<DtcRequestParameter> getListFromMap(Map<String, String> urlParams)
      throws IOException, FileNotFoundException {
    DtcIni ini = DtcIniFactory.getIni(urlParams.get("path"));
    List<DtcRequestParameter> params = new ArrayList<DtcRequestParameter>();

    for (DtcRequestProperty prop : ini.getRequestProps()) {
      String value = urlParams.get(prop.getKey());
      params.add(new DtcRequestParameter(prop.getKey(), null, value));
    }
    params.add(new DtcRequestParameter("IP", null, urlParams.get("IP")));
    params.add(new DtcRequestParameter("Port", null, urlParams.get("Port")));

    return params;
  }

  private final DtcRequest request;

  private static Map<String, String> defaultCndParameters;
  static {
    Map<String, String> params = new HashMap<String, String>();
    params.put("path", "/kkeywords/204.ini");
    params.put("appName", "KKEYWORDSD");
    params.put("apiNumber", "100");
    params.put("APIVersion", "204");
    params.put("Nativequery", "Coffee and Donut");
    params.put("RevisionLevel", "1");
    params.put("FindKeywordAlias", "Y");
    params.put("FindKeywordAdult", "Y");
    params.put("FindKeywordList", "Y");
    params.put("charset", "euc-kr");
    params.put("Port", "7777");
    params.put("IP", "10.141.11.143");
    DtcRequestHttpAdapter.defaultCndParameters = Collections.unmodifiableMap(params);
  }

  public static DtcRequest createCndRequest(String nativeQuery) throws FileNotFoundException,
      IOException {
    Map<String, String> params = DtcRequestHttpAdapter.getCndRequestParameters(nativeQuery);
    return DtcRequestHttpAdapter.createDtcRequest(params);
  }

  public static Map<String, String> getCndRequestParameters(String nativeQuery) {
    Map<String, String> params = new HashMap<String, String>(
        DtcRequestHttpAdapter.defaultCndParameters);
    params.put("Nativequery", nativeQuery);
    return params;
  }

  public DtcRequestHttpAdapter(DtcRequest request) {
    this.request = request;
  }

  private void appendOrigin(StringBuilder url) {
    url.append("http://");
    url.append(this.request.getRequestParameter("IP"));
    url.append(":");
    url.append(this.request.getRequestParameter("Port"));
    url.append("/");
    url.append(this.request.getAppName());
    url.append("/");
    url.append(this.request.getApiNumber());
  }

  private void appendQueryString(StringBuilder url) throws UnsupportedEncodingException {
    url.append("?");
    url.append("Dummy1=1&Dummy2=1&Dummy3=1&Dummy4=1");

    for (DtcRequestParameter param : this.request.getRequestParameters()) {
      if (param.getKey().equals("IP") || param.getKey().equals("Port")) {
        continue;
      }
      url.append("&");
      url.append(param.getKey());
      url.append("=");
      url.append(URLEncoder.encode(DtcHelper.getOrElse(param.getValue(), ""),
          this.request.getCharset()));
    }
  }

  public String combineUrl() throws UnsupportedEncodingException {
    StringBuilder url = new StringBuilder();
    this.appendOrigin(url);
    this.appendQueryString(url);
    return url.toString();
  }
}
