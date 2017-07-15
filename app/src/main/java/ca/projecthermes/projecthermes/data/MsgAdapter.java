package ca.projecthermes.projecthermes.data;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Arrays;

import ca.projecthermes.projecthermes.MessageDetail;
import ca.projecthermes.projecthermes.R;
import ca.projecthermes.projecthermes.networking.payload.Message;
import ca.projecthermes.projecthermes.util.Encryption;

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
        byte[] getLastStoredPrivateKey();
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
        Log.d(TAG, "pos: " + position);

        //TODO: Use all stored private key instead of only last one
        byte[] verifier = mCursor.getBlob(1);
        final String verifierString = Encryption.decryptString(verifier, mClickHandler.getLastStoredPrivateKey());
        Log.d(TAG, "verifier String: " + verifierString);
        if (Arrays.equals(verifierString.getBytes(HermesDbHelper.CHARSET),(Message.VALID_VERIFIER))) {

            byte[] msgBlob = mCursor.getBlob(2);
            final String msg = Encryption.decryptString(msgBlob, mClickHandler.getLastStoredPrivateKey());
            msgAdapterViewHolder.mMsgTextView.setText(msg);

            msgAdapterViewHolder.mMsgTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, MessageDetail.class);
                    String msgId = new String(mCursor.getBlob(0), HermesDbHelper.CHARSET);
                    intent.putExtra("msgId", msgId);
                    intent.putExtra("verifier", verifierString);
                    intent.putExtra("msg", msg);
                    mContext.startActivity(intent);
                }
            });
        } else {
           msgAdapterViewHolder.mMsgTextView.setVisibility(View.GONE);
        }
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
