package uk.org.ssvc.dbsync.application;

import lombok.extern.slf4j.Slf4j;
import uk.org.ssvc.dbsync.application.application.DaggerApplication;

@Slf4j
public class SsvcDbSyncApplication {

    public static void main(String[] args) {
        DaggerApplication
            .builder()
            .build()
            .databaseMigrationService()
            .migrateToCurrentDatabase();
    }

}
