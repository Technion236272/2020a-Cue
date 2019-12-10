//package com.technion.cue;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageButton;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
//import com.firebase.ui.firestore.FirestoreRecyclerOptions;
//
//public class MyFavoriteListAdapter extends FirestoreRecyclerAdapter<BFavoriteListItem,MyFavoriteListAdapter.itemHolder > {
//
//
//    public MyFavoriteListAdapter(@NonNull FirestoreRecyclerOptions<BFavoriteListItem> options) {
//        super(options);
//    }
//
//    @Override
//    protected void onBindViewHolder(@NonNull itemHolder holder, int position, @NonNull BFavoriteListItem model) {
////        holder.num_item.setText(model.getNum_item());
//        holder.businessName.setText(model.getBo_name());
//        if (model.getLogo_path().toString().length() > 0) {
//            holder.logo.setImageResource(model.getLogo_path());
//        }
//    }
//
//    @NonNull
//    @Override
//    public itemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.my_favorite_list_adapter,parent,false);
//        return new itemHolder(v);
//    }
//
//    class itemHolder extends RecyclerView.ViewHolder {
//        TextView businessName;
//        ImageButton logo;
//
//
//        public itemHolder(@NonNull View itemView) {
//            super(itemView);
//            businessName = itemView.findViewById(R.id.businessName);
//            logo = itemView.findViewById(R.id.businessLogo);
//
//        }
//    }
//}
