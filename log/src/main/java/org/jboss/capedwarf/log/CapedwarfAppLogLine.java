package org.jboss.capedwarf.log;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.google.appengine.api.log.AppLogLine;
import com.google.appengine.api.log.LogService;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.NumericField;
import org.hibernate.search.annotations.ProvidedId;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@Indexed
@ProvidedId
public class CapedwarfAppLogLine extends CapedwarfLogElement implements Externalizable {

    public static final String TIME_USEC = "timeUsec";
    public static final String SEQUENCE_NUMBER = "sequenceNumber";

    private String requestId;
    private long sequenceNumber;
    private AppLogLine appLogLine = new AppLogLine();

    public CapedwarfAppLogLine() {
    }

    public CapedwarfAppLogLine(String requestId, long sequenceNumber) {
        this.requestId = requestId;
        this.sequenceNumber = sequenceNumber;
    }

    public String getRequestId() {
        return requestId;
    }

    public AppLogLine getAppLogLine() {
        return appLogLine;
    }

    @NumericField
    @Field(name = TIME_USEC)
    public long getTimeUsec() {
        return appLogLine.getTimeUsec();
    }

    @NumericField
    @Field(name = SEQUENCE_NUMBER)
    public long getSequenceNumber() {
        return sequenceNumber;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(1);    // version
        out.writeUTF(requestId);
        out.writeLong(sequenceNumber);
        out.writeInt(appLogLine.getLogLevel().ordinal());
        out.writeUTF(appLogLine.getLogMessage());
        out.writeLong(appLogLine.getTimeUsec());
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int version = in.readInt();
        if (version == 1) {
            requestId = in.readUTF();
            sequenceNumber = in.readLong();
            appLogLine.setLogLevel(LogService.LogLevel.values()[in.readInt()]);
            appLogLine.setLogMessage(in.readUTF());
            appLogLine.setTimeUsec(in.readLong());
        } else {
            throw new IOException("Unsupported version " + version);
        }
    }
}
