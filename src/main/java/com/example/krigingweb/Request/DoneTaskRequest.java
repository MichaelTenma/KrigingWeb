package com.example.krigingweb.Request;

import com.example.krigingweb.Entity.LandEntity;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class DoneTaskRequest {
    public UUID interpolaterID;
    public UUID taskID;
    public List<LandEntity> landEntityList;
}
