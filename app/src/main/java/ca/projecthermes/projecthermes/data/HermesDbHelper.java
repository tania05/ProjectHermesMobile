package ca.projecthermes.projecthermes.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.nio.charset.Charset;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import ca.projecthermes.projecthermes.Ethereum.Ethereum;
import ca.projecthermes.projecthermes.data.HermesDbContract.ContactKeysEntry;
import ca.projecthermes.projecthermes.data.HermesDbContract.DecodedEntry;
import ca.projecthermes.projecthermes.data.HermesDbContract.KeyPairEntry;
import ca.projecthermes.projecthermes.data.HermesDbContract.MessageEntry;
import ca.projecthermes.projecthermes.networking.payload.Message;
import ca.projecthermes.projecthermes.util.Encryption;
import ca.projecthermes.projecthermes.util.Util;

import static ca.projecthermes.projecthermes.data.HermesDbContract.ContactKeysEntry.COLUMN_CONTACT_NAME;
import static ca.projecthermes.projecthermes.data.HermesDbContract.ContactKeysEntry.COLUMN_CONTACT_PUBLIC_KEY;
import static ca.projecthermes.projecthermes.data.HermesDbContract.KeyPairEntry.COLUMN_NAME;
import static ca.projecthermes.projecthermes.data.HermesDbContract.KeyPairEntry.COLUMN_PRIVATE_KEY;
import static ca.projecthermes.projecthermes.data.HermesDbContract.KeyPairEntry.COLUMN_PUBLIC_KEY;
import static ca.projecthermes.projecthermes.data.HermesDbContract.MessageEntry.COLUMN_MSG_BODY;
import static ca.projecthermes.projecthermes.data.HermesDbContract.MessageEntry.COLUMN_MSG_ID;
import static ca.projecthermes.projecthermes.data.HermesDbContract.MessageEntry.COLUMN_MSG_KEY;
import static ca.projecthermes.projecthermes.data.HermesDbContract.MessageEntry.COLUMN_MSG_PRIVATE_NONCE;
import static ca.projecthermes.projecthermes.data.HermesDbContract.MessageEntry.COLUMN_MSG_PUBLIC_NONCE;
import static ca.projecthermes.projecthermes.data.HermesDbContract.MessageEntry.COLUMN_MSG_VERIFIER;

public class HermesDbHelper extends SQLiteOpenHelper implements IMessageStore {
    private static final String DATABASE_NAME = "hermes.db";
    private static final int DATABASE_VERSION = 12;
    public static final Charset CHARSET = Charset.forName("UTF-16");
    public static final Charset ID_CHARSET = Charset.forName("US-ASCII");

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
                        COLUMN_MSG_ID     + " TEXT NOT NULL," +
                        COLUMN_MSG_VERIFIER    + " BLOB NOT NULL, " +
                        COLUMN_MSG_KEY    + " BLOB NOT NULL, " +
                        COLUMN_MSG_BODY   + " BLOB NOT NULL, " +
                        COLUMN_MSG_PUBLIC_NONCE   + " BLOB NOT NULL, " +
                        COLUMN_MSG_PRIVATE_NONCE  + " BLOB NOT NULL" +
                ");";

        sqLiteDatabase.execSQL(SQL_CREATE_MSG_TABLE);

        final String SQL_CREATE_KEYPAIR_TABLE =
                "CREATE TABLE " + KeyPairEntry.TABLE_NAME + " (" +
                        KeyPairEntry._ID    + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_NAME         + " TEXT NOT NULL UNIQUE, " +
                        COLUMN_PUBLIC_KEY  + " BLOB NOT NULL, " +
                        COLUMN_PRIVATE_KEY + " BLOB NOT NULL" + ");";
        sqLiteDatabase.execSQL(SQL_CREATE_KEYPAIR_TABLE);



        final String SQL_CREATE_CONTACTS_TABLE =
                "CREATE TABLE " + ContactKeysEntry.TABLE_NAME + " (" +
                        ContactKeysEntry._ID    + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_CONTACT_NAME         + " TEXT NOT NULL UNIQUE, " +
                        COLUMN_CONTACT_PUBLIC_KEY + " BLOB NOT NULL" + ");";
        sqLiteDatabase.execSQL(SQL_CREATE_CONTACTS_TABLE);

        final String SQL_CREATE_DECODED_TABLE =
                "CREATE TABLE " + DecodedEntry.TABLE_NAME + " (" +
                        DecodedEntry._ID        + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        DecodedEntry.COLUMN_MSG_ID + " TEXT NOT NULL UNIQUE, " +
                        DecodedEntry.COLUMN_MSG_BODY + " BLOB NOT NULL, " +
                        DecodedEntry.COLUMN_DECODING_ALIAS + " TEXT NOT NULL" + ");";
        sqLiteDatabase.execSQL(SQL_CREATE_DECODED_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MessageEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + KeyPairEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ContactKeysEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DecodedEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public void storeNewEncryptedMessage(String msg, byte[] publicKeyBytes) {
        byte[] msgBytes = msg.getBytes(CHARSET);
        byte[] key = Encryption.generateAESKey();
        byte[] encryptedMsg = Encryption.encryptUnderAes(key, msgBytes);//Encryption.encryptString(msgBytes, publicKeyBytes);
        byte[] encryptedKey = Encryption.encryptString(key, publicKeyBytes);

        byte[] publicNonce = Encryption.getBytesFromUUID(UUID.randomUUID());
        byte[] privateNonce = Encryption.getBytesFromUUID(UUID.randomUUID());
        byte[] encryptedPrivateNonce = Encryption.encryptString(privateNonce, publicKeyBytes);

        byte[] msgId = Encryption.getBytesFromUUID(UUID.randomUUID());
        Message m = new Message(msgId,
                                Message.getValidVerifier(publicKeyBytes),
                                encryptedKey,
                                encryptedMsg,
                                publicNonce,
                                encryptedPrivateNonce);

        Log.d("hermes", "Added message with identifier: " + Util.bytesToHex(m.identifier));
        Ethereum.getInstance(_context).newMessage(msgId, publicNonce, privateNonce);
        storeMessage(m);
    }

    public void storeMessage(Message m) {
        SQLiteDatabase db = this.getWritableDatabase();

        if (getMessageForIdentifier(m.identifier) != null) {
            Log.w("HermesDbHelper", "Already have this message stored, skipping.");
            // TODO probably a race condition here.
        } else {
            Log.e("FFFF", Ethereum.hexToString(m.identifier));
            ContentValues values = new ContentValues();
            values.put(COLUMN_MSG_ID, Ethereum.hexToString(m.identifier));
            values.put(COLUMN_MSG_BODY, m.body);
            values.put(COLUMN_MSG_KEY, m.key);
            values.put(COLUMN_MSG_VERIFIER, m.verifier);
            values.put(COLUMN_MSG_PUBLIC_NONCE, m.publicNonce);
            values.put(COLUMN_MSG_PRIVATE_NONCE, m.privateNonce);

            long newRowId = db.insert(MessageEntry.TABLE_NAME, null, values);
            Log.d("HermesDbHelper", "Db Row " + newRowId);

            onEncryptedStored(m, db);
            Ethereum.getInstance(_context).addHop(m.identifier, m.publicNonce);
        }
    }

    private void onEncryptedStored(Message m, SQLiteDatabase db) {
        Intent broadcast = new Intent();
        broadcast.setAction(MESSAGE_ADDED_ACTION);
        broadcast.putExtra(EXTRA_MESSAGE_IDENTIFIER, m.identifier);
        _context.sendBroadcast(broadcast);

        Cursor cursor = db.query(
                KeyPairEntry.TABLE_NAME,
                new String[] { KeyPairEntry.COLUMN_NAME, KeyPairEntry.COLUMN_PRIVATE_KEY},
                null,
                null,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            do {
                byte[] privateKey = cursor.getBlob(cursor.getColumnIndex(KeyPairEntry.COLUMN_PRIVATE_KEY));

                byte[] decryptedVerifier = Encryption.decryptString(m.verifier, privateKey);
                if (Arrays.equals(decryptedVerifier, Message.VALID_VERIFIER)) {
                    // we can decrypt.
                    byte[] decryptedAesKey = Encryption.decryptString(m.key, privateKey);
                    byte[] decryptedMessage = Encryption.decryptUnderAes(decryptedAesKey, m.body);
                    String decryptingAlias = cursor.getString(cursor.getColumnIndex(KeyPairEntry.COLUMN_NAME));
                    byte[] decryptedPrivateNonce = Encryption.decryptString(m.privateNonce, privateKey);

                    Log.d("hermesdb", "Successfully decrypted identifier " + Ethereum.hexToString(m.identifier) + " with alias " + decryptingAlias);
                    Ethereum.getInstance(_context).receiveMessage(m.identifier, decryptedPrivateNonce);

                    storeDecryptedMessage(m.identifier, decryptedMessage, decryptingAlias, db);
                    break;
                }
            } while (cursor.moveToNext());
        }
    }

    private void storeDecryptedMessage(byte[] identifier, byte[] decryptedMessage, String name, SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(DecodedEntry.COLUMN_DECODING_ALIAS, name);
        values.put(DecodedEntry.COLUMN_MSG_ID, Ethereum.hexToString(identifier));
        values.put(DecodedEntry.COLUMN_MSG_BODY, decryptedMessage);
        long newRowId = db.insert(DecodedEntry.TABLE_NAME, null, values);
        Log.e("hermesdb", "Stored decrypted message row " + newRowId);
    }



    @Override
    public ArrayList<byte[]> getStoredMessageIdentifiers() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<byte[]> msgIdList = new ArrayList<>();

        Cursor cursor = db.query(MessageEntry.TABLE_NAME, new String[]{COLUMN_MSG_ID},
                null, null, null, null, null);
        cursor.moveToFirst();

        while(cursor.moveToNext()) {
            byte[] msgId = Ethereum.hexStringToByteArray(cursor.getString(cursor.getColumnIndex(COLUMN_MSG_ID)));
            msgIdList.add(msgId);
        }
        cursor.close();

        return msgIdList;
    }

    @Override
    public Message getMessageForIdentifier(byte[] identifier) {

        SQLiteDatabase db = this.getReadableDatabase();

        String hexEncoding = Util.bytesToHex(identifier);
        Cursor cursor = db.rawQuery("SELECT * FROM " + MessageEntry.TABLE_NAME + " WHERE " +
                COLUMN_MSG_ID + " = ?",
                new String[] { Ethereum.hexToString(identifier) }
        );

        if (cursor.moveToFirst()) {
            byte[] msgId = Ethereum.hexStringToByteArray(cursor.getString(cursor.getColumnIndex(COLUMN_MSG_ID)));
            byte[] body = cursor.getBlob(cursor.getColumnIndex(COLUMN_MSG_BODY));
            byte[] key = cursor.getBlob(cursor.getColumnIndex(COLUMN_MSG_KEY));
            byte[] verifier = cursor.getBlob(cursor.getColumnIndex(COLUMN_MSG_VERIFIER));
            byte[] publicNonce = cursor.getBlob(cursor.getColumnIndex(COLUMN_MSG_PUBLIC_NONCE));
            byte[] privateNonce = cursor.getBlob(cursor.getColumnIndex(COLUMN_MSG_PRIVATE_NONCE));

            cursor.close();
            return new Message(msgId, verifier, key, body, publicNonce, privateNonce);
        }

        cursor.close();
        return null;
    }


    public boolean insertKey(KeyPair keyPair, String keyName) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, keyName);
            values.put(COLUMN_PRIVATE_KEY, Encryption.getEncodedPrivateKey(keyPair));
            values.put(COLUMN_PUBLIC_KEY, Encryption.getEncodedPublicKey(keyPair));

            db.insertOrThrow(KeyPairEntry.TABLE_NAME, null, values);
            return true;
        }
        catch (SQLException e){
            Log.e("Insert Key FAILED", Arrays.toString(e.getStackTrace()));
            return false;
        }
    }

    public boolean insertContact(byte[] key, String keyName){
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(COLUMN_CONTACT_NAME, keyName);
            values.put(COLUMN_CONTACT_PUBLIC_KEY, key);

            db.insertOrThrow(ContactKeysEntry.TABLE_NAME, null, values);
            return true;
        }
        catch (SQLException e){
            Log.e("Insert contct FAILED", Arrays.toString(e.getStackTrace()));
            return false;
        }
    }

    public byte[] getReciepientKey(String contactName){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(ContactKeysEntry.TABLE_NAME, new String [] {ContactKeysEntry.COLUMN_CONTACT_PUBLIC_KEY}, COLUMN_CONTACT_NAME + " = ?",new String[]{contactName}, null, null, null);

        if(cursor.moveToFirst()){
            byte [] publicKey = cursor.getBlob(cursor.getColumnIndex(COLUMN_CONTACT_PUBLIC_KEY));
            cursor.close();
            return publicKey;
        }
        return null;
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

    public void deleteDecodedMsg(String msgId) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db.delete(DecodedEntry.TABLE_NAME, DecodedEntry.COLUMN_MSG_ID + " = ?", new String[]{msgId}) != 0) {
            Log.e("Deleted:::", "SUCCESS");
        }
    }
    public void deleteAlias(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(KeyPairEntry.TABLE_NAME, KeyPairEntry.COLUMN_NAME + " = ?", new String[] { name });
    }

    public void deleteContact(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(ContactKeysEntry.TABLE_NAME, ContactKeysEntry.COLUMN_CONTACT_NAME + " = ?", new String[] { name });
    }
}
