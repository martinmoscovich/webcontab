package com.mmoscovich.webcontab.job;

import javax.inject.Inject;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.mmoscovich.webcontab.services.DBService;
import com.mmoscovich.webcontab.services.DBService.BackupType;

import lombok.extern.slf4j.Slf4j;

/**
 * Job recurrente que se encarga de hacer backup de la base de datos.
 */
@Slf4j
@Service
public class DbBackupJob {

	@Inject
	private DBService db;
	
	@Scheduled(cron =  "${webcontab.db.backup.cron}")
	public void run() {
		long start = System.currentTimeMillis();
		log.debug("Realizando backup de la base de datos");
		
		try {
			db.backUpWithDate(BackupType.DB);
			log.debug("Terminado backup en {} ms", System.currentTimeMillis() - start);

		} catch(Exception e) {
			log.error("ERROR AL GENERAR BACKUP!!", e);
		}
	}
	
}
