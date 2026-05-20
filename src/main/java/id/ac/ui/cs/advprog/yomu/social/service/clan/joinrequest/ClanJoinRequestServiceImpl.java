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
import id.ac.ui.cs.advprog.yomu.social.event.JoinRequestAcceptedEvent;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.ClanJoinRequest;
import id.ac.ui.cs.advprog.yomu.social.model.ClanJoinRequestStatus;
import id.ac.ui.cs.advprog.yomu.social.port.ClanLookupPort;
import id.ac.ui.cs.advprog.yomu.social.port.ClanMemberValidationPort;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanJoinRequestRepository;
import id.ac.ui.cs.advprog.yomu.social.validation.ClanValidator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClanJoinRequestServiceImpl implements ClanJoinRequestService {

    private final ClanLookupPort clanLookup;
    private final ClanMemberValidationPort memberValidation;
    private final ClanJoinRequestRepository joinRequestRepository;
    private final ClanValidator clanValidator;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void requestJoin(final String clanId, final String username) {
        clanValidator.requireClanId(clanId);
        clanValidator.requireUsername(username);

        final String validClanId = Objects.requireNonNull(clanId);
        clanLookup.findClanById(validClanId)
                .orElseThrow(() -> new IllegalArgumentException(
                        SocialConstants.CLAN_NOT_FOUND_MESSAGE));

        clanValidator.requireNotAlreadyMember(
                memberValidation.existsByClanIdAndUsername(validClanId, username));
        clanValidator.requireNotMemberOfOtherClan(
                memberValidation.existsByUsername(username));

        boolean hasPending = joinRequestRepository
                .findByClanIdAndUsernameAndStatus(validClanId, username, ClanJoinRequestStatus.PENDING)
                .isPresent();
        if (hasPending) {
            throw new IllegalArgumentException(SocialConstants.ALREADY_REQUESTED_JOIN_MESSAGE);
        }

        ClanJoinRequest req = new ClanJoinRequest();
        req.setClanId(validClanId);
        req.setUsername(username);
        req.setStatus(ClanJoinRequestStatus.PENDING);
        req.setCreatedAt(LocalDateTime.now());
        joinRequestRepository.save(req);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClanJoinRequestResponse> getJoinRequests(String clanId, String leaderId, int page, int size) {
        Clan clan = requireLeaderAccess(clanId, leaderId, SocialConstants.ONLY_LEADER_CAN_ACCEPT_REQUEST_MESSAGE);
        final String validClanId = clan.getId();

        Pageable pageable = PageRequest.of(page, size);
        return joinRequestRepository
                .findByClanIdAndStatus(validClanId, ClanJoinRequestStatus.PENDING, pageable)
                .map(r -> new ClanJoinRequestResponse(
                        r.getId(), r.getClanId(), r.getUsername(), r.getStatus().toString(), r.getCreatedAt()));
    }

    @Override
    @Transactional
    public void acceptJoinRequest(String clanId, Long requestId, String leaderId) {
        Clan clan = requireLeaderAccess(clanId, leaderId, SocialConstants.ONLY_LEADER_CAN_ACCEPT_REQUEST_MESSAGE);
        final String validClanId = clan.getId();
        ClanJoinRequest req = requireValidPendingRequest(validClanId, requestId);

        clanValidator.requireNotAlreadyMember(
                memberValidation.existsByClanIdAndUsername(validClanId, req.getUsername()));
        clanValidator.requireNotMemberOfOtherClan(
                memberValidation.existsByUsername(req.getUsername()));
        clanValidator.requireClanNotFull(memberValidation.countByClanId(validClanId));

        req.accept();
        joinRequestRepository.save(req);

        eventPublisher.publishEvent(new JoinRequestAcceptedEvent(this, req.getUsername(), validClanId, clan.getName()));
    }

    @Override
    @Transactional
    public void rejectJoinRequest(String clanId, Long requestId, String leaderId) {
        Clan clan = requireLeaderAccess(clanId, leaderId, SocialConstants.ONLY_LEADER_CAN_REJECT_REQUEST_MESSAGE);
        final String validClanId = clan.getId();
        ClanJoinRequest req = requireValidPendingRequest(validClanId, requestId);

        req.reject();
        joinRequestRepository.save(req);
    }

    @Override
    @Transactional
    public void rejectAllJoinRequests(String clanId, String leaderId) {
        Clan clan = requireLeaderAccess(clanId, leaderId, SocialConstants.ONLY_LEADER_CAN_REJECT_REQUEST_MESSAGE);
        final String validClanId = clan.getId();

        joinRequestRepository.updateStatusByClanIdAndStatus(validClanId, ClanJoinRequestStatus.PENDING,
                ClanJoinRequestStatus.REJECTED);
    }

    private Clan requireLeaderAccess(String clanId, String leaderId, String message) {
        clanValidator.requireClanId(clanId);
        Clan clan = clanLookup.findClanById(Objects.requireNonNull(clanId))
                .orElseThrow(() -> new IllegalArgumentException(SocialConstants.CLAN_NOT_FOUND_MESSAGE));
        clanValidator.requireLeaderPrivilege(clan, leaderId, message);
        return clan;
    }

    private ClanJoinRequest requireValidPendingRequest(String clanId, Long requestId) {
        ClanJoinRequest req = joinRequestRepository
                .findById(Objects.requireNonNull(requestId, "Request ID cannot be null"))
                .orElseThrow(() -> new IllegalArgumentException(SocialConstants.REQUEST_NOT_FOUND_MESSAGE));
        if (!req.getClanId().equals(clanId) || req.getStatus() != ClanJoinRequestStatus.PENDING) {
            throw new IllegalArgumentException(SocialConstants.REQUEST_INVALID_MESSAGE);
        }
        return req;
    }
}
