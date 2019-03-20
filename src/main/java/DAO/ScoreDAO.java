package DAO;

import Model.Score;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ScoreDAO {

    public static void CreatUserScoreTable(Connection conn,int userid,int totalCount,int winCount) {
        String insertSql = "INSERT INTO score(userid,totalcount,wincount) VALUES (?,?,?)";
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(insertSql);
            preparedStatement.setInt(1,userid);
            preparedStatement.setInt(2,totalCount);
            preparedStatement.setInt(3,winCount);
            preparedStatement.executeUpdate();
            preparedStatement.close();

        }catch (Exception e)
        {

            System.out.println("建立玩家数据时出现异常，异常信息为："+e);
        }
    }

    public static Score getScoreByUserid(Connection conn,int userid)
    {
        String selectSqlStr="SELECT * FROM score WHERE userid=?";
        try{
            PreparedStatement preparedStatement=conn.prepareStatement(selectSqlStr);
            preparedStatement.setInt(1,userid);
            ResultSet resultSet=preparedStatement.executeQuery();
            if(resultSet.next())
            {
                int id=resultSet.getInt("id");
                int userId=resultSet.getInt("userid");
                int totalCount=resultSet.getInt("totalcount");
                int winCount=resultSet.getInt("wincount");
                resultSet.close();
                preparedStatement.close();
                Score score=new Score(id,userId,totalCount,winCount);
                return score;
            }else {
                resultSet.close();
                preparedStatement.close();
                return null;
            }
        }catch (Exception e){
            System.out.println("获取用户战绩出现异常，异常信息为："+e);
            return null;
        }

    }

    public static void UpdateScore(Connection conn,int userid,int totalCount,int winCount)
    {
        String updateSqlStr="UPDATE score SET totalcount=?,wincount=? WHERE userid=?";
        try{
            PreparedStatement preparedStatement=conn.prepareStatement(updateSqlStr);
            preparedStatement.setInt(1,totalCount);
            preparedStatement.setInt(2,winCount);
            preparedStatement.setInt(3,userid);
            preparedStatement.executeUpdate();
            preparedStatement.close();
        }catch (Exception e)
        {
            System.out.println("更新战绩时发生异常，异常信息为:"+e);
        }
    }
}
