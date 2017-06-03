package ca.projecthermes.projecthermes.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ca.projecthermes.projecthermes.data.MessageContract.MessageEntry;

public class MessageDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "message.db";
    private static final int DATABASE_VERSION = 1;

    public MessageDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_WEATHER_TABLE =

                "CREATE TABLE " + MessageEntry.TABLE_NAME + " (" +

                        MessageEntry._ID               + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        MessageEntry.COLUMN_MSG_ID     + " TEXT NOT NULL, " +
                        MessageEntry.COLUMN_MSG_BODY   + " TEXT NOT NULL, " +
                        MessageEntry.COLUMN_MSG_RECIPIENT    + " INTEGER NOT NULL" + ");";

        sqLiteDatabase.execSQL(SQL_CREATE_WEATHER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MessageEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

}
