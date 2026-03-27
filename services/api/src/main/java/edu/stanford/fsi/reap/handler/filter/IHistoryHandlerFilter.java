package edu.stanford.fsi.reap.handler.filter;

public interface IHistoryHandlerFilter {

  /**
   * 记录更新信息
   *
   * @param target
   */
  public void recordUpdateHistory(Class repository, Object target);

  /**
   * 记录删除信息
   *
   * @param repository
   * @param id
   */
  public void recordDelHistory(Class repository, Long id);
}
