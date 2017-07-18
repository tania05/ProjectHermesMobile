package ca.projecthermes.projecthermes.data;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ca.projecthermes.projecthermes.R;

/**
 * Created by Tanjulia on 7/17/2017.
 */

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder> {
    private final Context mContext;
    private final ContactsAdapterOnClickHandler mClickHandler;
    private Cursor mCursor;

    public interface ContactsAdapterOnClickHandler{
        void onclick(String contactName);
        void onLongClick(String contactName);
    }
    public ContactsAdapter(Context context, ContactsAdapterOnClickHandler viewHandler){
        mContext = context;
        mClickHandler = viewHandler;
    }

    @Override
    public ContactsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);

        View view = inflater.inflate(R.layout.list_contact_items, parent, false);
        return new ContactsAdapter.ContactsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ContactsViewHolder holder, int position) {
        final int pos = position;
        mCursor.moveToPosition(position);
        final String name = mCursor.getString(0);
        holder.mContactsTextView.setText(name);

        holder.mContactsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickHandler.onclick(name);
            }
        });

        holder.mContactsTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mClickHandler.onLongClick(name);
                return true;
            }
        });

        DisplayMetrics metrics = holder.mCardView.getResources().getDisplayMetrics();
        int margin_bottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, metrics);
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

    public class ContactsViewHolder extends RecyclerView.ViewHolder {
        public final TextView mContactsTextView;
        public View mCardView;

        public ContactsViewHolder(View v){
            super(v);
            mCardView = v.findViewById(R.id.contactCardView);
            mContactsTextView = (TextView) v.findViewById(R.id.contacts_item_body);
        }
    }

    public void swapCursor(Cursor c) {
        mCursor = c;
    }

}
