package Servers;

import java.sql.Connection;

import Model.Score;
import Model.User;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

import java.nio.channels.Channel;


public class Client {
    private Connection mysqlConn;//该客户端与Mysql数据库的连接
    private SocketChannel socketChannel;
    private Server currentServer;

    //Client对象的玩家信息
    public User user;//当前用户
    public Score score;//当前玩家的战绩
    public int CurrentHP;//当前血量
    public Room currentEnteredRoom;//当前所进入的房间

    public SocketChannel getSocketChannel(){
        return socketChannel;
    }
    public Connection getMysqlConn(){return mysqlConn; }
    private boolean isDie=false;
    public boolean IsDie(){
        return isDie;
    }
    public Client(){}

    public Client(SocketChannel socketChannel, Connection mysqlConn,Server currentServer)
    {
        this.mysqlConn=mysqlConn;
        this.socketChannel=socketChannel;
        this.currentServer=currentServer;

    }

    /**
     * 扣血函数，返回bool，为真时代表死亡，游戏结束
     * @param deltaHp
     * @return
     */
    public boolean DecreaseHP(int deltaHp)
    {
        CurrentHP-=deltaHp;
        if(CurrentHP<=0)
        {
            CurrentHP=0;
            isDie=true;
            return true;
        }
        return false;
    }

}
