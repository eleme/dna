//
//  NSObject+DnaRuntime.h
//  LPDAdditionsKit
//
//  Created by Assuner on 2018/4/13.
//

#import <Foundation/Foundation.h>

@interface NSObject (DnaRuntime)

+ (id)dna_objectWithBuffer:(void *)valueLoc type:(const char *)typeStr;
- (void)dna_getValue:(void *)valueLoc type:(const char *)argType; // id / NSValue Type
- (id)dna_performSelector:(SEL)aSelector withObjects:(NSArray *)objects; // id / NSValue Type

@end

@interface NSInvocation (DnaObjectParams)

- (id)dna_getArgumentObjectAtIndex:(NSInteger)idx;
- (void)dna_setArgumentObject:(id)arguementObject atIndex:(NSInteger)idx;

@end

