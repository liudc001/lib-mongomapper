package net.karmafiles.ff.core.tool.dbutil.daohelper;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import net.karmafiles.ff.core.tool.Assert;

import java.util.*;

/**
 * Created by Ilya Brodotsky
 * Date: 14.10.2010
 * Time: 19:09:22
 * <p/>
 * All rights reserved.
 * <p/>
 * Contact me:
 * email, jabber: ilya.brodotsky@gmail.com
 * skype: ilya.brodotsky
 */

public abstract class BaseDaoHelper<T> extends DaoHelperTemplate<T> {
    private Set<DaoHelperFilter> creationFilters;
    private Set<DaoHelperFilter> updateFilters;
    private Set<DaoHelperFilter> removeFilters;

    protected void init(Class<T> type, DBCollection collection) {
        super.init(type, collection);
    }

    @Override
    public void beforeAdd(DBObject dbObject) {
        executeFilters(creationFilters, dbObject, getDbCollection());
    }

    @Override
    public void beforeUpdate(DBObject dbObject) {
        executeFilters(updateFilters, dbObject, getDbCollection());
    }

    @Override
    public void beforeRemove(DBObject dbObject) {
        executeFilters(removeFilters, dbObject, getDbCollection());
    }

    public void chainCreationFilter(DaoHelperFilter daoHelperFilter) {
        if(creationFilters == null) {
            creationFilters = new HashSet<DaoHelperFilter>();
        }

        creationFilters.add(daoHelperFilter);
    }

    public void chainUpdateFilter(DaoHelperFilter daoHelperFilter) {
        if(updateFilters == null) {
            updateFilters = new HashSet<DaoHelperFilter>();
        }

        updateFilters.add(daoHelperFilter);
    }

    public void chainRemoveFilter(DaoHelperFilter daoHelperFilter) {
        if(removeFilters == null) {
            removeFilters = new HashSet<DaoHelperFilter>();
        }

        removeFilters.add(daoHelperFilter);
    }

    protected void executeFilters(Set<DaoHelperFilter> filters, DBObject dbObject, DBCollection dbCollection) {
        if(filters != null) {
            for(DaoHelperFilter filter : filters) {
                filter.doFilter(dbCollection, dbObject);
            }
        }
    }

    public void addFieldValueChecker(String fieldName, FieldValueChecker checker) {
        Assert.notEmpty(fieldName);
        Assert.notNull(checker);

        final FieldValueChecker finalChecker = checker;
        final String finalFieldName = fieldName;

        DaoHelperFilter filter = new DaoHelperFilter() {
            public DBObject doFilter(DBCollection dbCollection, DBObject entity) {
                finalChecker.check(dbCollection, entity, finalFieldName);
                return entity;
            }
        };

        chainCreationFilter(filter);
        chainUpdateFilter(filter);
    }

    public void addNotNullField(String fieldName) {
        Assert.notEmpty(fieldName);
        
        addFieldValueChecker(fieldName, new FieldValueChecker() {
            public void check(DBCollection dbCollection, DBObject dbObject, String fieldName) {
                if(!dbObject.containsField(fieldName) || dbObject.get(fieldName) == null) {
                    throw new DaoException("Field '" + fieldName + "' doesn't exist or null.");                        
                }
            }
        });
    }

    protected static String expand(String[] list, String s) {
        Iterator<String> i = Arrays.asList(list).iterator();
        String result = "";

        while(i.hasNext()) {
            result += i.next();
            if(i.hasNext()) {
                result += s;
            }
        }

        return result;
    }


    protected static class UniqueFieldFilter implements DaoHelperFilter {
        private String[] fields;

        public UniqueFieldFilter(String[] fields) {
            this.fields = fields;
        }

        public DBObject doFilter(DBCollection dbCollection, DBObject entity) {
            if(fields.length == 0) {
                return entity;
            }

            QueryBuilder queryBuilder = QueryBuilder.start();
            queryBuilder.put("_id").notEquals(entity.get("_id"));
            List<DBObject> fieldQueries = new ArrayList();
            for(String field : fields) {
                if(!entity.containsField(field)) {
                    throw new DaoException("Can't check for field uniqueness: field doesn't exist.");
                }
                fieldQueries.add(QueryBuilder.start(field).is(entity.get(field)).get());
            }
            DBObject[] arr = fieldQueries.toArray(new DBObject[]{});
            queryBuilder.or(arr);

            long count = dbCollection.count(queryBuilder.get());
            if(count > 0) {
                throw new DaoException("Fields {" + expand(fields, ", ") + "} must be unique withing the collection " + dbCollection.getFullName());
            } else {
                return entity;
            }
        }
    }

    public void addUniqueField(String fieldName) {
        addUniqueFields(new String[] {fieldName });   
    }

    public void addUniqueFields(String[] fieldNames) {
        DaoHelperFilter filter = new UniqueFieldFilter(fieldNames);
        chainCreationFilter(filter);
        chainUpdateFilter(filter);
    }

    protected static class FieldRenewalFilter implements DaoHelperFilter {
        private String fieldName;
        private FieldValueProvider valueProvider;

        public FieldRenewalFilter(String fieldName, FieldValueProvider valueProvider) {
            this.fieldName = fieldName;
            this.valueProvider = valueProvider;
        }

        public DBObject doFilter(DBCollection dbCollection, DBObject entity) {
            if(!entity.containsField(fieldName)) {
                throw new DaoException("Entity must contain field '" + fieldName + "'");
            }

            entity.put(fieldName, valueProvider.provide(dbCollection, entity, fieldName));
            return entity;
        }
    }

    public void renewFieldOnCreation(String fieldName, FieldValueProvider fieldValueProvider) {
        chainCreationFilter(new FieldRenewalFilter(fieldName, fieldValueProvider)); 
    }

    public void renewFieldOnUpdate(String fieldName, FieldValueProvider fieldValueProvider) {
        chainUpdateFilter(new FieldRenewalFilter(fieldName, fieldValueProvider));
    }

  
}
