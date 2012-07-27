package net.skcomms.dtc.server.requesthandler;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import net.skcomms.dtc.server.DtcAtpFactory;
import net.skcomms.dtc.server.model.DtcAtp;
import net.skcomms.dtc.server.model.DtcIni;
import net.skcomms.dtc.shared.DtcRequest;

public class AtpRequestHandler implements RequestHandler {

  private static Socket openSocket(DtcRequest request) throws UnknownHostException, IOException {
    String ip = request.getRequestParameter("IP");
    String port = request.getRequestParameter("Port");
    return new Socket(ip, Integer.parseInt(port));
  }

  @Override
  public DtcResult handle(DtcRequest request, DtcIni ini) throws IOException {
    Socket socket = AtpRequestHandler.openSocket(request);
    try {
      DtcAtp atpRequest = DtcAtpFactory.createRequest(request);
      socket.getOutputStream().write(atpRequest.getBytes(request.getCharset()));
      socket.getOutputStream().flush();

      DtcAtp atpResponse = DtcAtpFactory.createResponse(socket.getInputStream(),
          request.getCharset());
      return new DtcAtpResult(atpResponse, ini);
    } finally {
      socket.close();
    }
  }

}
