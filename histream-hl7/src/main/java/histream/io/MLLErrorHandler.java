package histream.io;

import java.nio.ByteBuffer;

public interface MLLErrorHandler {
	void processingError(ByteBuffer buffer, Throwable error);
}
