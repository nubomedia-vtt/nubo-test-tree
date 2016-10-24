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

package fi.vtt.nubotest.util;

/**
 * Created by GleasonK on 7/30/15.
 */
public class Constants {
    //public static final String SHARED_PREFS = "fi.vtt.nubotest.SHARED_PREFS";
    public static final String USER_NAME    = "fi.vtt.nubotest.SHARED_PREFS.USER_NAME";
    public static final String CALL_USER    = "fi.vtt.nubotest.SHARED_PREFS.CALL_USER";
    public static final String STDBY_SUFFIX = "-stdby";

    public static final String PUB_KEY = "pub-c-9d0d75a5-38db-404f-ac2a-884e18b041d8";
    public static final String SUB_KEY = "sub-c-4e25fb64-37c7-11e5-a477-0619f8945a4f";

    public static final String JSON_CALL_USER = "call_user";
    public static final String JSON_CALL_TIME = "call_time";
    public static final String JSON_OCCUPANCY = "occupancy";
    public static final String JSON_STATUS    = "status";

    // JSON for user messages
    public static final String JSON_USER_MSG  = "user_message";
    public static final String JSON_MSG_UUID  = "msg_uuid";
    public static final String JSON_MSG       = "msg_message";
    public static final String JSON_TIME      = "msg_timestamp";
    public static final String STATUS_AVAILABLE = "Available";
    public static final String STATUS_OFFLINE   = "Offline";
    public static final String STATUS_BUSY      = "Busy";
    public static final String SERVER_NAME      = "serverName";
    public static final String DEFAULT_SERVER   = "wss://roomtestbed.kurento.org:8443/room";
//    public static final String DEFAULT_SERVER   = "wss://193.166.161.152:8443/room";
    public static String SERVER_ADDRESS_SET_BY_USER = "wss://roomtestbed.kurento.org:8443/room";
    public static final String ROOM_NAME    = "fi.vtt.nubotest.SHARED_PREFS.ROOM_NAME";
    public static int id    = 0;
}
