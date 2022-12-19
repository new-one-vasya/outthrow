package nw.one.vasya.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public class CollectHandler extends SimpleChannelInboundHandler<String> {

    private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(CollectHandler.class);
    private volatile State state = State.EMPTY;
    private String message;

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        LOGGER.info("Connection for client - {}", ctx);
    }
    @Override
    public void channelRead0(ChannelHandlerContext ctx, String msg) {
        String str = msg.trim();
        if (isEmptyString(str)) {
            return;
        }
        if (state == State.EMPTY) {
            message = str;
            state = State.HALF;
        } else if (state == State.HALF) {
            ctx.fireChannelRead(message + ":" + str); // сводим задачу к известной
            state = State.EMPTY;
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        LOGGER.info("Closing connection for client - {}", ctx);
        ctx.close();
    }

    private enum State {
        EMPTY,
        HALF
    }

    /**
     * Именно так, потому что trim  уже случился
     */
    boolean isEmptyString(String string) {
        return string == null || string.isEmpty() || string.isBlank();
    }
}