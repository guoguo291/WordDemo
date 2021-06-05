package com.guoj.worddemo.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.guoj.worddemo.R;
import com.guoj.worddemo.Word;
import com.guoj.worddemo.WordAdpter;
import com.guoj.worddemo.WordViewModel;

import java.util.List;

import static androidx.core.content.ContextCompat.getSystemService;


public class WordFragment extends Fragment {
    WordViewModel wordViewModel;
    RecyclerView recyclerView;
    WordAdpter wordAdpter;
    LiveData<List<Word>> filteredWords;
    public WordFragment() {
        //设置显示menu
        setHasOptionsMenu(true);
    }

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
        filteredWords=wordViewModel.getAllWords();
        filteredWords.observe(getViewLifecycleOwner(), words -> {
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

    @Override
    public void onResume() {
        InputMethodManager imm = (InputMethodManager) getSystemService(requireContext(), InputMethodManager.class);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getView().getWindowToken(),0);
        }
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull  Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_word,menu);
//       xml中的menuitem需要设置 app:actionViewClass="android.widget.SearchView" 否则会空指针
        MenuItem menuItem = menu.findItem(R.id.app_bar_search);
        SearchView searchView= (SearchView) menuItem.getActionView();
        searchView.setMaxWidth(700);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //移除之前设置的观察，防止碰撞
                filteredWords.removeObservers(getViewLifecycleOwner());
                filteredWords=wordViewModel.getWordsWithPatten(newText);
                filteredWords.observe(getViewLifecycleOwner(), words -> {
                    if (wordAdpter.getItemCount() != words.size()) {
                        wordAdpter.setWords(words);
                        wordAdpter.notifyDataSetChanged();
                        recyclerView.scrollToPosition(wordAdpter.getItemCount() - 1);
                    }
                });
                return true;
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (R.id.menu_add==item.getItemId()){
            Word word1=new Word("Hello","你好");
            Word word2=new Word("World","世界");
            Word word3=new Word("blue","蓝色");
            wordViewModel.insertWords(word1,word2,word3);
        }else if (R.id.menu_clear==item.getItemId()){
            AlertDialog.Builder builder=new AlertDialog.Builder(requireActivity());
            builder.setMessage("清空数据?")
                    .setPositiveButton("确定", (dialog, which) -> wordViewModel.deleteAllWords())
                    .setNegativeButton("取消", (dialog, which) -> { })
                    .create()
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }
}
