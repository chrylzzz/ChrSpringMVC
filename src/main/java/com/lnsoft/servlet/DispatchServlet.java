package com.lnsoft.servlet;

import com.lnsoft.annotation.*;
import com.lnsoft.controller.MyController;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created By Chr on 2019/1/24/0024.
 */
public class DispatchServlet extends HttpServlet {

    //用来存放 tomcat启动时实例化的对象
    List<String> classNames = new ArrayList<>();
    //ioc容器:key-注解的值，value-实例化的对象
    Map<String, Object> beans = new HashMap<>();
    //路径映射方法
    Map<String, Object> handerMap = new HashMap<>();

    /**
     * tomcat执行时要执行的方法
     * <load-on-startup>0</load-on-startup>,导致tomcat启动会执行该方法
     * 初始化方法：bean初始化,autowired ,url--method
     * 想要初始化，必须先要扫描spring bean，扫描那些是核心类
     */
    public void init(ServletConfig config) throws ServletException {
        //扫描：得到com下的全类路径名
        doScanPackage("com.lnsoft");
        //实例化:把扫描的实例化
        doInstance();
        //自动装配：哪些类对象，自动注入进去，Autowired
        doAutowired();
        //方法和路径url一对一映射：http://127.0.0.1:8080/map.put(/chr/query,query())
        doMethodToUrlMapping();
    }

    private void doMethodToUrlMapping() {
        //只需要有路径的注解
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            Object instance = entry.getValue();
            Class<?> clazz = instance.getClass();
            if (clazz.isAnnotationPresent(ChrController.class)) {//这里只需要看看是不是控制类，控制类才有拦截路径url
                //拿类上拦截的的路径
                ChrRequestMapping requestMapping1 = clazz.getAnnotation(ChrRequestMapping.class);
                String classPath = requestMapping1.value();//  /chr
                //拿该类中方法拦截的路径
                Method[] methods = clazz.getMethods();//拿到类里所有的方法  query()
                for (Method method : methods) {
                    if (method.isAnnotationPresent(ChrRequestMapping.class)) {//该方法有没有拦截url
                        ChrRequestMapping requestMapping2 = method.getAnnotation(ChrRequestMapping.class);

                        String methodPath = requestMapping2.value();//    /query

                        handerMap.put(classPath + methodPath, method);//  map.put(/chr/query,query())
                    }
                }
            }
        }
    }

    private void doAutowired() {
        //遍历ioc容器
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            Object instance = entry.getValue();
            Class<?> clazz = instance.getClass();
            //只有controller和service有autowired，这里用controller演示
            if (clazz.isAnnotationPresent(ChrController.class)) {
                //得到controller里所有的属性
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    if (field.isAnnotationPresent(ChrAutowired.class)) {//如果属性上有autowired注解（只拿该注解的对象）
                        //得到有注解对象的注解
                        ChrAutowired auto = field.getAnnotation(ChrAutowired.class);
                        //得到auto注解的值
                        String key = auto.value();//myServiceImpl
//                        Object value = beans.get(key);//从iocMap中拿取key对应的对象，这里和下面的 field.set(instance, value);是一起的，一个道理
                        //破封装
                        field.setAccessible(true);
                        try {
                            //在MyController类中注入对象MyService类的对象myService
                            field.set(instance, beans.get(key));
//                            field.set(instance, value);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    } else {
                        continue;
                    }
                }
            } else {
                continue;
            }
        }
    }

    /**
     * 只把有@Service，@Controller注解的类实例化
     */
    private void doInstance() {
        for (String className : classNames) {//com.lnsoft.MyController.class
            String cn = className.replace(".class", "");//com.lnsoft.MyController
            //实例化
            try {
                Class<?> clazz = Class.forName(cn);//com.lnsoft.MyController
                if (clazz.isAnnotationPresent(ChrController.class)) {//判断这个类是不是ChrController类，是就是注解类
                    Object value1 = clazz.newInstance();//实例化对象，对象就是map的value
                    //************************************************************************************
                    ChrRequestMapping reqMap1 = clazz.getAnnotation(ChrRequestMapping.class);//这是模拟视频，用类上的@RequestMapping的value作为iocMap中的MyController对象的的key
//                    ChrController con = clazz.getAnnotation(ChrController.class);//得到注解后面的value，这是自己的方法
                    String key1 = reqMap1.value();//myController或/chr
                    beans.put(key1, value1);//自己：beans.put("myController",new MyController());  或 视频：beans.put("/chr",new MyController());这里用后一个测试，视频里的方法
                    //************************************************************************************
                } else if (clazz.isAnnotationPresent(ChrService.class)) {//判断这个类是不是ChrService类
                    Object value2 = clazz.newInstance();
                    ChrService ser = clazz.getAnnotation(ChrService.class);
                    String key2 = ser.value();//注解后面的括号的值
                    beans.put(key2, value2);
                } else {
                    continue;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void doScanPackage(String basePackage) {//com.lnsoft
        //扫描类的路径：G:/WorkSpace/apache-tomcat-7.0.69/ChrSpringMVC/com/lnsoft
        URL url = this.getClass().getClassLoader().getResource("/" + basePackage.replace(".", "/"));
        System.err.println(url);
        String fileStr = url.getFile(); //G:/WorkSpace/apache-tomcat-7.0.69/ChrSpringMVC/com/lnsoft
        File file = new File(fileStr);  //获得该文件路径

        //得到com/lnsoft的路径下的文件（文件夹，文件）
        String[] filesStr = file.list();
        //遍历该路径下的文件（文件夹，文件）
        for (String path : filesStr) {
            //该File可能是文件，也可能是路径
            File filePath = new File(fileStr + path);//lnsoft/controller等文件路径 或 lnsoft/controller.class等class文件

            if (filePath.isDirectory()) {//是文件夹，递归
                doScanPackage(basePackage + "." + path);
            } else {//是class文件
                classNames.add(basePackage + "." + filePath.getName());//  add(com.lnsoft.MyController.class)
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request, response);
    }

    //http://ip:port/ChrSpringMVC/chr/query
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //获得请求路径:工程名+请求路径
        String uri = request.getRequestURI();//    /ChrSpringMVC/chr/query
        System.err.println(uri);
        //拿到工程名
        String context = request.getContextPath();  //    /ChrSpringMVC
        //切除工程名，得到请求路径
        String path = uri.replace(context, "");//    /chr/query     ，该路径就是handlerMap中的key，可以得到对应执行的方法
        //得到拦截路径对应需要执行的方法
        Method method = (Method) handerMap.get(path);//   query()
//************************************************************************************
        //修改123行，这是得到类的对象，之前是用类上RequestMapping的值为类的对象，这里用RequestMapping的值做测试
        MyController instance = (MyController) beans.get("/" + path.split("/")[1]); //beans.get("/chr/query");得到new MyController()
        //Object instance=beans.get()得到该controller类的对象
//************************************************************************************
        //获得映射的方法上的参数
        Object[] args = hand(request, response, method);

        for (int x = 0; x < args.length; x++) {
            System.err.println(args[x]);
        }
        try {
            System.err.println(instance);
            //执行路径映射的方法
            method.invoke(instance, args);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    //这不是策略模式
    private static Object[] hand(HttpServletRequest request, HttpServletResponse response, Method method) {
        //拿到当前待执行方法有哪些参数
        Class<?>[] paramClazzs = method.getParameterTypes();
        //根据参数的个数，new 一个参数数组，将方法里的所有参数赋值到args来
        Object[] args = new Object[paramClazzs.length];

        int args_i = 0;
        int index = 0;
        for (Class<?> paramClazz : paramClazzs) {
            if (ServletRequest.class.isAssignableFrom(paramClazz)) {
                args[args_i++] = request;
            }
            if (ServletResponse.class.isAssignableFrom(paramClazz)) {
                args[args_i++] = response;
            }
            //从0-3判断有没有RequestParam注解，很明显paramClazz为0和1时，不是，
            //当为2和3时，@RequestParam，需要解析
            //[@com.enjoy.james.annotation.EnjoyRequestParam(value=name)]
            Annotation[] paramAns = method.getParameterAnnotations()[index];
            if (paramAns.length > 0) {
                for (Annotation paramAn : paramAns) {
                    if (ChrRequestParam.class.isAssignableFrom(paramAn.getClass())) {
                        ChrRequestParam rp = (ChrRequestParam) paramAn;
                        //找到注解里的name和age
                        args[args_i++] = request.getParameter(rp.value());
                    }
                }
            }
            index++;
        }
        return args;
    }
}
