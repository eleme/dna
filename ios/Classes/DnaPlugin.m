#import "DnaPlugin.h"
#import <dna/NSObject+dna_runtime.h>

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
    NSObject *returnVar = nil;
    
    for (NSDictionary *varJSON in context[@"_vars"]) {
        [vars setObject:[NSObject new] forKey:varJSON[@"varId"]];
    }
    
    for (NSDictionary *invocationJSON in context[@"_invocationNodes"]) {
        NSObject *object = nil;
        NSDictionary *objectJSON = invocationJSON[@"object"];
        if (objectJSON[@"clsName"]) {
            object = (NSObject *)NSClassFromString(objectJSON[@"clsName"]);
        } else if (objectJSON[@"varId"]) {
            object = vars[objectJSON[@"varId"]];
        } else {
            object = nil;
        }
        
        SEL sel = NSSelectorFromString(invocationJSON[@"method"]);
        
        NSArray *args = nil;
        if (![invocationJSON[@"args"] isEqual:NSNull.null]) {
            args = invocationJSON[@"args"];
        }
        
        NSString *returnVarName = invocationJSON[@"returnVar"][@"varId"];
        id returnVarValue = vars[returnVarName];
        //TODO returnVarValue = [object dna_performSelector:sel withObjects:args];
        if (returnVarValue) {
            vars[returnVarName] = returnVarValue;
        }
    }
    
    if (context[@"returnVar"] != NSNull.null) {
        NSString *returnVarName = context[@"returnVar"][@"varId"];
        returnVar = vars[returnVarName];
    }
    
    if (result) {
        result(returnVar);
    }
}

@end
