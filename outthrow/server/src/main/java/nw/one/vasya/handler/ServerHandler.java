package nw.one.vasya.handler;

import io.netty.channel.*;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public class ServerHandler extends SimpleChannelInboundHandler<String> {

    private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(ServerHandler.class);

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String msg) {
        LOGGER.info("Message received: {}", msg);
        replay(ctx.channel(), msg.trim());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.info("Closing connection for client - {}", ctx);
        ctx.close();
    }

    public void replay(Channel channel, String msg) {
        if (isValid(msg)) {
            send(channel, Answer.WIN.getAnswer());
        } else {
            send(channel, Answer.LOOSE.getAnswer());
        }
    }

    private boolean isValid(String msg) {
        String[] split = msg.split(":", 2);
        return Integer.parseInt(split[0]) == split[1].length();
    }

    private void send(Channel channel, String answer) {
        channel.writeAndFlush(answer);
    }

    private enum Answer {
        WIN("2\nok\n"),
        LOOSE("3\nerr\n");

        final String answer;
        Answer(String answer) {
            this.answer = answer;
        }

        public String getAnswer() {
            return answer;
        }
    }
}