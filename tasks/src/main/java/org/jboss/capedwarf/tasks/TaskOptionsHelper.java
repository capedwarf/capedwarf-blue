package org.jboss.capedwarf.tasks;

import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskOptions;
import org.jboss.capedwarf.common.reflection.ReflectionUtils;
import org.jboss.capedwarf.common.reflection.TargetInvocation;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class TaskOptionsHelper {

    private static final TargetInvocation<TaskOptions.Method> getMethod = ReflectionUtils.cacheInvocation(TaskOptions.class, "getMethod");
    private static final TargetInvocation<String> getTaskName = ReflectionUtils.cacheInvocation(TaskOptions.class, "getTaskName");
    private static final TargetInvocation<Long> getEtaMillis = ReflectionUtils.cacheInvocation(TaskOptions.class, "getEtaMillis");
    private static final TargetInvocation<RetryOptions> getRetryOptions = ReflectionUtils.cacheInvocation(TaskOptions.class, "getRetryOptions");

    private static final TargetInvocation<Integer> getTaskRetryLimit = ReflectionUtils.cacheInvocation(RetryOptions.class, "getTaskRetryLimit");

    private TaskOptions taskOptions;

    public TaskOptionsHelper(TaskOptions taskOptions) {
        this.taskOptions = taskOptions;
    }

    public RetryOptions getRetryOptions() {
        try {
            return getRetryOptions.invoke(taskOptions);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Integer getTaskRetryLimit() {
        RetryOptions retryOptions = getRetryOptions();
        if (retryOptions == null) {
            return null;
        }
        try {
            return getTaskRetryLimit.invoke(retryOptions);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Long getEtaMillis() {
        try {
            return getEtaMillis.invoke(taskOptions);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getTaskName() {
        try {
            return getTaskName.invoke(taskOptions);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public TaskOptions.Method getMethod() {
        try {
            return getMethod.invoke(taskOptions);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
