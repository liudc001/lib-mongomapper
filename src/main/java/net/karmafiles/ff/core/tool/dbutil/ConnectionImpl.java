package net.karmafiles.ff.core.tool.dbutil;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.ServerAddress;
import net.karmafiles.ff.core.tool.dbutil.daohelper.DaoException;

import javax.annotation.PostConstruct;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

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
    private String connectionDescriptor;

    public void setConnectionDatabase(String connectionDatabase) {
        this.connectionDatabase = connectionDatabase;
    }

    public void setConnectionPort(Integer connectionPort) {
        this.connectionPort = connectionPort;
    }

    public void setConnectionHost(String connectionHost) {
        this.connectionHost = connectionHost;
    }

    public String getConnectionDescriptor() {
        return connectionDescriptor;
    }

    public void setConnectionDescriptor(String connectionDescriptor) {
        this.connectionDescriptor = connectionDescriptor;
    }

    @PostConstruct
    public void connect() {
        try {
            if (connectionDescriptor != null) {
                String[] hosts = connectionDescriptor.split(",");
                List<ServerAddress> addr = new ArrayList<ServerAddress>();
                for (String host: hosts) {
                    String[] hostPortPair = host.split(":");
                    int port = 27017;
                    if (hostPortPair.length > 1) {
                        try {
                            port = Integer.parseInt(hostPortPair[1]);
                        } catch (NumberFormatException e) {
                            // port doesn't look as port
                        }
                    }
                    addr.add(new ServerAddress(host, port));
                }
                mongo = new Mongo(addr);
            } else {
                mongo = new Mongo(connectionHost, connectionPort);
            }
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
