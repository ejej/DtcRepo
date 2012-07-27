package net.skcomms.dtc.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import net.skcomms.dtc.server.model.DtcAtp;
import net.skcomms.dtc.server.model.DtcAtpRecord;
import net.skcomms.dtc.server.util.DtcRequestHttpAdapter;
import net.skcomms.dtc.shared.DtcRequest;

public class CndQueryProvider {

  private static final byte FEILD_DELIMETER = 0x10;

  public static String getCndQuery(String nativeQuery) throws FileNotFoundException, IOException,
      UnknownHostException {
    DtcRequest cndRequest = DtcRequestHttpAdapter.createCndRequest(nativeQuery);

    DtcAtp atpRequest = DtcAtpFactory.createRequest(cndRequest);
    String ip = cndRequest.getRequestParameter("IP");
    String port = cndRequest.getRequestParameter("Port");

    // FIXME AtpRequestHandler 클래스의 통신 로직을 재사용할 수 있게 리팩토링하자.
    DtcAtp cndResponse = CndQueryProvider.sendAndReceiveAtp(atpRequest, ip, port);
    return CndQueryProvider.makeFormattedCndQuery(cndResponse);
  }

  private static String makeFormattedCndQuery(DtcAtp cndResponse) {
    StringBuilder cndQuery = new StringBuilder();

    for (DtcAtpRecord record : cndResponse.getRecords().subList(10, 16)) {
      cndQuery.append(record.getFields().get(0));
      cndQuery.append((char) CndQueryProvider.FEILD_DELIMETER);
    }
    if (cndQuery.length() > 0) {
      cndQuery.deleteCharAt(cndQuery.length() - 1);
    }

    return cndQuery.toString();
  }

  private static DtcAtp sendAndReceiveAtp(DtcAtp atpRequest, String ip, String port)
      throws UnknownHostException, IOException {
    DtcAtp cndResponse;
    Socket socket = new Socket(ip, Integer.parseInt(port));
    try {
      socket.getOutputStream().write(atpRequest.getBytes(atpRequest.getCharset()));
      socket.getOutputStream().flush();

      cndResponse = DtcAtpFactory.createResponse(socket.getInputStream(), atpRequest.getCharset());
    } finally {
      socket.close();
    }
    return cndResponse;
  }

}
