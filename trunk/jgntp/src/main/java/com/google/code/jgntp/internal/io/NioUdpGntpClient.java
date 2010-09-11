package com.google.code.jgntp.internal.io;

import java.net.*;
import java.util.concurrent.*;

import org.jboss.netty.bootstrap.*;
import org.jboss.netty.channel.socket.*;
import org.jboss.netty.channel.socket.oio.*;

import com.google.code.jgntp.*;
import com.google.code.jgntp.internal.message.*;
import com.google.common.base.*;
import com.google.common.collect.*;

public class NioUdpGntpClient extends NioGntpClient {

	private final ConnectionlessBootstrap bootstrap;
	private final DatagramChannel datagramChannel;

	public NioUdpGntpClient(GntpApplicationInfo applicationInfo, SocketAddress growlAddress, Executor executor, GntpPassword password, boolean encrypted) {
		super(applicationInfo, growlAddress, password, encrypted);
		Preconditions.checkNotNull(executor, "Executor must not be null");

		bootstrap = new ConnectionlessBootstrap(new OioDatagramChannelFactory(executor));
		bootstrap.setPipelineFactory(new GntpChannelPipelineFactory(new GntpChannelHandler(this, null)));
		bootstrap.setOption("broadcast", "false");

		datagramChannel = (DatagramChannel) bootstrap.bind(new InetSocketAddress(0));
	}

	@Override
	protected void doRegister() {
		GntpMessage message = new GntpRegisterMessage(getApplicationInfo(), getPassword(), isEncrypted());
		datagramChannel.write(message, getGrowlAddress());
		getRegistrationLatch().countDown();
	}

	@Override
	protected void doNotify(GntpNotification notification) {
		GntpNotifyMessage message = new GntpNotifyMessage(notification, -1, getPassword(), isEncrypted());
		datagramChannel.write(message, getGrowlAddress());
	}

	@Override
	protected void doShutdown(long timeout, TimeUnit unit) throws InterruptedException {
		datagramChannel.close().await(timeout, unit);
		bootstrap.releaseExternalResources();
	}

	@Override
	BiMap<Long, Object> getNotificationsSent() {
		throw new UnsupportedOperationException();
	}

	@Override
	boolean canRetry() {
		return false;
	}

	@Override
	void retryRegistration() {
		throw new UnsupportedOperationException();
	}
}
