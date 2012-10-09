//
//  AlertService.m
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 12/09/12.
//  Copyright (c) 2012 CSIRO. All rights reserved.
//

#import "AlertService.h"

@implementation AlertService

// handle displaying of error messages
+ (void)DisplayAlertWithError:(NSError *)error title:(NSString *)title {
    NSString *errorMessage = [error localizedDescription];
    
    UIAlertView *alertView =[[UIAlertView alloc] 
                             initWithTitle:title
                             message:errorMessage
                             delegate:nil
                             cancelButtonTitle:@"OK"
                             otherButtonTitles:nil];
    
    [alertView show];
}

// handle displaying of general messages
+ (void)DisplayMessageWithTitle:(NSString *)title message:(NSString *)message {
    UIAlertView *alertView =[[UIAlertView alloc] 
                             initWithTitle:title
                             message:message
                             delegate:nil
                             cancelButtonTitle:@"OK"
                             otherButtonTitles:nil];
    
    [alertView show];
}




@end
