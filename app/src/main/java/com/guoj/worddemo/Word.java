package com.guoj.worddemo;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Word {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "english_word")
    private String word;
    @ColumnInfo(name = "chinese_meaning")
    private String chinese;
    @ColumnInfo(name = "showMean")
    private boolean showMean;
//    @ColumnInfo(name = "showMean2")
//    private boolean showMean2;
    public Word(String word, String chinese) {
        this.word = word;
        this.chinese = chinese;
    }

//    public boolean isShowMean2() {
//        return showMean2;
//    }
//
//    public void setShowMean2(boolean showMean2) {
//        this.showMean2 = showMean2;
//    }

    public boolean isShowMean() {
        return showMean;
    }

    public void setShowMean(boolean showMean) {
        this.showMean = showMean;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getChinese() {
        return chinese;
    }

    public void setChinese(String chinese) {
        this.chinese = chinese;
    }
}
