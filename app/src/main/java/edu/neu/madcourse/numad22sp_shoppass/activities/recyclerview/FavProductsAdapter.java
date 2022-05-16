package edu.neu.madcourse.numad22sp_shoppass.activities.recyclerview;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.neu.madcourse.numad22sp_shoppass.Examples;
import edu.neu.madcourse.numad22sp_shoppass.R;
import edu.neu.madcourse.numad22sp_shoppass.components.Product;
import edu.neu.madcourse.numad22sp_shoppass.utils.BitmapConverter;

public class FavProductsAdapter extends RecyclerView.Adapter<FavProductsAdapter.FavProductsViewHolder> {
    private List<Product> productList;
    private MyClickListener likeClickListener;
    private MyClickListener openProdClickListener;

    public FavProductsAdapter(List<Product> productList) {
        this.productList = productList;
    }

    public void setClickListeners(MyClickListener likeClickListener, MyClickListener openProdClickListener) {
        this.likeClickListener = likeClickListener;
        this.openProdClickListener = openProdClickListener;
    }

    @NonNull
    @Override
    public FavProductsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_favproducts, parent, false);
        return new FavProductsViewHolder(view, likeClickListener, openProdClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull FavProductsViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.tv_productName.setText(product.name);
        holder.tv_productPrice.setText("$ " + product.price);
        if (product.bitmapList != null && product.bitmapList.size() > 0) {
            holder.iv_productImg.setImageBitmap(product.bitmapList.get(0));
        }
        if (product.isLiked) {
            holder.ib_like.setImageResource(R.drawable.icons_likeheart_30);
        } else {
            holder.ib_like.setImageResource(R.drawable.icons_unlikeheart_30);
        }

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public class FavProductsViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_productName, tv_productPrice;
        private ImageView iv_productImg;
        private ImageButton ib_like;

        public FavProductsViewHolder(@NonNull View itemView, MyClickListener likeClickListener, MyClickListener openProdClickListener) {
            super(itemView);
            tv_productName = itemView.findViewById(R.id.tv_fav_productName);
            tv_productPrice = itemView.findViewById(R.id.tv_fav_productPrice);
            iv_productImg = itemView.findViewById(R.id.iv_productImg);
            ib_like = itemView.findViewById(R.id.ib_likeProduct);
            ib_like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (likeClickListener != null) {
                        int position = getLayoutPosition();
                        likeClickListener.clickListener(position);
                    }
                }
            });
            tv_productName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (openProdClickListener != null) {
                        openProdClickListener.clickListener(getLayoutPosition());
                    }
                }
            });
        }
    }
}
