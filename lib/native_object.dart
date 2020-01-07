import 'dart:math';
import 'native_context.dart';

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

////////////////////
class JavaObjectConstructor extends NativeObject {
  final List args;
  final String cls;

  JavaObjectConstructor(NativeContext context, this.cls, this.args) : super(context) {
    context.invoke(object: this, method: null, args: null, returnVar: this);
  }

  Map toJSON() {
    Map json = super.toJSON();
    json['contructCls'] = cls;

    if (args != null) {
      List argsJSON = List();
      for (var arg in args) {
        if (arg is NativeObject) {
          argsJSON.add(arg.toJSON());
        } else {
          argsJSON.add(arg);
        }
      }
      json['contructArgs'] = argsJSON;
    }

    return json;
  }
}
