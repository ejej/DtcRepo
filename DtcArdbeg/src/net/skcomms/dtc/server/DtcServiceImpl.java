/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package net.skcomms.dtc.server;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;
import javax.swing.text.html.parser.ParserDelegator;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.skcomms.dtc.client.service.DtcService;
import net.skcomms.dtc.shared.DtcNodeMetaModel;
import net.skcomms.dtc.shared.DtcRequestInfoModel;
import net.skcomms.dtc.shared.DtcRequestParameterModel;
import net.skcomms.dtc.shared.DtcServiceVerifier;
import net.skcomms.dtc.shared.HttpRequestInfoModel;
import net.skcomms.dtc.shared.IpInfoModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.xml.sax.SAXException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class DtcServiceImpl extends RemoteServiceServlet implements DtcService {

  private static final String DTC_URL = "http://10.141.6.198/";

  public static String combineQueryString(Map<String, String> params, String encoding) {
    System.out.println("charset:" + encoding);
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, String> entry : params.entrySet()) {
      sb.append(entry.getKey());
      sb.append("=");

      if (entry.getValue() != null) {
        try {
          sb.append(URLEncoder.encode(entry.getValue(), encoding));
        } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
        }
      }
      else {
        sb.append("");
      }
      sb.append("&");
    }
    return sb.toString().substring(0, sb.length() - 1);
  }

  private static ParserCallback
      createDtcDirectoryParserCallback(final List<DtcNodeMetaModel> items) {
    return new ParserCallback() {
      private int textCount;
      private DtcNodeMetaModel currentItem = null;
      private boolean beforeHeaderRow = true;

      @Override
      public void handleEndOfLineString(String eol) {
        this.reset();
      }

      @Override
      public void handleEndTag(HTML.Tag tag, int pos) {
        if (tag == Tag.TR) {
          if (this.currentItem != null) {
            GWT.log("path:" + this.currentItem.getPath());
            items.add(this.currentItem);
            this.currentItem = null;
          }
        }
      }

      @Override
      public void handleStartTag(HTML.Tag tag, MutableAttributeSet a, int pos) {
        if (this.beforeHeaderRow) {
          if (tag == Tag.TR) {
            this.beforeHeaderRow = false;
          }
          return;
        }

        if (tag == Tag.TR) {
          this.currentItem = new DtcNodeMetaModel();
          this.textCount = 0;
        } else if (tag == Tag.A) {
          String href = DtcServiceImpl.getAttributeByName(a, "href");
          int index = href.indexOf('=');
          this.currentItem.setPath("/" + href.substring(index + 1));
        }
      }

      @Override
      public void handleText(char[] data, int pos) {
        if (this.currentItem != null) {
          this.setColumn(String.valueOf(data));
        }
      }

      private void reset() {
        this.textCount = 0;
        this.currentItem = null;
        this.beforeHeaderRow = true;
      }

      private void setColumn(String value) {
        switch (this.textCount++) {
        case 0:
          this.currentItem.setName(value);
          if (!this.currentItem.isLeaf()) {
            this.textCount++;
          }
          break;
        case 2:
          this.currentItem.setDescription(value);
          break;
        case 3:
          this.currentItem.setUpdateTime(value);
        }
      }
    };
  }

  /**
   * @param path
   * @param contents
   * @return
   * @throws IOException
   */
  static List<DtcNodeMetaModel> createDtcNodeInfosFrom(byte[] contents) throws IOException {
    final List<DtcNodeMetaModel> nodeInfos = new ArrayList<DtcNodeMetaModel>();

    ParserCallback callback = DtcServiceImpl.createDtcDirectoryParserCallback(nodeInfos);

    new ParserDelegator().parse(
        new InputStreamReader(new ByteArrayInputStream(contents), "windows-949"), callback, true);
    return nodeInfos;
  }

  /**
   * @param requestInfo
   * @param contents
   * @return
   */
  private static ParserCallback createDtcRequestFrameParserCallback(
      final DtcRequestInfoModel requestInfo,
      final byte[] contents) {
    return new ParserCallback() {

      private int scriptStart = 0;

      private int scriptEnd = 0;

      private boolean insideRequestTable;

      private List<DtcRequestParameterModel> params;

      private String currentKey;

      private IpInfoModel createIpInfoFrom(String javascript) {
        IpInfoModel ipInfo = new IpInfoModel();

        Pattern pattern = Pattern.compile("<input .* id=\"ip_text\" value=\"([0-9.]+)\" .*>");
        Matcher m = pattern.matcher(javascript);
        if (m.find()) {
          ipInfo.setIpText(m.group(1));
        }

        pattern = Pattern.compile("<option value=\"(.*)\">(.*)</option>");
        m = pattern.matcher(javascript);
        while (m.find()) {
          ipInfo.addOption(m.group(1), m.group(2));
        }

        return ipInfo;
      }

      @Override
      public void handleEndOfLineString(String eol) {
        this.reset();
      }

      @Override
      public void handleEndTag(HTML.Tag tag, int pos) {
        if (tag == Tag.SCRIPT) {
          if (this.scriptStart == 0) {
            this.scriptStart = pos;
          }
        }
        else if (tag == Tag.TABLE) {
          this.insideRequestTable = false;
          requestInfo.setParams(this.params);
        }
      }

      @Override
      public void handleSimpleTag(HTML.Tag tag, MutableAttributeSet a, int pos) {
        if (tag == Tag.INPUT) {
          if (this.insideRequestTable) {
            String value = DtcServiceImpl.getAttributeByName(a, "value");
            String name = DtcServiceImpl.getAttributeByName(a, "name");
            this.params.add(new DtcRequestParameterModel(this.currentKey, name, value));
          }
        }
      }

      @Override
      public void handleStartTag(HTML.Tag tag, MutableAttributeSet a, int pos) {
        if (tag == Tag.STYLE) {
          if (this.scriptEnd == 0) {
            this.scriptEnd = pos;
            try {
              String encoding = DtcServiceImpl.guessCharacterEncoding(contents);
              requestInfo.setEncoding(encoding);

              String javascript = new String(contents, this.scriptStart, this.scriptEnd
                  - this.scriptStart, encoding);
              IpInfoModel ipInfo = this.createIpInfoFrom(javascript);
              requestInfo.setIpInfo(ipInfo);
            } catch (UnsupportedEncodingException e) {
              e.printStackTrace();
            } catch (IOException e) {
              e.printStackTrace();
            }

          }
        }
        else if (tag == Tag.TD) {
        }
        else if (tag == Tag.TABLE) {
          String id = DtcServiceImpl.getAttributeByName(a, "id");
          if ("tblREQUEST".equals(id)) {
            this.insideRequestTable = true;
            this.params = new ArrayList<DtcRequestParameterModel>();
          }
        }
      }

      @Override
      public void handleText(char[] data, int pos) {
        if (this.insideRequestTable) {
          this.currentKey = new String(data);
        }
      }

      private void reset() {
        this.scriptStart = 0;
        this.scriptEnd = 0;
        this.insideRequestTable = false;
        this.currentKey = null;
        this.params = null;
      }
    };
  }

  /**
   * @param path
   * @param contents
   * @throws IOException
   * @throws UnsupportedEncodingException
   */
  static DtcRequestInfoModel createDtcRequestInfoFrom(String path, byte[] contents)
      throws UnsupportedEncodingException, IOException {
    DtcRequestInfoModel requestInfo = new DtcRequestInfoModel();
    requestInfo.setPath(path);

    String encoding = DtcServiceImpl.guessCharacterEncoding(contents);
    ParserCallback callback = DtcServiceImpl.createDtcRequestFrameParserCallback(requestInfo,
        contents);
    new ParserDelegator().parse(
        new InputStreamReader(new ByteArrayInputStream(contents), encoding), callback, true);

    return requestInfo;
  }

  static String createDtcResponse(HttpRequestInfoModel httpRequestInfo) {

    String response = null;
    String encoding = null;
    StringBuilder responseBuffer = new StringBuilder();

    URL conUrl;

    try {
      conUrl = new URL(httpRequestInfo.getUrl());
      HttpURLConnection httpCon = (HttpURLConnection) conUrl.openConnection();
      httpCon.setDoInput(true);
      httpCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      if (httpRequestInfo.getEncoding() != "") {
        httpCon.setRequestProperty("Content-Type", "text/html; charset="
            + httpRequestInfo.getEncoding());
      }

      httpCon.setRequestMethod(httpRequestInfo.getHttpMethod().toUpperCase());

      if (httpRequestInfo.getHttpMethod().toUpperCase().equals("POST")) {
        httpCon.setDoOutput(true);
        OutputStream postStream = httpCon.getOutputStream();

        postStream.write(httpRequestInfo.getRequestData().getBytes());
        postStream.flush();
        postStream.close();
      }

      byte[] content = DtcServiceImpl.readAllBytes(httpCon.getInputStream());
      encoding = DtcServiceImpl.guessCharacterEncoding(content);

      BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(
          content), encoding));
      String line;

      while ((line = reader.readLine()) != null) {
        responseBuffer.append(line);
        // System.out.println(line);
      }
      reader.close();

    } catch (IOException e) {
      System.out.println("IOException: " + e.getLocalizedMessage());
      e.printStackTrace();
    }

    Pattern regExp = Pattern.compile("src=\"([^\"]*)");
    Matcher matcher = regExp.matcher(responseBuffer.toString());

    if (matcher.find() == false) {
      response = responseBuffer.toString();
      return response;
    }
    else {

      String targetUrl = null;
      String responseUrl = null;
      String htmlData = null;

      try {
        responseUrl = URLDecoder.decode(matcher.group(0).split("/")[1], "utf-8");
        System.out.println("Response URL: " + responseUrl);
      } catch (UnsupportedEncodingException e1) {
        e1.printStackTrace();
      }

      HttpRequestInfoModel responseRequestInfo = new HttpRequestInfoModel();
      responseRequestInfo.setEncoding(encoding);
      responseRequestInfo.setHttpMethod("GET");

      String url = responseUrl.split("\\?u=")[0];
      String requestUrl = responseUrl.split("\\?u=")[1].split("\\?")[0];
      String query = responseUrl.split("\\?u=")[1].split("\\?")[1];

      System.out.println("url: " + url);
      System.out.println("requestURL: " + requestUrl);
      System.out.println("query: " + query);

      query = requestUrl + "?" + query;

      try {
        targetUrl = DtcServiceImpl.DTC_URL + url + "?u=" + URLEncoder.encode(query, "utf-8");
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }

      System.out.println("Target URL: " + targetUrl);
      responseRequestInfo.setUrl(targetUrl);
      response = DtcServiceImpl.createDtcResponse(responseRequestInfo);
      System.out.println("Response: " + response);

      if (responseUrl.contains("response_json.html")) {
        // JSONParser parser = new JSONParser();
        // DtcJsonToXmlHandler jsonHandler = new DtcJsonToXmlHandler();
        //
        // try {
        // parser.parse(response, jsonHandler);
        // } catch (ParseException e) {
        // e.printStackTrace();
        // }
        //
        // String xmlString = jsonHandler.toString();
        //
        // try {
        // htmlData =
        // DtcServiceImpl.getHtmlFromXml(xmlString.getBytes(encoding));
        // } catch (UnsupportedEncodingException e) {
        // e.printStackTrace();
        // }
        htmlData = response;

      } else if (responseUrl.contains("response_xml.html")) {

        try {
          htmlData = DtcServiceImpl.getHtmlFromXml(response.getBytes(encoding));
        } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
        }
      }
      return htmlData;
    }

  }

  public static String escapeForXml(String src) {
    return src.replaceAll("\\\\u000B", "&#11;").replaceAll("\\\\f", "&#12;");
  }

  private static String getAttributeByName(MutableAttributeSet a, String name) {
    Enumeration<?> attrs = a.getAttributeNames();
    while (attrs.hasMoreElements()) {
      Object attr = attrs.nextElement();
      if (attr.toString().equals(name)) {
        return (String) a.getAttribute(attr);
      }
    }
    return null;
  }

  private static String getEncodedQuery(String query, String encoding) {

    // decode UTF-8 query
    StringBuilder paramList = new StringBuilder();
    String decodedQuery = null;
    String encodedQuery = null;

    if (encoding.toLowerCase().equals("utf-8")) {
      return query;
    }

    String[] params = query.split("&");

    for (String param : params) {
      String[] pair = param.split("=");
      if (pair.length == 2) {
        try {

          decodedQuery = URLDecoder.decode(pair[1], "utf-8");
          encodedQuery = URLEncoder.encode(decodedQuery, encoding);
          // System.out.println("decodedQuery: " + decodedQuery);
          // System.out.println("encodedQuery: " + encodedQuery);
          paramList.append(pair[0]);
          paramList.append("=");
          paramList.append(encodedQuery);
          paramList.append("&");
        } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
        }
      } else {
        paramList.append(pair[0]);
        paramList.append("=");
        paramList.append("");
        paramList.append("&");
      }
    }

    return paramList.toString().substring(0, paramList.length() - 1);
  }

  /**
   * @param url
   * @return
   * @throws IOException
   */
  static byte[] getHtmlContents(String href) throws IOException {
    URL url = new URL(href);
    URLConnection conn = url.openConnection();
    byte[] contents = DtcServiceImpl.readAllBytes(conn.getInputStream());

    return contents;
  }

  public static String getHtmlFromXml(byte[] content) {
    DtcXmlToHtmlHandler dp = new DtcXmlToHtmlHandler();
    ByteArrayInputStream bufferInputStream = new ByteArrayInputStream(content);
    SAXParserFactory sf = SAXParserFactory.newInstance();
    SAXParser sp;
    try {
      sp = sf.newSAXParser();
      sp.parse(bufferInputStream, dp);

    } catch (ParserConfigurationException | SAXException | IOException e) {
      e.printStackTrace();
    }
    // sp.parse(new String(content, "windows-949"), dp);
    return dp.getHtml().toString();
  }

  static String guessCharacterEncoding(byte[] bytes) throws IOException {
    String string;
    if (bytes.length > 1024) {
      string = new String(bytes, 0, 1024);
    }
    else {
      string = new String(bytes);
    }
    if (string.contains("charset=utf-8") || string.contains("encoding=\"utf-8\"")) {
      return "utf-8";
    }
    return "windows-949";
  }

  static byte[] readAllBytes(InputStream is) throws IOException {
    DataInputStream dis = new DataInputStream(is);
    ByteArrayOutputStream bos = new ByteArrayOutputStream(40960);
    byte[] buffer = new byte[4096];
    int len;
    while ((len = dis.read(buffer)) != -1) {
      bos.write(buffer, 0, len);
    }
    return bos.toByteArray();
  }

  private EntityManagerFactory emf;

  @Override
  public List<DtcNodeMetaModel> getDir(String path) {
    if (!DtcServiceVerifier.isValidDirectoryPath(path)) {
      throw new IllegalArgumentException("Invalid directory:" + path);
    }

    EntityManager manager = this.emf.createEntityManager();
    manager.getTransaction().begin();
    manager.persist(new DtcLog("\"" + path + "\" requested."));
    manager.getTransaction().commit();
    manager.close();

    try {
      String href;
      if (path.equals("/")) {
        href = DtcServiceImpl.DTC_URL;
      }
      else if (path.endsWith(".ini")) {
        href = DtcServiceImpl.DTC_URL + "?c=" + path.substring(1);
      }
      else {
        href = DtcServiceImpl.DTC_URL + "?b=" + path.substring(1);
      }

      byte[] contents = DtcServiceImpl.getHtmlContents(href);
      return DtcServiceImpl.createDtcNodeInfosFrom(contents);
    } catch (MalformedURLException e) {
      e.printStackTrace();
      throw new IllegalArgumentException(e);
    } catch (IOException e) {
      e.printStackTrace();
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public DtcRequestInfoModel getDtcRequestPageInfo(String path) {
    if (!DtcServiceVerifier.isValidTestPage(path)) {
      throw new IllegalArgumentException("Invalid test page:" + path);
    }
    try {
      String href = DtcServiceImpl.DTC_URL + "request.html?c=" + path.substring(1);
      byte[] contents = DtcServiceImpl.getHtmlContents(href);
      return DtcServiceImpl.createDtcRequestInfoFrom(path, contents);
    } catch (MalformedURLException e) {
      e.printStackTrace();
      throw new IllegalArgumentException(e);
    } catch (IOException e) {
      e.printStackTrace();
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public String getDtcTestPageResponse(HttpRequestInfoModel httpRequestInfo) {

    String rawHtml = null;
    if (!DtcServiceVerifier.isValidMethod(httpRequestInfo.getHttpMethod())) {
      throw new IllegalArgumentException("Invalid HTTP Method: " + httpRequestInfo.getHttpMethod());
    }

    // change encoding
    String encodedData =
        DtcServiceImpl.getEncodedQuery(httpRequestInfo.getRequestData(),
            httpRequestInfo.getEncoding());
    httpRequestInfo.setRequestData(encodedData);

    // replace URL
    String targetUrl = DtcServiceImpl.DTC_URL + "response.html";
    httpRequestInfo.setUrl(targetUrl);
    rawHtml = DtcServiceImpl.createDtcResponse(httpRequestInfo);
    return rawHtml;
  }

  @Override
  public void init(ServletConfig config) throws ServletException {
    System.out.println("init() called.");
    super.init(config);
    WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext())
        .getAutowireCapableBeanFactory().autowireBean(this);
  }

  @Autowired
  void setEntityManagerFactory(EntityManagerFactory emf) {
    System.out.println("setEntityManagerFactory() called.");
    this.emf = emf;
  }

}
