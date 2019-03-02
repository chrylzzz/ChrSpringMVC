package com.lnsoft.controller;

import com.lnsoft.annotation.ChrAutowired;
import com.lnsoft.annotation.ChrController;
import com.lnsoft.annotation.ChrRequestMapping;
import com.lnsoft.annotation.ChrRequestParam;
import com.lnsoft.service.MyService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * Created By Chr on 2019/1/24/0024.
 */
@ChrController("myController")
@ChrRequestMapping("/chr")
public class MyController {

    @ChrAutowired("myServiceImpl") //iocMap.get("MyServiceImpl")
    private MyService myService;

    @ChrRequestMapping("/query")
    public void query(HttpServletRequest request,//
                      HttpServletResponse response,//
                      @ChrRequestParam("name") String name,//
                      @ChrRequestParam("age") String age) {
        try {
            PrintWriter printWriter = response.getWriter();
            String result = myService.query(name, age);
            printWriter.write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
