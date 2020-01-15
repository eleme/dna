import 'dart:math';
import 'native_context.dart';

class NativeObject extends Object {
  final NativeContext context;

  static String _alphabet = 'qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM';
  static int _strlenght = 8;
  String _objectId;

  static String _randomString() {
    String randomString = '';
    for (var i = 0; i < _strlenght; i++) {
      randomString = randomString + _alphabet[Random().nextInt(_alphabet.length)];
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
    NativeObject returnValue = NativeObject(this.context);
    context.invoke(object: this, method: method, args: args, returnVar: returnValue);
    return returnValue;
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
  final String cls;

  JavaObjectConstructor(NativeContext context, this.cls, List args) : super(context) {
    context.invoke(object: this, method: null, args: args, returnVar: this);
  }

  Map toJSON() {
    Map json = super.toJSON();
    json['constructCls'] = cls;
    return json;
  }
}
