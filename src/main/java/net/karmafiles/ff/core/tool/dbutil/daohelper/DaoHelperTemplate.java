package net.karmafiles.ff.core.tool.dbutil.daohelper;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import net.karmafiles.ff.core.tool.Assert;
import net.karmafiles.ff.core.tool.dbutil.converter.MongoConverter;
import org.bson.types.ObjectId;

/**
 * Created by Ilya Brodotsky
 * Date: 17.10.2010
 * Time: 17:49:06
 * <p/>
 * All rights reserved.
 * <p/>
 * Contact me:
 * email, jabber: ilya.brodotsky@gmail.com
 * skype: ilya.brodotsky
 */

public abstract class DaoHelperTemplate<T> {

    private Class<T> type;
    private DBCollection dbCollection;

    public abstract void beforeAdd(DBObject dbObject);
    public abstract void beforeUpdate(DBObject dbObject);
    public abstract void beforeRemove(DBObject dbObject);

    protected DBCollection getDbCollection() {
        return dbCollection;
    }

    protected Class<T> getType() {
        return type;
    }

    protected void init(Class<T> type, DBCollection dbCollection) {
        Assert.notNull(type);
        Assert.notNull(dbCollection);

        this.type = type;
        this.dbCollection = dbCollection;
    }

    private DBObject idQuery(String id) {
        Assert.notNull(id);
        return QueryBuilder.start("_id").is(id).get();
    }

    public T add(T entity) {
        Assert.notNull(entity, "entity may not be null");
        DBObject dbObject = MongoConverter.toDBObject(entity);

        beforeAdd(dbObject);
        
        getDbCollection().save(dbObject);

        return MongoConverter.toObject(getType(), dbObject);
    }

    public T get(String id) {
        DBObject dbObject = getDbCollection().findOne(idQuery(id));
        if(dbObject != null) {
            return MongoConverter.toObject(getType(), dbObject);
        } else {
            return null;
        }
    }

    public void remove(String id) {
        Assert.notNull(id);

        DBObject entity = getDbCollection().findOne(idQuery(id));
        if(entity == null) {
            throw new DaoException("Can't remove: object with id='"
                    + id + "' was not found in the collection '" + dbCollection.getFullName() + "'.");
        }

        beforeRemove(entity);
        
        getDbCollection().remove(entity);
    }

    public T update(T entity) {


        Assert.notNull(entity);
        DBObject dbObject = MongoConverter.toDBObject(entity);

        beforeUpdate(dbObject);

        String id = (String)dbObject.get("_id");
        Assert.notNull(id, "Can't update: id may not be null");

        getDbCollection().update(idQuery(id), dbObject);

        // kind of need to do this explicitly to avoid stale data 
        getDbCollection().getDB().command("{fsync:1}");

        return MongoConverter.toObject(getType(), dbObject);
    }

}
