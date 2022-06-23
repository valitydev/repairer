package dev.vality.repairer.listener;

import dev.vality.kafka.common.util.LogUtil;
import dev.vality.machinegun.lifesink.LifecycleEvent;
import dev.vality.repairer.service.MachineEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MachineEventKafkaListener {

    private final MachineEventService machineEventService;

    @KafkaListener(topics = "${kafka.topics.lifecycle.id}", containerFactory = "lcContainerFactory")
    public void handle(List<ConsumerRecord<String, LifecycleEvent>> messages, Acknowledgment ack) {
        log.debug("Got lifecycle machine event batch with size: {}", messages.size());
        for (ConsumerRecord<String, LifecycleEvent> message : messages) {
            machineEventService.processEvent(message.value());
        }
        ack.acknowledge();
        log.debug("Batch lifecycle has been committed, size={}, {}", messages.size(),
                LogUtil.toString(messages));
    }
}