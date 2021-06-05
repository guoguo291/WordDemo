package com.guoj.worddemo.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.guoj.worddemo.R;
import com.guoj.worddemo.Word;
import com.guoj.worddemo.WordViewModel;

import static androidx.core.content.ContextCompat.getSystemService;

public class AddFragment extends Fragment {

    EditText et_english;
    EditText et_chinese;
    Button btn_ok;
    WordViewModel wordViewModel;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable  Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        wordViewModel= new ViewModelProvider(requireActivity()).get(WordViewModel.class);
        et_chinese=requireActivity().findViewById(R.id.editTextChinese);
        et_english=requireActivity().findViewById(R.id.editTextEnglish);
        btn_ok=requireActivity().findViewById(R.id.btn_ok);
        TextWatcher watcher=new TextWatcher(){

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btn_ok.setEnabled(!TextUtils.isEmpty(et_chinese.getText())&&!TextUtils.isEmpty(et_english.getText()));
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        et_english.addTextChangedListener(watcher);
        et_chinese.addTextChangedListener(watcher);
        InputMethodManager imm = (InputMethodManager) getSystemService(requireContext(), InputMethodManager.class);
        if (imm != null) {
            et_english.requestFocus();
            imm.showSoftInput(et_english, 0);
        }
        btn_ok.setOnClickListener(v -> {
            if (imm != null) {
                imm.hideSoftInputFromWindow(v.getWindowToken(),0);
            }
            Word word=new Word(et_english.getText().toString().trim(),et_chinese.getText().toString().trim());
            wordViewModel.insertWords(word);
            et_english.setText("");
            et_chinese.setText("");
            Toast.makeText(requireContext(),"添加成功!",Toast.LENGTH_SHORT).show();
            NavController controller= Navigation.findNavController(v);
            controller.navigateUp();
        });
    }
}