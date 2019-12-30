#import "DnaPlugin.h"
#import <dna/NSObject+DnaRuntime.h>

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


//{
//    "_invocationNodes" =     (
//                {
//            args = "<null>";
//            method = currentDevice;
//            object =             {
//                clsName = UIDevice;
//            };
//            returnVar =             {
//                varId = "varId_vxgCygex";
//            };
//        },
//                {
//            args = "<null>";
//            method = systemVersion;
//            object =             {
//                varId = "varId_vxgCygex";
//            };
//            returnVar =             {
//                varId = "varId_eTkywOHI";
//            };
//        }
//    );
//    "_vars" =     (
//                {
//            varId = "varId_vxgCygex";
//        },
//                {
//            varId = "varId_eTkywOHI";
//        }
//    );
//    returnVar =     {
//        varId = "varId_eTkywOHI";
//    };
//}

NSString * const dna_invocationNodes = @"_invocationNodes";
NSString * const dna_object = @"object";
NSString * const dna_clsName = @"clsName";
NSString * const dna_method = @"method";
NSString * const dna_args = @"args";

NSString * const dna_returnVar = @"returnVar";
NSString * const dna_varId = @"varId";

- (void)executeNativeContext:(NSDictionary *)context result:(FlutterResult)result {
    NSMutableDictionary *varsInContext = [NSMutableDictionary dictionary];
    
    NSDictionary *returnVarJSON = context[dna_returnVar];
    BOOL hasSetReturnVar = dna_isAvailable(returnVarJSON);
    NSString *returnVarId = nil;
    if (hasSetReturnVar) {
        returnVarId = dna_getVarId(returnVarJSON);
    }
    
    NSArray *_invocationNodesJSON = context[dna_invocationNodes];
    for (NSUInteger i = 0; i < _invocationNodesJSON.count; i++) {
        NSDictionary *invocationJSON = _invocationNodesJSON[i];
        
        NSObject *object = nil;
        NSDictionary *objectJSON = invocationJSON[dna_object];
        if (dna_isAvailable(objectJSON[dna_clsName])) {
            object = (NSObject *)NSClassFromString(objectJSON[dna_clsName]);
        } else if (dna_isAvailable(dna_getVarId(objectJSON))) {
            object = varsInContext[dna_getVarId(objectJSON)];
        } else {
            object = nil;
        }
        
        SEL sel = NSSelectorFromString(invocationJSON[dna_method]);
        
        NSArray *args = nil;
        if (dna_isAvailable(invocationJSON[dna_args])) {
            args = invocationJSON[dna_args];
        }
        
        NSString *invacationReturnVarId = dna_getVarId(invocationJSON[dna_returnVar]);
        
        id invocationReturnVar = [object dna_performSelector:sel withObjects:args];
        if (invocationReturnVar) {
            varsInContext[invacationReturnVarId] = invocationReturnVar;
            if (!hasSetReturnVar && (i == _invocationNodesJSON.count - 1)) {
                returnVarId = invacationReturnVarId;
            }
        }
    }
    
    id returnVar = nil;
    if (returnVarId) {
        returnVar = varsInContext[returnVarId];
    }
    
    if (result) {
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
