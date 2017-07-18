package ca.projecthermes.projecthermes;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.security.KeyPair;

import ca.projecthermes.projecthermes.data.AliasAdapter;
import ca.projecthermes.projecthermes.data.HermesDbContract;
import ca.projecthermes.projecthermes.data.HermesDbHelper;
import ca.projecthermes.projecthermes.util.Encryption;

public class AliasesActivity extends AppCompatActivity {
    private HermesDbHelper hermesDbHelper;
    private AliasAdapter aliasAdapter;
    private RecyclerView mRecyclerView;
    public SharedPreferences sharedProfilePic;
    public static final String PREFERRED_PIC = "profilePic";
    public static final String PROFILE_DIR = "Profiles";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aliases);
        hermesDbHelper = new HermesDbHelper(this);
        aliasAdapter = new AliasAdapter(this, new AliasAdapter.AliasAdapterOnClickHandler() {
            @Override
            public void onClick(String name) {
                setProfileImage(name);
                Toast.makeText(AliasesActivity.this, name, Toast.LENGTH_SHORT).show();
                sharedProfilePic.edit().putString(PREFERRED_PIC ,name).apply();
            }

            @Override
            public void onLongClick(final String name) {
                new AlertDialog.Builder(AliasesActivity.this)
                        .setTitle("Delete?")
                        .setMessage("Are you sure you want to delete the alias " + name + "?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                hermesDbHelper.deleteAlias(name);
                                new AliasLoader().execute(hermesDbHelper.getReadableDatabase());

                                File file = new File(AliasesActivity.this.getFilesDir(), name + ".png");
                                //noinspection ResultOfMethodCallIgnored
                                file.delete();
                                setProfileImage(sharedProfilePic.getString(PREFERRED_PIC, null));
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.alias_recycler);
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setAdapter(aliasAdapter);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        final AliasLoader loader = new AliasLoader();
        loader.execute(hermesDbHelper.getReadableDatabase());

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

                final View inflate = inflater.inflate(R.layout.dialog_add_alias, null);
                final EditText text = (EditText)inflate.findViewById(R.id.edit_alias_name);
                builder.setView(inflate)
                        .setTitle("Create New Alias")
                        .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                final KeyPair keyPair = Encryption.generateKeyPair();
                                String keyName = text.getText().toString();
                                if(hermesDbHelper.insertKey(keyPair, keyName)){
                                    QRCodeEncoder.saveQRCode(keyPair, AliasesActivity.this, keyName);
                                    File file = new File(AliasesActivity.this.getFilesDir(),keyName+".png");
                                    AliasLoader taskLoader  = new AliasLoader();
                                    taskLoader.execute(hermesDbHelper.getReadableDatabase());
                                }
                            }
                        }).setNegativeButton(android.R.string.cancel, null)
                        .show();
            }
        });

        sharedProfilePic =  getSharedPreferences(PROFILE_DIR, AliasesActivity.MODE_PRIVATE);

        String preferredProfile = sharedProfilePic.getString(PREFERRED_PIC, null);
        if(preferredProfile != null)
            setProfileImage(preferredProfile);
    }

    public class AliasLoader extends AsyncTask<SQLiteDatabase, Void, Cursor> {

        @Override
        protected Cursor doInBackground(SQLiteDatabase... db) {
            //get all messages for now
            Cursor cursor = db[0].query(HermesDbContract.KeyPairEntry.TABLE_NAME,
                    new String[]{HermesDbContract.KeyPairEntry.COLUMN_NAME,
                            HermesDbContract.KeyPairEntry.COLUMN_PRIVATE_KEY,
                            HermesDbContract.KeyPairEntry.COLUMN_PUBLIC_KEY},
                    null, null, null, null, null);
            return cursor;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            aliasAdapter.swapCursor(cursor);
            aliasAdapter.notifyDataSetChanged();
        }
    }

    private void setProfileImage(String name){
        File file = new File(AliasesActivity.this.getFilesDir(),name+".png");
        ImageView imageView = (ImageView) findViewById(R.id.alias_profile);
        if(file.exists()) {
            imageView.setImageURI(Uri.fromFile(file));
        } else {
            imageView.setImageResource(R.drawable.anonymous);
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
