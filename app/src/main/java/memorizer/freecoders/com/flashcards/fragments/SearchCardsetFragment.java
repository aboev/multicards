package memorizer.freecoders.com.flashcards.fragments;


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

import memorizer.freecoders.com.flashcards.GameplayManager;
import memorizer.freecoders.com.flashcards.R;
import memorizer.freecoders.com.flashcards.classes.CallbackInterface;
import memorizer.freecoders.com.flashcards.classes.CardsetListAdapter;
import memorizer.freecoders.com.flashcards.common.Constants;
import memorizer.freecoders.com.flashcards.common.Multicards;
import memorizer.freecoders.com.flashcards.dao.Cardset;
import memorizer.freecoders.com.flashcards.json.CardSet;
import memorizer.freecoders.com.flashcards.json.quizlet.QuizletSearchResult;
import memorizer.freecoders.com.flashcards.network.ServerInterface;

/**
 * Created by alex-mac on 12.12.15.
 */
public class SearchCardsetFragment extends Fragment {
    private static String LOG_TAG = "SearchCardsetFragment";

    ListView cardSetListView;
    ListView popularCardSetListView;
    CardsetListAdapter cardSetListAdapter;
    CardsetListAdapter popularCardSetListAdapter;
    EditText inputEditText;
    TextView popularCardsetsTextView;
    Button buttonCardsetPicker;
    public int intNextFragment;

    public ProgressDialog progressDialog;

    private ArrayList<String> pendingRequests;

    private int intFragmentType = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_cardset, container, false);

        popularCardsetsTextView = (TextView) view.findViewById(R.id.textViewPopularCardsets);
        popularCardSetListView = (ListView) view.findViewById(R.id.listViewPopularCardsets);
        popularCardSetListAdapter = new CardsetListAdapter(Multicards.getMainActivity());
        popularCardSetListView.setAdapter(popularCardSetListAdapter);

        cardSetListView = (ListView) view.findViewById(R.id.listViewCardSetPicker);
        cardSetListAdapter = new CardsetListAdapter(Multicards.getMainActivity());
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
                                cardSetListAdapter.setQValues(response.sets);
                                cardSetListAdapter.notifyDataSetChanged();
                            }
                        }, null);
                pendingRequests.add(strTag);

                if (inputEditText.getText().toString().isEmpty()) {
                    popularCardSetListView.setVisibility(View.VISIBLE);
                    popularCardsetsTextView.setVisibility(View.VISIBLE);
                } else {
                    popularCardSetListView.setVisibility(View.GONE);
                    popularCardsetsTextView.setVisibility(View.GONE);
                }
            }
        });

        buttonCardsetPicker = (Button) view.findViewById(R.id.buttonCardSetPicker);

        cardSetListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String strSetID = String.valueOf(cardSetListAdapter.qvalues.get(position).id);
                String strGID = "quizlet_" + strSetID;
                if (intNextFragment == Constants.UI_STATE_TRAIN_MODE) {
                    startSinglePlayer(strGID);
                } else if (intNextFragment == Constants.UI_STATE_MULTIPLAYER_MODE) {
                    startMultiplayer(strGID);
                }
            }
        });

        pendingRequests = new ArrayList<String>();

        populateView();

        return view;
    }

    public void populateView() {
        ServerInterface.getPopularCardsetsRequest(new Response.Listener<ArrayList<CardSet>>() {
            @Override
            public void onResponse(ArrayList<CardSet> response) {
                if (response.size()>0) {
                    Log.d(LOG_TAG, "Received " + response.size() + " items");
                    popularCardsetsTextView.setVisibility(View.VISIBLE);
                    popularCardSetListView.setVisibility(View.VISIBLE);
                    popularCardSetListAdapter.setValues(response);
                    popularCardSetListAdapter.notifyDataSetChanged();
                    popularCardSetListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            final String strGID = String.valueOf(
                                    popularCardSetListAdapter.values.get(position).gid);
                            if (intNextFragment == Constants.UI_STATE_TRAIN_MODE) {
                                startSinglePlayer(strGID);
                            } else if (intNextFragment == Constants.UI_STATE_MULTIPLAYER_MODE) {
                                startMultiplayer(strGID);
                            }
                        }
                    });
                }
            }
        }, null);
    }

    public void setNextFragment (int intNextFragment) {
        this.intNextFragment = intNextFragment;
    }

    private void startSinglePlayer (final String strGID) {
        Cardset cardset = Multicards.getFlashCardsDAO().fetchCardset(strGID);
        if (cardset == null) {
            Multicards.getFlashCardsDAO().importFromWeb(strGID,
                    new CallbackInterface() {
                        @Override
                        public void onResponse(Object obj) {
                            if (progressDialog != null)
                                progressDialog.dismiss();
                            Multicards.getFlashCardsDAO().setRecentCardset(strGID);
                            Long setID = (Long) obj;
                            GameplayManager.startSingleplayerGame(setID);
                            Multicards.getCardsetPickerActivity().finish();
                        }
                    }, new CallbackInterface() {
                        @Override
                        public void onResponse(Object obj) {
                            Multicards.getMainActivity().getFragmentManager().beginTransaction().
                                    detach(Multicards.getMainActivity().mainMenuFragment).commit();
                            if (progressDialog != null)
                                progressDialog.dismiss();
                            Toast.makeText(Multicards.getMainActivity(),
                                    "Failed to fetch cardset",
                                    Toast.LENGTH_LONG).show();
                            Multicards.getMainActivity().returnToMainMenu();
                        }
                    });
            String strMessage = getResources().
                    getString(R.string.download_cardset_dialog_message);
            progressDialog = ProgressDialog.show(
                    Multicards.getCardsetPickerActivity(), "", strMessage, true);
            progressDialog.setCancelable(true);
        } else {
            Multicards.getFlashCardsDAO().setRecentCardset(strGID);
            GameplayManager.startSingleplayerGame(cardset.getId());
            Multicards.getCardsetPickerActivity().finish();
        }
    }

    private void startMultiplayer (String strGID) {
        GameplayManager.requestMultiplayerGame(strGID);
        Multicards.getCardsetPickerActivity().finish();
    }
}
