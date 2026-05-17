package id.ac.ui.cs.advprog.yomu.social.bootstrap;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.ClanJoinRequest;
import id.ac.ui.cs.advprog.yomu.social.model.ClanMember;
import id.ac.ui.cs.advprog.yomu.social.model.SeasonState;
import id.ac.ui.cs.advprog.yomu.social.model.Tier;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanJoinRequestRepository;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanMemberRepository;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanRepository;
import id.ac.ui.cs.advprog.yomu.social.repository.SeasonStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class ClanDataSeeder implements CommandLineRunner {

    private static final String FULL_CAPACITY_CLAN_NAME = "Full Capacity Silver";
    private static final String JOIN_REQUEST_TEST_CLAN_NAME = "Join Request Test Clan";
    private static final String ROLE_MEMBER = "MEMBER";
    private static final String ROLE_LEADER = "LEADER";
    private static final String STATUS_PENDING = "PENDING";

    private final ClanRepository clanRepository;
    private final ClanMemberRepository clanMemberRepository;
    private final SeasonStateRepository seasonStateRepository;
    private final ClanJoinRequestRepository clanJoinRequestRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (clanRepository.count() == 0) {
            seedClans();
            seedMembers();
            seedJoinRequests();
            if (log.isInfoEnabled()) {
                log.info("Seeded dummy clan data for end-season simulation.");
            }
        }

        if (seasonStateRepository.count() == 0) {
            SeasonState seasonState = new SeasonState();
            seasonState.setSeasonNumber(1);
            seasonState.setActive(true);
            seasonStateRepository.save(seasonState);
            if (log.isInfoEnabled()) {
                log.info("Seeded initial season state for social module.");
            }
        }
    }

    private void seedClans() {
        // Gunakan ArrayList agar list bisa dimodifikasi (ditambah 50 clan baru)
        List<Clan> clans = new ArrayList<>(List.of(
            createClan("Aether Bronze", "Bronze clan for promotion testing", "user-leader-a", Tier.BRONZE, 120),
            createClan("Boreal Bronze", "Lower bronze clan to be relegated", "user-leader-b", Tier.BRONZE, 45),
            createClan("Crimson Bronze", "Mid bronze clan with stable score", "user-leader-c", Tier.BRONZE, 80),
            createClan("Ivory Bronze", "Bronze clan with balanced growth", "user-leader-i", Tier.BRONZE, 72),
            createClan("Jade Bronze", "Bronze clan focused on consistency", "user-leader-j", Tier.BRONZE, 66),
            createClan("Dawn Silver", "High silver clan for promotion testing", "user-leader-d", Tier.SILVER, 260),
            createClan("Echo Silver", "Lower silver clan to be demoted", "user-leader-e", Tier.SILVER, 140),
            createClan("Kite Silver", "Silver clan with strong member activity", "user-leader-k", Tier.SILVER, 220),
            createClan("Lumen Silver", "Silver clan in stable mid-table", "user-leader-l", Tier.SILVER, 190),
            createClan("Frost Gold", "Gold clan near top tier", "user-leader-f", Tier.GOLD, 410),
            createClan("Gale Gold", "Lower gold clan to be demoted", "user-leader-g", Tier.GOLD, 300),
            createClan("Mistral Gold", "Gold clan with aggressive scoring", "user-leader-m", Tier.GOLD, 355),
            createClan("Nova Gold", "Gold clan with steady performance", "user-leader-n", Tier.GOLD, 332),
            createClan("Helix Diamond", "Diamond clan for stability checks", "user-leader-h", Tier.DIAMOND, 650),
            createClan("Orion Diamond", "Diamond clan in close title race", "user-leader-o", Tier.DIAMOND, 615),
            createClan("Pulse Diamond", "Diamond clan with high consistency", "user-leader-p", Tier.DIAMOND, 590),
            // Special clans untuk QA testing
            createClan(FULL_CAPACITY_CLAN_NAME, "Silver clan already at 50/50 capacity for TC-SOC-02b testing", "user-leader-full-silver", Tier.SILVER, 250),
            createClan(JOIN_REQUEST_TEST_CLAN_NAME, "Clan with pending join requests for TC-SOC-03a and TC-SOC-03b testing", "leadreqclan", Tier.BRONZE, 100)
        ));

        // Tambah 50 clan secara dinamis
        Tier[] availableTiers = {Tier.BRONZE, Tier.SILVER, Tier.GOLD, Tier.DIAMOND};
        Random random = new Random();

        for (int i = 1; i <= 50; i++) {
            Tier randomTier = availableTiers[random.nextInt(availableTiers.length)];
            
            // Menentukan skor acak berdasarkan Tier agar realistis
            int baseScore = switch (randomTier) {
                case BRONZE -> 20 + random.nextInt(100);
                case SILVER -> 130 + random.nextInt(120);
                case GOLD -> 280 + random.nextInt(150);
                case DIAMOND -> 500 + random.nextInt(300);
                default -> 0;
            };

            clans.add(createClan(
                "Generated " + randomTier.name() + " " + i,
                "Auto-generated clan for load testing",
                "user-leader-gen-" + i,
                randomTier,
                baseScore
            ));
        }

        clans.forEach(clanRepository::save);
        if (log.isInfoEnabled()) {
            log.info("Seeded {} dummy clans (including 2 special QA test clans).", clans.size());
        }
    }

    private void seedMembers() {
        clanRepository.findAll().forEach(clan -> {
            List<ClanMember> members = createMembersForClan(clan);
            members.forEach(clanMemberRepository::save);
        });
    }

    private Clan createClan(String name, String description, String leaderUserId, Tier tier, int score) {
        Clan clan = new Clan();
        clan.setName(name);
        clan.setDescription(description);
        clan.setLeaderUserId(leaderUserId);
        clan.setTier(tier);
        clan.setScore(score);
        return clan;
    }

    private List<ClanMember> createMembersForClan(Clan clan) {
        // Special handling untuk clan yang akan ditest
        if (FULL_CAPACITY_CLAN_NAME.equals(clan.getName())) {
            // Buat 50 members untuk menguji clan kapasitas penuh (TC-SOC-02b)
            return createFullCapacityMembers(clan);
        } else if (JOIN_REQUEST_TEST_CLAN_NAME.equals(clan.getName())) {
            // Buat 10 members untuk clan dengan pending join requests (TC-SOC-03a, TC-SOC-03b)
            return createMembersWithPendingRequests(clan);
        } else {
            // Default: 4 members (1 leader + 3 members)
            return List.of(
                createMember(clan, clan.getLeaderUserId(), clan.getName() + " Leader", ROLE_LEADER),
                createMember(clan, clan.getId() + "-m1", clan.getName() + " Member 1", ROLE_MEMBER),
                createMember(clan, clan.getId() + "-m2", clan.getName() + " Member 2", ROLE_MEMBER),
                createMember(clan, clan.getId() + "-m3", clan.getName() + " Member 3", ROLE_MEMBER)
            );
        }
    }

    private List<ClanMember> createFullCapacityMembers(Clan clan) {
        List<ClanMember> members = new ArrayList<>();
        // Tambah leader
        members.add(createMember(clan, clan.getLeaderUserId(), clan.getName() + " Leader", ROLE_LEADER));
        // Tambah 49 members untuk mencapai kapasitas maksimal 50/50
        for (int i = 1; i <= 49; i++) {
            members.add(createMember(clan, clan.getId() + "-full-" + i, clan.getName() + " Member " + i, ROLE_MEMBER));
        }
        return members;
    }

    private List<ClanMember> createMembersWithPendingRequests(Clan clan) {
        List<ClanMember> members = new ArrayList<>();
        // Tambah leader
        members.add(createMember(clan, clan.getLeaderUserId(), clan.getName() + " Leader", ROLE_LEADER));
        // Tambah 10 members
        for (int i = 1; i <= 10; i++) {
            members.add(createMember(clan, clan.getId() + "-req-" + i, clan.getName() + " Member " + i, ROLE_MEMBER));
        }
        return members;
    }

    private void seedJoinRequests() {
        // Cari "Join Request Test Clan" untuk membuat pending requests
        var clanOptional = clanRepository.findAll().stream()
            .filter(c -> JOIN_REQUEST_TEST_CLAN_NAME.equals(c.getName()))
            .findFirst();

        if (clanOptional.isPresent()) {
            Clan clan = clanOptional.get();
            // Buat 5 pending join requests untuk testing TC-SOC-03a dan TC-SOC-03b
            for (int i = 1; i <= 5; i++) {
                ClanJoinRequest request = new ClanJoinRequest();
                request.setClanId(clan.getId());
                request.setUserId("test-requester-" + i);
                request.setUsername("Test Requester " + i);
                request.setStatus(STATUS_PENDING);
                clanJoinRequestRepository.save(request);
            }
            if (log.isInfoEnabled()) {
                log.info("Seeded 5 pending join requests for '{}' clan.", clan.getName());
            }
        }
    }

    private ClanMember createMember(Clan clan, String userId, String username, String role) {
        ClanMember member = new ClanMember();
        member.setClanId(clan.getId());
        member.setUserId(userId);
        member.setUsername(username);
        member.setRole(role);
        return member;
    }
}