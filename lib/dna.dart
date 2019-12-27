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
  final String varName;
  NativeVar(this.varName);

  Map toJSON () {
    return {'varName':varName};
  }
}


class NativeInvocation extends NativeObject {
  final NativeObject object;
  final String method;
  final List args;
  final NativeVar ret;
  
  NativeInvocation(this.object, this.method, this.args, this.ret);

  Map toJSON () {
    return {'object':(object != null ? object.toJSON() : null), 'method':method, 'args':args, 'ret':(ret != null ? ret.toJSON() : null)};
  }
}


class NativeContext {
  final List _invocationNodes = List();
  final List _vars = List();
  NativeVar ret;

  String _randomString() {
    String alphabet = 'qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM';
    int strlenght = 8; 
    String randomString = '';
    for (var i = 0; i < strlenght; i++) {
      randomString = randomString + alphabet[Random().nextInt(alphabet.length)];
    }
    return randomString;
  }

  void invoke({NativeObject object, String method, List args, NativeVar ret}) {
    NativeInvocation invacation = NativeInvocation(object, method, args, ret);
    _invocationNodes.add(invacation);
  }

  NativeVar newNativeVar() {
    NativeVar object = NativeVar('varName_' + _randomString());
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
