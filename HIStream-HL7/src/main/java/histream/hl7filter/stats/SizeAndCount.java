package histream.hl7filter.stats;

public interface SizeAndCount{
	long getCount();
	long getSize();
	void incrementCount();
	void addSize(int size);
}