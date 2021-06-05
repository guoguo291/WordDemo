package com.guoj.worddemo;

import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

public class WordRepository {
    private LiveData<List<Word>> allWordsLive;
    WordDao wordDao;
    public WordRepository(Context context) {
        WordDatabase wordDatabase = WordDatabase.getDataBaseInstance(context);
        wordDao = wordDatabase.getWordDao();
        allWordsLive = wordDao.getAllWordsLive();
    }

    public LiveData<List<Word>> getAllWordsLive() {
        return allWordsLive;
    }

    public void insertWords(Word... words) {
        new InsertAsyncTask(wordDao).execute(words);
    }
    public void updateWords(Word... words) {
        new UpdateAsyncTask(wordDao).execute(words);
    }
    public void deleteWords(Word... words) {
        new DeleteAsyncTask(wordDao).execute(words);
    }
    public void deleteAllWords() {
        new DeleteAllAsyncTask(wordDao).execute();
    }
    public LiveData<List<Word>> getWordsWithPatten(String pattern) {
        return wordDao.getWordsWithPatten("%"+pattern+"%");
    }
    static class InsertAsyncTask extends AsyncTask<Word, Void, Void> {
        WordDao wordDao;
        InsertAsyncTask(WordDao wordDao){
            this.wordDao=wordDao;
        }
        @Override
        protected Void doInBackground(Word... words) {
            wordDao.insertWords(words);
            return null;
        }
    }
    static class UpdateAsyncTask extends AsyncTask<Word, Void, Void> {
        WordDao wordDao;
        UpdateAsyncTask(WordDao wordDao){
            this.wordDao=wordDao;
        }
        @Override
        protected Void doInBackground(Word... words) {
            wordDao.updateWords(words);
            return null;
        }
    }
    static class DeleteAsyncTask extends AsyncTask<Word, Void, Void> {
        WordDao wordDao;
        DeleteAsyncTask(WordDao wordDao){
            this.wordDao=wordDao;
        }
        @Override
        protected Void doInBackground(Word... words) {
            wordDao.deleteWords(words);
            return null;
        }
    }
    static class DeleteAllAsyncTask extends AsyncTask<Void, Void, Void> {
        WordDao wordDao;
        DeleteAllAsyncTask(WordDao wordDao){
            this.wordDao=wordDao;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            wordDao.deleteAllWords();
            return null;
        }
    }
}
