package com.example.krigingweb.Controller;

import com.example.krigingweb.Exception.EmptyException;
import com.example.krigingweb.Interpolation.Core.TaskData;
import com.example.krigingweb.Interpolation.Interpolater.InterpolaterManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@ConditionalOnProperty(prefix = "interpolater", name = "enable", havingValue = "true")
@Slf4j
@RestController
@RequestMapping(value = "/interpolater")
public class InterpolaterController {

    private final InterpolaterManager interpolaterManager;

    @Autowired
    public InterpolaterController(InterpolaterManager interpolaterManager) {
        this.interpolaterManager = interpolaterManager;
    }

    @PostMapping("/addTask")
    public ResponseEntity<String> addTask(@RequestBody TaskData taskData) throws EmptyException {
        EmptyException.check("taskData", taskData);

        this.interpolaterManager.addTask(taskData);
        log.info(
            "[INTERPOLATER]: add task " + taskData.taskID +
            ". SamplePoints: " + taskData.getSamplePointEntityList().size() +
            ", LandEntities: " + taskData.getLandEntityList().size()
        );
        return new ResponseEntity<>("添加插值任务成功！", HttpStatus.OK);
    }

    @GetMapping("/showID")
    public ResponseEntity<String> showID(){
        return new ResponseEntity<>(this.interpolaterManager.interpolaterID.toString(), HttpStatus.OK);
    }
}
