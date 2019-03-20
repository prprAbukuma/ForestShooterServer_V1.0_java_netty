package Controller;
import DAO.ScoreDAO;
import DAO.UserDAO;
import Enum.RequestType;
import Enum.ReturnType;
import Model.Score;
import Model.User;
import Servers.Client;
import Servers.Server;
import io.netty.channel.Channel;
import jdk.nashorn.internal.ir.ReturnNode;


public class UserController extends BaseController {
    public UserController()
    {
        requestType= RequestType.User;
    }

    /**
     * 处理注册请求
     * @param data
     * @param channel
     * @param server
     * @return
     */
    public String Register(String data, Channel channel, Server server)
    {

        //data已经是用户的数据，包含用户名和密码，进行解析
        String []dataStrArr=data.split("#");
        String username=dataStrArr[0];
        String password=dataStrArr[1];
        System.out.println("执行注册：用户名"+username+"密码"+password);
        //问题所在：通过channel找到Client对象
        Client client=server.clientDictionary.get(channel);
        boolean isSuccessful= UserDAO.ResgisterUser(client.getMysqlConn(),username,password);
        if(isSuccessful)
        {
            return String.valueOf(ReturnType.Successful.ordinal());
        }else {
            return String.valueOf(ReturnType.Failed.ordinal());
        }



    }

    /**
     * 处理登录请求
     * @param data
     * @param channel
     * @param server
     * @return
     */
    public String Login(String data, Channel channel, Server server)
    {
        String[] dataStrArr=data.split("#");//必须保证密码中没有#号，否则翻车
        String username=dataStrArr[0];
        String password=dataStrArr[1];
        System.out.println("执行登录：用户名"+username+"密码"+password);
        Client client=server.clientDictionary.get(channel);
        User user= UserDAO.VerifyUser(client.getMysqlConn(),username,password);
        if(user!=null)
        {
            // 说明登录成功-要把战绩信息也返回过去
            Score score= ScoreDAO.getScoreByUserid(client.getMysqlConn(),user.getId());
            if(score!=null)
            {
                //给当前的Client中user和score对象进行赋值
                client.user=user;
                client.score=score;
                //按#进行组拼
                String response= String.valueOf(ReturnType.Successful.ordinal())+"#"+user.getId()+"#"+user.getUsername()+"#"+score.getTotalCount()+"#"+score.getWinCount();
                return response;
            }else{
                //给当前的Client中user和score对象进行赋值
                client.user=null;
                client.score=null;
                //登录成功，但由于某种情况，没有找到其战绩，依然要返回，只不过数据为-1，代表有问题
                String response= String.valueOf(ReturnType.Successful.ordinal())+"#"+user.getId()+"#"+user.getUsername()+"#"+-1+"#"+-1;
                return response;
            }

        }else{
            //登录失败
            return String.valueOf(ReturnType.Failed.ordinal());
        }
    }
}
