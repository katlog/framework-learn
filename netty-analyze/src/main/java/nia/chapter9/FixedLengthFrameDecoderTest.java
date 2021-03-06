package nia.chapter9;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by fw on 2020/5/26
 */
public class FixedLengthFrameDecoderTest {

    @Test
    public void testFramesDecoded() {

        // 创建一个 ByteBuf ， 并存储 9 个字节
        ByteBuf buf = Unpooled.buffer();
        for (int i = 0; i < 9; i++) {
            buf.writeByte(i);
        }
        ByteBuf input = buf.duplicate();
        // 创建一个 EmbeddedChannel ，并添加一个 FixedLengthFrameDecoder，其将以 3 字节的帧长度被测试
        EmbeddedChannel channel = new EmbeddedChannel(new FixedLengthFrameDecoder(3));
        // 将数据写入 EmbeddedChannel
        Assert.assertTrue(channel.writeInbound(input.retain()));
        // 标记 Channel 为已完成状态
        Assert.assertTrue(channel.finish());

        // 读取所生成的消息，并且验证是否有 3 帧（切片），其中每帧（切片）都为 3 字节
        ByteBuf read = channel.readInbound();
        Assert.assertTrue(buf.readSlice(3).equals(read));
        read.release();

        read = channel.readInbound();
        Assert.assertTrue(buf.readSlice(3).equals(read));
        read.release();

        read = channel.readInbound();
        Assert.assertTrue(buf.readSlice(3).equals(read));
        read.release();

        Assert.assertNull(channel.readInbound());
        buf.release();

    }

    @Test
    public void testFramesDecoded2() {

        ByteBuf buf = Unpooled.buffer();
        for (int i = 0; i < 9; i++) {
            buf.writeByte(i);
        }
        ByteBuf input = buf.duplicate();

        EmbeddedChannel channel = new EmbeddedChannel(new FixedLengthFrameDecoder(3));
        // 返回 false ，因为没有一个完整的可供读取的帧
        Assert.assertTrue(!channel.writeInbound(input.readBytes(2)));
        Assert.assertTrue(channel.writeInbound(input.readBytes(7)));

        Assert.assertTrue(channel.finish());
        ByteBuf read = channel.readInbound();
        Assert.assertTrue(buf.readSlice(3).equals(read));
        read.release();

        read = channel.readInbound();
        Assert.assertTrue(buf.readSlice(3).equals(read));
        read.release();

        read = channel.readInbound();
        Assert.assertTrue(buf.readSlice(3).equals(read));
        read.release();

        Assert.assertNull(channel.readInbound());
        buf.release();
    }

}