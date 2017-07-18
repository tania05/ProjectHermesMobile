package ca.projecthermes.projecthermes.data;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ca.projecthermes.projecthermes.R;

/**
 * Created by Tanjulia on 7/16/2017.
 */

public class AliasAdapter extends RecyclerView.Adapter<AliasAdapter.AliasAdapterViewHolder> {
    private final Context mContext;
    private final AliasAdapter.AliasAdapterOnClickHandler mClickHandler;
    private Cursor mCursor;

    public interface  AliasAdapterOnClickHandler{
        void onClick(String name);
        void onLongClick(String name);
    }

    public AliasAdapter(@NonNull Context context, AliasAdapterOnClickHandler clickHandler){
        mContext = context;
        mClickHandler = clickHandler;

    }

    @Override
    public AliasAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);

        View view = inflater.inflate(R.layout.list_alias_item, parent, false);

        return new AliasAdapter.AliasAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AliasAdapter.AliasAdapterViewHolder holder, int position) {
        final int pos = position;
        mCursor.moveToPosition(position);
        final String name = mCursor.getString(0);
        holder.mAliasTextView.setText(name);

        holder.mAliasTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickHandler.onClick(name);
            }
        });

        holder.mAliasTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mClickHandler.onLongClick(name);
                return true;
            }
        });

        DisplayMetrics metrics = holder.mCardView.getResources().getDisplayMetrics();
        int margin_bottom = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, metrics);
        int margin_top = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, metrics);

        if (position == getItemCount() - 1) {
            ViewGroup.MarginLayoutParams layoutParams =
                    (ViewGroup.MarginLayoutParams) holder.mCardView.getLayoutParams();
            layoutParams.setMargins(0, margin_top, 0, margin_bottom);
            holder.mCardView.requestLayout();
        } else {
            ViewGroup.MarginLayoutParams layoutParams =
                    (ViewGroup.MarginLayoutParams) holder.mCardView.getLayoutParams();
            layoutParams.setMargins(0, margin_top, 0, 0);
            holder.mCardView.requestLayout();
        }
    }

    @Override
    public int getItemCount() {
        if(mCursor == null)
            return 0;
        return mCursor.getCount();
    }

    public class AliasAdapterViewHolder extends RecyclerView.ViewHolder {
        public final TextView mAliasTextView;
        public View mCardView;

        public AliasAdapterViewHolder(View v){
            super(v);
            mCardView = v.findViewById(R.id.cardView);
            mAliasTextView = (TextView) v.findViewById(R.id.alias_item_body);
        }
    }
    public void swapCursor(Cursor c) {
        mCursor = c;
    }

}
