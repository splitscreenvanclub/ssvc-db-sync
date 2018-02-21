package uk.org.ssvc.dbsync.integration.csv.parser;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVRecord;
import uk.org.ssvc.core.domain.model.Address;
import uk.org.ssvc.core.domain.model.ContactDetails;
import uk.org.ssvc.core.domain.model.TelephoneNumber;
import uk.org.ssvc.core.domain.model.member.Member;
import uk.org.ssvc.core.domain.model.member.MemberAssociate;
import uk.org.ssvc.core.domain.model.member.RenewalDate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.ZoneOffset.UTC;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.*;

@Singleton
@Slf4j
public class MemberParser {

    private final static String DATE_PATTERN = "(([0-9]{1,2})[\\./\\-]([0-9]{1,2})[\\./\\-]([0-9]{2,4}))";
    private final static Pattern CHILDREN_PATTERN = Pattern.compile("([A-Z][a-z]+)( ([A-Za-z][A-Za-z]+?))??\\s*(DOB|dob)?\\s*" + DATE_PATTERN);

    @Inject
    public MemberParser() {
    }

    public Member parse(CSVRecord csvRecord) {
        return Member.builder()
            .id(normalise(csvRecord.get("fldMembershipNumber")))
            .firstName(names(csvRecord.get("fldFirstName")).get(0))
            .lastName(name(csvRecord.get("fldSurname")))
            .associates(parseAssociates(csvRecord))
            .address(parseAddress(csvRecord))
            .contactDetails(parseContactDetails(csvRecord))
            .renewalDate(parseExpiryDate(normalise(csvRecord.get("fldExpiry"))))
            .build();
    }

    private ContactDetails parseContactDetails(CSVRecord csvRecord) {
        String numberField = csvRecord.get("fldPhone");
        List<TelephoneNumber> numbers = isBlank(numberField) ? emptyList() : stream(numberField.split("\\s*[/&]\\s*"))
            .map(raw -> new TelephoneNumber(normalise(raw)))
            .collect(toList());

        return ContactDetails.builder()
            .telephoneNumbers(numbers)
            .emailAddress(normalise(csvRecord.get("fldEmail")))
            .build();
    }

    private Address parseAddress(CSVRecord csvRecord) {
        return Address.builder()
            .lineOne(normalise(csvRecord.get("fldAddress1")))
            .lineTwo(normalise(csvRecord.get("fldAddress2")))
            .lineThree(normalise(csvRecord.get("fldAddress3")))
            .lineFour(normalise(csvRecord.get("fldAddress4")))
            .county(normalise(csvRecord.get("fldCounty")))
            .region(normalise(csvRecord.get("fldRegion")))
            .postcode(normalise(csvRecord.get("fldPostcode")))
            .build();
    }

    private RenewalDate parseExpiryDate(String fldExpiry) {
        if (fldExpiry.equals("0")) {
            return RenewalDate.POTENTIALLY_PASSED_AWAY;
        }

        int year = Integer.parseInt(fldExpiry.substring(0, 4));
        int month = Integer.parseInt(fldExpiry.substring(4));

        if (year > 9000) {
            return RenewalDate.LIFETIME_MEMBERSHIP;
        }

        return new RenewalDate(LocalDate.now(UTC)
            .withYear(year)
            .withMonth(month == 0 ? 1 : month)
            .with(TemporalAdjusters.lastDayOfMonth()));
    }

    private Set<MemberAssociate> parseAssociates(CSVRecord csvRecord) {
        Set<MemberAssociate> associates = new HashSet<>();

        extractPossiblePartner(csvRecord).ifPresent(associates::add);
        associates.addAll(extractPossibleChildren(csvRecord));

        return associates;
    }

    private Optional<MemberAssociate> extractPossiblePartner(CSVRecord csvRecord) {
        List<String> namesInFirstNameField = names(csvRecord.get("fldFirstName"));
        String lastName = name(csvRecord.get("fldSurname"));
        String partnerFirstName = name(csvRecord.get("fldFirstName2"));
        String partnerLastName = name(csvRecord.get("fldSurname2"));

        boolean hasMultipleInFirstName = namesInFirstNameField.size() > 1;
        boolean hasPartner = isNotBlank(partnerFirstName);

        if (hasMultipleInFirstName && hasPartner && !namesInFirstNameField.contains(partnerFirstName)) {
            throw new RuntimeException("Too many partners record=" + csvRecord);
        }
        if (hasMultipleInFirstName) {
            return Optional.of(new MemberAssociate(namesInFirstNameField.get(1), lastName, null));
        }
        if (hasPartner) {
            return Optional.of(new MemberAssociate(partnerFirstName, defaultIfBlank(partnerLastName, lastName), null));
        }

        return Optional.empty();
    }

    private Set<MemberAssociate> extractPossibleChildren(CSVRecord csvRecord) {
        Set<MemberAssociate> ret = new HashSet<>();
        String notes = csvRecord.get("fldOtherHelp");
        Matcher m = CHILDREN_PATTERN.matcher(notes);

        while (m.find()) {
            String firstName = name(m.group(1));
            String lastName = name(defaultIfBlank(m.group(3), csvRecord.get("fldSurname")));
            String dobDay = m.group(6);
            String dobMonth = m.group(7);
            int dobYear = Integer.parseInt(m.group(8));

            if (dobYear > 60 && dobYear < 1000) {
                dobYear += 1900;
            }
            else if (dobYear < 30) {
                dobYear += 2000;
            }

            try {
                LocalDate dob = LocalDate.now()
                    .withYear(dobYear)
                    .withMonth(Integer.parseInt(dobMonth))
                    .withDayOfMonth(Integer.parseInt(dobDay));

                ret.add(new MemberAssociate(firstName, lastName, dob));
            }
            catch (Exception e) {
                log.info("Failed to parse potential seedling information, ignoring record={} error={}",
                    csvRecord, e.getMessage());
            }
        }

        return ret;
    }

    private String normalise(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private String name(String name) {
        return isBlank(name) ? null : capitalize(normalise(name).toLowerCase());
    }

    private List<String> names(String rawValue) {
        return stream(rawValue.split("(\\s*&\\s*|\\s+and\\s+)"))
            .map(this::name)
            .collect(toList());
    }

}
