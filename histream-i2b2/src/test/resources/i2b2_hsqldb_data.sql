-- demo data for testing query executions via SQL

INSERT INTO patient_dimension(patient_num,vital_status_cd,birth_date,sex_cd)VALUES(1001, NULL, '2001-01-01','F');
INSERT INTO patient_dimension(patient_num,vital_status_cd,birth_date,sex_cd)VALUES(1002, NULL, '2002-02-02','M');
INSERT INTO patient_dimension(patient_num,vital_status_cd,birth_date,sex_cd)VALUES(1003, NULL, '2003-03-03','F');

INSERT INTO visit_dimension(encounter_num,patient_num,active_status_cd,start_date)VALUES(2001, 1001, NULL, '2011-01-01 01:00:00');
INSERT INTO visit_dimension(encounter_num,patient_num,active_status_cd,start_date)VALUES(2002, 1001, NULL, '2011-01-02 02:00:00');
INSERT INTO visit_dimension(encounter_num,patient_num,active_status_cd,start_date)VALUES(2003, 1001, NULL, '2011-01-03 03:00:00');
INSERT INTO visit_dimension(encounter_num,patient_num,active_status_cd,start_date)VALUES(2004, 1002, NULL, '2011-01-01 01:00:00');
INSERT INTO visit_dimension(encounter_num,patient_num,active_status_cd,start_date)VALUES(2005, 1002, NULL, '2011-01-02 02:00:00');

INSERT INTO observation_fact(encounter_num,patient_num,start_date,concept_cd)VALUES(2001,1001,'2011-01-01 01:01:00','ICD10GM:F30.0');
INSERT INTO observation_fact(encounter_num,patient_num,start_date,concept_cd)VALUES(2001,1001,'2011-01-01 01:02:00','ICD10GM:F30.8');
INSERT INTO observation_fact(encounter_num,patient_num,start_date,concept_cd)VALUES(2002,1001,'2011-01-02 01:01:00','ICD10GM:Y36.9!');
INSERT INTO observation_fact(encounter_num,patient_num,start_date,concept_cd)VALUES(2004,1002,'2011-01-01 01:01:00','ICD10GM:Y36.9!');
COMMIT;
