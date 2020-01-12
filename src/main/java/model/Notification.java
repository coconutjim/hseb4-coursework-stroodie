package model;

import org.joda.time.DateTime;

/**
 * Notification
 */
public interface Notification {

    DateTime getDate();
    String getParsedDate();
    String getContent();
    String getUrl();
    void setRead(boolean read);
    boolean isRead();
}
