package org.telegram.tutorbot.proxy;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.tutorbot.bot.Bot;
import org.telegram.tutorbot.model.User;
import org.telegram.tutorbot.model.enums.Action;
import org.telegram.tutorbot.model.enums.Role;
import org.telegram.tutorbot.repository.UserRepository;
import org.telegram.tutorbot.service.manager.impl.AuthManager;


@Aspect
@Order(100)
@Component
public class AuthAspect {
    private final UserRepository userRepository;
    private final AuthManager authManager;

    @Autowired
    public AuthAspect(UserRepository userRepository, AuthManager authManager) {
        this.userRepository = userRepository;
        this.authManager = authManager;
    }

    @Pointcut("execution(* org.telegram.tutorbot.service.UpdateDispatcher.distribute(..))")
    public void distributeMethodPointcut() {
    }

    @Around("distributeMethodPointcut()")
    public Object authMethodAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        Update update = (Update) joinPoint.getArgs()[0];
        User user;

        if (update.hasMessage()) {
            user = userRepository
                    .findById(update.getMessage().getChatId())
                    .orElseThrow();
        } else if (update.hasCallbackQuery()) {
            user = userRepository
                    .findById(update.getCallbackQuery().getMessage().getChatId())
                    .orElseThrow();
        } else {
            return joinPoint.proceed();
        }

        if (user.getRole() != Role.EMPTY || user.getAction() == Action.AUTH) {
            return joinPoint.proceed();
        }

        return authManager.answerMessage(update.getMessage(), (Bot) joinPoint.getArgs()[1]);
    }
}