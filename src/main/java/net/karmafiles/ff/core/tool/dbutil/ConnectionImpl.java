package net.karmafiles.ff.core.tool.dbutil;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import net.karmafiles.ff.core.tool.dbutil.daohelper.DaoException;

import javax.annotation.PostConstruct;
import java.net.UnknownHostException;

/**
 * Created by Ilya Brodotsky
 * Date: 16.10.2010
 * Time: 3:12:06
 * <p/>
 * All rights reserved.
 * <p/>
 * Contact me:
 * email, jabber: ilya.brodotsky@gmail.com
 * skype: ilya.brodotsky
 */

public class ConnectionImpl implements MongoConnection {

    private Mongo mongo;
    private DB db;

    private String connectionHost;
    private Integer connectionPort;
    private String connectionDatabase;

    public void setConnectionDatabase(String connectionDatabase) {
        this.connectionDatabase = connectionDatabase;
    }

    public void setConnectionPort(Integer connectionPort) {
        this.connectionPort = connectionPort;
    }

    public void setConnectionHost(String connectionHost) {
        this.connectionHost = connectionHost;
    }

    @PostConstruct
    public void connect() {
        try {
            mongo = new Mongo(connectionHost, connectionPort);
            db = mongo.getDB(connectionDatabase);
        } catch (UnknownHostException e) {
            throw new DaoException(e);
        }
    }

    public DBCollection getCollection(String name) {
        return db.getCollection(name);
    }

    public void dropDatabase() {
        db.dropDatabase();
    }
}
