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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.guoj.worddemo.R;
import com.guoj.worddemo.Word;
import com.guoj.worddemo.WordViewModel;

import java.util.List;

import static androidx.core.content.ContextCompat.getSystemService;


public class WordFragment extends Fragment {
    WordViewModel wordViewModel;
    RecyclerView recyclerView;
    WordAdpter wordAdpter;
    LiveData<List<Word>> filteredWords;
    List<Word> allWords;
    boolean undo;
    public WordFragment() {
        //设置显示menu
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_word, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i("guoj", "======onViewCreated");
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i("guoj", "======onActivityCreated");
        wordViewModel = new ViewModelProvider(requireActivity()).get(WordViewModel.class);
        initView();
    }

    private void initView() {
        recyclerView = requireView().findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        wordAdpter = new WordAdpter(wordViewModel);
        recyclerView.setAdapter(wordAdpter);
        recyclerView.setItemAnimator(new DefaultItemAnimator() {
            @Override
            public void onAnimationFinished(@NonNull RecyclerView.ViewHolder viewHolder) {
                super.onAnimationFinished(viewHolder);
                //动画结束后更新item的序号显示
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                    int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                    for (int i = firstVisibleItemPosition; i <= lastVisibleItemPosition; i++) {
                        WordAdpter.WordViewHolder holder = (WordAdpter.WordViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
                        if (holder != null) {
                            holder.tv_num.setText(String.valueOf(i + 1));
                        }
                    }
                }
            }
        });
        filteredWords = wordViewModel.getAllWords();
        filteredWords.observe(getViewLifecycleOwner(), words -> {
            allWords = words;
            if (wordAdpter.getItemCount() != words.size()) {
                //删除数据时候不滑动
                if (wordAdpter.getItemCount() < words.size()&&!undo) {
                    recyclerView.smoothScrollBy(0, -200);
                }
                undo=false;
                wordAdpter.submitList(words);
            }
        });
        FloatingActionButton floatingActionButton = requireActivity().findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(view -> {
            NavController controller = Navigation.findNavController(view);
            controller.navigate(R.id.action_wordFragment_to_addFragment);
        });
        //为recycleView添加滑动和拖拽处理
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.START | ItemTouchHelper.END) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                //定义一个list保存words 防止使用livedate在后台获取数据时候出现空指针
                Word wordtoDelete = allWords.get(viewHolder.getAdapterPosition());
                wordViewModel.deleteWords(wordtoDelete);
                Snackbar.make(requireActivity().findViewById(R.id.wordFragmentLayout), "删除了一条数据", Snackbar.LENGTH_SHORT)
                        .setAction("撤销", v -> {
                            undo=true;
                            wordViewModel.insertWords(wordtoDelete);
                        }).show();
            }
        }).attachToRecyclerView(recyclerView);
    }

    @Override
    public void onResume() {
        InputMethodManager imm = (InputMethodManager) getSystemService(requireContext(), InputMethodManager.class);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_word, menu);
//       xml中的menuitem需要设置 app:actionViewClass="android.widget.SearchView" 否则会空指针
        MenuItem menuItem = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
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
                filteredWords = wordViewModel.getWordsWithPatten(newText);
                filteredWords.observe(getViewLifecycleOwner(), words -> {
                    allWords = words;
                    if (wordAdpter.getItemCount() != words.size()) {
                        if (wordAdpter.getItemCount() < words.size()&&!undo) {
                            recyclerView.smoothScrollBy(0, -200);
                        }
                        undo=false;
                        wordAdpter.submitList(words);
                    }
                });
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (R.id.menu_add == item.getItemId()) {
            Word word1 = new Word("Hello", "你好");
            Word word2 = new Word("World", "世界");
            Word word3 = new Word("blue", "蓝色");
            wordViewModel.insertWords(word1, word2, word3);
        } else if (R.id.menu_clear == item.getItemId()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setMessage("清空数据?")
                    .setPositiveButton("确定", (dialog, which) -> wordViewModel.deleteAllWords())
                    .setNegativeButton("取消", (dialog, which) -> {
                    })
                    .create()
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }
}
