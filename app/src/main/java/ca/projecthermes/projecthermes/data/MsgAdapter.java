package ca.projecthermes.projecthermes.data;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import ca.projecthermes.projecthermes.R;

/**
 * Created by abc on 2017-07-12.
 */

public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.MsgAdapterViewHolder> {
    private final Context mContext;
    private final MsgAdapterOnClickHandler mClickHandler;
    private Cursor mCursor;
    private final String TAG = this.getClass().getSimpleName();

    public interface MsgAdapterOnClickHandler {
        void onClick(long msgId);
    }

    public MsgAdapter(@NonNull Context context, MsgAdapterOnClickHandler clickHandler) {
        mContext = context;
        mClickHandler = clickHandler;
        Log.d(TAG, "in msg adapter");
    }

    @Override
    public MsgAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);

        View view = inflater.inflate(R.layout.list_msg_item, parent, false);

        return new MsgAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MsgAdapterViewHolder msgAdapterViewHolder, int position) {
        mCursor.moveToPosition(position);
        final int pos = position;

        String msg = mCursor.getString(1);
        Log.d(TAG, "pos: " + position);
        msgAdapterViewHolder.mMsgTextView.setText(msg);

        msgAdapterViewHolder.mMsgTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext, "Recycle Click" + pos, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        Log.d(TAG, "getItemCount: " + mCursor.getCount());
        return mCursor.getCount();
    }

    public class MsgAdapterViewHolder extends RecyclerView.ViewHolder {
        public final TextView mMsgTextView;

        public MsgAdapterViewHolder(View v){
            super(v);
            mMsgTextView = (TextView) v.findViewById(R.id.msg_item_body);
        }
    }

    public void swapCursor(Cursor c) {
        mCursor = c;
    }

}
