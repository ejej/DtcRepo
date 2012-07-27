package net.skcomms.dtc.server.requesthandler;

import net.skcomms.dtc.server.model.DtcAtp;
import net.skcomms.dtc.server.model.DtcIni;
import net.skcomms.dtc.server.util.DtcHelper;

public class DtcAtpResult implements DtcResult {

  private DtcAtp response;

  private DtcIni ini;

  public DtcAtpResult(DtcAtp atpResponse, DtcIni ini) {
    this.response = atpResponse;
    this.ini = ini;
  }

  @Override
  public String getResult() {
    return DtcHelper.getHtmlFromAtp(this.response, this.ini);
  }

}
