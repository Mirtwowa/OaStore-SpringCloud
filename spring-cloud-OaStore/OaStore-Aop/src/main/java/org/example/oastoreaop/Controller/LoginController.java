package org.example.oastoreaop.Controller;

import org.example.oastoreaop.mapper.UserMapper;
import org.example.oastoreaop.service.UserService;
import org.example.oastoreaop.util.EncryptUtils;
import org.example.oastoreaop.util.JwtUtil;
import org.example.oastoreaop.util.ThreadLocalUtil;
import org.example.oastoreaop.vo.Result;
import org.example.oastoreaop.vo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Pattern;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@RestController
public class LoginController {
    @Autowired
    SimpleEmailCodeAPI simpleEmailCodeAPI;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    UserMapper userMapper;
    @Autowired
    UserService userService;

    @PostMapping("/register")
    public Result register(String account, String captcha, String password, String name, String userId, String stu_id){
        String verify = simpleEmailCodeAPI.verifyCode(userId,captcha);
        User user = userService.findByUserName(account);
        if (user == null) {
            //没有占用
            //注册
            userService.insertUser(account, password,name,stu_id);
            User loginuser = userService.findByUserName(account);
            String token = String.valueOf(userService.insertRedis(loginuser));
            return Result.success(token);
        } else if(user == null&& Objects.equals(verify, "验证码错误或者不存在！")){
            //占用
            return Result.error("验证码错误或者不存在！");
        }else {
            return Result.error("用户名已被占用");
        }
    }

    @RequestMapping("/userLogin")
    public Result<String> loginUser(@Pattern(regexp = "^\\s{1,100}")String account, String password){
        User loginuser = userService.findByUserName(account);
        System.out.println(account+password);
        if (loginuser==null){
            return Result.error("用户名错误");
        }
        if (EncryptUtils.getMD5String(password).equals(loginuser.getPassword())){
            Map<String,Object> claims =new HashMap<>();
            claims.put("id",loginuser.getId());
            claims.put("username",loginuser.getUsername());
            claims.put("password",loginuser.getPassword());
            //生成请求头
            String token = JwtUtil.genToken(claims);
            ValueOperations<String,String> operations = stringRedisTemplate.opsForValue();
            operations.set("AuthKey:"+token,token,1, TimeUnit.DAYS);
            return Result.success(token);
        }

        return   Result.error("密码错误");
    }
    @GetMapping("/userinfo")
    public User userInfo(){
        Map<String,Object> map = ThreadLocalUtil.get();
        String username = (String) map.get("username");
        return userService.findByUserName(username);
    }
    @PostMapping("/resetPwd")
    public Result ResetPwd(String account,String captcha,String newPassword,String userId){
        User user = userService.findByUserName(account);
        String verify = simpleEmailCodeAPI.verifyCode(userId,captcha);
        if (user != null&& Objects.equals(verify, "校验成功！")) {
            userService.updatePwd(newPassword,account);
            Map<String,Object> claims =new HashMap<>();
            claims.put("id",user.getId());
            claims.put("username",user.getUsername());
            claims.put("password",user.getPassword());
            //生成请求头
            String token = JwtUtil.genToken(claims);
            ValueOperations<String,String> operations = stringRedisTemplate.opsForValue();
            operations.set(token,token,1, TimeUnit.DAYS);
            return Result.success(token);
        } else if(user != null&& Objects.equals(verify, "验证码错误或者不存在！")){
            //占用
            return Result.error("验证码错误或者不存在！");
        }else {
            return Result.error("用户名不存在");
        }
    }
}

