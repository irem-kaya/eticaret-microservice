package com.eticaret.product.aop;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Merkezi loglama aspect'i.
 * Tüm servis metodlarına otomatik olarak:
 *   - Giriş/çıkış logları
 *   - Execution süresi
 *   - Exception loglama
 * ekler. @Slf4j ile tek tek yazmaktan kurtarır.
 */
@Aspect
@Component
public class LoggingAspect {

    // ── Service katmanı loglama ────────────────────────────────────────────────

    @Around("execution(* com.eticaret..service.*Service.*(..))")
    public Object logServiceMethod(ProceedingJoinPoint jp) throws Throwable {
        Logger log = LoggerFactory.getLogger(jp.getTarget().getClass());

        String method = jp.getSignature().getName();
        String args   = Arrays.toString(jp.getArgs());

        log.debug("→ {}{}", method, args.length() > 100 ? "(args...)" : args);

        long start = System.currentTimeMillis();
        try {
            Object result = jp.proceed();
            long elapsed = System.currentTimeMillis() - start;
            log.debug("← {} tamamlandı ({}ms)", method, elapsed);
            return result;
        } catch (Exception e) {
            log.error("✗ {} HATA: {}", method, e.getMessage());
            throw e;
        }
    }

    // ── Controller katmanı loglama ────────────────────────────────────────────

    @Around("execution(* com.eticaret..controller.*Controller.*(..))" +
            " || execution(* com.eticaret..domain.*Controller.*(..))")
    public Object logControllerMethod(ProceedingJoinPoint jp) throws Throwable {
        Logger log = LoggerFactory.getLogger(jp.getTarget().getClass());

        String method = jp.getSignature().getName();
        long start = System.currentTimeMillis();

        try {
            Object result = jp.proceed();
            long elapsed = System.currentTimeMillis() - start;
            log.info("API {} → {}ms", method, elapsed);
            return result;
        } catch (Exception e) {
            log.error("API {} HATA: {}", method, e.getMessage());
            throw e;
        }
    }

    // ── RabbitMQ listener loglama ─────────────────────────────────────────────

    @Before("@annotation(org.springframework.amqp.rabbit.annotation.RabbitListener)")
    public void logRabbitListener(JoinPoint jp) {
        Logger log = LoggerFactory.getLogger(jp.getTarget().getClass());
        log.debug("🐇 RabbitMQ mesaj alındı: {}", jp.getSignature().getName());
    }

    // ── Exception loglama ─────────────────────────────────────────────────────

    @AfterThrowing(
            pointcut = "execution(* com.eticaret..*(..))",
            throwing  = "ex"
    )
    public void logException(JoinPoint jp, Exception ex) {
        Logger log = LoggerFactory.getLogger(jp.getTarget().getClass());
        log.error("💥 Exception [{}]: {} → {}",
                jp.getSignature().toShortString(),
                ex.getClass().getSimpleName(),
                ex.getMessage());
    }
}