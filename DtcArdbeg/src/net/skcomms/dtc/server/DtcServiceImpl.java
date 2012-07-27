/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package net.skcomms.dtc.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.skcomms.dtc.client.model.DtcResponse;
import net.skcomms.dtc.client.service.DtcService;
import net.skcomms.dtc.server.model.DtcIni;
import net.skcomms.dtc.server.model.DtcLog;
import net.skcomms.dtc.server.requesthandler.AtpRequestHandler;
import net.skcomms.dtc.server.requesthandler.DtcResult;
import net.skcomms.dtc.server.requesthandler.JsonRequestHandler;
import net.skcomms.dtc.server.requesthandler.RequestHandler;
import net.skcomms.dtc.server.requesthandler.XmlRequestHandler;
import net.skcomms.dtc.server.util.DtcHelper;
import net.skcomms.dtc.server.util.DtcPathHelper;
import net.skcomms.dtc.server.util.DtcRequestHttpAdapter;
import net.skcomms.dtc.server.util.HttpServletHelper;
import net.skcomms.dtc.shared.DtcNodeMeta;
import net.skcomms.dtc.shared.DtcRequest;
import net.skcomms.dtc.shared.DtcRequestMeta;
import net.skcomms.dtc.shared.DtcServiceVerifier;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class DtcServiceImpl extends RemoteServiceServlet implements DtcService {

  private static Map<String, RequestHandler> handlerMap;
  static {
    Map<String, RequestHandler> map = new HashMap<String, RequestHandler>();
    map.put("ATP", new AtpRequestHandler());
    map.put("XML", new XmlRequestHandler());
    map.put("JSON", new JsonRequestHandler());
    DtcServiceImpl.handlerMap = Collections.unmodifiableMap(map);
  }

  private EntityManagerFactory emf;

  private static final Logger logger = Logger.getLogger(DtcServiceImpl.class);

  static List<DtcNodeMeta> getDirImpl(String path) throws IOException {
    String parentPath = DtcPathHelper.getFilePath(path);
    File file = new File(parentPath);
    List<DtcNodeMeta> nodes = new ArrayList<DtcNodeMeta>();

    File[] files = DtcPathHelper.getChildNodes(file);
    for (File child : files) {
      DtcNodeMeta node = DtcHelper.createDtcNodeMeta(child);
      nodes.add(node);
    }
    return nodes;
  }

  private static DtcRequestMeta getDtcRequestMetaImpl(String path) throws IOException {
    DtcIni ini = DtcIniFactory.getIni(path);
    DtcRequestMeta requestMeta = ini.createRequestMeta();
    requestMeta.setPath(path);

    return requestMeta;
  }

  private static DtcResponse getDtcResponseImpl(DtcRequest request) throws IOException,
      FileNotFoundException {

    DtcServiceImpl.preProcessDtcRequest(request);

    DtcResponse response = new DtcResponse();
    response.setRequest(request);

    DtcIni ini = DtcIniFactory.getIni(request.getPath());
    Date startTime = new Date();
    String result = DtcServiceImpl.getHtmlResponse(request, ini);
    response.setResponseTime(new Date().getTime() - startTime.getTime());
    response.setResult(result);

    return response;
  }

  private static String getHtmlResponse(DtcRequest request, DtcIni ini) throws IOException {
    DtcResult result;
    RequestHandler requestHandler = DtcServiceImpl.handlerMap.get(ini.getProtocol());
    if (requestHandler == null) {
      throw new UnsupportedOperationException("Unsupported protocol:" + ini.getProtocol());
    }
    result = requestHandler.handle(request, ini);
    return result.getResult();
  }

  private static void preProcessDtcRequest(DtcRequest request) throws FileNotFoundException,
      IOException {
    if (request.useCndQuery()) {
      String cndQuery = CndQueryProvider.getCndQuery(request.getQuery());
      if (DtcServiceImpl.logger.isDebugEnabled()) {
        DtcServiceImpl.logger.debug("NativeQuery:" + request.getQuery());
        DtcServiceImpl.logger.debug("CndQuery:" + cndQuery);
      }
      request.setCndQuery(cndQuery);
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    Map<String, String> params = HttpServletHelper.rebuildParameterMap(req);
    DtcRequest request = DtcRequestHttpAdapter.createDtcRequest(params);
    DtcResponse dtcResponse = DtcServiceImpl.getDtcResponseImpl(request);
    HttpServletHelper.writeHtmlResponse(resp, dtcResponse.getResult(), request.getCharset());
  }

  @Override
  public List<DtcNodeMeta> getDir(String path) {
    if (!DtcServiceVerifier.isValidDirectoryPath(path)) {
      throw new IllegalArgumentException("Invalid directory format:" + path);
    }

    this.logPath(path);
    try {
      return DtcServiceImpl.getDirImpl(path);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public DtcRequestMeta getDtcRequestMeta(String path) {
    if (!DtcServiceVerifier.isValidTestPage(path)) {
      throw new IllegalArgumentException("Invalid test page:" + path);
    }
    try {
      return DtcServiceImpl.getDtcRequestMetaImpl(path);
    } catch (IOException e) {
      e.printStackTrace();
      throw new IllegalStateException(e);
    }
  }

  @Override
  public DtcResponse getDtcResponse(DtcRequest request) throws IllegalArgumentException {
    try {
      return DtcServiceImpl.getDtcResponseImpl(request);
    } catch (Exception e) {
      e.printStackTrace();
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public void init(ServletConfig config) throws ServletException {
    System.out.println("init() called.");
    super.init(config);
    WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext())
        .getAutowireCapableBeanFactory().autowireBean(this);
  }

  private void logPath(String path) {
    EntityManager manager = this.emf.createEntityManager();
    manager.getTransaction().begin();
    manager.persist(new DtcLog("\"" + path + "\" requested."));
    manager.getTransaction().commit();
    manager.close();
  }

  @Autowired
  void setEntityManagerFactory(EntityManagerFactory emf) {
    if (DtcServiceImpl.logger.isInfoEnabled()) {
      DtcServiceImpl.logger.info("setEntityManagerFactory() called.");
    }
    this.emf = emf;
  }

}
