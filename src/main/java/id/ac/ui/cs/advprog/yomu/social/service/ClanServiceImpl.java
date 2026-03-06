package id.ac.ui.cs.advprog.yomu.social.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.social.dto.ClanRequest;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.ClanMember;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanMemberRepository;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClanServiceImpl implements ClanService {

    private final ClanRepository clanRepository;
    private final ClanMemberRepository memberRepository;

    @Override
    @Transactional
    public Clan createClan(final ClanRequest request) {
        if (clanRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Nama Clan sudah digunakan");
        }

        final Clan clan = new Clan();
        clan.setName(request.getName());
        clan.setDescription(request.getDescription());
        clan.setLeaderUserId(request.getUserId());
        
        final Clan savedClan = clanRepository.save(clan);

        joinClan(savedClan.getId(), request.getUserId());
        
        return savedClan;
    }

    @Override
    @Transactional
    public void joinClan(final String clanId, final String userId) {
        clanRepository.findById(clanId)
                .orElseThrow(() -> new IllegalArgumentException("Clan tidak ditemukan"));

        memberRepository.findByClanIdAndUserId(clanId, userId)
                .ifPresent(existing -> {
                    throw new IllegalStateException("Kamu sudah menjadi anggota Clan ini");
                });

        memberRepository.findByUserId(userId)
                .ifPresent(existing -> {
                    throw new IllegalStateException("Kamu sudah tergabung di Clan lain");
                });

        final ClanMember member = new ClanMember();
        member.setClanId(clanId);
        member.setUserId(userId);
        memberRepository.save(member);
    }

    @Override
    public List<Clan> findAll() {
        return clanRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteClan(final String clanId, final String leaderId) {
        final Clan clan = clanRepository.findById(clanId)
                .orElseThrow(() -> new IllegalArgumentException("Clan tidak ditemukan"));
        
        if (!clan.getLeaderUserId().equals(leaderId)) {
            throw new IllegalStateException("Hanya Leader yang bisa menghapus Clan");
        }
        
        memberRepository.deleteByClanId(clanId);
        
        clanRepository.delete(clan);
    }

    @Override
    @Transactional
    public void leaveClan(final String clanId, final String userId) {
        final Clan clan = clanRepository.findById(clanId)
                .orElseThrow(() -> new IllegalArgumentException("Clan tidak ditemukan"));

        if (clan.getLeaderUserId().equals(userId)) {
            handleLeaderLeave(clan, userId);
        } else {
            memberRepository.deleteByClanIdAndUserId(clanId, userId);
        }
    }

    private void handleLeaderLeave(final Clan clan, final String leaderId) {
        final List<ClanMember> allMembers = memberRepository.findByClanId(clan.getId());
        int MIN_CLAN_SIZE = 1;

        if (allMembers.size() <= MIN_CLAN_SIZE) {
            memberRepository.deleteByClanIdAndUserId(clan.getId(), leaderId);
            clanRepository.delete(clan);
        }

        else {
            final String newLeaderId = allMembers.stream()
                    .map(ClanMember::getUserId)
                    .filter(id -> !id.equals(leaderId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Gagal menemukan pengganti Leader"));

            clan.setLeaderUserId(newLeaderId);
            clanRepository.save(clan);
            memberRepository.deleteByClanIdAndUserId(clan.getId(), leaderId);
        }
    }
}