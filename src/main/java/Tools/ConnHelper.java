package Tools;


import java.sql.Connection;
import java.sql.DriverManager;


public class ConnHelper {

    private static final String URL="jdbc:mysql://localhost:3306/forestwar"+"?useUnicode=true&characterEncoding=utf-8&useSSL=false";
    private static final String USER="root";
    private static final String PW="660317";

    /**
     *与MySql数据库进行连接
     * @return connection对象
     */
    public static Connection ConnectToMySql()
    {
        try {
            //1：加载驱动程序
            Class.forName("com.mysql.jdbc.Driver");
            //2：获得数据库连接
            Connection conn= DriverManager.getConnection(URL,USER,PW);
            return conn;
        }catch (Exception e){
            System.out.println("连接数据库时发生异常.异常信息为："+e);
            return null;
        }
    }


    public static void CloseMySqlConnection(Connection conn)
    {
        if(conn!=null)
        {
            try {
                conn.close();
            }catch (Exception e)
            {
                System.out.println("关闭MySql数据库连接时发生异常，异常信息为："+e);
            }
        }else{
            System.out.println("MySQL连接不存在,无法关闭");
        }
    }




}
