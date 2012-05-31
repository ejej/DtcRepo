package net.skcomms.dtc.client.view;

import java.util.ArrayList;
import java.util.List;

import net.skcomms.dtc.client.DtcTestPageViewObserver;
import net.skcomms.dtc.shared.DtcRequestInfoModel;
import net.skcomms.dtc.shared.DtcRequestParameterModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.ui.RootPanel;
import com.smartgwt.client.types.ListGridEditEvent;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.HTMLPane;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.fields.ComboBoxItem;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridEditorContext;
import com.smartgwt.client.widgets.grid.ListGridEditorCustomizer;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.EditCompleteEvent;
import com.smartgwt.client.widgets.grid.events.EditCompleteHandler;
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
  private VLayout vLayoutLeftBottom;

  private HLayout hLayoutRight;

  private HTMLPane htmlPane;

  private DtcRequestInfoModel requestInfo;

  private DtcTestPageViewObserver readyRequestDataCb;

  private void createGridRecord() {

    List<DtcRequestParameterModel> params = this.requestInfo.getParams();
    List<RequestGridRecord> records = new ArrayList<RequestGridRecord>();

    int index = 0;
    for (DtcRequestParameterModel param : params) {
      records
          .add(new RequestGridRecord(index++, param.getKey(), param.getName(), param.getValue()));
    }

    this.requestFormGrid.setData(records
        .toArray(new RequestGridRecord[0]));

    // add IP combo box
    RequestGridRecord ipRrecord = new RequestGridRecord(params.size(), "IP", "ip_select",
        this.requestInfo.getIpInfo().getIpText());

    this.requestFormGrid.addData(ipRrecord);
    this.requestFormGrid.setEditorCustomizer(new ListGridEditorCustomizer() {

      @Override
      public FormItem getEditor(ListGridEditorContext context) {

        ListGridField field = context.getEditField();

        if (field.getName().equals("value")) {

          RequestGridRecord record = (RequestGridRecord) context.getEditedRecord();
          if (record.getAttributeAsString("key").equals("IP")) {

            ComboBoxItem cbItem = new ComboBoxItem();
            cbItem.setType("comboBox");
            String[] ipList = new String[DtcTestPageView.this.requestInfo.getIpInfo()
                .getOptions().size()];

            for (int i = 0; i < ipList.length; i++) {

              cbItem.setAttribute("key", DtcTestPageView.this.requestInfo.getIpInfo()
                  .getOptions().get(i).getKey());

              cbItem.setAttribute("name", DtcTestPageView.this.requestInfo.getIpInfo()
                  .getOptions().get(i).getName());

              ipList[i] = DtcTestPageView.this.requestInfo.getIpInfo().getOptions()
                  .get(i).getValue();

            }

            cbItem.setValueMap(ipList);
            return cbItem;

          } else {

            TextItem textItem = new TextItem();
            return textItem;
          }
        }
        return context.getDefaultProperties();
      }
    });
  }

  public String createRequestData() {
    StringBuffer requestData = new StringBuffer();
    for (ListGridRecord record : this.requestFormGrid.getRecords()) {
      requestData.append("&");

      String request = "";
      if (record.getAttribute("name").toLowerCase().equals("ip_select")) {
        RegExp regExp = RegExp.compile("[0-9]+.[0-9]+.[0-9]+.[0-9]+");
        MatchResult match = regExp.exec(record.getAttribute("value"));
        request = record.getAttribute("name") + "=" + match.getGroup(0);
      } else {
        String value = URL.encode(record.getAttribute("value"));
        request = record.getAttribute("name") + "=" + value;
      }
      requestData.append(request);
    }
    GWT.log("Request: " + requestData.toString());
    return requestData.toString();
  }

  public void draw() {
    if (this.layout != null) {
      RootPanel.get("dtcContainer").remove(this.layout);
    }

    this.layout = new HLayout();
    RootPanel.get("dtcContainer").add(this.layout);

    this.layout.setWidth100();
    this.layout.setHeight(800);
    this.layout.setMembersMargin(20);

    this.vLayoutLeft = new VLayout();
    this.vLayoutLeft.setShowEdges(true);
    this.vLayoutLeft.setWidth(300);
    this.vLayoutLeft.setMembersMargin(10);
    this.vLayoutLeft.setLayoutMargin(10);

    this.requestFormGrid = new ListGrid();
    this.requestFormGrid.setWidth(300);
    this.requestFormGrid.setShowAllRecords(true);

    this.requestFormGrid.setCanEdit(true);
    this.requestFormGrid.setEditEvent(ListGridEditEvent.CLICK);
    this.requestFormGrid.setEditByCell(true);
    this.requestFormGrid.addEditCompleteHandler(new EditCompleteHandler() {
      @Override
      public void onEditComplete(EditCompleteEvent event) {
        if (DtcTestPageView.this.validateRequestData() == 0) {
          DtcTestPageView.this.createRequestData();
          DtcTestPageView.this.readyRequestDataCb.onReadyRequestData();
        }
      }
    });
    this.requestFormGrid.setHeight(1);
    this.requestFormGrid.setShowAllRecords(true);
    this.requestFormGrid.setBodyOverflow(Overflow.VISIBLE);
    this.requestFormGrid.setOverflow(Overflow.VISIBLE);
    this.requestFormGrid.setLeaveScrollbarGap(false);
    this.requestFormGrid.setCanAutoFitFields(false);
    this.requestFormGrid.setCanCollapseGroup(false);

    this.createGridRecord();

    this.nameField = new ListGridField("key", "Name", 120);
    this.nameField.setCanEdit(false);
    this.nameField.setCanFilter(false);
    this.nameField.setCanSort(false);
    this.nameField.setCanReorder(false);
    this.nameField.setCanGroupBy(false);

    this.valueField = new ListGridField("value", "Value", 180);
    this.valueField.setCanFilter(false);
    this.valueField.setCanSort(false);
    this.valueField.setCanReorder(false);
    this.valueField.setCanGroupBy(false);

    this.requestFormGrid.setFields(this.nameField, this.valueField);

    this.searchButton = new Button("Search");
    this.searchButton.setWidth(120);
    this.searchButton.setLeft(60);
    this.searchButton.setTop(45);

    this.searchButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (DtcTestPageView.this.validateRequestData() == 0) {
          DtcTestPageView.this.createRequestData();
          DtcTestPageView.this.readyRequestDataCb.onReadyRequestData();
        }
      }
    });

    this.vLayoutLeftBottom = new VLayout();
    this.vLayoutLeftBottom.setShowEdges(true);
    this.vLayoutLeftBottom.setWidth(300);
    this.vLayoutLeftBottom.setHeight(250);

    this.vLayoutLeftBottom.setMembersMargin(5);
    this.vLayoutLeftBottom.setLayoutMargin(0);

    this.vLayoutLeft.addMember(this.requestFormGrid);
    this.vLayoutLeft.addMember(this.searchButton);
    this.vLayoutLeft.addMember(this.vLayoutLeftBottom);

    this.hLayoutRight = new HLayout();
    this.hLayoutRight.setShowEdges(true);

    this.hLayoutRight.setMembersMargin(5);
    this.hLayoutRight.setLayoutMargin(10);

    this.htmlPane = new HTMLPane();
    this.htmlPane.setTop(40);
    this.htmlPane.setWidth100();
    this.htmlPane.setStyleName("response_panel");
    // this.htmlPane.setContentsURL(URL.encode(this.module.getDtcProxyUrl()
    // + "response.html"));

    this.hLayoutRight.addMember(this.htmlPane);

    this.layout.addMember(this.vLayoutLeft);
    this.layout.addMember(this.hLayoutRight);
    this.layout.setLayoutMargin(10);
    this.layout.redraw();
    this.layout.setVisible(true);
  }

  public void DtcTestPageView() {

  }

  public void setHTMLData(String convertedHTML) {

    this.htmlPane.setContents(convertedHTML);

  }

  public void setOnReadyRequestDataObserver(DtcTestPageViewObserver cb) {
    this.readyRequestDataCb = cb;

  }

  public void setRequestInfo(DtcRequestInfoModel requestInfo) {
    this.requestInfo = requestInfo;
  }

  public int validateRequestData() {

    for (ListGridRecord record : this.requestFormGrid.getRecords()) {

      // GWT.log("Key: " + record.getAttribute("key"));
      if (record.getAttribute("key").toLowerCase().equals("query")) {

        // GWT.log("Value: " + record.getAttribute("value"));

        if (record.getAttribute("value") == null) {
          this.invalidRecordIdx = this.requestFormGrid.getRecordIndex(record);
          SC.warn("Invalid Query", new BooleanCallback() {

            @Override
            public void execute(Boolean value) {
              int recordIdx = DtcTestPageView.this.invalidRecordIdx;
              DtcTestPageView.this.requestFormGrid.selectRecord(recordIdx);
            }

          });
          return -1;
        }
      } else if (record.getAttribute("name").toLowerCase().equals("ip_select")) {
        RegExp regExp = RegExp.compile("[0-9]+.[0-9]+.[0-9]+.[0-9]+");
        MatchResult match = regExp.exec(record.getAttribute("value"));
        this.invalidRecordIdx = this.requestFormGrid.getRecordIndex(record);

        if (match.getGroup(0) == null) {
          SC.warn("Invalid IP value", new BooleanCallback() {

            @Override
            public void execute(Boolean value) {
              int recordIdx = DtcTestPageView.this.invalidRecordIdx;
              DtcTestPageView.this.requestFormGrid.selectRecord(recordIdx);
            }

          });
          return -1;
        }

      }
    }
    return 0;
  }
}