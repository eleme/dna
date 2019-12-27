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
    return {'clsName':clsName};
  }
}


class NativeVar extends NativeObject {
  String varId;

  NativeVar.withId(NativeContext context, String varId) {
    this.context = context;
    this.varId = varId;
  }
  Map toJSON () {
    return {'varId': varId};
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
    return {'object':(object != null ? object.toJSON() : null), 'method':method, 'args':args, 'returnVar':(returnVar != null ? returnVar.toJSON() : null)};
  }
}

////////////////////////////////////////////////////////////////////////////


class NativeContext {
  final List _invocationNodes = List();
  final List _vars = List();
  NativeVar returnVar;

  String _randomString() {
    String alphabet = 'qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM';
    int strlenght = 8; 
    String randomString = '';
    for (var i = 0; i < strlenght; i++) {
      randomString = randomString + alphabet[Random().nextInt(alphabet.length)];
    }
    return randomString;
  }

  void invoke({NativeObject object, String method, List args, NativeVar returnVar}) {
    NativeInvocation invacation = NativeInvocation(object, method, args, returnVar);
    _invocationNodes.add(invacation);
  }

  NativeClass classFromString(String clsName) {
    NativeClass cls = NativeClass.fromString(this, clsName);
    return cls;
  }

  NativeVar newNativeVar() {
    NativeVar object = NativeVar.withId(this, 'varId_' + _randomString());
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

    return {'_invocationNodes':invocationNodesJSON, '_vars':varsJSON, 'returnVar':(returnVar != null ? returnVar.toJSON() : null)};
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
