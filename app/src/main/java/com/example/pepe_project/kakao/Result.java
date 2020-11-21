
package com.example.pepe_project.kakao;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Result {

    @SerializedName("boxes")
    @Expose
    private List<List<Integer>> boxes = null;
    @SerializedName("recognition_words")
    @Expose
    private List<String> recognitionWords = null;

    public List<List<Integer>> getBoxes() {
        return boxes;
    }

    public void setBoxes(List<List<Integer>> boxes) {
        this.boxes = boxes;
    }

    public List<String> getRecognitionWords() {
        return recognitionWords;
    }

    public void setRecognitionWords(List<String> recognitionWords) {
        this.recognitionWords = recognitionWords;
    }

}
