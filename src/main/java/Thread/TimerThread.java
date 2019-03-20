package Thread;
import Enum.ActionType;
import Servers.Room;
public class TimerThread extends  Thread {
    private Room currentRoom;
    private int seconds;
    public TimerThread(Room currentRoom,int seconds)
    {
        this.currentRoom=currentRoom;
        this.seconds=seconds;
    }

    @Override
    public void run(){
        try {
            Thread.sleep(1000);
            for (int i = 3; i >= 1; i--) {
                //广播倒计时
                currentRoom.BroadCastMessage(null, ActionType.ShowTimer, String.valueOf(i));
                Thread.sleep(1000);
            }
            //倒计时结束后-发送开始游玩响应
            currentRoom.BroadCastMessage(null, ActionType.StartPlay, "null");
        }catch (Exception e)
        {
            System.out.println("执行倒计时线程时发送异常：异常信息为："+e);
        }

    }

}
