package de.sekmi.histream.i2b2;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Assert;
import org.junit.Test;

public class TestHSQLDataSource {

	@Test
	public void testCreateTables() throws IOException, SQLException {
		LocalHSQLDataSource ds = new LocalHSQLDataSource();
		ds.delete();
		ds.createI2b2();
		// perform queries
		Assert.assertEquals(0, ds.executeCountQuery("SELECT COUNT(*) FROM observation_fact").intValue());
		// drop database
		ds.delete();
	}

	private int executeIntQuery(Connection c, String sql) throws SQLException {
		Statement st = c.createStatement();
		ResultSet rs = st.executeQuery(sql);
		Assert.assertTrue(rs.next());
		int res = rs.getInt(1);
		rs.close();
		st.close();
		return res;
	}
	@Test
	public void testLoadedData() throws IOException, SQLException {
		LocalHSQLDataSource ds = new LocalHSQLDataSource();
		ds.createI2b2();
		// perform queries
		ds.loadTestDataset1();
		// all data is 4 records
		Assert.assertEquals(4, ds.executeCountQuery("SELECT COUNT(*) FROM observation_fact").intValue());
		// try simple selection
		String sql = "SELECT COUNT(*) FROM observation_fact WHERE concept_cd='ICD10GM:Y36.9!'";
		Assert.assertEquals(2, ds.executeCountQuery(sql).intValue());
		// try more complex selection
		sql = "SELECT COUNT(*) FROM observation_fact WHERE start_date BETWEEN '2011-01-01 00:00:00' AND '2011-01-03 00:00:00' AND concept_cd='ICD10GM:Y36.9!'";
		Assert.assertEquals(2, ds.executeCountQuery(sql).intValue());

		try( Connection c = ds.getConnection() ){
			// TODO create temporary table with single concept
			String[] tmp = new String[] {"CREATE TABLE temp_concepts (concept VARCHAR(255) PRIMARY KEY)", "INSERT INTO temp_concepts(concept) VALUES('ICD10GM:Y36.9!')"};
			for( String s : tmp ) {
				Statement st = c.createStatement();
				st.executeUpdate(s);
				st.close();
			}
			Assert.assertEquals(1, executeIntQuery(c,"SELECT COUNT(*) FROM temp_concepts"));
			//sql = "SELECT f.patient_num, f.encounter_num, f.instance_num, f.concept_cd, f.modifier_cd, f.provider_id, f.location_cd, f.start_date, f.end_date, RTRIM(f.valtype_cd) valtype_cd, f.tval_char, f.nval_num, RTRIM(f.valueflag_cd) valueflag_cd, f.units_cd, f.download_date, f.sourcesystem_cd FROM observation_fact f INNER JOIN temp_concepts tc ON f.concept_cd=tc.concept WHERE f.start_date BETWEEN ? AND ?  ORDER BY f.patient_num, f.encounter_num, f.start_date, f.instance_num, f.concept_cd, f.modifier_cd NULLS FIRST";
			Assert.assertEquals(2, executeIntQuery(c,"SELECT COUNT(*) FROM observation_fact f INNER JOIN temp_concepts tc ON f.concept_cd=tc.concept WHERE f.start_date BETWEEN '2011-01-01 00:00:00' AND '2011-01-03 00:00:00'"));
			// drop temp table
			Statement st = c.createStatement();
			st.executeUpdate("DROP TABLE temp_concepts");
			st.close();
		}

		// drop database
		ds.delete();
	}
}
