package com.fly.serverless.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.net.URLClassLoader;

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

    @PostMapping("api")
    public void loadApiClass(@RequestParam MultipartFile classFile) {

        MyController controller = new MyController();

        Method[] methods = controller.getClass().getDeclaredMethods();
        for (Method method : methods) {
            registerMethod(controller, method);
        }
    }

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
