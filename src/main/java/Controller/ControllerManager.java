package Controller;
import Enum.RequestType;
import Enum.ActionType;
import Handler.Response;
import Servers.Server;
import Tools.ConvertTools;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import io.netty.buffer.ByteBuf;

import java.lang.reflect.Method;
import java.util.Dictionary;
import java.util.Hashtable;
import io.netty.channel.Channel;

public class ControllerManager {
    private Dictionary<RequestType,BaseController> controllerDictionary=new Hashtable<RequestType, BaseController>();
    private Server server;

    public ControllerManager(Server server){
        this.server=server;
        InitController();
    }

    /**
     * 实例化所有Controller并加入字典
     */
    public void InitController()
    {
        //生成Controller，并加入字典
        controllerDictionary.put(RequestType.None,new DefaultController());
        controllerDictionary.put(RequestType.User,new UserController());
        controllerDictionary.put(RequestType.Room,new RoomController());
        controllerDictionary.put(RequestType.Game,new GameController());
    }

    /**
     * 处理来自客户端的请求
     * @param requestType
     * @param actionType
     * @param data
     *
     */
    public void HandleRequeust(RequestType requestType, ActionType actionType,String data, Channel clientChannel)
    {
        BaseController controller=controllerDictionary.get(requestType);
        if(controller==null)
        {
            System.out.println("未找到"+requestType+"对应的Controller");
            return;
        }
        String methodName=actionType.toString();
        try{
            Method methodInfo=controller.getClass().getMethod(methodName,Class.forName("java.lang.String"),Channel.class,Server.class);//与C#不同，这里就要指定出这个函数的参数类型
            if(methodInfo==null)
            {
                System.out.println("未找到类"+controller.getClass()+"对应的方法"+methodName);
                return;
            }
            //Object[] parameters=new Object[]{data,clientChannel,server};//传入参数【与C#不同，不能传入数组】
            Object responseObj=methodInfo.invoke(controller,data,clientChannel,server);
            //判断该请求是否有响应
            if(responseObj!=null&&responseObj!="")
            {
                //返回响应-到客户端
                server.SendResponseToClient(responseObj,actionType,clientChannel);
            }
        }catch (Exception e){
            System.out.println("在处理请求时发生异常，异常信息为"+e);
        }

    }
}
