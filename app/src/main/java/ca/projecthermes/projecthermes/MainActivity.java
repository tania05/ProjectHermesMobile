package ca.projecthermes.projecthermes;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import ca.projecthermes.projecthermes.data.HermesDbHelper;

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


        final HermesDbHelper hermesDbHelper = new HermesDbHelper(this);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                hermesDbHelper.insertMessage(msg.getText().toString(),
                                              recipient.getText().toString());
                Log.d("hermes", System.currentTimeMillis() + "");
            }
        });

        Button keysBtn = (Button) this.findViewById(R.id.keys);
        keysBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hermesDbHelper.insertKey(Encryption.generateKeyPair());
            }
        });

        Button lastMsgBtn = (Button) this.findViewById(R.id.lastMsg);
        lastMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, hermesDbHelper.showLastMsg(), Toast.LENGTH_SHORT).show();
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
