package uk.org.ssvc.dbsync.application.application;

import dagger.Component;
import uk.org.ssvc.dbsync.application.module.CommonModule;
import uk.org.ssvc.dbsync.domain.service.DatabaseMigrationService;

import javax.inject.Singleton;

@Component(modules = { CommonModule.class })
@Singleton
public interface Application {

    DatabaseMigrationService databaseMigrationService();

}
