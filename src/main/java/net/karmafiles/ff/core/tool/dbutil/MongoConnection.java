package net.karmafiles.ff.core.tool.dbutil;

import com.mongodb.DBCollection;

/**
 * Introduced for easier mockup during unit testing
 *
 * @author timur
 */
public interface MongoConnection {
    DBCollection getCollection(String name);

    void dropDatabase();
}
