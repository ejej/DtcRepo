package net.skcomms.dtc.server.model;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Test;

public class DtcIniTest {

  @Test
  public void testIpPattern() {
    DtcIni ini = new DtcIni();

    ini.setBaseProp(new DtcBaseProperty("IP", "10.101.2.1"));
    Assert.assertEquals(1, ini.getIps().size());
    Assert.assertEquals(0, ini.getErrors().size());

    ini.setBaseProp(new DtcBaseProperty("IP", "{ \"10.101.2.2\" }"));
    System.out.println(ini.getIps());
    Assert.assertEquals(2, ini.getIps().size());
    Assert.assertEquals(0, ini.getErrors().size());

    ini.setBaseProp(new DtcBaseProperty("IP", " { \"10.101.2.3\" \"abc de\" } "));
    System.out.println(ini.getIps());
    Assert.assertEquals(3, ini.getIps().size());
    Assert.assertEquals("abc de", ini.getIps().get("10.101.2.3"));
    Assert.assertEquals(0, ini.getErrors().size());

    ini.setBaseProp(new DtcBaseProperty("IP",
        " { \"10.101.2.4\" \"abc de\" } { \"10.101.2.5\" \"1289381.1\" } "));
    System.out.println(ini.getIps());
    Assert.assertEquals(5, ini.getIps().size());
    Assert.assertEquals(0, ini.getErrors().size());

    ini.setBaseProp(new DtcBaseProperty("IP", "10.101.2.!"));
    Assert.assertEquals(5, ini.getIps().size());
    Assert.assertEquals(1, ini.getErrors().size());
    System.out.println(ini.getErrors());

    ini.setBaseProp(new DtcBaseProperty(
        "IP",
        "{10.141.151.52 ssttlb02} {10.141.151.51 ssttlb01} {10.141.242.38 vm2} {10.141.144.120 LB} {10.141.15.250 xcschtest20}"));
    System.out.println(ini.getIps());
    Assert.assertEquals(10, ini.getIps().size());

  }

  @Test
  public void testMap() {
    Map<String, String> map = new HashMap<String, String>();
    Map<String, String> linkedMap = new LinkedHashMap<String, String>();

    String src = "10.141.144.196=LB;10.171.145.213=idx;10.141.148.191=search1;10.141.148.192=search2;10.141.148.193=search3;10.141.148.194=search4;10.141.242.31=test";
    String[] split1 = src.split(";");
    for (String item : split1) {
      String[] split2 = item.split("=");
      map.put(split2[0], split2[1]);
      linkedMap.put(split2[0], split2[1]);
    }

    for (Entry<String, String> entry : map.entrySet()) {
      System.out.println("key : " + entry.getKey() + " value : " + entry.getValue());
    }

    for (Entry<String, String> entry : linkedMap.entrySet()) {
      System.out.println("key : " + entry.getKey() + " value : " + entry.getValue());
    }
  }

}
