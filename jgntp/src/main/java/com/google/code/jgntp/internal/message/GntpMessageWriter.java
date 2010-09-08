package com.google.code.jgntp.internal.message;

import java.io.*;

import com.google.code.jgntp.*;
import com.google.code.jgntp.internal.message.GntpMessage.*;

public interface GntpMessageWriter {

	void prepare(OutputStream output, GntpPassword password);
	void writeStatusLine(GntpMessageType type) throws IOException;
	
	void startHeaders() throws IOException;
	void writeHeaderLine(String line) throws IOException;
	void finishHeaders() throws IOException;
	
	void writeBinarySection(BinarySection binarySection) throws IOException;
	
	void writeSeparator() throws IOException;

}
