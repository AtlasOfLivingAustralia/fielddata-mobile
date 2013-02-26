//
//  ImageCell.h
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 27/09/12.
//
//

#import <UIKit/UIKit.h>
#import "SurveyInputCell.h"

@interface ImageCell : SurveyInputCell <UIImagePickerControllerDelegate, UINavigationControllerDelegate> {
    
}
@property (nonatomic, retain) UIButton* startCamera;
@property (nonatomic, retain) UIImageView* cameraImage;
@property (nonatomic, retain) UITableViewController* parentController;

- (void)updateImage:(NSString*)imagePath;

@end
