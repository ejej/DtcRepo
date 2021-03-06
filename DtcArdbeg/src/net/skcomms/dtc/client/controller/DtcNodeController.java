package net.skcomms.dtc.client.controller;

import net.skcomms.dtc.client.DtcNodeObserver;
import net.skcomms.dtc.client.model.DtcNodeModel;
import net.skcomms.dtc.client.view.DtcNodeView;
import net.skcomms.dtc.shared.DtcRequestMeta;

public class DtcNodeController implements DtcNodeObserver {

  private DtcNodeModel dtcNodeModel = null;

  private DtcNodeView dtcNodeView = null;

  private DtcNodeView dtcFavoriteNodeView = null;

  public void initialize(DtcNodeView nodeView, DtcNodeView favoriteNodeView) {
    this.dtcNodeView = nodeView;
    this.dtcFavoriteNodeView = favoriteNodeView;

    this.dtcNodeModel = DtcNodeModel.getInstance();
    this.dtcNodeModel.addObserver(this);
  }

  @Override
  public void onDtcTestPageLoaded(DtcRequestMeta requestInfo) {
  }

  @Override
  public void onFavoriteNodeListChanged() {
    this.dtcFavoriteNodeView.setDtcNodeWidget(this.dtcNodeModel.getFavoriteNodeList());
  }

  @Override
  public void onNodeListChanged() {
    this.dtcNodeView.setDtcNodeWidget(this.dtcNodeModel.getNodeList());
  }
}
