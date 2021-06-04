package com.guoj.worddemo.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.guoj.worddemo.R;
import com.guoj.worddemo.WordAdpter;
import com.guoj.worddemo.WordViewModel;


public class WordFragment extends Fragment {
    WordViewModel wordViewModel;
    RecyclerView recyclerView;
    WordAdpter wordAdpter;
    @Nullable
    @Override
    public View onCreateView(@NonNull  LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_word,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i("guoj","======onViewCreated");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i("guoj","======onActivityCreated");
        wordViewModel = new ViewModelProvider(requireActivity()).get(WordViewModel.class);
        initView();
    }

    private void initView() {
        recyclerView = requireView().findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        wordAdpter = new WordAdpter(wordViewModel);
        recyclerView.setAdapter(wordAdpter);
        wordViewModel.getAllWords().observe(getViewLifecycleOwner(), words -> {
            if (wordAdpter.getItemCount() != words.size()) {
                wordAdpter.setWords(words);
                wordAdpter.notifyDataSetChanged();
                recyclerView.scrollToPosition(wordAdpter.getItemCount() - 1);
            }
        });
        FloatingActionButton floatingActionButton = requireActivity().findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(view -> {
            NavController controller= Navigation.findNavController(view);
            controller.navigate(R.id.action_wordFragment_to_addFragment);
        });
    }
}
