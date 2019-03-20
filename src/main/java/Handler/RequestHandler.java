package Handler;

import Servers.Client;
import Servers.Server;
import Tools.ConvertTools;
import com.dyuproject.protostuff.ProtobufIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import Enum.ActionType;
import Enum.RequestType;
import Servers.Room;

public class RequestHandler extends ChannelInboundHandlerAdapter {
    //解码器的类样式
    private Schema<Request> schema= RuntimeSchema.getSchema(Request.class);
    //保存所有连接的Channel，用于消息广播//相当于一个列表
    private static ChannelGroup allChannels=new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);


    // Channel连接成功时，加入allChannels
    @Override
    public void channelActive(ChannelHandlerContext ctx)throws Exception{
        allChannels.add(ctx.channel());
        Server.Instance().clientDictionary.put(ctx.channel(), Server.Instance().currentOnlineClientList.get(Server.Instance().RequestHanlderCounter));
        System.out.println("currentOnlineClientList列表的长度"+Server.Instance().currentOnlineClientList.size());
        System.out.println("当前下标"+ Server.Instance().RequestHanlderCounter +"  channel为"+ctx.channel()+"所对应的Client对象是"+Server.Instance().currentOnlineClientList.get( Server.Instance().RequestHanlderCounter));
        Server.Instance().RequestHanlderCounter++;
        System.out.println(ctx.channel().localAddress()+"已加入");//此处会打印加入计算机的计算机名字
        super.channelActive(ctx);
    }
    //Channel连接断开时【客户端关闭的情况】，从allChannels中移除
    @Override
    public void channelInactive(ChannelHandlerContext ctx)throws Exception{
        // 还要处理这个这个ctx.channel所对应的client如果有所在房间，且还是房主情况下，房间的销毁
        Room room=Server.Instance().clientDictionary.get(ctx.channel()).currentEnteredRoom;
        //判断是否为房主
        if(room!=null&&room.getRoomChannelList().get(0)==ctx.channel())
        {
            //若为房主，则需要销毁这个房间
            room.Close();
        }
        //获得这个channel所对应的client对象
        Client removedClient=Server.Instance().clientDictionary.get(ctx.channel());
        //从Server中的在线Client列表中移除
        Server.Instance().currentOnlineClientList.remove(removedClient);
        //从Server中的这个ClientDic把这一组键值对移除
        Server.Instance().clientDictionary.remove(ctx.channel());
        //全局计数器--
        Server.Instance().RequestHanlderCounter--;
        allChannels.remove(ctx.channel());
        System.out.println(ctx.channel().localAddress()+"已离开");
        super.channelInactive(ctx);
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("收到了请求");
        byte[] dataBytes= ConvertTools.ParseMsg(msg);
        //System.out.println(dataBytes.length);
        if(dataBytes==null||dataBytes.length==0)
        {
            return;
        }
        //Protobuf解码-获得RequestFromClient对象
        Request requestFromClient=new Request();
        ProtobufIOUtil.mergeFrom(dataBytes,requestFromClient,schema);
        System.out.println("收到请求："+requestFromClient.requestType+"即RequestType为"+RequestType.values()[requestFromClient.requestType]+" "+requestFromClient.actionType+"即ActionType为"+ActionType.values()[requestFromClient.actionType]+" 数据部分"+requestFromClient.data);
        //此时已经拿到requestFromClient对象。【此时需要取得里面的东西，然后执行对应Controller中的对应方法】
        //将数字转化为枚举-然后去执行对应的请求
        RequestType requestType=RequestType.values()[requestFromClient.requestType];
        ActionType actionType=ActionType.values()[requestFromClient.actionType];
        Server.Instance().getControllerManager().HandleRequeust(requestType,actionType,requestFromClient.data,ctx.channel());

    }
}
