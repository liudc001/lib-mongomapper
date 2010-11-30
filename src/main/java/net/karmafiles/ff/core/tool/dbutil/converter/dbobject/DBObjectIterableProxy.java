package net.karmafiles.ff.core.tool.dbutil.converter.dbobject;

import com.mongodb.BasicDBList;
import net.karmafiles.ff.core.tool.dbutil.converter.BaseProxy;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Ilya Brodotsky
 * Date: 08.10.2010
 * Time: 16:08:15
 * <p/>
 * All rights reserved.
 * <p/>
 * Contact me:
 * email, jabber: ilya.brodotsky@gmail.com
 * skype: ilya.brodotsky
 */

public class DBObjectIterableProxy extends BasicDBList {


    public static DBObjectIterableProxy create(Iterable iterable, PropertyDescriptor propertyDescriptor) {
        if(iterable == null) {
            throw new RuntimeException("Passed object is null");
        }

        DBObjectIterableProxy dbObjectIterableProxy = new DBObjectIterableProxy();
        Class genericClass = null;
        if (propertyDescriptor != null) {
            genericClass = BaseProxy.getGenericListClass(propertyDescriptor.getReadMethod().getGenericReturnType());
        }

        Iterator iterator = iterable.iterator();
        while(iterator.hasNext()) {

            Object nextObj = iterator.next();
            Object obj = BaseProxy.passValue(nextObj);

            if(obj instanceof DBObjectProxy) { // we have converted POJO
                // and generic field list class doesnt match POJO's class 
                if(genericClass != null && !BaseProxy.getClassName(nextObj).equals(genericClass.getName())) {
                    Map<String, String> genericInfo = new HashMap();
                    genericInfo.put("implementation", BaseProxy.getClassName(nextObj));
                    ((DBObjectProxy)obj).addGenericInfo(genericInfo);
                }
            }

            dbObjectIterableProxy.add(obj);
        }

        return dbObjectIterableProxy;
    }

}
