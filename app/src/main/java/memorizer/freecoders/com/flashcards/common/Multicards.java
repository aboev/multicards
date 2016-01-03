package memorizer.freecoders.com.flashcards.common;

import android.app.Application;

import memorizer.freecoders.com.flashcards.CardsetPickerActivity;
import memorizer.freecoders.com.flashcards.MainActivity;
import memorizer.freecoders.com.flashcards.MultiplayerInterface;
import memorizer.freecoders.com.flashcards.dao.FlashCardsDAO;
import memorizer.freecoders.com.flashcards.network.ServerInterface;

/**
 * Created by alex-mac on 08.11.15.
 */
public class Multicards extends Application {
    private static MainActivity mMainActivity;
    private static FlashCardsDAO mFlashCardsDAO;
    private static ServerInterface mServerInterface;
    private static Preferences mPreferences;
    private static MultiplayerInterface mMultiplayerInterface;
    private static CardsetPickerActivity mCardsetPickerActivity;

    @Override
    public void onCreate() {
        super.onCreate();

        mPreferences = new Preferences(this);
        mPreferences.loadPreferences();

        mServerInterface = new ServerInterface();

        mMultiplayerInterface = new MultiplayerInterface();
    }

    public final static void setMainActivity(MainActivity mainActivity) {
        mMainActivity = mainActivity;
    }

    public final static MainActivity getMainActivity(){
        return mMainActivity;
    }

    public final static void setFlashCardsDAO(FlashCardsDAO flashCardsDAO) {
        mFlashCardsDAO = flashCardsDAO;
    }

    public final static FlashCardsDAO getFlashCardsDAO (){
        return mFlashCardsDAO;
    }

    public final static void setServerInterface (ServerInterface serverInterface) {
        mServerInterface = serverInterface;
    }

    public final static ServerInterface getServerInterface () {
        return mServerInterface;
    }

    public final static Preferences getPreferences () {
        return mPreferences;
    }

    public final static void setMultiPlayerInterface (MultiplayerInterface multiPlayerInterface) {
        mMultiplayerInterface = multiPlayerInterface;
    }

    public final static MultiplayerInterface getMultiplayerInterface () {
        return mMultiplayerInterface;
    }

    public final static void setCardsetPickerActivity(CardsetPickerActivity cardsetPickerActivity) {
        mCardsetPickerActivity = cardsetPickerActivity;
    }

    public final static CardsetPickerActivity getCardsetPickerActivity(){
        return mCardsetPickerActivity;
    }

}