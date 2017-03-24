package v1.utils.uuid;

import java.util.UUID;
import org.hashids.Hashids;

public class UniqueIdentifier {

	public static String getHashID() {
		// https://github.com/jiecao-fm/hashids-java
		Hashids hashids = new Hashids(getUniversallyUniqueIdentifier2(), 12);
		String hash = hashids.encode(1234567L);
		return hash;
	}
	
	public static String getUniversallyUniqueIdentifier2() {
		UUID newUUID = UUID.randomUUID();
		return newUUID.toString();
	}

}
