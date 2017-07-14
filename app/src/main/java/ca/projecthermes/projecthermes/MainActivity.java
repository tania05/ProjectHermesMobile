package ca.projecthermes.projecthermes;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import ca.projecthermes.projecthermes.data.HermesDbContract;
import ca.projecthermes.projecthermes.data.HermesDbHelper;
import ca.projecthermes.projecthermes.data.MsgAdapter;
import ca.projecthermes.projecthermes.services.WiFiPeerDiscoverService;

/**
 * Created by abc on 2017-07-12.
 */

public class MainActivity extends AppCompatActivity implements MsgAdapter.MsgAdapterOnClickHandler {

    private RecyclerView mRecyclerView;
    private MsgAdapter mMsgAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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


        final HermesDbHelper hermesDbHelper = new HermesDbHelper(this);
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
    }

    public class MsgLoader extends AsyncTask<SQLiteDatabase, Void, Cursor> {

        @Override
        protected Cursor doInBackground(SQLiteDatabase... db) { //TODO: implement other methods later
            //get all messages for now
            Cursor cursor = db[0].query(HermesDbContract.MessageEntry.TABLE_NAME,
                    new String[]{HermesDbContract.MessageEntry.COLUMN_MSG_BODY,
                            HermesDbContract.MessageEntry.COLUMN_MSG_ID},
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

    @Override
    public void onClick(long msgId) {

    }

}
