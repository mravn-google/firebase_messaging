// Copyright 2017 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.firebase_messaging_example;

import android.content.Intent;
import android.os.Bundle;
import io.flutter.app.FlutterActivity;
import io.flutter.plugins.PluginRegistry;

public class MainActivity extends FlutterActivity {
  PluginRegistry pluginRegistry;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    pluginRegistry = new PluginRegistry();
    pluginRegistry.registerAll(this);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    pluginRegistry.firebase_messaging.onNewIntent(intent);
  }
}
