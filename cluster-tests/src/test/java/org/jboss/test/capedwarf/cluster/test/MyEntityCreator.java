package org.jboss.test.capedwarf.cluster.test;

import java.util.List;
import java.util.Random;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.tools.mapreduce.DatastoreMutationPool;
import com.google.appengine.tools.mapreduce.Mapper;

/**
 * @author Matej Lazar
 */
public class MyEntityCreator extends Mapper<Long, Void, Void> {
    private static final long serialVersionUID = 1L;

    private final String kind;
    private final List<String> payloads;
    private final Random random = new Random();

    private transient DatastoreMutationPool pool;

    public MyEntityCreator(String kind, List<String> payloads) {
        if (kind == null)
            throw new IllegalArgumentException("Null kind");

        this.kind = kind;
        this.payloads = payloads;
    }

    @Override
    public void beginSlice() {
        pool = DatastoreMutationPool.create();
    }

    @Override
    public void endSlice() {
        pool.flush();
    }

    @Override
    public void map(Long index) {
        System.out.println("Executing M/R task.");
        String name = String.valueOf(random.nextLong() & Long.MAX_VALUE);
        Entity e = new Entity(kind, name);
        e.setProperty("payload", new Text(payloads.get((int)(index % payloads.size()))));
        pool.put(e);
    }
}
