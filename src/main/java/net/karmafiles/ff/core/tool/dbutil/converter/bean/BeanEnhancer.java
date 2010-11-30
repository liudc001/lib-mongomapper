package net.karmafiles.ff.core.tool.dbutil.converter.bean;

import com.mongodb.DBObject;
import net.karmafiles.ff.core.tool.dbutil.converter.BeanDescription;
import net.sf.cglib.proxy.Enhancer;

/**
 * Created by Ilya Brodotsky
 * Date: 09.10.2010
 * Time: 17:34:59
 * <p/>
 * All rights reserved.
 * <p/>
 * Contact me:
 * email, jabber: ilya.brodotsky@gmail.com
 * skype: ilya.brodotsky
 */

public class BeanEnhancer {
    public static Object create(Class clazz, final DBObject source) {
        if(clazz == null || source == null) {
            throw new RuntimeException("One of parameters is null");
        }

        final BeanDescription beanDescription = BeanDescription.describe(clazz);

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallbackType(BeanMethodInterceptor.class);
        enhancer.setUseFactory(false);
        
        enhancer.setCallback(new BeanMethodInterceptor() {
            @Override
            public DBObject getSource() {
                return source;
            }

            @Override
            public BeanDescription getBeanDescription() {
                return beanDescription;
            }
        });

        

        return enhancer.create();

    }
}
