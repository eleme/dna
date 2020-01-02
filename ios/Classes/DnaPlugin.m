#import "DnaPlugin.h"
#import <dna/NSObject+DnaRuntime.h>
#import <YYModel/YYModel.h>

//@interface ClassA : NSObject
//@property (nonatomic) NSInteger a;
//@property (nonatomic) NSInteger b;
//@end
//@implementation ClassA
//@end
//
//@interface ClassB : NSObject
//@property (nonatomic) NSInteger c;
//@end
//@implementation ClassB
//
//- (NSInteger)sum:(ClassA *)objectA {
//    return self.c + objectA.a + objectA.b;
//}
//@end

@implementation DnaPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"dna"
            binaryMessenger:[registrar messenger]];
  DnaPlugin* instance = [[DnaPlugin alloc] init];
  [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  if ([@"getPlatformVersion" isEqualToString:call.method]) {
    result([@"iOS " stringByAppendingString:[[UIDevice currentDevice] systemVersion]]);
  } else if ([@"executeNativeContext" isEqualToString:call.method]) {
      [self executeNativeContext:call.arguments result:result];
  } else {
    result(FlutterMethodNotImplemented);
  }
}



NSString * const dna_objectId = @"_objectId";
NSString * const dna_clsName = @"clsName";

NSString * const dna_objectJSONWrappers = @"_objectJSONWrappers";
NSString * const dna_json = @"json";
NSString * const dna_cls = @"cls";

NSString * const dna_invocationNodes = @"_invocationNodes";
NSString * const dna_object = @"object";
NSString * const dna_method = @"method";
NSString * const dna_args = @"args";

NSString * const dna_returnVar = @"returnVar";

- (void)executeNativeContext:(NSDictionary *)context result:(FlutterResult)result {
    // key : _objectId, value: object
    NSMutableDictionary<NSString *, id> *objectsInContextMap = [NSMutableDictionary dictionary];
    
    // 直接根据objectJSONWrapper生成对象，以_objectId为键，加入到objectsInContextMap中
    NSArray *_objectJSONWrappers = context[dna_objectJSONWrappers];
    for (NSDictionary *objectJSONWrapper in _objectJSONWrappers) {
        NSDictionary *json = objectJSONWrapper[dna_json];
        NSString *cls = objectJSONWrapper[dna_cls];
        id object = [NSClassFromString(cls) yy_modelWithJSON:json];
        if (object) {
            NSString *objectId = dna_getObjectId(objectJSONWrapper);
            objectsInContextMap[objectId] = object;
        }
    }
    
    // 得到context returnVar对应的_objectId
    NSDictionary *contextReturnVar = context[dna_returnVar];
    BOOL hasContextReturnVarFlag = dna_isAvailable(contextReturnVar);
    NSString *contextReturnVarObjectId = nil;
    if (hasContextReturnVarFlag) {
        contextReturnVarObjectId = dna_getObjectId(contextReturnVar);
    }
    
    // 陆续调用所有Invocation
    NSArray *_invocationNodes = context[dna_invocationNodes];
    for (NSUInteger i = 0; i < _invocationNodes.count; i++) {
        NSDictionary *invocation = _invocationNodes[i];
        
        // 得到当前调用方法的对象，类或实例;
        NSObject *object = nil;
        NSDictionary *objectInfo = invocation[dna_object];
        if (dna_isAvailable(dna_getObjectId(objectInfo))) {
            // 根据_objectId得到，可能是其他invocation的返回值 / 从objectJSONWrapper生成
            object = objectsInContextMap[dna_getObjectId(objectInfo)];
            if (!object && dna_isAvailable(objectInfo[dna_clsName])) {
                // 根据类名获取到类对象，并加入到变量表里
                object = (id)NSClassFromString(objectInfo[dna_clsName]);
                if (object) {
                    objectsInContextMap[dna_getObjectId(objectInfo)] = object;
                }
            }
        }
        
        // 获取当前selector
        SEL sel = NSSelectorFromString(invocation[dna_method]);
        
        // 处理获得当前所有参数
        NSArray *invocationArgs = invocation[dna_args];
        NSMutableArray *absoluteArgs = [NSMutableArray array];
        if (dna_isAvailable(invocationArgs)) {
            for (id arg in invocationArgs) {
                if ([arg isKindOfClass:NSDictionary.class] && dna_getObjectId(arg)) {
                    // 如果含有_objectId, 根据_objectId得到实例
                    id argInContext = objectsInContextMap[dna_getObjectId(arg)];
                    if (argInContext) {
                        [absoluteArgs addObject:argInContext];
                    }
                } else {
                    // channel 约定的基本类型，直接添加
                    [absoluteArgs addObject:arg];
                }
            }
        }
        
        // 当前Invocation的返回值_objectId;
        NSString *invocationReturnVarObjectId = dna_getObjectId(invocation[dna_returnVar]);
        
        // 执行Invocation;
        id invocationReturnVar = [object dna_performSelector:sel withObjects:absoluteArgs];
        if (invocationReturnVar) {
            // 当前invocation有返回值，把返回值 返回值_objectId 加入到objectsInContextMap中
            objectsInContextMap[invocationReturnVarObjectId] = invocationReturnVar;
            if (!hasContextReturnVarFlag && (i == _invocationNodes.count - 1)) {
                // 如果context没有设置返回值_objectId，以最后一个invocation的返回值_objectId作为context 返回值_objectId
                contextReturnVarObjectId = invocationReturnVarObjectId;
            }
        }
    }
    
    id contextReturnValue = nil;
    if (contextReturnVarObjectId) {
        // 取得context的返回值
        contextReturnValue = objectsInContextMap[contextReturnVarObjectId];
    }
    
    if (result) {
        // 回调给dart返回值
        result(contextReturnValue);
    }
}

NS_INLINE BOOL dna_isAvailable(id arg) {
    return arg && ![arg isKindOfClass:NSNull.class];
}

NS_INLINE NSString *dna_getObjectId(NSDictionary *nativeVarJSON) {
    return nativeVarJSON[dna_objectId];
}

@end
