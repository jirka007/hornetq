/*
 * JBoss, Home of Professional Open Source Copyright 2008, Red Hat Middleware
 * LLC, and individual contributors by the @authors tag. See the copyright.txt
 * in the distribution for a full listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package org.jboss.messaging.jms.server.management.impl;

import static org.jboss.messaging.core.client.management.impl.ManagementHelper.HDR_ATTRIBUTE;
import static org.jboss.messaging.core.client.management.impl.ManagementHelper.HDR_OPERATION_EXCEPTION;
import static org.jboss.messaging.core.client.management.impl.ManagementHelper.HDR_OPERATION_NAME;
import static org.jboss.messaging.core.client.management.impl.ManagementHelper.HDR_OPERATION_PREFIX;
import static org.jboss.messaging.core.client.management.impl.ManagementHelper.HDR_OPERATION_SUCCEEDED;
import static org.jboss.messaging.core.client.management.impl.ManagementHelper.HDR_RESOURCE_NAME;

import javax.jms.JMSException;
import javax.jms.Message;

/*
 * @author <a href="mailto:jmesnil@redhat.com">Jeff Mesnil</a>
 * 
 * @version <tt>$Revision$</tt>
 */
public class JMSManagementHelper
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   public static void putAttribute(final Message message, final String resourceName, final String attribute) throws JMSException
   {
      message.setStringProperty(HDR_RESOURCE_NAME.toString(), resourceName);
      message.setStringProperty(HDR_ATTRIBUTE.toString(), attribute);
   }
   
   public static void putOperationInvocation(final Message message,
                                             final String resourceName,
                                             final String operationName) throws JMSException
   {
      putOperationInvocation(message, resourceName, operationName, (Object[])null);
   }

   public static void putOperationInvocation(final Message message,
                                             final String resourceName,
                                             final String operationName,
                                             final Object... parameters) throws JMSException
   {
      // store the name of the operation...
      message.setStringProperty(HDR_RESOURCE_NAME.toString(), resourceName);
      message.setStringProperty(HDR_OPERATION_NAME.toString(), operationName);
      // ... and all the parameters (preserving their types)
      if (parameters != null)
      {
         for (int i = 0; i < parameters.length; i++)
         {
            Object parameter = parameters[i];
            // use a zero-filled 2-padded index:
            // if there is more than 10 parameters, order is preserved (e.g. 02 will be before 10)
            String key = String.format("%s%02d", HDR_OPERATION_PREFIX, i);
            storeTypedProperty(message, key, parameter);
         }
      }
   }

   public static boolean isOperationResult(final Message message) throws JMSException
   {
      return message.propertyExists(HDR_OPERATION_SUCCEEDED.toString());
   }

   public static boolean isAttributesResult(final Message message) throws JMSException
   {
      return !(isOperationResult(message));
   }

   public static boolean hasOperationSucceeded(final Message message) throws JMSException
   {
      if (!isOperationResult(message))
      {
         return false;
      }
      if (message.propertyExists(HDR_OPERATION_SUCCEEDED.toString()))
      {
         return message.getBooleanProperty(HDR_OPERATION_SUCCEEDED.toString());
      }
      return false;
   }

   public static String getOperationExceptionMessage(final Message message) throws JMSException
   {
      if (message.propertyExists(HDR_OPERATION_EXCEPTION.toString()))
      {
         return message.getStringProperty(HDR_OPERATION_EXCEPTION.toString());
      }
      return null;
   }

   public static void storeTypedProperty(final Message message, final String key, final Object typedProperty) throws JMSException
   {
      if (typedProperty instanceof Void)
      {
         // do not put the returned value if the operation was a procedure
      }
      else if (typedProperty instanceof Boolean)
      {
         message.setBooleanProperty(key, (Boolean)typedProperty);
      }
      else if (typedProperty instanceof Byte)
      {
         message.setByteProperty(key, (Byte)typedProperty);
      }
      else if (typedProperty instanceof Short)
      {
         message.setShortProperty(key, (Short)typedProperty);
      }
      else if (typedProperty instanceof Integer)
      {
         message.setIntProperty(key, (Integer)typedProperty);
      }
      else if (typedProperty instanceof Long)
      {
         message.setLongProperty(key, (Long)typedProperty);
      }
      else if (typedProperty instanceof Float)
      {
         message.setFloatProperty(key, (Float)typedProperty);
      }
      else if (typedProperty instanceof Double)
      {
         message.setDoubleProperty(key, (Double)typedProperty);
      }
      else if (typedProperty instanceof String)
      {
         message.setStringProperty(key, (String)typedProperty);
      }
      else if (typedProperty instanceof String[])
      {
         String str = "L[";
         String[] strings = (String[])typedProperty;
         for (String string : strings)
         {
            str += string + "||";
         }
         message.setStringProperty(key, str);         
      }
      // serialize as a SimpleString
      else
      {
         message.setStringProperty(key, typedProperty.toString());
      }
   }

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
