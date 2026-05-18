package id.ac.ui.cs.advprog.yomu.reading.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ReadingCompletedEvent extends ApplicationEvent {
    private final Long readingTextId;
    private final String username;

    public ReadingCompletedEvent(Object source, Long readingTextId, String username) {
        super(source);
        this.readingTextId = readingTextId;
        this.username = username;
    }
}