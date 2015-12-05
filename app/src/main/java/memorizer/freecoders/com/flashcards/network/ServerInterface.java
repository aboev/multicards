package memorizer.freecoders.com.flashcards.network;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.HashMap;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import memorizer.freecoders.com.flashcards.MainMenuFragment;
import memorizer.freecoders.com.flashcards.R;
import memorizer.freecoders.com.flashcards.common.Constants;
import memorizer.freecoders.com.flashcards.common.MemorizerApplication;
import memorizer.freecoders.com.flashcards.json.Game;
import memorizer.freecoders.com.flashcards.json.Question;
import memorizer.freecoders.com.flashcards.json.ServerResponse;
import memorizer.freecoders.com.flashcards.json.SocketMessage;
import memorizer.freecoders.com.flashcards.json.UserDetails;

/**
 * Created by alex-mac on 21.11.15.
 */
public class ServerInterface {

    private static Gson gson = new Gson();

    private static String LOG_TAG = "ServerInterface";

    private Socket mSocketIO;
    private String strSocketID;

    public static final void registerUserRequest(
            UserDetails userDetails,
            final Response.Listener<String> responseListener,
            final Response.ErrorListener errorListener) {
        HashMap<String, String> headers = makeHTTPHeaders();
        Log.d(LOG_TAG, "Register user request");
        StringRequest request = new StringRequest(Request.Method.POST,
                Constants.SERVER_URL + Constants.SERVER_PATH_USER ,
                gson.toJson(userDetails), headers,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(LOG_TAG, "Response: " + response);
                        Type type = new TypeToken<ServerResponse
                                <HashMap<String,String>>>(){}.getType();
                        try {
                            ServerResponse<HashMap<String, String>> res =
                                    gson.fromJson(response, type);
                            if ( res != null && res.isSuccess() && res.data != null
                                    && res.data.containsKey(Constants.KEY_ID)
                                    && responseListener != null)
                                responseListener.onResponse(res.data.get(Constants.KEY_ID));
                            else if (errorListener != null)
                                errorListener.onErrorResponse(new VolleyError());
                        } catch (Exception e) {
                            Log.d(LOG_TAG, "Exception: " + e.getLocalizedMessage());
                            if (errorListener != null) errorListener.onErrorResponse(
                                    new VolleyError());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (errorListener != null) errorListener.onErrorResponse(error);
            }
        }
        );
        VolleySingleton.getInstance(MemorizerApplication.getMainActivity()).
                addToRequestQueue(request);
    }

    public static final void updateUserDetailsRequest(
             UserDetails userDetails,
             final Response.Listener<String> responseListener,
             final Response.ErrorListener errorListener) {
        HashMap<String, String> headers = makeHTTPHeaders();
        Log.d(LOG_TAG, "Update user details request");
        StringRequest request = new StringRequest(Request.Method.PUT,
                Constants.SERVER_URL + Constants.SERVER_PATH_USER ,
                gson.toJson(userDetails), headers,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(LOG_TAG, "Response: " + response);
                        Type type = new TypeToken<ServerResponse
                                <String>>(){}.getType();
                        try {
                            ServerResponse<String> res =
                                    gson.fromJson(response, type);
                            if ( res != null && res.isSuccess() && responseListener != null)
                                responseListener.onResponse(null);
                            else if (errorListener != null)
                                errorListener.onErrorResponse(new VolleyError());
                        } catch (Exception e) {
                            Log.d(LOG_TAG, "Exception: " + e.getLocalizedMessage());
                            if (errorListener != null) errorListener.onErrorResponse(
                                    new VolleyError());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (errorListener != null) errorListener.onErrorResponse(error);
            }
        }
        );
        VolleySingleton.getInstance(MemorizerApplication.getMainActivity()).
                addToRequestQueue(request);
    }

    public static final void newGameRequest(
            final Response.Listener<Game> responseListener,
            final Response.ErrorListener errorListener) {
        HashMap<String, String> headers = makeHTTPHeaders();
        Log.d(LOG_TAG, "New game request");
        StringRequest request = new StringRequest(Request.Method.POST,
                Constants.SERVER_URL + Constants.SERVER_PATH_GAME ,
                "", headers,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(LOG_TAG, "Response: " + response);
                        Type type = new TypeToken<ServerResponse
                                <Game>>(){}.getType();
                        try {
                            ServerResponse<Game> res =
                                    gson.fromJson(response, type);
                            if ( res != null && res.isSuccess() && res.data != null
                                    && responseListener != null)
                                responseListener.onResponse(res.data);
                            else if (errorListener != null)
                                errorListener.onErrorResponse(new VolleyError());
                        } catch (Exception e) {
                            Log.d(LOG_TAG, "Exception: " + e.getLocalizedMessage());
                            if (errorListener != null) errorListener.onErrorResponse(
                                    new VolleyError());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (errorListener != null) errorListener.onErrorResponse(error);
            }
        }
        );
        VolleySingleton.getInstance(MemorizerApplication.getMainActivity()).
                addToRequestQueue(request);
    }

    public void uploadImageRequest(final String strImageURI) {
        HashMap<String, String> headers = makeHTTPHeaders();
        MultipartRequest uploadRequest = new MultipartRequest(
                Constants.SERVER_URL+Constants.SERVER_PATH_UPLOAD,
                strImageURI,
                headers,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d(LOG_TAG, response.toString());
                        try {
                            MemorizerApplication.getPreferences().strAvatar = strImageURI;
                            MemorizerApplication.getPreferences().savePreferences();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d(LOG_TAG, "Exception " + e.getLocalizedMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.d(LOG_TAG, "Error: " + error.getMessage());
            }
        }
        );
        VolleySingleton.getInstance(MemorizerApplication.
                getMainActivity()).addToRequestQueue(uploadRequest);
    }

    public void setSocketIO (Socket socket){
        this.mSocketIO = socket;
    }

    public Socket getSocketIO() {
        return this.mSocketIO;
    }

    public void setSocketID (String strSocketID) {
        this.strSocketID = strSocketID;
    }

    public String getSocketID () {
        return this.strSocketID;
    }

    public Emitter.Listener onNewSocketMessage = new Emitter.Listener() {
        @Override
        public void call(final Object[] args) {
            Log.d(LOG_TAG, "Received message " + args[0].toString());
            MemorizerApplication.getMainActivity().
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String strMessageType = "";
                    try {
                        strMessageType = new JSONObject(args[0].toString()).
                                getString(Constants.JSON_SOCK_MSG_TYPE);
                    } catch (JSONException e) {
                        Log.d(LOG_TAG, "Json exception while processing " + args[0].toString());
                    }
                    Log.d(LOG_TAG, "Received socket message " + args[0]);
                    if (strMessageType.equals(Constants.SOCK_MSG_TYPE_ANNOUNCE_SOCKETID)) {
                        Type type = new TypeToken<SocketMessage<String>>() {
                        }.getType();
                        SocketMessage<String> socketMessage = gson.fromJson(args[0].toString(), type);
                        String socketID = (String) socketMessage.msg_body;
                        MemorizerApplication.getPreferences().strSocketID = socketID;
                        Log.d(LOG_TAG, "Assigned socket ID to " + socketID);
                    } else if (strMessageType.equals(Constants.SOCK_MSG_TYPE_ANNOUNCE_NEW_QUESTION)) {
                        if ((MemorizerApplication.getMultiplayerInterface().progressDialog != null)
                                && (MemorizerApplication.getMultiplayerInterface().progressDialog.
                                isShowing()))
                            MemorizerApplication.getMultiplayerInterface().progressDialog.dismiss();
                        Type type = new TypeToken<SocketMessage<Question>>() {}.getType();
                        SocketMessage<Question> socketMessage = gson.fromJson(args[0].toString(), type);
                        MemorizerApplication.getMainActivity().nextFlashCard(socketMessage.msg_body);
                    } else if (strMessageType.equals(Constants.SOCK_MSG_TYPE_GAME_START)) {
                        Type type = new TypeToken<SocketMessage<Game>>() {}.getType();
                        SocketMessage<Game> socketMessage = gson.fromJson(args[0].toString(), type);
                        MemorizerApplication.getMultiplayerInterface().currentGame =
                                socketMessage.msg_body;
                        MemorizerApplication.getMainActivity().showPlayersInfo();
                    } else if (strMessageType.
                            equals(Constants.SOCK_MSG_TYPE_PLAYER_ANSWERED)) {
                        Type type = new TypeToken<SocketMessage<String>>() {}.getType();
                        SocketMessage<String> socketMessage = gson.fromJson(args[0].toString(), type);
                        String strAnswerID = socketMessage.msg_body;
                        MemorizerApplication.getMultiplayerInterface().
                                renderEvent(MemorizerApplication.getMultiplayerInterface().
                                        EVENT_USER_ANSWER, strAnswerID);
                    } else if (strMessageType.
                            equals(Constants.SOCK_MSG_TYPE_GAME_END)) {
                        Toast.makeText(MemorizerApplication.getMainActivity(), "Game Over",
                                Toast.LENGTH_LONG);
                        MainMenuFragment mainMenuFragment = new MainMenuFragment();
                        MemorizerApplication.getMainActivity().getFragmentManager()
                                .beginTransaction().add(R.id.fragment_flashcard_container,
                                mainMenuFragment).commit();
                        MemorizerApplication.getMainActivity().getFragmentManager()
                                .beginTransaction().remove(MemorizerApplication.getMainActivity().
                                playersInfoFragment).commit();
                        MemorizerApplication.getMainActivity().getFragmentManager()
                                .beginTransaction().remove(MemorizerApplication.getMainActivity().
                                currentFlashCardFragment).commit();
                    }
                }
            });
        }
    };

    private static HashMap<String, String> makeHTTPHeaders() {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", "*/*");
        headers.put(Constants.HEADER_USERID, MemorizerApplication.getPreferences().strUserID);
        headers.put(Constants.HEADER_SOCKETID, MemorizerApplication.getPreferences().strSocketID);
        Log.d(LOG_TAG, "Setting socket id to " + MemorizerApplication.getPreferences().strSocketID);
        return headers;
    }
}
