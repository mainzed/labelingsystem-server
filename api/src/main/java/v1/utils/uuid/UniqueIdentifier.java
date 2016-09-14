package v1.utils.uuid;

import exceptions.UniqueIdentifierException;
import java.util.UUID;

public class UniqueIdentifier {

	public static String getUUID() throws UniqueIdentifierException {
		try {
			UUID newUUID = UUID.randomUUID();
			return newUUID.toString();
		} catch (Exception e) {
			throw new UniqueIdentifierException();
		}
	}

}
