package ca.projecthermes.projecthermes.data;

import android.provider.BaseColumns;

public class MessageContract {

    public static final class MessageEntry implements BaseColumns {
        public static final String TABLE_NAME = "Messages";
        public static final String COLUMN_MSG_ID = "msgId";
        public static final String COLUMN_MSG_BODY = "body";
        public static final String COLUMN_MSG_RECIPIENT = "recipient";
    }
}