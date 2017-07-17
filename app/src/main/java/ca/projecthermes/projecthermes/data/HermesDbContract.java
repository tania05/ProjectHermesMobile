package ca.projecthermes.projecthermes.data;

import android.provider.BaseColumns;

public class HermesDbContract {

    public static final class MessageEntry implements BaseColumns {
        public static final String TABLE_NAME = "Messages";
        public static final String COLUMN_MSG_ID = "msgId";
        public static final String COLUMN_MSG_BODY = "body";
        public static final String COLUMN_MSG_KEY = "key";
        public static final String COLUMN_MSG_VERIFIER = "verifier";
    }
    
    public static final class KeyPairEntry implements BaseColumns {
        public static final String TABLE_NAME = "KeyPairs";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PUBLIC_KEY = "publicKey";
        public static final String COLUMN_PRIVATE_KEY = "privateKey";
    }

    public static final class ContactKeysEntry implements BaseColumns{
        public static final String TABLE_NAME = "ContactKeys";
        public static final String COLUMN_CONTACT_NAME = "name";
        public static final String COLUMN_CONTACT_PUBLIC_KEY = "publicKey";
    }
}