package edu.stanford.fsi.reap.handler;

import edu.stanford.fsi.reap.entity.Visit;
import edu.stanford.fsi.reap.entity.VisitHistory;
import edu.stanford.fsi.reap.entity.enumerations.VisitStatus;
import edu.stanford.fsi.reap.handler.filter.UserHistoryHandlerFilter;
import edu.stanford.fsi.reap.repository.VisitHistoryRepository;
import edu.stanford.fsi.reap.repository.VisitRepository;
import java.lang.reflect.Field;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Aspect
@Component
@Slf4j
public class HistoryHandlerFactory {

  @Autowired private UserHistoryHandlerFilter userFilter;

  @Autowired private VisitRepository visitRepository;

  @Autowired private VisitHistoryRepository visitHistoryRepository;

  @Pointcut(
      "execution(*"
          + " edu.stanford.fsi.reap.repository.VisitRepository.deleteByLessonIdAndStatus(java.lang.Long,edu.stanford.fsi.reap.entity.enumerations.VisitStatus))")
  public void delByLessonIdMethod() {}

  @Before(value = "delByLessonIdMethod()")
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void delByLessonIdRecord(JoinPoint joinPoint) {
    try {
      Object[] args = joinPoint.getArgs();
      if (args[0] == null) {
        return;
      }
      Long lessonId = (Long) args[0];
      VisitStatus visitStatus = (VisitStatus) args[1];
      List<Visit> visits = visitRepository.findByLessonIdAndStatus(lessonId, visitStatus);
      if (!CollectionUtils.isEmpty(visits)) {
        visits.forEach(
            visit -> {
              VisitHistory visitHistory = new VisitHistory();
              BeanUtils.copyProperties(visit, visitHistory);
              visitHistory.setHistoryId(visit.getId());
              visitHistoryRepository.save(visitHistory);
            });
      }
    } catch (Exception e) {
      log.error("记录删除操作出现异常！", e);
    }
  }

  @Pointcut(
      "execution(*"
          + " edu.stanford.fsi.reap.repository.VisitRepository.deleteByBabyIdAndStatus(java.lang.Long,edu.stanford.fsi.reap.entity.enumerations.VisitStatus))")
  public void delByBabyIdMethod() {}

  @Before(value = "delByBabyIdMethod()")
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void delByBabyIdRecord(JoinPoint joinPoint) {
    try {
      Object[] args = joinPoint.getArgs();
      if (args[0] == null) {
        return;
      }
      Long babyId = (Long) args[0];
      VisitStatus visitStatus = (VisitStatus) args[1];
      visitRepository
          .findByBabyIdAndStatus(babyId, visitStatus)
          .ifPresent(
              visit -> {
                VisitHistory visitHistory = new VisitHistory();
                BeanUtils.copyProperties(visit, visitHistory);
                visitHistory.setHistoryId(visit.getId());
                visitHistoryRepository.save(visitHistory);
              });
    } catch (Exception e) {
      log.error("记录删除操作出现异常！", e);
    }
  }

  @Pointcut("execution(* edu.stanford.fsi.reap.repository.*.save(*))")
  public void updateMethod() {}

  @Before(value = "updateMethod()")
  public void updateRecord(JoinPoint joinPoint) {
    try {
      // 避免循环处理
      Class curClass = joinPoint.getSignature().getDeclaringType();
      if (curClass.getName().indexOf("History") > 0) {
        return;
      }
      Object[] args = joinPoint.getArgs();
      Object target = args[0];
      try {
        Field idField = target.getClass().getDeclaredField("id");
        idField.setAccessible(true);
        if (idField.get(target) == null) {
          return;
        }
        userFilter.recordUpdateHistory(joinPoint.getSignature().getDeclaringType(), target);
      } catch (NoSuchFieldException e) {
        log.warn("类：{}没有找到对应的主键名", target.getClass().getName());
      }
    } catch (Exception e) {
      log.error("记录更新操作出现异常！", e);
    }
  }

  @Pointcut("execution(* edu.stanford.fsi.reap.repository.*.deleteById(*))")
  public void delIdMethod() {}

  @Before(value = "delIdMethod()")
  public void delIdRecord(JoinPoint joinPoint) {
    try {
      // 避免循环处理
      Class curClass = joinPoint.getSignature().getDeclaringType();
      if (curClass.getName().indexOf("History") > 0) {
        return;
      }
      Object[] args = joinPoint.getArgs();
      Object target = args[0];
      userFilter.recordDelHistory(curClass, (Long) target);
    } catch (Exception e) {
      log.error("记录删除操作出现异常！", e);
    }
  }
}
