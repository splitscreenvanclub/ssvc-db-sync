package uk.org.ssvc.dbsync.domain.service;

import lombok.extern.slf4j.Slf4j;
import uk.org.ssvc.core.domain.repository.MemberRepository;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Slf4j
public class DatabaseMigrationService {

    private final MemberRepository legacyRepository;
    private final MemberRepository currentRepository;

    @Inject
    public DatabaseMigrationService(@Named("legacyRepository") MemberRepository legacyRepository,
                                    @Named("currentRepository") MemberRepository currentRepository) {
        this.legacyRepository = legacyRepository;
        this.currentRepository = currentRepository;
    }

    public void migrateToCurrentDatabase() {
        log.info("Running migration...");
        currentRepository.addAll(legacyRepository.findAll());
        log.info("Completed migration");
    }

}
