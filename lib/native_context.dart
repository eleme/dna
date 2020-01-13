import 'dart:io';
import 'dna.dart';
import 'native_object.dart';

class NativeInvocation {
  final NativeObject object;
  final String method;
  final List args;
  final NativeObject returnVar;

  NativeInvocation(this.object, this.method, this.args, this.returnVar);

  Map toJSON() {
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
    NativeInvocation invocation = NativeInvocation(object, method, args, returnVar);
    _invocationNodes.add(invocation);
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

  Future<Object> execute() async {
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

  NativeObject newJavaObjectFromConstructor(String clsName, List args) {
    NativeObject orignVar = JavaObjectConstructor(this, clsName, args);
    return orignVar;
  }
}
