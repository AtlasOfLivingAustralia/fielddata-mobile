//
//  FileService.m
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 26/10/12.
//
//

#import "FileService.h"

@implementation FileService

+ (NSString*)saveImage:(UIImage *)image withName:(NSString *)name {
	
    //save image
    NSData *data = UIImageJPEGRepresentation(image, 1.0);
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask,  YES);
    NSString *documentsDirectory = [paths objectAtIndex:0];
    NSString *fullPath = [documentsDirectory stringByAppendingPathComponent:name];
    [fileManager createFileAtPath:fullPath contents:data attributes:nil];
    return fullPath;
}


+ (BOOL)deleteFilesInFolder:(NSString *)directory {
    
    if(![[NSFileManager defaultManager] fileExistsAtPath:directory])
        return NO;
   
    NSFileManager *fm = [NSFileManager defaultManager];
    NSError *error = nil;
    
    for (NSString *file in [fm contentsOfDirectoryAtPath:directory error:&error]) {
        NSString *imagePath =[directory stringByAppendingPathComponent:[NSString stringWithFormat:@"%@",file]];
        BOOL success = [fm removeItemAtPath:[NSString stringWithFormat:@"%@", imagePath] error:&error];
        if (!success || error) {
            NSLog(@"%@",error);
            return NO;
        }
    }
    return YES;
}

+ (NSString*)getDocumentsPath{
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    return  [paths objectAtIndex:0];
}

+ (NSString*)getTempFolderPath{
    NSString *paths = [FileService getDocumentsPath];
    return [paths stringByAppendingPathComponent:[NSString stringWithFormat:@"temp"]];
}

+ (NSString*)getSavedFolderPath{
    NSString *paths = [FileService getDocumentsPath];
    return [paths stringByAppendingPathComponent:[NSString stringWithFormat:@"saved"]];
}

+ (NSString*)getSavedFilePath: (NSString*) fileName{
    NSString *paths = [FileService getDocumentsPath];
    NSString *newPath = [paths stringByAppendingPathComponent:[NSString stringWithFormat:@"saved"]];
    return [newPath stringByAppendingPathComponent:fileName];
}

+ (BOOL)deleteSavedFile:(NSString *)fileName {
    NSError *error;
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSString *documentsPath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    NSString *savedPath = [documentsPath stringByAppendingPathComponent:@"saved"];
    NSString *filePath = [savedPath stringByAppendingPathComponent:fileName];
    BOOL success = [fileManager removeItemAtPath:filePath error:&error];
    return success;
}

+ (NSString*) getUniqueFileName{
    
    NSString *documentsDirectory = [FileService getDocumentsPath];
    CFUUIDRef newUniqueId = CFUUIDCreate(kCFAllocatorDefault);
    CFStringRef newUniqueIdString = CFUUIDCreateString(kCFAllocatorDefault, newUniqueId);
    NSString *fileName = [[NSString alloc] initWithFormat:@"%@.jpg",(__bridge NSString *)newUniqueIdString];
    NSString *newPath = [documentsDirectory stringByAppendingPathComponent:@"/temp"];
    
    NSError *error;
    if (![[NSFileManager defaultManager] fileExistsAtPath:newPath])
        [[NSFileManager defaultManager] createDirectoryAtPath:newPath withIntermediateDirectories:NO attributes:nil error:&error];
    
    NSString *imagePath =[newPath stringByAppendingPathComponent:[NSString stringWithFormat:@"%@",fileName]];
    CFRelease(newUniqueId);
    CFRelease(newUniqueIdString);
    return imagePath;
}

+ (BOOL) copyFiles: (NSString*) srcFolder : (NSString*) destFolder {

    // Is source folder and desfolder exists?
    NSFileManager *fm = [NSFileManager defaultManager];
    if (![fm fileExistsAtPath:srcFolder])
        return NO;
  
    if (![fm fileExistsAtPath:destFolder]){
        NSError *folderError = nil;
        [[NSFileManager defaultManager] createDirectoryAtPath:destFolder withIntermediateDirectories:NO attributes:nil error:&folderError];
    }
    
    NSError *fileExistsError = nil;
    for (NSString *file in [fm contentsOfDirectoryAtPath:srcFolder error:&fileExistsError]) {
        NSString *srcFile =[srcFolder stringByAppendingPathComponent:[NSString stringWithFormat:@"%@",file]];
        NSString *destFile =[destFolder stringByAppendingPathComponent:[NSString stringWithFormat:@"%@",file]];
        if([fm fileExistsAtPath:srcFile]){
            NSError *copyError = nil;
            if([fm copyItemAtPath:srcFile toPath:destFile error:&copyError]== YES){
                NSError *deleteError = nil;
                BOOL success = [fm removeItemAtPath: srcFile error:&deleteError];
                if (!success || deleteError) {
                    NSLog(@"%@",deleteError);
                }
            }
        }
    }
    return YES;
}
    


@end
