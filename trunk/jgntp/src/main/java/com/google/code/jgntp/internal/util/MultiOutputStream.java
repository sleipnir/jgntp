package com.google.code.jgntp.internal.util;

import java.io.*;
import java.util.*;

/**
 * Sends written bytes to all specified output streams.
 * Exceptions are propagated as soon as they are generated,
 * it means if an exception is thrown there is no guarantee
 * of what OutputStreams have received the last bytes written.
 */
public class MultiOutputStream extends OutputStream {

	private final OutputStream[] outputStreams;

	public MultiOutputStream(OutputStream... outputStreams) {
		this.outputStreams = Arrays.copyOf(outputStreams, outputStreams.length);
	}

	@Override
	public void write(int b) throws IOException {
		for (OutputStream os : outputStreams) {
			os.write(b);
		}
	}

	@Override
	public void write(byte[] b) throws IOException {
		for (OutputStream os : outputStreams) {
			os.write(b);
		}
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		for (OutputStream os : outputStreams) {
			os.write(b, off, len);
		}
	}

	@Override
	public void flush() throws IOException {
		for (OutputStream os : outputStreams) {
			os.flush();
		}
	}

	@Override
	public void close() throws IOException {
		for (OutputStream os : outputStreams) {
			os.close();
		}
	}

	public OutputStream[] getOutputStreams() {
		return Arrays.copyOf(outputStreams, outputStreams.length);
	}
}
