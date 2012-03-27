package net.skcomms.dtc.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class DtcArdbeg implements EntryPoint {

  private final static ServiceDao serviceDao = new ServiceDao();

  // private final static String DTC_HOME_URL =
  // "http://127.0.0.1:8888/testpage/DtcList.html";
  private final static String DTC_HOME_URL = "http://dtc.skcomms.net/";

  private final Frame frame = new Frame();

  @Override
  public void onModuleLoad() {
    this.initializeDtcFrame();

    this.initializeNavigationBar();
  }

  private void initializeNavigationBar() {
    Anchor navi = new Anchor("Home");

    navi.getElement().addClassName("navi-bar");

    navi.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        DtcArdbeg.this.frame.setUrl(DtcArdbeg.DTC_HOME_URL);
      }
    });
    RootPanel.get("naviBarContainer").add(navi);
  }

  private void initializeDtcFrame() {
    this.frame.setPixelSize(Window.getClientWidth() - 30,
        Window.getClientHeight() - 120);
    this.frame.setUrl(DtcArdbeg.DTC_HOME_URL);
    this.frame.addLoadHandler(new LoadHandler() {
      @Override
      public void onLoad(LoadEvent event) {
        Document doc = IFrameElement.as(
            DtcArdbeg.this.frame.getElement()).getContentDocument();

        if (doc == null) {
          return;
        }

        // TODO 서비스 선택 판정을 링크 클릭으로 변경해야 함.
        if (doc.getReferrer().equals(DtcArdbeg.DTC_HOME_URL)) {
          int index = doc.getURL().indexOf("?b=");
          if (index != -1) {
            String serviceName = doc.getURL().substring(index + 3)
                .replaceAll("/", "");
            DtcArdbeg.serviceDao.addVisitCount(serviceName);
          }
        }

        if (doc.getURL().equals(DtcArdbeg.DTC_HOME_URL)) {

          DtcArdbeg.removeComaparePageAnchor(doc);

          DtcArdbeg.this.sortServices();
        }

      }
    });

    RootPanel.get("dtcContainer").add(this.frame);

    Window.addResizeHandler(new ResizeHandler() {
      @Override
      public void onResize(ResizeEvent event) {
        DtcArdbeg.this.frame.setPixelSize(Window.getClientWidth() - 30,
            Window.getClientHeight() - 200);
      }
    });
  }

  /**
	 * 
	 */
  private void sortServices() {
    Document doc = IFrameElement.as(DtcArdbeg.this.frame.getElement())
        .getContentDocument();
    Element tbody = doc.getElementsByTagName("tbody").getItem(0);
    Element sortedBody = doc.createElement("tbody");

    // 데이터 행 접근방법
    // Window.alert(header.getChild(1).getChild(0).getChild(0).getNodeValue());

    class Pair<K, V> {
      /**
       * @param name
       * @param node
       */
      public Pair(K key, V value) {
        this.key = key;
        this.value = value;
      }

      K key;
      V value;
    }

    List<Pair<Integer, Node>> rows = new ArrayList<Pair<Integer, Node>>();
    for (int i = 1; i < tbody.getChildCount(); i++) {
      Node row = tbody.getChild(i);

      // 브라우저에 따라 white space로 구성된 텍스트 노드가 반환되는 경우가 있음.
      if (row.getChildCount() == 0) {
        continue;
      }

      String name;
      if (row.getChild(0).getNodeType() == Node.ELEMENT_NODE) { // IE8, 7
        name = row.getChild(0).getChild(0).getChild(0).getNodeValue();
      } else {
        if (row.getChild(1).getChild(0).getNodeType() == Node.ELEMENT_NODE) { // dtc.ini
          name = row.getChild(1).getChild(0).getChild(0).getNodeValue();
        }
        else {
          name = row.getChild(1).getChild(1).getChild(0).getNodeValue();
        }
      }

      Integer score = DtcArdbeg.serviceDao.getVisitCount(name);
      if (score == 0) {
        Element.as(row).setAttribute("style", "color:gray;");
      }
      rows.add(new Pair<Integer, Node>(score, row));
    }

    Collections.sort(rows, new Comparator<Pair<Integer, Node>>() {
      @Override
      public int compare(Pair<Integer, Node> arg0,
          Pair<Integer, Node> arg1) {
        return -arg0.key.compareTo(arg1.key);
      }
    });

    if (!rows.isEmpty() && rows.get(0).key > 0) {
      sortedBody.appendChild(tbody.getFirstChild().cloneNode(true));
    }

    int prevScore = 1;
    for (Pair<Integer, Node> pair : rows) {
      if (prevScore != 0 && pair.key.equals(0)) {
        sortedBody.appendChild(tbody.getFirstChild());
      }
      sortedBody.appendChild(pair.value);
      prevScore = pair.key;
    }

    tbody.getParentNode().replaceChild(sortedBody, tbody);
  }

  private static void removeComaparePageAnchor(Document doc) {
    Node anchor = doc.getBody().getChild(0);
    doc.getBody().removeChild(anchor);

    Node br = doc.getBody().getChild(0);
    doc.getBody().removeChild(br);
  }

}
