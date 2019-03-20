package Tools;

import Handler.Response;
import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtobufIOUtil;
import com.dyuproject.protostuff.Schema;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ConvertTools {
    /**
     * int转换成byte数组
     * @param data
     * @param offset
     * @return
     */
    public static int bytesToInt(byte[] data, int offset) {
        int num = 0;
        for (int i = offset; i < offset + 4; i++) {
            num <<= 8;
            num |= (data[i] & 0xff);
        }
        return num;
    }

    /**
     * int转换成byte数组
     * @param num
     * @return
     */
    public static byte[] intToBytes(int num) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) (num >>> (24 - i * 8));
        }
        return b;
    }

    /**
     * 用于解析channelRead传来的msg对象，转化为byte数组
     * @param msg
     * @return
     */
    public static byte[] ParseMsg(Object msg)
    {
        if (!(msg instanceof ByteBuf)) {
            return null;
        }
        ByteBuf data = (ByteBuf) msg;
        // 前面4个字节存储长度
        if (data.readableBytes() < 4) {
            return null;
        }
        data.markReaderIndex();
        // 解析出消息体的长度
        byte[] lenBytes = new byte[4];
        data.readBytes(lenBytes);
        int length = ConvertTools.bytesToInt(lenBytes, 0);
        // 消息体长度不够，继续等待
        if (data.readableBytes() < length) {
            data.resetReaderIndex();
            return null;
        }
        // 解析出消息体
        byte[] dataBytes = new byte[length];
        data.readBytes(dataBytes);
        return dataBytes;
    }

    /**
     * 将传输用的数据对象[响应]转化为ByteBuf
     */
    public static ByteBuf ConvertResponseObjToByteBuf(Response responseMsg, Schema schema)
    {
        LinkedBuffer buffer1=LinkedBuffer.allocate(1024);
        byte[] msgData= ProtobufIOUtil.toByteArray(responseMsg,schema,buffer1);
        ByteBuf responseData= Unpooled.copiedBuffer(
                ConvertTools.intToBytes(msgData.length),msgData
        );

        return responseData;
    }


}
