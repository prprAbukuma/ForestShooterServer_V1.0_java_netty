package Handler;
import Enum.ActionType;
public class Response {
    public int actionType;
    public String responseData;
    public Response(){}
    public Response(int actionType,String responseData)
    {
        this.actionType=actionType;
        this.responseData=responseData;
    }

}
