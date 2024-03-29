package com.gugugu.haochat.common.utils.sensitive.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gugugu.haochat.common.utils.sensitive.domain.SensitiveWord;
import com.gugugu.haochat.common.utils.sensitive.mapper.SensitiveWordMapper;
import org.springframework.stereotype.Service;

@Service
public class SensitiveWordDao extends ServiceImpl<SensitiveWordMapper, SensitiveWord> {

}
