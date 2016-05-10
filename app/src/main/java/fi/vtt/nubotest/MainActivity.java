package fi.vtt.nubotest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.vtt.nubomedia.kurentotreeclientandroid.KurentoTreeAPI;
import fi.vtt.nubomedia.kurentotreeclientandroid.TreeListener;
import fi.vtt.nubomedia.kurentotreeclientandroid.TreeNotification;
import fi.vtt.nubomedia.kurentotreeclientandroid.TreeResponse;
import fi.vtt.nubomedia.kurentotreeclientandroid.TreeError;

import fi.vtt.nubomedia.utilitiesandroid.LooperExecutor;
import fi.vtt.nubotest.util.Constants;
import org.java_websocket.WebSocketImpl;

public class MainActivity extends Activity implements TreeListener {
    private SharedPreferences mSharedPreferences;
    private String username, roomname;
    private String TAG = "MainActivity";
    private LooperExecutor executor;
    private static KurentoTreeAPI kurentoTreeAPI;
    private int roomId=0;
    private EditText mCallNumET, mTextMessageET;
    private TextView mUsernameTV;
    Handler mHandler;
    public boolean mBounded;
    public static Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebSocketImpl.DEBUG = true;
        setContentView(R.layout.activity_main);
        MainActivity.context = getApplicationContext();
        this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        Constants.SERVER_ADDRESS_SET_BY_USER = this.mSharedPreferences.getString(Constants.SERVER_NAME, Constants.DEFAULT_SERVER);

        this.username     = this.mSharedPreferences.getString(Constants.USER_NAME, "");
        this.roomname     = this.mSharedPreferences.getString(Constants.ROOM_NAME, "");

        Log.i(TAG, "username: "+this.username);
        Log.i(TAG, "roomname: "+this.roomname);

        this.mCallNumET   = (EditText) findViewById(R.id.call_num);
        this.mUsernameTV = (TextView) findViewById(R.id.main_username);
        this.mTextMessageET = (EditText) findViewById(R.id.main_text_message);
        this.mUsernameTV.setText("Connecting to tree server...");

        String wsUri = this.mSharedPreferences.getString(Constants.SERVER_NAME, Constants.DEFAULT_SERVER);

        if(executor==null) {
            executor = new LooperExecutor();
            executor.requestStart();
        }

        if(kurentoTreeAPI==null) {

            Log.i(TAG, "kurentoRoomAPI is null");
            kurentoTreeAPI = new KurentoTreeAPI(executor, wsUri, this);


            // Load test certificate from assets
            CertificateFactory cf;
            Certificate ca = null;
            try {
                cf = CertificateFactory.getInstance("X.509");
                InputStream caInput = new BufferedInputStream(MainActivity.context.getAssets().open("kurento_tree_base64.cer"));
                ca = cf.generateCertificate(caInput);
                kurentoTreeAPI.addTrustedCertificate("ca", ca);
            } catch (CertificateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            kurentoTreeAPI.useSelfSignedCertificate(true);
        }

        if (!kurentoTreeAPI.isWebSocketConnected()) {
            Log.i(TAG, "connectWebSocket");
            kurentoTreeAPI.connectWebSocket();
        }

        mHandler = new Handler();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, PreferencesActivity.class);
            startActivity(intent);
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sign_out) {
//            kurentoRoomAPI.sendLeaveRoom(roomId);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
//        if (kurentoRoomAPI.isWebSocketConnected()) {
//            kurentoRoomAPI.sendLeaveRoom(roomId);
//        }
        kurentoTreeAPI.disconnectWebSocket();
        super.onDestroy();
    }

//    private void joinRoom () {
//        if (kurentoRoomAPI != null) {
//            Constants.id++;
//            roomId = Constants.id;
//            Log.i(TAG, "Joinroom: User: "+this.username+", Room: "+this.roomname+" id:"+roomId);
//            if (kurentoRoomAPI.isWebSocketConnected()) {
//                kurentoRoomAPI.sendJoinRoom(this.username, this.roomname, roomId);
//            }
//        }
//        else
//        {
//            Log.wtf(TAG, "kurentoRoomAPI is null!");
//        }
//    }

//    private Runnable clearMessageView = new Runnable() {
//        @Override
//        public void run() {
//            MainActivity.this.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    mTextMessageTV.setText("");
//                }
//            });
//        }
//    };

    /**
     * Take the user to a video screen. USER_NAME is a required field.
     * @param view button that is clicked to trigger toVideo
     */
    public void masterTree(View view){
        String treeId = mCallNumET.getText().toString();
        Log.i(TAG, "masterTree: " + treeId);
        if (treeId.isEmpty()){
            showToast("Enter a valid tree ID to master.");
            return;
        }
        dispatchMasterTree(treeId);
    }

    public void showToast(String string) {
        try {
            CharSequence text = string;
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(this, text, duration);
            toast.show();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /**TODO: Debate who calls who. Should one be on standby? Or use State API for busy/available
     * Check that user is online. If they are, dispatch the call by publishing to their standby
     *   channel. If the publish was successful, then change activities over to the video chat.
     * The called user will then have the option to accept of decline the call. If they accept,
     *   they will be brought to the video chat activity as well, to connect video/audio. If
     *   they decline, a hangup will be issued, and the VideoChat adapter's onHangup callback will
     *   be invoked.
     * @param treeId Number to publish a call to.
     */
    public void dispatchMasterTree(final String treeId){
        Log.i(TAG, "dispatchMasterTree: " + treeId);

        Intent intent = new Intent(MainActivity.this, MasterVideoActivity.class);
        intent.putExtra(Constants.USER_NAME, treeId);
//        intent.putExtra(Constants.CALL_USER, callNum);  // Only accept from this number?
        startActivity(intent);
    }


    public void dispatchViewTree(final String treeId){
        Log.i(TAG, "dispatchViewTree: " + treeId);

        Intent intent = new Intent(MainActivity.this, ViewerVideoActivity.class);
        intent.putExtra(Constants.USER_NAME, treeId);
//        intent.putExtra(Constants.CALL_USER, callNum);  // Only accept from this number?
        startActivity(intent);
    }

    /**
     * Take the user to a video screen. USER_NAME is a required field.
     * @param view button that is clicked to trigger toVideo
     */
    public void viewTree(View view){
        String treeId = mTextMessageET.getText().toString();

        Log.i(TAG, "viewTree: " + treeId);
        if (treeId.isEmpty()){
            showToast("Enter a valid tree ID to master.");
            return;
        }
        dispatchViewTree(treeId);

    }

    private void logAndToast(String message) {
        Log.i(TAG, message);
        showToast(message);
    }

    @Override
    public void onTreeResponse(TreeResponse response) {
        //logAndToast(response.toString());
        Log.i(TAG, response.toString());

//        List<HashMap<String, String>> mapList = response.getValues();
//        if(mapList!=null) {
//            for (HashMap<String, String> map : mapList) {
//                for (String key : map.keySet()) {
//
//                    if (key.equals("id")) {
//                        final String otherUser = map.get("id");
//                        logAndToast("User: " + otherUser);
//                        MainActivity.this.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                mCallNumET.setText(otherUser);
//                            }
//                        });
//                    }
//                    Log.i(TAG, key + ":" + map.get(key));
//                }
//            }
//        }
    }


    @Override
    public void onTreeError(TreeError error) {
        Log.wtf(TAG, error.toString());
        logAndToast(error.toString());

        if(error.getCode().equals("104")) {
            finish();
        }
    }

    @Override
    public void onTreeNotification(TreeNotification notification) {
        Log.i(TAG, notification.toString());
//        if(notification.getMethod().equals("sendMessage")) {
//            Map<String, Object> map = notification.getParams();
//            final String user = map.get("user").toString();
//            final String message = map.get("message").toString();
//            MainActivity.this.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    mTextMessageTV.setText(user + ": " + message);
//                    mHandler.removeCallbacks(clearMessageView);
//                    mHandler.postDelayed(clearMessageView, 5000);
//                }
//            });
//        }
//
//        if(notification.getMethod().equals("participantLeft")) {
//            Map<String, Object> map = notification.getParams();
//            final String user = map.get("name").toString();
//
//            MainActivity.this.runOnUiThread(new Runnable() {
//                @Override
//                public void run(){
//                    mCallNumET.setText("");
//                    mTextMessageTV.setText("participantLeft: " + user);
//                    mHandler.removeCallbacks(clearMessageView);
//                    mHandler.postDelayed(clearMessageView, 3000);
//                }
//            });
//        }
//
//        if(notification.getMethod().equals("participantJoined"))
//        {
//            Map<String, Object> map = notification.getParams();
//            final String user = map.get("id").toString();
//            MainActivity.this.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    mCallNumET.setText(user);
//                    mTextMessageTV.setText("participantJoined: " + user);
//                    mHandler.removeCallbacks(clearMessageView);
//                    mHandler.postDelayed(clearMessageView, 3000);
//                }
//            });
//        }
    }

    @Override
    public void onTreeConnected() {
        if (kurentoTreeAPI.isWebSocketConnected()) {
           // joinRoom();
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mUsernameTV.setText("Connected to tree server");
                }
            });


        }
    }

    @Override
    public void onTreeDisconnected() {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mUsernameTV.setText("Connection to tree server lost");
            }
        });

    }

    public static KurentoTreeAPI getKurentoTreeAPIInstance(){
        return kurentoTreeAPI;
    }
}
