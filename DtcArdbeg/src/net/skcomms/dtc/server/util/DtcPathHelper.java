package net.skcomms.dtc.server.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

public class DtcPathHelper {

  public static class DtcNodeFilter implements FileFilter {

    @Override
    public boolean accept(File file) {
      return file.isDirectory() || (file.isFile() && file.getName().endsWith(".ini"));
    }
  }

  public static final Comparator<File> NODE_COMPARATOR = new Comparator<File>() {

    @Override
    public int compare(File arg0, File arg1) {
      if (arg0.isDirectory() != arg1.isDirectory()) {
        if (arg0.isDirectory()) {
          return -1;
        } else {
          return 1;
        }
      } else {
        return arg0.getName().compareTo(arg1.getName());
      }
    }

  };

  public static File[] getChildNodes(File file) {
    File[] files = file.listFiles(new DtcNodeFilter());
    Arrays.sort(files, DtcPathHelper.NODE_COMPARATOR);
    return files;
  }

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
