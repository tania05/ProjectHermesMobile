package ca.projecthermes.projecthermes;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.text.SimpleDateFormat;
import java.util.Date;

import ca.projecthermes.projecthermes.data.HermesDbHelper;
import ca.projecthermes.projecthermes.util.Encryption;

public class SendMessageActivity extends AppCompatActivity {

    private EditText _recipient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Button sendBtn = (Button) this.findViewById(R.id.sendBtn);
        _recipient = (EditText)findViewById(R.id.recipient);
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
                final KeyPair keyPair = Encryption.generateKeyPair();
                hermesDbHelper.insertKey(keyPair);


                saveQRCode(keyPair, SendMessageActivity.this);
            }
        });

        Button lastMsgBtn = (Button) this.findViewById(R.id.lastMsg);
        lastMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SendMessageActivity.this, hermesDbHelper.showLastMsg(), Toast.LENGTH_SHORT).show();
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

    static public void saveQRCode(KeyPair keyPair, Activity activity) {
        FileOutputStream out = null;
        try {
            BitMatrix encoded = (new BarcodeEncoder()).encode(Base64.encodeToString(Encryption.getEncodedPublicKey(keyPair), Base64.DEFAULT), BarcodeFormat.QR_CODE, 20, 20);
            File storageDir = activity.getFilesDir();
            File image = new File(storageDir, "QR_Code.png");

            out = new FileOutputStream(image);
            Log.d("hermes", "saving to " + image.getAbsolutePath());
            Bitmap bit = (new QRCodeEncoder()).encodeAsBitmap(encoded);
            bit.compress(Bitmap.CompressFormat.PNG, 100, out);

        } catch (WriterException | IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
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
