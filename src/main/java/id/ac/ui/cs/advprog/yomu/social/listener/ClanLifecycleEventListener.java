package id.ac.ui.cs.advprog.yomu.social.listener;

import id.ac.ui.cs.advprog.yomu.social.event.ClanShouldBeDeletedEvent;
import id.ac.ui.cs.advprog.yomu.social.service.clan.lifecycle.ClanLifecycleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClanLifecycleEventListener {

    private final ClanLifecycleService clanLifecycleService;

    @EventListener
    @Transactional
    public void onClanShouldBeDeleted(ClanShouldBeDeletedEvent event) {
        if (log.isInfoEnabled()) {
            log.info("Handling ClanShouldBeDeletedEvent for clan ID: {}", event.getClanId());
        }
        clanLifecycleService.deleteClan(event.getClanId(), event.getLeaderUsername());
    }
}
