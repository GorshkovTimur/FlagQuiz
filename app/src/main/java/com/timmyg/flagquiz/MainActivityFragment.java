package com.timmyg.flagquiz;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
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
    private String corresctAnswer;
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

    private void loadNextFlag() {
    String nextImage = quizCountriesList.remove(0);
    corresctAnswer = nextImage;
    answerTextview.setText("");

    questionNumberTextView.setText(getString(R.string.question, (correctAnswers + 1), FLAGS_IN_QUIZ));
    String region = nextImage.substring(0, nextImage.indexOf('-'));

    AssetManager assets = getActivity().getAssets();

    try (InputStream stream = assets.open(region + "/" + nextImage + ".png")){
        Drawable flag = Drawable.createFromStream(stream, nextImage);
        flagImageview.setImageDrawable(flag);
        animate(false);
    }
    catch (IOException exception){
        Log.e(TAG, "Error loading " + nextImage, exception);
    }

    Collections.shuffle(fileNameList);

    int correct = fileNameList.indexOf(corresctAnswer);
    fileNameList.add(fileNameList.remove(correct));

        for (int row = 0; row < guessRows; row++) {
            for (int column = 0; column < guessLinearLayouts[row].getChildCount(); column++) {
                Button newGuessButton = (Button) guessLinearLayouts[row].getChildAt(column);
                newGuessButton.setEnabled(true);
                String filename = fileNameList.get(row*2+column);
                newGuessButton.setText(getCountryName(filename));
            }

            int rowran = random.nextInt(guessRows);
            int column = random.nextInt(2);
            LinearLayout randomRow = guessLinearLayouts[rowran];
            String countryName = getCountryName(corresctAnswer);
            ((Button) randomRow.getChildAt(column)).setText(countryName);

        }

    }

    private void animate(boolean animateOut) {
        if (correctAnswers == 0)
            return;

        int centerX = (quizLinearLayout.getLeft() + quizLinearLayout.getRight())/2;
        int centerY = (quizLinearLayout.getTop() + quizLinearLayout.getBottom())/2;

        int radius = Math.max(quizLinearLayout.getWidth(), quizLinearLayout.getHeight());

        Animator animator;

        if (animateOut) {
            animator = ViewAnimationUtils.createCircularReveal(quizLinearLayout, centerX, centerY, radius, 0);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    loadNextFlag();
                }
            });
        } else {
            animator = ViewAnimationUtils.createCircularReveal(quizLinearLayout, centerX, centerY, 0, radius);
        }

        animator.setDuration(500);
        animator.start();

    }

    private String getCountryName(String name) {
        return name.substring(name.indexOf('-')+1).replace('_',' ');
    }

    private View.OnClickListener guessButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Button guessButton = (Button) view;
            String guess = guessButton.getText().toString();
            String answer = getCountryName(corresctAnswer);
            ++totalGuesses;

            if (guess.equals(answer)){
                ++correctAnswers;

                answerTextview.setText(answer + "!");
                answerTextview.setTextColor(getResources().getColor(R.color.correct_answer, getContext().getTheme()));

                disableButtons();

                if (correctAnswers == FLAGS_IN_QUIZ){
                    DialogFragment quizResults = new DialogFragment(){
                        @NonNull
                        @Override
                        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setMessage(
                                    getString(R.string.results,
                                            totalGuesses,
                                            (1000 / (double) totalGuesses)));

                            builder.setPositiveButton(R.string.reset_quiz,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            resetQuiz();
                                        }
                                    });
                            return builder.create();
                        }
                    };

                    quizResults.setCancelable(false);
                    quizResults.show(getFragmentManager(),"quiz results");
                } else {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            animate(true);
                        }
                    }, 2000);
                }
            } else {
                flagImageview.startAnimation(shakeAnimation);

                answerTextview.setText(R.string.incorrect_answer);
                answerTextview.setTextColor(getResources().getColor(R.color.incorrect_answer, getContext().getTheme()));
                guessButton.setEnabled(false);
            }
        }
    };

    private void disableButtons() {
        for (int i = 0; i < guessRows ; i++) {
            LinearLayout guessRow = guessLinearLayouts[i];
            for (int j = 0; j < guessRow.getChildCount(); j++) {
                guessRow.getChildAt(j).setEnabled(false);
            }

        }
    }


}
