//
//  ImageCell.m
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 27/09/12.
//
//

#import "ImageCell.h"
#import <UIKit/UIKit.h>
#import <AssetsLibrary/AssetsLibrary.h>
#import "FileService.h"
#import "UIImage+Resize.h"

@implementation ImageCell

@synthesize startCamera, cameraImage, parentController;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        
        // Initialization code
        cameraImage=[[UIImageView alloc] initWithFrame:CGRectMake(10, 30, 48, 48)];
        cameraImage.autoresizingMask = ( UIViewAutoresizingNone );
        cameraImage.autoresizesSubviews = NO;
        [self.contentView addSubview:cameraImage];
        
        UIImage *cellImage = nil; //[UIImage imageNamed:@"image_not_available.png"];
        [cameraImage setImage:cellImage];
        
        UIImage *cameraBtn = [UIImage imageNamed:@"camera.png"];
        startCamera = [UIButton buttonWithType:UIButtonTypeCustom];
        [startCamera addTarget:self action:@selector(showCameraUI:) forControlEvents:UIControlEventTouchUpInside];
        NSInteger width = 48;
        startCamera.frame = CGRectMake(self.bounds.size.width-30-width, 30, 48, 48);
        [startCamera setImage:cameraBtn forState:UIControlStateNormal];
        [self.contentView addSubview:startCamera];
        
    }
    return self;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}


- (void)updateImage:(NSString*)imagePath
{
    if (imagePath != NULL && ![imagePath isEqualToString:@""]) {
        [cameraImage setImage:[UIImage imageWithContentsOfFile:imagePath]];
    }
}

- (IBAction)showCameraUI:(id)sender {
    [self startCameraControllerFromViewController: parentController usingDelegate: self];
}

- (BOOL) startCameraControllerFromViewController: (UIViewController*) controller
                                   usingDelegate: (id <UIImagePickerControllerDelegate,
                                                   UINavigationControllerDelegate>) delegate {
    
    if (([UIImagePickerController isSourceTypeAvailable:
          UIImagePickerControllerSourceTypeCamera] == NO)
        || (delegate == nil)
        || (controller == nil))
        return NO;
    
    UIImagePickerController *cameraUI = [[UIImagePickerController alloc] init];
    cameraUI.sourceType = UIImagePickerControllerSourceTypeCamera;
    
    // Displays a control that allows the user to choose picture or
    // movie capture, if both are available:
    //cameraUI.mediaTypes =
    //[UIImagePickerController availableMediaTypesForSourceType:
    // UIImagePickerControllerSourceTypeCamera];
    
    // Hides the controls for moving & scaling pictures, or for
    // trimming movies. To instead show the controls, use YES.
    cameraUI.allowsEditing = NO;
    cameraUI.delegate = delegate;
    
    [controller presentModalViewController: cameraUI animated: YES];
    
    return YES;
}

// For responding to the user tapping Cancel.

- (void) imagePickerControllerDidCancel: (UIImagePickerController *) picker {
    
    [parentController dismissModalViewControllerAnimated: YES];
}

-(void) imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary *)info{
    UIImage* image = [info valueForKey:UIImagePickerControllerOriginalImage];
    
    UIImageOrientation orientation = [image imageOrientation];
    
    if (image.size.height > 1024 || image.size.width > 1024) {
        image = [image resizedImageWithContentMode:UIViewContentModeScaleAspectFit
                                            bounds:CGSizeMake(1024, 1024)
                              interpolationQuality:kCGInterpolationMedium];
    } 
    
    NSString* fileName = [NSString stringWithFormat:@"%f.jpg", [[NSDate date] timeIntervalSince1970]];
    self.value = [FileService saveImage:image withName:fileName];
    
    image = nil;
    
    if (orientation == UIImageOrientationUp) {
        cameraImage.frame = CGRectMake(10, 30, 48, 48);
    } else {
        cameraImage.frame = CGRectMake(10, 30, 48, 48);
    }
    
    // reload the image
    [self updateImage:self.value];
    
    //[cameraImage setImage:image];
    
    [parentController dismissModalViewControllerAnimated: YES];
}


@end
