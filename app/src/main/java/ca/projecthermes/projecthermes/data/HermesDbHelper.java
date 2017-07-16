package ca.projecthermes.projecthermes.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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
import ca.projecthermes.projecthermes.util.Util;

import static ca.projecthermes.projecthermes.data.HermesDbContract.KeyPairEntry.COLUMN_PRIVATE_KEY;
import static ca.projecthermes.projecthermes.data.HermesDbContract.KeyPairEntry.COLUMN_PUBLIC_KEY;
import static ca.projecthermes.projecthermes.data.HermesDbContract.MessageEntry.COLUMN_MSG_BODY;
import static ca.projecthermes.projecthermes.data.HermesDbContract.MessageEntry.COLUMN_MSG_ID;
import static ca.projecthermes.projecthermes.data.HermesDbContract.MessageEntry.COLUMN_MSG_VERIFIER;

public class HermesDbHelper extends SQLiteOpenHelper implements IMessageStore {
    private static final String DATABASE_NAME = "hermes.db";
    private static final int DATABASE_VERSION = 3;
    public static final Charset CHARSET = Charset.forName("UTF-16");

    public static final String MESSAGE_ADDED_ACTION = "ca.projecthermes.projecthermes.broadcast.MESSAGE_ADDED";
    public static final String EXTRA_MESSAGE_IDENTIFIER = "identifier";

    private final Context _context;

    public HermesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        _context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_MSG_TABLE =

                "CREATE TABLE " + MessageEntry.TABLE_NAME + " (" +
                        MessageEntry._ID                + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_MSG_ID     + " TEXT NOT NULL, " +
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
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + KeyPairEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public void storeNewEncryptedMessage(String msg, byte[] publicKeyBytes) {
        byte[] msgBytes = msg.getBytes(CHARSET);
        byte[] encryptedMsg = Encryption.encryptString(msgBytes, publicKeyBytes);

        Message m = new Message(Message.generateIdentifier(),
                                Message.getValidVerifier(publicKeyBytes),
                                encryptedMsg);

        Log.d("hermes", "Added message with identifier: " + Util.bytesToHex(m.identifier));
        storeMessage(m);
    }

    public void storeMessage(Message m) {
        SQLiteDatabase db = this.getWritableDatabase();

        if (getMessageForIdentifier(m.identifier) != null) {
            Log.w("HermesDbHelper", "Already have this message stored, skipping.");
            // TODO probably a race condition here.
        } else {
            ContentValues values = new ContentValues();
            values.put(COLUMN_MSG_ID, new String(m.identifier, CHARSET));
            values.put(COLUMN_MSG_BODY, m.body);
            values.put(COLUMN_MSG_VERIFIER, m.verifier);

            long newRowId = db.insert(MessageEntry.TABLE_NAME, null, values);
            Log.d("HermesDbHelper", "Db Row " + newRowId);

            Intent broadcast = new Intent();
            broadcast.setAction(MESSAGE_ADDED_ACTION);
            broadcast.putExtra(EXTRA_MESSAGE_IDENTIFIER, m.identifier);
            _context.sendBroadcast(broadcast);
        }
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

        String hexEncoding = Util.bytesToHex(identifier);
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_MSG_ID + "," + COLUMN_MSG_BODY + "," +
                COLUMN_MSG_VERIFIER + " FROM " + MessageEntry.TABLE_NAME + " WHERE " +
                COLUMN_MSG_ID + " = ?",
                new String[] { new String(identifier, CHARSET) }
        );



        if (cursor.moveToFirst()) {
            byte[] msgId = cursor.getString(cursor.getColumnIndex(COLUMN_MSG_ID)).getBytes(CHARSET);
            byte[] body = cursor.getBlob(cursor.getColumnIndex(COLUMN_MSG_BODY));
            byte[] verifier = cursor.getBlob(cursor.getColumnIndex(COLUMN_MSG_VERIFIER));

            cursor.close();
            return new Message(msgId, verifier, body);
        }

        cursor.close();
        return null;
    }


    public void insertKey(KeyPair keyPair) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_PRIVATE_KEY, Encryption.getEncodedPrivateKey(keyPair));
        values.put(COLUMN_PUBLIC_KEY, Encryption.getEncodedPublicKey(keyPair));

        long newRowId = db.insert(KeyPairEntry.TABLE_NAME, null, values);

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
