package info.bytecraft.database.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.dbcp.BasicDataSource;
import org.bukkit.configuration.file.FileConfiguration;

import info.bytecraft.database.DAOException;
import info.bytecraft.database.IContext;
import info.bytecraft.database.IContextFactory;

public class DBContextFactory implements IContextFactory
{
    private BasicDataSource ds;

    public DBContextFactory(FileConfiguration config)
    {
        String driver = config.getString("db.driver");
        if (driver == null) {
            driver = "com.mysql.jdbc.Driver";
        }

        try {
            Class.forName(driver).newInstance();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }

        String user = config.getString("db.user");
        String password = config.getString("db.password");
        String url = config.getString("db.url");

        ds = new BasicDataSource();
        ds.setDriverClassName(driver);
        ds.setUsername(user);
        ds.setPassword(password);
        ds.setUrl(url);
        ds.setMaxActive(5);
        ds.setMaxIdle(5);
        ds.setDefaultAutoCommit(true);
    }

    @Override
    public IContext createContext() 
    throws DAOException
    {
        try{
            Connection conn = ds.getConnection();
            try(Statement stm = conn.createStatement()){
                stm.execute("SET NAMES latin1");
            }
            
            return new DBContext(conn);
        }catch(SQLException e){
            throw new DAOException(e);
        }
    }

}
