package ca.projecthermes.projecthermes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;

import ca.projecthermes.projecthermes.data.HermesDbHelper;
import ca.projecthermes.projecthermes.util.Encryption;

public class SendMessageActivity extends AppCompatActivity {

    private EditText _recipient;
    public static final String RECIEPEINT_ADDR = "recient_addr";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Button sendBtn = (Button) this.findViewById(R.id.sendBtn);
        _recipient = (EditText)findViewById(R.id.recipient);

        byte [] recipientAddr = getIntent().getByteArrayExtra(RECIEPEINT_ADDR);

        if(recipientAddr!= null)
            _recipient.setText(Base64.encodeToString(recipientAddr, Base64.DEFAULT));

        final EditText msg = (EditText)findViewById(R.id.msgBody);

        final HermesDbHelper hermesDbHelper = new HermesDbHelper(this);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String recipient = _recipient.getText().toString();
                byte[] decodedRecipient = Base64.decode(recipient, Base64.DEFAULT);

                hermesDbHelper.storeNewEncryptedMessage(msg.getText().toString(), decodedRecipient);

                @SuppressLint("InflateParams") View alert = getLayoutInflater().inflate(R.layout.sent_message_confimation, null);


                new AlertDialog.Builder(SendMessageActivity.this).setView(alert)
                        .show();

                Toolbar message_sent_toolbar = (Toolbar) alert.findViewById(R.id.message_transmission);
                message_sent_toolbar.setTitle("Message transmitting...");
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onBackPressed();
                    }
                }, (int)(Math.random()*700)+1800);
            }
        });

        Button scanButton = (Button) this.findViewById(R.id.scanButton);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QRCodeEncoder.scanQRCode(SendMessageActivity.this);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (result != null) {
            // We have the QR code information.
            _recipient.setText(result.getContents());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
