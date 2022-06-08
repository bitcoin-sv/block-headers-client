package io.bitcoinsv.headerSV.core.service.network.impl;

import io.bitcoinsv.headerSV.core.common.EventConsumer;
import io.bitcoinsv.headerSV.core.common.MessageConsumer;
import io.bitcoinsv.headerSV.core.service.network.NetworkConsumerConfig;
import io.bitcoinsv.jcl.net.network.events.P2PEvent;
import io.bitcoinsv.jcl.net.protocol.events.data.MsgReceivedEvent;
import io.bitcoinsv.jcl.net.protocol.messages.common.Message;
import io.bitcoinsv.jcl.tools.events.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2020 Bitcoin Association
 *
 * Base class for a NetworkService. It provides functionality to subscribe to specific incoming messages.
 * @author i.fernandez@nchain.com
 */
public abstract class NetworkServiceBase {

    private Logger log = LoggerFactory.getLogger(NetworkServiceBase.class);

    // Map all the subscribers by message
    private Map<Class<? extends Message>, Map<MessageConsumer, NetworkConsumerConfig>> messageConsumers = new ConcurrentHashMap<>();
    private Map<Class<? extends Event>, List<EventConsumer>> eventConsumers = new ConcurrentHashMap<>();

    //keep track of received messages
    private Set<Long> processedMessages = Collections.synchronizedSet(new HashSet<>());

    /**
     * Constructor.
     */
    public NetworkServiceBase() { }


    public void subscribe(Class<? extends Message> eventClass, MessageConsumer messageConsumer, boolean requiresMinimumPeers, boolean sendDuplicates) {
        HashMap<MessageConsumer, NetworkConsumerConfig> entry = new HashMap<>();
        entry.put(messageConsumer, NetworkConsumerConfig.builder()
                .requiresMinimumPeers(Boolean.valueOf(requiresMinimumPeers))
                .sendDuplicates(sendDuplicates)
                .build());

        messageConsumers.merge(eventClass, entry, (w, prev) -> {
            prev.putAll(w);
            return prev;
        });
    }

    public void subscribe(Class<? extends Event> eventClass, EventConsumer messageConsumer) {
        List<EventConsumer> entry = new ArrayList<>();
        entry.add(messageConsumer);

        eventConsumers.merge(eventClass, entry, (w, prev) -> {
            prev.addAll(w);
            return prev;
        });
    }

    public void onMessage(MsgReceivedEvent msgReceivedEvent) {

        Map<MessageConsumer, NetworkConsumerConfig> handlers = messageConsumers.get(msgReceivedEvent.getBtcMsg().getBody().getClass());

        if (handlers == null) {
            return;
        }

        handlers.forEach((consumer, config) -> {
            if (config.isRequiresMinimumPeers()) {
                if(!checkMinimumPeersConnected()) {
                    log.info("Message " + msgReceivedEvent.getBtcMsg().getHeader().getCommand() + " rejected. Not enough connected peers.");
                    return;
                }
            }

            // Check if we've already processed this header message
            if(!config.isSendDuplicates()) {
                if (processedMessages.contains(msgReceivedEvent.getBtcMsg().getHeader().getChecksum())) {
                    return;
                } else {
                    processedMessages.add(msgReceivedEvent.getBtcMsg().getHeader().getChecksum());
                }
            }

            consumer.consume(msgReceivedEvent.getBtcMsg(), msgReceivedEvent.getPeerAddress());
        });
    }

    public void onEvent(Event event) {
        if (eventConsumers.containsKey(event.getClass())) {
            eventConsumers.get(event.getClass()).forEach(c -> c.consume(event));
        }
    }
    protected abstract boolean checkMinimumPeersConnected();

}
