package edu.neu.madcourse.numad22sp_shoppass.activities.recyclerview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.neu.madcourse.numad22sp_shoppass.R;
import edu.neu.madcourse.numad22sp_shoppass.components.Store;

public class FavStoresAdapter extends RecyclerView.Adapter<FavStoresAdapter.FavStoresViewHolder> {
    private List<Store> storeList;
    private MyClickListener clickListener;

    public FavStoresAdapter(List<Store> storeList) {
        this.storeList = storeList;
    }
    public void setClickListener(MyClickListener myClickListener) {
         this.clickListener = myClickListener;
    }

    @NonNull
    @Override
    public FavStoresViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_favstores, parent, false);
        return new FavStoresViewHolder(view, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull FavStoresViewHolder holder, int position) {
        Store store = storeList.get(position);
        store.isLiked = true;
        holder.tv_shopName.setText(store.name);
        holder.tv_shopAdd.setText("Address: " + store.address);
    }

    @Override
    public int getItemCount() {
        return storeList.size();
    }

    public class FavStoresViewHolder extends RecyclerView.ViewHolder{
        private TextView tv_shopName, tv_shopAdd;
        private Button btn_favVisit;
        public FavStoresViewHolder(@NonNull View itemView, MyClickListener myClickListener) {
            super(itemView);
            tv_shopName = itemView.findViewById(R.id.tv_fav_storename);
            tv_shopAdd = itemView.findViewById(R.id.tv_fav_storeadd);
            btn_favVisit = itemView.findViewById(R.id.btn_fav_visit);
            btn_favVisit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (myClickListener != null) {
                        int position = getLayoutPosition();
                        myClickListener.clickListener(position);
                    }
                }
            });
        }
    }
}
