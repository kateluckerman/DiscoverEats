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
import com.kateluckerman.discovereats.models.User;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private Context context;
    private List<Business> businesses;
    // holds all of the viewholders to be able to change them all at once
    List<ViewHolder> views;
    // keeps track of checked viewholders while editing list
    List<Business> selected;
    boolean editingMode;

    public ListAdapter(Context context, List<Business> businesses) {
        this.context = context;
        this.businesses = businesses;
        views = new ArrayList<>();
        selected = new ArrayList<>();
    }

    @NonNull
    @Override
    public ListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(ItemRestaurantBinding.inflate(LayoutInflater.from(context)));
        views.add(holder);
        if (editingMode)
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
        selected.clear();
        for (ViewHolder viewHolder : views) {
            viewHolder.makeSelectable();
            viewHolder.uncheck();
        }
        editingMode = true;
    }

    public void turnOffListEditing() {
        for (ViewHolder viewHolder : views) {
            viewHolder.makeUnselectable();
            viewHolder.uncheck();
        }
        editingMode = false;
    }

    public List<Business> getSelected() {
        return selected;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ItemRestaurantBinding binding;

        public ViewHolder(ItemRestaurantBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final Business business) {
//            binding.getRoot().setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Intent intent = new Intent(context, DetailsActivity.class);
//                        intent.putExtra("business", businesses.get(getAdapterPosition()));
//                        context.startActivity(intent);
//                    }
//                });

            business.getFromParse();
            binding.tvName.setText(business.getName());
            binding.tvCategory.setText(business.getCategoryString());
            binding.tvLocation.setText(business.getLocation());
            binding.tvPrice.setText(business.getPrice());
            final int resourceId = context.getResources().getIdentifier(business.getRatingDrawableName(false), "drawable",
                    context.getPackageName());
            binding.ivRating.setImageDrawable(ContextCompat.getDrawable(context, resourceId));
            Glide.with(context).load(business.getPhotoURL()).into(binding.ivMainImage);

            ParseUser.getCurrentUser().getRelation(User.KEY_COMPLETED).getQuery().whereEqualTo(Business.KEY_ALIAS, business.getAlias()).findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (objects == null || objects.isEmpty()) {
                        binding.getRoot().setBackgroundColor(context.getColor(R.color.white));
                        return;
                    }
                    binding.getRoot().setBackgroundColor(context.getColor(R.color.light_gray));
                }
            });

            // if this business has already been selected in this session
            if (selected.contains(business)) {
                binding.checkbox.setChecked(true);
            } else {
                binding.checkbox.setChecked(false);
            }

            binding.checkbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (binding.checkbox.isChecked()) {
                        selected.add(business);
                    } else {
                        selected.remove(business);
                    }
                }
            });
        }

        public void makeSelectable() {
            binding.checkbox.setVisibility(View.VISIBLE);
        }

        public void makeUnselectable() {
            binding.checkbox.setVisibility(View.GONE);
        }

        public void uncheck() {
            binding.checkbox.setChecked(false);
        }
    }
}
