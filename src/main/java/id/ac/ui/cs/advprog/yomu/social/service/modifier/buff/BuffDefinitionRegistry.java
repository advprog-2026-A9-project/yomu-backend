package id.ac.ui.cs.advprog.yomu.social.service.modifier.buff;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BuffDefinitionRegistry {
    private final List<BuffDefinition> definitions;

    public Optional<BuffDefinition> findByKey(String key) {
        return definitions.stream()
                .filter(d -> d.getKey().equals(key))
                .findFirst();
    }
}
