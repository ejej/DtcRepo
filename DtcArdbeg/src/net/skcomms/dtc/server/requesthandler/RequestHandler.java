package net.skcomms.dtc.server.requesthandler;

import java.io.IOException;

import net.skcomms.dtc.server.model.DtcIni;
import net.skcomms.dtc.shared.DtcRequest;

public interface RequestHandler {

  DtcResult handle(DtcRequest request, DtcIni ini) throws IOException;

}
