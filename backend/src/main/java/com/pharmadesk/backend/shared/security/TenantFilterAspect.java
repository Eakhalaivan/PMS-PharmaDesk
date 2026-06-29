package com.pharmadesk.backend.shared.security;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class TenantFilterAspect {

    @PersistenceContext
    private EntityManager entityManager;

    @Around("execution(* com.pharmadesk.backend..service..*(..))")
    public Object applyTenantFilter(ProceedingJoinPoint pjp) throws Throwable {
        HttpServletRequest request = null;
        try {
            request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        } catch (Exception e) {
            // Context not available, e.g. scheduled tasks
        }

        if (request != null) {
            Long branchId = (Long) request.getAttribute("branchId");
            if (branchId == null) {
                branchId = 1L;
            }
            Session session = entityManager.unwrap(Session.class);
            session.enableFilter("branchFilter").setParameter("branchId", branchId);
        }

        return pjp.proceed();
    }
}
