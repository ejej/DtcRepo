package net.skcomms.dtc.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import net.skcomms.dtc.server.model.DtcAtp;
import net.skcomms.dtc.server.model.DtcAtpRecord;
import net.skcomms.dtc.server.util.DtcHelper;
import net.skcomms.dtc.shared.DtcRequest;
import net.skcomms.dtc.shared.DtcRequestParameter;

public class DtcAtpFactory {

  private static void addArguments(DtcRequest request, DtcAtp atp) {
    List<DtcRequestParameter> params = request.getRequestParameters();
    for (DtcRequestParameter param : params) {
      if (param.getKey().equals("IP") || param.getKey().equals("Port")) {
        continue;
      }

      DtcAtpRecord record = new DtcAtpRecord();
      record.addField(DtcHelper.getOrElse(param.getValue(), ""));
      atp.addRecord(record);
    }
  }

  private static void addDummyRecords(DtcAtp atp) {
    DtcAtpRecord record = new DtcAtpRecord();
    for (int i = 0; i < 4; i++) {
      record.addField("1");
    }
    atp.addRecord(record);
  }

  public static DtcAtp createRequest(DtcRequest request) {
    DtcAtp atp = new DtcAtp();
    atp.setCharset(request.getCharset());
    DtcAtpFactory.setSignature(request, atp);
    DtcAtpFactory.addDummyRecords(atp);
    DtcAtpFactory.addArguments(request, atp);
    atp.setBinary(new byte[0]);
    return atp;
  }

  public static DtcAtp createResponse(InputStream is, String charset) throws IOException {
    return DtcAtpParser.parse(is, charset);
  }

  private static void setSignature(DtcRequest request, DtcAtp atp) {
    String sign = "ATP/1.2 " + request.getAppName() + " "
        + request.getApiNumber();
    atp.setSignature(sign);
  }
}
