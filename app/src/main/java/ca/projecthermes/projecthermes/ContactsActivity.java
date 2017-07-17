package ca.projecthermes.projecthermes;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import ca.projecthermes.projecthermes.data.ContactsAdapter;
import ca.projecthermes.projecthermes.data.HermesDbContract;
import ca.projecthermes.projecthermes.data.HermesDbHelper;

public class ContactsActivity extends AppCompatActivity {
    private HermesDbHelper hermesDbHelper;
    private ContactsAdapter contactsAdapter;
    private RecyclerView mRecyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        hermesDbHelper  = new HermesDbHelper(this);
        contactsAdapter = new ContactsAdapter(this, new ContactsAdapter.ContactsAdapterOnClickHandler(){

            @Override
            public void onclick(String contactName) {

                Toast.makeText(ContactsActivity.this, contactName, Toast.LENGTH_SHORT).show();
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.contacts_recycler);
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setAdapter(contactsAdapter);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        final ContactsLoader loader = new ContactsLoader();
        loader.execute(hermesDbHelper.getReadableDatabase());
    }

    public class ContactsLoader extends AsyncTask<SQLiteDatabase, Void, Cursor> {

        @Override
        protected Cursor doInBackground(SQLiteDatabase... db) {
            //get all messages for now
            Cursor cursor = db[0].query(HermesDbContract.ContactKeysEntry.TABLE_NAME,
                    new String[]{HermesDbContract.ContactKeysEntry.COLUMN_CONTACT_NAME,
                            HermesDbContract.ContactKeysEntry.COLUMN_CONTACT_PUBLIC_KEY},
                    null, null, null, null, null, null);
            return cursor;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            contactsAdapter.swapCursor(cursor);
            contactsAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
