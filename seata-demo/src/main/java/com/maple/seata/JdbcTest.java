package com.maple.seata;

import com.mysql.cj.jdbc.MysqlXADataSource;
import com.mysql.cj.jdbc.MysqlXid;

import javax.sql.XAConnection;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import java.sql.SQLException;
import java.util.UUID;

/**
 * @author 陈其丰
 */
public class JdbcTest {
    /* XA */
    public static void main(String[] args) throws SQLException, XAException {
        String ip = System.getenv("ENV_CLOUD_IP");
        String dbName1 = "distributed_transaction1";
        String dbName2 = "distributed_transaction2";
        String username = "root";
        String password = "123456";

        MysqlXid mysqlXid1 = new MysqlXid("111".getBytes(), "333".getBytes(), 1);
        MysqlXid mysqlXid2 = new MysqlXid("222".getBytes(), "444".getBytes(), 1);

        String url1 = "jdbc:mysql://" + ip + ":3306/" + dbName1 + "?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8";
        String url2 = "jdbc:mysql://" + ip + ":3306/" + dbName2 + "?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8";

        MysqlXADataSource xaDataSource1 = new MysqlXADataSource();
        xaDataSource1.setUrl(url1);
        xaDataSource1.setUser(username);
        xaDataSource1.setPassword(password);

        MysqlXADataSource xaDataSource2 = new MysqlXADataSource();
        xaDataSource2.setUrl(url2);
        xaDataSource2.setUser(username);
        xaDataSource2.setPassword(password);

        // 获取资源管理器 DB1
        XAConnection xaConnection1 = xaDataSource1.getXAConnection();
        XAResource xaResource1 = xaConnection1.getXAResource();

        // 获取资源管理器 DB2
        XAConnection xaConnection2 = xaDataSource2.getXAConnection();
        XAResource xaResource2 = xaConnection2.getXAResource();

        try{
            /* 数据库 1 */
            // 开启XA事务
            xaResource1.start(mysqlXid1,XAResource.TMNOFLAGS);
            // 执行SQL
            String sql1 = "update account set money=money-100 where id=1";
            xaConnection1.getConnection().prepareStatement(sql1).execute();
            // 关闭XA事务
            xaResource1.end(mysqlXid1,XAResource.TMSUCCESS);

            /* 数据库 2 */
            // 开启XA事务
            xaResource2.start(mysqlXid2,XAResource.TMNOFLAGS);
            // 执行SQL
            String sql2 = "update account set money=money+100 where id=2";
            xaConnection2.getConnection().prepareStatement(sql2).execute();
            // 关闭XA事务
            xaResource2.end(mysqlXid2,XAResource.TMSUCCESS);

            // Prepare 阶段
            int prepare1 = xaResource1.prepare(mysqlXid1);
            int prepare2 = xaResource2.prepare(mysqlXid2);

            if(prepare1 == XAResource.XA_OK && prepare2 == XAResource.XA_OK){
                xaResource1.commit(mysqlXid1,false);
                xaResource2.commit(mysqlXid2,false);
            }else{
                xaResource1.rollback(mysqlXid1);
                xaResource2.rollback(mysqlXid2);
            }
        }catch (Exception e){
            e.printStackTrace();
            xaResource1.rollback(mysqlXid1);
            xaResource2.rollback(mysqlXid2);
        }
    }
}
