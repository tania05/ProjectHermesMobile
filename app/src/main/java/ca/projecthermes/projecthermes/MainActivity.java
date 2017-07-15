package ca.projecthermes.projecthermes;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavigationView nagivationView = (NavigationView) findViewById(R.id.nav_view);
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

        nagivationView.setNavigationItemSelectedListener(
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
                                return true;
                            case R.id.aliases:
                                final KeyPair keyPair = Encryption.generateKeyPair();
                                hermesDbHelper.insertKey(keyPair);
                                SendMessageActivity.saveQRCode(keyPair, MainActivity.this);
                                File file = new File(MainActivity.this.getFilesDir(),"QR_Code.png");
                                if(file.exists()){
                                    ImageView imageView = (ImageView) findViewById(R.id.imageView);
                                    imageView.setImageURI(Uri.fromFile(file));
                                }
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
                            Toast.makeText(MainActivity.this, result.getContents(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null).show();
        }
    }


    public class MsgLoader extends AsyncTask<SQLiteDatabase, Void, Cursor> {

        @Override
        protected Cursor doInBackground(SQLiteDatabase... db) {
            //get all messages for now
            Cursor cursor = db[0].query(HermesDbContract.MessageEntry.TABLE_NAME,
                    new String[]{HermesDbContract.MessageEntry.COLUMN_MSG_ID,
                            HermesDbContract.MessageEntry.COLUMN_MSG_VERIFIER,
                            HermesDbContract.MessageEntry.COLUMN_MSG_BODY},
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClick(long msgId) {

    }

    public byte[] getLastStoredPrivateKey() {
        return hermesDbHelper.getLastStoredPrivateKey();
    }

}
