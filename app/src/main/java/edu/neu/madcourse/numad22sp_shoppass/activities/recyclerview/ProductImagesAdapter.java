package edu.neu.madcourse.numad22sp_shoppass.activities.recyclerview;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.neu.madcourse.numad22sp_shoppass.R;

public class ProductImagesAdapter extends RecyclerView.Adapter<ProductImagesAdapter.ProductImagesViewHolder> {
    private List<Bitmap> productImgBitmapList;

    public ProductImagesAdapter(List<Bitmap> productImgBitmapList) {
        this.productImgBitmapList = productImgBitmapList;
    }


    @NonNull
    @Override
    public ProductImagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_product_images, parent, false);
        return new ProductImagesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductImagesViewHolder holder, int position) {
        Bitmap bitmap = productImgBitmapList.get(position);
        holder.iv_productImg.setImageBitmap(bitmap);

    }

    @Override
    public int getItemCount() {
        return productImgBitmapList.size();
    }

    public class ProductImagesViewHolder extends RecyclerView.ViewHolder {
        public ImageView iv_productImg;

        public ProductImagesViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_productImg = itemView.findViewById(R.id.img_prod_oneimg);
        }
    }
}
