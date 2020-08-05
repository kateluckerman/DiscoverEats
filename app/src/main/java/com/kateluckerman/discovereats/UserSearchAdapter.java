package com.kateluckerman.discovereats;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kateluckerman.discovereats.databinding.ItemUserBinding;
import com.kateluckerman.discovereats.models.User;

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

        public void bind(User user) {
            binding.tvUsername.setText(user.getUsername());
        }
    }
}
