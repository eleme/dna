#import "DnaPlugin.h"
#import <dna/NSObject+DnaRuntime.h>
#import <YYModel/YYModel.h>

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
    
    // 直接根据objectJSONWrapper生成对象，以_objectId为键，加入到varsInContextMap中
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
    
    // 得到returnVar对应的_objectId
    NSDictionary *returnVar = context[dna_returnVar];
    BOOL hasReturnVarFlag = dna_isAvailable(returnVar);
    NSString *returnVarObjectId = nil;
    if (hasReturnVarFlag) {
        returnVarObjectId = dna_getObjectId(returnVar);
    }
    
    // 陆续调用所有Invocation
    NSArray *_invocationNodes = context[dna_invocationNodes];
    for (NSUInteger i = 0; i < _invocationNodes.count; i++) {
        NSDictionary *invocation = _invocationNodes[i];
        
        // 得到当前调用方法的对象，类或实例;
        NSObject *object = nil;
        NSDictionary *objectJSON = invocation[dna_object];
        if (dna_isAvailable(dna_getObjectId(objectJSON))) {
            // 根据_objectId得到，可能是其他invocation的返回值 / 从objectJSONWrapper生成
            object = objectsInContextMap[dna_getObjectId(objectJSON)];
            if (!object && dna_isAvailable(objectJSON[dna_clsName])) {
                // 根据类名获取到类对象，并加入到变量表里
                object = (id)NSClassFromString(objectJSON[dna_clsName]);
                if (object) {
                    objectsInContextMap[dna_getObjectId(objectJSON)] = object;
                }
            }
        }
        
        // 获取当前selector
        SEL sel = NSSelectorFromString(invocation[dna_method]);
        
        // 处理获得当前所有参数
        NSArray *argsJSON = invocation[dna_args];
        NSMutableArray *args = [NSMutableArray array];
        if (dna_isAvailable(argsJSON)) {
            for (id arg in argsJSON) {
                if ([arg isKindOfClass:NSDictionary.class] && dna_getObjectId(arg)) {
                    // 如果含有varId, 根据_objectId得到实例
                    id varInContext = objectsInContextMap[dna_getObjectId(arg)];
                    if (varInContext) {
                        [args addObject:varInContext];
                    }
                } else {
                    // channel 约定的基本类型，直接添加
                    [args addObject:arg];
                }
            }
        }
        
        // 当前Invocation的返回值_objectId;
        NSString *invacationReturnVarId = dna_getObjectId(invocation[dna_returnVar]);
        
        // 执行Invocation;
        id invocationReturnVar = [object dna_performSelector:sel withObjects:args];
        if (invocationReturnVar) {
            // 当前invocation有返回值，把返回值 返回值_objectId 加入到varsInContextMap中
            objectsInContextMap[invacationReturnVarId] = invocationReturnVar;
            if (!hasReturnVarFlag && (i == _invocationNodes.count - 1)) {
                // 如果context没有设置返回值_objectId，以最后一个invocation的返回值_objectId作为context 返回值_objectId
                returnVarObjectId = invacationReturnVarId;
            }
        }
    }
    
    id returnValue = nil;
    if (returnVarObjectId) {
        // 取得context的返回值
        returnValue = objectsInContextMap[returnVarObjectId];
    }
    
    if (result) {
        // 回调给dart返回值
        result(returnValue);
    }
}

NS_INLINE BOOL dna_isAvailable(id arg) {
    static NSNull *null;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        null = NSNull.null;
    });
    return arg && ![arg isEqual:null];
}

NS_INLINE NSString *dna_getObjectId(NSDictionary *nativeVarJSON) {
    return nativeVarJSON[dna_objectId];
}

@end
