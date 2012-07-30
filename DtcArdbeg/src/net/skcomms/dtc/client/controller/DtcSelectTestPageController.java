package net.skcomms.dtc.client.controller;

import java.util.List;

import com.google.gwt.core.client.GWT;

import net.skcomms.dtc.client.DtcSelectTestPageViewObserver;
import net.skcomms.dtc.client.DtcTestPageViewObserver;
import net.skcomms.dtc.client.model.DtcVisitedPage;
import net.skcomms.dtc.client.model.DtcVisitedPageDao;
import net.skcomms.dtc.client.view.DtcSelectTestPageView;
import net.skcomms.dtc.client.view.DtcTestPageView;

public class DtcSelectTestPageController implements DtcTestPageViewObserver, DtcSelectTestPageViewObserver {
  
  private DtcVisitedPageDao visitedPageDao;
  
  private DtcSelectTestPageView selectTestPageView;
  
  public void init(DtcTestPageView testPageView, DtcSelectTestPageView selectTestPageView) {
    testPageView.addObserver(this);
    selectTestPageView.addObserver(this);
    this.selectTestPageView= selectTestPageView;
  }
  
  @Override
  public void onVisitPage() {
     // TODO visitedPageDao에 방문 기록을 추가 
    GWT.log("OnVisitPage");
  }

  @Override
  public void onReadyRequestData() {
  }

  @Override
  public void onUpdateVisitedTestPages() {
    // TODO SelectTestPage가 로딩되었을 때, VisitedPage 정보를 가져와서 View를 그려준다. path 갑을 넣어야 한다. 
    List<DtcVisitedPage> visitedPageList = visitedPageDao.getVisitedPages(null);
    selectTestPageView.updateVisitTestPageList(visitedPageList);
    
  }
  
  
}
