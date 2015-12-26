package memorizer.freecoders.com.flashcards;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import memorizer.freecoders.com.flashcards.classes.AutoResizeTextView;
import memorizer.freecoders.com.flashcards.classes.CallbackInterface;
import memorizer.freecoders.com.flashcards.classes.FlashCard;
import memorizer.freecoders.com.flashcards.classes.ListViewAdapter;
import memorizer.freecoders.com.flashcards.common.Animations;
import memorizer.freecoders.com.flashcards.common.Multicards;

/**
 * Created by alex-mac on 07.11.15.
 */
public class FlashCardFragment extends Fragment {

    private static String LOG_TAG = "FlashCardFragment";

    public final static int INT_SHOW_ANSWER = 0;   // Show user mistake and correct answer
    public final static int INT_NEW_FLASHCARD = 1; // Show new flashcard (default)
    public final static int INT_GIVEN_FLASHCARD = 2; // Show server flashcard

    public static int numCorrectAnswers=0;
    public static int numTotalAnswers=0;

    private View view;

    private AdapterView.OnItemClickListener onFlashCardItemClickListener;

    public FlashCard mFlashCard;
    ListView flashCardsListView;
    ListViewAdapter listViewAdapter;
    AutoResizeTextView questionTextView;
    private int intActionType = INT_NEW_FLASHCARD;


    public void setActionType(int intActionType) {
        this.intActionType = intActionType;
    }

    public void setFlashCard(FlashCard flashCard) {
        this.mFlashCard = flashCard;
    }

    public FlashCard getFlashCard (){
        return this.mFlashCard;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.flashcard, container, false);
        this.view = view;

        populateView();

        return view;
    }

    public void wrongAnswerNotify()
    {
        Snackbar snackbar = Snackbar
                .make(view, "Wrong! Try again", Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(Color.parseColor("#f7df65"));
        TextView textSnackView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textSnackView.setTextColor(Color.parseColor("#e46d6f"));
        snackbar.show();
    }

    public void answerHighlight(final int intAnswerID, final Boolean boolOpponentAnswer,
                                CallbackInterface onComplete) {
        /*
            Visual animation of opponent answer
         */
        Log.d(LOG_TAG, "Highlighting answer " + intAnswerID);
        if ((intAnswerID >= 0) && (intAnswerID < flashCardsListView.getChildCount())) {
            View option = flashCardsListView.getChildAt(intAnswerID);
            TextView textView = (TextView) option.findViewById(R.id.TextView_ButtonName);
            int colorFrom = Color.argb(0, 0, 255, 0);
            int colorTo = Color.argb(255, 0, 255, 0); // Green (correct answer)
            final Boolean boolCorrect;
            if (mFlashCard.answer_id == intAnswerID)
                boolCorrect = true;
            else {
                colorTo = Color.argb(255, 255, 0, 0); // Red (wrong answer)
                boolCorrect = false;
            }

            Animations.highlightColor(textView, colorFrom, colorTo, onComplete);
        }
    }


    public boolean populateView() {
        flashCardsListView = (ListView) view.findViewById(R.id.ListView_FlashCard);
        questionTextView = (AutoResizeTextView) view.findViewById(R.id.TextView_Question);
        listViewAdapter = new ListViewAdapter(view.getContext());
        //Multicards.getMainActivity().updateScore();

        if (intActionType == INT_NEW_FLASHCARD) {
            if (Multicards.getMainActivity().getSetID() != null)
                mFlashCard = Multicards.getFlashCardsDAO().fetchRandomCard(
                        Multicards.getMainActivity().getSetID());
            else
                mFlashCard = Multicards.getFlashCardsDAO().fetchRandomCard();
            questionTextView.setText(mFlashCard.question);
            listViewAdapter.setValues(mFlashCard.options);

            flashCardsListView.setAdapter(listViewAdapter);

            flashCardsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    numTotalAnswers++;
                    if (mFlashCard.answer_id == position) {
                        listViewAdapter.setCorrectAnswer(mFlashCard.answer_id);
                        listViewAdapter.notifyDataSetChanged();
                        numCorrectAnswers++;
                        Multicards.getMainActivity().playersInfoFragment.updateScore();

                        Multicards.getMainActivity().nextFlashCard();
                        Multicards.getMainActivity().playersInfoFragment.increaseScore(0);
                        Multicards.getMainActivity().playersInfoFragment.
                                highlightAnswer(0, true, null);
                    } else {
                        Multicards.getMainActivity().playersInfoFragment.updateScore();
                        wrongAnswerNotify();
                        Multicards.getMainActivity().showAnswer(position);
                        Multicards.getMainActivity().playersInfoFragment.
                                highlightAnswer(0, false, null);
                    }
                    setEmptyOnFlashcardItemClickListener();
                }
            });

            Multicards.getMainActivity().playersInfoFragment.intTotalQuestions++;
        } else if (intActionType == INT_SHOW_ANSWER) {
            questionTextView.setText(mFlashCard.question);
            listViewAdapter.setValues(mFlashCard.options);
            listViewAdapter.setCorrectAnswer(mFlashCard.answer_id);
            listViewAdapter.setWrongAnswer(mFlashCard.wrong_answer_id);

            flashCardsListView.setAdapter(listViewAdapter);

            flashCardsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    if (mFlashCard.answer_id == position) {
                        Multicards.getMainActivity().nextFlashCard();
                    } else
                        wrongAnswerNotify();
                    setEmptyOnFlashcardItemClickListener();
                }
            });

        } else if (intActionType == INT_GIVEN_FLASHCARD) {
            questionTextView.setText(mFlashCard.question);
            listViewAdapter.setValues(mFlashCard.options);

            flashCardsListView.setAdapter(listViewAdapter);

            flashCardsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    Multicards.getMultiplayerInterface().invokeEvent(
                            Multicards.getMultiplayerInterface().EVENT_USER_ANSWER,
                            String.valueOf(position));
                    setEmptyOnFlashcardItemClickListener();
                }
            });
            Multicards.getMainActivity().playersInfoFragment.intTotalQuestions++;
        }

        return true;
    }

    public void setEmptyOnFlashcardItemClickListener() {
        flashCardsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }

    @Override
    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim)
    {

        Log.d(LOG_TAG, "onCreateAnimator" + nextAnim);
        try {
            final Animator anim = AnimatorInflater.loadAnimator(getActivity(), nextAnim);
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    onFlashCardItemClickListener = flashCardsListView.getOnItemClickListener();
                    setEmptyOnFlashcardItemClickListener();
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    flashCardsListView.setOnItemClickListener(onFlashCardItemClickListener);
                }
            });
            return anim;
        } catch (Exception e ) {
            return super.onCreateAnimator(transit, enter, nextAnim);
        }
    }
}
