/*
 * 2012-3 Red Hat Inc. and/or its affiliates and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.overlord.rtgov.quickstarts.demos.ordermgmt.logisticsservice;

import org.overlord.rtgov.quickstarts.demos.ordermgmt.model.DeliveryAck;
import org.overlord.rtgov.quickstarts.demos.ordermgmt.model.Order;

public interface LogisticsService {

    DeliveryAck deliver(Order order);
}