# dna
### [README->](./README.md)
一个flutter plugin. 轻量级的Dart到Native的超级通道, 可直接在dart代码中调用原生代码，目前支持安卓 JAVA 和 iOS ObjC. 主要用途:

* 可以把channel中的原生代码写在dart代码中，
* 让原生代码也支持热加载.

# 开始
1.这里建议使用 `git` 依赖, 在flutter工程 pubspec.yaml 添加如下:

```
dependencies:
 dna:
    git:git@github.com:Assuner-Lee/dna.git
```

> [参考文档: https://flutter.dev/docs/development/packages-and-plugins/using-packages](https://flutter.dev/docs/development/packages-and-plugins/using-packages)

2.在dart代码中引入头文件

```
import 'package:dna/dna.dart';
```


# 使用介绍
`dna` 在`Dart代码`中:

* 定义了 `NativeContext 类` ，以执行 `Dart 代码` 的方式，描述 `Native 代码` 调用上下文(调用栈)；最后调用 `context.execute()` 执行对应平台的 `Native 代码` 并返回结果。

* 定义了 `NativeObject 类` ，用于标识 `Native 变量`. `调用者 NativeObject 对象` 可借助 `所在NativeContext上下文` 调用 `invoke方法` 传入 `方法名 method` 和 `参数数组 args list` ，得到 `返回值NativeObject对象` 。

`NativeContext 子类` 的API是一致的. 下面先详细介绍通过 `ObjCContext` 调用 `ObjC` ，再区别介绍 `JAVAContext` 调用 `JAVA`.

## Dart 调用 ObjC
`ObjCContext` 仅在iOS平台会实际执行.

### 1. 支持上下文调用
##### (1) 返回值作为调用者
ObjC代码

```
NSString *versionString = [[UIDevice currentDevice] systemVersion];
// 通过channel返回versionString
``` 
Dart 代码

```
ObjCContext context = ObjCContext();
NativeObject UIDevice = context.classFromString('UIDevice');
NativeObject device = UIDevice.invoke(method: 'currentDevice');
NativeObject version = device.invoke(method: 'systemVersion');

context.returnVar = version; // 可省略设定最终返回值, 参考3

// 直接获得原生执行结果  
var versionString = await context.execute(); 
```

##### (2) 返回值作为参数
ObjC代码

```
NSString *versionString = [[UIDevice currentDevice] systemVersion];
NSString *platform = @"iOS-";
versionString = [platform stringByAppendingString: versionString];

// 通过channel返回versionString
``` 
Dart 代码

```
ObjCContext context = ObjCContext();
NativeClass UIDevice = context.classFromString('UIDevice');
NativeObject device = UIDevice.invoke(method: 'currentDevice');
NativeObject version = device.invoke(method: 'systemVersion');
NativeObject platform = context.classFromString("NSString").invoke(method: 'stringWithString:', args: ['iOS-']);
version = platform.invoke(method: 'stringByAppendingString:', args: [version]);

context.returnVar = version; // 可省略设定最终返回值, 参考3

// 直接获得原生执行结果  
var versionString = await context.execute(); 
```


### 2. 支持链式调用
ObjC代码

```
NSString *versionString = [[UIDevice currentDevice] systemVersion];
versionString = [@"iOS-" stringByAppendingString: versionString];

// 通过channel返回versionString
```

Dart 代码

```
ObjCContext context = ObjCContext();
NativeObject version = context.classFromString('UIDevice').invoke(method: 'currentDevice').invoke(method: 'systemVersion');
version = context.classFromString("NSString").invoke(method: 'stringWithString:', args: ['iOS-']).invoke(method: 'stringByAppendingString:', args: [version]);

context.returnVar = version; // 可省略设定最终返回值, 参考3

// 直接获得原生执行结果
var versionString = await context.execute(); 
```

### *关于Context的最终返回值

`context.returnVar` 是 `context` 最终执行完毕返回值的标记

1. 设定context.returnVar: 返回该NativeObject对应的Native变量
2. 不设定context.returnVar: 执行到最后一个invoke，如果有返回值，作为context的最终返回值; 无返回值则返回空值;

```
ObjCContext context = ObjCContext();
context.classFromString('UIDevice').invoke(method: 'currentDevice').invoke(method: 'systemVersion');

// 直接获得原生执行结果
var versionString = await context.execute(); 
```

### 3.支持快捷使用JSON中实例化对象
或许有些时候，我们需要用 `JSON` 直接实例化一个对象.

ObjC代码

```
ClassA *objectA = [ClassA new]; 
objectA.a = 1;
objectA.b = @"sss";
``` 

一般时候，这样写
Dart 代码

```
ObjCContext context = ObjCContext();
NativeObject objectA = context.classFromString('ClassA').invoke(method: 'new');
objectA.invoke(method: 'setA:', args: [1]);
objectA.invoke(method: 'setB:', args: ['sss']);
```
也可以从JSON中生成

```
ObjCContext context = ObjCContext();
NativeObject objectA = context.newNativeObjectFromJSON({'a':1,'b':'sss'}, 'ClassA');
```

## Dart 调用 JAVA
`JAVAContext` 仅在安卓系统中会被实际执行. `JAVAContext` 拥有上述 `ObjCContext` `Dart调ObjC` 的全部特性.

* 支持上下文调用
* 支持链式调用
* 支持用JSON中实例化对象

另外，额外支持了从构造器中实例化一个对象

### 4. 支持快捷使用构造器实例化对象
JAVA代码

```
String platform = new String("android");
``` 

Dart 代码

```
NativeObject version = context
            .newJavaObjectFromConstructor('java.lang.String', ["android "])

```

### *在安卓系统中已知的问题，等待解决 

* 暂时不支持在代码混淆的工程中使用
* 不支持泛型

## 快捷组织双端代码
提供了一个快捷的方法来 初始化和执行 context.

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
可以快速书写两端的原生调用

```
platformVersion = await Dna.traversingNative((ObjCContext context) {
    NativeObject version = context.classFromString('UIDevice').invoke(method: 'currentDevice').invoke(method: 'systemVersion');
    version = context.classFromString("NSString").invoke(method: 'stringWithString:', args: ['iOS-']).invoke(method: 'stringByAppendingString:', args: [version]);
    
    context.returnVar = version; // 该句可省略
}, (JAVAContext context) {
    NativeObject versionId = context.newJavaObjectFromConstructor('com.example.dna_example.DnaTest', null).invoke(method: 'getDnaVersion').invoke(method: 'getVersion');
    NativeObject version = context.newJavaObjectFromConstructor('java.lang.String', ["android "]).invoke(method: "concat", args: [versionId]);
    
    context.returnVar = version; // 该句可省略
});
```

# 原理简介
`dna` 并不涉及` dart对象到Native对象的转换` ，也不关心 `Native对象的生命周期`，而是着重与描述原生方法调用的上下文，在 `context execute` 时通过 `channel` 调用一次原生方法，把调用栈以 `JSON` 的形式传过去供原生动态解析调用。

如前文的中 dart 代码

```
ObjCContext context = ObjCContext();
NativeObject version = context.classFromString('UIDevice').invoke(method: 'currentDevice').invoke(method: 'systemVersion');
version = context.classFromString("NSString").invoke(method: 'stringWithString:', args: ['iOS-']).invoke(method: 'stringByAppendingString:', args: [version]);

context.returnVar = version; // 可省略设定最终返回值, 参考3

// 直接获得原生执行结果
var versionString = await context.execute(); 
```
`NativeContext的execute()` 方法，实际调用了

```
static Future<Object> executeNativeContext(NativeContext context) async {
    return await _channel.invokeMethod('executeNativeContext', context.toJSON());
}
```

在 `原生的 executeNativeContext` 对应执行的方法中，接收到的 `JSON` 是这样的

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
我们在 `Native` 维护了一个 `objectsInContextMap` , `以objectId` 为键，以 `Native对象` 为值。

`_invocationNodes` 便是方法的调用上下文, 看单个

这里会动态调用 `[UIDevice currentDevice]`, 返回对象以 `returnVar中存储的"_objectId_KNWtiPuM" ` 为键放到 `objectsInContextMap` 里

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

这里 `调用方法的对象的objectId` 是 `"_objectId_KNWtiPuM"` ，是上一个方法的返回值，从`objectsInContextMap` 中取出，继续动态调用，以 `returnVar的object_id为键` 存储新的返回值。

```
{
	"returnVar": {
		"_objectId": "_objectId_haPktBlL"
	},
	"object": {
		"_objectId": "_objectId_KNWtiPuM" // 会在objectsInContextMap找到中真正的对象
	},
	"method": "systemVersion"
}
```
方法有参数时，支持自动装包和解包的，如 `int<->NSNumber..`, 如果参数是非 `channel` 规定的15种基本类型，是`NativeObject`, 我们会把对象从 `objectsInContextMap ` 中找出，放到实际的参数列表里

```
{
	"object": {
		"_objectId": "_objectId_UiCMaHAN"
	},
	"method": "stringByAppendingString:",
	"args": [{
		"_objectId": "_objectId_haPktBlL" // 会在objectsInContextMap找到中真正的对象
	}],
	"returnVar": {
		"_objectId": "_objectId_WyWRIsLl"
}
```
...

如果设置了`最终的returnVar`, 将把该 `returnVar objectId` 对应的对象从 `objectsInContextMap` 中找出来，作为 `channel的返回值` 回调回去。如果没有设置，取最后一个 `invocation` 的返回值(如果有)。

## 作者
zyd178591@alibaba-inc.com, zhengguang.zzg@alibaba-inc.com, yongguang.lyg@alibaba-inc.com

## 更新日志
| version | note |
| ------ | ------ | 
| 0.1.0 | 能用 | 

## License

dna is available under the MIT license. See the LICENSE file for more info.

## 其他

* 代码仓库近期会迁移到eleme下
* 欢迎试用，建议和提交代码