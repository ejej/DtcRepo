package net.skcomms.dtc.client.view;

import java.util.ArrayList;
import java.util.List;

import net.skcomms.dtc.client.DtcTestPageViewObserver;
import net.skcomms.dtc.shared.DtcRequestMeta;
import net.skcomms.dtc.shared.DtcRequestParameter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.RootPanel;
import com.smartgwt.client.types.ListGridEditEvent;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.HTMLPane;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.CloseClickEvent;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.form.fields.ComboBoxItem;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridEditorContext;
import com.smartgwt.client.widgets.grid.ListGridEditorCustomizer;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

public class DtcTestPageView {

  static class RequestGridRecord extends ListGridRecord {

    public RequestGridRecord() {
    }

    public RequestGridRecord(int id, String key, String name, ComboBoxItem value) {

      this.setId(id);
      this.setKey(key);
      this.setName(name);
      this.setValue(value);
    }

    public RequestGridRecord(int id, String key, String name, String value) {

      this.setId(id);
      this.setKey(key);
      this.setName(name);
      this.setValue(value);
    }

    public int getId() {
      return this.getAttributeAsInt("ID");
    }

    public String getName() {
      return this.getAttributeAsString("name");

    }

    private void setId(int id) {
      this.setAttribute("ID", id);
    }

    private void setKey(String key) {
      this.setAttribute("key", key);
    }

    private void setName(String name) {
      this.setAttribute("name", name);
    }

    private void setValue(ComboBoxItem value) {
      this.setAttribute("value", value);
    }

    private void setValue(String value) {
      this.setAttribute("value", value);
    }

  }

  private int invalidRecordIdx;

  private HLayout layout;

  private VLayout vLayoutLeft;

  private ListGrid requestFormGrid;

  private ListGridField nameField;

  private ListGridField valueField;

  private Button searchButton;

  private Button modalButton;

  private VLayout vLayoutLeftBottom;

  private HLayout hLayoutRight;

  private HTMLPane htmlPane;

  private DtcRequestMeta requestInfo;

  private DtcChronoView chronoView;

  private DtcSelectTestPageView dtcSelectTestPageView;

  private static final RegExp IP_PATTERN = RegExp.compile("[0-9]+.[0-9]+.[0-9]+.[0-9]+");

  private final List<DtcTestPageViewObserver> dtcTestPageViewObservers = new ArrayList<DtcTestPageViewObserver>();

  public void addObserver(DtcTestPageViewObserver observer) {
    dtcTestPageViewObservers.add(observer);
  }

  public void chronoStart() {
    chronoView.start();
  }

  public void chronoStop() {
    chronoView.end();
  }

  private FormItem createComboBoxControl() {
    ComboBoxItem cbItem = new ComboBoxItem();

    cbItem.setType("comboBox");
    String[] ipList = new String[DtcTestPageView.this.requestInfo.getIpInfo()
                                 .getOptions().size()];

    for (int i = 0; i < ipList.length; i++) {
      cbItem.setAttribute("key",
          DtcTestPageView.this.requestInfo.getIpInfo().getOptions().get(i).getKey());
      cbItem.setAttribute("name", DtcTestPageView.this.requestInfo.getIpInfo().getOptions()
          .get(i).getName());
      ipList[i] = DtcTestPageView.this.requestInfo.getIpInfo().getOptions().get(i).getValue();

      GWT.log("ip : " + ipList[i]);
    }

    cbItem.setValueMap(ipList);
    return cbItem;
  }

  private void createGridRecord() {
    List<DtcRequestParameter> params = requestInfo.getParams();
    List<RequestGridRecord> records = new ArrayList<RequestGridRecord>();

    int index = 0;
    for (DtcRequestParameter param : params) {
      records
      .add(new RequestGridRecord(index++, param.getKey(), param.getName(), param.getValue()));
    }

    requestFormGrid.setData(records.toArray(new RequestGridRecord[0]));

    // add IP combo box
    RequestGridRecord ipRecord = new RequestGridRecord(params.size(), "IP", "ip_select",
        this.getInitIpText());

    requestFormGrid.addData(ipRecord);
    requestFormGrid.setEditorCustomizer(this.createListGridEditorCustomizer());
  }

  private ListGridEditorCustomizer createListGridEditorCustomizer() {
    return new ListGridEditorCustomizer() {

      @Override
      public FormItem getEditor(ListGridEditorContext context) {
        ListGridField field = context.getEditField();

        if (field.getName().equals("value")) {
          RequestGridRecord record = (RequestGridRecord) context.getEditedRecord();
          if (record.getAttributeAsString("key").equals("IP")) {
            return DtcTestPageView.this.createComboBoxControl();
          } else {
            return new TextItem();
          }
        }
        return context.getDefaultProperties();
      }
    };
  }

  public String createRequestData() {
    StringBuffer requestData = new StringBuffer();
    for (ListGridRecord record : requestFormGrid.getRecords()) {
      requestData.append("&");

      String request = "";
      GWT.log(record.getAttribute("value"));
      if (record.getAttribute("name").toLowerCase().equals("ip_select")) {
        MatchResult match = DtcTestPageView.IP_PATTERN.exec(record.getAttribute("value"));
        request = record.getAttribute("name") + "=" + match.getGroup(0);
      } else {
        String value = record.getAttribute("value");
        String encodedValue = (value == null) ? "" : URL.encode(value);
        request = record.getAttribute("name") + "=" + encodedValue;
      }
      requestData.append(request);
    }
    GWT.log("Request: " + requestData.toString());
    return requestData.toString();
  }

  public void draw() {
    this.setupVLayoutLeft();
    this.setupHLayoutRight();
    this.setupContentsLayout();

    layout.redraw();
    layout.setVisible(true);
  }

  private void fireReadyToRequest() {
    for (DtcTestPageViewObserver observer : dtcTestPageViewObservers) {
      observer.onReadyRequestData();
    }
  }

  private String getInitIpText() {

    if(requestInfo.getIpInfo().getOptions().size() > 0) {
      return requestInfo.getIpInfo().getOptions().get(0).getValue();
    }

    return requestInfo.getIpInfo().getIpText();
  }

  public List<DtcRequestParameter> getRequestParameters() {
    List<DtcRequestParameter> params = new ArrayList<DtcRequestParameter>();
    for (ListGridRecord record : requestFormGrid.getRecords()) {
      DtcRequestParameter param = new DtcRequestParameter();
      param.setKey(record.getAttribute("key"));
      param.setValue(this.getSafeRecordValue(record));
      params.add(param);
    }
    return params;
  }

  private String getSafeRecordValue(ListGridRecord record) {
    String value = null;
    if (record.getAttribute("name").toLowerCase().equals("ip_select")) {
      MatchResult match = DtcTestPageView.IP_PATTERN.exec(record.getAttribute("value"));
      if (match != null) {
        value = match.getGroup(0);
      }
    } else {
      value = record.getAttribute("value");
    }
    return value == null ? "" : value;
  }

  public void setHTMLData(String convertedHTML) {
    htmlPane.setContents(convertedHTML);
  }

  public void setRequestMeta(DtcRequestMeta requestInfo) {
    this.requestInfo = requestInfo;
  }

  private void setupChronoView() {
    chronoView = new DtcChronoView();
    chronoView.setWidth(300);
    chronoView.setHeight(10);
  }

  private void setupContentsLayout() {
    if (layout != null) {
      RootPanel.get("dtcContainer").remove(layout);
    }

    layout = new HLayout();
    RootPanel.get("dtcContainer").add(layout);

    layout.setWidth100();
    layout.setHeight(800);
    layout.setMembersMargin(20);
    layout.addMember(vLayoutLeft);
    layout.addMember(hLayoutRight);
    layout.setLayoutMargin(10);
  }

  private void setupHLayoutRight() {
    hLayoutRight = new HLayout();
    hLayoutRight.setShowEdges(true);

    hLayoutRight.setMembersMargin(5);
    hLayoutRight.setLayoutMargin(10);

    htmlPane = new HTMLPane();
    htmlPane.setTop(40);
    htmlPane.setWidth100();
    htmlPane.setStyleName("response_panel");

    hLayoutRight.addMember(htmlPane);
    hLayoutRight.addStyleName("response-area");
  }

  private void setupModalButton() {
    modalButton = new Button("Modal");
    modalButton.setWidth(120);
    modalButton.setLeft(60);
    modalButton.setTop(45);
    modalButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {

        dtcSelectTestPageView = new DtcSelectTestPageView();
        dtcSelectTestPageView.setWidth(800);
        dtcSelectTestPageView.setHeight(600);
        dtcSelectTestPageView.setTitle("Select Test Page Window");
        dtcSelectTestPageView.setShowMinimizeButton(false);
        dtcSelectTestPageView.setIsModal(true);
        dtcSelectTestPageView.setShowModalMask(true);
        dtcSelectTestPageView.centerInPage();
        dtcSelectTestPageView.setDismissOnOutsideClick(true);
        dtcSelectTestPageView.addCloseClickHandler(new CloseClickHandler() {

          @Override
          public void onCloseClick(CloseClickEvent event) {

            dtcSelectTestPageView.destroy();
          }
        });

        dtcSelectTestPageView.show();
      }
    });
  }

  private void setupNameField() {
    nameField = new ListGridField("key", "Name", 120);
    nameField.setCanEdit(false);
    nameField.setCanFilter(false);
    nameField.setCanSort(false);
    nameField.setCanReorder(false);
    nameField.setCanGroupBy(false);
  }

  private void setupPreviewHandler() {
    Event.addNativePreviewHandler(new Event.NativePreviewHandler() {

      @Override
      public void onPreviewNativeEvent(NativePreviewEvent event) {
        if (event.isFirstHandler() &&
            event.getTypeInt() == Event.ONKEYUP &&
            event.getNativeEvent().getKeyCode() == 13 &&
            DtcTestPageView.this.validateRequestData() == 0) {
          DtcTestPageView.this.fireReadyToRequest();
        }
      }

    });
  }

  private void setupRequestFormGrid() {
    requestFormGrid = new ListGrid();
    requestFormGrid.setWidth(300);
    requestFormGrid.setShowAllRecords(true);
    requestFormGrid.setCanEdit(true);
    requestFormGrid.setEditEvent(ListGridEditEvent.CLICK);
    requestFormGrid.setEditByCell(true);
    requestFormGrid.setHeight(1);
    requestFormGrid.setShowAllRecords(true);
    requestFormGrid.setBodyOverflow(Overflow.VISIBLE);
    requestFormGrid.setOverflow(Overflow.VISIBLE);
    requestFormGrid.setLeaveScrollbarGap(false);
    requestFormGrid.setCanAutoFitFields(false);
    requestFormGrid.setCanCollapseGroup(false);

    this.createGridRecord();
    this.setupNameField();
    this.setupValueField();
    requestFormGrid.setFields(nameField, valueField);

    this.setupPreviewHandler();
  }

  private void setupSearchButton() {
    searchButton = new Button("Search");
    searchButton.setWidth(120);
    searchButton.setLeft(60);
    searchButton.setTop(45);
    searchButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        if (DtcTestPageView.this.validateRequestData() == 0) {
          DtcTestPageView.this.fireReadyToRequest();
        }
      }
    });
  }

  private void setupValueField() {
    valueField = new ListGridField("value", "Value", 180);
    valueField.setCanFilter(false);
    valueField.setCanSort(false);
    valueField.setCanReorder(false);
    valueField.setCanGroupBy(false);
  }

  private void setupVLayoutBottom() {
    vLayoutLeftBottom = new VLayout();
    vLayoutLeftBottom.setShowEdges(true);
    vLayoutLeftBottom.setWidth(300);
    vLayoutLeftBottom.setHeight100();

    vLayoutLeftBottom.setMembersMargin(5);
    vLayoutLeftBottom.setLayoutMargin(0);
  }

  private void setupVLayoutLeft() {
    this.setupChronoView();
    this.setupRequestFormGrid();
    this.setupSearchButton();
    this.setupModalButton();
    this.setupVLayoutBottom();

    vLayoutLeft = new VLayout();
    vLayoutLeft.setShowEdges(true);
    vLayoutLeft.setWidth(300);
    vLayoutLeft.setMembersMargin(10);
    vLayoutLeft.setLayoutMargin(10);

    this.wireVLayoutLeft();

  }

  public int validateRequestData() {
    for (ListGridRecord record : requestFormGrid.getRecords()) {
      if (record.getAttribute("key").toLowerCase().equals("query")) {
        if (record.getAttribute("value") == null) {
          invalidRecordIdx = requestFormGrid.getRecordIndex(record);
          SC.warn("Invalid Query", new BooleanCallback() {

            @Override
            public void execute(Boolean value) {
              int recordIdx = invalidRecordIdx;
              requestFormGrid.selectRecord(recordIdx);
            }

          });
          return -1;
        }
      } else if (record.getAttribute("name").toLowerCase().equals("ip_select")) {
        RegExp regExp = RegExp.compile("[0-9]+.[0-9]+.[0-9]+.[0-9]+");
        MatchResult match = regExp.exec(record.getAttribute("value"));
        invalidRecordIdx = requestFormGrid.getRecordIndex(record);

        if (match.getGroup(0) == null) {
          SC.warn("Invalid IP value", new BooleanCallback() {

            @Override
            public void execute(Boolean value) {
              int recordIdx = invalidRecordIdx;
              requestFormGrid.selectRecord(recordIdx);
            }

          });
          return -1;
        }

      }
    }
    return 0;
  }

  private void wireVLayoutLeft() {
    vLayoutLeft.addMember(chronoView);
    vLayoutLeft.addMember(requestFormGrid);
    vLayoutLeft.addMember(searchButton);
    vLayoutLeft.addMember(modalButton);
    vLayoutLeft.addMember(vLayoutLeftBottom);
  }
}
