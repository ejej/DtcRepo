package net.skcomms.dtc.server.atp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import java_cup.runtime.Symbol;
import net.skcomms.dtc.server.DtcAtpFactory;
import net.skcomms.dtc.server.DtcIniFactory;
import net.skcomms.dtc.server.model.DtcAtp;
import net.skcomms.dtc.server.model.DtcIni;
import net.skcomms.dtc.server.util.DtcHelper;
import net.skcomms.dtc.shared.DtcRequest;
import net.skcomms.dtc.shared.DtcRequestParameter;

import org.junit.Assert;
import org.junit.Test;

public class AtpLexTest {

  private static final String LT = Character.toString((char) 0x1E);

  private static final String SP = Character.toString((char) 0x20);

  private static final String FT = Character.toString((char) 0x1F);

  private static final String RS = Character.toString((char) 0x1E);

  private void assertAtp(byte[] bs) throws IOException {
    Socket soc = new Socket("10.141.10.51", 9100);

    OutputStream os_socket = soc.getOutputStream(); // 소켓에 쓰고
    os_socket.write(bs);
    Date start = new Date();
    byte[] bytes = DtcHelper.readAllBytes(soc.getInputStream());
    Date time = new Date();
    System.out.println("Read Time:" + Double.toString((time.getTime() - start.getTime()) / 1000));
    AtpLex lex = new AtpLex(new InputStreamReader(new ByteArrayInputStream(bytes), "utf-8"));

    while (true) {
      Symbol symbol = lex.next_token();
      if (symbol.sym == AtpSym.EOF) {
        break;
      }
    }
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
  public void testAtp() throws IOException {

    String[] messages = {
        "ATP/1.2 KCBBSD 0" + AtpLexTest.LT + AtpLexTest.LT +
            AtpLexTest.LT + "0"
            + AtpLexTest.LT,
        "ATP/1.2 KCBBSD 100" + AtpLexTest.LT + AtpLexTest.LT +
            "" + AtpLexTest.FT +
            "" + AtpLexTest.FT +
            "" + AtpLexTest.FT +
            "" + AtpLexTest.FT + AtpLexTest.LT +
            "100" + AtpLexTest.FT + AtpLexTest.LT +
            "blog" + AtpLexTest.FT + AtpLexTest.LT +
            "1" + AtpLexTest.FT + AtpLexTest.LT +
            "3" + AtpLexTest.FT + AtpLexTest.LT +
            "TS" + AtpLexTest.FT + AtpLexTest.LT +
            "PD" + AtpLexTest.FT + AtpLexTest.LT +
            "256" + AtpLexTest.FT + AtpLexTest.LT +
            "TEST" + AtpLexTest.FT + AtpLexTest.LT +
            AtpLexTest.LT +
            "0" + AtpLexTest.LT
    };
    for (String msg : messages) {
      this.assertAtp(msg.getBytes());
    }
  }

  @Test
  public void testAtpRequest() throws IOException {
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
  public void testScanner() throws IOException {
    String response = "ATP/1.2 100 Continue" + AtpLexTest.LT + AtpLexTest.LT + "0"
        + AtpLexTest.LT;
    AtpLex t = new AtpLex(new ByteArrayInputStream(response.getBytes()));

    Assert.assertEquals("ATP", t.next_token().value);
    Assert.assertEquals("/", t.next_token().value);
    Assert.assertEquals("1", t.next_token().value);
    Assert.assertEquals(".", t.next_token().value);
    Assert.assertEquals("2", t.next_token().value);
    Assert.assertEquals(" ", t.next_token().value);
    Assert.assertEquals("100", t.next_token().value);
    Assert.assertEquals(" ", t.next_token().value);
    Assert.assertEquals("Continue", t.next_token().value);
    Assert.assertEquals(AtpLexTest.LT, t.next_token().value);
    Assert.assertEquals(AtpLexTest.LT, t.next_token().value);
    Assert.assertEquals("0", t.next_token().value);
    Assert.assertEquals(AtpLexTest.LT, t.next_token().value);
  }

}
