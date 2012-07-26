package net.skcomms.dtc.client.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.skcomms.dtc.client.PersistenceManager;

public class DtcVisitedPageDao {
  public static final String PREFIX = "DtcVisitedPageDao.";
  private static final int MAX_PAGELIST_SIZE = 5;

  public static String combineKey(DtcVisitedPage visitedPage) {
    return DtcSearchHistoryDao.combineKeyPrefix(visitedPage.getPath())
        + visitedPage.getAccessTime().getTime();
  }

  public static String combineKeyPrefix(String path) {
    return DtcSearchHistoryDao.PREFIX + path + ".";
  }

  private List<DtcVisitedPage> getAllVisitedPages(String path) {
    List<DtcVisitedPage> visitedPages = new ArrayList<DtcVisitedPage>();
    for (String key : PersistenceManager.getInstance().getItemKeys()) {
      if (key.startsWith(DtcSearchHistoryDao.combineKeyPrefix(path))) {
        DtcVisitedPage visitedPage = DtcVisitedPage.deserialize(PersistenceManager.getInstance()
            .getItem(key));
        visitedPages.add(visitedPage);
      }
    }
    return visitedPages;
  }

  public List<DtcVisitedPage> getVisitedPages(String path) {
    List<DtcVisitedPage> visitedPages = this.getAllVisitedPages(path);
    this.sortVisitedPagesOrderByTimeDesc(visitedPages);
    this.removeOldVisitedPages(visitedPages);
    return visitedPages;
  }

  public void persist(DtcVisitedPage visitedPages) {
    PersistenceManager.getInstance().setItem(
        DtcVisitedPageDao.combineKey(visitedPages), visitedPages.serialize());
  }

  private void removeOldVisitedPages(List<DtcVisitedPage> visitedPages) {
    for (int i = DtcVisitedPageDao.MAX_PAGELIST_SIZE; i < visitedPages.size(); i++) {
      PersistenceManager.getInstance().removeItem(
          DtcVisitedPageDao.combineKey(visitedPages.get(i)));
    }

    if (visitedPages.size() > DtcVisitedPageDao.MAX_PAGELIST_SIZE) {
      visitedPages.subList(DtcVisitedPageDao.MAX_PAGELIST_SIZE, visitedPages.size())
      .clear();
    }
  }

  private void sortVisitedPagesOrderByTimeDesc(List<DtcVisitedPage> visitedPages) {
    Collections.sort(visitedPages, new Comparator<DtcVisitedPage>() {

      @Override
      public int compare(DtcVisitedPage o1, DtcVisitedPage o2) {
        return (int) (-(o1.getAccessTime().getTime() - o2.getAccessTime().getTime()));
      }
    });
  }
}
