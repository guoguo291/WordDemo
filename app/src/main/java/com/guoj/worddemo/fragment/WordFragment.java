package com.guoj.worddemo.fragment;

import android.app.AlertDialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
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
import androidx.core.content.ContextCompat;
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
                if (wordAdpter.getItemCount() < words.size() && !undo) {
                    recyclerView.smoothScrollBy(0, -200);
                }
                undo = false;
                wordAdpter.submitList(words);
            }
        });
        FloatingActionButton floatingActionButton = requireActivity().findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(view -> {
            NavController controller = Navigation.findNavController(view);
            controller.navigate(R.id.action_wordFragment_to_addFragment);
        });
        //为recycleView添加滑动和拖拽处理
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.START | ItemTouchHelper.END) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                wordAdpter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return false;
            }

            @Override
            public void onMoved(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, int fromPos, @NonNull RecyclerView.ViewHolder target, int toPos, int x, int y) {
                super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
                //更新数据库数据在移动结束后，防止在移动过程中更新导致数据保存不及时，数据错乱
                Word wordFrom = allWords.get(viewHolder.getAdapterPosition());
                Word wordTo = allWords.get(target.getAdapterPosition());
                int tempId = wordFrom.getId();
                wordFrom.setId(wordTo.getId());
                wordTo.setId(tempId);
                wordViewModel.updateWords(wordFrom, wordTo);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                //定义一个list保存words 防止使用livedate在后台获取数据时候出现空指针
                Word wordtoDelete = allWords.get(viewHolder.getAdapterPosition());
                wordViewModel.deleteWords(wordtoDelete);
                Snackbar.make(requireActivity().findViewById(R.id.wordFragmentLayout), "删除了一条数据", Snackbar.LENGTH_SHORT)
                        .setAction("撤销", v -> {
                            undo = true;
                            wordViewModel.insertWords(wordtoDelete);
                        }).show();
            }
            //在滑动的时候，画出浅灰色背景和垃圾桶图标，增强删除的视觉效果

            Drawable icon = ContextCompat.getDrawable(requireActivity(),R.drawable.ic_delete_24);
            Drawable background = new ColorDrawable(Color.LTGRAY);
            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                View itemView = viewHolder.itemView;
                int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;

                int iconLeft,iconRight,iconTop,iconBottom;
                int backTop,backBottom,backLeft,backRight;
                backTop = itemView.getTop();
                backBottom = itemView.getBottom();
                iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) /2;
                iconBottom = iconTop + icon.getIntrinsicHeight();
                if (dX > 0) {
                    backLeft = itemView.getLeft();
                    backRight = itemView.getLeft() + (int)dX;
                    background.setBounds(backLeft,backTop,backRight,backBottom);
                    iconLeft = itemView.getLeft() + iconMargin ;
                    iconRight = iconLeft + icon.getIntrinsicWidth();
                    icon.setBounds(iconLeft,iconTop,iconRight,iconBottom);
                } else if (dX < 0){
                    backRight = itemView.getRight();
                    backLeft = itemView.getRight() + (int)dX;
                    background.setBounds(backLeft,backTop,backRight,backBottom);
                    iconRight = itemView.getRight()  - iconMargin;
                    iconLeft = iconRight - icon.getIntrinsicWidth();
                    icon.setBounds(iconLeft,iconTop,iconRight,iconBottom);
                } else {
                    background.setBounds(0,0,0,0);
                    icon.setBounds(0,0,0,0);
                }
                background.draw(c);
                icon.draw(c);
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
                        if (wordAdpter.getItemCount() < words.size() && !undo) {
                            recyclerView.smoothScrollBy(0, -200);
                        }
                        undo = false;
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
//            Word word1 = new Word("Hello", "你好");
//            Word word2 = new Word("World", "世界");
//            Word word3 = new Word("blue", "蓝色");
            Word word1 = new Word("1", "1");
            Word word2 = new Word("2", "2");
            Word word3 = new Word("3", "3");
            Word word4 = new Word("4", "4");
            Word word5 = new Word("5", "5");
            Word word6 = new Word("6", "6");
            Word word7 = new Word("7", "7");
            Word word8 = new Word("8", "8");
            Word word9 = new Word("9", "9");
            Word word10 = new Word("10", "10");
            wordViewModel.insertWords(word1, word2, word3, word4, word5, word6, word7, word8, word9, word10);
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
