package Servers;

import Controller.ControllerManager;
import DAO.UserDAO;
import Handler.RequestHandler;
import Handler.Response;
import Tools.ConnHelper;
import Tools.ConvertTools;
import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtobufIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.omg.CORBA.PUBLIC_MEMBER;
import Model.User;
import Enum.ActionType;

import java.sql.Connection;
import java.util.*;

public class Server {
    private static Server instance;
    public static Server Instance(){
        if(instance==null)
        {
            instance=new Server();
        }
        return instance;
    }

    private ControllerManager controllerManager;
    public Server(){
        controllerManager=new ControllerManager(this);
    }

    public ControllerManager getControllerManager()
    {
        return controllerManager;
    }
    //当前登入的client对象的列表
    public List<Client> currentOnlineClientList=new ArrayList<Client>();
    //当前登入的channel，通过channel找client对象的字典
    public Dictionary<Channel,Client> clientDictionary=new Hashtable<Channel, Client>();
    //当前存在的房间
    public List<Room> Roomlist=new ArrayList<Room>();
    //全局下标计数器：保证clientDictionary中channel和Client能够正确对应
    public int RequestHanlderCounter=0;
    public void StartServer()
    {
        //Client client=new Client(currentOnlineClient,currentOnlineClient,this);
        EventLoopGroup bossGroup = new NioEventLoopGroup();//用于接收客户端的TCP连接
        EventLoopGroup workerGroup = new NioEventLoopGroup();//另一个用于处理I/O相关的读写操作，或者执行系统Task、定时任务Task等
        try{
            ServerBootstrap bs=new ServerBootstrap();
            bs.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            System.out.println("报告");
                            System.out.println("信息：有一客户端链接到本服务端");
                            System.out.println("IP:" + channel.localAddress().getHostName());
                            System.out.println("Port:" + channel.localAddress().getPort());
                            System.out.println("报告完毕");

                            //建立数据库连接
                            Connection conn= ConnHelper.ConnectToMySql();
                            //创建Client对象
                            Client client=new Client(channel,conn,instance);
                            //加入Client列表
                            currentOnlineClientList.add(client);
                            System.out.println("channel为"+channel+ "的Client已创建");
                            channel.pipeline().addLast(new RequestHandler());//客户端触发操作。注册客户端触发的操作

                        }

                    });
            ChannelFuture start=bs.bind(6688).sync();//服务器异步创建绑定
            System.out.println("启动正在监听:"+start.channel().localAddress());
            start.channel().closeFuture().sync();//关闭服务器通道
        }catch (Exception e)
        {
            System.out.println("开启服务失败，异常信息为："+e);
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();//释放线程池单元
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * 向客户端发送响应
     * @param responseDataObj 响应的内容-只含数据部分
     * @param actionType 响应类型
     * @param clientChannel 需要得到该响应的客户端
     */

    public void SendResponseToClient(Object responseDataObj, ActionType actionType,Channel clientChannel)
    {
        //注意：并不会把actionType直接作为一个部分，拼接发送出去，actiontype是用于来创建Response对象的
        //发送的东西，只是这个Response对象
        //要封装一个对象，专门响应的对象
        String responseData=responseDataObj.toString();//响应数据转化为字符串
        Response responseToClientObj=new Response(actionType.ordinal(),responseData);
        //转换为ByteBuff
        ByteBuf byteBuf= ConvertTools.ConvertResponseObjToByteBuf(responseToClientObj, RuntimeSchema.getSchema(Response.class));
        //将响应发生给客户端
        clientChannel.write(byteBuf);
        clientChannel.flush();
    }

    /**
     *
     * @param roomId
     * @return
     */
    public Room getRoomById(int roomId)
    {
        for(Room room :Roomlist)
        {
            Client roomOwnerClient=clientDictionary.get(room.getRoomChannelList().get(0));
            if(roomOwnerClient.user.getId()==roomId)
            {
                return room;
            }
        }
        return null;
    }

}
