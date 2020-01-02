//
//  NSObject+DnaRuntime.m
//  LPDAdditionsKit
//
//  Created by Assuner on 2018/4/13.
//

#import "NSObject+DnaRuntime.h"

@implementation NSObject (DnaRuntime)

+ (id)dna_objectWithBuffer:(void *)valueLoc type:(const char *)argType {
  #define RETURN_WRAPPERED_OBJECT(type) \
    do { \
      type val = 0; \
      val = *((type *) valueLoc); \
      return @(val); \
  } while(0);
  
  if (strcmp(argType, @encode(id)) == 0 || strcmp(argType, @encode(Class)) == 0 || strcmp(argType, @encode(void(^)(void))) == 0) {
    return *((__autoreleasing id *)valueLoc);
  } else if (strcmp(argType, @encode(char)) == 0) {
    RETURN_WRAPPERED_OBJECT(char);
  } else if (strcmp(argType, @encode(int)) == 0) {
    RETURN_WRAPPERED_OBJECT(int);
  } else if (strcmp(argType, @encode(short)) == 0) {
    RETURN_WRAPPERED_OBJECT(short);
  } else if (strcmp(argType, @encode(long)) == 0) {
    RETURN_WRAPPERED_OBJECT(long);
  } else if (strcmp(argType, @encode(long long)) == 0) {
    RETURN_WRAPPERED_OBJECT(long long);
  } else if (strcmp(argType, @encode(unsigned char)) == 0) {
    RETURN_WRAPPERED_OBJECT(unsigned char);
  } else if (strcmp(argType, @encode(unsigned int)) == 0) {
    RETURN_WRAPPERED_OBJECT(unsigned int);
  } else if (strcmp(argType, @encode(unsigned short)) == 0) {
    RETURN_WRAPPERED_OBJECT(unsigned short);
  } else if (strcmp(argType, @encode(unsigned long)) == 0) {
    RETURN_WRAPPERED_OBJECT(unsigned long);
  } else if (strcmp(argType, @encode(unsigned long long)) == 0) {
    RETURN_WRAPPERED_OBJECT(unsigned long long);
  } else if (strcmp(argType, @encode(float)) == 0) {
    RETURN_WRAPPERED_OBJECT(float);
  } else if (strcmp(argType, @encode(double)) == 0) {
    RETURN_WRAPPERED_OBJECT(double);
  } else if (strcmp(argType, @encode(BOOL)) == 0) {
    RETURN_WRAPPERED_OBJECT(BOOL);
  } else if (strcmp(argType, @encode(char *)) == 0) {
    RETURN_WRAPPERED_OBJECT(const char *);
  } else {
    return [NSValue valueWithBytes:valueLoc objCType:argType];
  }
}

- (void)dna_getValue:(void *)valueLoc type:(const char *)argType {
#define UNWRAPPER_AND_SET(type, selector) \
do { \
*((type *) valueLoc) = [(id)self selector];\
} while (0)
  
  if (strcmp(argType, @encode(id)) == 0 || strcmp(argType, @encode(Class)) == 0 || strcmp(argType, @encode(void(^)(void))) == 0) {
    *((__autoreleasing id *)valueLoc) = self;
  } else if (strcmp(argType, @encode(char)) == 0) {
    UNWRAPPER_AND_SET(char, charValue);
  } else if (strcmp(argType, @encode(int)) == 0) {
    UNWRAPPER_AND_SET(int, intValue);
  } else if (strcmp(argType, @encode(short)) == 0) {
    UNWRAPPER_AND_SET(short, shortValue);
  } else if (strcmp(argType, @encode(long)) == 0) {
    UNWRAPPER_AND_SET(long, longValue);
  } else if (strcmp(argType, @encode(long long)) == 0) {
    UNWRAPPER_AND_SET(long long, longLongValue);
  } else if (strcmp(argType, @encode(unsigned char)) == 0) {
    UNWRAPPER_AND_SET(unsigned char, unsignedCharValue);
  } else if (strcmp(argType, @encode(unsigned int)) == 0) {
    UNWRAPPER_AND_SET(unsigned int, unsignedIntValue);
  } else if (strcmp(argType, @encode(unsigned short)) == 0) {
    UNWRAPPER_AND_SET(unsigned short, unsignedShortValue);
  } else if (strcmp(argType, @encode(unsigned long)) == 0) {
    UNWRAPPER_AND_SET(unsigned long, unsignedLongValue);
  } else if (strcmp(argType, @encode(unsigned long long)) == 0) {
    UNWRAPPER_AND_SET(unsigned long long, unsignedLongLongValue);
  } else if (strcmp(argType, @encode(float)) == 0) {
    UNWRAPPER_AND_SET(float, floatValue);
  } else if (strcmp(argType, @encode(double)) == 0) {
    UNWRAPPER_AND_SET(double, doubleValue);
  } else if (strcmp(argType, @encode(BOOL)) == 0) {
    UNWRAPPER_AND_SET(BOOL, boolValue);
  } else if (strcmp(argType, @encode(char *)) == 0) {
    *((char **) valueLoc) = (char *)[(id)self UTF8String];
  } else {
    [(NSValue *)self getValue:valueLoc];
  }
}

- (id)dna_performSelector:(SEL)aSelector withObjects:(NSArray *)objects {
  NSMethodSignature *signature = [self methodSignatureForSelector:aSelector];
  if (!signature || objects.count != signature.numberOfArguments - 2) {
    return nil;
  }
  NSInvocation *invocation = [NSInvocation invocationWithMethodSignature:signature];
  for (NSInteger i = 0; i < objects.count; i ++) {
    [invocation dna_setArgumentObject:objects[i] atIndex:i+2];
  }
  invocation.selector = aSelector;
  invocation.target = self;
  [invocation retainArguments];
  [invocation invoke];
  if (!signature.methodReturnLength) {
    return nil;
  } else {
    void *valueLoc = alloca(signature.methodReturnLength);
    [invocation getReturnValue:valueLoc];
    return [NSObject dna_objectWithBuffer:valueLoc type:signature.methodReturnType];
  }
}

@end


@implementation NSInvocation (DnaObjectParams)

- (id)dna_getArgumentObjectAtIndex:(NSInteger)idx {
  #define WRAP_AND_RETURN(type) \
    do { \
      type val = 0; \
      [self getArgument:&val atIndex:idx]; \
      return @(val); \
  } while (0)
  
  const char *argType = [self.methodSignature getArgumentTypeAtIndex:idx];
  if (strcmp(argType, @encode(id)) == 0 || strcmp(argType, @encode(Class)) == 0) {
    __autoreleasing id returnObj;
    [self getArgument:&returnObj atIndex:idx];
    return returnObj;
  } else if (strcmp(argType, @encode(char)) == 0) {
    WRAP_AND_RETURN(char);
  } else if (strcmp(argType, @encode(int)) == 0) {
    WRAP_AND_RETURN(int);
  } else if (strcmp(argType, @encode(short)) == 0) {
    WRAP_AND_RETURN(short);
  } else if (strcmp(argType, @encode(long)) == 0) {
    WRAP_AND_RETURN(long);
  } else if (strcmp(argType, @encode(long long)) == 0) {
    WRAP_AND_RETURN(long long);
  } else if (strcmp(argType, @encode(unsigned char)) == 0) {
    WRAP_AND_RETURN(unsigned char);
  } else if (strcmp(argType, @encode(unsigned int)) == 0) {
    WRAP_AND_RETURN(unsigned int);
  } else if (strcmp(argType, @encode(unsigned short)) == 0) {
    WRAP_AND_RETURN(unsigned short);
  } else if (strcmp(argType, @encode(unsigned long)) == 0) {
    WRAP_AND_RETURN(unsigned long);
  } else if (strcmp(argType, @encode(unsigned long long)) == 0) {
    WRAP_AND_RETURN(unsigned long long);
  } else if (strcmp(argType, @encode(float)) == 0) {
    WRAP_AND_RETURN(float);
  } else if (strcmp(argType, @encode(double)) == 0) {
    WRAP_AND_RETURN(double);
  } else if (strcmp(argType, @encode(BOOL)) == 0) {
    WRAP_AND_RETURN(BOOL);
  } else if (strcmp(argType, @encode(char *)) == 0) {
    WRAP_AND_RETURN(const char *);
  } else if (strcmp(argType, @encode(void (^)(void))) == 0) {
    __unsafe_unretained id block = nil;
    [self getArgument:&block atIndex:idx];
    return [block copy];
  } else {
    NSUInteger valueSize = 0;
    NSGetSizeAndAlignment(argType, &valueSize, NULL);
    unsigned char valueBytes[valueSize];
    [self getArgument:valueBytes atIndex:idx];
    return [NSValue valueWithBytes:valueBytes objCType:argType];
  }
}

- (void)dna_setArgumentObject:(id)arguementObject atIndex:(NSInteger)idx {
  #define PULL_AND_SET(type, selector) \
    do { \
        type val = [arguementObject selector]; \
        [self setArgument:&val atIndex:idx]; \
  } while (0)
  if ([arguementObject isKindOfClass:NSNull.class]) {
    arguementObject = nil;
  }
  const char *argType = [self.methodSignature getArgumentTypeAtIndex:idx];
  if (strcmp(argType, @encode(id)) == 0 || strcmp(argType, @encode(Class)) == 0) {
    [self setArgument:&arguementObject atIndex:idx];
  } else if (strcmp(argType, @encode(char)) == 0) {
    PULL_AND_SET(char, charValue);
  } else if (strcmp(argType, @encode(int)) == 0) {
    PULL_AND_SET(int, intValue);
  } else if (strcmp(argType, @encode(short)) == 0) {
    PULL_AND_SET(short, shortValue);
  } else if (strcmp(argType, @encode(long)) == 0) {
    PULL_AND_SET(long, longValue);
  } else if (strcmp(argType, @encode(long long)) == 0) {
    PULL_AND_SET(long long, longLongValue);
  } else if (strcmp(argType, @encode(unsigned char)) == 0) {
    PULL_AND_SET(unsigned char, unsignedCharValue);
  } else if (strcmp(argType, @encode(unsigned int)) == 0) {
    PULL_AND_SET(unsigned int, unsignedIntValue);
  } else if (strcmp(argType, @encode(unsigned short)) == 0) {
    PULL_AND_SET(unsigned short, unsignedShortValue);
  } else if (strcmp(argType, @encode(unsigned long)) == 0) {
    PULL_AND_SET(unsigned long, unsignedLongValue);
  } else if (strcmp(argType, @encode(unsigned long long)) == 0) {
    PULL_AND_SET(unsigned long long, unsignedLongLongValue);
  } else if (strcmp(argType, @encode(float)) == 0) {
    PULL_AND_SET(float, floatValue);
  } else if (strcmp(argType, @encode(double)) == 0) {
    PULL_AND_SET(double, doubleValue);
  } else if (strcmp(argType, @encode(BOOL)) == 0) {
    PULL_AND_SET(BOOL, boolValue);
  } else if (strcmp(argType, @encode(char *)) == 0) {
    const char *cString = [arguementObject UTF8String];
    [self setArgument:&cString atIndex:idx];
    [self retainArguments];
  } else if (strcmp(argType, @encode(void (^)(void))) == 0) {
    [self setArgument:&arguementObject atIndex:idx];
  } else {
    NSCParameterAssert([arguementObject isKindOfClass:NSValue.class]);
    NSUInteger valueSize = 0;
    NSGetSizeAndAlignment([arguementObject objCType], &valueSize, NULL);
    
    unsigned char valueBytes[valueSize];
    [arguementObject getValue:valueBytes];
    [self setArgument:valueBytes atIndex:idx];
  }
}


@end
