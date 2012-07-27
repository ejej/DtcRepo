package net.skcomms.dtc.server.util;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class DtcPathHelperTest {

  @Test
  public void testGetRootPath() throws IOException {
    Assert.assertEquals("sample/dtc/", DtcPathHelper.getRootPath());
    String absolutePath = DtcPathHelper.getRootPath() + "/";
    File file = new File(absolutePath);

    for (File item : file.listFiles(new DtcPathHelper.DtcNodeFilter())) {
      System.out.println(item.getName());
    }
  }

  @Test
  public void testNodeComparator() {
    File file1 = new File("sample/dtc/dtc.ini");
    File file2 = new File("sample/dtc/habong");
    File[] files = { file1, file2 };
    Arrays.sort(files, DtcPathHelper.NODE_COMPARATOR);

    Assert.assertEquals(false, file1.isDirectory());
    Assert.assertEquals(true, file2.isDirectory());
    Assert.assertEquals("habong", files[0].getName());
  }

}
