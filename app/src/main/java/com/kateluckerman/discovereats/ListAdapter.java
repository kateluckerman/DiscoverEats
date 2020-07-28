package com.kateluckerman.discovereats;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kateluckerman.discovereats.databinding.ItemRestaurantBinding;
import com.kateluckerman.discovereats.models.Business;

import java.util.ArrayList;
import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private Context context;
    private List<Business> businesses;
    List<ViewHolder> views;

    boolean editing;

    public ListAdapter(Context context, List<Business> businesses) {
        this.context = context;
        this.businesses = businesses;
        // holds all of the viewholders to be able to change them all at once
        views = new ArrayList<>();
    }

    @NonNull
    @Override
    public ListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(ItemRestaurantBinding.inflate(LayoutInflater.from(context)));
        views.add(holder);
        if (editing)
            holder.makeSelectable();
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ListAdapter.ViewHolder holder, int position) {
        Business business = businesses.get(position);
        holder.bind(business);
    }

    @Override
    public int getItemCount() {
        return businesses.size();
    }

    public void turnOnListEditing() {
        for (ViewHolder viewHolder : views) {
            viewHolder.makeSelectable();
        }
        editing = true;
    }

    public void turnOffListEditing() {
        for (ViewHolder viewHolder : views) {
            viewHolder.makeUnselectable();
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ItemRestaurantBinding binding;

        public ViewHolder(ItemRestaurantBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, DetailsActivity.class);
                    intent.putExtra("business", businesses.get(getAdapterPosition()));
                    context.startActivity(intent);
                }
            });
        }

        public void bind(Business business) {
            business.getFromParse();
            binding.tvName.setText(business.getName());
            binding.tvCategory.setText(business.getCategoryString());
            binding.tvLocation.setText(business.getLocation());
            binding.tvPrice.setText(business.getPrice());
            final int resourceId = context.getResources().getIdentifier(business.getRatingDrawableName(false), "drawable",
                    context.getPackageName());
            binding.ivRating.setImageDrawable(ContextCompat.getDrawable(context, resourceId));
            Glide.with(context).load(business.getPhotoURL()).into(binding.ivMainImage);
        }

        public void makeSelectable() {
            binding.checkbox.setVisibility(View.VISIBLE);
        }

        public void makeUnselectable() {
            binding.checkbox.setVisibility(View.GONE);
        }
    }
}
