import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:dna/dna.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await Dna.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }
    try {

      // 调用ObjC
      /*
          id currentDevice;
          currentDevice = [UIDevice currentDevice]
          id systemVersion;
          systemVersion = [currentDevice systemVersion];
      */

      // dart 
      ObjCContext context = ObjCContext();
      NativeVar device = context.newNativeVar('device');
      context.invoke(object: NativeClass('UIDevice'), method: 'currentDevice', args: null, ret: device);
      NativeVar version = context.newNativeVar('systemVersion');
      context.invoke(object: device, method: 'systemVersion', args: null, ret: version);
      context.ret = version;
      //

      /*
      JAVAContext *context
      */

      platformVersion = await context.execute();
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Text('Running on: $_platformVersion\n'),
        ),
      ),
    );
  }
}
