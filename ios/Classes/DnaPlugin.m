#import "DnaPlugin.h"

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
      [self executeNativeContext:call.arguments resultresult:result];
  } else {
    result(FlutterMethodNotImplemented);
  }
}


- (void)executeNativeContext:(NSDictionary *)context resultresult:(FlutterResult)result {
    NSMutableDictionary *vars = [NSMutableDictionary dictionary];
    NSObject *ret = nil;
    
    for (NSDictionary *varJSON in context[@"_vars"]) {
        [vars setObject:[NSObject new] forKey:varJSON[@"varName"]];
    }
    
    for (NSDictionary *invocationJSON in context[@"_invocationNodes"]) {
        NSObject *object = nil;
        NSDictionary *objectJSON = invocationJSON[@"object"];
        if (objectJSON[@"clsName"]) {
            object = (NSObject *)NSClassFromString(objectJSON[@"clsName"]);
        } else if (objectJSON[@"varName"]) {
            object = vars[objectJSON[@"varName"]];
        } else {
            object = nil;
        }
        
        SEL sel = NSSelectorFromString(invocationJSON[@"method"]);
        
        NSArray *args = nil;
        if (![invocationJSON[@"args"] isEqual:NSNull.null]) {
            args = invocationJSON[@"args"];
        }
        
        NSMethodSignature *signature = [object methodSignatureForSelector:sel];
        NSInvocation *invocation = [NSInvocation invocationWithMethodSignature:signature];
        invocation.target = object;
        invocation.selector = sel;
        [invocation invoke];
        if (signature.methodReturnLength > 0) {
            NSString *retName = invocationJSON[@"ret"][@"varName"];
            id retValue = vars[retName];
            [invocation getReturnValue:&retValue];
            vars[retName] = retValue;
            NSLog(@"xxx");
        }
    }
    
    if (context[@"ret"]) {
        NSString *retName = context[@"ret"][@"varName"];
        ret = vars[retName];
    }
    
    if (result) {
        result(ret);
    }
}

@end
