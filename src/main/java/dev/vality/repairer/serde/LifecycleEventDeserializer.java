package dev.vality.repairer.serde;

import dev.vality.kafka.common.serialization.AbstractThriftDeserializer;
import dev.vality.machinegun.lifesink.LifecycleEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LifecycleEventDeserializer extends AbstractThriftDeserializer<LifecycleEvent> {

    @Override
    public LifecycleEvent deserialize(String topic, byte[] data) {
        return this.deserialize(data, new LifecycleEvent());
    }

}