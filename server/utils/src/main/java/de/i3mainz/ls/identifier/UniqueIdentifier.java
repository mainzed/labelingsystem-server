package de.i3mainz.ls.identifier;

import de.i3mainz.ls.rdfutils.exceptions.UniqueIdentifierException;
import java.util.UUID;

/**
 * CLASS to create an Universally Unique Identifier (UUID)
 *
 * @author Florian Thiery M.Sc.
 * @author i3mainz - Institute for Spatial Information and Surveying Technology
 * @version 05.02.2015
 */
public class UniqueIdentifier {

	/**
	 * get UUID
	 *
	 * @return UUID as String
	 * @throws de.i3mainz.ls.rdfutils.exceptions.UniqueIdentifierException
	 */
	public static String getUUID() throws UniqueIdentifierException {
		try {
			UUID newUUID = UUID.randomUUID();
			return newUUID.toString();
		} catch (Exception e) {
			throw new UniqueIdentifierException();
		}
	}

	/**
	 * get UUID without hyphen
	 *
	 * @return UUID without hyphen as String
	 * @throws de.i3mainz.ls.exceptions.UniqueIdentifierException
	 */
	public static String getUUIDwithoutHyphen() throws UniqueIdentifierException {
		try {
			UUID newUUID = UUID.randomUUID();
			return newUUID.toString().replaceAll("-", "");
		} catch (Exception e) {
			throw new UniqueIdentifierException();
		}
	}

	/**
	 * get UUID with "P-"
	 *
	 * @return UUID with "P-" as String
	 * @throws de.i3mainz.ls.exceptions.UniqueIdentifierException
	 */
	public static String getUUIDforProject() throws UniqueIdentifierException {
		try {
			UUID newUUID = UUID.randomUUID();
			return "P-" + newUUID.toString();
		} catch (Exception e) {
			throw new UniqueIdentifierException();
		}
	}

	/**
	 * get UUID with "V-"
	 *
	 * @return UUID with "V-" as String
	 * @throws de.i3mainz.ls.exceptions.UniqueIdentifierException
	 */
	public static String getUUIDforVocabulary() throws UniqueIdentifierException {
		try {
			UUID newUUID = UUID.randomUUID();
			return "V-" + newUUID.toString();
		} catch (Exception e) {
			throw new UniqueIdentifierException();
		}
	}

	/**
	 * get UUID with "L-"
	 *
	 * @return UUID with "L-" as String
	 * @throws de.i3mainz.ls.exceptions.UniqueIdentifierException
	 */
	public static String getUUIDforLabel() throws UniqueIdentifierException {
		try {
			UUID newUUID = UUID.randomUUID();
			return "L-" + newUUID.toString();
		} catch (Exception e) {
			throw new UniqueIdentifierException();
		}
	}

}
