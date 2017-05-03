# Firebase Cloud Messaging for Flutter

[![Build Status](https://travis-ci.org/flutter/firebase_messaging.svg?branch=master)](https://travis-ci.org/flutter/firebase_messaging)

**WARNING: This is incomplete and experimental.**

This plugin allows Flutter apps to interact with the [Firebase Cloud Messaging (FCM) API](https://firebase.google.com/docs/cloud-messaging/) from  Dart code.

With this plugin, your Flutter app can receive and process push notifications as well as data messages on Android and iOS. Read Firebase's [About FCM Messages](https://firebase.google.com/docs/cloud-messaging/concept-options) to learn more about the differences between notification messages and data messages.

Not all features of the API are implemented in the plugin yet. If something is missing feel free to send a [pull request](https://github.com/flutter/firebase_messaging/pull/new/master) or file an [issue](https://github.com/flutter/firebase_messaging/issues/new).

## Getting Started

Check out the `example` directory for a sample app that uses this plugin. To learn more about Flutter plugins in general, view our [online documentation](https://flutter.io/platform-plugins).

*TODO(goderbauer): Add Step-by-step instructions for integrating the plugin into an app.*

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
