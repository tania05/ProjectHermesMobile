package ca.projecthermes.projecthermes;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.UUID;

import ca.projecthermes.projecthermes.data.MessageContract.MessageEntry;
import ca.projecthermes.projecthermes.data.MessageDbHelper;

public class MainActivity extends AppCompatActivity {

    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent alarmIntent = new Intent(getApplicationContext(), AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 30000, pendingIntent);

        Button sendBtn = (Button) this.findViewById(R.id.sendBtn);
        final EditText recipient = (EditText)findViewById(R.id.recipient);
        final EditText msg = (EditText)findViewById(R.id.msgBody);


        MessageDbHelper messageDbHelper = new MessageDbHelper(this);
        final SQLiteDatabase db = messageDbHelper.getWritableDatabase();


        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ContentValues values = new ContentValues();
                values.put(MessageEntry.COLUMN_MSG_BODY, msg.getText().toString());
                values.put(MessageEntry.COLUMN_MSG_ID, UUID.randomUUID().toString());
                values.put(MessageEntry.COLUMN_MSG_RECIPIENT, recipient.getText().toString());

                long newRowId = db.insert(MessageEntry.TABLE_NAME, null, values);
                Toast.makeText(MainActivity.this, newRowId+"", Toast.LENGTH_SHORT).show();

                Log.d("hermes", "Db Row " + newRowId);
                Log.d("hermes", System.currentTimeMillis() + "");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }
}
