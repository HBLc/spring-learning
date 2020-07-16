DROP TABLE STUDENT;
CREATE TABLE STUDENT (
     SNO VARCHAR2(3 BYTE) NOT NULL ,
     NAME VARCHAR2(9 BYTE) NOT NULL ,
     SEX CHAR(2 BYTE) NOT NULL ,
     DATASOURCE VARCHAR2(10 BYTE) NULL
)
LOGGING NOCOMPRESS NOCACHE;

-- ----------------------------
-- Records of STUDENT
-- ----------------------------
INSERT INTO STUDENT VALUES ('001', 'KangKang', 'M ', 'oracle');
INSERT INTO STUDENT VALUES ('002', 'Mike', 'M ', 'oracle');
INSERT INTO STUDENT VALUES ('003', 'Jane', 'F ', 'oracle');
INSERT INTO STUDENT VALUES ('004', 'Maria', 'F ', 'oracle');

-- ----------------------------
-- Checks structure for table STUDENT
-- ----------------------------
ALTER TABLE STUDENT ADD CHECK (SNO IS NOT NULL);
ALTER TABLE STUDENT ADD CHECK (NAME IS NOT NULL);
ALTER TABLE STUDENT ADD CHECK (SEX IS NOT NULL);
