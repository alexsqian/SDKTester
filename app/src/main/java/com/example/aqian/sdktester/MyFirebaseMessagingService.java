package com.example.aqian.sdktester;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";
    private NotificationManager mNotificationManager;
    public static int notificationID;
    private String message = "";
    private String messageType = "";
    private Map<String, String> dataPayload;

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getMessageId());

        //Get the Data from the message
        dataPayload = remoteMessage.getData();

        // Check if message contains a data payload.
        if (dataPayload.size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            //Get the message string and the unique ID for the message (getMessageId()
            // is not the same on diff devices so can't use that, instead used postman random int
            messageType = dataPayload.get("type");
            message = dataPayload.get("message");
            String fcm_push_id = dataPayload.get("id");

            //Check to see if it's a message to delete other notifications
            if (messageType.equals("cancel")) {
                String toDeleteMessageID = dataPayload.get("toDeleteMessageID");
                deleteNotification(toDeleteMessageID);
            }
            //Check the other types of messages
            else if (messageType.equals("date") || messageType.equals("log")) {
                //Create a random notification ID to be used to cancel notification
                notificationID = new Random().nextInt(Integer.MAX_VALUE);

                savePreferences(fcm_push_id, notificationID);
                sendNotification(message, fcm_push_id, messageType);
            } else {
                Log.e(TAG, "Wrong message type");
            }
        }
    }

    //Create the notification along with the intent to open up Broadcast service when the notification is cancelled
    //Broadcast service will then go and send a post request to server to send message to delete the notification on
    //other devices
    private void sendNotification(String bodymsg, String fcm_push_id, String action) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Aspera File Transfer")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(bodymsg))
                .setContentText(bodymsg)
                .setContentIntent(pushIntent(action, fcm_push_id))
                .setDeleteIntent(cancelIntent("cancel", fcm_push_id));
        getNotificationManager().notify(notificationID, mBuilder.build());
    }

    private NotificationManager getNotificationManager() {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager)
                    this.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        return mNotificationManager;
    }

    //Deletes the notification associated with the notification ID
    private void deleteNotification(String deleteID) {
        getNotificationManager().cancel(loadSavedPreferences(deleteID));
    }

    //Save the unique ID of the message with the notification ID that was generated
    private void savePreferences(String fcm_push_messageID, int notificationID) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(fcm_push_messageID, notificationID);
        editor.commit();
    }

    //Retrieves the correct notification ID based upon the unique message ID
    private int loadSavedPreferences(String deleteID) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        int toDeleteMessageID = sharedPreferences.getInt(deleteID, 0);
        return toDeleteMessageID;
    }

    //Creates the intent when the notification is pushed
    private PendingIntent pushIntent(String action, String fcm_push_id) {
        Intent intent1 = new Intent(this, MyBroadcastReciever.class);
        intent1.putExtra("uniqueMessageID", fcm_push_id);
        intent1.setAction(action);
        PendingIntent pushIntent = PendingIntent.getBroadcast(this, Integer.parseInt(fcm_push_id),
                intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        return pushIntent;
    }

    //Creates the intent when the notification is swiped and cancelled
    private PendingIntent cancelIntent(String action, String fcm_push_id) {
        Intent intent = new Intent(this, MyBroadcastReciever.class);
        intent.putExtra("uniqueMessageID", fcm_push_id);
        intent.setAction(action);
        PendingIntent cancelIntent = PendingIntent.getBroadcast(this, Integer.parseInt(fcm_push_id),
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return cancelIntent;
    }

}
