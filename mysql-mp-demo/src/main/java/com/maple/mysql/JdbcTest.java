package com.maple.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author 陈其丰
 */
public class JdbcTest {

    /**
     * 最普通的事务
     */
    public static void main(String[] args) throws SQLException {
        String ip = System.getenv("ENV_CLOUD_IP");
        String dbName = "standalone_transaction";
        String username = "root";
        String password = "123456";
        String url = "jdbc:mysql://" + ip + ":3306/" + dbName + "?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8";
        System.out.println(url);
        Connection connection = DriverManager.getConnection(url, username, password);

        //开启事务
        connection.setAutoCommit(false);
        try{
            String sql1 = "update account set money=money-100 where id=1";
            connection.prepareStatement(sql1).execute();
            String sql2 = "update account set money=money+100 where id=2";
            connection.prepareStatement(sql2).execute();

            // 提交事务
            connection.commit();
        }catch (Exception e){
            // 事务回滚
            connection.rollback();
        }
    }
}
