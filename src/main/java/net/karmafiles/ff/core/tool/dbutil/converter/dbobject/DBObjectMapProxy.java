package net.karmafiles.ff.core.tool.dbutil.converter.dbobject;

import com.mongodb.DBObject;
import net.karmafiles.ff.core.tool.dbutil.converter.BaseProxy;
import net.karmafiles.ff.core.tool.dbutil.converter.MapClasses;
import org.bson.BSONObject;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Ilya Brodotsky
 * Date: 08.10.2010
 * Time: 15:41:57
 * <p/>
 * All rights reserved.
 * <p/>
 * Contact me:
 * email, jabber: ilya.brodotsky@gmail.com
 * skype: ilya.brodotsky
 */

public class DBObjectMapProxy extends BaseProxy implements DBObject {

    private Map<String, Object> map;
    private Class genericClass;

    public static DBObjectMapProxy create(Map map, PropertyDescriptor propertyDescriptor) {
        if(map == null) {
            throw new RuntimeException("Passed object is null");
        }

        DBObjectMapProxy dbObjectMapProxy = new DBObjectMapProxy();
        dbObjectMapProxy.map = new HashMap<String, Object>();

        dbObjectMapProxy.genericClass = getGenericMapClasses(propertyDescriptor.getReadMethod().getGenericReturnType()).classB;

        for (Object s: map.keySet()) {
            Object value = map.get(s);
            String key;
            try {
                key = s.getClass().isEnum() ? s.toString() : (String) s;
            } catch (Exception e) {
                throw new IllegalArgumentException("Only String or enum map keys are supported");
            }

            dbObjectMapProxy.put(key, value);
        }
        return dbObjectMapProxy;
    }

    private Object convertAndEnhance(Object value) {
        Object obj = BaseProxy.passValue(value);

        if(obj instanceof DBObjectProxy) { // we have converted POJO
            // and generic field list class doesnt match POJO's class
            if(!BaseProxy.getClassName(value).equals(genericClass.getName())) {
                Map<String, String> genericInfo = new HashMap();
                genericInfo.put("implementation", BaseProxy.getClassName(value));
                ((DBObjectProxy)obj).addGenericInfo(genericInfo);
            }
        }
        return obj;
    }


    public Object put(String key, Object v) {
        return map.put(key, convertAndEnhance(v));
    }

    public void putAll(BSONObject o) {
        for(String key : o.keySet()) {
            put(key, o.get(key));
        }
    }

    public void putAll(Map m) {
        map.putAll(m);
    }

    public Object get(String key) {
        return map.get(key);
    }

    public Map toMap() {
        return map;
    }

    public Object removeField(String key) {
        return map.remove(key);
    }

    @SuppressWarnings("deprecated")
    public boolean containsKey(String s) {
        return map.containsKey(s);
    }

    @SuppressWarnings("deprecated")
    public boolean containsField(String s) {
        return containsKey(s);
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    public void markAsPartialObject() {
        throw new RuntimeException("Method not implemented.");
    }

    public boolean isPartialObject() {
        return false;
    }
}
