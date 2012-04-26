/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008-12, Red Hat Middleware LLC, and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.savara.tests.switchyard.beanservice;

import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.savara.bam.activity.model.ActivityUnit;
import org.savara.bam.activity.model.soa.RequestReceived;
import org.savara.bam.activity.model.soa.RequestSent;
import org.savara.bam.activity.model.soa.ResponseReceived;
import org.savara.bam.activity.model.soa.ResponseSent;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class BeanServiceTest {

    @Deployment
    public static WebArchive createDeployment() {
        
        return ShrinkWrap.create(WebArchive.class)
            .addClass(InventoryService.class)
            .addClass(InventoryServiceBean.class)
            .addClass(Item.class)
            .addClass(ItemNotFoundException.class)
            .addClass(Order.class)
            .addClass(OrderAck.class)
            .addClass(OrderService.class)
            .addClass(OrderServiceBean.class)
            .addClass(Transformers.class)
            .addClass(ExchangeInterceptor.class)
            .addClass(TestActivityStore.class)
            .addAsResource("wsdl/OrderService.wsdl")
            .addAsResource("META-INF/switchyard.xml")
            .addAsResource(EmptyAsset.INSTANCE, "META-INF/beans.xml")
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
            .addAsManifestResource("switchyard.xml")
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
            .addAsLibraries(
                    DependencyResolvers
                    .use(MavenDependencyResolver.class)
                    .artifacts("org.savara.bam.activity-management:collector:1.0.0-SNAPSHOT",
                            "org.savara.bam.integration:bam-jbossas7:1.0.0-SNAPSHOT",
                            "org.savara.bam.activity-management:collector-activity-server:1.0.0-SNAPSHOT")
                    .resolveAsFiles());
    }

    @Inject
    org.savara.tests.switchyard.beanservice.OrderService _orderService;

    @Test
    public void submitOrderDirectNoTxn() {

        if (_orderService == null) {
            fail("Order Service has not been set");
        }
        
        Order order=new Order();
        order.setOrderId("abc");
        order.setItemId("BUTTER");
        order.setQuantity(10);
        
        OrderAck ack=_orderService.submitOrder(order);
        
        if (ack == null) {
            fail("Acknowledgement is null");
        } else if (!ack.isAccepted()) {
            fail("Order was not accepted");
        }
        
        // Delay awaiting results
        try {
            Thread.sleep(1000);
        } catch(Exception e) {
            fail("Failed to wait for events: "+e);
        }
        
        // Check that store method only called once
        if (TestActivityStore.getStoreCount() != 1) {
            fail("Store count was not 1: "+TestActivityStore.getStoreCount());
        }
        
        // Check that the four expected activity units occurred
        if (TestActivityStore.getActivities().size() != 4) {
            fail("Activity count should be 4: "+TestActivityStore.getActivities().size());
        }
        
        // Check that each activity unit only has a single event
        for (ActivityUnit au : TestActivityStore.getActivities()) {
            if (au.getActivityTypes().size() != 1) {
                fail("Activity unit does not have single activity type: "+au.getActivityTypes().size());
            }
        }
        
        if (!(TestActivityStore.getActivities().get(0).getActivityTypes().get(0) instanceof RequestSent)) {
            fail("Expected 'RequestSent'");
        }
        
        if (!(TestActivityStore.getActivities().get(1).getActivityTypes().get(0) instanceof RequestReceived)) {
            fail("Expected 'RequestReceived'");
        }
        
        if (!(TestActivityStore.getActivities().get(2).getActivityTypes().get(0) instanceof ResponseSent)) {
            fail("Expected 'ResponseSent'");
        }
        
        if (!(TestActivityStore.getActivities().get(3).getActivityTypes().get(0) instanceof ResponseReceived)) {
            fail("Expected 'ResponseReceived'");
        }
        
        // Check that all the events had different transaction ids, as a
        // transaction manager is available, but the method is not performed
        // within the scope of a transaction
        java.util.List<String> txnIds=new java.util.Vector<String>();
        
        for (ActivityUnit au : TestActivityStore.getActivities()) {
            if (txnIds.contains(au.getOrigin().getTransaction())) {
                fail("Txn id should be unique");
            }
            txnIds.add(au.getOrigin().getTransaction());
        }
    }
}