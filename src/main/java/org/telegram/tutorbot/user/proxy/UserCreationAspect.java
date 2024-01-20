package org.telegram.tutorbot.user.proxy;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.tutorbot.user.model.UserDetails;
import org.telegram.tutorbot.user.model.enums.Action;
import org.telegram.tutorbot.user.model.enums.Role;
import org.telegram.tutorbot.user.repository.UserDetailsRepository;
import org.telegram.tutorbot.user.repository.UserRepository;
import java.time.LocalDateTime;

@Aspect
@Component
public class UserCreationAspect {
    private final UserRepository userRepository;
    private final UserDetailsRepository userDetailsRepository;

    @Autowired
    public UserCreationAspect(UserRepository userRepository, UserDetailsRepository userDetailsRepository) {
        this.userRepository = userRepository;
        this.userDetailsRepository = userDetailsRepository;
    }

    @Pointcut("execution(* org.telegram.tutorbot.bot.service.UpdateDispatcher.distribute(..))")
    public void distributeMethodPointcut() {}

    @Around("distributeMethodPointcut()")
    public Object distributeMethodAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Update update = (Update) args[0];
        User tgUser;

        if (update.hasMessage()) {
            tgUser = update.getMessage().getFrom();
        } else if (update.hasCallbackQuery()) {
            tgUser = update.getCallbackQuery().getFrom();
        } else {
            return joinPoint.proceed();
        }

        if (userRepository.existsById(tgUser.getId())) {
            return joinPoint.proceed();
        }

        UserDetails userDetails = UserDetails.builder()
                .username(tgUser.getUserName())
                .firstName(tgUser.getFirstName())
                .lastName(tgUser.getLastName())
                .registeredAt(LocalDateTime.now())
                .build();
        userDetailsRepository.save(userDetails);

        org.telegram.tutorbot.user.model.User user = org.telegram.tutorbot.user.model.User.builder()
                .chatId(tgUser.getId())
                .action(Action.FREE)
                .role(Role.USER)
                .userDetails(userDetails)
                .build();
        userRepository.save(user);

        return joinPoint.proceed();
    }
}
