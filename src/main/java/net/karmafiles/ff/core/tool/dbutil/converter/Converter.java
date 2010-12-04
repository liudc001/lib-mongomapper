package net.karmafiles.ff.core.tool.dbutil.converter;

import com.mongodb.DBObject;
import net.karmafiles.ff.core.tool.dbutil.converter.bean.BeanEnhancer;
import org.apache.commons.beanutils.PropertyUtils;

import java.util.Map;

/**
 * Created by Ilya Brodotsky
 * Date: 08.10.2010
 * Time: 14:20:11
 * <p/>
 * All rights reserved.
 * <p/>
 * Contact me:
 * email, jabber: ilya.brodotsky@gmail.com
 * skype: ilya.brodotsky
 */

public class Converter {


    public static DBObject toDBObject(Object obj) {
        Object val = BaseProxy.passValue(obj);
        if (val instanceof DBObject) {
            return (DBObject) val;
        } else {
            throw new RuntimeException("Converted object is not instance of DBObject.");
        }
    }

    @SuppressWarnings({"unchecked"})
    public static <T> T toObject(Class<T> clazz, DBObject source) {
        T newObject = (T) BeanEnhancer.create(clazz, source);
        try {
            T copyObject = clazz.newInstance();
            PropertyUtils.copyProperties(copyObject, newObject);
            return copyObject;
        } catch (Exception e) {
            return newObject;
        }
    }

}
