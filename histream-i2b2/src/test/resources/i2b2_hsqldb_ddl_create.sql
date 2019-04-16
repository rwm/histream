-- demo data for testing query executions via SQL

CREATE TABLE observation_fact(
	encounter_num INTEGER NOT NULL, 
	patient_num INTEGER NOT NULL, 
	concept_cd VARCHAR(50) NOT NULL,
	provider_id VARCHAR(50) NULL,
	start_date DATETIME NOT NULL,
	modifier_cd VARCHAR(50) NULL,
	instance_num INTEGER NULL,
	valtype_cd VARCHAR(50) NULL,
	tval_char VARCHAR(255) NULL,
	nval_num DECIMAL(18,5) NULL,
	units_cd VARCHAR(50) NULL,
	end_date DATETIME NULL,
	location_cd VARCHAR(50) NULL,
	update_date DATETIME NULL,
	download_date DATETIME NULL,
	import_date DATETIME NULL,
	sourcesystem_cd VARCHAR(50) NULL
);
CREATE TABLE patient_dimension(
	patient_num INTEGER NOT NULL,
	vital_status_cd VARCHAR(3) NULL,
	birth_date DATETIME NULL,
	death_date DATETIME NULL,
	sex_cd VARCHAR(8) NULL,
	update_date DATETIME NULL,
	download_date DATETIME NULL,
	import_date DATETIME NULL,
	sourcesystem_cd VARCHAR(50) NULL
);
CREATE TABLE patient_mapping(
	patient_ide VARCHAR(200) NOT NULL,
	patient_ide_source VARCHAR(50) NOT NULL,
	patient_num INTEGER NOT NULL,
	patient_ide_status VARCHAR(50) NULL,
	project_id VARCHAR(50) NOT NULL,
	update_date DATETIME NULL,
	download_date DATETIME NULL,
	import_date DATETIME NULL,
	sourcesystem_cd VARCHAR(50) NULL
);

CREATE TABLE visit_dimension(
	encounter_num INTEGER NOT NULL,
	patient_num INTEGER NOT NULL,
	active_status_cd VARCHAR(3) NULL,
	start_date DATETIME NULL,
	end_date DATETIME NULL,
	inout_cd VARCHAR(8) NULL,
	location_cd VARCHAR(64) NULL,
	update_date DATETIME NULL,
	download_date DATETIME NULL,
	import_date DATETIME NULL,
	sourcesystem_cd VARCHAR(50) NULL
);
-- references to patient_ide, patient_ide_source NULL??
CREATE TABLE encounter_mapping(
	encounter_ide VARCHAR(200) NOT NULL,
	encounter_ide_source VARCHAR(50) NOT NULL,
	project_id VARCHAR(50) NOT NULL,
	encounter_num INTEGER NOT NULL,
	patient_ide VARCHAR(200) NULL, 
	patient_ide_source VARCHAR(50) NULL,
	encounter_ide_status VARCHAR(50) NULL,
	update_date DATETIME NULL,
	download_date DATETIME NULL,
	import_date DATETIME NULL,
	sourcesystem_cd VARCHAR(50) NULL
);
