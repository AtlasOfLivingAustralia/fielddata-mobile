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
+ (BOOL)deleteFilesInFolder:(NSString *)directory;
+ (NSString*)getDocumentsPath;
+ (NSString*)getUniqueFileName;
+ (BOOL) copyFiles: (NSString*) srcFolder : (NSString*) destFolder;
+ (NSString*)getSavedFolderPath;
+ (NSString*)getTempFolderPath;
+ (NSString*)getSavedFilePath: (NSString*) fileName;
+ (BOOL)deleteSavedFile:(NSString *)fileName;
@end
