package com.example.outfitchanges.ui.home.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.outfitchanges.R;
import com.example.outfitchanges.ui.home.model.OutfitDisplayModel;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {

    public interface OnFavoriteClickListener {
        void onFavoriteClick(String imageUrl);
    }

    private final List<OutfitDisplayModel> dataList = new ArrayList<>();
    private OnFavoriteClickListener favoriteClickListener;

    public void setData(List<OutfitDisplayModel> newData) {
        dataList.clear();
        if (newData != null) {
            dataList.addAll(newData);
        }
        notifyDataSetChanged();
    }

    public void setOnFavoriteClickListener(OnFavoriteClickListener listener) {
        this.favoriteClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_outfit_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(dataList.get(position), favoriteClickListener);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final ChipGroup chipGroup;
        private final ImageView favoriteIcon;
        private final MaterialCardView favoriteButton;
        private final TextView ownerText;
        private final TextView likesText;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_outfit);
            chipGroup = itemView.findViewById(R.id.chip_group_tags);
            favoriteIcon = itemView.findViewById(R.id.icon_favorite);
            favoriteButton = itemView.findViewById(R.id.btn_favorite);
            ownerText = itemView.findViewById(R.id.text_owner);
            likesText = itemView.findViewById(R.id.text_likes);
        }

        void bind(OutfitDisplayModel model, OnFavoriteClickListener listener) {
            Context context = itemView.getContext();

            Glide.with(context)
                    .load(model.getImageUrl())
                    .placeholder(R.color.gray_button_bg)
                    .into(imageView);

            ownerText.setText(model.getOwner());
            likesText.setText(model.getLikes() + " 喜欢");

            chipGroup.removeAllViews();
            for (String tag : model.getTags()) {
                chipGroup.addView(createTagChip(context, tag));
            }

            favoriteIcon.setImageResource(model.isFavorite() ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
            favoriteIcon.setColorFilter(model.isFavorite()
                    ? context.getColor(R.color.primary_color)
                    : context.getColor(R.color.text_primary));

            favoriteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFavoriteClick(model.getImageUrl());
                }
            });
        }

        private Chip createTagChip(Context context, String text) {
            Chip chip = new Chip(context);
            chip.setText(text);
            chip.setTextSize(12);
            chip.setCheckable(false);
            chip.setClickable(false);
            chip.setChipBackgroundColorResource(R.color.chip_bg_color);
            chip.setChipStrokeWidth(1);
            chip.setChipStrokeColorResource(R.color.gray_button_bg);
            chip.setTextColor(context.getColorStateList(R.color.chip_text_color));
            chip.setEnsureMinTouchTargetSize(false);
            return chip;
        }
    }
}