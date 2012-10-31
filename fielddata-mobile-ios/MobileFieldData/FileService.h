//
//  FileService.h
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 26/10/12.
//
//

#import <Foundation/Foundation.h>

@interface FileService : NSObject

+ (NSString*)saveImage:(UIImage *)image withName:(NSString *)name;

@end
