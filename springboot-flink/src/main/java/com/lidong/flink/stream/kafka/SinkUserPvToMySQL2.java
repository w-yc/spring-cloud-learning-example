package com.lidong.flink.stream.kafka;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;


/**
 * 使用jdbc导入结果
 */
public class SinkUserPvToMySQL2 extends RichSinkFunction<UserPvEntity> {

    private PreparedStatement ps;
    private Connection connection;

    /**
     * open() 方法中建立连接，这样不用每次 invoke 的时候都要建立连接和释放连接
     *
     * @param parameters
     * @throws Exception
     */
    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        connection = getConnection();
        String sql = "replace into t_user_pv(pvtime,userId, pvcount) values(?, ?, ?);";
        ps = this.connection.prepareStatement(sql);
    }

    @Override
    public void close() throws Exception {
        super.close();
        //关闭连接和释放资源
        if (connection != null) {
            connection.close();
        }
        if (ps != null) {
            ps.close();
        }
    }

    /**
     * 每条数据的插入都要调用一次 invoke() 方法
     *
     * @param userPvEntity
     * @param context
     * @throws Exception
     */
    @Override
    public void invoke(UserPvEntity userPvEntity, Context context) throws Exception {
        //组装数据，执行插入操作
    	ps.setTimestamp(1, userPvEntity.getTime());
    	ps.setString(2, userPvEntity.getUserId());
        ps.setLong(3, userPvEntity.getPvcount());
        ps.executeUpdate();
    }

    private static Connection getConnection() {
        Connection con = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/db01?useUnicode=true&characterEncoding=UTF-8&useSSL=false","root","123456");
        } catch (Exception e) {
            System.out.println("mysql get connection has exception , msg = "+ e.getMessage());
        }
        return con;
    }
}
