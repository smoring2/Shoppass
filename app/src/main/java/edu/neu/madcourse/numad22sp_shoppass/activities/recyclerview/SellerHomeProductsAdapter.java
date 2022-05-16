package edu.neu.madcourse.numad22sp_shoppass.activities.recyclerview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.neu.madcourse.numad22sp_shoppass.Examples;
import edu.neu.madcourse.numad22sp_shoppass.R;
import edu.neu.madcourse.numad22sp_shoppass.components.Product;

public class SellerHomeProductsAdapter extends RecyclerView.Adapter<SellerHomeProductsAdapter.StoreProductsViewHolder> {
    private List<Product> productList;
    private MyClickListener openProdClickListener, longClickListener;

    public SellerHomeProductsAdapter(List<Product> productList) {
        this.productList = productList;
    }

    public void setClickListeners(MyClickListener openProdClickListener, MyClickListener longClickListener) {
        this.openProdClickListener = openProdClickListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public StoreProductsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_storehome_products, parent, false);
        return new StoreProductsViewHolder(view, openProdClickListener, longClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull StoreProductsViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.tv_productName.setText(product.name);
        holder.tv_productPrice.setText("$ " + product.price);
        if (product.bitmapList != null && product.bitmapList.size() > 0) {
            holder.iv_productImg.setImageBitmap(product.bitmapList.get(0));
        }
        if (position == 0) {
            holder.iv_productImg.setImageResource(Examples.ADD_PROMOTION_IMG);
            holder.tv_productName.setText("");
            holder.tv_productPrice.setText("");
        }

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public class StoreProductsViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_productName, tv_productPrice;
        private ImageView iv_productImg;

        public StoreProductsViewHolder(@NonNull View itemView, MyClickListener openProdClickListener, MyClickListener longClickListener) {
            super(itemView);
            tv_productName = itemView.findViewById(R.id.tv_fav_productName);
            tv_productPrice = itemView.findViewById(R.id.tv_fav_productPrice);
            iv_productImg = itemView.findViewById(R.id.iv_productImg);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (openProdClickListener != null) {
                        int position = getLayoutPosition();
                        openProdClickListener.clickListener(position);
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (longClickListener != null) {
                        int position = getLayoutPosition();
                        if (position != 0) {
                            longClickListener.clickListener(position);
                        }
                    }
                    return true;
                }
                });
            }
        }
    }
