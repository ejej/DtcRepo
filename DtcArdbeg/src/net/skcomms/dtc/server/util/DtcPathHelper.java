package net.skcomms.dtc.server.util;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import net.skcomms.dtc.server.util.DtcHelper.DtcNodeFilter;


public class DtcPathHelper {

  public static String getDtcPath(File file) throws IOException {
    if (file.isDirectory()) {
      return file.getPath().substring(DtcPathHelper.getRootPath().length()).replace('\\', '/') + "/";
    } else {
      return file.getPath().substring(DtcPathHelper.getRootPath().length()).replace('\\', '/');
    }
  }

  public static String getDtcPath(String path) throws IOException {
    return "/" + path.substring(DtcPathHelper.getRootPath().length() + 1);
  }

  public static String getRootPath() throws IOException {
    if (new File("/home/search/dtc").isDirectory()) {
      return "/home/search/dtc";
    } else if (new File("sample/dtc").isDirectory()) {
      return "sample/dtc";
    } else {
      return "../sample/dtc";
    }
  
  }

  public static String getFilePath(String dtcPath) throws IOException {
    return getRootPath() + dtcPath;
  }

  public static File[] getChildNodes(File file) {
    File[] files = file.listFiles(new DtcHelper.DtcNodeFilter());
    Arrays.sort(files, DtcHelper.NODE_COMPARATOR);
    return files;
  }

}
