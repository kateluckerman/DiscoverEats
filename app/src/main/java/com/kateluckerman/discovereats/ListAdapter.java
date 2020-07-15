package com.kateluckerman.discovereats;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kateluckerman.discovereats.databinding.ItemRestaurantBinding;
import com.kateluckerman.discovereats.models.Business;

import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private Context context;
    private List<Business> businesses;

    public ListAdapter(Context context, List<Business> businesses) {
        this.context = context;
        this.businesses = businesses;
    }

    @NonNull
    @Override
    public ListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemRestaurantBinding.inflate(LayoutInflater.from(context)));
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

    class ViewHolder extends RecyclerView.ViewHolder {

        ItemRestaurantBinding binding;

        public ViewHolder(ItemRestaurantBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Business business) {
            business.getFromParse();
            binding.tvName.setText(business.getName());
            binding.tvCategory.setText(business.getCategoryString());
            binding.tvLocation.setText(business.getLocation());
            binding.tvPrice.setText(business.getPrice());
            final int resourceId = context.getResources().getIdentifier(business.getRatingDrawableName(true), "drawable",
                    context.getPackageName());
            binding.ivRating.setImageDrawable(ContextCompat.getDrawable(context, resourceId));
            Glide.with(context).load(business.getPhotoURL()).into(binding.ivMainImage);
            // Create "view on Yelp" link and set the textview to respond to link clicks
            Spanned html = Html.fromHtml("<a href='" + business.getWebsite() + "'>" + context.getString(R.string.yelp_link) + "</a>");
            binding.tvWebsite.setMovementMethod(LinkMovementMethod.getInstance());
            binding.tvWebsite.setText(html);
        }
    }
}
