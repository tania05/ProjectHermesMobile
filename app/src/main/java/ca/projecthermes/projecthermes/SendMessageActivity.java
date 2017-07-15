package ca.projecthermes.projecthermes;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hermesDbHelper.storeNewEncryptedMessage(msg.getText().toString(), "dummy"); //XXX
            }
        });

        Button keysBtn = (Button) this.findViewById(R.id.keys);
        keysBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final KeyPair keyPair = Encryption.generateKeyPair();
                hermesDbHelper.insertKey(keyPair);


                FileOutputStream out = null;
                try {
                    BitMatrix encoded = (new BarcodeEncoder()).encode(Base64.encodeToString(Encryption.getEncodedPublicKey(keyPair), Base64.DEFAULT), BarcodeFormat.QR_CODE, 20, 20);
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String imageFileName = "JPEG_" + timeStamp + "_";
                    File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    File image = File.createTempFile(
                            imageFileName,  /* prefix */
                            ".jpg",         /* suffix */
                            storageDir      /* directory */
                    );

                    out = new FileOutputStream(image);
                    Log.d("hermes", "saving to " + image.getAbsolutePath());
                    Bitmap bit = (new QRCodeEncoder()).encodeAsBitmap(encoded);
                    bit.compress(Bitmap.CompressFormat.PNG, 100, out);

                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DATA, image.getAbsolutePath());
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                    getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

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
        });


        Button scanButton = (Button) this.findViewById(R.id.scanButton);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(SendMessageActivity.this);
                integrator.setOrientationLocked(true);
                integrator.setBarcodeImageEnabled(false);
                integrator.setPrompt("Please scan QR Code");
                integrator.setCaptureActivity(PortraitCaptureActivity.class);
                integrator.initiateScan(IntentIntegrator.QR_CODE_TYPES);
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
