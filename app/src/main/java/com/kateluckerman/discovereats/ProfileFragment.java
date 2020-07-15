package com.kateluckerman.discovereats;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kateluckerman.discovereats.databinding.FragmentProfileBinding;
import com.kateluckerman.discovereats.models.Business;
import com.kateluckerman.discovereats.models.User;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    public static final String TAG = "ProfileFragment";

    private ListAdapter adapter;
    private List<Business> allBusinesses;
    private User user;
    private FragmentProfileBinding binding;


    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        user = new User(ParseUser.getCurrentUser());

        // set name in view if user has one, otherwise switch out TextView with EditText to add a name
        if (ParseUser.getCurrentUser().getString(User.KEY_NAME) != null) {
            binding.tvName.setText(user.getName());
        } else {
            binding.tvName.setVisibility(View.GONE);
            binding.llEditName.setVisibility(View.VISIBLE);

            binding.btnSaveName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String name = binding.etName.getText().toString();
                    user.setName(name);
                    binding.tvName.setText(name);
                    binding.llEditName.setVisibility(View.GONE);
                    binding.tvName.setVisibility(View.VISIBLE);
                }
            });
        }

        // set username in view and initialize business array
        binding.tvUsername.setText(user.getUsername());
        allBusinesses = new ArrayList<>();

        // set up RecyclerView with adapter, layout manager, and empty business list
        adapter = new ListAdapter(getContext(), allBusinesses);
        binding.rvList.setAdapter(adapter);
        binding.rvList.setLayoutManager(new LinearLayoutManager(getContext()));

        // get the user's saved business list
        ParseRelation<Business> list = user.getUser().getRelation(User.KEY_LIST);
        list.getQuery().findInBackground(new FindCallback<Business>() {
            @Override
            public void done(List<Business> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error getting business list", e);
                } else {
                    Log.i(TAG, "success");
                    allBusinesses.addAll(objects);
                    adapter.notifyDataSetChanged();
                }
            }
        });

    }
}