ALTER TABLE 
	ASIENTO
ALTER COLUMN "NUMERO" SMALLINT SELECTIVITY 85 CHECK ("NUMERO" <> 0);