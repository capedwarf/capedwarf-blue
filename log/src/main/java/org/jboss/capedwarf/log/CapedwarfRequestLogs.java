package org.jboss.capedwarf.log;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.google.appengine.api.log.AppLogLine;
import com.google.appengine.api.log.LogService;
import com.google.appengine.api.log.RequestLogs;
import com.google.appengine.api.utils.SystemProperty;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.NumericField;
import org.hibernate.search.annotations.ProvidedId;
import org.jboss.capedwarf.shared.reflection.MethodInvocation;
import org.jboss.capedwarf.shared.reflection.ReflectionUtils;
import org.jboss.util.Base64;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@Indexed
@ProvidedId
public class CapedwarfRequestLogs extends CapedwarfLogElement implements Externalizable {
    public static final String END_TIME_USEC = "endTimeUsec";
    public static final String MAX_LOG_LEVEL = "maxLogLevel";
    public static final String FINISHED = "finished";

    private static final int EXTERNALIZER_VERSION = 2;

    private static MethodInvocation<String> getAppEngineRelease = ReflectionUtils.optionalMethod(RequestLogs.class, "getAppEngineRelease");
    private static MethodInvocation<Void> setAppEngineRelease = ReflectionUtils.optionalMethod(RequestLogs.class, "setAppEngineRelease", String.class);

    private RequestLogs requestLogs = new RequestLogs();
    private Integer maxLogLevel;

    public CapedwarfRequestLogs() {
        // TODO -- right values?
        setAppEngineRelease.invokeWithTarget(requestLogs, SystemProperty.version.get());
        requestLogs.setUrlMapEntry("");
        requestLogs.setOffset(Base64.encodeBytes(String.valueOf(System.nanoTime()).getBytes()));
    }

    public RequestLogs getRequestLogs() {
        return requestLogs;
    }

    @NumericField
    @Field(name = MAX_LOG_LEVEL)
    public Integer getMaxLogLevel() {
        return maxLogLevel;
    }

    public void setMaxLogLevel(int maxLogLevel) {
        this.maxLogLevel = maxLogLevel;
    }

    public void logLineAdded(AppLogLine appLogLine) {
        LogService.LogLevel logLevel = appLogLine.getLogLevel();
        if (maxLogLevel == null || logLevel.ordinal() > maxLogLevel) {
            setMaxLogLevel(logLevel.ordinal());
        }
        requestLogs.setEndTimeUsec(appLogLine.getTimeUsec());
    }

    @NumericField
    @Field(name = END_TIME_USEC)
    public long getEndTimeUsec() {
        return requestLogs.getEndTimeUsec();
    }

    @Field(name = FINISHED)
    public boolean isFinished() {
        return requestLogs.isFinished();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(EXTERNALIZER_VERSION);
        out.writeObject(maxLogLevel);
        writeUTF(out, requestLogs.getAppId());
        writeUTF(out, requestLogs.getVersionId());
        writeUTF(out, requestLogs.getRequestId());
        writeUTF(out, requestLogs.getOffset());
        writeUTF(out, requestLogs.getIp());
        writeUTF(out, requestLogs.getNickname());
        out.writeLong(requestLogs.getStartTimeUsec());
        out.writeLong(requestLogs.getEndTimeUsec());
        out.writeLong(requestLogs.getLatencyUsec());
        out.writeLong(requestLogs.getMcycles());
        writeUTF(out, requestLogs.getMethod());
        writeUTF(out, requestLogs.getResource());
        writeUTF(out, requestLogs.getHttpVersion());
        out.writeInt(requestLogs.getStatus());
        out.writeLong(requestLogs.getResponseSize());
        writeUTF(out, requestLogs.getReferrer());
        writeUTF(out, requestLogs.getUserAgent());
        writeUTF(out, requestLogs.getUrlMapEntry());
        writeUTF(out, requestLogs.getCombined());
        out.writeLong(requestLogs.getApiMcycles());
        writeUTF(out, requestLogs.getHost());
        out.writeDouble(requestLogs.getCost());
        writeUTF(out, requestLogs.getTaskQueueName());
        writeUTF(out, requestLogs.getTaskName());
        out.writeBoolean(requestLogs.isLoadingRequest());
        out.writeLong(requestLogs.getPendingTimeUsec());
        out.writeInt(requestLogs.getReplicaIndex());
        out.writeBoolean(requestLogs.isFinished());
        writeUTF(out, requestLogs.getInstanceKey());
        writeUTF(out, getAppEngineRelease.invokeWithTarget(requestLogs));
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int version = in.readInt();
        if (version == EXTERNALIZER_VERSION) {
            maxLogLevel = (Integer) in.readObject();
            requestLogs.setAppId(readUTF(in));
            requestLogs.setVersionId(readUTF(in));
            requestLogs.setRequestId(readUTF(in));
            requestLogs.setOffset(readUTF(in));
            requestLogs.setIp(readUTF(in));
            requestLogs.setNickname(readUTF(in));
            requestLogs.setStartTimeUsec(in.readLong());
            requestLogs.setEndTimeUsec(in.readLong());
            requestLogs.setLatency(in.readLong());
            requestLogs.setMcycles(in.readLong());
            requestLogs.setMethod(readUTF(in));
            requestLogs.setResource(readUTF(in));
            requestLogs.setHttpVersion(readUTF(in));
            requestLogs.setStatus(in.readInt());
            requestLogs.setResponseSize(in.readLong());
            requestLogs.setReferrer(readUTF(in));
            requestLogs.setUserAgent(readUTF(in));
            requestLogs.setUrlMapEntry(readUTF(in));
            requestLogs.setCombined(readUTF(in));
            requestLogs.setApiMcycles(in.readLong());
            requestLogs.setHost(readUTF(in));
            requestLogs.setCost(in.readDouble());
            requestLogs.setTaskQueueName(readUTF(in));
            requestLogs.setTaskName(readUTF(in));
            requestLogs.setWasLoadingRequest(in.readBoolean());
            requestLogs.setPendingTime(in.readLong());
            requestLogs.setReplicaIndex(in.readInt());
            requestLogs.setFinished(in.readBoolean());
            requestLogs.setInstanceKey(readUTF(in));
            setAppEngineRelease.invokeWithTarget(requestLogs, readUTF(in));
        } else {
            throw new IOException("Unsupported version " + version);
        }
    }

    private void writeUTF(ObjectOutput out, String str) throws IOException {
        if (str == null) {
            out.writeBoolean(true);
        } else {
            out.writeBoolean(false);
            out.writeUTF(str);
        }
    }

    private String readUTF(ObjectInput in) throws IOException, ClassNotFoundException {
        boolean isNull = in.readBoolean();
        if (isNull) {
            return null;
        } else {
            return in.readUTF();
        }
    }
}
