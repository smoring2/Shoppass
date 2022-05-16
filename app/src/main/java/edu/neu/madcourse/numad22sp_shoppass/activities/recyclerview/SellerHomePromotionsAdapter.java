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

public class SellerHomePromotionsAdapter extends RecyclerView.Adapter<SellerHomePromotionsAdapter.SellerHomePromotionsViewHolder> {
    private List<Promotion> promotionList;
    private MyClickListener clickListener, longClikcListener;

    public SellerHomePromotionsAdapter(List<Promotion> promotionList) {
        this.promotionList = promotionList;
    }
    public void setClickListener(MyClickListener clickListener, MyClickListener longClickListener) {
         this.clickListener = clickListener;
         this.longClikcListener = longClickListener;
    }

    @NonNull
    @Override
    public SellerHomePromotionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_buyerhome_promotions, parent, false);
        return new SellerHomePromotionsViewHolder(view, clickListener, longClikcListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SellerHomePromotionsViewHolder holder, int position) {
        Promotion promotion = promotionList.get(position);
        holder.tv_promotion.setText(promotion.title);
    }

    @Override
    public int getItemCount() {
        return promotionList.size();
    }

    public class SellerHomePromotionsViewHolder extends RecyclerView.ViewHolder{
        public TextView tv_promotion;
        public SellerHomePromotionsViewHolder(@NonNull View itemView, MyClickListener clickListener, MyClickListener longClickListener) {
            super(itemView);
            tv_promotion = itemView.findViewById(R.id.tv_buyerHomePromotion);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (clickListener != null) {
                        int position = getLayoutPosition();
                        clickListener.clickListener(position);
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (longClickListener != null) {
                        int position = getLayoutPosition();
                        longClickListener.clickListener(position);
                    }
                    return true;
                }
            });
        }
    }
}
