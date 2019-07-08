package de.julielab.ir.ltr.features.featuregroups;

import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.features.FeatureGroup;

import java.util.function.Consumer;

public class TopicMatchFeatureGroup extends FeatureGroup {
    public static final String MATCH_TOPIC_DISEASE = "MATCH_TOPIC_DISEASE";
    public static final String MATCH_TOPIC_GENE = "MATCH_TOPIC_GENE";
    public static final String MATCH_TOPIC_VARIANT = "MATCH_TOPIC_VARIANT";

    public TopicMatchFeatureGroup() {
        super("TOPIC_MATCH");
    }


    @Override
    protected void addFeatures() {
        addMatchTopicDisease();
        addMatchTopicGene();
        addMatchTopicVariant();
    }

    private void addMatchTopicVariant() {
        Consumer<Instance> featureValueAssigner = instance -> {
            final Token token = (Token) instance.getData();
            final Document document = (Document) instance.getSource();
        };
        addFeature(MATCH_TOPIC_VARIANT, featureValueAssigner);
    }

    private void addMatchTopicGene() {
        Consumer<Instance> featureValueAssigner = instance -> {
            final Token token = (Token) instance.getData();
            final Document document = (Document) instance.getSource();
        };
        addFeature(MATCH_TOPIC_GENE, featureValueAssigner);
    }

    private void addMatchTopicDisease() {
        Consumer<Instance> featureValueAssigner = instance -> {
            final Token token = (Token) instance.getData();
            final Document document = (Document) instance.getSource();
        };
        addFeature(MATCH_TOPIC_DISEASE, featureValueAssigner);
    }
}