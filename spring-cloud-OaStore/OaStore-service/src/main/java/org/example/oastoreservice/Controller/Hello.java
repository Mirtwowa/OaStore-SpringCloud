package org.example.oastoreservice.Controller;

import lombok.extern.slf4j.Slf4j;
import org.example.common.vo.UserVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class Hello {
    @PostMapping("/login")
    public UserVo hello(){
        UserVo userVo = new UserVo();
        userVo.setUsername("admin");
        userVo.setPassword("123456");
        return userVo;
    }
}
