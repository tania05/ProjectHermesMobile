package ca.projecthermes.projecthermes;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.security.Key;
import java.security.KeyPair;

import ca.projecthermes.projecthermes.data.HermesDbContract;
import ca.projecthermes.projecthermes.data.HermesDbHelper;
import ca.projecthermes.projecthermes.data.MsgAdapter;
import ca.projecthermes.projecthermes.services.WiFiPeerDiscoverService;
import ca.projecthermes.projecthermes.util.Encryption;


public class MainActivity extends AppCompatActivity implements MsgAdapter.MsgAdapterOnClickHandler {

    private RecyclerView mRecyclerView;
    private MsgAdapter mMsgAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private final String TAG = this.getClass().getSimpleName();
    private DrawerLayout mDrawerLayout;
    public HermesDbHelper hermesDbHelper;
    private NavigationView navigationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mRecyclerView = (RecyclerView) findViewById(R.id.msg_recycler);
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mMsgAdapter = new MsgAdapter(this, this);
        mRecyclerView.setAdapter(mMsgAdapter);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                linearLayoutManager.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            VectorDrawableCompat indicator
                    = VectorDrawableCompat.create(getResources(), R.drawable.ic_menu_black_24dp, getTheme());
            indicator.setTint(ResourcesCompat.getColor(getResources(),R.color.white,getTheme()));
            supportActionBar.setHomeAsUpIndicator(indicator);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }else{
            Log.e("Toolbar", "Toolbar not found");
        }

        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(getApplicationContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 15000, pendingIntent);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SendMessageActivity.class);
                startActivity(intent);
            }
        });

        hermesDbHelper = new HermesDbHelper(this);
        final SQLiteDatabase db = hermesDbHelper.getReadableDatabase();
        new MsgLoader().execute(db);


        mSwipeRefreshLayout =
                (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i(TAG, "onRefresh called from SwipeRefreshLayout");
                        Intent wifiIntent = new Intent(getApplicationContext(),
                                                        WiFiPeerDiscoverService.class);
                        startService(wifiIntent);

                        new MsgLoader().execute(db);
                    }

                }
        );
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener()
                {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch(item.getItemId()){
                            case R.id.inbox:
                                return true;
                            case R.id.outbox:
                                return true;
                            case R.id.scan_qr:
                                QRCodeEncoder.scanQRCode(MainActivity.this);
                                return true;
                            case R.id.settings:
                                return true;
                            case R.id.contacts:
                                Intent contactsIntent = new Intent(MainActivity.this, ContactsActivity.class);
                                startActivity(contactsIntent);
                                return true;
                            case R.id.aliases:
                                Intent intent = new Intent(MainActivity.this, AliasesActivity.class);
                                startActivity(intent);
                                return true;
                        }
                        return true;
                    }
                }

        );
    }

    @Override
    public void onActivityResult(int requestCode, final int resultCode, final Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        final IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (result != null && result.getContents() != null ) {
            // We have the QR code information.
//            _recipient.setText(result.getContents());
            new AlertDialog.Builder(this)
                    .setTitle("ADD TO CONTACT")
                    .setMessage("Do you want to add to the scanned QR code to contact?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            AlertDialog.Builder contactNameBuilder = new AlertDialog.Builder(MainActivity.this);
                            LayoutInflater inflater = MainActivity.this.getLayoutInflater();

                            final View inflate = inflater.inflate(R.layout.dialog_add_alias, null);
                            final EditText text = (EditText) inflate.findViewById(R.id.edit_alias_name);

                            contactNameBuilder.setView(inflate)
                                    .setTitle("Contact Name")
                                    .setPositiveButton("Save", new DialogInterface.OnClickListener(){

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String contactName = text.getText().toString();
                                            if(hermesDbHelper.insertContact(Base64.decode(result.getContents(), Base64.DEFAULT), contactName))
                                                Toast.makeText(MainActivity.this, contactName+ " has been added to the contact.", Toast.LENGTH_LONG).show();
                                        }
                                    }).setNegativeButton(android.R.string.cancel, null).show();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null).show();
        }
    }

    public class MsgLoader extends AsyncTask<SQLiteDatabase, Void, Cursor> {

        @Override
        protected Cursor doInBackground(SQLiteDatabase... db) {
            //get all messages for now
            Cursor cursor = db[0].query(HermesDbContract.DecodedEntry.TABLE_NAME,
                    new String[]{HermesDbContract.DecodedEntry.COLUMN_MSG_ID,
                            HermesDbContract.DecodedEntry.COLUMN_DECODING_ALIAS,
                            HermesDbContract.DecodedEntry.COLUMN_MSG_BODY},
                    null, null, null, null, null);
            return cursor;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            mSwipeRefreshLayout.setRefreshing(false);
            mMsgAdapter.swapCursor(cursor);
            mMsgAdapter.notifyDataSetChanged();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        final SQLiteDatabase db = hermesDbHelper.getReadableDatabase();
        new MsgLoader().execute(db);
        SharedPreferences sharedPreferences = getSharedPreferences(AliasesActivity.PROFILE_DIR, AliasesActivity.MODE_PRIVATE);
        String preferred_pic = sharedPreferences.getString(AliasesActivity.PREFERRED_PIC,null);

        if(preferred_pic != null){
            setProfileImage(preferred_pic);
            Log.e("TEIUREOI", "ERERE");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClick(long msgId) {

    }

    private void setProfileImage(String name){
        File file = new File(MainActivity.this.getFilesDir(),name+".png");
        if(file.exists()) {
            ImageView imageView = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.nav_profile);
            imageView.setImageURI(Uri.fromFile(file));
        }
    }
    public byte[] getLastStoredPrivateKey() {
        return hermesDbHelper.getLastStoredPrivateKey();
    }

}
