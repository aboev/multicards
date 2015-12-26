package memorizer.freecoders.com.flashcards;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import memorizer.freecoders.com.flashcards.classes.CallbackInterface;
import memorizer.freecoders.com.flashcards.classes.CardsetListAdapter;
import memorizer.freecoders.com.flashcards.common.Constants;
import memorizer.freecoders.com.flashcards.common.MemorizerApplication;
import memorizer.freecoders.com.flashcards.dao.Cardset;
import memorizer.freecoders.com.flashcards.json.quizlet.QuizletSearchResult;
import memorizer.freecoders.com.flashcards.network.ServerInterface;

/**
 * Created by alex-mac on 12.12.15.
 */
public class SearchCardsetFragment extends Fragment {
    private static String LOG_TAG = "SearchCardsetFragment";

    ListView cardSetListView;
    CardsetListAdapter cardSetListAdapter;
    EditText inputEditText;
    Button buttonCardsetPicker;
    int intNextFragment;

    public ProgressDialog progressDialog;

    private ArrayList<String> pendingRequests;

    private int intFragmentType = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_cardset, container, false);

        cardSetListView = (ListView) view.findViewById(R.id.listViewCardSetPicker);
        cardSetListAdapter = new CardsetListAdapter(MemorizerApplication.getMainActivity());
        cardSetListView.setAdapter(cardSetListAdapter);

        inputEditText = (EditText) view.findViewById(R.id.editTextCardSetPicker);
        inputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String strKeywords = "";
                try {
                    strKeywords = URLEncoder.encode(inputEditText.getText().toString(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    Log.d(LOG_TAG, "UnsupportedEncodingException");
                }

                for (int i = 0; i < pendingRequests.size(); i++) {
                    ServerInterface.cancelRequestByTag(pendingRequests.get(i));
                    pendingRequests.remove(i);
                }
                String strTag = ServerInterface.searchCardsetQuizletRequest(
                        strKeywords,
                        new Response.Listener<QuizletSearchResult>() {
                            @Override
                            public void onResponse(QuizletSearchResult response) {
                                cardSetListAdapter.setValues(response.sets);
                                cardSetListAdapter.notifyDataSetChanged();
                            }
                        }, null);
                pendingRequests.add(strTag);
            }
        });

        buttonCardsetPicker = (Button) view.findViewById(R.id.buttonCardSetPicker);
        buttonCardsetPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MemorizerApplication.getMainActivity().nextFlashCard();

                MemorizerApplication.getMainActivity().scoreView =
                        (TextView) MemorizerApplication.getMainActivity().
                                findViewById(R.id.scoreView);

                MemorizerApplication.getMainActivity().intUIState = Constants.UI_STATE_TRAIN_MODE;

                MemorizerApplication.getMainActivity().showPlayersInfo();
            }
        });

        cardSetListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String strSetID = String.valueOf(cardSetListAdapter.values.get(position).id);
                String strGID = "quizlet_" + strSetID;
                Cardset cardset = MemorizerApplication.getFlashCardsDAO().fetchCardset(strGID);
                if ((cardset == null) && (intNextFragment == Constants.UI_STATE_TRAIN_MODE)) {
                    MemorizerApplication.getFlashCardsDAO().importFromWeb(strGID,
                            new CallbackInterface() {
                                @Override
                                public void onResponse(Object obj) {
                                    if (progressDialog != null)
                                        progressDialog.dismiss();
                                    Long setID = (Long) obj;
                                    MemorizerApplication.getMainActivity().setSetID(setID);
                                    MemorizerApplication.getMainActivity().nextFlashCard();
                                    MemorizerApplication.getMainActivity().scoreView =
                                            (TextView) MemorizerApplication.getMainActivity().
                                                    findViewById(R.id.scoreView);
                                    MemorizerApplication.getMainActivity().intUIState =
                                            Constants.UI_STATE_TRAIN_MODE;
                                    MemorizerApplication.getMainActivity().showPlayersInfo();
                                    MemorizerApplication.getCardsetPickerActivity().finish();
                                }
                            }, new CallbackInterface() {
                                @Override
                                public void onResponse(Object obj) {
                                    if (progressDialog != null)
                                        progressDialog.dismiss();
                                    Toast.makeText(MemorizerApplication.getMainActivity(),
                                            "Failed to fetch cardset",
                                            Toast.LENGTH_LONG).show();
                                    MemorizerApplication.getMainActivity().returnToMainMenu();
                                }
                            });
                    String strMessage = getResources().
                            getString(R.string.download_cardset_dialog_message);
                    progressDialog = ProgressDialog.show(
                            MemorizerApplication.getCardsetPickerActivity(), "", strMessage, true);
                    progressDialog.setCancelable(true);
                } else if ((cardset != null) && (intNextFragment == Constants.UI_STATE_TRAIN_MODE)) {
                    Long setID = cardset.getId();
                    MemorizerApplication.getMainActivity().setSetID(setID);
                    MemorizerApplication.getMainActivity().nextFlashCard();
                    MemorizerApplication.getMainActivity().scoreView =
                            (TextView) MemorizerApplication.getMainActivity().
                                    findViewById(R.id.scoreView);
                    MemorizerApplication.getMainActivity().intUIState =
                            Constants.UI_STATE_TRAIN_MODE;
                    MemorizerApplication.getMainActivity().showPlayersInfo();
                    MemorizerApplication.getCardsetPickerActivity().finish();
                } else if (intNextFragment == Constants.UI_STATE_MULTIPLAYER_MODE) {
                    MultiplayerInterface multiplayerInterface = new MultiplayerInterface();
                    MemorizerApplication.setMultiPlayerInterface(multiplayerInterface);
                    multiplayerInterface.requestNewGame(strGID);
                    MemorizerApplication.getCardsetPickerActivity().finish();
                }
            }
        });

        pendingRequests = new ArrayList<String>();

        return view;
    }

    public void setNextFragment (int intNextFragment) {
        this.intNextFragment = intNextFragment;
    }
}