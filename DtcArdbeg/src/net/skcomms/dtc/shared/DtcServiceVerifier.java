/**
 * 
 */
package net.skcomms.dtc.shared;

/**
 * @author jujang@sk.com
 * 
 */
public class DtcServiceVerifier {

  /**
   * ������ ����� ��ȿ���� �˻��Ѵ�.
   * 
   * ���Խ� "/([a-zA-Z0-9_.-]+/)*"�� ��ġ�ϸ� ��ȿ�� ������ �Ǵ��Ѵ�.
   * 
   * @param path
   *          �˻��� ���
   * @return ��ȿ�� ��� true; �ƴϸ� false
   */
  public static boolean isValidPath(String path) {
    if (path == null) {
      return false;
    }

    String regex = "/([a-zA-Z0-9_.-]+/)*";
    return path.matches(regex);
  }

}
