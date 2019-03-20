package Controller;
import Enum.RequestType;
import Handler.Response;
import Servers.Server;
import Tools.ConvertTools;
import Tools.Tool;
import io.netty.channel.Channel;
import Servers.Room;
import Enum.RoleColor;
import Enum.ReturnType;
import Enum.RoomState;
import Enum.ActionType;
import Servers.Client;


public class RoomController extends BaseController {
    public RoomController(){
        requestType=RequestType.Room;
    }

    /**
     * 处理创建房间请求
     * @param data
     * @param channel
     * @param server
     * @return
     */
    public String CreateRoom(String data, Channel channel, Server server)
    {
        //创建房间请求没有传有效数据，不用解析data
        Room newRoom=new Room(server);
        //将该channel加入到room【下面这个函数已经将该channel对应的client的currentEnteredRoom进行了设置】
        newRoom.AddChannelToRoom(channel);
        //将该房间加入到Server的房间列表
        server.Roomlist.add(newRoom);
        //向发起创建房间的客户端【即房主】返回成功，并分配角色
        return String.valueOf(ReturnType.Successful.ordinal())+"#"+String.valueOf(RoleColor.Blue.ordinal());
    }

    /**
     * 列出房间列表，需要在客户端展示，房主名，房主总场数，房主胜利数
     * @param data
     * @param channel
     * @param server
     * @return
     */
    public String ListRoom(String data,Channel channel,Server server)
    {
        String roomListInfo="";
        //只显示处于等待加入的房间
        for(Room room :server.Roomlist)
        {
            if(room.roomState==RoomState.WaitingJoin)
            {
                //每个房间之间用*分割，房间内部的信息用#作为分割
                roomListInfo+=room.GetHouseOwnerData()+"*";

            }
        }
        //如果有房间数据，那么最后会多一个*，需要删除这个*号
        if(roomListInfo.length()>0)
        {
            roomListInfo= Tool.removeCharAt(roomListInfo,roomListInfo.length()-1);
            System.out.println("房间列表信息为："+roomListInfo);
            return roomListInfo;
        }else {
            //没有房间
            System.out.println("没有房间");
            //返回一个为"EMPTY"的字符串
            roomListInfo="EMPTY";
            return roomListInfo;
        }
    }

    /**
     * 加入房间请求
     * @param data
     * @param channel
     * @param server
     * @return
     */
    public String JoinRoom(String data,Channel channel,Server server)
    {
        //解析获得房间id
        int roomId=Integer.parseInt(data);
        //通过房间id查找到房间
        Room room=server.getRoomById(roomId);
        if(room==null)
        {
            //房间不存在，不可加入
            return String.valueOf(ReturnType.Failed.ordinal());

        }else {
            //房间存在
            //判断房间状态
            if(room.roomState==RoomState.WaitingJoin) {
                //将P2玩家加入房间
                room.AddChannelToRoom(channel);
                //获得房间中所有玩家的信息
                String playersInfoInRoom=room.GetInfoOfPlayerInfoInRoom(true);
                // 广播给房间中非该加入者【即房主】
                room.BroadCastMessage(channel,ActionType.UpdateRoom,playersInfoInRoom);
                //返回给该加入者，并分配角色，和把playersInfoInRoom传过去；涉及3个部分
                //由于playersInfoInRoom已经使用了* #作为分割，所以现在用-进行分割
                String response=String.valueOf(ReturnType.Successful.ordinal())+"-"+String.valueOf(RoleColor.Red.ordinal())+"-"+playersInfoInRoom;
                return response;
            }else{
                //房间存在，但不可加入，返回给当前正在加入别人房间的这个客户端
                return String.valueOf(ReturnType.Failed.ordinal());
            }
        }
    }

    /**
     * 退出房间
     * @param data
     * @param channel
     * @param server
     * @return
     */
    public String QuitRoom(String data,Channel channel,Server server)
    {
        //不用解析data
        //先判断当前这个channel是否是房主
        Client quitClient=server.clientDictionary.get(channel);
        Room currentRoom=quitClient.currentEnteredRoom;
        if(currentRoom.getRoomChannelList().get(0)==channel)
        {
            //为房主-广播给其他客户端让他们退出且需删除掉这个房间
            currentRoom.BroadCastMessage(channel,ActionType.QuitRoom,String.valueOf(ReturnType.Successful.ordinal()));
            //关闭这个房间
            currentRoom.Close();
            //向房主返回退出成功
            return String.valueOf(ReturnType.Successful.ordinal());

        }else {
            //非房主-不用删除房间,向其他客户端【即房主】广播有客户端已经退出
            currentRoom.BroadCastMessage(channel,ActionType.UpdateRoom,currentRoom.GetInfoOfPlayerInfoInRoom(false));
            //从该房间中移除该客户端
            currentRoom.RemoveChannelFromRoom(channel);
            //向退出的这个客户端返回成功
            return  String.valueOf(ReturnType.Successful.ordinal());
        }
    }
}
