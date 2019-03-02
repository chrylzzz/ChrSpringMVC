package com.lnsoft.service.impl;

import com.lnsoft.annotation.ChrService;
import com.lnsoft.service.MyService;

/**
 * Created By Chr on 2019/1/24/0024.
 */
//iocMap.put("myServiceImpl",new MyServiceImpl());
@ChrService("myServiceImpl")
public class MyServiceImpl implements MyService {
    @Override
    public String query(String name, String age) {
        return "===name=="+name+"===age==="+age;
    }

}
