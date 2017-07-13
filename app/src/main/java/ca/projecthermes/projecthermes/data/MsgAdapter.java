package ca.projecthermes.projecthermes.data;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ca.projecthermes.projecthermes.R;

/**
 * Created by abc on 2017-07-12.
 */

public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.MsgAdapterViewHolder> {
    private final Context mContext;
    private final MsgAdapterOnClickHandler mClickHandler;
    private Cursor mCursor;

    public interface MsgAdapterOnClickHandler {
        void onClick(long msgId);
    }

    public MsgAdapter(@NonNull Context context, MsgAdapterOnClickHandler clickHandler) {
        mContext = context;
        mClickHandler = clickHandler;
    }

    @Override
    public MsgAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);

        View view = inflater.inflate(R.layout.list_msg_item, parent, false);
        view.setFocusable(true);
        return new MsgAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MsgAdapterViewHolder msgAdapterViewHolder, int position) {
//        mCursor.moveToPosition(position);

//        String msg = mCursor.getString(1);
        msgAdapterViewHolder.mMsgTextView.setText("1234");
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 30; //TODO: FIX
        return mCursor.getCount();
    }

    public class MsgAdapterViewHolder extends RecyclerView.ViewHolder {
        public final TextView mMsgTextView;

        public MsgAdapterViewHolder(View v){
            super(v);
            mMsgTextView = (TextView) v.findViewById(R.id.msg_item_body);
        }
    }

    //TODO: Show message details
    public void onClick(View v) {

    }

}
