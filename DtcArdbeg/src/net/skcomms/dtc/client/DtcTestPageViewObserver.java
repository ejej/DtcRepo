package net.skcomms.dtc.client;

public interface DtcTestPageViewObserver {

  void onReadyRequestData();
  
  void onDtcTestPageViewAppeared(String path);

  void onHistoryGridRecordClicked(int recordNum);

}
