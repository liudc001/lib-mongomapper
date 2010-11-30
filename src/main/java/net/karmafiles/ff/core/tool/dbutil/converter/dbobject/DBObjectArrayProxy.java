package net.karmafiles.ff.core.tool.dbutil.converter.dbobject;

import com.mongodb.BasicDBList;
import net.karmafiles.ff.core.tool.dbutil.converter.BaseProxy;

/**
 * Created by Ilya Brodotsky
 * Date: 08.10.2010
 * Time: 17:47:31
 * <p/>
 * All rights reserved.
 * <p/>
 * Contact me:
 * email, jabber: ilya.brodotsky@gmail.com
 * skype: ilya.brodotsky
 */

public class DBObjectArrayProxy extends BasicDBList {
      public static DBObjectArrayProxy create(Object[] array) {
        if(array == null) {
            throw new RuntimeException("Passed object is null");
        }

        DBObjectArrayProxy dbObjectArrayProxy = new DBObjectArrayProxy();

        for(Object o : array) {
            dbObjectArrayProxy.add(BaseProxy.passValue(o));
        }

        return dbObjectArrayProxy;
    }
}
