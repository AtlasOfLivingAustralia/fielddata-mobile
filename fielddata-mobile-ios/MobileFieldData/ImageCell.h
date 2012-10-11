//
//  ImageCell.h
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 27/09/12.
//
//

#import <UIKit/UIKit.h>

@interface ImageCell : UITableViewCell <UIImagePickerControllerDelegate, UINavigationControllerDelegate> {
    
}

@property (nonatomic, retain) UILabel* label;
@property (nonatomic, retain) UIButton* startCamera;
@property (nonatomic, retain) UIImageView* cameraImage;
@property (nonatomic, retain) NSMutableString* filePath;
@property (nonatomic, retain) UITableViewController* parentController;

- (void)setImage:(NSString*)imagePath;

@end
