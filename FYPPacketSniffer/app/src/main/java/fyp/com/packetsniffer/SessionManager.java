package fyp.com.packetsniffer;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private final String MY_PREF = "my_preferences";

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(MY_PREF, Context.MODE_PRIVATE);
    }

    public void firstTimeAsking(String permission, boolean isFirstTime) {
        doEdit();
        editor.putBoolean(permission, isFirstTime);
        doCommit();
    }

    public boolean isFirstTimeAsking(String permission) {
        return sharedPreferences.getBoolean(permission, true);
    }

    private void doEdit() {
        if (editor == null) {
            editor = sharedPreferences.edit();
        }
    }

    private void doCommit() {
        if (editor != null) {
            editor.commit();
            editor = null;
        }
    }


}
