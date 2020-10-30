package com.aman.fileupload.controller;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class FileUploadController {
    final Logger log = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/upload", method = RequestMethod.PUT, consumes = {"multipart/form-data"})
    public DeferredResult<ResponseEntity> upload(HttpServletRequest request, HttpServletResponse response, @RequestParam("file") MultipartFile file
    ) throws InterruptedException, ExecutionException {
        log.info("Done");
        DeferredResult<ResponseEntity> result = null;
        ExecutorService exec = Executors.newFixedThreadPool(2);
        try {
            Future future = exec.submit(new Callable<>() {
                @Override
                public DeferredResult<ResponseEntity> call() throws Exception {
                    DeferredResult<ResponseEntity> callResult = new DeferredResult<>();
                    log.info(Thread.currentThread().getName());
                    try (InputStream in = file.getInputStream();
                            OutputStream out = new FileOutputStream("test.dmg")) {
                        log.info("copying file...");
                        FileCopyUtils.copy(in, out);
                    } catch (IOException ex) {
                        log.info("exception: ", ex);
                    }
                    callResult.setResult(ResponseEntity.ok("File uploaded successfully\n"));
                    return callResult;
                }
            });
            while (true) {
                if (future.isDone()) {
                    result = (DeferredResult<ResponseEntity>) future.get();
                    break;
                }
            }
        } catch (Exception ex) {
            System.out.println("Exception: " + ex);
        } finally {
            if (!exec.isShutdown()) {
                exec.shutdown();
            }
        }

//        if(request.getHeader("Expect") != null && request.getHeader("Expect").equals("100-continue")) {
//            response.setHeader("Location", "http://localhost:8080/upload");
//            return new ResponseEntity(HttpStatus.CONTINUE); 
//        } 
//        else {
        log.info("****200 ok");
        if (result == null) {
            result.setResult(ResponseEntity.ok("Null success\n"));
        }
        return result;
//        }
    }

    @RequestMapping(value = "/callable", method = RequestMethod.GET)
    public Callable<ResponseEntity<?>> timeCallable() {
        log.info("Callable time request");
        return new Callable<ResponseEntity<?>>() {
            @Override
            public ResponseEntity<?> call() throws Exception {
                log.info("Inside callable");
                return ResponseEntity.ok("success");
            }
        };
    }

    @RequestMapping(value = "/file/upload", method = RequestMethod.PUT, consumes = {"multipart/form-data"})
    public Callable<ResponseEntity<?>> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("Callable time request");
        Callable<ResponseEntity<?>> response = new Callable<ResponseEntity<?>>() {
            @Override
            public ResponseEntity<?> call() throws Exception {
                log.info("Inside callable");
                try (InputStream in = file.getInputStream();
                        OutputStream out = new FileOutputStream("test.dmg")) {
                    log.info("copying file...");
                    FileCopyUtils.copy(in, out);
                } catch (IOException ex) {
                    log.info("exception: ", ex);
                }
                return ResponseEntity.ok("success");
            }
        };
        log.info("Processing done");
        return response;
    }
    
    @RequestMapping(value = "/test", method = RequestMethod.HEAD)
    public void testHead204Response(HttpServletResponse response) {
        response.setStatus(200);
    }
}
