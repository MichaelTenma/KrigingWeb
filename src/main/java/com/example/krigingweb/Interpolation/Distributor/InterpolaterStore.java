package com.example.krigingweb.Interpolation.Distributor;

import com.example.krigingweb.Interpolation.Distributor.Core.InterpolaterNode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
class InterpolaterStore {
    @Getter
    private final Map<UUID, InterpolaterNode> interpolaterNodeMap;

    public InterpolaterStore() {
        this.interpolaterNodeMap = new ConcurrentHashMap<>();
    }

    public void registerInterpolater(InterpolaterNode interpolaterNode){
        interpolaterNodeMap.put(interpolaterNode.id, interpolaterNode);
    }

    public void deleteInterpolater(UUID interpolaterID){
        this.interpolaterNodeMap.remove(interpolaterID);
    }

    public boolean hasInterpolater(UUID interpolaterID){
        return this.interpolaterNodeMap.get(interpolaterID) != null;
    }

    public InterpolaterNode getInterpolater(UUID interpolaterID){
        return this.interpolaterNodeMap.get(interpolaterID);
    }

    public InterpolaterNode working(UUID interpolaterID){
        InterpolaterNode interpolaterNode = this.interpolaterNodeMap.get(interpolaterID);
        if(interpolaterNode != null){
            interpolaterNode.doneTask();
        }
        return interpolaterNode;
    }

    public InterpolaterNode getRandomInterpolater(){
        UUID[] uuidArray = this.interpolaterNodeMap.keySet().toArray(new UUID[0]);
        int index = new Random().nextInt() % uuidArray.length;

        InterpolaterNode interpolaterNode = null;
        if(index >= 0 && index < uuidArray.length){
            interpolaterNode = this.interpolaterNodeMap.get(uuidArray[index]);
        }
        return interpolaterNode;
    }

    public void heartBeat(UUID interpolaterID){
        InterpolaterNode interpolaterNode = this.getInterpolater(interpolaterID);
        if(interpolaterNode != null){
            interpolaterNode.heartBeat();
            log.info("[DISTRIBUTOR]: Interpolater " + interpolaterID + " heartbeats.");
        }
    }

    public void heartBeatDetected(){
        if(this.interpolaterNodeMap.size() > 0){
            List<UUID> removeList = new LinkedList<>();
            for(Map.Entry<UUID, InterpolaterNode> entry : this.interpolaterNodeMap.entrySet()){
                if(!entry.getValue().isValid()){
                    removeList.add(entry.getKey());
                }
            }
            removeList.forEach(uuid -> {
                this.interpolaterNodeMap.remove(uuid);
                log.warn("[DISTRIBUTOR]: Interpolater " + uuid + " has been removed for no heartbeat!");
            });
        }
    }
}
