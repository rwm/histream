package histream.hl7;


import java.nio.ByteBuffer;

/**
 * A Decoder decodes a HL7v2 byte buffer into a {@link Message}.
 * Per default behavior, only a single thread may use the Decoder at a given time.
 * If multiple threads access the {@link #decode(ByteBuffer)} method, external synchronization
 * must be ensured.
 * 
 * TODO: write a DecoderFactory to instantiate decoders for parallelization.
 * 
 * @author marap1
 *
 */
public interface Decoder {
	Message decode(ByteBuffer in);
}
