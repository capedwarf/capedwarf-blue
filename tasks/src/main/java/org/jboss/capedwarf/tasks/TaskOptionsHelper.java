package org.jboss.capedwarf.tasks;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

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
    private static final TargetInvocation<Long> getCountdownMillis = ReflectionUtils.cacheInvocation(TaskOptions.class, "getCountdownMillis");
    private static final TargetInvocation<RetryOptions> getRetryOptions = ReflectionUtils.cacheInvocation(TaskOptions.class, "getRetryOptions");
    private static final TargetInvocation<byte[]> getPayload = ReflectionUtils.cacheInvocation(TaskOptions.class, "getPayload");
    private static final TargetInvocation<List<Object>> getParams = ReflectionUtils.cacheInvocation(TaskOptions.class, "getParams");
    private static final TargetInvocation<HashMap<String, List<String>>> getHeaders = ReflectionUtils.cacheInvocation(TaskOptions.class, "getHeaders");

    private static final TargetInvocation<Integer> getTaskRetryLimit = ReflectionUtils.cacheInvocation(RetryOptions.class, "getTaskRetryLimit");
    private static final TargetInvocation<Boolean> supportsBody = ReflectionUtils.cacheInvocation(TaskOptions.Method.class, "supportsBody");

    private TaskOptions taskOptions;

    public TaskOptionsHelper(TaskOptions taskOptions) {
        this.taskOptions = taskOptions;
    }

    public RetryOptions getRetryOptions() {
        return invoke(taskOptions, getRetryOptions);
    }

    public Integer getTaskRetryLimit() {
        RetryOptions retryOptions = getRetryOptions();
        if (retryOptions == null) {
            return null;
        }
        return invoke(retryOptions, getTaskRetryLimit);
    }

    public Long getEtaMillis() {
        return invoke(taskOptions, getEtaMillis);
    }

    public Long getCountdownMillis() {
        return invoke(taskOptions, getCountdownMillis);
    }

    public String getTaskName() {
        return invoke(taskOptions, getTaskName);
    }

    public TaskOptions.Method getMethod() {
        return invoke(taskOptions, getMethod);
    }

    public byte[] getPayload() {
        return invoke(taskOptions, getPayload);
    }

    public List<Object> getParams() {
        return invoke(taskOptions, getParams);
    }

    public HashMap<String, List<String>> getHeaders() {
        return invoke(taskOptions, getHeaders);
    }

    private <T> T invoke(Object target, TargetInvocation<T> targetInvocation) {
        try {
            return targetInvocation.invoke(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getUrl() {
        return taskOptions.getUrl();
    }

    public String getTag() throws UnsupportedEncodingException {
        return taskOptions.getTag();
    }

    public byte[] getTagAsBytes() {
        return taskOptions.getTagAsBytes();
    }

    public TaskOptions getTaskOptions() {
        return taskOptions;
    }

    public boolean isPayloadAllowed() {
        return invoke(getMethod(), supportsBody);
    }
}
