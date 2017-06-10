package ca.projecthermes.projecthermes;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import ca.projecthermes.projecthermes.data.HermesDbHelper;
import ca.projecthermes.projecthermes.util.Encryption;

public class SendMessageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent alarmIntent = new Intent(getApplicationContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 30000, pendingIntent);

        Button sendBtn = (Button) this.findViewById(R.id.sendBtn);
        final EditText recipient = (EditText)findViewById(R.id.recipient);
        final EditText msg = (EditText)findViewById(R.id.msgBody);


        final HermesDbHelper hermesDbHelper = new HermesDbHelper(this);
        //XXX
        hermesDbHelper.insertKey(Encryption.generateKeyPair());


        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Demo encryption with own public key
                hermesDbHelper.storeNewEncryptedMessage(msg.getText().toString(),
                        hermesDbHelper.getLastStoredPublicKey());
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
                Toast.makeText(SendMessageActivity.this, hermesDbHelper.showLastMsg(), Toast.LENGTH_SHORT).show();
            }
        });

    }

}
