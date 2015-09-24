package histream.hl7;


import java.nio.ByteBuffer;

public interface Encoder {
	int encode(Message message, ByteBuffer out);
}
