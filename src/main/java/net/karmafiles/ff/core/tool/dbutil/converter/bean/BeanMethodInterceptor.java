package net.karmafiles.ff.core.tool.dbutil.converter.bean;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import net.karmafiles.ff.core.tool.dbutil.converter.BaseProxy;
import net.karmafiles.ff.core.tool.dbutil.converter.BeanDescription;
import net.karmafiles.ff.core.tool.dbutil.converter.MapClasses;
import net.karmafiles.ff.core.tool.dbutil.converter.MongoConverter;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.commons.beanutils.PropertyUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.util.*;

/**
 * Created by Ilya Brodotsky
 * Date: 09.10.2010
 * Time: 17:38:22
 * <p/>
 * All rights reserved.
 * <p/>
 * Contact me:
 * email, jabber: ilya.brodotsky@gmail.com
 * skype: ilya.brodotsky
 */

public abstract class BeanMethodInterceptor implements MethodInterceptor {
    protected abstract DBObject getSource();
    protected abstract BeanDescription getBeanDescription();

    private Set<String> fieldsSet = new HashSet();

    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        String property;
        property = getBeanDescription().getPropertyNameByReadMethod(method.getName());
        if (property != null) { // we've intercepted getter met hod
            if(fieldsSet.contains(property)) { // value was retrieved and already set, passing to original getter
                Object valToReturn = proxy.invokeSuper(obj, args); // invoking the original getter
                return valToReturn;
            }
            if (getSource().containsField(property)) { // we have such field in source
                PropertyDescriptor propertyDescriptor = getBeanDescription().getPropertyDescriptor(property);
                Object sourceValue = getSource().get(property);
                Object resultValue = null;

                if(sourceValue != null) {
                    if(propertyDescriptor.getPropertyType().isAssignableFrom(Set.class)) {
                        if(!(sourceValue instanceof BasicDBList)) { // all lists in DBObject are instances of BasicDBList
                            throw new RuntimeException("Source value is not instance of BasicDBList");
                        }

                        Class c = BaseProxy.getGenericListClass(propertyDescriptor.getReadMethod().getGenericReturnType());
                        boolean isStandardClass = BaseProxy.isStandardClass(c);
                        BasicDBList basicDBList = (BasicDBList)sourceValue;
                        Set set = new HashSet();
                        for(Object o : basicDBList) {
                            if(isStandardClass) {
                                set.add(o);
                            } else if (c.isEnum() && o instanceof String) {
                                set.add(Enum.valueOf(c, (String) o));
                            } else {
                                // TODO: bugs are here! rework
                                set.add(createInstance(c, o));
                            }
                        }

                        resultValue = set;

                    } else if(propertyDescriptor.getPropertyType().isAssignableFrom(Map.class)) {
                        if(!(sourceValue instanceof DBObject)) { // Map is filled from DBObject 
                            throw new RuntimeException("Source value is not instance of DBObject");
                        }
                        MapClasses mapClasses = // get generic descriptors, expecting <String, ?>
                                BaseProxy.getGenericMapClasses(propertyDescriptor.getReadMethod().getGenericReturnType());
                        boolean enumKey = mapClasses.classA.isEnum();
                        if(!mapClasses.classA.isAssignableFrom(String.class) && !enumKey) { // the key must be string
                            throw new RuntimeException("Map keys must be of class String or enum");
                        }
                        boolean isStandardClass = BaseProxy.isStandardClass(mapClasses.classB);
                        DBObject dbObject = (DBObject)sourceValue;
                        Map map = new HashMap();
                        for(String k : dbObject.keySet()) {

                            Object value = dbObject.get(k);
                            Object key = enumKey ? Enum.valueOf(mapClasses.classA, k) : k;

                            if (isStandardClass) {
                                map.put(key, value);
                            } else if (mapClasses.classB.isEnum() && value instanceof String) {
                                map.put(key, Enum.valueOf(mapClasses.classB, (String) value));
                            } else {
                                // TODO: bugs are here! rework
                                map.put(key, createInstance(mapClasses.classB, value));
                            }
                        }

                        resultValue = map;
                        
                    } else if(propertyDescriptor.getPropertyType().isAssignableFrom(List.class)) {
                        if(!(sourceValue instanceof BasicDBList)) { // all lists in DBObject are instances of BasicDBList
                            throw new RuntimeException("Source value is not instance of BasicDBList");
                        }

                        Class c = BaseProxy.getGenericListClass(propertyDescriptor.getReadMethod().getGenericReturnType());
                        boolean isStandardClass = BaseProxy.isStandardClass(c);
                        BasicDBList basicDBList = (BasicDBList)sourceValue;
                        List list = new ArrayList();
                        for(Object o : basicDBList) {
                            if(isStandardClass) {
                                list.add(o);
                            } else {
                                // TODO: bugs are here! rework
                                list.add(createInstance(c, o));
                            }
                        }

                        resultValue = list;                           

                    } else if(propertyDescriptor.getPropertyType().isEnum()) {

                        Class c = BaseProxy.getGenericListClass(propertyDescriptor.getReadMethod().getGenericReturnType());
                        resultValue = Enum.valueOf(c, (String) sourceValue);

                    } else if(sourceValue instanceof DBObject) {
                        resultValue = MongoConverter.toObject(propertyDescriptor.getPropertyType(), (DBObject)sourceValue);
                    }

                    if(resultValue == null) { // other types are passed with no change
                        resultValue = sourceValue;
                    }
                }

                // setProperty will fire the setter method interception, and the setter will call invokeSuper
                PropertyUtils.setProperty(obj, property, resultValue);
                fieldsSet.add(property);

                return resultValue;

            } else { // we have no value in source.
                if(!getBeanDescription().getPropertyDescriptor(property).getPropertyType().isPrimitive()) {
                    PropertyUtils.setProperty(obj, property, null);
                }
                fieldsSet.add(property);
                
                return null;
            }
        } else {
            // we are in setter method, or property is unavailable, or other method is called (why?).
            // Passing call to the implementation
            property = getBeanDescription().getPropertyNameByWriteMethod(method.getName());
            if(property != null) {
                fieldsSet.add(property);
            }
            return proxy.invokeSuper(obj, args);             
        }
    }

    private Object createInstance(Class c, Object o) throws ClassNotFoundException {
        DBObject dbObject = (DBObject)o;
        Object instance;
        if(dbObject.containsField("implementation")) {
            Class genericClass = Class.forName((String)dbObject.get("implementation"));
            instance = MongoConverter.toObject(genericClass, dbObject);
        } else {
            instance = MongoConverter.toObject(c, dbObject);
        }
        return instance;
    }
}
