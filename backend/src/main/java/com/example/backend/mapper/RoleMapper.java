package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {
    
    @Select("SELECT r.* FROM roles r " +
            "INNER JOIN user_roles ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<Role> selectRolesByUserId(@Param("userId") Long userId);
    
    @Select("SELECT r.* FROM roles r " +
            "INNER JOIN role_permissions rp ON r.id = rp.role_id " +
            "WHERE rp.permission_id = #{permissionId}")
    List<Role> selectRolesByPermissionId(@Param("permissionId") Long permissionId);
}
