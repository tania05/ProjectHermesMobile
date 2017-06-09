package ca.projecthermes.projecthermes;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link InboxContentFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class InboxContentFragment extends Fragment {


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inbox_content, container, false);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView avator;
        public TextView name;
        public TextView description;
        public ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.inbox_list, parent, false));
        }
    }
//    /**
//     * Adapter to display recycler view.
//     */
//    public static class ContentAdapter extends RecyclerView.Adapter<ViewHolder> {
//        // Set numbers of List in RecyclerView.
//        private static final int LENGTH = 18;
////        private final String[] mPlaces;
////        private final String[] mPlaceDesc;
////        private final Drawable[] mPlaceAvators;
//        public ContentAdapter(Context context) {
////            Resources resources = context.getResources();
////            mPlaces = resources.getStringArray(R.array.places);
////            mPlaceDesc = resources.getStringArray(R.array.place_desc);
////            TypedArray a = resources.obtainTypedArray(R.array.place_avator);
////            mPlaceAvators = new Drawable[a.length()];
////            for (int i = 0; i < mPlaceAvators.length; i++) {
////                mPlaceAvators[i] = a.getDrawable(i);
////            }
////            a.recycle();
//        }
//
//        @Override
//        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
//        }
//
//        @Override
//        public void onBindViewHolder(ViewHolder holder, int position) {
////            holder.avator.setImageDrawable(mPlaceAvators[position % mPlaceAvators.length]);
////            holder.name.setText(mPlaces[position % mPlaces.length]);
////            holder.description.setText(mPlaceDesc[position % mPlaceDesc.length]);
//        }
//
//        @Override
//        public int getItemCount() {
//            return LENGTH;
//        }
//    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}
