package memorizer.freecoders.com.flashcards.utils;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.google.gson.Gson;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import memorizer.freecoders.com.flashcards.R;
import memorizer.freecoders.com.flashcards.classes.CallbackInterface;
import memorizer.freecoders.com.flashcards.common.Constants;
import memorizer.freecoders.com.flashcards.common.InputDialogInterface;
import memorizer.freecoders.com.flashcards.common.Multicards;
import memorizer.freecoders.com.flashcards.json.UserDetails;
import memorizer.freecoders.com.flashcards.network.ServerInterface;
import memorizer.freecoders.com.flashcards.network.StringRequest;

/**
 * Created by alex-mac on 06.01.16.
 */
public class Utils {
    public static final UserDetails extractOpponentProfile(HashMap<String, UserDetails> map) {
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            String strSocketID = (String) pair.getKey();
            UserDetails userDetails = (UserDetails) pair.getValue();
            if ((!strSocketID.equals(Multicards.getPreferences().strSocketID)) &&
                    (userDetails.name != null) && (!userDetails.name.isEmpty()))
                return userDetails;
            it.remove();
        }
        return null;
    }

    public static final Integer extractOpponentScore(HashMap<String, Integer> map) {
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            String strSocketID = (String) pair.getKey();
            Integer userScore = (Integer) pair.getValue();
            if ((!strSocketID.equals(Multicards.getPreferences().strSocketID)))
                return userScore;
            it.remove();
        }
        return null;
    }

    public final static class AvatarListener implements ImageLoader.ImageListener {
        CircleImageView imageView;

        public AvatarListener(CircleImageView imgView) {
            this.imageView = imgView;
        }

        @Override
        public void onErrorResponse(VolleyError error) {
        }

        @Override
        public void onResponse(ImageLoader.ImageContainer response, boolean arg1) {
            if (response.getBitmap() != null) {
                imageView.setImageResource(0);
                imageView.setImageBitmap(response.getBitmap());
            }
        }
    }

    public final static String getLocale () {
        String lang = Locale.getDefault().getISO3Language().substring(0, 2);
        return lang;
    }


    public final static HashMap sortHashByValues(HashMap map) {
        List list = new LinkedList(map.entrySet());

        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o2)).getValue())
                        .compareTo(((Map.Entry) (o1)).getValue());
            }
        });

        HashMap sortedHashMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }

    public final static void checkLatestVersion () {
        ServerInterface.getServerInfoRequest(
                new Response.Listener<HashMap<String, String>>() {
                    @Override
                    public void onResponse(HashMap<String, String> response) {
                        Gson gson = new Gson();
                        String strServerInfo = gson.toJson(response);
                        if (Multicards.getPreferences().strServerInfo.equals(strServerInfo)) return;
                        PackageInfo pInfo = null;
                        try {
                            pInfo = Multicards.getMainActivity().getPackageManager().getPackageInfo(
                                    Multicards.getMainActivity().getPackageName(), 0);
                        } catch (PackageManager.NameNotFoundException e) {
                        }
                        if (response.containsKey(Constants.KEY_LATEST_APK_VER) &&
                                response.containsKey(Constants.KEY_LATEST_APK_URL) &&
                                response.containsKey(Constants.KEY_MIN_CLIENT_VERSION) &&
                                pInfo != null) {
                            int intLatestAPKVersion = Integer.
                                    valueOf(response.get(Constants.KEY_LATEST_APK_VER));
                            int intMinClientVersion = Integer.
                                    valueOf(response.get(Constants.KEY_MIN_CLIENT_VERSION));
                            if (intMinClientVersion > pInfo.versionCode)
                                InputDialogInterface.showUpdateDialog(true, response);
                            else if (intLatestAPKVersion > pInfo.versionCode)
                                InputDialogInterface.showUpdateDialog(false, response);
                        }
                    }
                }, null);
    }

    public final static void OpenPlayMarketPage() {
        final String appPackageName = Multicards.getMainActivity().getPackageName();

        try {
            Multicards.getMainActivity().startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse(Multicards.getMainActivity().getString(R.string.playMarketDetailsLink) +
                        appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            Multicards.getMainActivity().startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(Multicards.getMainActivity().getString(
                            R.string.playMarketDetailsLinkAlt) + appPackageName)));
        }

    }
}
