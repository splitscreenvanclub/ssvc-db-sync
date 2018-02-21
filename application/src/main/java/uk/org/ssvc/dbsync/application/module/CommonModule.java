package uk.org.ssvc.dbsync.application.module;

import com.google.cloud.firestore.Firestore;
import dagger.Module;
import dagger.Provides;
import uk.org.ssvc.core.domain.repository.MemberRepository;
import uk.org.ssvc.core.integration.encryption.repository.EncryptingMemberRepository;
import uk.org.ssvc.core.integration.encryption.service.EncryptionService;
import uk.org.ssvc.dbsync.integration.csv.loader.CsvMemberDataLoader;
import uk.org.ssvc.dbsync.integration.csv.loader.CsvMemberHtmlLoader;
import uk.org.ssvc.dbsync.integration.csv.repository.CsvMemberRepository;
import uk.org.ssvc.firestore.integration.repository.v1.FirestoreMemberRepository;
import uk.org.ssvc.firestore.integration.service.FirestoreFactory;

import javax.inject.Named;
import javax.inject.Singleton;

@Module(includes = { CommonPropertiesModule.class })
public class CommonModule {

    @Provides
    @Singleton
    Firestore firestore(FirestoreFactory firestoreFactory) {
        return firestoreFactory.create();
    }

    @Provides
    @Singleton
    CsvMemberDataLoader csvMemberDataLoader(CsvMemberHtmlLoader impl) {
        return impl;
    }

    @Provides
    @Singleton
    @Named("currentRepository")
    MemberRepository memberRepository(EncryptionService encryptionService,
                                      FirestoreMemberRepository firestoreMemberRepository) {
        return new EncryptingMemberRepository(
            firestoreMemberRepository,
            encryptionService
        );
    }

    @Provides
    @Singleton
    @Named("legacyRepository")
    MemberRepository csvMemberRepository(CsvMemberRepository legacyRepo) {
        return legacyRepo;
    }

}
