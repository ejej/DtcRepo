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
  public static boolean isValidDirectoryPath(String path) {
    if (path == null) {
      return false;
    }

    String regex = "/([a-zA-Z0-9_.-]+/)*";
    return path.matches(regex);
  }

  /**
   * ������ �׽�Ʈ �������� ��ȿ���� �˻��Ѵ�.
   * 
   * ���Խ� "/([a-zA-Z0-9_.-]+/)*[a-zA-Z0-9_.-]+\\.ini"�� ��ġ�ϸ� ��ȿ�� ������ �Ǵ��Ѵ�.
   * 
   * @param path
   * @return
   */
  public static boolean isValidTestPage(String path) {
    if (path == null) {
      return false;
    }

    String regex = "/([a-zA-Z0-9_.-]+/)*[a-zA-Z0-9_.-]+\\.ini";
    return path.matches(regex);
  }

}
