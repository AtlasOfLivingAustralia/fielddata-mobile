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

@implementation ImageCell

@synthesize label, startCamera, cameraImage, filePath, parentController;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        
        // Initialization code
        cameraImage=[[UIImageView alloc] initWithFrame:CGRectMake(10, 30, 72, 96)];
        cameraImage.autoresizingMask = ( UIViewAutoresizingNone );
        cameraImage.autoresizesSubviews = NO;
        [self.contentView addSubview:cameraImage];
        
        UIImage *cellImage = [UIImage imageNamed:@"image_not_available.png"];
        [cameraImage setImage:cellImage];
        
        label = [[UILabel alloc] initWithFrame:CGRectMake(10, 0, self.bounds.size.width-20, 24)];
        label.font = [UIFont boldSystemFontOfSize:12.0];
        label.numberOfLines = 0;
        [self.contentView addSubview:label];
    
        UIImage *cameraBtn = [UIImage imageNamed:@"camera.png"];
        startCamera = [UIButton buttonWithType:UIButtonTypeCustom];
        [startCamera addTarget:self action:@selector(showCameraUI:) forControlEvents:UIControlEventTouchUpInside];
        startCamera.frame = CGRectMake(120, 50, 48, 48);
        [startCamera setImage:cameraBtn forState:UIControlStateNormal];
        [self.contentView addSubview:startCamera];
        
        filePath = [[NSMutableString alloc]init];
    }
    return self;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
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
    cameraUI.mediaTypes =
    [UIImagePickerController availableMediaTypesForSourceType:
     UIImagePickerControllerSourceTypeCamera];
    
    // Hides the controls for moving & scaling pictures, or for
    // trimming movies. To instead show the controls, use YES.
    cameraUI.allowsEditing = NO;
    cameraUI.delegate = delegate;
    
    [controller presentModalViewController: cameraUI animated: YES];
    
    return YES;
}

// For responding to the user tapping Cancel.

- (void) imagePickerControllerDidCancel: (UIImagePickerController *) picker {
    
    [[picker parentViewController] dismissModalViewControllerAnimated: YES];
}

// For responding to the user accepting a newly-captured picture or movie
/*
- (void) imagePickerController: (UIImagePickerController *) picker
            didFinishPickingMediaWithInfo: (NSDictionary *) info {
    
    NSString *mediaType = [info objectForKey: UIImagePickerControllerMediaType];
    UIImage *originalImage, *editedImage, *imageToSave;
    
    // Handle a still image capture
    if (CFStringCompare ((CFStringRef) mediaType, kUTTypeImage, 0) == kCFCompareEqualTo) {
                
        editedImage = (UIImage *) [info objectForKey:UIImagePickerControllerEditedImage];
        originalImage = (UIImage *) [info objectForKey:UIImagePickerControllerOriginalImage];
        
        if (editedImage) {
            imageToSave = editedImage;
        } else {
            imageToSave = originalImage;
        }
        
        // Save the new image (original or edited) to the Camera Roll
        UIImageWriteToSavedPhotosAlbum (imageToSave, nil, nil, nil);
        //UIImageWriteToSavedPhotosAlbum (imageToSave, self, @selector(image:) , nil);
    }
    // Handle a movie capture
    if (CFStringCompare ((CFStringRef) mediaType, kUTTypeMovie, 0) == kCFCompareEqualTo) {
        NSString *moviePath = [[info objectForKey:UIImagePickerControllerMediaURL] path];
        
        if (UIVideoAtPathIsCompatibleWithSavedPhotosAlbum (moviePath)) {
            UISaveVideoAtPathToSavedPhotosAlbum(moviePath, nil, nil, nil);
        }
        
    }
    [[picker parentViewController] dismissModalViewControllerAnimated: YES];
}*/

-(void) imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary *)info{
    UIImage *image = [info valueForKey:UIImagePickerControllerOriginalImage];
    
    if(image){
        ALAssetsLibrary *library = [[ALAssetsLibrary alloc] init];
        [library writeImageToSavedPhotosAlbum:[image CGImage] orientation:(ALAssetOrientation)[image imageOrientation]
            completionBlock:^
            (NSURL *assetURL, NSError *error){
                if (!error) {
                    [filePath setString:[NSString stringWithFormat:@"%@",assetURL]];
                }
            }];
        if ([image imageOrientation] == UIImageOrientationUp) {
            cameraImage.frame = CGRectMake(10, 30, 96, 72);
        } else {
            cameraImage.frame = CGRectMake(10, 30, 72, 96);
        }
        [cameraImage setImage:image];
    }
    
    [parentController dismissModalViewControllerAnimated: YES];
}


@end
