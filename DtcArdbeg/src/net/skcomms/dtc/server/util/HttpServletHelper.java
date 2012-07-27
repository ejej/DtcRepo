package net.skcomms.dtc.server.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class HttpServletHelper {

  public static Map<String, String> rebuildParameterMap(HttpServletRequest req) {
    Map<String, String> params = new HashMap<String, String>();
    for (Object element : req.getParameterMap().entrySet()) {
      @SuppressWarnings("unchecked")
      Entry<String, String[]> entry = (Entry<String, String[]>) element;
      params.put(entry.getKey(), entry.getValue()[0]);
    }
    return params;
  }

  public static void writeHtmlResponse(HttpServletResponse resp, String htmlBody,
      String charset)
      throws IOException {
    resp.setCharacterEncoding(charset);
    resp.setContentType("text/html");
    resp.getWriter().write("<!DOCTYPE html><html><head>");
    resp.getWriter().write(
        "<meta http-equiv=\"content-type\" content=\"text/html; charset=" + charset + "\">");
    resp.getWriter().write("<link type=\"text/css\" rel=\"stylesheet\" href=\"../DtcArdbeg.css\">");
    resp.getWriter().write("</head><body>");
    resp.getWriter().write(htmlBody);
    resp.getWriter().write("</body></html>");
    resp.getWriter().close();
  }

}
