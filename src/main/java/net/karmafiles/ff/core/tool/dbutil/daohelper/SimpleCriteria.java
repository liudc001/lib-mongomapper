package net.karmafiles.ff.core.tool.dbutil.daohelper;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import net.karmafiles.ff.core.tool.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ilya Brodotsky
 * Date: 16.10.2010
 * Time: 3:32:09
 * <p/>
 * All rights reserved.
 * <p/>
 * Contact me:
 * email, jabber: ilya.brodotsky@gmail.com
 * skype: ilya.brodotsky
 */

public class SimpleCriteria {
    private Map<Object, Object> params;

    private SimpleCriteria() {
        params = new HashMap();
    }

    public static SimpleCriteria create() {
        return new SimpleCriteria();
    }

    public static SimpleCriteria create(Object param, Object value) {
        return create().add(param, value);
    }

    public SimpleCriteria add(Object param, Object value) {
        Assert.notNull(param);
        Assert.notNull(value);

        params.put(param, value);

        return this;
    }

    public DBObject build() {
        DBObject dbObject = new BasicDBObject();
        for(Object key : params.keySet()) {
            dbObject.put(key.toString(), params.get(key));                        
        }
        return dbObject;
    }
}
