package util;

import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;
import task.DBInit;

import javax.sql.DataSource;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBUtil {

    private static volatile DataSource DATA_SOURCE;

    /**
     * 提供获取数据库连接池的功能。
     * 使用单例模式（多线程版本）
     * 回顾：
     * 为什么在外层判读是否等于null
     * synchronized加锁以后，为什么还要判断等于null
     * 为什么DataSource类变量要使用volatile关键字修饰
     * 多线程操作：原子性、可见性、有序性
     * synchronized保证：三个特性
     * volatile:保证可见性、有序性
     */
    private static DataSource getDataSource(){
        if(DATA_SOURCE==null){//提高效率
            //刚开始所有进入这行代码的线程：DATA_SOURCE对象都是null
            //可能是第一个进去的线程，这时DATA_SOURCE对象是null;
            //也可能是第一个线程之后的线程进入并执行
            synchronized (DBUtil.class) {
                if(DATA_SOURCE==null) {//禁止指令重排序，建立内存屏障
                    SQLiteConfig config = new SQLiteConfig();
                    config.setDateStringFormat(Util.DATE_PATTERN);
                    //初始化操作
                    DATA_SOURCE = new SQLiteDataSource(config);
                    ((SQLiteDataSource) DATA_SOURCE).setUrl(getUrl());
                }
            }
        }
        return DATA_SOURCE;
    }

    /**
     * 获取sqlite数据库文件url的方法
     * @return
     */
    private static String getUrl(){
         /*获取target编译文件夹的路径
          通过getClassLoader().getResource()
          /getClassLoader().getResourceAsStream()这样的方法

          默认的根路径为编译文件夹的路径（target/classes）
         */
        String url=null;
        try {
            //获取DBinit编译文件后的路径，classe用./  父类用../但这里到不了父类
            URL classesURL= DBUtil.class.getClassLoader().getResource("./");
            //获取target/classes文件夹的父目录路径target
            String dir = new File(classesURL.getPath()).getParent();
            url="jdbc:sqlite://"+dir+File.separator+"everything-like.db";
            url= URLDecoder.decode(url,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            System.out.println("获取数据库文件路径失败");
        }
        System.out.println("获取数据库文件路径"+url);
        return url;
    }

    /**
     * 提供获取数据库连接的方法
     * 从数据库连接DataSource.getConnection()来获取数据库连接
     * @return
     * @throws SQLException
     */
    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }



    public static void close(Connection connection, Statement statement) {

        close(connection, statement, null);
    }

    /**
     * 释放数据库资源
     * @param connection 数据库连接
     * @param statement sql执行对接
     * @param resultSet 结果集
     */
    public static void close(Connection connection, Statement statement, ResultSet resultSet) {
        try {
            if (connection != null) {
                connection.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("释放数据库资源失败",e);
        }
    }

    public static void main(String[] args) throws SQLException {
        for(int i=0;i<10;i++){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DataSource dataSource=DBUtil.getDataSource();
                }
            }).start();
        }
    }
}
