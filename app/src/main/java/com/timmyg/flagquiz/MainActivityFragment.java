package com.timmyg.flagquiz;

import androidx.fragment.app.Fragment;

import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public static final int FLAGS_IN_QUIZ = 10;

    private static final String TAG = "MainActivityFragment";
    private List<String> fileNameList;
    private Set<String> regionSet;
    private List<String> quizCountriesList;
    private int correctAnswers;
    private int totalGuesses;
    private int guessRows;
    private SecureRandom random;
    private Handler handler;
    private Animation shakeAnimation;

    private LinearLayout quizLinearLayout;
    private TextView questionNumberTextView;
    private ImageView flagImageview;
    private LinearLayout[] guessLinearLayouts;
    private TextView answerTextview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        fileNameList = new ArrayList<>();
        quizCountriesList = new ArrayList<>();
        random = new SecureRandom();
        handler = new Handler();

        shakeAnimation = AnimationUtils.loadAnimation(getActivity(),R.anim.incorrect_shake);
        shakeAnimation.setRepeatCount(3);

        quizLinearLayout = view.findViewById(R.id.quiz_linear_layout);
        questionNumberTextView = view.findViewById(R.id.question_number_text_view);
        flagImageview = view.findViewById(R.id.flag_image_view);
        guessLinearLayouts = new LinearLayout[4];
        guessLinearLayouts[0] = view.findViewById(R.id.row1_linear_layout);
        guessLinearLayouts[1] = view.findViewById(R.id.row2_linear_layout);
        guessLinearLayouts[2] = view.findViewById(R.id.row3_linear_layout);
        guessLinearLayouts[3] = view.findViewById(R.id.row4_linear_layout);
        answerTextview = view.findViewById(R.id.answer_text_view);

        for (LinearLayout row : guessLinearLayouts){
            for (int i=0 ; i < row.getChildCount(); i++){
                Button button = (Button) row.getChildAt(i);
                button.setOnClickListener(guessButtonListener);
                }
        }

        questionNumberTextView.setText(getString(R.string.question,1 , FLAGS_IN_QUIZ));

        return view;



    }

    public void updateGuessRows(SharedPreferences preference) {
        String choices = preference.getString(MainActivity.CHOICES, null);
        guessRows = Integer.parseInt(choices) / 2;

        for (LinearLayout linearLayout : guessLinearLayouts){
            linearLayout.setVisibility(View.GONE);
        }

        for (int row = 0; row < guessRows; row++){
            guessLinearLayouts[row].setVisibility(View.VISIBLE);
        }
    }

    public void updateRegions(SharedPreferences defaultSharedPreferences) {
        regionSet = defaultSharedPreferences.getStringSet(MainActivity.REGIONS, null);
    }

    public void resetQuiz() {
        AssetManager assets = getActivity().getAssets();
        fileNameList.clear();

        for (String region : regionSet){
            try {
                String[] paths = assets.list(region);

                for (String path: paths){
                    fileNameList.add(path.replace(".png", ""));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        correctAnswers = 0;
        totalGuesses = 0;

        quizCountriesList.clear();

        int flagCounter = 1;
        int numberOfFlags = fileNameList.size();

        while (flagCounter <= FLAGS_IN_QUIZ) {
            int randomIndex = random.nextInt(numberOfFlags);

            String filename = fileNameList.get(randomIndex);

            if (!quizCountriesList.contains(filename)){
                quizCountriesList.add(filename);
                ++flagCounter;
            }
        }
        loadNextFlag();

    }
}
