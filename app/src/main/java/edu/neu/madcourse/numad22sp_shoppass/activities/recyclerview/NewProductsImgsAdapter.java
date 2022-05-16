package edu.neu.madcourse.numad22sp_shoppass.activities.recyclerview;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import edu.neu.madcourse.numad22sp_shoppass.R;

public class NewProductsImgsAdapter extends RecyclerView.Adapter<NewProductsImgsAdapter.NewProductsImgsViewHolder> {
    private List<String> imgList;
    private MyClickListener deleteClickListener;
    private Context context;

    public NewProductsImgsAdapter(List<String> imgList) {
        this.imgList = imgList;
    }

    public void setDeleteClickListener(MyClickListener deleteClickListener) {
        this.deleteClickListener = deleteClickListener;
    }


    @NonNull
    @Override
    public NewProductsImgsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_new_products_imgs, parent, false);
        context = parent.getContext();
        return new NewProductsImgsViewHolder(view, deleteClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull NewProductsImgsViewHolder holder, int position) {
        String imgUri = imgList.get(position);
        ImageView img = holder.iv_productImg;
        if (imgUri != null) {
            Glide
                    .with(context)
                    .load(imgUri)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(img);
        }
    }

    @Override
    public int getItemCount() {
        return imgList.size();
    }

    public class NewProductsImgsViewHolder extends RecyclerView.ViewHolder {
        private ImageView iv_productImg;
        private ImageButton ib_delImg;

        public NewProductsImgsViewHolder(@NonNull View itemView, MyClickListener deleteClickListener) {
            super(itemView);
            iv_productImg = itemView.findViewById(R.id.iv_newProductImg);
            ib_delImg = itemView.findViewById(R.id.ib_deleteNewProductImg);
            ib_delImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (deleteClickListener != null) {
                        int position = getLayoutPosition();
                        deleteClickListener.clickListener(position);
                    }
                }
            });
        }
    }
}
