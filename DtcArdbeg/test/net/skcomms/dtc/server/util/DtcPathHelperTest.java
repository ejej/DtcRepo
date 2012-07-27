package net.skcomms.dtc.server.util;

import java.io.File;
import java.io.IOException;

import net.skcomms.dtc.server.DtcServiceImpl;

import org.junit.Assert;
import org.junit.Test;

public class DtcPathHelperTest {

  @Test
  public void testGetRootPath() throws IOException {
    Assert.assertEquals("sample/dtc/", DtcPathHelper.getRootPath());
    String absolutePath = DtcPathHelper.getRootPath() + "/";
    File file = new File(absolutePath);

    for (File item : file.listFiles(new DtcServiceImpl.DtcNodeFilter())) {
      System.out.println(item.getName());
    }
  }

}
