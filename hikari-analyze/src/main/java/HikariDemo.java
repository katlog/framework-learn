import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by fw on 2019/9/17
 */
public class HikariDemo {

    public static void main(String[] args) {

        //配置文件
        HikariConfig hikariConfig = new HikariConfig();
//        hikariConfig.setJdbcUrl("jdbc:mysql://localhost:3306/mydata");//mysql
        hikariConfig.setJdbcUrl("jdbc:mysql://39.104.71.97:3306/db_level?useUnicode=true&characterEncoding=utf8&useSSL=false");
        hikariConfig.setDriverClassName("com.mysql.jdbc.Driver");
        hikariConfig.setUsername("wiki");
        hikariConfig.setPassword("wiki123");
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        HikariDataSource ds = new HikariDataSource(hikariConfig);
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;
        try{

            //创建connection
            conn = ds.getConnection();
            statement = conn.createStatement();

            //执行sql
            rs = statement.executeQuery("select 100 s  from dual");

            //取数据
            if (rs.next()){
                System.out.println(rs.getString("s"));
            }

            //关闭connection
            conn.close();
        }
        catch (SQLException e){
            e.printStackTrace();
        }

    }
}
