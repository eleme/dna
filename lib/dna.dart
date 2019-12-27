import 'dart:async';
import 'dart:math';
import 'package:flutter/services.dart';
import 'dart:io';

class Dna {
  static const MethodChannel _channel =
      const MethodChannel('dna');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
  
  static Future<Object> executeNativeContext(NativeContext context) async {
    return await _channel.invokeMethod('executeNativeContext', context.toJSON());
  }
}

////////////////////////////////////////////////////////////////////////////
class NativeObject extends Object {
  Map toJSON () {
    return {};
  }
}


class NativeClass extends NativeObject {
  final String clsName;
  NativeClass(this.clsName);

  Map toJSON () {
    return {'clsName':clsName};
  }
}


class NativeVar extends NativeObject {
  String varName;
  NativeVar(this.varName);

  Map toJSON () {
    return {'varName':varName};
  }
}


class NativeInvocation extends NativeObject {
  NativeObject object;
  String method;
  List args;
  NativeVar ret;
  
  NativeInvocation(this.object, this.method, this.args, this.ret);

  Map toJSON () {
    return {'object':(object != null ? object.toJSON() : null), 'method':method, 'args':args, 'ret':(ret != null ? ret.toJSON() : null)};
  }
}


class NativeContext {
  List _invocationNodes = List();
  List _vars = List();
  NativeVar ret;

  void invoke({NativeObject object, String method, List args, NativeVar ret}) {
    NativeInvocation invacation = NativeInvocation(object, method, args, ret);
    _invocationNodes.add(invacation);
  }

  NativeVar newNativeVar(String varName) {
    NativeVar object = NativeVar(varName);
    _vars.add(object); 
    return object;
  }


  Map toJSON() {
    List invocationNodesJSON = List();
    for (NativeInvocation invocation in _invocationNodes) {
      invocationNodesJSON.add(invocation.toJSON());
    }

    List varsJSON = List();
    for (NativeVar object in _vars) {
      varsJSON.add(object.toJSON());
    }

    return {'_invocationNodes':invocationNodesJSON, '_vars':varsJSON, 'ret':(ret != null ? ret.toJSON() : null)};
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
