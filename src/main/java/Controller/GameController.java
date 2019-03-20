package Controller;
import DAO.ScoreDAO;
import Enum.RequestType;
import Enum.ReturnType;
import Enum.ActionType;
import Model.Score;
import Servers.Client;
import Servers.Room;
import Servers.Server;
import io.netty.channel.Channel;
import org.omg.CORBA.PUBLIC_MEMBER;
import org.omg.PortableInterceptor.ClientRequestInfo;


public class GameController extends BaseController {
    public GameController(){
        requestType= RequestType.Game;
    }

    /**
     * 处理开始游戏请求
     * @param data
     * @param channel
     * @param server
     * @return
     */
    public String StartGame(String data, Channel channel, Server server)
    {
        //发起该请求的必然是房主
        if(data.equals("EMPTY"))
        {
            //说明没有P2玩家，不能开启游戏-返回失败
            return String.valueOf(ReturnType.Failed.ordinal());
        }else{
            //向房间中的非房主其他客户端广播
            Client hostClient=server.clientDictionary.get(channel);
            Room room=hostClient.currentEnteredRoom;
            room.BroadCastMessage(channel,ActionType.StartGame, String.valueOf(ReturnType.Successful.ordinal()));
            // 开启房间的倒计时线程
            room.StartTimerThread();
            //向房主返回开始游戏成功
            return String.valueOf(ReturnType.Successful.ordinal());
        }
    }

    /**
     * 处理同步位置信息
     * @param data
     * @param channel
     * @return
     */
    public String SyncMovement(String data,Channel channel,Server server)
    {
        //不用解析，直接广播给房间中的其他客户端
        Client client=server.clientDictionary.get(channel);
        Room currentRoom=client.currentEnteredRoom;
        if(currentRoom!=null)
        {
            currentRoom.BroadCastMessage(channel,ActionType.SyncMovement,data);
        }
        return null;//当前客户端无必要返回任何数据
    }

    /**
     * 处理同步攻击动画
     * @param data
     * @param channel
     * @param server
     * @return
     */
    public String SyncAnimation(String data,Channel channel,Server server)
    {
        //同样不用解析数据，直接广播给其他客户端
        Client client=server.clientDictionary.get(channel);
        Room currentRoom=client.currentEnteredRoom;
        if(currentRoom!=null)
        {
            currentRoom.BroadCastMessage(channel,ActionType.SyncAnimation,data);
        }
        return null;//当前客户端无必要返回任何数据
    }

    /**
     * 处理同步箭的生成
     * @param data
     * @param channel
     * @param server
     * @return
     */
    public String SyncArrow(String data,Channel channel,Server server)
    {
        //不用解析，直接将数据广播给其他客户端
        Client client=server.clientDictionary.get(channel);
        Room room=client.currentEnteredRoom;
        if(room!=null)
        {
            room.BroadCastMessage(channel,ActionType.SyncArrow,data);
        }
        return null;//本对象不用返回任何内容
    }

    /**
     *  处理伤害请求，并更新血条
     * @param data
     * @param channel
     * @param server
     * @return
     */
    public String TakeDamage(String data,Channel channel,Server server)
    {
        //发起该请求的是扣对方的血，而不是自己的血
        int reduceHP=Integer.parseInt(data);
        Client client =server.clientDictionary.get(channel);
        Room currentRoom=client.currentEnteredRoom;
        if(currentRoom!=null)
        {
            for(Channel temp :currentRoom.getRoomChannelList())
            {
                //遍历房间中的channel，对另一个channel所对应的进行扣血
                if(temp!=channel)
                {
                    Client needDecraseHpClient=server.clientDictionary.get(temp);
                    boolean isOver= needDecraseHpClient.DecreaseHP(reduceHP);
                    //广播当前房间内所有玩家的血量
                    currentRoom.BroadCastMessage(null,ActionType.SyncHP,currentRoom.GetAllClientHP());
                    if(isOver) {
                        // 游戏结束
                        for (Channel channel1 : currentRoom.getRoomChannelList()) {
                            Client client1 = server.clientDictionary.get(channel1);
                            Score currentScore=client1.score;
                            if (client1.IsDie()) {
                                //输了
                                server.SendResponseToClient(String.valueOf(ReturnType.Failed.ordinal()), ActionType.GameOver, channel1);
                                // 更新战绩-ScoreDao.UpdateScore
                                client1.score.setTotalCount(currentScore.getTotalCount()+1);//服务器数据更新
                                // 数据库数据更新
                                ScoreDAO.UpdateScore(client1.getMysqlConn(),client1.user.getId(),client1.score.getTotalCount(),client1.score.getWinCount());
                                // 发送最新的用户信息到客户端
                                String newPlayerInfo=client1.score.getTotalCount()+"#"+client1.score.getWinCount();
                                server.SendResponseToClient(newPlayerInfo,ActionType.UpdatePlayerInfo,channel1);
                            } else {
                                //赢了
                                server.SendResponseToClient(String.valueOf(ReturnType.Successful.ordinal()), ActionType.GameOver, channel1);
                                // 更新战绩-ScoreDao.UpdateScore
                                client1.score.setWinCount(currentScore.getWinCount()+1);//服务器数据更新
                                client1.score.setTotalCount(currentScore.getTotalCount()+1);//服务器数据更新
                                // 数据库数据更新
                                ScoreDAO.UpdateScore(client1.getMysqlConn(),client1.user.getId(),client1.score.getTotalCount(),client1.score.getWinCount());
                                // 发送最新的用户信息到客户端
                                String newPlayerInfo=client1.score.getTotalCount()+"#"+client1.score.getWinCount();
                                server.SendResponseToClient(newPlayerInfo,ActionType.UpdatePlayerInfo,channel1);
                            }


                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 战斗中退出
     * @param data
     * @param channel
     * @param server
     * @return
     */
    public String QuitInBattle(String data,Channel channel,Server server)
    {
        //不必解析数据
        Client client=server.clientDictionary.get(channel);
        Room currentRoom=client.currentEnteredRoom;
        if(currentRoom!=null)
        {
            currentRoom.BroadCastMessage(null,ActionType.QuitInBattle,"null");
            currentRoom.Close();
        }
        return null;
    }
}
