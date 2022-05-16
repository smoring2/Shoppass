package edu.neu.madcourse.numad22sp_shoppass.activities.recyclerview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.neu.madcourse.numad22sp_shoppass.R;
import edu.neu.madcourse.numad22sp_shoppass.components.Store;

public class BuyerHomeFavStoresAdapter extends RecyclerView.Adapter<BuyerHomeFavStoresAdapter.SellerHomeFavStoresViewHolder> {
    private List<Store> storeList;
    private MyClickListener clickListener;

    public BuyerHomeFavStoresAdapter(List<Store> storeList) {
        this.storeList = storeList;
    }
    public void setClickListener(MyClickListener myClickListener) {
         this.clickListener = myClickListener;
    }

    @NonNull
    @Override
    public SellerHomeFavStoresViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_buyername_favstores, parent, false);
        return new SellerHomeFavStoresViewHolder(view, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SellerHomeFavStoresViewHolder holder, int position) {
        Store store = storeList.get(position);
        holder.tv_favStoreName.setText(store.name);
    }

    @Override
    public int getItemCount() {
        return storeList.size();
    }

    public class SellerHomeFavStoresViewHolder extends RecyclerView.ViewHolder{
        public TextView tv_favStoreName;
        public SellerHomeFavStoresViewHolder(@NonNull View itemView, MyClickListener myClickListener) {
            super(itemView);
            tv_favStoreName = itemView.findViewById(R.id.ib_favStoreName);
            itemView.setOnClickListener(new View.OnClickListener() {
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
