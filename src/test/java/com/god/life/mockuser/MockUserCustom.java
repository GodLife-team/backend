package com.god.life.mockuser;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = MockCustomUserSecurityContextFactory.class)
public @interface MockUserCustom {

    String memberId() default "1";

    String role() default "ROLE_USER";


}
