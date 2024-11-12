package org.example.oastoreaop.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.oastoreaop.vo.User;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select("select * from `user`")
    List<User> selectAll();

    @Insert("insert into user(username , password,create_time,update_time,name,stu_id)"+
            "values (#{username},#{password},now(),now(),#{name},#{stu_id})")
    void insertUser(String username,String password,String name,String stu_id);

    @Select("select * from user where username=#{account}")
    User findByUserName(String account);

    @Update("update user set user.username=#{name},update_time=#{updateTime} where username =#{username}")
    void update(User user);

    @Update("update user set user_pic=#{UserIcon},update_time=now() where id =#{id}")
    void updateUserIcon(String UserIcon, Integer id);

    @Update("update user set password=#{md5String},update_time=now() where username=#{account}")
    void updatePwd(String md5String, String account);
}
