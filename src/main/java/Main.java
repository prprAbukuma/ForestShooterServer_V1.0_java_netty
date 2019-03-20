import DAO.UserDAO;
import Model.User;
import Servers.Server;
import Tools.ConnHelper;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.sql.Connection;

public class Main {
    public static void main(String[] args){

        Server.Instance().StartServer();




    }
}
