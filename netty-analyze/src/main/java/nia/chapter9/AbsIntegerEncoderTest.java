package nia.chapter9;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by fw on 2020/5/26
 */
public class AbsIntegerEncoderTest {
    @Test
    public void testEncoded() {
        // 创建一个 ByteBuf ，并且写入 9 个负整数
        ByteBuf buf = Unpooled.buffer();
        for (int i = 1; i < 10; i++) {
            buf.writeInt(i * -1);
        }

        // 创建一个 EmbeddedChannel ，并安装一个要测试的 AbsIntegerEncoder
        EmbeddedChannel channel = new EmbeddedChannel(new AbsIntegerEncoder());
        Assert.assertTrue(channel.writeOutbound(buf));
        // 将该 Channel 标记为已完成状态
        Assert.assertTrue(channel.finish());

        // 读取所产生的消息，并断言它们包含了对应的绝对值
        for (int i = 1; i < 10; i++) {
            Assert.assertTrue(channel.readOutbound().equals(i));
        }
        Assert.assertNull(channel.readOutbound());
    }
}