/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.client;

import java.util.Queue;

public interface NotificationQueueProvider {
    
    Queue<String> getNotificationQueue();

}
