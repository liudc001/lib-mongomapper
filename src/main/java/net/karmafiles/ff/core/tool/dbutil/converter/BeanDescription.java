package net.karmafiles.ff.core.tool.dbutil.converter;

import org.apache.commons.beanutils.PropertyUtils;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Ilya Brodotsky
 * Date: 08.10.2010
 * Time: 14:24:51
 * <p/>
 * All rights reserved.
 * <p/>
 * Contact me:
 * email, jabber: ilya.brodotsky@gmail.com
 * skype: ilya.brodotsky
 */

public class BeanDescription {
    private Map<String, PropertyDescriptor> description = new HashMap();
    private Map<String, PropertyDescriptor> writeMethodToDescriptor = new HashMap();
    private Map<String, PropertyDescriptor> readMethodToDescriptor = new HashMap();
    private Map<String, String> writeMethodToPropertyName = new HashMap();
    private Map<String, String> readMethodToPropertyName = new HashMap();

    private BeanDescription() {
    }

    private static Map<String, BeanDescription> knownBeanDescriptions = new ConcurrentHashMap();

    public static BeanDescription describe(Class beanClass) {
        BeanDescription beanDescription = knownBeanDescriptions.get(beanClass.getName());
        if(beanDescription != null) {
            return beanDescription;
        }

        try {
            beanDescription = new BeanDescription();
            PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(beanClass);

            for(PropertyDescriptor descriptor : descriptors) {
                if(descriptor.getReadMethod() == null || descriptor.getWriteMethod() == null) {
                    continue;
                }

                beanDescription.description.put(descriptor.getName(), descriptor);

                beanDescription.readMethodToDescriptor.put(
                        descriptor.getReadMethod().getName(), descriptor);

                beanDescription.writeMethodToDescriptor.put(
                        descriptor.getWriteMethod().getName(), descriptor);

                beanDescription.readMethodToPropertyName.put(
                        descriptor.getReadMethod().getName(), descriptor.getName());

                beanDescription.writeMethodToPropertyName.put(
                        descriptor.getWriteMethod().getName(), descriptor.getName());
            }

            return beanDescription;
        } catch(Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public PropertyDescriptor getPropertyDescriptor(String propertyName) {
        return description.get(propertyName);
    }

    public PropertyDescriptor getWriteMethodDescriptor(String writeMethodName) {
        return writeMethodToDescriptor.get(writeMethodName);
    }

    public PropertyDescriptor getReadMethodDescriptor(String readMethodName) {
        return readMethodToDescriptor.get(readMethodName);
    }

    public String getPropertyNameByReadMethod(String readMethodName) {
        return readMethodToPropertyName.get(readMethodName);
    }

    public String getPropertyNameByWriteMethod(String writeMethodName) {
        return writeMethodToPropertyName.get(writeMethodName);
    }

    public Set<String> getPropertyNames() {
        return description.keySet();
    }
}
