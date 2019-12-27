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
        
        NSString *retName = invocationJSON[@"ret"][@"varName"];
        id retValue = vars[retName];
        retValue = [object dna_performSelector:sel withObjects:args];
        if (retValue) {
            vars[retName] = retValue;
        }
    }
    
    if (context[@"ret"] != NSNull.null) {
        NSString *retName = context[@"ret"][@"varName"];
        ret = vars[retName];
    }
    
    if (result) {
        result(ret);
    }
}

@end

//{
//    "_invocationNodes" =     (
//                {
//            args = "<null>";
//            method = currentDevice;
//            object =             {
//                clsName = UIDevice;
//            };
//            ret =             {
//                varName = device;
//            };
//        },
//                {
//            args = "<null>";
//            method = systemVersion;
//            object =             {
//                varName = device;
//            };
//            ret =             {
//                varName = systemVersion;
//            };
//        }
//    );
//    "_vars" =     (
//                {
//            varName = device;
//        },
//                {
//            varName = systemVersion;
//        }
//    );
//    ret =     {
//        varName = systemVersion;
//    };
//}
