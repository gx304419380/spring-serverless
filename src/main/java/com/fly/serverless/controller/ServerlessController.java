package com.fly.serverless.controller;

import com.fly.serverless.jdbc.Jdbc;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.Objects.isNull;


/**
 * @author guoxiang
 * @version 1.0.0
 * @since 2021/6/18
 */
@RestController
@RequestMapping("serverless")
@AllArgsConstructor
@Slf4j
public class ServerlessController {

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    private final JdbcTemplate jdbcTemplate;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @PostMapping("sql")
    @Transactional(rollbackFor = Exception.class)
    public void executeSql(@RequestParam String sql) {

        log.info("sql = {}", sql);

        List<LocalDateTime> timeList = Jdbc.selectList("select time from tb_test", LocalDateTime.class, null);

        System.out.println(timeList);

    }

    /**
     * 删除对应的url
     * @param url   url
     */
    @DeleteMapping("api")
    public void deleteApi(@RequestParam String url) {

    }


    @PostMapping("class")
    public void loadApiClass(@RequestPart MultipartFile file) throws IOException, CannotCompileException, IllegalAccessException, InstantiationException {

        String filename = file.getOriginalFilename();
        long size = file.getSize();

        log.info("- receive class file: {}, size: {}", filename, size);

        Class<?> controllerClass = getClassByFile(file);

        Object controller = controllerClass.newInstance();

        Method[] methods = controller.getClass().getDeclaredMethods();
        for (Method method : methods) {
            registerMethod(controller, method);
        }
    }

    /**
     * 将传入的class文件转为java Class
     *
     * @param file  上传的class文件
     * @return      Class
     * @throws IOException              IOException
     * @throws CannotCompileException   CannotCompileException
     */
    private Class<?> getClassByFile(MultipartFile file) throws IOException, CannotCompileException {
        ClassPool pool = ClassPool.getDefault();

        CtClass ctClass = pool.makeClass(file.getInputStream());

        ClassLoader classLoader = getClass().getClassLoader();
        ProtectionDomain protectionDomain = getClass().getProtectionDomain();

        return ctClass.toClass(classLoader, protectionDomain);
    }

    /**
     * 将方法注册到spring mvc的处理器映射器中
     * @param controller    controller
     * @param method        方法
     */
    private void registerMethod(Object controller, Method method) {

        RequestMapping requestMapping =
                AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);

        if (isNull(requestMapping)) {
            return;
        }

        RequestMapping controllerRequestMapping =
                AnnotatedElementUtils.findMergedAnnotation(controller.getClass(), RequestMapping.class);

        RequestMappingInfo mappingInfo = getMappingInfo(requestMapping, controllerRequestMapping);

        requestMappingHandlerMapping.registerMapping(mappingInfo, controller, method);
    }


    /**
     * get method mapping info
     *
     * @param requestMapping        requestMapping
     * @param controllerMapping     controllerMapping
     * @return  RequestMappingInfo
     */
    private RequestMappingInfo getMappingInfo(RequestMapping requestMapping, RequestMapping controllerMapping) {

        RequestMappingInfo methodMappingInfo = RequestMappingInfo
                .paths(requestMapping.path())
                .methods(requestMapping.method())
                .params(requestMapping.params())
                .headers(requestMapping.headers())
                .consumes(requestMapping.consumes())
                .produces(requestMapping.produces())
                .mappingName(requestMapping.name())
                .build();

        if (controllerMapping == null) {
            return methodMappingInfo;
        }

        RequestMappingInfo controllerMappingInfo = RequestMappingInfo
                .paths(controllerMapping.path())
                .methods(controllerMapping.method())
                .params(controllerMapping.params())
                .headers(controllerMapping.headers())
                .consumes(controllerMapping.consumes())
                .produces(controllerMapping.produces())
                .mappingName(controllerMapping.name())
                .build();

        return controllerMappingInfo.combine(methodMappingInfo);
    }

}
