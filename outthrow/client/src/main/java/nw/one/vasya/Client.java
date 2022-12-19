package nw.one.vasya;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import nw.one.vasya.handler.ClientHandler;
import nw.one.vasya.handler.CollectHandler;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Client {

    public static final int PORT = 8080;
    public static final String HOST = "localhost";
    private static final List<String> COMMANDS = new ArrayList<>(List.of("5\nhello\n", "4\ncool\n", "6\nhaha\n"));

    public static void main(String[] args) throws Exception {
        EventLoopGroup workerGroup = new NioEventLoopGroup(1);

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) {
                    ch.pipeline().addLast(
                            new LineBasedFrameDecoder(32),
                            new StringDecoder(StandardCharsets.UTF_8),
                            new StringEncoder(StandardCharsets.UTF_8),
                            new CollectHandler(),
                            new ClientHandler()
                    );
                }
            });

            ChannelFuture lastWriteFuture = null;
            Channel channel = bootstrap.connect(HOST, PORT).sync().channel();

            for (String line : COMMANDS) {
                lastWriteFuture = channel.writeAndFlush(line);
                Thread.sleep(100); // Обходное решение склеивания запросов в один пакет
            }
            channel.closeFuture().sync();

            if (lastWriteFuture != null) {
                lastWriteFuture.sync();
            }

        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}