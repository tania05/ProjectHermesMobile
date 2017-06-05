package ca.projecthermes.projecthermes.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.security.KeyPair;
import java.util.UUID;

import ca.projecthermes.projecthermes.Encryption;
import ca.projecthermes.projecthermes.data.HermesDbContract.KeyPairEntry;
import ca.projecthermes.projecthermes.data.HermesDbContract.MessageEntry;

import static ca.projecthermes.projecthermes.data.HermesDbContract.KeyPairEntry.COLUMN_PRIVATE_KEY;

public class HermesDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "hermes.db";
    private static final int DATABASE_VERSION = 1;

    public HermesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_WEATHER_TABLE =

                "CREATE TABLE " + MessageEntry.TABLE_NAME + " (" +

                        MessageEntry._ID               + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        MessageEntry.COLUMN_MSG_ID     + " TEXT NOT NULL, " +
                        MessageEntry.COLUMN_MSG_BODY   + " BLOB NOT NULL, " +
                        MessageEntry.COLUMN_MSG_RECIPIENT    + " INTEGER NOT NULL" + ");";

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

    public void insertMessage(String msg, String recipient) {
        SQLiteDatabase db = this.getWritableDatabase();

        byte[] encryptedMsg = Encryption.encryptString(msg, getLastStoredPublicKey());

        ContentValues values = new ContentValues();
        values.put(MessageEntry.COLUMN_MSG_BODY, encryptedMsg);
        values.put(MessageEntry.COLUMN_MSG_ID, UUID.randomUUID().toString());
        values.put(MessageEntry.COLUMN_MSG_RECIPIENT, recipient);

        long newRowId = db.insert(MessageEntry.TABLE_NAME, null, values);
        Log.d("HermesDbHelper", "Db Row " + newRowId);
        db.close();

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


    public byte[] getLastStorePrivateKey() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(KeyPairEntry.TABLE_NAME, new String[]{KeyPairEntry.COLUMN_PRIVATE_KEY},
                null, null, null, null, KeyPairEntry._ID +" DESC", "1");

        if (cursor.moveToFirst()) {
            byte[] privateKey = cursor.getBlob(cursor.getColumnIndex(KeyPairEntry.COLUMN_PRIVATE_KEY));
            Log.d("HermesDbHelper", "Public Key: " + new String(privateKey));
            cursor.close();
            return privateKey;
        }
        return null;
    }

    public String showLastMsg() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(MessageEntry.TABLE_NAME, new String[]{MessageEntry.COLUMN_MSG_BODY},
                null, null, null, null, null);

        if (cursor.moveToLast()) {
            byte[] encryptedMsg = cursor.getBlob(cursor.getColumnIndex(MessageEntry.COLUMN_MSG_BODY));
            String decryptedMsg = Encryption.decryptString(encryptedMsg, getLastStorePrivateKey());
            Log.d("HermesDbHelper", decryptedMsg);
            cursor.close();
            return decryptedMsg;
        }
        return null;
    }

}
