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
  NativeContext context;

  Map toJSON () {
    return {};
  }

  NativeVar invoke({String method, List args}) {
    NativeVar value = context.newNativeVar();
    context.invoke(object: this, method: method, args: args, returnVar: value);
    return value;
  }
}


class NativeClass extends NativeObject {
  String clsName;

  NativeClass.fromString(NativeContext context, String clsName) {
    this.context = context;
    this.clsName = clsName;
  }
   
  Map toJSON () {
    Map json = Map();
    if (clsName != null) {
      json['clsName'] = clsName;
    }
    return json;
  }
}


class NativeVar extends NativeObject {
  String varId;

  String _randomString() {
    String alphabet = 'qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM';
    int strlenght = 8; 
    String randomString = '';
    for (var i = 0; i < strlenght; i++) {
      randomString = randomString + alphabet[Random().nextInt(alphabet.length)];
    }
    return randomString;
  }

  NativeVar.init(NativeContext context) {
    this.context = context;
    this.varId = 'varId_' + _randomString();
  }
  
  Map toJSON () {
    Map json = Map();
    if (varId != null) {
      json['varId'] = varId;
    }
    return json;
  }
}

////////////////////////////////////////////////////////////////////////////

class NativeInvocation {
  final NativeObject object;
  final String method;
  final List args;
  final NativeVar returnVar;
  
  NativeInvocation(this.object, this.method, this.args, this.returnVar);

  Map toJSON () {
    Map json = Map();
    if (object != null) {
      json['object'] = object.toJSON();
    }

    if (method != null) {
      json['method'] = method;
    }

    if (args != null) {
      json['args'] = args;
    }

    if (returnVar != null) {
      json['returnVar'] = returnVar.toJSON();
    }
    return json;  
  }
}

////////////////////////////////////////////////////////////////////////////


class NativeContext {
  final List _invocationNodes = List();
  NativeVar returnVar;

  void invoke({NativeObject object, String method, List args, NativeVar returnVar}) {
    NativeInvocation invacation = NativeInvocation(object, method, args, returnVar);
    _invocationNodes.add(invacation);
  }

  NativeClass classFromString(String clsName) {
    NativeClass cls = NativeClass.fromString(this, clsName);
    return cls;
  }

  NativeVar newNativeVar() {
    NativeVar object = NativeVar.init(this);
    return object;
  }


  Map toJSON() {
    List invocationNodesJSON = List();
    for (NativeInvocation invocation in _invocationNodes) {
      invocationNodesJSON.add(invocation.toJSON());
    }

    Map json = Map();
    json['_invocationNodes'] = invocationNodesJSON;
    if (returnVar != null) {
      json['returnVar'] = returnVar.toJSON();
    }
    return json;
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
