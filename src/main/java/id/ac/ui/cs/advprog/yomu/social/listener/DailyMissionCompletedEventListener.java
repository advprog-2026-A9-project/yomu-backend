package id.ac.ui.cs.advprog.yomu.social.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.gamification.event.AllDailyMissionsCompletedEvent;
import id.ac.ui.cs.advprog.yomu.gamification.event.DailyMissionCompletedEvent;
import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanMemberRepository;
import id.ac.ui.cs.advprog.yomu.social.service.modifier.buff.BuffApplicationService;
import id.ac.ui.cs.advprog.yomu.social.service.score.ClanScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailyMissionCompletedEventListener {

    private final ClanMemberRepository memberRepository;
    private final ClanScoreService scoreService;
    private final BuffApplicationService buffApplicationService;

    @EventListener
    @Transactional
    public void onDailyMissionCompleted(DailyMissionCompletedEvent event) {
        if (log.isInfoEnabled()) {
            log.info("Received DailyMissionCompletedEvent for user: {} with reward score: {}", event.username(), event.score());
        }
        memberRepository.findByUsername(event.username())
                .map(member -> member.getClanId())
                .filter(clanId -> clanId != null && !clanId.isBlank())
                .ifPresent(clanId -> {
                    scoreService.updateClanScore(clanId, event.score());
                    if (log.isInfoEnabled()) {
                        log.info("Successfully updated clan score for clan {} by {} points due to daily mission completion",
                                clanId, event.score());
                    }
                });
    }

    @EventListener
    @Transactional
    public void onAllDailyMissionsCompleted(AllDailyMissionsCompletedEvent event) {
        if (log.isInfoEnabled()) {
            log.info("Received AllDailyMissionsCompletedEvent for user: {}", event.username());
        }
        memberRepository.findByUsername(event.username())
                .map(member -> member.getClanId())
                .filter(clanId -> clanId != null && !clanId.isBlank())
                .ifPresent(clanId -> {
                    buffApplicationService.applyBuff(clanId, SocialConstants.DAILY_MISSION_BUFF_KEY);
                    if (log.isInfoEnabled()) {
                        log.info("Delegated applying daily mission buff for clan {} to BuffApplicationService", clanId);
                    }
                });
    }
}
