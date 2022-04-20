package com.example.krigingweb.Interpolation.Distributor;

import com.example.krigingweb.Interpolation.Distributor.Core.InterpolaterNode;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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

    public InterpolaterNode exception(UUID interpolaterID){
        InterpolaterNode interpolaterNode = this.interpolaterNodeMap.get(interpolaterID);
        if(interpolaterNode != null){
            interpolaterNode.exception();
        }
        return interpolaterNode;
    }

    public InterpolaterNode working(UUID interpolaterID){
        InterpolaterNode interpolaterNode = this.interpolaterNodeMap.get(interpolaterID);
        if(interpolaterNode != null){
            interpolaterNode.working();
        }
        return interpolaterNode;
    }
}
