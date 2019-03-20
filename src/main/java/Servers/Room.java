package Servers;

import Tools.Tool;
import io.netty.channel.Channel;
import Enum.RoomState;
import Thread.TimerThread;
import Enum.ActionType;

import java.util.ArrayList;
import java.util.List;

public class Room {
    //用于存储处于房间内的channel
    private List<Channel> roomChannelList=new ArrayList<Channel>();
    //当前房间状态
    public RoomState roomState=RoomState.WaitingJoin;
    //Server引用
    private Server currentServer;
    //玩家最大血量
    private static final int MAX_HP = 100;

    public Room(Server currentServer)
    {
        this.currentServer=currentServer;
        System.out.println("房间已创建");
    }
    public List<Channel> getRoomChannelList(){
        return roomChannelList;
    }

    /**
     * 向房间中添加channel
     * @param channel
     */
    public void AddChannelToRoom(Channel channel)
    {
        //将该channel加入房间
        roomChannelList.add(channel);
        //设置初始血量
        Client client= currentServer.clientDictionary.get(channel);
        client.CurrentHP=MAX_HP;
        //设置这个Client的当前所在房间
        client.currentEnteredRoom=this;
        //每次加入后判断列表中数量，>=则改为WaitingStart
        if(roomChannelList.size()>=2)
        {
            roomState=RoomState.WaitingStart;
        }
    }

    /**
     * 从房间的channel列表中移除指定的channel
     * @param channel
     */
    public void RemoveChannelFromRoom(Channel channel)
    {
        //获得channel对应的Client
        Client removeClient=currentServer.clientDictionary.get(channel);
        //将该client的room置空
        removeClient.currentEnteredRoom=null;
        //移除该channel
        roomChannelList.remove(channel);
        // 判断房间客户端数量，改变状态为可加入
        if(roomChannelList.size()<2&&roomChannelList.size()>=0)
        {
            roomState=RoomState.WaitingJoin;
        }

    }

    /**
     *得到房主信息，并组拼发送至客户端
     * @return
     */
    public String GetHouseOwnerData()
    {
        //房主信息需要四个，userid，username，totalcount，wincount
        //房间也是需要id来唯一确定，此处就使用房主的id
        Client houseOwnerClient=currentServer.clientDictionary.get(roomChannelList.get(0));
        //测试
        System.out.println("房主channel是"+roomChannelList.get(0));
        String data=houseOwnerClient.user.getId()+"#"+houseOwnerClient.user.getUsername()+"#"+houseOwnerClient.score.getTotalCount()+"#"+houseOwnerClient.score.getWinCount();
        return data;
    }

    /**
     * 获得房间内玩家的信息
     * @param isHaveP2
     * @return
     */
    public String GetInfoOfPlayerInfoInRoom(boolean isHaveP2)
    {
        if(isHaveP2)
        {
            //两个玩家之间的信息用*号分割
            String data=GetHouseOwnerData()+"*";
            Client p2client=currentServer.clientDictionary.get(roomChannelList.get(1));
            String p2data=p2client.user.getId()+"#"+p2client.user.getUsername()+"#"+p2client.score.getTotalCount()+"#"+p2client.score.getWinCount();
            data+=p2data;
            return data;

        }else {
            //只发送房主信息
            return GetHouseOwnerData();
        }
    }

    /**
     * 向房间中的客户端广播响应
     * @param excludeChannel 需要剔除的channel
     * @param responseDataObject 响应数据的内容
     */
    public void BroadCastMessage(Channel excludeChannel,ActionType actionType,Object responseDataObject)
    {
        for(Channel channel : roomChannelList)
        {
            if(channel!=excludeChannel)
            {
                currentServer.SendResponseToClient(responseDataObject,actionType,channel);
            }
        }
    }

    /**
     * 销毁房间-房主退出房间时调用-正常情况下
     */
    public void Close()
    {
        //将所有房间内的channel所对应的client中的currentEnteredRoom置空
        for(Channel channel:roomChannelList)
        {
            Client client=currentServer.clientDictionary.get(channel);
            client.currentEnteredRoom=null;
        }
        //将该房间从server的房间列表中移除
        currentServer.Roomlist.remove(this);
    }

    //实现倒计时
    /**
     * 开启计时器线程
     */
    public void StartTimerThread()
    {

        Thread t1=new TimerThread(this,3);
        t1.start();
    }

    /**
     * 获得当前房间内所有玩家的血量
     * @return
     */
    public String GetAllClientHP()
    {
        String playerHp="";
        for(Channel channel :roomChannelList)
        {
            Client client=currentServer.clientDictionary.get(channel);
            playerHp+=client.CurrentHP+"#";
        }
        //删除结尾多余的#号
        playerHp=Tool.removeCharAt(playerHp,playerHp.length()-1);
        return playerHp;
    }


}
