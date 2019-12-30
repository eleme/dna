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
////////////////////////////////////////////////////////////////////////////

class NativeObject extends Object {
  final NativeContext context;
  NativeObject(this.context);
  Map toJSON () {
    return Map();
  }

  NativeVar invoke({String method, List args}) {
    NativeVar value = context.newNativeVar();
    context.invoke(object: this, method: method, args: args, returnVar: value);
    return value;
  }
}


//////////////////
class NativeVar extends NativeObject {
  String _varId;

  String _randomString() {
    String alphabet = 'qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM';
    int strlenght = 8; 
    String randomString = '';
    for (var i = 0; i < strlenght; i++) {
      randomString = randomString + alphabet[Random().nextInt(alphabet.length)];
    }
    return randomString;
  }

  NativeVar(NativeContext context) : super(context) {
    _varId = 'varId_' + _randomString();
  }
  
  Map toJSON () {
    Map json = super.toJSON();
    if (_varId != null) {
      json['_varId'] = _varId;
    }
    return json;
  }
}


//////////////////
class NativeClass extends NativeVar {
  final String clsName;
  NativeClass(NativeContext context, this.clsName) : super(context);
   
  Map toJSON () {
    Map json = super.toJSON();
    if (clsName != null) {
      json['clsName'] = clsName;
    }
    return json;
  }
}


//////////////////
class NativeJSONVar extends NativeVar {
  final Map json;
  final String cls;
  NativeJSONVar(NativeContext context, this.json, this.cls) : super(context);

  Map toJSON () {
    Map json = super.toJSON();
    if (this.json != null) {
      json['json'] = this.json;
    }

    if (cls != null) {
      json['cls'] = cls;
    }
    return json;
  }
}

////////////////////////////////////////////////////////////////////////////
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
      List argsJSON = List();
      for (var arg in args) {
        if (arg is NativeObject) {
          argsJSON.add(arg.toJSON());
        } else {
          argsJSON.add(arg);
        }
      }
      json['args'] = argsJSON;
    }

    if (returnVar != null) {
      json['returnVar'] = returnVar.toJSON();
    }
    return json;  
  }
}

////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////


class NativeContext {
  final List _invocationNodes = List();
  final List _jsonVars = List();
  NativeVar returnVar;

  void invoke({NativeObject object, String method, List args, NativeVar returnVar}) {
    NativeInvocation invacation = NativeInvocation(object, method, args, returnVar);
    _invocationNodes.add(invacation);
  }

  NativeClass classFromString(String clsName) {
    NativeClass cls = NativeClass(this, clsName);
    return cls;
  }

  NativeVar newNativeVar() {
    NativeVar object = NativeVar(this);
    return object;
  }

  NativeJSONVar newNativeJSONVar(Map json, String cls) {
    NativeVar object = NativeJSONVar(this, json, cls);
    _jsonVars.add(object);
    return object;
  }


  Map toJSON() {
    List invocationNodesJSON = List();
    for (var invocation in _invocationNodes) {
      invocationNodesJSON.add(invocation.toJSON());
    }

    List jsonVarsJSON = List();
    for (var jsonVar in _jsonVars) {
      jsonVarsJSON.add(jsonVar.toJSON());
    }

    Map json = Map();
    json['_invocationNodes'] = invocationNodesJSON;
    json['_jsonVars'] = jsonVarsJSON;

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


//////////////////
class ObjCContext extends NativeContext {
  bool canExecute() {
    return Platform.isIOS;
  }
}

//////////////////
class JAVAContext extends NativeContext {
  bool canExecute() {
    return Platform.isAndroid;
  }
}
