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

////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////

class NativeObject extends Object {
  final NativeContext context;
  String _objectId;

  String _randomString() {
    String alphabet = 'qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM';
    int strlenght = 8; 
    String randomString = '';
    for (var i = 0; i < strlenght; i++) {
      randomString = randomString + alphabet[Random().nextInt(alphabet.length)];
    }
    return randomString;
  }

  NativeObject(this.context) {
     _objectId = '_objectId_' + _randomString();
  }

  Map toJSON () {
    Map json = Map();
    if (_objectId != null) {
      json['_objectId'] = _objectId;
    }
    return json;
  }

  NativeObject invoke({String method, List args}) {
    NativeObject value = NativeObject(this.context);
    context.invoke(object: this, method: method, args: args, returnVar: value);
    return value;
  }
}

//////////////////
class NativeClass extends NativeObject {
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
class NativeObjectJSONWrapper extends NativeObject {
  final Map json;
  final String cls;
  NativeObjectJSONWrapper(NativeContext context, this.json, this.cls) : super(context);

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
  final NativeObject returnVar;
  
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
  final List _objectJSONWrappers = List();
  NativeObject returnVar;

  void invoke({NativeObject object, String method, List args, NativeObject returnVar}) {
    NativeInvocation invacation = NativeInvocation(object, method, args, returnVar);
    _invocationNodes.add(invacation);
  }

  NativeObject classFromString(String clsName) {
    NativeClass cls = NativeClass(this, clsName);
    return cls;
  }

  NativeObject newNativeObject() {
    NativeObject object = NativeObject(this);
    return object;
  }

  NativeObject newNativeObjectFromJSON(Map json, String cls) {
    NativeObject object = NativeObjectJSONWrapper(this, json, cls);
    _objectJSONWrappers.add(object);
    return object;
  }


  Map toJSON() {
    List invocationNodesJSON = List();
    for (var invocation in _invocationNodes) {
      invocationNodesJSON.add(invocation.toJSON());
    }

    List objectJSONWrappersJSON = List();
    for (var jsonVar in _objectJSONWrappers) {
      objectJSONWrappersJSON.add(jsonVar.toJSON());
    }

    Map json = Map();
    json['_invocationNodes'] = invocationNodesJSON;
    json['_objectJSONWrappers'] = objectJSONWrappersJSON;

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
