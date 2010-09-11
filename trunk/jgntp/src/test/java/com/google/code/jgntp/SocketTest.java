package com.google.code.jgntp;

import java.io.*;
import java.net.*;

import org.junit.*;

import com.google.common.io.*;

public class SocketTest {

	@Test
	public void test() throws Exception {
		byte[] data = ByteStreams.toByteArray(new FileInputStream("C:/Users/leandro/Documents/My Dropbox/Public/dump-closed-connection.out"));
		Socket s = new Socket("127.0.0.1", 23053);

		InputStream is = s.getInputStream();
		OutputStream os = s.getOutputStream();

		os.write(data);
		os.flush();

		byte[] buf = new byte[1024];
		while (true) {
			int r = is.read(buf);
			if (r == -1) {
				break;
			}
			System.out.write(buf, 0, r);
		}

		is.close();
		os.close();
		s.close();
	}
}
