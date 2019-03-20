package Handler;
import Enum.RequestType;
import Enum.ActionType;
public class Request {
    public int requestType;
    public int actionType;
    public String data;
    public Request(){}
    public Request(int requestType, int actionType,String data)
    {
        this.requestType=requestType;
        this.actionType=actionType;
        this.data=data;
    }
}
