//
//  AlertService.h
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 12/09/12.
//  Copyright (c) 2012 CSIRO. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface AlertService : NSObject

+ (void)DisplayAlertWithError:(NSError *)error title:(NSString *)title;
+ (void)DisplayMessageWithTitle:(NSString *)title message:(NSString *)message;

@end
