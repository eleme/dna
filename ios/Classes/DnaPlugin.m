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



NSString * const dna_varId = @"_varId";
NSString * const dna_clsName = @"clsName";


NSString * const dna_jsonVars = @"_jsonVars";
NSString * const dna_json = @"json";
NSString * const dna_cls = @"cls";

NSString * const dna_invocationNodes = @"_invocationNodes";
NSString * const dna_object = @"object";
NSString * const dna_method = @"method";
NSString * const dna_args = @"args";

NSString * const dna_returnVar = @"returnVar";

- (void)executeNativeContext:(NSDictionary *)context result:(FlutterResult)result {
    // key : _varId, value: object
    NSMutableDictionary<NSString *, id> *varsInContextMap = [NSMutableDictionary dictionary];
    
    // 根据json和类解析为对象，以_varId为键，加入到varsInContextMap中
    NSArray *_jsonVarsJSON = context[dna_jsonVars];
    for (NSDictionary *jsonVarJSON in _jsonVarsJSON) {
        NSDictionary *json = jsonVarJSON[dna_json];
        NSString *cls = jsonVarJSON[dna_cls];
        id jsonVar = [NSClassFromString(cls) yy_modelWithJSON:json];
        if (jsonVar) {
            NSString *varId = dna_getVarId(jsonVarJSON);
            varsInContextMap[varId] = jsonVar;
        }
    }
    
    // 得到returnVar对应的_varId
    NSDictionary *returnVarJSON = context[dna_returnVar];
    BOOL hasSetReturnVar = dna_isAvailable(returnVarJSON);
    NSString *returnVarId = nil;
    if (hasSetReturnVar) {
        returnVarId = dna_getVarId(returnVarJSON);
    }
    
    // 陆续调用所有Invocation
    NSArray *_invocationNodesJSON = context[dna_invocationNodes];
    for (NSUInteger i = 0; i < _invocationNodesJSON.count; i++) {
        NSDictionary *invocationJSON = _invocationNodesJSON[i];
        
        // 得到当前调用方法的对象，类或实例;
        NSObject *object = nil;
        NSDictionary *objectJSON = invocationJSON[dna_object];
        if (dna_isAvailable(objectJSON[dna_clsName])) {
            // 根据类名获取到类
            object = (NSObject *)NSClassFromString(objectJSON[dna_clsName]);
        } else if (dna_isAvailable(dna_getVarId(objectJSON))) {
            // 根据_varId得到实例
            object = varsInContextMap[dna_getVarId(objectJSON)];
        } else {
            object = nil;
        }
        
        // 获取当前selector
        SEL sel = NSSelectorFromString(invocationJSON[dna_method]);
        
        // 处理获得当前所有参数
        NSArray *argsJSON = invocationJSON[dna_args];
        NSMutableArray *args = [NSMutableArray array];
        if (dna_isAvailable(argsJSON)) {
            for (id arg in argsJSON) {
                if ([arg isKindOfClass:NSDictionary.class] && dna_getVarId(arg)) {
                    // 如果含有varId, 根据_varId得到实例
                    id varInContext = varsInContextMap[dna_getVarId(arg)];
                    if (varInContext) {
                        [args addObject:varInContext];
                    }
                } else {
                    // channel 约定的基本类型，直接添加
                    [args addObject:arg];
                }
            }
        }
        
        // 当前Invocation的返回值_varId;
        NSString *invacationReturnVarId = dna_getVarId(invocationJSON[dna_returnVar]);
        
        // 执行Invocation;
        id invocationReturnVar = [object dna_performSelector:sel withObjects:args];
        if (invocationReturnVar) {
            // 当前invocation有返回值，把返回值 返回值_varId 加入到varsInContextMap中
            varsInContextMap[invacationReturnVarId] = invocationReturnVar;
            if (!hasSetReturnVar && (i == _invocationNodesJSON.count - 1)) {
                // 如果context没有设置返回值_varId，以最后一个invocation的返回值_varId作为context 返回值_varId
                returnVarId = invacationReturnVarId;
            }
        }
    }
    
    id returnVar = nil;
    if (returnVarId) {
        // 取得context的返回值
        returnVar = varsInContextMap[returnVarId];
    }
    
    if (result) {
        // 回调给dart返回值
        result(returnVar);
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

NS_INLINE NSString *dna_getVarId(NSDictionary *nativeVarJSON) {
    return nativeVarJSON[dna_varId];
}

@end
