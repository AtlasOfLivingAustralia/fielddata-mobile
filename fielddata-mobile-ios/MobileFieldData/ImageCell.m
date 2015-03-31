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

@synthesize startCamera, cameraImage, parentController,photopoints, multiPhotoEnabled,locationManager, photoLibrary;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        multiPhotoEnabled = 0;
        // Initialization code
        cameraImage=[[UIImageView alloc] initWithFrame:CGRectMake(10, 30, 48, 48)];
        cameraImage.autoresizingMask = ( UIViewAutoresizingNone );
        cameraImage.autoresizesSubviews = NO;
        [self.contentView addSubview:cameraImage];
        
        UIImage *cellImage = nil; //[UIImage imageNamed:@"image_not_available.png"];
        [cameraImage setImage:cellImage];

        
        UIImage *cameraBtn = [UIImage imageNamed:@"1.png"];
        startCamera = [UIButton buttonWithType:UIButtonTypeCustom];
        [startCamera addTarget:self action:@selector(showCameraUI:) forControlEvents:UIControlEventTouchUpInside];
        NSInteger width = 48;
        startCamera.frame = CGRectMake(self.bounds.size.width-30-width, 30, 48, 48);
        [startCamera setImage:cameraBtn forState:UIControlStateNormal];
        [self.contentView addSubview:startCamera];
        
        UIImage *galleryBtn = [UIImage imageNamed:@"3.png"];
        photoLibrary = [UIButton buttonWithType:UIButtonTypeCustom];
        [photoLibrary addTarget:self action:@selector(showPhotoLibraryUI:) forControlEvents:UIControlEventTouchUpInside];
        photoLibrary.frame = CGRectMake(self.bounds.size.width-30-100, 30, 48, 48);
        [photoLibrary setImage:galleryBtn forState:UIControlStateNormal];
        [self.contentView addSubview:photoLibrary];


    }
    return self;
}

- (id)initWithStyleForPhotopoints:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    
    if (self) {
        multiPhotoEnabled = 1;

        // Remove all unused images.
        NSString *newPath = [[FileService getDocumentsPath] stringByAppendingPathComponent:@"/temp"];
        [FileService deleteFilesInFolder:newPath];
        
        photopoints = [[UILabel alloc] initWithFrame:CGRectMake(10, 30, 148, 48)];
        photopoints.font = [UIFont systemFontOfSize:12.0];
        photopoints.text = @"Total photos taken: 0" ;
        [self.contentView addSubview:photopoints];
        
        
        
        UIImage *cameraBtn = [UIImage imageNamed:@"1.png"];
        startCamera = [UIButton buttonWithType:UIButtonTypeCustom];
        [startCamera addTarget:self action:@selector(showCameraUI:) forControlEvents:UIControlEventTouchUpInside];
        NSInteger width = 48;
        startCamera.frame = CGRectMake(self.bounds.size.width-30-width, 30, 48, 48);
        [startCamera setImage:cameraBtn forState:UIControlStateNormal];
        [self.contentView addSubview:startCamera];
        
        UIImage *galleryBtn = [UIImage imageNamed:@"3.png"];
        photoLibrary = [UIButton buttonWithType:UIButtonTypeCustom];
        [photoLibrary addTarget:self action:@selector(showPhotoLibraryUI:) forControlEvents:UIControlEventTouchUpInside];
        photoLibrary.frame = CGRectMake(self.bounds.size.width-30-100, 30, 48, 48);
        [photoLibrary setImage:galleryBtn forState:UIControlStateNormal];
        [self.contentView addSubview:photoLibrary];

        
        // Track user location.
        locationManager = [[CLLocationManager alloc] init];
        locationManager.distanceFilter = kCLDistanceFilterNone; // whenever we move
        locationManager.desiredAccuracy = kCLLocationAccuracyBest;
        locationManager.delegate = self;
        
        // Check for iOS 8. Without this guard the code will crash with "unknown selector" on iOS 7.
        if ([self.locationManager respondsToSelector:@selector(requestWhenInUseAuthorization)]) {
            [self.locationManager requestWhenInUseAuthorization];
        }
        [locationManager startUpdatingLocation];
    }
    return self;
}

-(void)locationManager:(CLLocationManager *)manager didUpdateToLocation:(CLLocation *)newLocation fromLocation:(CLLocation *)oldLocation
{
    /*  CLLocationManagerDelegate delegate to update new location. */
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
        self.value = imagePath;
    }
    else {
        self.value = @"";
    }
}

- (void)updatePhotopoints:(NSString*)points
{
    if(points != nil && ![points isEqualToString:@""]){
        NSArray *tokens = [points componentsSeparatedByString:@"|"];
        photopoints.text = [[NSString alloc] initWithFormat:@"Total photos taken: %d",[tokens count]-1] ;
        self.value = points;
    }
    else
        self.value = @"";
}

- (IBAction)showCameraUI:(id)sender {
    [self startCameraControllerFromViewController: parentController usingDelegate: self];
}

- (IBAction)showPhotoLibraryUI:(id)sender {
    [self photoLibraryFromViewController: parentController usingDelegate: self];
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

- (BOOL) photoLibraryFromViewController: (UIViewController*) controller
                                   usingDelegate: (id <UIImagePickerControllerDelegate,
                                                   UINavigationControllerDelegate>) delegate {
    
    if (([UIImagePickerController isSourceTypeAvailable:
          UIImagePickerControllerSourceTypeCamera] == NO)
        || (delegate == nil)
        || (controller == nil))
        return NO;
    
    UIImagePickerController *cameraUI = [[UIImagePickerController alloc] init];
    cameraUI.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
    
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

    if(self.multiPhotoEnabled == 1)
        [self multiplePhotoHandler:info];
    else
       [self singlePhotoHandler:info];
    
    [parentController dismissModalViewControllerAnimated: YES];
}

-(void) multiplePhotoHandler :(NSDictionary *)info {
   
    UIImage *image = [info valueForKey:UIImagePickerControllerOriginalImage];
    
    if (image.size.height > 1024 || image.size.width > 1024) {
        image = [image resizedImageWithContentMode:UIViewContentModeScaleAspectFit
                                            bounds:CGSizeMake(1024, 1024)
                              interpolationQuality:kCGInterpolationMedium];
    }
    
    NSData *imageData = UIImagePNGRepresentation(image);
    NSString *imagePath = [FileService getUniqueFileName];
    if (![imageData writeToFile:imagePath atomically:NO])
    {
        NSLog((@"Failed to cache image data to disk"));
    }
    else
    {
        self.value = [[NSString alloc] initWithFormat:@"%@%.20f,%.20f,%@,%@|",self.value,
                      locationManager.location.coordinate.latitude,
                      locationManager.location.coordinate.longitude,
                      @"",
                      [imagePath lastPathComponent]];
        int times = [[self.value componentsSeparatedByString:@"|"] count]-1;
        photopoints.text = [[NSString alloc] initWithFormat:@"Total photos taken = %d",times];
    }
    
}


-(void) singlePhotoHandler :(NSDictionary *)info{
    
    UIImage* image = [info valueForKey:UIImagePickerControllerOriginalImage];
    
    UIImageOrientation orientation = [image imageOrientation];
    
    if (image.size.height > 1024 || image.size.width > 1024) {
        image = [image resizedImageWithContentMode:UIViewContentModeScaleAspectFit
                                            bounds:CGSizeMake(1024, 1024)
                              interpolationQuality:kCGInterpolationMedium];
    }
    
    NSString* fileName = [NSString stringWithFormat:@"%f.jpg", [[NSDate date] timeIntervalSince1970]];
    NSString* filePath = [FileService saveImage:image withName:fileName];
    
    image = nil;
    
    if (orientation == UIImageOrientationUp) {
        cameraImage.frame = CGRectMake(10, 30, 48, 48);
    } else {
        cameraImage.frame = CGRectMake(10, 30, 48, 48);
    }
    
    // reload the image
    [self updateImage:filePath];
}

@end
