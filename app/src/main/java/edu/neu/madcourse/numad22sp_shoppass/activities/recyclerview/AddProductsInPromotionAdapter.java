package edu.neu.madcourse.numad22sp_shoppass.activities.recyclerview;

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

public class AddProductsInPromotionAdapter extends RecyclerView.Adapter<AddProductsInPromotionAdapter.FavProductsViewHolder> {
    private List<Product> productList;
    private MyClickListener addClickListener;
    private MyClickListener openProdClickListener;

    public AddProductsInPromotionAdapter(List<Product> productList) {
        this.productList = productList;
    }

    public void setClickListeners(MyClickListener addClickListener, MyClickListener openProdClickListener) {
        this.addClickListener = addClickListener;
        this.openProdClickListener = openProdClickListener;
    }

    @NonNull
    @Override
    public FavProductsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_add_products_inpromotion, parent, false);
        return new FavProductsViewHolder(view, addClickListener, openProdClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull FavProductsViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.tv_productName.setText(product.name);
        holder.tv_productPrice.setText("$ " + product.price);
        if (product.bitmapList != null && product.bitmapList.size() > 0) {
            holder.iv_productImg.setImageBitmap(product.bitmapList.get(0));
        }

        if (product.isChosenForPromotion) {
            holder.ib_add.setImageResource(R.drawable.icons_chosen_square_24);
        } else {
            holder.ib_add.setImageResource(R.drawable.icons_notchosen_square_24);
        }

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public class FavProductsViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_productName, tv_productPrice;
        private ImageView iv_productImg;
        private ImageButton ib_add;

        public FavProductsViewHolder(@NonNull View itemView, MyClickListener addClickListener, MyClickListener openProdClickListener) {
            super(itemView);
            tv_productName = itemView.findViewById(R.id.tv_fav_productName);
            tv_productPrice = itemView.findViewById(R.id.tv_fav_productPrice);
            iv_productImg = itemView.findViewById(R.id.iv_productImg);
            ib_add = itemView.findViewById(R.id.ib_chosenProduct);
            ib_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (addClickListener != null) {
                        int position = getLayoutPosition();
                        addClickListener.clickListener(position);
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
