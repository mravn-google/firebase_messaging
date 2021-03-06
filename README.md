# Firebase Cloud Messaging for Flutter

[![Build Status](https://travis-ci.org/flutter/firebase_messaging.svg?branch=master)](https://travis-ci.org/flutter/firebase_messaging)
[![pub package](https://img.shields.io/pub/v/firebase_messaging.svg)](https://pub.dartlang.org/packages/firebase_messaging)

**WARNING: This is incomplete and experimental.**

This plugin allows Flutter apps to interact with the [Firebase Cloud Messaging (FCM) API](https://firebase.google.com/docs/cloud-messaging/) from  Dart code.

With this plugin, your Flutter app can receive and process push notifications as well as data messages on Android and iOS. Read Firebase's [About FCM Messages](https://firebase.google.com/docs/cloud-messaging/concept-options) to learn more about the differences between notification messages and data messages.

Not all features of the API are implemented in the plugin yet. If something is missing feel free to send a [pull request](https://github.com/flutter/firebase_messaging/pull/new/master) or file an [issue](https://github.com/flutter/firebase_messaging/issues/new).

## Getting Started

Check out the `example` directory for a sample app that uses this plugin. To learn more about Flutter plugins in general, view our [online documentation](https://flutter.io/platform-plugins).

### Add the Plugin

Open the `pubspec.yaml` file of your app and under `dependencies` add a line for this plugin:

```yaml
dependencies:
  firebase_messaging: <version you want to depend on>
```

You can find the most recent version of the plugin on [pub](https://pub.dartlang.org/packages/firebase_messaging).

### Android Integration

To integrate your plugin into the Android part of your app, follow these steps:

1. Using the [Firebase Console](https://console.firebase.google.com/) add an Android app to your project: Follow the assistant, download the generated `google-services.json` file and place it inside `android/app`. Next, modify the `android/build.gradle` file and the `android/app/build.gradle` file to add the Google services plugin as described by the Firebase assistant.

1. If want to be notified in your app (via `onResume` and `onLaunch`, see below) when the user clicks on a notification in the system tray perform the following (optional, but recommended) steps:
   1. Include the following `intent-filter` within the `<activity>` tag of your `android/app/src/main/AndroidManifest.xml`:
      ```xml
      <intent-filter>
          <action android:name="FLUTTER_NOTIFICATION_CLICK" />
          <category android:name="android.intent.category.DEFAULT" />
      </intent-filter>
      ```
   1. Override the `onNewIntent` method in your `android/app/src/main/java/.../MainActivity.java` to forward the call to the plugin (we might be able to remove this step once [flutter/flutter#9215](https://github.com/flutter/flutter/issues/9215) is resolved):
      ```java
      import android.content.Intent;
    
      // ...
    
      @Override
      protected void onNewIntent(Intent intent) {
        pluginRegistry.firebase_messaging.onNewIntent(intent);
      }
      ```

### iOS Integration

To integrate your plugin into the iOS part of your app, follow these steps:

1. Generate the certificates required by Apple for receiving push notifications following [this guide](https://firebase.google.com/docs/cloud-messaging/ios/certs) in the Firebase docs. You can skip the section titled "Create the Provisioning Profile".

1. Using the [Firebase Console](https://console.firebase.google.com/) add an iOS app to your project: Follow the assistant, download the generated `GoogleService-Info.plist` file and place it inside `ios/Runner`. **Don't** follow the steps named "Add Firebase SDK" and "Add initialization code" in the assistant.

1. Follow the steps in the "[Upload your APNs certificate](https://firebase.google.com/docs/cloud-messaging/ios/client#upload_your_apns_certificate)" section of the Firebase docs.

1. Open your `ios/Runner/AppDelegate.m` and forward some of the app delegate methods to the plugin as described in the following snippet (we might be able to remove this step once [flutter/flutter#9682](https://github.com/flutter/flutter/issues/9682) is resolved):
   ```objective-c
   - (BOOL)application:(UIApplication *)application
     // ...
     [plugins.firebase_messaging didFinishLaunchingWithOptions:launchOptions];
     // ...
   }
   
   - (void)applicationDidEnterBackground:(UIApplication *)application {
     [plugins.firebase_messaging applicationDidEnterBackground];
   }
   
   - (void)applicationDidBecomeActive:(UIApplication *)application {
     [plugins.firebase_messaging applicationDidBecomeActive:application];
   }
   
   - (void)application:(UIApplication *)application
       didReceiveRemoteNotification:(NSDictionary *)userInfo {
     [plugins.firebase_messaging didReceiveRemoteNotification:userInfo];
   }

   - (void)application:(UIApplication *)application
       didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {
     [plugins.firebase_messaging didRegisterForRemoteNotificationsWithDeviceToken:deviceToken];
   }
   
   - (void)application:(UIApplication *)application
       didRegisterUserNotificationSettings:(UIUserNotificationSettings *)notificationSettings {
     [plugins.firebase_messaging didRegisterUserNotificationSettings:notificationSettings];
   }
   ```

### Dart/Flutter Integration

From your Dart code, you need to import the plugin and instantiate it:

```dart
import 'package:firebase_messaging/firebase_messaging.dart';

final FirebaseMessaging _firebaseMessaging = new FirebaseMessaging();
```

Next, you should probably request permissions for receiving Push Notifications. For this, call `_firebaseMessaging.requestNotificationPermissions()`. This will bring up a permissions dialog for the user to confirm on iOS. It's a no-op on Android. Last, but not least, register `onMessage`, `onResume`, and `onLaunch` callbacks via `_firebaseMessaging.configure()` to listen for incoming messages (see table below for more information).

## Receiving Messages

Messages are sent to your Flutter app via the `onMessage`, `onLaunch`, and `onResume` callbacks that you configured with the plugin during setup. Here is how different message types are delivered on the supported platforms:

|                             | App in Foreground | App in Background | App Terminated |
| --------------------------: | ----------------- | ----------------- | -------------- |
| **Notification on Android** | `onMessage` | Notification is delivered to system tray. When the user clicks on it to open app `onResume` fires if `click_action: FLUTTER_NOTIFICATION_CLICK` is set (see below). | Notification is delivered to system tray. When the user clicks on it to open app `onLaunch` fires if `click_action: FLUTTER_NOTIFICATION_CLICK` is set (see below). |
| **Notification on iOS** | `onMessage` | Notification is delivered to system tray. When the user clicks on it to open app `onResume` fires. | Notification is delivered to system tray. When the user clicks on it to open app `onLaunch` fires. |
| **Data Message on Android** | `onMessage` | `onMessage` while app stays in the background. | *not supported by plugin, message is lost* |
| **Data Message on iOS**     | `onMessage` | Message is stored by FCM and delivered to app via `onMessage` when the app is brought back to foreground. | Message is stored by FCM and delivered to app via `onMessage` when the app is brought back to foreground. |

Additional reading: Firebase's [About FCM Messages](https://firebase.google.com/docs/cloud-messaging/concept-options).

## Sending Messages
Refer to the [Firebase documentation](https://firebase.google.com/docs/cloud-messaging/) about FCM for all the details about sending messages to your app. When sending a notification message to an Android device, you need to make sure to set the `click_action` property of the message to `FLUTTER_NOTIFICATION_CLICK`. Otherwise the plugin will be unable to deliver the notification to your app when the users clicks on it in the system tray.

For testing purposes, the simplest way to send a notification is via the [Firebase Console](https://firebase.google.com/docs/cloud-messaging/send-with-console). Make sure to include `click_action: FLUTTER_NOTIFICATION_CLICK` as a "Custom data" key-value-pair (under "Advanced options") when targeting an Android device. The Firebase Console does not support sending data messages.

Alternatively, a notification or data message can be sent from a terminal:

```shell
DATA='{"notification": {"click_action": "FLUTTER_NOTIFICATION_CLICK", "body": "this is a body","title": "this is a title"}, "priority": "high", "data": {"id": "1", "status": "done"}, "to": "<FCM TOKEN>"}'
curl https://fcm.googleapis.com/fcm/send -H "Content-Type:application/json" -X POST -d "$DATA" -H "Authorization: key=<FCM SERVER KEY>"
```

Remove the `notification` property in `DATA` to send a data message.
