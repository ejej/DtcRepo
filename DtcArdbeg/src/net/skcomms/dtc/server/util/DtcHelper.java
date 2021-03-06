package net.skcomms.dtc.server.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.skcomms.dtc.server.DtcIniFactory;
import net.skcomms.dtc.server.DtcXmlToHtmlHandler;
import net.skcomms.dtc.server.model.DtcAtp;
import net.skcomms.dtc.server.model.DtcIni;
import net.skcomms.dtc.shared.DtcNodeMeta;

import org.xml.sax.SAXException;

public class DtcHelper {

  private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat(
      "yyyy/MM/dd HH:mm:ss");

  /**
   * @param node
   * @return
   * @throws IOException
   */
  public static DtcNodeMeta createDtcNodeMeta(File node) throws IOException {
    DtcNodeMeta nodeMeta = new DtcNodeMeta();
    nodeMeta.setName(node.getName());
    if (node.isDirectory()) {
      nodeMeta.setDescription("디렉토리");
      nodeMeta.setPath(DtcPathHelper.getNodePath(node.getPath()) + "/");
    } else {
      DtcIni ini = new DtcIniFactory().createFrom(node.getPath());
      nodeMeta.setDescription(ini.getBaseProp("DESCRIPTION").getValue());
      nodeMeta.setPath(DtcPathHelper.getNodePath(node.getPath()));
    }
    String updateTime = DtcHelper.SIMPLE_DATE_FORMAT.format(new Date(node.lastModified()));
    nodeMeta.setUpdateTime(updateTime);
    return nodeMeta;
  }

  public static String getFormattedDateString(Date date) {
    return DtcHelper.SIMPLE_DATE_FORMAT.format(date);
  }

  public static String getFormattedDateString(long time) {
    return DtcHelper.getFormattedDateString(new Date(time));
  }

  public static String getHtmlFromAtp(DtcAtp response, DtcIni ini) {
    return response.toHtmlString(ini);
  }

  public static String getHtmlFromJson(String json, String charset) {
    return json;
  }

  public static String getHtmlFromXml(String xml, String encoding) {
    try {
      DtcXmlToHtmlHandler dp = new DtcXmlToHtmlHandler();
      ByteArrayInputStream bufferInputStream = new ByteArrayInputStream(xml.getBytes(encoding));
      SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
      sp.parse(bufferInputStream, dp);

      return dp.getHtml().toString();
    } catch (SAXException e) {
      e.printStackTrace();
      throw new IllegalStateException("Invalid XML:" + xml);
    } catch (Exception e) {
      e.printStackTrace();
      throw new IllegalStateException(e);
    }
  }

  public static <T> T getOrElse(T obj, T alternative) {
    return obj != null ? obj : (T) alternative;
  }

  public static byte[] readAllBytes(InputStream is) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream(40960);
    byte[] buffer = new byte[4096];
    int len;
    while ((len = is.read(buffer)) != -1) {
      bos.write(buffer, 0, len);
    }
    return bos.toByteArray();
  }

}
