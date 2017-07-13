package ca.projecthermes.projecthermes.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.nio.charset.Charset;
import java.security.KeyPair;
import java.util.ArrayList;

import ca.projecthermes.projecthermes.data.HermesDbContract.KeyPairEntry;
import ca.projecthermes.projecthermes.data.HermesDbContract.MessageEntry;
import ca.projecthermes.projecthermes.networking.payload.Message;
import ca.projecthermes.projecthermes.util.Encryption;

import static ca.projecthermes.projecthermes.data.HermesDbContract.KeyPairEntry.COLUMN_PRIVATE_KEY;

public class HermesDbHelper extends SQLiteOpenHelper implements IMessageStore {
    private static final String DATABASE_NAME = "hermes.db";
    private static final int DATABASE_VERSION = 1;

    public HermesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_WEATHER_TABLE =

                "CREATE TABLE " + MessageEntry.TABLE_NAME + " (" +
                        MessageEntry._ID                + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        MessageEntry.COLUMN_MSG_ID     + " TEXT NOT NULL, " +
                        MessageEntry.COLUMN_MSG_BODY   + " TEXT NOT NULL, " +
                        MessageEntry.COLUMN_MSG_VERIFIER    + " BLOB NOT NULL" + ");";

        sqLiteDatabase.execSQL(SQL_CREATE_WEATHER_TABLE);

        final String SQL_CREATE_KEYPAIR_TABLE =
                "CREATE TABLE " + KeyPairEntry.TABLE_NAME + " (" +
                        KeyPairEntry._ID                + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        KeyPairEntry.COLUMN_PUBLIC_KEY  + " BLOB NOT NULL, " +
                        COLUMN_PRIVATE_KEY + " BLOB NOT NULL" + ");";
        sqLiteDatabase.execSQL(SQL_CREATE_KEYPAIR_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MessageEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public void storeNewEncryptedMessage(String msg, byte[] publicKey) {
        byte[] msgBytes = msg.getBytes(Charset.forName("UTF-16"));
//        byte[] encryptedMsg = Encryption.encryptString(msgBytes, publicKey);
        Message m = new Message(Message.generateIdentifier(msgBytes),
                                Message.getValidVerifier(publicKey), msgBytes);
        storeMessage(m);
    }

    public void storeMessage(Message m) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MessageEntry.COLUMN_MSG_ID, new String(m.identifier, Charset.forName("US-ASCII")));
        values.put(MessageEntry.COLUMN_MSG_BODY, m.body);
        values.put(MessageEntry.COLUMN_MSG_VERIFIER, m.verifier);

        long newRowId = db.insert(MessageEntry.TABLE_NAME, null, values);
        Log.d("HermesDbHelper", "Db Row " + newRowId);
        db.close();
    }

    @Override
    public ArrayList<byte[]> getStoredMessageIdentifiers() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<byte[]> msgIdList = new ArrayList<>();

        Cursor cursor = db.query(MessageEntry.TABLE_NAME, new String[]{MessageEntry.COLUMN_MSG_ID},
                null, null, null, null, null);
        cursor.moveToFirst();

        while(cursor.moveToNext()) {
            String msgId = cursor.getString(cursor.getColumnIndex(MessageEntry.COLUMN_MSG_ID));
            msgIdList.add(msgId.getBytes(Charset.forName("US-ASCII")));
            Log.d(this.getClass().getCanonicalName(), msgId);
        }
        cursor.close();

        return msgIdList;
    }

    @Override
    public Message getMessageForIdentifier(byte[] identifier) {
        Log.d(this.getClass().getCanonicalName(), "id: " + new String(identifier, Charset.forName("US-ASCII")));

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(MessageEntry.TABLE_NAME, new String[]{MessageEntry.COLUMN_MSG_ID,
                                MessageEntry.COLUMN_MSG_BODY, MessageEntry.COLUMN_MSG_VERIFIER},
                MessageEntry.COLUMN_MSG_ID + " = ?", new String[]{new String(identifier, Charset.forName("US-ASCII"))}, null, null, null);

        if (cursor.moveToFirst()) {
            String msgId = cursor.getString(cursor.getColumnIndex(MessageEntry.COLUMN_MSG_ID));
            byte[] body = cursor.getBlob(cursor.getColumnIndex(MessageEntry.COLUMN_MSG_BODY));
            byte[] verifier = cursor.getBlob(cursor.getColumnIndex(MessageEntry.COLUMN_MSG_VERIFIER));

            return new Message(msgId.getBytes(Charset.forName("US-ASCII")), verifier, body);
        }
        return null;
    }


    public void insertKey(KeyPair keyPair) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_PRIVATE_KEY, Encryption.getEncodedPrivateKey(keyPair));
        values.put(KeyPairEntry.COLUMN_PUBLIC_KEY, Encryption.getEncodedPublicKey(keyPair));

        long newRowId = db.insert(KeyPairEntry.TABLE_NAME, null, values);

        Log.d("HermesDbHelper", "Inserting Public Key: " + new String(Encryption.getEncodedPublicKey(keyPair)));
        Log.d("HermesDbHelper", "Db Row for key: " + newRowId);
        db.close();
    }

    public byte[] getLastStoredPublicKey() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(KeyPairEntry.TABLE_NAME, new String[]{KeyPairEntry.COLUMN_PUBLIC_KEY},
                                 null, null, null, null, KeyPairEntry._ID +" DESC", "1");

        if (cursor.moveToFirst()) {
            byte[] publicKey = cursor.getBlob(cursor.getColumnIndex(KeyPairEntry.COLUMN_PUBLIC_KEY));
            Log.d("HermesDbHelper", "Public Key: " + new String(publicKey));
            cursor.close();
            return publicKey;
        }
        return null;
    }


    public byte[] getLastStoredPrivateKey() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(KeyPairEntry.TABLE_NAME, new String[]{KeyPairEntry.COLUMN_PRIVATE_KEY},
                null, null, null, null, KeyPairEntry._ID +" DESC", "1");

        if (cursor.moveToFirst()) {
            byte[] privateKey = cursor.getBlob(cursor.getColumnIndex(KeyPairEntry.COLUMN_PRIVATE_KEY));
            Log.d("HermesDbHelper", "Public Key: " + new String(privateKey));
            cursor.close();
            return privateKey;
        }
        cursor.close();
        return null;
    }

    public String showLastMsg() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(MessageEntry.TABLE_NAME, new String[]{MessageEntry.COLUMN_MSG_ID, MessageEntry.COLUMN_MSG_BODY},
                null, null, null, null, MessageEntry._ID + " DESC", "1");

        if (cursor.moveToFirst()) {
            byte[] encryptedMsg = cursor.getBlob(cursor.getColumnIndex(MessageEntry.COLUMN_MSG_BODY));
//            String decryptedMsg = Encryption.decryptString(encryptedMsg, getLastStorePrivateKey());
            Log.d("LastMsg", new String(encryptedMsg, Charset.forName("US-ASCII")));
            cursor.close();
            //XXX
            return new String(encryptedMsg, Charset.forName("US-ASCII"));
        }
        return null;
    }

}
