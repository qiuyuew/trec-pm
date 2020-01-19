package de.julielab.ir.experiments.ablation;

import at.medunigraz.imi.bst.config.TrecConfig;
import de.julielab.ir.Multithreading;
import de.julielab.java.utilities.cache.CacheAccess;
import de.julielab.java.utilities.cache.CacheServer;
import de.julielab.java.utilities.cache.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static de.julielab.ir.paramopt.HttpParamOptClient.requestScoreFromServer;

public class AblationExperiments {

    private final static Logger log = LoggerFactory.getLogger(AblationExperiments.class);

    private CacheAccess<CacheKey, AblationComparisonPair> cache;

    public AblationExperiments() {
        cache = CacheService.getInstance().getCacheAccess("AblationCache", "AblatinoPairs", CacheAccess.JAVA, CacheAccess.JAVA);
    }

    /**
     * Issues two searches: One with the given <tt>referenceParameters</tt> and one where part of the <tt>referenceParameters</tt> is overridden with <tt>ablationOverrides</tt> for deactivate features or set some parameters to neutral values.
     *
     * @param ablationName
     * @param instance            The name of the SMAC-like instance to get the performance for (e.g. pm-split0-train)
     * @param indexSuffix         The name suffix of the index copy to use. May be the empty string if there are no index copies.
     * @param endpoint            The evaluation server endpoint to BA or CT, one of {@link de.julielab.ir.paramopt.HttpParamOptServer#GET_CONFIG_SCORE_CT} and  {@link de.julielab.ir.paramopt.HttpParamOptServer#GET_CONFIG_SCORE_PM}.
     * @param metricsToReturn
     * @param referenceParameters The search-template parameters of the reference against which ablation experiments are performed.
     * @param ablationOverrides   Parameters that will be used to override parameters in <tt>referenceParameters</tt> for the ablation.
     * @return A <tt>ComparisonPair</tt> containing the two run scores.
     * @throws IOException
     */
    private AblationComparisonPair getAblationComparison(String ablationName, String instance, String indexSuffix, String endpoint, String metricsToReturn, Map<String, String> referenceParameters, Map<String, String> ablationOverrides) throws IOException {
        double[] referenceScores = requestScoreFromServer(referenceParameters, instance, indexSuffix, endpoint, metricsToReturn);
        Map<String, String> ablationParameters = new HashMap<>(referenceParameters);
        ablationParameters.putAll(ablationOverrides);
        double[] ablationScores = requestScoreFromServer(ablationParameters, instance, indexSuffix, endpoint, metricsToReturn);
        return new AblationComparisonPair(ablationName, metricsToReturn, referenceScores, ablationScores);
    }

    /**
     * @param ablationParameterMaps Either one parameter set for each crossval split (commonly used for bottom-up ablation) or one single parameter set to be applied for all splits (for top-down ablation).
     * @param referenceParameters   The reference parameter set for each split (in top-down ablation) or a single reference parameter set (for the bottom-up baseline).
     * @param instances
     * @param indexSuffixes
     * @param metricsToReturn
     * @param endpoint
     * @return
     * @throws IOException
     */
    public Map<String, AblationCrossValResult> getAblationCrossValResult(List<Map<String, Map<String, String>>> ablationParameterMaps, List<Map<String, String>> referenceParameters, List<String> instances, List<String> indexSuffixes, String metricsToReturn, String endpoint) throws IOException {
        Map<String, AblationCrossValResult> ablationResult = new LinkedHashMap<>();
        List<Future<?>> jobs = new ArrayList<>();
        // For each cross val split...
        for (int i = 0; i < instances.size(); i++) {
            final int finalI = i;
            Future<?> future = Multithreading.getInstance().submit(() -> {
                try {
                    Map<String, Map<String, String>> ablationParameterMap = ablationParameterMaps.size() > 1 ? ablationParameterMaps.get(finalI) : ablationParameterMaps.get(0);
                    // Apply each ablation group
                    for (String ablationGroupName : ablationParameterMap.keySet()) {
                        String instance = instances.get(finalI);
                        String indexSuffix = indexSuffixes.get(finalI);
                        Map<String, String> referenceParametersForThisSplit = referenceParameters.size() > 1 ? referenceParameters.get(finalI) : referenceParameters.get(0);
                        CacheKey cacheKey = new CacheKey(ablationGroupName, instance, indexSuffix, endpoint, metricsToReturn, referenceParametersForThisSplit, ablationParameterMap.get(ablationGroupName));
                        AblationComparisonPair comparison = cache.get(cacheKey);
                        if (comparison == null) {
                            comparison = getAblationComparison(ablationGroupName, instance, indexSuffix, endpoint, metricsToReturn, referenceParametersForThisSplit, ablationParameterMap.get(ablationGroupName));
                            cache.put(cacheKey, comparison);
                        }
                        // Get the cross val result object for the current group and add the result for this cross val split
                        ablationResult.compute(ablationGroupName, (k, v) -> v == null ? new AblationCrossValResult(ablationGroupName) : v).add(comparison);
                        log.debug("Finished ablation group {} on instance {}", ablationGroupName, instance);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            jobs.add(future);
        }
        // Wait for the jobs to finish
        for (Future job : jobs) {
            try {
                job.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        return ablationResult;
    }

    private class CacheKey implements Serializable {
        private String ablationGroupName;
        private String instance;
        private String indexSuffix;
        private String endpoint;
        private String metricsToReturn;
        private Map<String, String> referenceParameters;
        private Map<String, String> ablationParameters;

        public CacheKey(String ablationGroupName, String instance, String indexSuffix, String endpoint, String metricsToReturn, Map<String, String> referenceParameters, Map<String, String> ablationParameters) {
            this.ablationGroupName = ablationGroupName;
            this.instance = instance;
            this.indexSuffix = indexSuffix;
            this.endpoint = endpoint;
            this.metricsToReturn = metricsToReturn;
            this.referenceParameters = referenceParameters;
            this.ablationParameters = ablationParameters;
        }
    }

}
