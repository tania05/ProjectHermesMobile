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
        final byte[] verifierBytes = Encryption.decryptString(verifier, mClickHandler.getLastStoredPrivateKey());
        final String verifierString = (verifierBytes == null) ? "" : new String(verifierBytes, HermesDbHelper.CHARSET);
        Log.d(TAG, "verifier String: " + verifierString);
        if (Arrays.equals(verifierBytes,(Message.VALID_VERIFIER))) {

            byte[] encryptedKeyBlob = mCursor.getBlob(2);
            byte[] encryptedMessageBlob = mCursor.getBlob(3);
            byte[] keyBlob = Encryption.decryptString(encryptedKeyBlob, mClickHandler.getLastStoredPrivateKey());
            byte[] msgBlob = Encryption.decryptUnderAes(keyBlob, encryptedMessageBlob);

            final String msg = new String(msgBlob, HermesDbHelper.CHARSET);
            msgAdapterViewHolder.mMsgTextView.setText(msg);

            msgAdapterViewHolder.mMsgTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, MessageDetail.class);
                    String msgId = mCursor.getString(0);
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
