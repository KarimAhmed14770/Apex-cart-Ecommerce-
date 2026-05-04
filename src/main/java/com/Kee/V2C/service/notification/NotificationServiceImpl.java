package com.Kee.V2C.service.notification;

import com.Kee.V2C.entity.Vendor;
import com.Kee.V2C.events.OrderPlacedEvent;
import com.Kee.V2C.utils.SecurityUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;


@Service
public class NotificationServiceImpl implements NotificationService{

    private ConcurrentHashMap<Long, CopyOnWriteArrayList<SseEmitter>> registryMap=new ConcurrentHashMap<>();
    private Cache<String,Long> ticketsCache=Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .maximumSize(1000)
            .build()
            ;
    private final SecurityUtil securityUtil;

    @Autowired
    public NotificationServiceImpl(SecurityUtil securityUtil){
        this.securityUtil=securityUtil;
    }



    @Override
    public String createTicket(){
        Long vendorId= securityUtil.getCurrentUserId();
        String ticket=UUID.randomUUID().toString();
        ticketsCache.put(ticket,vendorId);
        return ticket;
    }

    @Override
    public Long validateAndRemoveTicket(String ticket){
        Long  vendorId=ticketsCache.getIfPresent(ticket);
        if(vendorId!=null){
            ticketsCache.invalidate(ticket);//destroy ticket after one usage
        }
        return vendorId;
    }

    @Override
    public SseEmitter subscribe(){
        Long vendorId=securityUtil.getCurrentUserId();
        SseEmitter sseEmitter=new SseEmitter();
        registryMap.computeIfAbsent(vendorId,k->new CopyOnWriteArrayList<>()).add(sseEmitter);
        //callbacks for cleanup the emitters on closing the connection
        sseEmitter.onCompletion(() -> registryMap.get(vendorId).remove(sseEmitter));
        sseEmitter.onTimeout(() -> registryMap.get(vendorId).remove(sseEmitter));
        sseEmitter.onError(e -> registryMap.get(vendorId).remove(sseEmitter));
        return sseEmitter;
    }

    @Override
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrders(OrderPlacedEvent orderPlacedEvent){
        CopyOnWriteArrayList<SseEmitter> sseEmitters=registryMap.get(orderPlacedEvent.getVendorId());
        if(sseEmitters==null || sseEmitters.isEmpty()) return;
        List<SseEmitter> deadEmitters=new ArrayList<>();
        for(SseEmitter sseEmitter:sseEmitters){
            try {
                sseEmitter.send(SseEmitter.event().name("new-order")
                        .data("SubOrder #" + orderPlacedEvent.getSubOrderId()));
            }
            catch (Exception e){
                deadEmitters.add(sseEmitter);
            }
        }
        sseEmitters.removeAll(deadEmitters);

    }

}
