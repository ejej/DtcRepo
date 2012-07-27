package net.skcomms.dtc.server.atp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import net.skcomms.dtc.server.DtcAtpFactory;
import net.skcomms.dtc.server.DtcAtpParser;
import net.skcomms.dtc.server.DtcIniFactory;
import net.skcomms.dtc.server.model.DtcAtp;
import net.skcomms.dtc.server.model.DtcIni;
import net.skcomms.dtc.server.util.DtcHelper;
import net.skcomms.dtc.shared.DtcRequest;
import net.skcomms.dtc.shared.DtcRequestParameter;

import org.junit.Assert;
import org.junit.Test;

public class AtpCupTest {

  private static final String LT = Character.toString((char) 0x1E);

  private static final String FT = Character.toString((char) 0x1F);

  // private static final String LT = Character.toString((char) 0x0A);
  //
  // private static final String FT = Character.toString((char) 0x09);

  private static final String SP = Character.toString((char) 0x20);

  private static final String RS = Character.toString((char) 0x1E);

  private void assertAtp(byte[] bs) throws Exception {
    // 10.141.10.51 | 9100 - 싸이블로그
    Socket soc = new Socket("10.141.10.51", 9100);

    OutputStream os_socket = soc.getOutputStream(); // 소켓에 쓰고
    os_socket.write(bs);
    os_socket.flush();

    byte[] bytes = DtcHelper.readAllBytes(soc.getInputStream());
    AtpLex lex = new AtpLex(new InputStreamReader(new ByteArrayInputStream(bytes), "utf-8"));
    DtcAtp atp = (DtcAtp) new AtpCup(lex).parse().value;

    Assert.assertNotNull(atp);
    System.out.println(atp);
  }

  private void printByte(byte[] bytes) {
    for (byte b : bytes) {
      if (b == 0) {
        // break;
      }
      System.out.println("byte:[" + b + "], char:[" + (char) b + "]");
    }
  }

  // 10.141.10.51 | 9100 - 싸이블로그
  @Test
  public void testAtp() throws Exception {

    String[] messages = {
        "ATP/1.2 KCBBSD 0" + AtpCupTest.LT + AtpCupTest.LT +
            AtpCupTest.LT + "0"
            + AtpCupTest.LT,
        "ATP/1.2 KCBBSD 100" + AtpCupTest.LT + AtpCupTest.LT +
            "" + AtpCupTest.FT +
            "" + AtpCupTest.FT +
            "" + AtpCupTest.FT +
            "" + AtpCupTest.FT + AtpCupTest.LT +
            "100" + AtpCupTest.FT + AtpCupTest.LT +
            "blog" + AtpCupTest.FT + AtpCupTest.LT +
            "1" + AtpCupTest.FT + AtpCupTest.LT +
            "10" + AtpCupTest.FT + AtpCupTest.LT +
            "TS" + AtpCupTest.FT + AtpCupTest.LT +
            "PD" + AtpCupTest.FT + AtpCupTest.LT +
            "256" + AtpCupTest.FT + AtpCupTest.LT +
            "TEST" + AtpCupTest.FT + AtpCupTest.LT +
            AtpCupTest.LT +
            "0" + AtpCupTest.LT
    };
    for (String msg : messages) {
      this.assertAtp(msg.getBytes());
    }
  }

  @Test
  public void testAtpRequest() throws Exception {

    List<DtcRequestParameter> requestParameter = new ArrayList<DtcRequestParameter>();
    requestParameter.add(new DtcRequestParameter("Version", null, "100"));
    requestParameter.add(new DtcRequestParameter("Query", null, "블로그"));
    requestParameter.add(new DtcRequestParameter("ResultStartPos", null, "1"));
    requestParameter.add(new DtcRequestParameter("ResultCount", null, "2"));
    requestParameter.add(new DtcRequestParameter("ClientCode", null, "TS"));
    requestParameter.add(new DtcRequestParameter("Sort", null, "PD"));
    requestParameter.add(new DtcRequestParameter("SummaryLen", null, "128"));
    requestParameter.add(new DtcRequestParameter("Referer", null, "TEST"));
    requestParameter.add(new DtcRequestParameter("Port", null, "9200"));
    requestParameter.add(new DtcRequestParameter("IP", null, "10.171.10.241"));

    DtcRequest request = new DtcRequest();
    request.setRequestParameters(requestParameter);

    String filePath = DtcHelper.getRootPath() + "kcbbs/blog.100.ini";
    DtcIni ini = new DtcIniFactory().createFrom(filePath);

    DtcAtp dtcAtp = DtcAtpFactory.createFrom(request, ini);
    this.assertAtp(dtcAtp.getBytes(ini.getCharacterSet()));
  }

  @Test
  public void testParseResponse() throws IOException {
    String response = "ATP/1.2 100 Continue" + AtpCupTest.LT + AtpCupTest.LT + "s-id"
        + AtpCupTest.FT + "s-key"
        + AtpCupTest.FT + AtpCupTest.LT + "s-id2" + AtpCupTest.FT + "s-key2"
        + AtpCupTest.FT + AtpCupTest.LT
        + AtpCupTest.LT + "3" + AtpCupTest.LT + "abc";
    DtcAtp atp = DtcAtpParser.parse(response.getBytes("euc-kr"), "euc-kr");

    Assert.assertNotNull(atp);
    System.out.println(atp);
  }

}
