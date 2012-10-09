//
//  Preferences.m
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 12/09/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import "Preferences.h"

@implementation Preferences

-(id)init
{
    defaults = [NSUserDefaults standardUserDefaults];
    [defaults setObject:@"root-uat.ala.org.au" forKey:@"baseURL"];
    [defaults setObject:@"fielddata-proxy" forKey:@"context"];
    [defaults setObject:@"koalacount" forKey:@"path"];
    [defaults setObject:@"Great+Koala+Count" forKey:@"portal"];

    return self;
}


-(NSString*)getPortalName
{
    NSString *portal = [defaults objectForKey:@"portal"];
    return portal;
}

-(NSString*)getFieldDataURL
{
    NSString *url = [defaults objectForKey:@"baseURL"];
    NSString *context = [defaults objectForKey:@"context"];
    NSString *path = [defaults objectForKey:@"path"];
    
    return [NSString stringWithFormat:@"http://%@/%@/%@/", url, context, path];
}

-(void)setFieldDataSessionKey:(NSString*)sessionKey
{
    [defaults setObject:sessionKey forKey:@"sessionKey"];
}

-(NSString*)getFieldDataSessionKey
{
    return [defaults objectForKey:@"sessionKey"];
}

@end
