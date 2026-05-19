package id.ac.ui.cs.advprog.yomu.social.service.clan.joinrequest;

import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanJoinRequestResponse;
import id.ac.ui.cs.advprog.yomu.social.event.UserJoinClanEvent;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.ClanJoinRequest;
import id.ac.ui.cs.advprog.yomu.social.model.ClanMember;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanJoinRequestRepository;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanMemberRepository;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanRepository;
import id.ac.ui.cs.advprog.yomu.social.validation.ClanValidator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClanJoinRequestServiceImpl implements ClanJoinRequestService {

    private final ClanRepository clanRepository;
    private final ClanMemberRepository memberRepository;
    private final ClanJoinRequestRepository joinRequestRepository;
    private final ClanValidator clanValidator;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void requestJoin(final String clanId, final String username) {
        clanValidator.requireClanId(clanId);
        clanValidator.requireUsername(username);

        final String validClanId = Objects.requireNonNull(clanId);
        clanRepository.findById(validClanId)
                .orElseThrow(() -> new IllegalArgumentException(
                        SocialConstants.CLAN_NOT_FOUND_MESSAGE));

        clanValidator.requireNotAlreadyMember(
                memberRepository.findByClanIdAndUsername(validClanId, username).isPresent());
        clanValidator.requireNotMemberOfOtherClan(
                memberRepository.findByUsername(username).isPresent());

        boolean hasPending = joinRequestRepository
                .findByClanIdAndUsernameAndStatus(validClanId, username, SocialConstants.REQUEST_STATUS_PENDING)
                .isPresent();
        if (hasPending) {
            throw new IllegalArgumentException("Anda sudah mengirimkan request join ke clan ini.");
        }

        ClanJoinRequest req = new ClanJoinRequest();
        req.setClanId(validClanId);
        req.setUsername(username);
        req.setStatus(SocialConstants.REQUEST_STATUS_PENDING);
        req.setCreatedAt(LocalDateTime.now());
        joinRequestRepository.save(req);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClanJoinRequestResponse> getJoinRequests(String clanId, String leaderId, int page, int size) {
        clanValidator.requireClanId(clanId);
        final String validClanId = Objects.requireNonNull(clanId);
        Clan clan = clanRepository.findById(validClanId)
                .orElseThrow(() -> new IllegalArgumentException(
                        SocialConstants.CLAN_NOT_FOUND_MESSAGE));
        clanValidator.requireLeaderPrivilege(clan, leaderId, "Hanya ketua yang dapat melihat join requests.");

        Pageable pageable = PageRequest.of(page, size);
        return joinRequestRepository.findByClanIdAndStatus(validClanId, SocialConstants.REQUEST_STATUS_PENDING, pageable)
                .map(r -> new ClanJoinRequestResponse(
                        r.getId(), r.getClanId(), r.getUsername(), r.getStatus(), r.getCreatedAt()));
    }

    @Override
    @Transactional
    public void acceptJoinRequest(String clanId, Long requestId, String leaderId) {
        clanValidator.requireClanId(clanId);
        final String validClanId = Objects.requireNonNull(clanId);
        Clan clan = clanRepository.findById(validClanId)
                .orElseThrow(() -> new IllegalArgumentException(
                        SocialConstants.CLAN_NOT_FOUND_MESSAGE));
        clanValidator.requireLeaderPrivilege(clan, leaderId, "Hanya ketua yang dapat menerima request.");

        final Long validRequestId = Objects.requireNonNull(requestId, "Request ID cannot be null");
        ClanJoinRequest req = joinRequestRepository.findById(validRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Request tidak ditemukan."));

        if (!req.getClanId().equals(validClanId)
                || !SocialConstants.REQUEST_STATUS_PENDING.equals(req.getStatus())) {
            throw new IllegalArgumentException("Request tidak valid.");
        }

        clanValidator.requireNotAlreadyMember(
                memberRepository.findByClanIdAndUsername(validClanId, req.getUsername()).isPresent());
        clanValidator.requireNotMemberOfOtherClan(
                memberRepository.findByUsername(req.getUsername()).isPresent());
        clanValidator.requireClanNotFull(memberRepository.countByClanId(validClanId));

        req.setStatus(SocialConstants.REQUEST_STATUS_ACCEPTED);
        joinRequestRepository.save(req);

        final ClanMember member = new ClanMember();
        member.setUsername(req.getUsername());
        member.setClanId(validClanId);
        member.setRole(SocialConstants.ROLE_MEMBER);
        memberRepository.save(member);

        eventPublisher.publishEvent(new UserJoinClanEvent(this, req.getUsername(), validClanId, clan.getName(),
                clan.getTier() != null ? clan.getTier().name() : "BRONZE"));
    }

    @Override
    @Transactional
    public void rejectJoinRequest(String clanId, Long requestId, String leaderId) {
        clanValidator.requireClanId(clanId);
        final String validClanId = Objects.requireNonNull(clanId);
        Clan clan = clanRepository.findById(validClanId)
                .orElseThrow(() -> new IllegalArgumentException(
                        SocialConstants.CLAN_NOT_FOUND_MESSAGE));
        clanValidator.requireLeaderPrivilege(clan, leaderId, "Hanya ketua yang dapat menolak request.");

        final Long validRequestId = Objects.requireNonNull(requestId, "Request ID cannot be null");
        ClanJoinRequest req = joinRequestRepository.findById(validRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Request tidak ditemukan."));

        if (!req.getClanId().equals(validClanId)
                || !SocialConstants.REQUEST_STATUS_PENDING.equals(req.getStatus())) {
            throw new IllegalArgumentException("Request tidak valid.");
        }

        req.setStatus(SocialConstants.REQUEST_STATUS_REJECTED);
        joinRequestRepository.save(req);
    }

    @Override
    @Transactional
    public void rejectAllJoinRequests(String clanId, String leaderId) {
        clanValidator.requireClanId(clanId);
        final String validClanId = Objects.requireNonNull(clanId);
        Clan clan = clanRepository.findById(validClanId)
                .orElseThrow(() -> new IllegalArgumentException(
                        SocialConstants.CLAN_NOT_FOUND_MESSAGE));
        clanValidator.requireLeaderPrivilege(clan, leaderId, "Hanya ketua yang dapat menolak request.");

        joinRequestRepository.updateStatusByClanIdAndStatus(validClanId, SocialConstants.REQUEST_STATUS_PENDING,
                SocialConstants.REQUEST_STATUS_REJECTED);
    }
}
