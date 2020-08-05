package com.kateluckerman.discovereats.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.kateluckerman.discovereats.ProfileActivity;
import com.kateluckerman.discovereats.databinding.ItemUserBinding;
import com.kateluckerman.discovereats.models.User;

import org.parceler.Parcels;

import java.util.List;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.ViewHolder> {

    Context context;
    List<User> users;


    public UserSearchAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(ItemUserBinding.inflate(LayoutInflater.from(context)));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ItemUserBinding binding;

        public ViewHolder(ItemUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final User user) {
            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ProfileActivity.class);
                    intent.putExtra("user", Parcels.wrap(user));
                    context.startActivity(intent);
                }
            });
            if (user.getName() != null) {
                binding.tvName.setText(user.getName());
            }
            binding.tvUsername.setText(user.getUsername());
            if (user.getProfileImage() != null) {
                Glide.with(context).load(user.getProfileImage().getUrl()).transform(new CircleCrop()).into(binding.ivProfileImage);
            }

            binding.ivAddFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    user.addFriend();
                }
            });
        }
    }
}
