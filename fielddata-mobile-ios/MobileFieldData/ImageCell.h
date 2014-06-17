//
//  ImageCell.h
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 27/09/12.
//
//

#import <UIKit/UIKit.h>
#import "SurveyInputCell.h"
#import "Annotation.h"

@interface ImageCell : SurveyInputCell <UIImagePickerControllerDelegate, UINavigationControllerDelegate, CLLocationManagerDelegate> {
    int multiPhotoEnabled;
    CLLocationManager *locationManager;
}
@property (nonatomic, retain) UIButton* startCamera;
@property (nonatomic, retain) UIImageView* cameraImage;
@property (nonatomic, retain) UILabel* photopoints;
@property (nonatomic, retain) UITableViewController* parentController;
@property (nonatomic, assign) int multiPhotoEnabled;
@property (nonatomic, retain) NSMutableArray* allPics;
@property (nonatomic, retain) CLLocationManager* locationManager;

- (void)updateImage:(NSString*)imagePath;
- (void)updatePhotopoints:(NSString*)points;
- (id)initWithStyleForPhotopoints:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier;
@end
