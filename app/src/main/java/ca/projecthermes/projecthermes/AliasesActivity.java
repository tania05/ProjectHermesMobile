package ca.projecthermes.projecthermes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
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
            @SuppressLint("InflateParams")
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AliasesActivity.this);
                LayoutInflater inflater = AliasesActivity.this.getLayoutInflater();

                builder.setView(inflater.inflate(R.layout.dialog_add_alias, null))
                        .setTitle("Create New Alias")
                        .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).setNegativeButton(android.R.string.cancel, null)
                        .show();

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
