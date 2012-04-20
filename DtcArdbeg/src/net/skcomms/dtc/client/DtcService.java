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
package net.skcomms.dtc.client;

import java.util.List;

import net.skcomms.dtc.shared.DtcNodeInfo;
import net.skcomms.dtc.shared.DtcRequestInfo;
import net.skcomms.dtc.shared.DtcServiceVerifier;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("dtcservice")
public interface DtcService extends RemoteService {

  /**
   * Utility class for simplifying access to the instance of async service.
   */
  public static class Util {
    private static DtcServiceAsync instance;

    public static DtcServiceAsync getInstance() {
      if (Util.instance == null) {
        Util.instance = GWT.create(DtcService.class);
      }
      return Util.instance;
    }
  }

  /**
   * ���丮 ����Ʈ�� �����´�.
   * 
   * @param path
   *          ���丮 ���. ����� ��ȿ����
   *          {@link DtcServiceVerifier#isValidDirectoryPath(String)}�� ����.
   * @return ���丮 ������ ����Ʈ.
   * @throws IllegalArgumentException
   *           ���丮 ��ΰ� ��ȿ���� ���� ���.
   */
  List<DtcNodeInfo> getDir(String path) throws IllegalArgumentException;

  DtcRequestInfo getDtcRequestPageInfo(String path);

}
