import 'dart:async';
import 'dart:math';
import 'package:flutter/services.dart';
import 'dart:io';

class Dna {
  static const MethodChannel _channel =
      const MethodChannel('dna');

  static Future<Object> executeNativeContext(NativeContext context) async {
    return await _channel.invokeMethod('executeNativeContext', context.toJSON());
  }
}

////////////////////////////////////////////////////////////////////////////
class NativeObject extends Object {

}


class NativeClass extends NativeObject {
  final String clsName;
  NativeClass(this.clsName);
}


class NativeVar extends NativeObject {
  String varName;
  NativeVar(this.varName);
}


class NativeInvocation {
  NativeObject object;
  String method;
  List args;
  NativeVar ret;
  
  NativeInvocation(this.object, this.method, this.args, this.ret);
}


class NativeContext {
  List _invocationNodes = List();
  List _vars = List();
  void invoke({NativeObject object, String method, List args, NativeVar ret}) {
    NativeInvocation node = NativeInvocation(object, method, args, ret);
    _invocationNodes.add(node);
  }

  NativeVar newNativeVar(String varName) {
    NativeVar object = NativeVar(varName);
    _vars.add(object); 
  }

  Map toJSON() {

  }

  bool canExecute() {
    return false;
  }

  Future<Object> execute () async {
    if (this.canExecute()) {
      return await Dna.executeNativeContext(this);
    } else {
      return null;
    }
  }
}


class ObjCContext extends NativeContext {
  bool canExecute() {
    return Platform.isIOS;
  }
}


class JAVAContext extends NativeContext {
  bool canExecute() {
    return Platform.isAndroid;
  }
}
