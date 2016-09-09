package de.sekmi.histream.i2b2.sql;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;

public class VirtualPreparedStatement extends VirtualStatement implements PreparedStatement {

	public String[] sqlFragments;
	public String[] sqlValues;

	public VirtualPreparedStatement(String sql, VirtualConnection connection) {
		super(connection);
		sqlFragments = sql.split("\\?");
		// if the SQL ends with ?, split will not produce a trailing empty fragment
		// we need to add that manually
		if( sql.endsWith("?") ){
			sqlFragments = Arrays.copyOf(sqlFragments, sqlFragments.length+1);
			sqlFragments[sqlFragments.length-1] = "";
		}
		sqlValues = new String[sqlFragments.length - 1];
	}
	
	@Override
	public void addBatch() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clearParameters() throws SQLException {
		Arrays.fill(sqlValues, null);
	}

	@Override
	public boolean execute() throws SQLException {
		executeUpdate();
		return false;
	}

	@Override
	public ResultSet executeQuery() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int executeUpdate() throws SQLException {
		// build SQL
		StringBuilder sb = new StringBuilder();
		for( int i=0; i<sqlValues.length; i++ ){
			sb.append(sqlFragments[i]);
			if( sqlValues[i] == null ){
				sb.append("NULL");
			}else{
				sb.append(sqlValues[i]);
			}
		}
		// append last fragment
		sb.append(sqlFragments[sqlValues.length]);
		// execute
		execute(sb.toString());
		// always affect one virtual row
		return 1;
	}

	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ParameterMetaData getParameterMetaData() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setArray(int arg0, Array arg1) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setAsciiStream(int arg0, InputStream arg1) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setAsciiStream(int arg0, InputStream arg1, int arg2) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setAsciiStream(int arg0, InputStream arg1, long arg2) throws SQLException {
		throw new UnsupportedOperationException();
	}

	
	@Override
	public void setBigDecimal(int index, BigDecimal value) throws SQLException {
		setObject(index, value);
	}

	@Override
	public void setBinaryStream(int arg0, InputStream arg1) throws SQLException {
		throw new UnsupportedOperationException();		
	}

	@Override
	public void setBinaryStream(int arg0, InputStream arg1, int arg2) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBinaryStream(int arg0, InputStream arg1, long arg2) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBlob(int arg0, Blob arg1) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBlob(int arg0, InputStream arg1) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBlob(int arg0, InputStream arg1, long arg2) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBoolean(int index, boolean value) throws SQLException {
		setObject(index, value);
	}

	@Override
	public void setByte(int arg0, byte arg1) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBytes(int arg0, byte[] arg1) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setCharacterStream(int arg0, Reader arg1) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setCharacterStream(int arg0, Reader arg1, int arg2) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setClob(int arg0, Clob arg1) throws SQLException {
		try( Reader r = arg1.getCharacterStream() ){
			setClob(arg0, r);
		} catch (IOException e) {
			throw new SQLException(e);
		}
	}

	@Override
	public void setClob(int arg0, Reader arg1) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setClob(int arg0, Reader arg1, long arg2) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDate(int arg0, Date arg1) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDate(int arg0, Date arg1, Calendar arg2) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDouble(int arg0, double arg1) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setFloat(int arg0, float arg1) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setInt(int index, int value) throws SQLException {
		setObject(index,value);
	}

	@Override
	public void setLong(int index, long value) throws SQLException {
		setObject(index,value);
	}

	@Override
	public void setNCharacterStream(int arg0, Reader arg1) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setNCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setNClob(int arg0, NClob arg1) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setNClob(int arg0, Reader arg1) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setNClob(int arg0, Reader arg1, long arg2) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setNString(int arg0, String arg1) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setNull(int index, int arg1) throws SQLException {
		setObject(index, null);
	}

	@Override
	public void setNull(int arg0, int arg1, String arg2) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setObject(int index, Object value) throws SQLException {
		String str;
		if( value == null ){
			str = null;
		}else if( value.getClass() == Boolean.class ){
			str = ((Boolean)value)?"TRUE":"FALSE";
		}else if( value instanceof Number ){
			str = value.toString();
		}else{
			str = "'"+escapeString(value.toString())+"'";
		}
		sqlValues[index-1] = str;
	}

	@Override
	public void setObject(int arg0, Object arg1, int arg2) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setObject(int arg0, Object arg1, int arg2, int arg3) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setRef(int arg0, Ref arg1) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setRowId(int arg0, RowId arg1) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSQLXML(int arg0, SQLXML arg1) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setShort(int index, short value) throws SQLException {
		setObject(index,value);
	}

	@Override
	public void setString(int index, String value) throws SQLException {
		setObject(index,value);
	}

	@Override
	public void setTime(int arg0, Time arg1) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setTime(int arg0, Time arg1, Calendar arg2) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setTimestamp(int index, Timestamp value) throws SQLException {
		setObject(index, value);
	}

	@Override
	public void setTimestamp(int arg0, Timestamp arg1, Calendar arg2) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setURL(int arg0, URL arg1) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setUnicodeStream(int arg0, InputStream arg1, int arg2) throws SQLException {
		throw new UnsupportedOperationException();
	}
}
