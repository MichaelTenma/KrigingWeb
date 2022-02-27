package com.example.krigingweb.Controller;

import com.example.krigingweb.Exception.EmptyException;
import com.example.krigingweb.Interpolation.Core.TaskData;
import com.example.krigingweb.Interpolation.Interpolater.InterpolaterManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/interpolater")
@ResponseBody
public class InterpolaterController {

    private final InterpolaterManager interpolaterManager;

    @Autowired
    public InterpolaterController(InterpolaterManager interpolaterManager) {
        this.interpolaterManager = interpolaterManager;
    }

    @PostMapping("/addTask")
    public ResponseEntity<String> addTask(TaskData taskData) throws EmptyException {
        EmptyException.check("taskData", taskData);

        this.interpolaterManager.addTask(taskData);
        return new ResponseEntity<>("添加插值任务成功！", HttpStatus.OK);
    }
}