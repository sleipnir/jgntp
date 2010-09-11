package com.google.code.jgntp.internal.io;

import java.net.*;
import java.util.concurrent.*;

import org.jboss.netty.bootstrap.*;
import org.jboss.netty.channel.socket.*;
import org.jboss.netty.channel.socket.nio.*;
import org.slf4j.*;

import com.google.code.jgntp.*;
import com.google.code.jgntp.internal.message.*;
import com.google.common.base.*;

public class NioUdpGntpClient implements GntpClient {

	private static final Logger logger = LoggerFactory.getLogger(NioUdpGntpClient.class);

	private final GntpApplicationInfo applicationInfo;
	private final SocketAddress growlAddress;
	private final GntpPassword password;
	private final boolean encrypted;

	private final ConnectionlessBootstrap bootstrap;
	private final DatagramChannel datagramChannel;
	private final CountDownLatch registrationLatch;
	private volatile boolean closed;

	public NioUdpGntpClient(GntpApplicationInfo applicationInfo, SocketAddress growlAddress, Executor executor, GntpPassword password, boolean encrypted) {
		Preconditions.checkNotNull(applicationInfo, "Application info must not be null");
		Preconditions.checkNotNull(growlAddress, "Address must not be null");
		Preconditions.checkNotNull(executor, "Executor must not be null");

		this.applicationInfo = applicationInfo;
		this.growlAddress = growlAddress;
		this.password = password;
		this.encrypted = encrypted;

		bootstrap = new ConnectionlessBootstrap(new NioDatagramChannelFactory(executor));
		//bootstrap.setPipelineFactory(new GntpChannelPipelineFactory(new GntpChannelHandler(this, null)));
		bootstrap.setOption("broadcast", "true");

		datagramChannel = (DatagramChannel)bootstrap.bind(new InetSocketAddress(0));
		
		registrationLatch = new CountDownLatch(1);
	}

	@Override
	public void register() {
		if (closed) {
			throw new IllegalStateException("GntpClient has been shutdown");
		}
		logger.debug("Registering GNTP application [{}]", applicationInfo);
		GntpMessage message = new GntpRegisterMessage(applicationInfo, password, encrypted);
		datagramChannel.write(message, growlAddress);
		registrationLatch.countDown();
	}

	@Override
	public boolean isRegistered() {
		return registrationLatch.getCount() == 0 && !closed;
	}

	@Override
	public void waitRegistration() throws InterruptedException {
		registrationLatch.await();
	}

	@Override
	public boolean waitRegistration(long time, TimeUnit unit) throws InterruptedException {
		return registrationLatch.await(time, unit);
	}

	@Override
	public void notify(GntpNotification notification) {
	}

	@Override
	public void notify(GntpNotification notification, long time, TimeUnit unit) throws InterruptedException {
	}

	@Override
	public void shutdown(long time, TimeUnit unit) throws InterruptedException {
	}
}
