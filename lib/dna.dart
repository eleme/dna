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
  Map toJSON () {
    return {'NativeObject':{}};
  }
}


class NativeClass extends NativeObject {
  final String clsName;
  NativeClass(this.clsName);

  Map toJSON () {
    return {'NativeClass':{'clsName':clsName}};
  }
}


class NativeVar extends NativeObject {
  String varName;
  NativeVar(this.varName);

  Map toJSON () {
    return {'NativeVar':{'varName':varName}};
  }
}


class NativeInvocation extends NativeObject {
  NativeObject object;
  String method;
  List args;
  NativeVar ret;
  
  NativeInvocation(this.object, this.method, this.args, this.ret);

  Map toJSON () {
    return {'NativeInvocation':{'object':object.toJSON(), 'method':method, 'args':args, 'ret':ret.toJSON()}};
  }
}


class NativeContext {
  List _invocations = List();
  List _vars = List();
  void invoke({NativeObject object, String method, List args, NativeVar ret}) {
    NativeInvocation node = NativeInvocation(object, method, args, ret);
    _invocations.add(node);
  }

  NativeVar newNativeVar(String varName) {
    NativeVar object = NativeVar(varName);
    _vars.add(object); 
  }

  Map toJSON() {
    List invocationsJSON = List();
    for (NativeInvocation invocation in _invocations) {
      invocationsJSON.add(invocation.toJSON());
    }

    List varsJSON = List();
    for (NativeVar object in varsJSON) {
      varsJSON.add(object.toJSON());
    }

    return {'NativeContext':{'_invocations':invocationsJSON, '_vars':varsJSON}};
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
