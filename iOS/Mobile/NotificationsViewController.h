//
//  NotificationsViewController.h
//  Mobile
//
//  Created by Jason Hocker on 9/27/12.
//  Copyright (c) 2012-2014 Ellucian Company L.P. and its affiliates. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "Module+Attributes.h"
#import "Notification.h"
#import "NotificationDetailViewController.h"
#import "NotificationsFetcher.h"
#import "NotificationNoDescriptionDetailViewController.h"
#import "EllucianUITableViewController.h"
#import "DetailSelectionDelegate.h"

@interface NotificationsViewController : EllucianUITableViewController<NSFetchedResultsControllerDelegate>

@property (strong, nonatomic) Module *module;
@property (strong, nonatomic) NSFetchedResultsController *fetchedResultsController;
@property (nonatomic, assign) id<DetailSelectionDelegate> detailSelectionDelegate;

+ (void)requestNotificationDetailById:(NSString*) notificationId;

@end
