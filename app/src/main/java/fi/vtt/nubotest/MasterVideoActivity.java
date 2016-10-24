/*
 * (C) Copyright 2016 VTT (http://www.vtt.fi)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package fi.vtt.nubotest;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;

import java.util.Map;

import fi.vtt.nubomedia.kurentotreeclientandroid.TreeError;
import fi.vtt.nubomedia.kurentotreeclientandroid.TreeListener;
import fi.vtt.nubomedia.kurentotreeclientandroid.TreeNotification;
import fi.vtt.nubomedia.kurentotreeclientandroid.TreeResponse;
import fi.vtt.nubomedia.webrtcpeerandroid.NBMMediaConfiguration;
import fi.vtt.nubomedia.webrtcpeerandroid.NBMPeerConnection;
import fi.vtt.nubomedia.webrtcpeerandroid.NBMWebRTCPeer;

import fi.vtt.nubotest.util.Constants;

/**
 * Activity for receiving the video stream of a peer
 * (based on MasterVideoActivity of Pubnub's video chat tutorial example.
 */
public class MasterVideoActivity extends Activity implements NBMWebRTCPeer.Observer, TreeListener {
    private static final String TAG = "MasterVideoActivity";

    private NBMMediaConfiguration peerConnectionParameters;
    private NBMWebRTCPeer nbmWebRTCPeer;

    private SessionDescription localSdp;
    private SessionDescription remoteSdp;

    private VideoRenderer.Callbacks localRender;
    private VideoRenderer.Callbacks remoteRender;
    private GLSurfaceView videoView;

    private SharedPreferences mSharedPreferences;

    private int createTreeRequestId;
    private int setTreeSourceRequestId;
    private int sendIceCandidateRequestId;

    private TextView mCallStatus;

    private String  treeId;
    private boolean backPressed = false;
    private Thread  backPressedThread = null;

    private static final int LOCAL_X_CONNECTED = 72;
    private static final int LOCAL_Y_CONNECTED = 72;
    private static final int LOCAL_WIDTH_CONNECTED = 25;
    private static final int LOCAL_HEIGHT_CONNECTED = 25;
    // Remote video screen position
    private static final int REMOTE_X = 0;
    private static final int REMOTE_Y = 0;
    private static final int REMOTE_WIDTH = 100;
    private static final int REMOTE_HEIGHT = 100;

    private Handler mHandler;
    private TreeState treeState;

    private enum TreeState{
        IDLE, CREATING, CREATING_LOCAL_SOURCE, SETTING_SOURCE, SET_SOURCE
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        treeState = TreeState.IDLE;

        setContentView(R.layout.activity_video_master);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mHandler = new Handler();
        Bundle extras = getIntent().getExtras();
        if (extras == null || !extras.containsKey(Constants.USER_NAME)) {
            ;
            Toast.makeText(this, "Need to pass username to MasterVideoActivity in intent extras (Constants.USER_NAME).",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        this.treeId      = extras.getString(Constants.USER_NAME, "");
        Log.i(TAG, "treeId: " + treeId);

        this.mCallStatus   = (TextView) findViewById(R.id.call_status);

        this.videoView = (GLSurfaceView) findViewById(R.id.gl_surface);
        // Set up the List View for chatting
        RendererCommon.ScalingType scalingType = RendererCommon.ScalingType.SCALE_ASPECT_FILL;
        VideoRendererGui.setView(videoView, null);

        remoteRender = VideoRendererGui.create( REMOTE_X, REMOTE_Y,
                REMOTE_WIDTH, REMOTE_HEIGHT,
                scalingType, false);
        localRender = VideoRendererGui.create(	LOCAL_X_CONNECTED, LOCAL_Y_CONNECTED,
                LOCAL_WIDTH_CONNECTED, LOCAL_HEIGHT_CONNECTED,
                scalingType, true);
        NBMMediaConfiguration.NBMVideoFormat receiverVideoFormat = new NBMMediaConfiguration.NBMVideoFormat(352, 288, PixelFormat.RGB_888, 20);
        peerConnectionParameters = new NBMMediaConfiguration(   NBMMediaConfiguration.NBMRendererType.OPENGLES,
                NBMMediaConfiguration.NBMAudioCodec.OPUS, 0,
                NBMMediaConfiguration.NBMVideoCodec.VP8, 0,
                receiverVideoFormat,
                NBMMediaConfiguration.NBMCameraPosition.FRONT);
        nbmWebRTCPeer = new NBMWebRTCPeer(peerConnectionParameters, this, remoteRender, this);
        nbmWebRTCPeer.initialize();
        Log.i(TAG, "MasterVideoActivity initialized");
//        mHandler.postDelayed(publishDelayed, 4000);

        MainActivity.getKurentoTreeAPIInstance().addObserver(this);

        createTreeRequestId = ++Constants.id;
        MainActivity.getKurentoTreeAPIInstance().sendCreateTree(treeId, createTreeRequestId);

        treeState = treeState.CREATING;
        mCallStatus.setText("Creating tree...");

    }

//    private Runnable publishDelayed = new Runnable() {
//        @Override
//        public void run() {
//            nbmWebRTCPeer.generateOffer("derp", true);
//        }
//    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_video_chat, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onPause() {
    //    nbmWebRTCPeer.stopLocalMedia();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    //    nbmWebRTCPeer.startLocalMedia();
    }

    @Override
    protected void onStop() {
        endCall();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // If back button has not been pressed in a while then trigger thread and toast notification
        if (!this.backPressed){
            this.backPressed = true;
            Toast.makeText(this,"Press back again to end.",Toast.LENGTH_SHORT).show();
            this.backPressedThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(5000);
                        backPressed = false;
                    } catch (InterruptedException e){ Log.d("VCA-oBP","Successfully interrupted"); }
                }
            });
            this.backPressedThread.start();
        }
        // If button pressed the second time then call super back pressed
        // (eventually calls onDestroy)
        else {
            if (this.backPressedThread != null)
                this.backPressedThread.interrupt();
            super.onBackPressed();
        }
    }

    public void hangup(View view) {
        MainActivity.getKurentoTreeAPIInstance().sendRemoveTreeSource(treeId, ++Constants.id);
        MainActivity.getKurentoTreeAPIInstance().sendRemoveTree(treeId, ++Constants.id);
        finish();
    }

//    public void receiveFromRemote(View view){
//        if (callState == CallState.PUBLISHED){
//            callState = CallState.WAITING_REMOTE_USER;
//            nbmWebRTCPeer.generateOffer("remote", false);
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    mCallStatus.setText("Waiting remote stream...");
//                }
//            });
//        }
//    }

    /**
     * Terminates the current call and ends activity
     */
    private void endCall() {
        treeState = TreeState.IDLE;
        try
        {
            if (nbmWebRTCPeer != null) {
                nbmWebRTCPeer.close();
                nbmWebRTCPeer = null;
            }
        }
        catch (Exception e){e.printStackTrace();}
    }

    @Override
    public void onLocalSdpOfferGenerated(final SessionDescription sessionDescription, NBMPeerConnection nbmPeerConnection) {
        localSdp = sessionDescription;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (MainActivity.getKurentoTreeAPIInstance() != null) {
                    Log.d(TAG, "Sending " + sessionDescription.type);
                    setTreeSourceRequestId = ++Constants.id;

                    MainActivity.getKurentoTreeAPIInstance().sendSetTreeSource(treeId, localSdp.description, setTreeSourceRequestId);
                    treeState = TreeState.SETTING_SOURCE;
                }
            }
        });
    }

    @Override
    public void onLocalSdpAnswerGenerated(SessionDescription sessionDescription, NBMPeerConnection nbmPeerConnection) {
    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate, NBMPeerConnection nbmPeerConnection) {
        sendIceCandidateRequestId = ++Constants.id;
        MainActivity.getKurentoTreeAPIInstance().sendAddIceCandidate(treeId,null,iceCandidate.sdpMid,
                                                                     iceCandidate.sdpMLineIndex,
                                                                     iceCandidate.sdp,
                                                                     sendIceCandidateRequestId);
    }

    @Override
    public void onIceStatusChanged(PeerConnection.IceConnectionState iceConnectionState, NBMPeerConnection nbmPeerConnection) {
        Log.i(TAG, "onIceStatusChanged");
    }

    @Override
    public void onRemoteStreamAdded(MediaStream mediaStream, NBMPeerConnection nbmPeerConnection) {
        Log.i(TAG, "onRemoteStreamAdded");
//        nbmWebRTCPeer.attachRendererToRemoteStream(remoteRender, mediaStream);
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                mCallStatus.setText("");
//            }
//        });
    }

    @Override
    public void onRemoteStreamRemoved(MediaStream mediaStream, NBMPeerConnection nbmPeerConnection) {
        Log.i(TAG, "onRemoteStreamRemoved");
    }

    @Override
    public void onPeerConnectionError(String s) {
        Log.e(TAG, "onPeerConnectionError:" + s);
    }

    @Override
    public void onDataChannel(DataChannel dataChannel, NBMPeerConnection connection) {

    }

    @Override
    public void onBufferedAmountChange(long l, NBMPeerConnection connection, DataChannel channel) {

    }

    @Override
    public void onStateChange(NBMPeerConnection connection, DataChannel channel) {

    }

    @Override
    public void onMessage(DataChannel.Buffer buffer, NBMPeerConnection connection, DataChannel channel) {

    }

    @Override
    public void onTreeResponse(TreeResponse response) {
        Log.d(TAG, "OnTreeResponse:" + response);
        if (Integer.valueOf(response.getId()) == createTreeRequestId) {

            // Tree created, now create the local source
            nbmWebRTCPeer.generateOffer("derp", true);
            treeState = TreeState.CREATING_LOCAL_SOURCE;
        }

        if (Integer.valueOf(response.getId()) == setTreeSourceRequestId){

            SessionDescription sd = new SessionDescription(SessionDescription.Type.ANSWER,
                                                            response.getValue("answerSdp"));
            nbmWebRTCPeer.processAnswer(sd, "derp");
            treeState = TreeState.SET_SOURCE;
        }
    }

    @Override
    public void onTreeError(TreeError error) {

        Log.e(TAG, "OnTreeError:" + error);
    }

    @Override
    public void onTreeNotification(TreeNotification notification) {
        Log.i(TAG, "OnTreeNotification (state=" + treeState.toString() + "):" + notification);

        if(notification.getMethod().equals("iceCandidate"))
        {
            Map<String, Object> map = notification.getParams();

            String sdpMid = map.get("sdpMid").toString();
            int sdpMLineIndex = Integer.valueOf(map.get("sdpMLineIndex").toString());
            String sdp = map.get("candidate").toString();

            IceCandidate ic = new IceCandidate(sdpMid, sdpMLineIndex, sdp);

            nbmWebRTCPeer.addRemoteIceCandidate(ic, "derp");
        }
    }

    @Override
    public void onTreeConnected() {

    }

    @Override
    public void onTreeDisconnected() {

    }
}