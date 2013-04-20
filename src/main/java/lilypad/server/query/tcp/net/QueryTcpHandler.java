package lilypad.server.query.tcp.net;

import java.util.Arrays;
import java.util.Set;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import lilypad.server.common.IPlayable;

@Sharable
public class QueryTcpHandler extends ChannelInboundMessageHandlerAdapter<String> {

	private IPlayable playable;
	
	public QueryTcpHandler(IPlayable playable) {
		this.playable = playable;
	}

	public void messageReceived(final ChannelHandlerContext context, String string) throws Exception {
		final Channel channel = context.channel();
		String response = this.generateResponse(string.toUpperCase());
		if(response != null) {
			channel.write(response).addListener(new ChannelFutureListener() {
				public void operationComplete(ChannelFuture future) throws Exception {
					channel.close();
				}
			});
		} else {
			channel.close();
		}
	}
	
	private String generateResponse(String request) {
		StringBuilder response = new StringBuilder();
		Set<String> players = this.playable.getPlayers();
		if(request.startsWith("QUERY_JSON")) {
			response.append(new GsonResponse(this.playable.getBindAddress().getPort(), this.playable.getPlayerMaximum(), players).toGson());
			response.append("\r\n");
			return response.toString();
		}
		if(request.startsWith("QUERY")) {
			response.append("SERVERPORT " + this.playable.getBindAddress().getPort() + "\r\n");
			response.append("PLAYERCOUNT " + players.size() + "\r\n");
			response.append("MAXPLAYERS " + this.playable.getPlayerMaximum() + "\r\n");
			response.append("PLAYERLIST " + Arrays.toString(players.toArray(new String[0])) + "\r\n");
			return response.toString();
		}
		return null;
	}
	
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) throws Exception {
		Channel channel = context.channel();
		//cause.printStackTrace(); ignore
		if(channel.isOpen()) {
			channel.close();
		}
	}
	
}
