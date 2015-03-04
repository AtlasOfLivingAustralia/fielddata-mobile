//
//  Util.m
//  MobileFieldData
//
//  Created by Sathish Babu Sathyamoorthy on 4/03/2015.
//
//

#import <Foundation/Foundation.h>
#import "FD_Util.h"

@implementation FD_Util

+ (UIColor *) getBackgroundColor
{
    NSString * red = [[NSBundle mainBundle] objectForInfoDictionaryKey: @"Background_Red"];
    NSString * green = [[NSBundle mainBundle] objectForInfoDictionaryKey: @"Background_Green"];
    NSString * blue = [[NSBundle mainBundle] objectForInfoDictionaryKey: @"Background_Blue"];
    UIColor * color;
    if(red && green && blue){
        color = [UIColor colorWithRed:[red doubleValue]/255.0 green:[green doubleValue]/255.0 blue:[blue doubleValue]/255.0 alpha:1.0];
    }
    return color;
}

@end