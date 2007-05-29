/*
 * Copyright 2006 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.modules.xfire_client;

import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.XFireRuntimeException;
import org.codehaus.xfire.aegis.MessageReader;
import org.codehaus.xfire.aegis.MessageWriter;
import org.codehaus.xfire.aegis.type.Type;
import org.codehaus.xfire.fault.XFireFault;

import javax.xml.namespace.QName;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * An XFire type for a generated wrapper bean.
 *
 * @author Ryan Heaton
 * @see GeneratedWrapperBean
 */
public class GeneratedWrapperBeanType extends Type {

  private final Class beanClass;
  private final PropertyDescriptor[] beanProperties;

  public GeneratedWrapperBeanType(Class beanType) {
    this.beanClass = beanType;
    setTypeClass(beanType);

    try {
      GeneratedWrapperBean emptyInstance = (GeneratedWrapperBean) this.beanClass.newInstance();
      setSchemaType(emptyInstance.getWrapperQName());
      ArrayList beanProperties = new ArrayList();
      PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(beanType, Object.class).getPropertyDescriptors();
      for (int i = 0; i < propertyDescriptors.length; i++) {
        PropertyDescriptor pd = propertyDescriptors[i];
        if ((pd.getReadMethod() != null) && (pd.getWriteMethod() != null)) {
          beanProperties.add(pd);
        }
      }
      this.beanProperties = (PropertyDescriptor[]) beanProperties.toArray(new PropertyDescriptor[beanProperties.size()]);
    }
    catch (Exception e) {
      throw new XFireRuntimeException("Error getting info for wrapper bean " + beanType.getName(), e);
    }

  }

  // Inherited.
  public Object readObject(MessageReader reader, MessageContext context) throws XFireFault {
    Object instance;
    try {
      instance = beanClass.newInstance();
    }
    catch (Exception e) {
      throw new XFireFault("Unable to instantiate the wrapper bean " + beanClass.getName(), e, XFireFault.RECEIVER);
    }

    while (reader.hasMoreElementReaders()) {
      MessageReader elementReader = reader.getNextElementReader();
      String elementName = elementReader.getLocalName();
      PropertyDescriptor pd = findProperty(elementName);
      if (pd != null) {
        Method getter = pd.getReadMethod();
        Class propertyType = getter.getReturnType();
        if (Map.class.isAssignableFrom(propertyType)) {
          try {
            Method putIn = getPutInMethod(pd.getName());
            Class keyType = putIn.getParameterTypes()[0];
            Class valueType = putIn.getParameterTypes()[1];
            while (elementReader.hasMoreElementReaders()) {
              MessageReader entryReader = elementReader.getNextElementReader();

              MessageReader keyReader = entryReader.getNextElementReader();
              Object key = getTypeMapping().getType(keyType).readObject(keyReader, context);
              MessageReader valueReader = entryReader.getNextElementReader();
              Object value = getTypeMapping().getType(valueType).readObject(valueReader, context);
              putIn.invoke(instance, new Object[]{key, value});
            }
          }
          catch (Exception e) {
            throw new XFireFault("Unable to put in map property " + pd.getName() + " for the wrapper bean " + beanClass.getName(), e, XFireFault.RECEIVER);
          }
        }
        else if (propertyType.isArray()) {
          Object item = getTypeMapping().getType(propertyType.getComponentType()).readObject(elementReader, context);
          try {
            Method addTo = getAddToMethod(pd.getName());
            addTo.invoke(instance, new Object[]{item});
          }
          catch (Exception e) {
            throw new XFireFault("Unable to add to property " + pd.getName() + " for the wrapper bean " + beanClass.getName(), e, XFireFault.RECEIVER);
          }
        }
        else if (Collection.class.isAssignableFrom(propertyType)) {
          try {
            Method addTo = getAddToMethod(pd.getName());
            Class componentType = addTo.getParameterTypes()[0];
            Object item = getTypeMapping().getType(componentType).readObject(elementReader, context);
            addTo.invoke(instance, new Object[]{item});
          }
          catch (Exception e) {
            throw new XFireFault("Unable to add to property " + pd.getName() + " for the wrapper bean " + beanClass.getName(), e, XFireFault.RECEIVER);
          }
        }
        else {
          Object value = getTypeMapping().getType(propertyType).readObject(elementReader, context);

          try {
            pd.getWriteMethod().invoke(instance, new Object[]{value});
          }
          catch (Exception e) {
            throw new XFireFault("Unable to invoke " + pd.getWriteMethod() + " for the wrapper bean " + beanClass.getName(), e, XFireFault.RECEIVER);
          }
        }
      }
    }

    return instance;
  }

  /**
   * Get the "addTo" method for the specified property.
   *
   * @param propertyName The property name.
   * @return The addToMethod
   */
  protected Method getAddToMethod(String propertyName) throws NoSuchMethodException {
    propertyName = Character.toString(propertyName.charAt(0)).toUpperCase() + propertyName.substring(1);
    Method[] methods = beanClass.getMethods();
    for (int i = 0; i < methods.length; i++) {
      Method method = methods[i];
      if (("addTo" + propertyName).equals(method.getName())) {
        return method;
      }
    }

    throw new NoSuchMethodException();
  }

  /**
   * Get the "putIn" method for the specified property.
   *
   * @param propertyName The property name.
   * @return The putInMethod
   */
  protected Method getPutInMethod(String propertyName) throws NoSuchMethodException {
    propertyName = Character.toString(propertyName.charAt(0)).toUpperCase() + propertyName.substring(1);
    Method[] methods = beanClass.getMethods();
    for (int i = 0; i < methods.length; i++) {
      Method method = methods[i];
      if (("putIn" + propertyName).equals(method.getName())) {
        return method;
      }
    }

    throw new NoSuchMethodException();
  }

  /**
   * Finds a property by name.
   *
   * @param name The name of the property to find.
   * @return The found property.
   */
  protected PropertyDescriptor findProperty(String name) {
    for (int i = 0; i < beanProperties.length; i++) {
      PropertyDescriptor property = beanProperties[i];
      if (property.getName().equals(name)) {
        return property;
      }
    }

    return null;
  }

  // Inherited.
  public void writeObject(Object object, MessageWriter writer, MessageContext context) throws XFireFault {
    QName wrapperQName = ((GeneratedWrapperBean) object).getWrapperQName();
    for (int n = 0; n < beanProperties.length; n++) {
      PropertyDescriptor property = beanProperties[n];
      Method getter = property.getReadMethod();
      Object propertyValue;
      try {
        propertyValue = getter.invoke(object, null);
      }
      catch (Exception e) {
        throw new XFireFault("Unable to invoke " + getter.getName() + " for the wrapper bean " + beanClass.getName(), e, XFireFault.RECEIVER);
      }

      if (propertyValue != null) {
        Class propertyType = getter.getReturnType();
        QName propertyQName = new QName(wrapperQName.getNamespaceURI(), property.getName());
        if (Map.class.isAssignableFrom(propertyType)) {
          Class keyClass;
          Class valueClass;
          try {
            Class[] paramTypes = getPutInMethod(property.getName()).getParameterTypes();
            keyClass = paramTypes[0];
            valueClass = paramTypes[1];
          }
          catch (NoSuchMethodException e) {
            throw new XFireFault("Unable to map property " + property.getName() + " for the wrapper bean: unknown key/value types." + beanClass.getName(), e, XFireFault.RECEIVER);
          }
          Type keyType = getTypeMapping().getType(keyClass);
          Type valueType = getTypeMapping().getType(valueClass);
          Iterator it = ((Map) propertyValue).entrySet().iterator();
          while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            MessageWriter entryWriter = writer.getElementWriter("entry");
            MessageWriter keyWriter = entryWriter.getElementWriter("key");
            Object key = entry.getKey();
            keyType.writeObject(key, keyWriter, context);
            keyWriter.close();
            MessageWriter valueWriter = entryWriter.getElementWriter("value");
            Object value = entry.getValue();
            valueType.writeObject(value, valueWriter, context);
            valueWriter.close();
            entryWriter.close();
          }
        }
        else if (propertyType.isArray()) {
          Class componentType = propertyType.getComponentType();
          Type type = getTypeMapping().getType(componentType);
          int length = Array.getLength(propertyValue);
          for (int i = 0; i < length; i++) {
            MessageWriter propertyWriter = writer.getElementWriter(propertyQName);
            Object item = Array.get(propertyValue, i);
            type.writeObject(item, propertyWriter, context);
            propertyWriter.close();
          }
        }
        else if (Collection.class.isAssignableFrom(propertyType)) {
          Class componentType;
          try {
            componentType = getAddToMethod(property.getName()).getParameterTypes()[0];
          }
          catch (NoSuchMethodException e) {
            throw new XFireFault("Unable to map property " + property.getName() + " for the wrapper bean: unknown component type." + beanClass.getName(), e, XFireFault.RECEIVER);
          }
          Type type = getTypeMapping().getType(componentType);

          Iterator it = ((Collection) propertyValue).iterator();
          while (it.hasNext()) {
            Object item = it.next();
            MessageWriter propertyWriter = writer.getElementWriter(propertyQName);
            type.writeObject(item, propertyWriter, context);
            propertyWriter.close();
          }
        }
        else {
          MessageWriter propertyWriter = writer.getElementWriter(propertyQName);
          Type type = getTypeMapping().getType(propertyType);
          type.writeObject(propertyValue, propertyWriter, context);
          propertyWriter.close();
        }
      }
    }
    writer.close();
  }

}