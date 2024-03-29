package com.gugugu.haochat.user.dao;

import com.gugugu.haochat.user.domain.entity.Role;
import com.gugugu.haochat.user.mapper.RoleMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 角色表 服务实现类
 * </p>
 *
 * @author <a href="https://github.com/PLzzz-373">gugugu</a>
 * @since 2024-03-23
 */
@Service
public class RoleDao extends ServiceImpl<RoleMapper, Role> {

}
