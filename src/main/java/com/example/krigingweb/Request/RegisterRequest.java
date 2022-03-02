package com.example.krigingweb.Request;

import com.example.krigingweb.Interpolation.Basic.Enum.CallbackHttpEnum;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    public UUID interpolaterID;
    public String apiPath;
    public String port;
    public CallbackHttpEnum callbackHttpEnum;
}
