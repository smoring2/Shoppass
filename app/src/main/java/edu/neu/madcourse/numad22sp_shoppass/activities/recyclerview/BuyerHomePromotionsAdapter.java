package edu.neu.madcourse.numad22sp_shoppass.activities.recyclerview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.neu.madcourse.numad22sp_shoppass.R;
import edu.neu.madcourse.numad22sp_shoppass.components.Promotion;

public class BuyerHomePromotionsAdapter extends RecyclerView.Adapter<BuyerHomePromotionsAdapter.SellerHomeFavStoresViewHolder> {
    private List<Promotion> promotionList;
    private MyClickListener clickListener;

    public BuyerHomePromotionsAdapter(List<Promotion> promotionList) {
        this.promotionList = promotionList;
    }
    public void setClickListener(MyClickListener myClickListener) {
         this.clickListener = myClickListener;
    }

    @NonNull
    @Override
    public SellerHomeFavStoresViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_buyerhome_promotions, parent, false);
        return new SellerHomeFavStoresViewHolder(view, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SellerHomeFavStoresViewHolder holder, int position) {
        Promotion promotion = promotionList.get(position);
        holder.tv_promotion.setText(promotion.title);
    }

    @Override
    public int getItemCount() {
        return promotionList.size();
    }

    public class SellerHomeFavStoresViewHolder extends RecyclerView.ViewHolder{
        public TextView tv_promotion;
        public SellerHomeFavStoresViewHolder(@NonNull View itemView, MyClickListener myClickListener) {
            super(itemView);
            tv_promotion = itemView.findViewById(R.id.tv_buyerHomePromotion);
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
