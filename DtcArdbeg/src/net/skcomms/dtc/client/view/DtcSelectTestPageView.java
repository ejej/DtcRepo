package net.skcomms.dtc.client.view;

import java.util.ArrayList;
import java.util.List;

import net.skcomms.dtc.client.DtcSelectTestPageViewObserver;
import net.skcomms.dtc.client.DtcTestPageControllerObserver;
import net.skcomms.dtc.client.controller.DtcSelectTestPageController;
import net.skcomms.dtc.client.model.DtcVisitedPage;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tree.Tree;
import com.smartgwt.client.widgets.tree.TreeGrid;
import com.smartgwt.client.widgets.tree.TreeGridField;
import com.smartgwt.client.widgets.tree.TreeNode;

public class DtcSelectTestPageView extends Window {
  
  private final List<DtcSelectTestPageViewObserver> dtcSelectTestPageViewObservers = new ArrayList<DtcSelectTestPageViewObserver>();
  public static class EmployeeTreeNode extends TreeNode {

    public EmployeeTreeNode(String employeeId, String reportsTo, String name) {
      this.setEmployeeId(employeeId);
      this.setReportsTo(reportsTo);
      this.setName(name);
    }

    public void setEmployeeId(String value) {
      this.setAttribute("EmployeeId", value);
    }

    @Override
    public void setName(String name) {
      this.setAttribute("Name", name);
    }

    public void setReportsTo(String value) {
      this.setAttribute("ReportsTo", value);
    }
  }

  private TreeGrid treeGrid;
  private TreeGridField field;
  private final Tree tree = new Tree();

  private final VLayout backgroundLayout;
  private final HLayout serviceLayout; //
  private final VLayout serviceListLayout;

  private VLayout visitedPageListLayout;
  private ListGrid visitedPageListGrid;

  public DtcSelectTestPageView() {
    this.backgroundLayout = new VLayout();
    this.backgroundLayout.setHeight100();
    this.backgroundLayout.setWidth100();
    this.backgroundLayout.setShowEdges(true);

    this.serviceLayout = new HLayout();
    this.serviceLayout.setStyleName("serviceLayout");
    this.serviceLayout.setWidth100();
    this.serviceLayout.setHeight(470);
    this.serviceLayout.setShowEdges(true);

    this.serviceListLayout = new VLayout();
    this.serviceListLayout.setStyleName("serviceListLayout");
    this.serviceListLayout.setHeight100();
    this.serviceListLayout.setWidth(400);
    this.serviceListLayout.setShowEdges(true);

    this.initVisitedPageListLayout();

    this.serviceLayout.addMember(this.serviceListLayout);
    this.serviceLayout.addMember(this.visitedPageListLayout);

    this.backgroundLayout.addMember(this.serviceLayout);
    this.addItem(this.backgroundLayout);

    // this.treeGrid = new TreeGrid();
    // this.treeGrid.setWidth(300);
    // this.treeGrid.setHeight(400);
    //
    // this.field = new TreeGridField("Name", "Tree from local data");
    // this.field.setCanSort(false);
    //
    // this.treeGrid.setFields(this.field);
    //
    // this.tree.setModelType(TreeModelType.PARENT);
    // this.tree.setNameProperty("Name");
    // this.tree.setIdField("EmployeeId");
    // this.tree.setParentIdField("ReportsTo");
    // this.tree.setShowRoot(true);
    //
    // EmployeeTreeNode root = new EmployeeTreeNode("4", "1",
    // "Charles Madigen");
    // EmployeeTreeNode node2 = new EmployeeTreeNode("188", "4",
    // "Rogine Leger");
    // EmployeeTreeNode node3 = new EmployeeTreeNode("189", "4", "Gene Porter");
    // EmployeeTreeNode node4 = new EmployeeTreeNode("265", "189",
    // "Olivier Doucet");
    // EmployeeTreeNode node5 = new EmployeeTreeNode("264", "189",
    // "Cheryl Pearson");
    // this.tree.setData(new TreeNode[] { root, node2, node3, node4, node5 });
    //
    // this.treeGrid.addDrawHandler(new DrawHandler() {
    // @Override
    // public void onDraw(DrawEvent event) {
    // DtcSelectTestPageView.this.tree.openAll();
    // }
    // });
    //
    // this.treeGrid.setData(this.tree);
    // this.addItem(this.treeGrid);
    // this.treeGrid.draw();
  }

  private void initVisitedPageListGrid() {
    this.visitedPageListGrid = new ListGrid();
    this.visitedPageListGrid.setWidth(365);
    this.visitedPageListGrid.setHeight100();
    this.visitedPageListGrid.setTop(50);
    this.visitedPageListGrid.setShowAllRecords(true);
    this.visitedPageListGrid.setShowEmptyMessage(true);
    this.visitedPageListGrid.setEmptyMessage("<br>Click the <b>Set data</b> button to populate this grid.");

    ListGridField nameField = new ListGridField("countryName", "Country", 120);
    ListGridField capitalField = new ListGridField("capital", "Capital");
    this.visitedPageListGrid.setFields(nameField, capitalField);
  }

  private void initVisitedPageListLayout() {
    this.initVisitedPageListGrid();
    this.updateVisitedPageList();

    this.visitedPageListLayout = new VLayout();
    this.visitedPageListLayout.setStyleName("visitedPageLayout");
    this.visitedPageListLayout.setHeight100();
    this.visitedPageListLayout.setWidth(365);
    this.visitedPageListLayout.setShowEdges(true);

    this.visitedPageListLayout.addMember(this.visitedPageListGrid);
  }
  
  private void updateVisitedPageList() {
    // TODO Record 불러와서 visitedPageListGrid에 추가해야 함. 
    GWT.log("update Visited Page List");
    for(DtcSelectTestPageViewObserver observer : dtcSelectTestPageViewObservers) {
      observer.onUpdateVisitedTestPages();
    }
  }

  public void addObserver(DtcSelectTestPageViewObserver dtcSelectTestPageViewObserver) {
    dtcSelectTestPageViewObservers.add(dtcSelectTestPageViewObserver);
  }

  public void updateVisitTestPageList(List<DtcVisitedPage> visitedPageList) {
    // TODO visitedPageListGrid에 visitedPageList값을 넣어준다.     
  }
}
