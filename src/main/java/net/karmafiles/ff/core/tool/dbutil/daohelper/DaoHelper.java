package net.karmafiles.ff.core.tool.dbutil.daohelper;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import net.karmafiles.ff.core.tool.IdGenerator;
import net.karmafiles.ff.core.tool.dbutil.ConnectionImpl;
import net.karmafiles.ff.core.tool.dbutil.converter.MongoConverter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Ilya Brodotsky
 * Date: 17.10.2010
 * Time: 19:35:30
 * <p/>
 * All rights reserved.
 * <p/>
 * Contact me:
 * email, jabber: ilya.brodotsky@gmail.com
 * skype: ilya.brodotsky
 */

public class DaoHelper<T> extends BaseDaoHelper<T> {

    private static final int MAX_NUMBER_AS_LIST = 10000;

    private ConnectionImpl connection;

    public ConnectionImpl getConnection() {
        return connection;
    }

    public void setConnection(ConnectionImpl connection) {
        this.connection = connection;
    }

    protected void init(Class<T> type) {
        this.init(type, extractCollectionName(type)); // by default, store in collections per class name
    }

    private String extractCollectionName(Class<T> type) {
        String fqName = type.getName();
        return fqName.substring(fqName.lastIndexOf(".")+1, fqName.length());
    }

    /**
     * Must be overridden in subclasses with specific values for class type and collection name
     * and be given @PostConstruct annotation.
     *
     * @param type class, served by this DAO subclass
     * @param dbCollectionName collection name
     */

    protected void init(Class<T> type, String dbCollectionName) {
        this.init(type, connection.getCollection(dbCollectionName));            
    }

    protected void init(Class<T> type, DBCollection dbCollection) {
        super.init(type, dbCollection);

        renewFieldOnCreation("created", new FieldValueProvider() {
            public Object provide(DBCollection dbCollection, DBObject dbObject, String fieldName) {
                return new Date();
            }
        });

        renewFieldOnCreation("modified", new FieldValueProvider() {
            public Object provide(DBCollection dbCollection, DBObject dbObject, String fieldName) {
                return new Date();
            }
        });

        renewFieldOnUpdate("modified", new FieldValueProvider() {
            public Object provide(DBCollection dbCollection, DBObject dbObject, String fieldName) {
                return new Date();
            }
        });

        chainCreationFilter(new DaoHelperFilter() {
            public DBObject doFilter(DBCollection dbCollection, DBObject entity) {
                if(entity.get("_id") == null) {
                    entity.put("_id", generateNewId(entity));
                }
                return entity;
            }
        });
    }

    // either one has to be overridden
    
    public String generateNewId(DBObject entity) {
        return generateNewId();
    }

    public String generateNewId() {
        return IdGenerator.createSecureId();
//        throw new DaoException("Id generator not set. To create entities with null ids please " +
//                "override generateNewId() method in DaoHelper<T>.");
    }

    public List<T> findAll() {

        return findAllWithFilter(null);

    }

    public List<T> findAllWithFilter(EntityFilter<T> filter) {

        List<T> entities = new ArrayList<T>();

        DBCursor cursor = getDbCollection().find();
        if (cursor.count() < MAX_NUMBER_AS_LIST) {
            while (cursor.hasNext()) {
                T t = MongoConverter.toObject(getType(), cursor.next());
                if (filter == null) {
                    entities.add(t);
                } else if (filter.accepts(t)) {
                    entities.add(t);
                }
            }
            return entities;
        } else {
            throw new IllegalStateException("Iterable lists for large collections not implemented yet");
        }

    }

    public T create() {
        try {
            T t = getType().newInstance();
            return t;
        } catch (Exception e) {
            throw new IllegalStateException("Can't instantiate " + getType(), e);
        }
    }



}
