//
//  Preferences.h
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 12/09/12.
//  Copyright (c) 2012 CSIRO. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface Preferences : NSObject {
    
    @private
    NSUserDefaults* defaults;
}

-(NSString*)getPortalName;
-(void)setPortalName:(NSString*)name;
-(NSString*)getFieldDataURL;
-(void)setFieldDataSessionKey:(NSString*)sessionKey;
-(NSString*)getFieldDataSessionKey;
-(void)setUsersName:(NSString*)name;
-(NSString*)getUsersName;

@end
