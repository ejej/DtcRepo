package net.skcomms.dtc.client.model;

import java.util.Date;

public class DtcVisitedPage {
  private static DtcVisitedPage create(String path, Date time) {
    return new DtcVisitedPage(path, time);
  }

  public static DtcVisitedPage deserialize(String source) {
    if (source == null) {
      throw new IllegalArgumentException("Error: Source should not be null!");
    }
    String[] elements = source.split(DtcSearchHistory.FORM_FIELD_DELIMETER);
    if (elements.length == 0) {
      throw new IllegalArgumentException("Error: Invalid argument: " + source);
    }
    String path = elements[0];
    Date time = new Date(Long.parseLong(elements[1]));

    return DtcVisitedPage.create(path, time);

  }

  private final String path;
  private final Date accessTime;

  private DtcVisitedPage(String path, Date time) {
    this.path = path;
    this.accessTime = time;
  }

  public Date getAccessTime() {
    return this.accessTime;
  }

  public String getPath() {
    return this.path;
  }

  public String serialize() {
    StringBuilder result = new StringBuilder();

    result.append(this.path);
    result.append(DtcSearchHistory.FORM_FIELD_DELIMETER);

    result.append(this.accessTime.getTime());
    result.append(DtcSearchHistory.FORM_FIELD_DELIMETER);

    return result.toString();
  }
}
