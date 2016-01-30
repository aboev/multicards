package memorizer.freecoders.com.flashcards.fragments;

/**
 * Created by alex-mac on 12.12.15.
 */

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Response;

import de.hdodenhof.circleimageview.CircleImageView;
import memorizer.freecoders.com.flashcards.GameplayManager;
import memorizer.freecoders.com.flashcards.R;
import memorizer.freecoders.com.flashcards.common.Constants;
import memorizer.freecoders.com.flashcards.common.Multicards;
import memorizer.freecoders.com.flashcards.json.GameOverMessage;
import memorizer.freecoders.com.flashcards.network.ServerInterface;
import memorizer.freecoders.com.flashcards.utils.Utils;

public class GameOverFragment extends Fragment {
    private static String LOG_TAG = "GameOverFragment";

    private View view;

    private LinearLayout buttonLikeCardset;
    private String strCardsetID;
    private GameOverMessage gameOverMessage;
    private TextView textViewWinnerName;
    private CircleImageView imageViewWinner;
    private TextView textViewWinner;
    public static int INT_GAME_TYPE_SINGLEPLAYER = 0;
    public static int INT_GAME_TYPE_MULTIPLAYER = 1;
    public int INT_GAME_TYPE = INT_GAME_TYPE_MULTIPLAYER;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_game_over, container, false);
        this.view = view;

        textViewWinnerName = (TextView) view.findViewById(R.id.textViewWinnerName);
        imageViewWinner = (CircleImageView) view.findViewById(R.id.imageViewWinner);
        textViewWinner = (TextView) view.findViewById(R.id.textViewWinner);

        populateView();

        return view;
    }

    public void setCardsetID (String strCardsetID) {
        this.strCardsetID = strCardsetID;
    }

    public String getCardsetID () {return this.strCardsetID;}

    public void populateView() {
        buttonLikeCardset = (LinearLayout) view.findViewById(R.id.buttonLikeCardset);
        buttonLikeCardset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServerInterface.likeCardsetRequest(strCardsetID, new Response.Listener<Boolean>() {
                    @Override
                    public void onResponse(Boolean response) {
                        buttonLikeCardset.setClickable(false);
                        buttonLikeCardset.setEnabled(false);
                        buttonLikeCardset.setPressed(true);
                    }
                }, null);
            }
        });

        if (INT_GAME_TYPE == INT_GAME_TYPE_MULTIPLAYER) {
            textViewWinnerName.setVisibility(View.VISIBLE);
            textViewWinner.setVisibility(View.VISIBLE);
            imageViewWinner.setVisibility(View.VISIBLE);
        } else if (INT_GAME_TYPE == INT_GAME_TYPE_SINGLEPLAYER) {
            textViewWinnerName.setVisibility(View.GONE);
            textViewWinner.setVisibility(View.GONE);
            imageViewWinner.setVisibility(View.GONE);
        }

        showGameOverMessage();
    }

    public void setGameOverMessage (GameOverMessage msg) {
        gameOverMessage = msg;
    }

    public void showGameOverMessage () {
        if (gameOverMessage != null) {
            if (gameOverMessage.winner.name != null) {
                textViewWinnerName.setText(gameOverMessage.winner.name);
                if (gameOverMessage.winner.name.equals(Multicards.getPreferences().strUserName))
                    textViewWinner.setText(getResources().getString(R.string.string_winner_you));
                else
                    textViewWinner.setText(getResources().getString(R.string.string_winner));
            }
            if (gameOverMessage.winner.avatar != null)
                Multicards.getAvatarLoader().get(gameOverMessage.winner.avatar,
                        new Utils.AvatarListener(imageViewWinner));
        }
    }

    public GameOverMessage getGameOverMessage () {
        return this.gameOverMessage;
    }

    public static GameOverFragment cloneFragment (GameOverFragment fragment) {
        GameOverFragment newFragment = new GameOverFragment();
        newFragment.INT_GAME_TYPE = fragment.INT_GAME_TYPE;
        newFragment.setCardsetID(fragment.getCardsetID());
        newFragment.setGameOverMessage(fragment.getGameOverMessage());
        return newFragment;
    }
}
