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
      platformVersion = await Dna.traversingNative((ObjCContext context) {
        NativeObject version = context.classFromString('UIDevice').invoke(method: 'currentDevice').invoke(method: 'systemVersion');
        version = context.classFromString("NSString").invoke(method: 'stringWithString:', args: ['iOS-']).invoke(method: 'stringByAppendingString:', args: [version]);
        context.returnVar = version; // 该句可省略
      }, (JAVAContext context) {
        NativeObject versionId = context.newJavaObjectFromConstructor('me.ele.dna_example.DnaTest', null).invoke(method: 'getDnaVersion').invoke(method: 'getVersion');
        NativeObject version = context.newJavaObjectFromConstructor('java.lang.String', ["android "]).invoke(method: "concat", args: [versionId]);
        context.returnVar = version; // 该句可省略
      });

      // ObjCContext context = ObjCContext();
      // NativeObject version = context.classFromString('UIDevice').invoke(method: 'currentDevice').invoke(method: 'systemVersion');
      // context.classFromString('NSString').invoke(method: 'stringWithString:', args: ['IOS-']).invoke(method:'stringByAppendingString:',args: [version]);

      // NativeObject objectA = context.newNativeObjectFromJSON({'a':1, 'b':2}, 'ClassA');
      // NativeObject objectB = context.classFromString('ClassB').invoke(method: 'new');
      // objectB.invoke(method: 'setC:',args: [3]);
      // objectB.invoke(method: 'sum:',args: [objectA]);

      // int x =  await context.execute();
      // platformVersion = await context.execute();

      // android 测试代码
      /*   NativeObject objectA = context
            .classFromString("com.example.dna_example.DnaTest")
            .invoke(method: "getDna")
            .invoke(method: "HelloDna", args: ["Hello dna"]);
        NativeObject objectC = context.newNativeObjectFromJSON(
            {'a': 1, 'b': 2}, 'com.example.dna_example.TestModel');
        NativeObject objectB = context
            .classFromString('com.example.dna_example.DnaComTest')
            .invoke(method: 'printlin', args: [objectA]).invoke(
                method: 'printlin', args: [objectC]);*/

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
    initPlatformState();
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
