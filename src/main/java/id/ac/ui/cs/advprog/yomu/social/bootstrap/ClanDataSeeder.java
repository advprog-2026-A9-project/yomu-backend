package id.ac.ui.cs.advprog.yomu.social.bootstrap;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.reading.model.Category;
import id.ac.ui.cs.advprog.yomu.reading.model.QuizOption;
import id.ac.ui.cs.advprog.yomu.reading.model.QuizQuestion;
import id.ac.ui.cs.advprog.yomu.reading.model.ReadingText;
import id.ac.ui.cs.advprog.yomu.reading.repository.CategoryRepository;
import id.ac.ui.cs.advprog.yomu.reading.repository.QuizOptionRepository;
import id.ac.ui.cs.advprog.yomu.reading.repository.QuizQuestionRepository;
import id.ac.ui.cs.advprog.yomu.reading.repository.ReadingTextRepository;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.ClanJoinRequest;
import id.ac.ui.cs.advprog.yomu.social.model.ClanJoinRequestStatus;
import id.ac.ui.cs.advprog.yomu.social.model.ClanMember;
import id.ac.ui.cs.advprog.yomu.social.model.ClanRole;
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
    private static final ClanRole ROLE_MEMBER = ClanRole.MEMBER;
    private static final ClanRole ROLE_LEADER = ClanRole.LEADER;
    private static final ClanJoinRequestStatus STATUS_PENDING = ClanJoinRequestStatus.PENDING;

    private final ClanRepository clanRepository;
    private final ClanMemberRepository clanMemberRepository;
    private final SeasonStateRepository seasonStateRepository;
    private final ClanJoinRequestRepository clanJoinRequestRepository;
    private final CategoryRepository categoryRepository;
    private final ReadingTextRepository readingTextRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizOptionRepository quizOptionRepository;

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

        if (categoryRepository.count() == 0) {
            seedCategories();
            seedReadingTexts();
            seedQuizQuestions();
            if (log.isInfoEnabled()) {
                log.info("Seeded reading and quiz data.");
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
                createClan(FULL_CAPACITY_CLAN_NAME, "Silver clan already at 50/50 capacity for TC-SOC-02b testing",
                        "user-leader-full-silver", Tier.SILVER, 250),
                createClan(JOIN_REQUEST_TEST_CLAN_NAME,
                        "Clan with pending join requests for TC-SOC-03a and TC-SOC-03b testing", "leadreqclan",
                        Tier.BRONZE, 100)));

        // Tambah 50 clan secara dinamis
        Tier[] availableTiers = { Tier.BRONZE, Tier.SILVER, Tier.GOLD, Tier.DIAMOND };
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
                    baseScore));
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

    private Clan createClan(String name, String description, String leaderUsername, Tier tier, int score) {
        Clan clan = new Clan();
        clan.setName(name);
        clan.setDescription(description);
        clan.setLeaderUsername(leaderUsername);
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
            // Buat 10 members untuk clan dengan pending join requests (TC-SOC-03a,
            // TC-SOC-03b)
            return createMembersWithPendingRequests(clan);
        } else {
            // Default: 4 members (1 leader + 3 members)
            return List.of(
                    createMember(clan, clan.getLeaderUsername(), ROLE_LEADER),
                    createMember(clan, clan.getId() + "-m1", ROLE_MEMBER),
                    createMember(clan, clan.getId() + "-m2", ROLE_MEMBER),
                    createMember(clan, clan.getId() + "-m3", ROLE_MEMBER));
        }
    }

    private List<ClanMember> createFullCapacityMembers(Clan clan) {
        List<ClanMember> members = new ArrayList<>();
        // Tambah leader
        members.add(createMember(clan, clan.getLeaderUsername(), ROLE_LEADER));
        // Tambah 49 members untuk mencapai kapasitas maksimal 50/50
        for (int i = 1; i <= 49; i++) {
            members.add(createMember(clan, clan.getId() + "-full-" + i, ROLE_MEMBER));
        }
        return members;
    }

    private List<ClanMember> createMembersWithPendingRequests(Clan clan) {
        List<ClanMember> members = new ArrayList<>();
        // Tambah leader
        members.add(createMember(clan, clan.getLeaderUsername(), ROLE_LEADER));
        // Tambah 10 members
        for (int i = 1; i <= 10; i++) {
            members.add(createMember(clan, clan.getId() + "-req-" + i, ROLE_MEMBER));
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
                request.setUsername("test-requester-" + i);
                request.setStatus(STATUS_PENDING);
                clanJoinRequestRepository.save(request);
            }
            if (log.isInfoEnabled()) {
                log.info("Seeded 5 pending join requests for '{}' clan.", clan.getName());
            }
        }
    }

    private ClanMember createMember(Clan clan, String username, ClanRole role) {
        ClanMember member = new ClanMember();
        member.setClanId(clan.getId());
        member.setUsername(username);
        member.setRole(role);
        return member;
    }

    private void seedCategories() {
        List<Category> categories = List.of(
                createCategory("Teknologi"),
                createCategory("Sains"),
                createCategory("Sejarah"),
                createCategory("Bahasa"),
                createCategory("Sastra"));
        categories.forEach(categoryRepository::save);
        if (log.isInfoEnabled()) {
            log.info("Seeded {} categories.", categories.size());
        }
    }

    private void seedReadingTexts() {
        List<Category> categories = categoryRepository.findAll();
        if (categories.isEmpty()) {
            if (log.isWarnEnabled()) {
                log.warn("No categories found. Skipping reading texts seeding.");
            }
            return;
        }

        List<ReadingText> readingTexts = new ArrayList<>();

        // Teknologi category
        Category tech = categories.stream()
                .filter(c -> "Teknologi".equals(c.getName()))
                .findFirst()
                .orElse(categories.get(0));
        readingTexts.add(createReadingText(
                "Pengenalan Artificial Intelligence",
                "Artificial Intelligence (AI) adalah bidang ilmu komputer yang berfokus pada pengembangan mesin cerdas. "
                        +
                        "AI memungkinkan komputer untuk belajar dari data, mengidentifikasi pola, dan membuat keputusan dengan minimal intervensi manusia. "
                        +
                        "Aplikasi AI saat ini mencakup image recognition, natural language processing, dan autonomous vehicles. "
                        +
                        "Teknologi ini terus berkembang dan menjadi bagian integral dari kehidupan modern.",
                tech));

        readingTexts.add(createReadingText(
                "Cloud Computing Explained",
                "Cloud computing adalah model komputasi yang menyediakan akses on-demand ke sumber daya komputasi melalui internet. "
                        +
                        "Layanan cloud dapat berupa Infrastructure as a Service (IaaS), Platform as a Service (PaaS), atau Software as a Service (SaaS). "
                        +
                        "Keuntungan utama cloud computing adalah skalabilitas, fleksibilitas, dan efisiensi biaya. " +
                        "Penyedia cloud terkemuka termasuk Amazon AWS, Microsoft Azure, dan Google Cloud Platform.",
                tech));

        // Sains category
        Category science = categories.stream()
                .filter(c -> "Sains".equals(c.getName()))
                .findFirst()
                .orElse(categories.get(1));
        readingTexts.add(createReadingText(
                "Fotosintesis: Proses Kehidupan Tumbuhan",
                "Fotosintesis adalah proses biokimia yang terjadi pada tumbuhan hijau untuk mengubah cahaya matahari menjadi energi kimia. "
                        +
                        "Proses ini berlangsung dalam dua tahap: reaksi terang dan reaksi gelap (siklus Calvin). " +
                        "Reaksi terang terjadi di tilakoid kloroplas dan menghasilkan ATP dan NADPH. " +
                        "Reaksi gelap menggunakan produk reaksi terang untuk mensintesis glukosa dari CO2.",
                science));

        readingTexts.add(createReadingText(
                "Termodinamika: Hukum Konservasi Energi",
                "Termodinamika adalah cabang fisika yang mempelajari hubungan antara panas, kerja, dan energi. " +
                        "Hukum pertama termodinamika menyatakan bahwa energi tidak dapat diciptakan atau dimusnahkan, hanya dapat diubah bentuknya. "
                        +
                        "Hukum kedua termodinamika mengenalkan konsep entropi, yang menyatakan bahwa entrop alam semesta selalu meningkat. "
                        +
                        "Hukum ketiga termodinamika mendefinisikan nol mutlak sebagai titik referensi entropi.",
                science));

        // Sejarah category
        Category history = categories.stream()
                .filter(c -> "Sejarah".equals(c.getName()))
                .findFirst()
                .orElse(categories.get(2));
        readingTexts.add(createReadingText(
                "Revolusi Industri dan Dampaknya",
                "Revolusi Industri dimulai di Inggris pada akhir abad ke-18 dan mengubah cara produksi barang secara fundamental. "
                        +
                        "Penemuan mesin uap oleh James Watt menjadi katalis utama transformasi ini. " +
                        "Dampak sosial termasuk urbanisasi masif, perubahan struktur keluarga, dan munculnya kelas pekerja. "
                        +
                        "Revolusi Industri juga membawa kemajuan teknologi yang luar biasa dalam tekstil, transportasi, dan manufaktur.",
                history));

        readingTexts.add(createReadingText(
                "Peradaban Kuno Mesir",
                "Mesir Kuno adalah salah satu peradaban tertua dan paling berpengaruh di dunia, berkembang di sepanjang Sungai Nil. "
                        +
                        "Dinasti Firaun berkuasa selama ribuan tahun dengan sistem pemerintahan yang sangat terorganisir. "
                        +
                        "Prestasi mereka mencakup konstruksi piramida, pengembangan sistem tulisan hieroglifik, dan kemajuan dalam matematika dan astronomi. "
                        +
                        "Budaya Mesir Kuno terus mempengaruhi seni, arsitektur, dan pemikiran filosofis hingga hari ini.",
                history));

        // Bahasa category
        Category language = categories.stream()
                .filter(c -> "Bahasa".equals(c.getName()))
                .findFirst()
                .orElse(categories.get(3));
        readingTexts.add(createReadingText(
                "Tata Bahasa Indonesia: Subjek dan Predikat",
                "Tata bahasa Indonesia mengikuti struktur dasar Subjek-Predikat-Objek (SPO). " +
                        "Subjek adalah bagian kalimat yang melakukan aksi atau keadaan yang dijelaskan oleh predikat. "
                        +
                        "Predikat adalah kata kerja yang menunjukkan aksi atau keadaan. " +
                        "Pemahaman tentang komponen-komponen ini sangat penting untuk membentuk kalimat yang benar dan efektif.",
                language));

        readingTexts.add(createReadingText(
                "Perkembangan Bahasa Inggris Modern",
                "Bahasa Inggris telah berkembang dari bahasa Anglo-Saxon menjadi bahasa global yang paling banyak digunakan. "
                        +
                        "Perkembangan ini dipercepat oleh ekspansi Britania Raya pada era kolonial dan dominasi Amerika Serikat pada abad ke-20. "
                        +
                        "Bahasa Inggris modern dipengaruhi oleh banyak bahasa lain, dengan kosa kata yang berasal dari Norman, Latin, dan bahasa-bahasa Eropa lainnya. "
                        +
                        "Saat ini, Bahasa Inggris terus berkembang dengan penambahan istilah-istilah baru dari teknologi dan budaya populer.",
                language));

        // Sastra category
        Category literature = categories.stream()
                .filter(c -> "Sastra".equals(c.getName()))
                .findFirst()
                .orElse(categories.get(4));
        readingTexts.add(createReadingText(
                "Puisi: Seni Ekspresi Kata",
                "Puisi adalah bentuk sastra yang menggunakan bahasa dengan cara yang sangat artistik dan penuh makna. "
                        +
                        "Elemen-elemen penting puisi termasuk rima, meter, metafora, dan imagery. " +
                        "Puisi dapat diekspresikan dalam berbagai bentuk seperti soneta, haiku, dan puisi bebas. " +
                        "Puisi memungkinkan penyair untuk mengekspresikan emosi dan ide-ide kompleks dengan cara yang indah dan ringkas.",
                literature));

        readingTexts.add(createReadingText(
                "Novel Modern dan Perkembangannya",
                "Novel modern adalah bentuk prosa naratif panjang yang berkembang sekitar abad ke-18. " +
                        "Perkembangan novel dipengaruhi oleh perubahan sosial, teknologi cetak, dan meningkatnya literasi masyarakat. "
                        +
                        "Genre novel sangat beragam, mulai dari realism, romanticism, naturalisme, hingga modernisme. "
                        +
                        "Novel modern memungkinkan eksplorasi mendalam tentang karakter, motivasi, dan hubungan kompleks antar individu.",
                literature));

        readingTexts.forEach(readingTextRepository::save);
        if (log.isInfoEnabled()) {
            log.info("Seeded {} reading texts.", readingTexts.size());
        }
    }

    @SuppressWarnings("null")
    private void seedQuizQuestions() {
        List<ReadingText> readingTexts = readingTextRepository.findAll();
        if (readingTexts.isEmpty()) {
            if (log.isWarnEnabled()) {
                log.warn("No reading texts found. Skipping quiz questions seeding.");
            }
            return;
        }

        // 1. Pengenalan Artificial Intelligence
        readingTexts.stream()
                .filter(rt -> "Pengenalan Artificial Intelligence".equals(rt.getTitle()))
                .findFirst()
                .ifPresent(rt -> {
                    QuizQuestion q1 = createQuizQuestion(rt, "Apa yang dimaksud dengan Artificial Intelligence?");
                    q1 = quizQuestionRepository.save(q1);
                    List<QuizOption> q1Options = List.of(
                            createQuizOption(q1,
                                    "Mesin yang dapat belajar dan membuat keputusan dengan minimal intervensi manusia",
                                    true),
                            createQuizOption(q1, "Program komputer yang hanya bisa mengikuti perintah", false),
                            createQuizOption(q1, "Perangkat keras komputer terbaru", false),
                            createQuizOption(q1, "Sistem operasi komputer", false));
                    q1Options.forEach(quizOptionRepository::save);

                    QuizQuestion q2 = createQuizQuestion(rt, "Manakah yang bukan merupakan aplikasi AI?");
                    q2 = quizQuestionRepository.save(q2);
                    List<QuizOption> q2Options = List.of(
                            createQuizOption(q2, "Image recognition", false),
                            createQuizOption(q2, "Natural language processing", false),
                            createQuizOption(q2, "Autonomous vehicles", false),
                            createQuizOption(q2, "Palu mekanik tradisional", true));
                    q2Options.forEach(quizOptionRepository::save);
                });

        // 2. Cloud Computing Explained
        readingTexts.stream()
                .filter(rt -> "Cloud Computing Explained".equals(rt.getTitle()))
                .findFirst()
                .ifPresent(rt -> {
                    QuizQuestion q3 = createQuizQuestion(rt, "Apa saja tiga model layanan cloud computing?");
                    q3 = quizQuestionRepository.save(q3);
                    List<QuizOption> q3Options = List.of(
                            createQuizOption(q3, "IaaS, PaaS, SaaS", true),
                            createQuizOption(q3, "HTML, CSS, JavaScript", false),
                            createQuizOption(q3, "CPU, RAM, GPU", false),
                            createQuizOption(q3, "HTTP, FTP, SMTP", false));
                    q3Options.forEach(quizOptionRepository::save);

                    QuizQuestion q4 = createQuizQuestion(rt,
                            "Manakah yang merupakan penyedia cloud terkemuka yang disebutkan dalam teks?");
                    q4 = quizQuestionRepository.save(q4);
                    List<QuizOption> q4Options = List.of(
                            createQuizOption(q4, "Amazon AWS, Microsoft Azure, dan Google Cloud Platform", true),
                            createQuizOption(q4, "Facebook, Twitter, dan Instagram", false),
                            createQuizOption(q4, "Intel, AMD, dan Nvidia", false),
                            createQuizOption(q4, "Linux, Windows, dan macOS", false));
                    q4Options.forEach(quizOptionRepository::save);
                });

        // 3. Fotosintesis: Proses Kehidupan Tumbuhan
        readingTexts.stream()
                .filter(rt -> "Fotosintesis: Proses Kehidupan Tumbuhan".equals(rt.getTitle()))
                .findFirst()
                .ifPresent(rt -> {
                    QuizQuestion q5 = createQuizQuestion(rt,
                            "Di mana reaksi terang fotosintesis terjadi dan apa hasilnya?");
                    q5 = quizQuestionRepository.save(q5);
                    List<QuizOption> q5Options = List.of(
                            createQuizOption(q5, "Di tilakoid kloroplas, menghasilkan ATP dan NADPH", true),
                            createQuizOption(q5, "Di stroma kloroplas, menghasilkan glukosa", false),
                            createQuizOption(q5, "Di mitokondria, menghasilkan air", false),
                            createQuizOption(q5, "Di sitoplasma, menghasilkan CO2", false));
                    q5Options.forEach(quizOptionRepository::save);

                    QuizQuestion q6 = createQuizQuestion(rt,
                            "Reaksi gelap pada fotosintesis dikenal juga dengan nama...");
                    q6 = quizQuestionRepository.save(q6);
                    List<QuizOption> q6Options = List.of(
                            createQuizOption(q6, "Siklus Calvin", true),
                            createQuizOption(q6, "Siklus Krebs", false),
                            createQuizOption(q6, "Glikolisis", false),
                            createQuizOption(q6, "Rantai Transpor Elektron", false));
                    q6Options.forEach(quizOptionRepository::save);
                });

        // 4. Termodinamika: Hukum Konservasi Energi
        readingTexts.stream()
                .filter(rt -> "Termodinamika: Hukum Konservasi Energi".equals(rt.getTitle()))
                .findFirst()
                .ifPresent(rt -> {
                    QuizQuestion q7 = createQuizQuestion(rt, "Apa yang dinyatakan oleh Hukum Pertama Termodinamika?");
                    q7 = quizQuestionRepository.save(q7);
                    List<QuizOption> q7Options = List.of(
                            createQuizOption(q7,
                                    "Energi tidak dapat diciptakan atau dimusnahkan, hanya dapat diubah bentuknya",
                                    true),
                            createQuizOption(q7, "Entropi alam semesta selalu meningkat", false),
                            createQuizOption(q7, "Nol mutlak adalah titik referensi entropi", false),
                            createQuizOption(q7, "Panas selalu mengalir dari benda dingin ke benda panas", false));
                    q7Options.forEach(quizOptionRepository::save);

                    QuizQuestion q8 = createQuizQuestion(rt, "Hukum kedua termodinamika memperkenalkan konsep...");
                    q8 = quizQuestionRepository.save(q8);
                    List<QuizOption> q8Options = List.of(
                            createQuizOption(q8, "Entropi", true),
                            createQuizOption(q8, "Kekekalan massa", false),
                            createQuizOption(q8, "Nol mutlak", false),
                            createQuizOption(q8, "Kapasitas panas", false));
                    q8Options.forEach(quizOptionRepository::save);
                });

        // 5. Revolusi Industri dan Dampaknya
        readingTexts.stream()
                .filter(rt -> "Revolusi Industri dan Dampaknya".equals(rt.getTitle()))
                .findFirst()
                .ifPresent(rt -> {
                    QuizQuestion q9 = createQuizQuestion(rt,
                            "Siapakah penemu mesin uap yang menjadi katalis utama Revolusi Industri?");
                    q9 = quizQuestionRepository.save(q9);
                    List<QuizOption> q9Options = List.of(
                            createQuizOption(q9, "James Watt", true),
                            createQuizOption(q9, "Thomas Edison", false),
                            createQuizOption(q9, "Alexander Graham Bell", false),
                            createQuizOption(q9, "Nikola Tesla", false));
                    q9Options.forEach(quizOptionRepository::save);

                    QuizQuestion q10 = createQuizQuestion(rt,
                            "Manakah yang merupakan salah satu dampak sosial dari Revolusi Industri?");
                    q10 = quizQuestionRepository.save(q10);
                    List<QuizOption> q10Options = List.of(
                            createQuizOption(q10, "Urbanisasi masif", true),
                            createQuizOption(q10, "Penurunan populasi kota", false),
                            createQuizOption(q10, "Hilangnya kelas pekerja", false),
                            createQuizOption(q10, "Kemunduran transportasi", false));
                    q10Options.forEach(quizOptionRepository::save);
                });

        // 6. Peradaban Kuno Mesir
        readingTexts.stream()
                .filter(rt -> "Peradaban Kuno Mesir".equals(rt.getTitle()))
                .findFirst()
                .ifPresent(rt -> {
                    QuizQuestion q11 = createQuizQuestion(rt,
                            "Di sepanjang sungai manakah peradaban Mesir Kuno berkembang?");
                    q11 = quizQuestionRepository.save(q11);
                    List<QuizOption> q11Options = List.of(
                            createQuizOption(q11, "Sungai Nil", true),
                            createQuizOption(q11, "Sungai Amazon", false),
                            createQuizOption(q11, "Sungai Gangga", false),
                            createQuizOption(q11, "Sungai Tigris", false));
                    q11Options.forEach(quizOptionRepository::save);

                    QuizQuestion q12 = createQuizQuestion(rt,
                            "Sistem tulisan yang dikembangkan oleh peradaban Mesir Kuno disebut...");
                    q12 = quizQuestionRepository.save(q12);
                    List<QuizOption> q12Options = List.of(
                            createQuizOption(q12, "Hieroglifik", true),
                            createQuizOption(q12, "Cuneiform", false),
                            createQuizOption(q12, "Alfabet Latin", false),
                            createQuizOption(q12, "Karakter Han", false));
                    q12Options.forEach(quizOptionRepository::save);
                });

        // 7. Tata Bahasa Indonesia: Subjek dan Predikat
        readingTexts.stream()
                .filter(rt -> "Tata Bahasa Indonesia: Subjek dan Predikat".equals(rt.getTitle()))
                .findFirst()
                .ifPresent(rt -> {
                    QuizQuestion q13 = createQuizQuestion(rt,
                            "Apa struktur dasar tata bahasa Indonesia yang disebutkan dalam teks?");
                    q13 = quizQuestionRepository.save(q13);
                    List<QuizOption> q13Options = List.of(
                            createQuizOption(q13, "Subjek-Predikat-Objek (SPO)", true),
                            createQuizOption(q13, "Subjek-Objek-Predikat (SOP)", false),
                            createQuizOption(q13, "Predikat-Subjek-Objek (PSO)", false),
                            createQuizOption(q13, "Objek-Subjek-Predikat (OSP)", false));
                    q13Options.forEach(quizOptionRepository::save);

                    QuizQuestion q14 = createQuizQuestion(rt,
                            "Bagian kalimat yang melakukan aksi atau keadaan disebut...");
                    q14 = quizQuestionRepository.save(q14);
                    List<QuizOption> q14Options = List.of(
                            createQuizOption(q14, "Subjek", true),
                            createQuizOption(q14, "Predikat", false),
                            createQuizOption(q14, "Objek", false),
                            createQuizOption(q14, "Keterangan", false));
                    q14Options.forEach(quizOptionRepository::save);
                });

        // 8. Perkembangan Bahasa Inggris Modern
        readingTexts.stream()
                .filter(rt -> "Perkembangan Bahasa Inggris Modern".equals(rt.getTitle()))
                .findFirst()
                .ifPresent(rt -> {
                    QuizQuestion q15 = createQuizQuestion(rt,
                            "Bahasa asal usul perkembangan awal Bahasa Inggris adalah...");
                    q15 = quizQuestionRepository.save(q15);
                    List<QuizOption> q15Options = List.of(
                            createQuizOption(q15, "Anglo-Saxon", true),
                            createQuizOption(q15, "Sanskerta", false),
                            createQuizOption(q15, "Arab", false),
                            createQuizOption(q15, "Mandarin", false));
                    q15Options.forEach(quizOptionRepository::save);

                    QuizQuestion q16 = createQuizQuestion(rt,
                            "Faktor apa yang mempercepat perkembangan Bahasa Inggris menjadi bahasa global pada abad ke-20?");
                    q16 = quizQuestionRepository.save(q16);
                    List<QuizOption> q16Options = List.of(
                            createQuizOption(q16, "Dominasi Amerika Serikat", true),
                            createQuizOption(q16, "Ekspansi Kekaisaran Romawi", false),
                            createQuizOption(q16, "Penemuan mesin cetak", false),
                            createQuizOption(q16, "Penyebaran karya sastra klasik", false));
                    q16Options.forEach(quizOptionRepository::save);
                });

        // 9. Puisi: Seni Ekspresi Kata
        readingTexts.stream()
                .filter(rt -> "Puisi: Seni Ekspresi Kata".equals(rt.getTitle()))
                .findFirst()
                .ifPresent(rt -> {
                    QuizQuestion q17 = createQuizQuestion(rt,
                            "Manakah yang merupakan elemen penting puisi menurut teks?");
                    q17 = quizQuestionRepository.save(q17);
                    List<QuizOption> q17Options = List.of(
                            createQuizOption(q17, "Rima, meter, metafora, dan imagery", true),
                            createQuizOption(q17, "Paragraf, dialog, dan bab", false),
                            createQuizOption(q17, "Daftar pustaka dan indeks", false),
                            createQuizOption(q17, "Catatan kaki dan glosarium", false));
                    q17Options.forEach(quizOptionRepository::save);

                    QuizQuestion q18 = createQuizQuestion(rt,
                            "Bentuk puisi tradisional asal Jepang yang memiliki pola baris tertentu adalah...");
                    q18 = quizQuestionRepository.save(q18);
                    List<QuizOption> q18Options = List.of(
                            createQuizOption(q18, "Haiku", true),
                            createQuizOption(q18, "Soneta", false),
                            createQuizOption(q18, "Puisi bebas", false),
                            createQuizOption(q18, "Pantun", false));
                    q18Options.forEach(quizOptionRepository::save);
                });

        // 10. Novel Modern dan Perkembangannya
        readingTexts.stream()
                .filter(rt -> "Novel Modern dan Perkembangannya".equals(rt.getTitle()))
                .findFirst()
                .ifPresent(rt -> {
                    QuizQuestion q19 = createQuizQuestion(rt,
                            "Sekitar abad ke berapakah novel modern mulai berkembang?");
                    q19 = quizQuestionRepository.save(q19);
                    List<QuizOption> q19Options = List.of(
                            createQuizOption(q19, "Abad ke-18", true),
                            createQuizOption(q19, "Abad ke-15", false),
                            createQuizOption(q19, "Abad ke-20", false),
                            createQuizOption(q19, "Abad ke-12", false));
                    q19Options.forEach(quizOptionRepository::save);

                    QuizQuestion q20 = createQuizQuestion(rt,
                            "Manakah di bawah ini yang bukan merupakan faktor yang mempengaruhi perkembangan novel?");
                    q20 = quizQuestionRepository.save(q20);
                    List<QuizOption> q20Options = List.of(
                            createQuizOption(q20, "Kemunduran teknologi cetak", true),
                            createQuizOption(q20, "Perubahan sosial", false),
                            createQuizOption(q20, "Meningkatnya literasi masyarakat", false),
                            createQuizOption(q20, "Teknologi cetak", false));
                    q20Options.forEach(quizOptionRepository::save);
                });

        if (log.isInfoEnabled()) {
            log.info("Seeded quiz questions with options.");
        }
    }

    private Category createCategory(String name) {
        Category category = new Category();
        category.setName(name);
        return category;
    }

    private ReadingText createReadingText(String title, String content, Category category) {
        ReadingText readingText = new ReadingText();
        readingText.setTitle(title);
        readingText.setContent(content);
        readingText.setCategory(category);
        return readingText;
    }

    private QuizQuestion createQuizQuestion(ReadingText readingText, String questionText) {
        QuizQuestion question = new QuizQuestion();
        question.setReadingText(readingText);
        question.setQuestionText(questionText);
        return question;
    }

    private QuizOption createQuizOption(QuizQuestion question, String optionText, boolean isCorrect) {
        QuizOption option = new QuizOption();
        option.setQuizQuestion(question);
        option.setOptionText(optionText);
        option.setCorrect(isCorrect);
        return option;
    }
}