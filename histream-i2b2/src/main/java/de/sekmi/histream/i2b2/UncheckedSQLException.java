package de.sekmi.histream.i2b2;

import java.sql.SQLException;

public class UncheckedSQLException extends RuntimeException{

	private static final long serialVersionUID = 1L;

	public UncheckedSQLException(SQLException cause){
		super(cause);
	}
	@Override
	public SQLException getCause(){
		return (SQLException)super.getCause();
	}
}
