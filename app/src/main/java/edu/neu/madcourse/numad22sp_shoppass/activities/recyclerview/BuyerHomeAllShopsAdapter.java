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

public class BuyerHomeAllShopsAdapter extends RecyclerView.Adapter<BuyerHomeAllShopsAdapter.SellerHomeAllShopsViewHolder> {
    private List<Store> storeList;
    private MyClickListener clickListener;

    public BuyerHomeAllShopsAdapter(List<Store> storeList) {
        this.storeList = storeList;
    }
    public void setClickListener(MyClickListener myClickListener) {
         this.clickListener = myClickListener;
    }

    @NonNull
    @Override
    public SellerHomeAllShopsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_buyerhome_allshops, parent, false);
        return new SellerHomeAllShopsViewHolder(view, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SellerHomeAllShopsViewHolder holder, int position) {
        Store store = storeList.get(position);
        holder.tv_shopName.setText(store.name);
        holder.tv_shopAdd.setText(store.address);
    }

    @Override
    public int getItemCount() {
        return storeList.size();
    }

    public class SellerHomeAllShopsViewHolder extends RecyclerView.ViewHolder{
        private TextView tv_shopName, tv_shopAdd;
        private Button btn_visit;
        public SellerHomeAllShopsViewHolder(@NonNull View itemView, MyClickListener myClickListener) {
            super(itemView);
            tv_shopName = itemView.findViewById(R.id.tv_shopName);
            tv_shopAdd = itemView.findViewById(R.id.tv_shopAdd);
            btn_visit = itemView.findViewById(R.id.btn_visit);
            btn_visit.setOnClickListener(new View.OnClickListener() {
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
