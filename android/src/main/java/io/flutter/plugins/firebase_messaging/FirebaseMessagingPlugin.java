// Copyright 2017 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.firebase_messaging;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import java.util.HashMap;
import java.util.Map;

/** FirebaseMessagingPlugin */
public class FirebaseMessagingPlugin extends BroadcastReceiver implements MethodCallHandler {
  private final Activity activity;
  private final MethodChannel channel;

  private static final String CLICK_ACTION_VALUE = "FLUTTER_NOTIFICATION_CLICK";

  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "firebase_messaging");
    final FirebaseMessagingPlugin instance = new FirebaseMessagingPlugin(registrar.activity(), channel);
    FirebaseApp.initializeApp(registrar.activity());
    channel.setMethodCallHandler(instance);

    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(FlutterFirebaseInstanceIDService.ACTION_TOKEN);
    intentFilter.addAction(FlutterFirebaseMessagingService.ACTION_REMOTE_MESSAGE);
    LocalBroadcastManager manager = LocalBroadcastManager.getInstance(registrar.activity());
    manager.registerReceiver(instance, intentFilter);
  }

  private FirebaseMessagingPlugin(Activity activity, MethodChannel channel) {
    this.activity = activity;
    this.channel = channel;
  }

  // BroadcastReceiver implementation.
  @Override
  public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();
    if (action.equals(FlutterFirebaseInstanceIDService.ACTION_TOKEN)) {
      String token = intent.getStringExtra(FlutterFirebaseInstanceIDService.EXTRA_TOKEN);
      channel.invokeMethod("onToken", token);
    } else if (action.equals(FlutterFirebaseMessagingService.ACTION_REMOTE_MESSAGE)) {
      RemoteMessage message =
          intent.getParcelableExtra(FlutterFirebaseMessagingService.EXTRA_REMOTE_MESSAGE);
      channel.invokeMethod("onMessage", message.getData());
    }
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    if ("configure".equals(call.method)) {
      FlutterFirebaseInstanceIDService.broadcastToken(activity);
      sendMessageFromIntent("onLaunch", activity.getIntent());
      result.success(null);
    } else if ("subscribeToTopic".equals(call.method)) {
      String topic = call.arguments();
      FirebaseMessaging.getInstance().subscribeToTopic(topic);
      result.success(null);
    } else if ("unsubscribeFromTopic".equals(call.method)) {
      String topic = call.arguments();
      FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
      result.success(null);
    } else {
      result.notImplemented();
    }
  }

  public void onNewIntent(Intent intent) {
    sendMessageFromIntent("onResume", intent);
  }

  private void sendMessageFromIntent(String method, Intent intent) {
    if (CLICK_ACTION_VALUE.equals(intent.getAction())
        || CLICK_ACTION_VALUE.equals(intent.getStringExtra("click_action"))) {
      Map<String, String> message = new HashMap<>();
      for (String key : intent.getExtras().keySet()) {
        message.put(key, intent.getStringExtra(key));
      }
      channel.invokeMethod(method, message);
    }
  }
}
