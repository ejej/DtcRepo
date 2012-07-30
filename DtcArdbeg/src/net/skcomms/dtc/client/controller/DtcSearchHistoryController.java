package net.skcomms.dtc.client.controller;

import java.util.List;

import net.skcomms.dtc.client.DtcTestPageModelObserver;
import net.skcomms.dtc.client.DtcTestPageViewObserver;
import net.skcomms.dtc.client.model.DtcResponse;
import net.skcomms.dtc.client.model.DtcSearchHistory;
import net.skcomms.dtc.client.model.DtcSearchHistoryDao;
import net.skcomms.dtc.client.model.DtcTestPageModel;
import net.skcomms.dtc.client.view.DtcTestPageView;
import net.skcomms.dtc.shared.DtcRequestParameter;

import com.google.gwt.core.client.GWT;

public class DtcSearchHistoryController implements DtcTestPageModelObserver, DtcTestPageViewObserver {

  private DtcSearchHistoryDao searchHistoryDao;
  private DtcTestPageView testPageView;
  private String path;

  public void initialize(DtcSearchHistoryDao searchHistoryDao, DtcTestPageModel testPageModel, DtcTestPageView testPageView) {
    this.searchHistoryDao = searchHistoryDao;
    testPageModel.addObserver(this);
    this.testPageView = testPageView;
    this.testPageView.addObserver(this);
  }

  @Override
  public void onRequestFailed(Throwable caught) {
  }

  @Override
  public void onTestPageResponseReceived(DtcResponse response) {
    this.persist(response);    
    this.updateSearchHistoryView(response.getRequest().getPath());
  }

  private void persist(DtcResponse response) {
    GWT.log("DtcSearchHistoryController.persist() called");
    String path = response.getRequest().getPath();
    List<DtcRequestParameter> params = response.getRequest().getRequestParameters();
    DtcSearchHistory searchHistory = DtcSearchHistory.create(path, params,
        response.getResponseTime());
    this.searchHistoryDao.persist(searchHistory);
  }

  public void updateSearchHistoryView(String path) {
    List<DtcSearchHistory> searchHistories = this.searchHistoryDao.getSearchHistroies(path);
    this.testPageView.updateSearchHistory(searchHistories);

    for (DtcSearchHistory history : searchHistories) {
      System.out.println(history.getFormattedString("Query", "IP"));
    }
  }
  @Override
  	public void onReadyRequestData() {
  }

  @Override
  public void onDtcTestPageViewAppeared(String path) {
    this.path = path;
    this.updateSearchHistoryView(path);
  }

  @Override
  public void onHistoryGridRecordClicked(int recordNum) {
    List<DtcSearchHistory> searchHistories = this.searchHistoryDao.getSearchHistroies(this.path);
    this.testPageView.updateRequest(searchHistories.get(recordNum));
  }
}
