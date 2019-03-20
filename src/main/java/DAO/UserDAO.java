package DAO;

import Model.Score;
import Model.User;
import java.sql.Connection;
import org.omg.CORBA.PUBLIC_MEMBER;


import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {

    public static User VerifyUser(Connection conn, String username, String password)
    {
        String selectSql="SELECT * FROM user WHERE username=? AND password=?";
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(selectSql);
            preparedStatement.setString(1,username);
            preparedStatement.setString(2,password);
            ResultSet resultSet=preparedStatement.executeQuery();
            if(resultSet.next())
            {
                int userId= resultSet.getInt("id");
                String userName=resultSet.getString("username");
                String passWord=resultSet.getString("password");
                User user =new User(userId,userName,passWord);
                //关闭与释放
                resultSet.close();
                preparedStatement.close();

                return user;
            }


        }catch (Exception e)
        {
            System.out.println("验证用户时发生异常，异常信息为："+e);
            return null;
        }
        return null;
    }

    public static boolean ResgisterUser(Connection conn,String username,String password)
    {
        //先查再加入
        String selectSqlStr="SELECT * FROM user WHERE username=?";

        try{
            PreparedStatement preparedStatement=conn.prepareStatement(selectSqlStr);
            preparedStatement.setString(1,username);
            ResultSet rs=preparedStatement.executeQuery();
            if(rs.next())
            {
                //查找到相关记录-不允许注册
                rs.close();
                preparedStatement.close();
                return false;

            }else{
                //无相关记录，可注册
                String insertSqlStr="INSERT INTO user(username,password) VALUES(?,?)";
                PreparedStatement preparedStatement1=conn.prepareStatement(insertSqlStr);
                preparedStatement1.setString(1,username);
                preparedStatement1.setString(2,password);
                int i=preparedStatement1.executeUpdate();
                rs.close();
                preparedStatement1.close();
                if(i>0)
                {
                    System.out.println("注册成功");
                    //说明注册成功
                    //为其建战绩表
                    //拿到userid，需要再进行一次查询
                    PreparedStatement preparedStatement2=conn.prepareStatement(selectSqlStr);
                    preparedStatement2.setString(1,username);
                    ResultSet rs2= preparedStatement2.executeQuery();
                    if(rs2.next())
                    {
                        int userid=rs2.getInt("id");
                        //释放本次查询
                        rs2.close();
                        preparedStatement2.close();
                        //创建用户战绩数据表
                        ScoreDAO.CreatUserScoreTable(conn,userid,0,0);



                    }
                    return true;

                }else{
                    //说明注册失败
                    return false;
                }

            }

        }catch (Exception e)
        {
            System.out.println("注册用户时发生异常。异常信息为："+e);
        }
        return false;

    }

}
