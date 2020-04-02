package com.bluesgao.funnymock.matcher;

import com.alibaba.fastjson.JSON;
import com.bluesgao.funnymock.data.DataReader;
import com.bluesgao.funnymock.data.Record;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RecordMatcher {
    public Record match(String method, String uri) {
        log.info("RecordMatcher match uri:{},method:{}", uri, method);
        Record record = DataReader.recordMap.get(method + ":" + uri);
        if (record == null) {
            record = DataReader.DEFAULT_RECORD;
            record.getReq().setMethod(method);
            record.getReq().setUri(uri);
        }
        log.info("RecordMatcher match record:{}", JSON.toJSONString(record));
        return record;
    }
}
