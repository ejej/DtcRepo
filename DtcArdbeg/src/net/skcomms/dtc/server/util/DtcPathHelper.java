package net.skcomms.dtc.server.util;

import java.io.File;
import java.io.IOException;

public class DtcPathHelper {

  public static String getFilePath(String nodePath) throws IOException {
    return DtcPathHelper.getRootPath() + nodePath.substring(1);
  }

  public static String getNodePath(String filePath) throws IOException {
    return filePath.substring(DtcPathHelper.getRootPath().length() - 1).replace('\\', '/');
  }

  static String getRootPath() throws IOException {
    if (new File("/home/search/dtc").isDirectory()) {
      return "/home/search/dtc/";
    } else if (new File("sample/dtc").isDirectory()) {
      return "sample/dtc/";
    } else {
      return "../sample/dtc/";
    }

  }

}
