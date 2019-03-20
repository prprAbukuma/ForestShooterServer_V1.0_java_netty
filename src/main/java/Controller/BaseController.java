package Controller;
import Enum.RequestType;
import Enum.ActionType;
import Servers.Server;
import io.netty.channel.Channel;
public class BaseController {
    public RequestType requestType=RequestType.None;

    public String DefaultHandle(String data, Channel clientChannel, Server server)
    {
        return null;
    }
}
