package org.example.oastoreservice.Controller;

import lombok.extern.slf4j.Slf4j;
import org.example.oastorecommon.vo.UserVo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class Hello {
    @GetMapping("/api/login")
    public UserVo hello(){
        UserVo userVo = new UserVo();
        userVo.setUsername("admin");
        userVo.setPassword("123456");
        return userVo;
    }
}
