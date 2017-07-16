package ca.projecthermes.projecthermes;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.security.KeyPair;

import ca.projecthermes.projecthermes.util.Encryption;

public class AliasesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aliases);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_alias);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

//        final KeyPair keyPair = Encryption.generateKeyPair();
//        hermesDbHelper.insertKey(keyPair);
//        SendMessageActivity.saveQRCode(keyPair, this);
//        File file = new File(this.getFilesDir(),"QR_Code.png");
//        if(file.exists()) {
//            ImageView imageView = (ImageView) findViewById(R.id.imageView);
//            imageView.setImageURI(Uri.fromFile(file));
//        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
