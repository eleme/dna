import 'dart:async';
import 'package:dna/native_object.dart';
import 'package:flutter/services.dart';
import 'dart:io';
import 'native_context.dart';

export 'native_context.dart' show NativeContext, ObjCContext, JAVAContext;
export 'native_object.dart' show NativeObject, NativeClass;


class Dna {
  static const MethodChannel _channel =
      const MethodChannel('dna');
  
  static Future<Object> executeNativeContext(NativeContext context) async {
    return await _channel.invokeMethod('executeNativeContext', context.toJSON());
  }

  static Future<Object> traversingNative(ObjCContextConstructor(ObjCContext objcContext), JAVAContextConstructor(JAVAContext javaContext)) async {
    NativeContext nativeContext;
    if (Platform.isIOS) {
      nativeContext = ObjCContext();
      ObjCContextConstructor(nativeContext);
    } else if (Platform.isAndroid) {
      nativeContext = JAVAContext();
      JAVAContextConstructor(JAVAContext());
    }
    return executeNativeContext(nativeContext);
  }
}

