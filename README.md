# dna
### [中文文档👉](./README_CN.md)
### [相关文章](https://juejin.im/post/5e5f1d41518825495b29a05b)
A lightweight dart to native super channel plugin, You can use it to invoke any native code directly in dart code.

Supported Platform(Language):

- iOS(Objective-C)
- Android(Java)

The primary scenario:

- Implement some simple channels directly in dart code;
- Native code that are calling using dna can also be hot-reloaded.


## Add dependency
1. Add folllowing code to the *pubspec.yaml* file in your flutter project:

	```
	dependencies:
 	dna:
    	git:git@github.com:Assuner-Lee/dna.git
	```

	> Reference: [https://flutter.dev/docs/development/packages-and-plugins/using-packages](https://flutter.dev/docs/development/packages-and-plugins/using-packages)

2. import header file in dart code:

	```
	import 'package:dna/dna.dart';
	```


## Usage

### Main class

- `NativeContext`: You can use it to describe *Native code* by *Dart code*, then call `context.execute()` to execute the final *Native code* on associated platform and get the returned value.

- `NativeObject`: Used to identify the *native variable*. The caller `NativeObject ` can call the `invoke` method to pass in the *method name* and the *parameter array args list* in the context of the `NativeContext` to get the return value `NativeObject` object.


The API of `NativeContext` is consistent. Now we will make a detailed introduction for call *ObjC* using `ObjCContext`, Then call *Java* using `JAVAContext`.

### Call ObjC using Dart

`ObjCContext` is the final executor on iOS platform.

#### Context call supported
##### Returned value as caller

ObjC code

```
NSString *versionString = [[UIDevice currentDevice] systemVersion];
// Return versionString using fluter channel
``` 
Dart code

```
ObjCContext context = ObjCContext();
NativeObject UIDevice = context.classFromString('UIDevice');
NativeObject device = UIDevice.invoke(method: 'currentDevice');
NativeObject version = device.invoke(method: 'systemVersion');

context.returnVar = version; // Can be omitted, See:Quick use of instantiated objects in JSON supported

// Get native execution results directly
var versionString = await context.execute(); 
```

##### Returned value as parameters

ObjC code

```
NSString *versionString = [[UIDevice currentDevice] systemVersion];
NSString *platform = @"iOS-";
versionString = [platform stringByAppendingString: versionString];

// Return versionString using fluter channel
``` 
Dart code

```
ObjCContext context = ObjCContext();
NativeClass UIDevice = context.classFromString('UIDevice');
NativeObject device = UIDevice.invoke(method: 'currentDevice');
NativeObject version = device.invoke(method: 'systemVersion');
NativeObject platform = context.classFromString("NSString").invoke(method: 'stringWithString:', args: ['iOS-']);
version = platform.invoke(method: 'stringByAppendingString:', args: [version]);

context.returnVar = version; // Can be omitted, See:Quick use of instantiated objects in JSON supported

// Get native execution results directly
var versionString = await context.execute(); 
```

#### Chaining calls supported

ObjC code

```
NSString *versionString = [[UIDevice currentDevice] systemVersion];
versionString = [@"iOS-" stringByAppendingString: versionString];

// Return versionString using fluter channel
```

Dart code

```
ObjCContext context = ObjCContext();
NativeObject version = context.classFromString('UIDevice').invoke(method: 'currentDevice').invoke(method: 'systemVersion');
version = context.classFromString("NSString").invoke(method: 'stringWithString:', args: ['iOS-']).invoke(method: 'stringByAppendingString:', args: [version]);

context.returnVar = version; // Can be omitted, See:Quick use of instantiated objects in JSON supported


// Get native execution results directly
var versionString = await context.execute(); 
```


> **Something about the final returned value of the `context`**

> `context.returnVar` is the marker of the final returned value of `context`.

> 1. When setting `context.returnVar`, you can get the *Native* variable corresponding to the `NativeObject`;
> 2. Without setting `context.returnVar`, execute to the last `invoke`, if there is a return value, it will be the final returned value of `context`; if not, it will return a `null` value.

> ```
> ObjCContext context = ObjCContext();
> context.classFromString('UIDevice').invoke(method: 'currentDevice').invoke(method: 'systemVersion');
> 
> // Get native execution results directly
> var versionString = await context.execute(); 
> ```

#### Quick use of instantiated objects in JSON supported

Sometimes, we need to directly instantiate an object with `JSON`.

ObjC code

```
ClassA *objectA = [ClassA new]; 
objectA.a = 1;
objectA.b = @"sss";
``` 

Dart code

One way

```
ObjCContext context = ObjCContext();
NativeObject objectA = context.classFromString('ClassA').invoke(method: 'new');
objectA.invoke(method: 'setA:', args: [1]);
objectA.invoke(method: 'setB:', args: ['sss']);
```

The other way

```
ObjCContext context = ObjCContext();
NativeObject objectA = context.newNativeObjectFromJSON({'a':1,'b':'sss'}, 'ClassA');
```

### Call Java using Dart

`JAVAContext` is the final executor on Android
 platform, it has all the fetures that `ObjCContext` have.
 
- Context call supported;
- Chaining calls supported;
- Quick use of instantiated objects in JSON supported.

In addition, it additionally supports the instantiation of an object from the constructor.

#### The instantiation of an object from the constructor supported

Java code

```
String platform = new String("android");
``` 

Dart code

```
NativeObject version = context
            .newJavaObjectFromConstructor('java.lang.String', ["android "])

```

### Fast organization of dual platform code

We provide you with a quick way to initialize and execute context:

```
static Future<Object> traversingNative(ObjCContextBuilder(ObjCContext objcContext), JAVAContextBuilder(JAVAContext javaContext)) async {
    NativeContext nativeContext;
    if (Platform.isIOS) {
      nativeContext = ObjCContext();
      ObjCContextBuilder(nativeContext);
    } else if (Platform.isAndroid) {
      nativeContext = JAVAContext();
      JAVAContextBuilder(nativeContext);
    }
    return executeNativeContext(nativeContext);
}
```

So you can write the native call of two platforms quickly:

```
platformVersion = await Dna.traversingNative((ObjCContext context) {
    NativeObject version = context.classFromString('UIDevice').invoke(method: 'currentDevice').invoke(method: 'systemVersion');
    version = context.classFromString("NSString").invoke(method: 'stringWithString:', args: ['iOS-']).invoke(method: 'stringByAppendingString:', args: [version]);
    
    context.returnVar = version; // Can be omitted
}, (JAVAContext context) {
    NativeObject versionId = context.newJavaObjectFromConstructor('com.example.dna_example.DnaTest', null).invoke(method: 'getDnaVersion').invoke(method: 'getVersion');
    NativeObject version = context.newJavaObjectFromConstructor('java.lang.String', ["android "]).invoke(method: "concat", args: [versionId]);
    
    context.returnVar = version; // Can be omitted
});
```

## Principle introduction

dna **does not involve the transformation from a dart object to a native object**, it also **does not care about the life cycle of the native object**, but **focuses on describing the `context` of native method calls**, When `context.execute()` called, a native method is called through `channel`, and the call stack is passed in the form of `JSON` for native dynamic parsing and calling.

for example,	Let's take a look at the previous Dart code:

```
ObjCContext context = ObjCContext();
NativeObject version = context.classFromString('UIDevice').invoke(method: 'currentDevice').invoke(method: 'systemVersion');
version = context.classFromString("NSString").invoke(method: 'stringWithString:', args: ['iOS-']).invoke(method: 'stringByAppendingString:', args: [version]);

context.returnVar = version; // Can be omitted, See: Quick use of instantiated objects in JSON supported

// Get native execution results directly
var versionString = await context.execute(); 
```

What the `execute()` method of `NativeContext` actually called is the following method:

```
static Future<Object> executeNativeContext(NativeContext context) async {
    return await _channel.invokeMethod('executeNativeContext', context.toJSON());
}
```

In the native executed method corresponding to the `executeNativeContext` method, the received 'JSON' is as follows:

```
{
	"_objectJSONWrappers": [],
	"returnVar": {
		"_objectId": "_objectId_WyWRIsLl"
	},
	"_invocationNodes": [{
		"returnVar": {
			"_objectId": "_objectId_KNWtiPuM"
		},
		"object": {
			"_objectId": "_objectId_qyfACNGb",
			"clsName": "UIDevice"
		},
		"method": "currentDevice"
	}, {
		"returnVar": {
			"_objectId": "_objectId_haPktBlL"
		},
		"object": {
			"_objectId": "_objectId_KNWtiPuM"
		},
		"method": "systemVersion"
	}, {
		"object": {
			"_objectId": "_objectId_UAUcgnOD",
			"clsName": "NSString"
		},
		"method": "stringWithString:",
		"args": ["iOS-"],
		"returnVar": {
			"_objectId": "_objectId_UiCMaHAN"
		}
	}, {
		"object": {
			"_objectId": "_objectId_UiCMaHAN"
		},
		"method": "stringByAppendingString:",
		"args": [{
			"_objectId": "_objectId_haPktBlL"
		}],
		"returnVar": {
			"_objectId": "_objectId_WyWRIsLl"
		}
	}]
}
```

Then we maintain an `objectsInContextMap` on the native side, its key is `objectId`, and the value is native object.

`_invocationNodes` is the call context of the method, let's take a look at one of them.

Here we will dynamically call `[UIDevice currentDevice]`, and return the object to `objectsInContextMap` with `_objectId_KNWtiPuM` stored in `returnVar` as the key.

```
{
	"returnVar": {
		"_objectId": "_objectId_KNWtiPuM"
	},
	"object": {
		"_objectId": "_objectId_qyfACNGb",
		"clsName": "UIDevice"
	},
	"method": "currentDevice"
 },
```

Here, the object `_objectId_KNWtiPuM` is the returned value of the previous method. Take it out from the `objectsInContextMap`, continue the dynamic call, and store the new returned value with the `_objectId` of the `returnVar` as the key.

```
{
	"returnVar": {
		"_objectId": "_objectId_haPktBlL"
	},
	"object": {
		"_objectId": "_objectId_KNWtiPuM" // Will find the real object in objectsInContextMap
	},
	"method": "systemVersion"
}
```

dna supports automatic package loading and unpacking when the method has parameters, such as `int<->NSNumber`, If the parameter is not one of the 15 basic types specified by `channel` but `NativeObject`, we will find the object from `objectsInContextMap` and put it into the actual parameter list.

```
{
	"object": {
		"_objectId": "_objectId_UiCMaHAN"
	},
	"method": "stringByAppendingString:",
	"args": [{
		"_objectId": "_objectId_haPktBlL" // Will find the real object in objectsInContextMap
	}],
	"returnVar": {
		"_objectId": "_objectId_WyWRIsLl"
}
```

If final `returnVar` is set, The object corresponding to the `returnVar objectId` will be found from the `objectsInContextMap` and called back as the return value of the `channel `, if not, take the return value of the last `invocation`(if any).

## Author

- yongguang.lyg@alibaba-inc.com
- zhengguang.zzg@alibaba-inc.com
- zyd178591@alibaba-inc.com

## Change log
| version | note |
| ------ | ------ | 
| 0.1.0 | alpha version | 

## License

dna is available under the MIT license. See the LICENSE file for more info.

## Other Tips

- Code warehouse will be migrated to **eleme** in the near future;
- You are welcome to star, issue and PR.
