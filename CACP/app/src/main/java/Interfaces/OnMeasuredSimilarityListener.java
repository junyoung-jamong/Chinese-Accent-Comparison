package Interfaces;

import java.util.ArrayList;

public interface OnMeasuredSimilarityListener {
    void onMeasured(double sim, ArrayList<Float> playedPitch, ArrayList<Float> recordedPitch);
}
