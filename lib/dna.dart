import 'dart:async';
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

  static Future<Object> traversingNative(
      ObjCContextBuilder(ObjCContext objcContext),
      JAVAContextBuilder(JAVAContext javaContext)) async {
    NativeContext nativeContext;
    if (Platform.isIOS) {
      nativeContext = ObjCContext();
      ObjCContextBuilder(nativeContext);
    } else if (Platform.isAndroid) {
      nativeContext = JAVAContext();
      JAVAContextBuilder(nativeContext);
    }
    return executeNativeContext(nativeContext);
  }
}

