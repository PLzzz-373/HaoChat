package com.gugugu.haochat.common.utils.sensitive;

import com.gugugu.haochat.common.utils.sensitive.dao.SensitiveWordDao;
import com.gugugu.haochat.common.utils.sensitive.domain.SensitiveWord;
import com.gugugu.haochat.common.utils.sensitive.strategy.IWordFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MyWordFactory implements IWordFactory {
    @Autowired
    private SensitiveWordDao sensitiveWordDao;

    @Override
    public List<String> getWordList() {
        return sensitiveWordDao.list()
                .stream()
                .map(SensitiveWord::getWord)
                .collect(Collectors.toList());
    }
}
