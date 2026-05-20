package id.ac.ui.cs.advprog.yomu.social.service.clan.lifecycle;

import java.util.Objects;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanRequest;
import id.ac.ui.cs.advprog.yomu.social.event.ClanCreatedEvent;
import id.ac.ui.cs.advprog.yomu.social.event.ClanNameChangedEvent;
import id.ac.ui.cs.advprog.yomu.social.event.UserDeleteClanEvent;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.Tier;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanMemberRepository;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanRepository;
import id.ac.ui.cs.advprog.yomu.social.validation.ClanValidator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ClanLifecycleServiceImpl implements ClanLifecycleService {

    private final ClanRepository clanRepository;
    private final ClanMemberRepository memberRepository;
    private final ClanValidator clanValidator;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public Clan createClan(final ClanRequest request) {
        final String validUsername = Objects.requireNonNull(request.getUsername(), "Username cannot be null");
        clanValidator.requireValidClanName(request.getName());
        clanValidator.requireValidClanDescription(request.getDescription());
        clanValidator.requireClanNameAvailable(clanRepository.existsByName(request.getName()));

        clanValidator.requireNotMemberOfOtherClan(
                memberRepository.findByUsername(validUsername).isPresent());

        final Clan clan = new Clan();
        clan.setName(request.getName());
        clan.setDescription(request.getDescription());
        clan.setLeaderUsername(validUsername);
        clan.setTier(Tier.BRONZE);
        clan.setScore(0);
        final Clan savedClan = clanRepository.save(clan);

        eventPublisher.publishEvent(new ClanCreatedEvent(
                this, validUsername, savedClan.getId(), savedClan.getName(),
                savedClan.getTier() != null ? savedClan.getTier().name() : "BRONZE"
        ));

        return savedClan;
    }

    @Override
    @Transactional
    public Clan editClan(final String clanId, final String leaderUsername, final ClanRequest request) {
        clanValidator.requireClanId(clanId);
        final String validClanId = Objects.requireNonNull(clanId);
        final Clan clan = clanRepository.findById(validClanId)
                .orElseThrow(() -> new IllegalArgumentException(
                        SocialConstants.CLAN_NOT_FOUND_MESSAGE));
        clanValidator.requireLeaderPrivilege(clan, leaderUsername, "Hanya Leader yang dapat mengubah info clan");

        clanValidator.requireValidClanName(request.getName());
        clanValidator.requireValidClanDescription(request.getDescription());

        final String oldName = clan.getName();
        if (!Objects.equals(oldName, request.getName())) {
            clanValidator.requireClanNameAvailable(clanRepository.existsByName(request.getName()));
            clan.setName(request.getName());
        }
        clan.setDescription(request.getDescription());
        final Clan updated = clanRepository.save(clan);

        if (!Objects.equals(oldName, request.getName())) {
            eventPublisher.publishEvent(new ClanNameChangedEvent(this, validClanId, request.getName()));
        }

        return updated;
    }

    @Override
    @Transactional
    public void deleteClan(final String clanId, final String leaderUsername) {
        clanValidator.requireClanId(clanId);
        final String validClanId = Objects.requireNonNull(clanId);
        final Clan clan = clanRepository.findById(validClanId)
                .orElseThrow(() -> new IllegalArgumentException(
                        SocialConstants.CLAN_NOT_FOUND_MESSAGE));
        clanValidator.requireLeaderPrivilege(clan, leaderUsername, "Hanya Leader yang dapat menghapus clan");

        clanRepository.delete(clan);

        eventPublisher.publishEvent(new UserDeleteClanEvent(this, validClanId));
    }
}
