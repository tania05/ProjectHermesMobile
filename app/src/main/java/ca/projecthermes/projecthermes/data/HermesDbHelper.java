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
import static ca.projecthermes.projecthermes.data.HermesDbContract.KeyPairEntry.COLUMN_PUBLIC_KEY;
import static ca.projecthermes.projecthermes.data.HermesDbContract.MessageEntry.COLUMN_MSG_BODY;
import static ca.projecthermes.projecthermes.data.HermesDbContract.MessageEntry.COLUMN_MSG_ID;
import static ca.projecthermes.projecthermes.data.HermesDbContract.MessageEntry.COLUMN_MSG_VERIFIER;

public class HermesDbHelper extends SQLiteOpenHelper implements IMessageStore {
    private static final String DATABASE_NAME = "hermes.db";
    private static final int DATABASE_VERSION = 1;
    public static final Charset CHARSET = Charset.forName("UTF-16");

    public HermesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_MSG_TABLE =

                "CREATE TABLE " + MessageEntry.TABLE_NAME + " (" +
                        MessageEntry._ID                + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_MSG_ID     + " BLOB NOT NULL, " +
                        COLUMN_MSG_BODY   + " BLOB NOT NULL, " +
                        COLUMN_MSG_VERIFIER    + " BLOB NOT NULL" + ");";

        sqLiteDatabase.execSQL(SQL_CREATE_MSG_TABLE);

        final String SQL_CREATE_KEYPAIR_TABLE =
                "CREATE TABLE " + KeyPairEntry.TABLE_NAME + " (" +
                        KeyPairEntry._ID                + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_PUBLIC_KEY  + " BLOB NOT NULL, " +
                        COLUMN_PRIVATE_KEY + " BLOB NOT NULL" + ");";
        sqLiteDatabase.execSQL(SQL_CREATE_KEYPAIR_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MessageEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public void storeNewEncryptedMessage(String msg, String publicKey) {
        byte[] msgBytes = msg.getBytes(CHARSET);
        byte[] publicKeyBytes = publicKey.getBytes(CHARSET);
        byte[] encryptedMsg = Encryption.encryptString(msgBytes, getLastStoredPublicKey()); //XXX

        Message m = new Message(Message.generateIdentifier(),
                                Message.getValidVerifier(getLastStoredPublicKey()), //XXX
                                encryptedMsg);
        storeMessage(m);
    }

    public void storeMessage(Message m) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_MSG_ID, m.identifier);
        values.put(COLUMN_MSG_BODY, m.body);
        values.put(COLUMN_MSG_VERIFIER, m.verifier);

        long newRowId = db.insert(MessageEntry.TABLE_NAME, null, values);
        Log.d("HermesDbHelper", "Db Row " + newRowId);
        db.close();
    }

    @Override
    public ArrayList<byte[]> getStoredMessageIdentifiers() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<byte[]> msgIdList = new ArrayList<>();

        Cursor cursor = db.query(MessageEntry.TABLE_NAME, new String[]{COLUMN_MSG_ID},
                null, null, null, null, null);
        cursor.moveToFirst();

        while(cursor.moveToNext()) {
            byte[] msgId = cursor.getBlob(cursor.getColumnIndex(COLUMN_MSG_ID));
            msgIdList.add(msgId);
        }
        cursor.close();

        return msgIdList;
    }

    @Override
    public Message getMessageForIdentifier(byte[] identifier) {

        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + COLUMN_MSG_ID + "," + COLUMN_MSG_BODY + "," +
                COLUMN_MSG_VERIFIER + " FROM " + MessageEntry.TABLE_NAME + " çWHERE " +
                COLUMN_MSG_ID + " = " + identifier;
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            byte[] msgId = cursor.getBlob(cursor.getColumnIndex(COLUMN_MSG_ID));
            byte[] body = cursor.getBlob(cursor.getColumnIndex(COLUMN_MSG_BODY));
            byte[] verifier = cursor.getBlob(cursor.getColumnIndex(COLUMN_MSG_VERIFIER));

            return new Message(msgId, verifier, body);
        }
        return null;
    }


    public void insertKey(KeyPair keyPair) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_PRIVATE_KEY, Encryption.getEncodedPrivateKey(keyPair));
        values.put(COLUMN_PUBLIC_KEY, Encryption.getEncodedPublicKey(keyPair));

        long newRowId = db.insert(KeyPairEntry.TABLE_NAME, null, values);

        db.close();
    }

    public byte[] getLastStoredPublicKey() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(KeyPairEntry.TABLE_NAME, new String[]{COLUMN_PUBLIC_KEY},
                                 null, null, null, null, KeyPairEntry._ID +" DESC", "1");

        if (cursor.moveToFirst()) {
            byte[] publicKey = cursor.getBlob(cursor.getColumnIndex(COLUMN_PUBLIC_KEY));
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
            cursor.close();
            return privateKey;
        }
        cursor.close();
        return null;
    }

}
