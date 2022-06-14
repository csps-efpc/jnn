package com.handshape.justneuralnets.repo;

import com.handshape.justneuralnets.JNNModelSpec;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author jturner
 */
public class TieredFallbackMetaRepository implements IJnnRepository {

    private final List<IJnnRepository> repos;
    // We'll need to figure out how to manage this -- this counts as a shared secret.
    // The current SAS is scheduled to expire April 2, 2030.
    private String connectionString = "BAD SECRET";
    private String containerName = "dev";

    public TieredFallbackMetaRepository(IJnnRepository... repos) {
        this(Arrays.asList(repos));
    }

    public TieredFallbackMetaRepository(List<IJnnRepository> repos) {
        this.repos = Collections.unmodifiableList(new ArrayList(repos));
    }

    @Override
    public String put(byte[] byteArray) throws IOException {
        String returnable = null;
        for (IJnnRepository repo : repos) {
            String localId = repo.put(byteArray);
            if (localId != null) {
                if (returnable != null && !returnable.equals(localId)) {
                    throw new IOException("Repositories disagree about secure IDs.");
                }
                returnable = localId;
            }
        }
        return returnable;
    }

    @Override
    public String put(MultiLayerNetwork network) throws IOException {
        String returnable = null;
        for (IJnnRepository repo : repos) {
            String localId = repo.put(network);
            if (localId != null) {
                if (returnable != null && !returnable.equals(localId)) {
                    throw new IOException("Repositories disagree about secure IDs.");
                }
                returnable = localId;
            }
        }
        return returnable;
    }

    @Override
    public String put(JNNModelSpec spec) throws IOException {
        String returnable = null;
        for (IJnnRepository repo : repos) {
            String localId = repo.put(spec);
            if (localId != null) {
                if (returnable != null && !returnable.equals(localId)) {
                    throw new IOException("Repositories disagree about secure IDs.");
                }
                returnable = localId;
            }
        }
        return returnable;
    }

    @Override
    public MultiLayerNetwork getMultiLayerNetwork(String key) throws IOException {
        List<IJnnRepository> misses = new ArrayList<>();
        MultiLayerNetwork returnable = null;
        for (IJnnRepository repo : repos) {
            if (returnable == null) {
                MultiLayerNetwork localFetch = repo.getMultiLayerNetwork(key);
                if (localFetch == null) {
                    // Repo miss, add the repo to the candidate list.
                    misses.add(repo);
                } else {
                    returnable = localFetch;
                }
            }
        }
        if (returnable != null && !misses.isEmpty()) {
            // Propagate the retrieved model in.
            for (IJnnRepository repo : misses) {
                repo.put(returnable);
            }
        }
        return returnable;
    }

    @Override
    public JNNModelSpec getModelSpec(String key) throws IOException {
        List<IJnnRepository> misses = new ArrayList<>();
        JNNModelSpec returnable = null;
        for (IJnnRepository repo : repos) {
            if (returnable == null) {
                JNNModelSpec localFetch = repo.getModelSpec(key);
                if (localFetch == null) {
                    // Repo miss, add the repo to the candidate list.
                    misses.add(repo);
                } else {
                    returnable = localFetch;
                }
            }
        }
        if (returnable != null && !misses.isEmpty()) {
            // Propagate the retrieved model in.
            for (IJnnRepository repo : misses) {
                repo.put(returnable);
            }
        }
        return returnable;
    }

    @Override
    public byte[] getBytes(String key) throws IOException {
        List<IJnnRepository> misses = new ArrayList<>();
        byte[] returnable = null;
        for (IJnnRepository repo : repos) {
            if (returnable == null) {
                byte[] localFetch = repo.getBytes(key);
                if (localFetch == null) {
                    // Repo miss, add the repo to the candidate list.
                    misses.add(repo);
                } else {
                    returnable = localFetch;
                }
            }
        }
        if (returnable != null && !misses.isEmpty()) {
            // Propagate the retrieved model in.
            for (IJnnRepository repo : misses) {
                repo.put(returnable);
            }
        }
        return returnable;
    }
}
