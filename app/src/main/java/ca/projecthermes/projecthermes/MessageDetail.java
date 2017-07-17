package ca.projecthermes.projecthermes;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

/**
 * Created by abc on 2017-07-14.
 */

public class MessageDetail extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        TextView msgView = (TextView) findViewById(R.id.msgIdView);
        msgView.setText(getIntent().getStringExtra("msgId"));

        TextView alias = (TextView) findViewById(R.id.msgAliasView);
        alias.setText(getIntent().getStringExtra("alias"));

        TextView msgBodyView = (TextView) findViewById(R.id.msgBodyView);
        msgBodyView.setText(getIntent().getStringExtra("msg"));

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
