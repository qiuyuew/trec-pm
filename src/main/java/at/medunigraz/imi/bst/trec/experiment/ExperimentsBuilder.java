package at.medunigraz.imi.bst.trec.experiment;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import at.medunigraz.imi.bst.trec.model.Gene;
import at.medunigraz.imi.bst.trec.query.ElasticSearchQuery;
import at.medunigraz.imi.bst.trec.query.GeneExpanderQueryDecorator;
import at.medunigraz.imi.bst.trec.query.Query;
import at.medunigraz.imi.bst.trec.query.TemplateQueryDecorator;
import at.medunigraz.imi.bst.trec.query.WordRemovalQueryDecorator;

public class ExperimentsBuilder {

	private Set<Experiment> experiments = new HashSet<>();

	private Experiment buildingExp = null;

	public ExperimentsBuilder() {
	}

	public ExperimentsBuilder newExperiment() {
		validate();
		buildingExp = new Experiment();
		return this;
	}

	public ExperimentsBuilder newExperiment(Experiment base) {
		buildingExp = new Experiment();
		buildingExp.setExperimentId(base.getExperimentId());
		buildingExp.setDecorator(base.getDecorator());
		return this;
	}

	@Deprecated
	public ExperimentsBuilder withId(String id) {
		buildingExp.setExperimentId(id);
		return this;
	}

	@Deprecated
	public ExperimentsBuilder withDecorator(Query decorator) {
		buildingExp.setDecorator(decorator);
		return this;
	}

	public ExperimentsBuilder withTemplate(File template) {
		Query previousDecorator = buildingExp.getDecorator();
		buildingExp.setDecorator(new TemplateQueryDecorator(template, previousDecorator));
		return this;
	}

	public ExperimentsBuilder withWordRemoval() {
		Query previousDecorator = buildingExp.getDecorator();
		buildingExp.setDecorator(new WordRemovalQueryDecorator(previousDecorator));
		return this;
	}

	public ExperimentsBuilder withGeneExpansion(Gene.Field[] expandTo) {
		Query previousDecorator = buildingExp.getDecorator();
		buildingExp.setDecorator(new GeneExpanderQueryDecorator(expandTo, previousDecorator));
		return this;
	}

	public ExperimentsBuilder withGoldStandard(Experiment.GoldStandard gold) {
		buildingExp.setGoldStandard(gold);
		return this;
	}

	public ExperimentsBuilder withTarget(Experiment.Task task) {
		buildingExp.setTask(task);
		buildingExp.setDecorator(new ElasticSearchQuery(buildingExp.getTaskName()));
		return this;
	}

	public Set<Experiment> build() {
		validate();
		return experiments;
	}

	private void validate() {
		if (buildingExp != null) {
			this.experiments.add(buildingExp);
			return;
		}

	}
}
