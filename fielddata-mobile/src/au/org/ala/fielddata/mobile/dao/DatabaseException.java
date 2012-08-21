package au.org.ala.fielddata.mobile.dao;

/**
 * Thrown if there are problems accessing the database.  These are 
 * generally considered fatal.
 */
public class DatabaseException extends RuntimeException {
	private static final long serialVersionUID = -5628558295230563696L;

	public DatabaseException(Exception e) {
		super(e);
	}
	
	public DatabaseException(String message) {
		super(message);
	}
}
