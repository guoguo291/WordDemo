package com.guoj.worddemo;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class WordViewModel extends AndroidViewModel {
    private final WordRepository wordRepository;
    public WordViewModel(Application application){
        super(application);
        wordRepository=new WordRepository(application);
    }
    public LiveData<List<Word>> getAllWords(){
        return wordRepository.getAllWordsLive();
    }
    public void insertWords(Word...words){
        wordRepository.insertWords(words);
    }
    public void deleteWords(Word...words){
        wordRepository.deleteWords(words);
    }
    public void updateWords(Word...words){
        wordRepository.updateWords(words);
    }
    public void deleteAllWords(){
        wordRepository.deleteAllWords();
    }
}
